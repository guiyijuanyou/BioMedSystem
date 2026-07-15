"""
香港浸会大学中药材图像数据库 爬虫
===============================
从 HKBU MMID (https://sys01.lib.hkbu.edu.hk/cmed/mmid/) 抓取中药材数据。
输出文件 (output/) 可直接通过管理后台导入项目。

用法:
    pip install requests beautifulsoup4 opencc-python-reimplemented
    python hkbu_crawler.py

输出的 output/data.json + output/images/ 即为可分享的数据包。
"""

import argparse
import json
import logging
import os
import re
import sys
import time
from pathlib import Path
from urllib.parse import urljoin, urlparse

import requests
from bs4 import BeautifulSoup

# 繁体转简体
try:
    from opencc import OpenCC
    cc = OpenCC("t2s")
except ImportError:
    cc = None
    print("警告: opencc-python 未安装，繁体字不会自动转换。pip install opencc-python-reimplemented")

# ── 配置 ──────────────────────────────────────────────
BASE_URL = "https://sys01.lib.hkbu.edu.hk/cmed/mmid/"
INDEX_URL = urljoin(BASE_URL, "index.php")

OUTPUT_DIR = Path(__file__).parent / "output"
IMAGE_DIR = OUTPUT_DIR / "images"

REQUEST_DELAY = 1.5  # 秒，防被封
TIMEOUT = 30

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler()],
)
log = logging.getLogger(__name__)


# ── 工具函数 ──────────────────────────────────────────
def init_log_file():
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    handler = logging.FileHandler(OUTPUT_DIR / "crawler.log", encoding="utf-8")
    handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(message)s"))
    log.addHandler(handler)


def t2s(text: str | None) -> str | None:
    if text is None:
        return None
    return cc.convert(text) if cc else text


def safe_get(session, url, **kwargs):
    for attempt in range(3):
        try:
            resp = session.get(url, timeout=TIMEOUT, **kwargs)
            resp.raise_for_status()
            return resp
        except requests.RequestException as e:
            log.warning("请求失败 (%s): %s, 重试 %d/3", url, e, attempt + 1)
            time.sleep(REQUEST_DELAY * 2)
    return None


def clean_text(text: str | None) -> str | None:
    if text is None:
        return None
    text = re.sub(r"\s+", " ", text).strip()
    return text if text else None


# ── 获取药材列表 ─────────────────────────────────────
def get_all_herb_links(session) -> list[tuple[str, str, str]]:
    """
    遍历拉丁字母 A-Z（含分页），获取所有药材 (中文名, 拼音, 详情URL)。
    """
    herbs = []
    latin_initials = [chr(i) for i in range(ord("A"), ord("Z") + 1)]

    for initial in latin_initials:
        page = 1
        while True:
            url = (f"{INDEX_URL}?fac_latin={initial}&lang=chs"
                   f"&sort=name_cht&page={page}")
            log.info("浏览拉丁字母 %s 第 %d 页 ...", initial, page)

            resp = safe_get(session, url)
            if resp is None:
                break

            soup = BeautifulSoup(resp.text, "html.parser")
            rows = soup.select("table#list tbody tr")
            if not rows:
                log.info("  %s 页无数据，跳过", initial)
                break

            for row in rows:
                # 详情链接在第一个 td 的 a 标签中
                link_tag = row.select_one("td a[href*='pid=']")
                if not link_tag:
                    continue
                href = link_tag.get("href")
                pid_match = re.search(r'pid=([A-Z]\d+)', href)
                if not pid_match:
                    continue
                pid = pid_match.group(1)
                detail_url = f"{BASE_URL}detail.php?pid={pid}&lang=chs"

                # 中文名 + 拼音在第二个 td（格式如 "白矾 BAifAn"）
                name_td = row.select("td")[1] if len(row.select("td")) > 1 else None
                chinese_name = ""
                pinyin = ""
                if name_td:
                    full_text = name_td.get_text(" ", strip=True)
                    # 分离中文名和拼音（中文在前，拼音在后）
                    m = re.match(r'^([一-鿿豈-﫿]+)\s*(.*)', full_text)
                    if m:
                        chinese_name = m.group(1)
                        pinyin = m.group(2).strip()

                if chinese_name:
                    herbs.append((chinese_name, pinyin, detail_url, pid))

            # 检查是否有下一页
            pagination = soup.select_one("#pagination")
            has_next = False
            if pagination:
                current_page = pagination.select_one("span.current")
                if current_page:
                    # 找比当前页大的页码链接
                    for link in pagination.select("a"):
                        href = link.get("href", "")
                        m = re.search(r'page=(\d+)', href)
                        if m and int(m.group(1)) > page:
                            has_next = True
                            break
            if not has_next:
                break
            page += 1
            time.sleep(REQUEST_DELAY)

    log.info("共发现 %d 个药材", len(herbs))
    return herbs


# ── 解析详情页 ───────────────────────────────────────
FIELD_MAP = {
    "名称": "name",
    "拼音": "pinyin",
    "英文": "englishName",
    "拉丁": "latinName",
    "类别": "category",
    "来源": "sourceDesc",
    "产地": "originDesc",
    "性状": "macroscopic",
    "品质": "qualityDesc",
    "性味": "natureFlavor",
    "功效": "efficacy",
}


def parse_detail_page(soup, source_url: str) -> dict:
    data = {"sourceUrl": source_url}

    # 标题行: <p class="title">葛根 <font size="-1">Gegen</font></p>
    title_p = soup.select_one("p.title")
    if title_p:
        title_parts = title_p.get_text(" ", strip=True)
        data["name"] = clean_text(title_parts)

    # 遍历 table#info 中的所有行
    info_rows = soup.select("table#info tr")
    for row in info_rows:
        label_td = row.select_one("td[align=left]")
        if not label_td:
            continue
        label = clean_text(label_td.get_text())
        if not label:
            continue
        # 去掉 【】 括号
        label_clean = label.strip("【】").strip()
        value_td = label_td.find_next_sibling("td")
        if not value_td:
            continue
        value = clean_text(value_td.get_text())

        field = FIELD_MAP.get(label_clean)
        if field and value:
            data[field] = value

    # 提取药材主图
    img = soup.select_one("table#content img[src*='images/']")
    if img and img.get("src"):
        src = img["src"]
        # 跳过 small/ 缩略图和 clear.gif 等
        if "small/" not in src and not src.endswith(".gif"):
            data["_imageUrl"] = urljoin(BASE_URL, src)

    return data


def download_image(session, url: str, filename: str) -> str | None:
    resp = safe_get(session, url, stream=True)
    if resp is None:
        return None

    content_type = resp.headers.get("Content-Type", "")
    ext = ".jpg"
    if "png" in content_type:
        ext = ".png"
    elif "gif" in content_type:
        ext = ".gif"

    safe_name = re.sub(r'[\\/:*?"<>|]', "_", filename) + ext
    path = IMAGE_DIR / safe_name
    with open(path, "wb") as f:
        for chunk in resp.iter_content(chunk_size=8192):
            f.write(chunk)
    log.info("  图片已保存: %s", safe_name)
    return safe_name


# ── 爬取主流程 ───────────────────────────────────────
def crawl(session, max_herbs: int | None = None):
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    IMAGE_DIR.mkdir(parents=True, exist_ok=True)

    herbs = get_all_herb_links(session)
    if max_herbs:
        herbs = herbs[:max_herbs]
        log.info("限制抓取 %d 条 (调试模式)", max_herbs)

    results = []
    total = len(herbs)
    for idx, (name, pinyin, url, pid) in enumerate(herbs, 1):
        log.info("[%d/%d] %s (%s)", idx, total, name, pid)
        resp = safe_get(session, url)
        if resp is None:
            results.append({"name": name, "sourceUrl": url, "_error": "请求失败"})
            continue

        soup = BeautifulSoup(resp.text, "html.parser")
        data = parse_detail_page(soup, url)

        # 如果 parse 没有提取到 name，用列表中的名字
        if not data.get("name"):
            data["name"] = name
        if not data.get("pinyin"):
            data["pinyin"] = pinyin

        # 繁体转简体
        for key in ("name", "pinyin", "category", "sourceDesc", "originDesc",
                     "macroscopic", "qualityDesc", "natureFlavor", "efficacy"):
            if data.get(key):
                data[key] = t2s(data[key])

        # 下载图片
        image_url = data.pop("_imageUrl", None)
        if image_url:
            img_name = data.get("name") or name or f"herb_{idx}"
            saved = download_image(session, image_url, img_name)
            if saved:
                data["imageFile"] = saved

        results.append(data)
        time.sleep(REQUEST_DELAY)

    return results


def save_results(results: list[dict]):
    json_path = OUTPUT_DIR / "data.json"
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    log.info("数据已保存: %s (%d 条)", json_path, len(results))

    ok = [r for r in results if "_error" not in r]
    failed = [r for r in results if "_error" in r]
    with_image = [r for r in ok if r.get("imageFile")]
    log.info("成功: %d, 失败: %d, 有图片: %d", len(ok), len(failed), len(with_image))
    if failed:
        for f in failed:
            log.warning("  失败: %s - %s", f.get("name"), f.get("_error"))


# ── 入口 ─────────────────────────────────────────────
def main():
    global REQUEST_DELAY
    init_log_file()

    parser = argparse.ArgumentParser(description="HKBU 中药材图像数据库爬虫")
    parser.add_argument("--max", type=int, default=None,
                        help="最大抓取数量（调试用）")
    parser.add_argument("--delay", type=float, default=None,
                        help="请求间隔秒数（默认 %.1f 秒）" % REQUEST_DELAY)
    parser.add_argument("--resume", type=str, default=None,
                        help="从已有的 data.json 继续（输入已有的 data.json 路径）")
    args = parser.parse_args()

    if args.delay is not None:
        REQUEST_DELAY = args.delay

    session = requests.Session()
    session.headers.update({
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                       "AppleWebKit/537.36 (KHTML, like Gecko) "
                       "Chrome/120.0.0.0 Safari/537.36",
        "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
    })

    print("=" * 50)
    print("香港浸会大学 中药材图像数据库 爬虫")
    print(f"输出目录: {OUTPUT_DIR}")
    print(f"请求间隔: {REQUEST_DELAY}s")
    print("=" * 50)

    if args.resume:
        with open(args.resume, "r", encoding="utf-8") as f:
            results = json.load(f)
        total_before = len(results)
        new_results = crawl(session, max_herbs=args.max)
        existing_names = {r.get("name") for r in results if r.get("name")}
        merged = results + [r for r in new_results if r.get("name") not in existing_names]
        log.info("合并: 原有 %d 条, 新增 %d 条, 共 %d 条",
                 total_before, len(merged) - total_before, len(merged))
        save_results(merged)
    else:
        results = crawl(session, max_herbs=args.max)
        save_results(results)

    print("\n完成！")
    print(f"数据文件: {OUTPUT_DIR / 'data.json'}")
    print(f"图片目录: {IMAGE_DIR / ''}")
    print("使用方法: 打包 data.json + images/ 文件夹，")
    print("         通过管理后台 POST /api/herb-encyclopedia/import 导入。")


if __name__ == "__main__":
    main()
