"""
中药材百科数据导入工具

使用方法：
    python import_data.py --data data.json --images ./images

依赖：pip install pymysql
数据库连接默认使用 root/root@localhost:3306/biomed，可通过环境变量覆盖：
    DB_HOST DB_PORT DB_USER DB_PASSWORD DB_NAME
"""

import argparse
import json
import os
import sys
import uuid
from datetime import datetime

try:
    import pymysql
except ImportError:
    print("请先安装依赖：pip install pymysql")
    sys.exit(1)


def get_connection():
    return pymysql.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PASSWORD", "root"),
        database=os.getenv("DB_NAME", "biomed"),
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def ensure_tables(conn):
    with conn.cursor() as cur:
        cur.execute("""
            CREATE TABLE IF NOT EXISTS file_asset (
                id VARCHAR(64) NOT NULL PRIMARY KEY,
                file_name VARCHAR(255) DEFAULT NULL,
                storage_path VARCHAR(500) DEFAULT NULL,
                file_type VARCHAR(50) DEFAULT NULL,
                file_size BIGINT DEFAULT NULL,
                category VARCHAR(50) DEFAULT NULL,
                uploaded_by VARCHAR(64) DEFAULT NULL,
                version INT NOT NULL DEFAULT 0,
                created_at DATETIME DEFAULT NULL,
                updated_at DATETIME DEFAULT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """)
        cur.execute("""
            CREATE TABLE IF NOT EXISTS herb_encyclopedia (
                id VARCHAR(64) NOT NULL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                pinyin VARCHAR(100) DEFAULT NULL,
                english_name VARCHAR(200) DEFAULT NULL,
                latin_name VARCHAR(200) DEFAULT NULL,
                category VARCHAR(50) DEFAULT NULL,
                source_desc TEXT DEFAULT NULL,
                origin_desc TEXT DEFAULT NULL,
                macroscopic TEXT DEFAULT NULL,
                quality_desc TEXT DEFAULT NULL,
                nature_flavor VARCHAR(200) DEFAULT NULL,
                efficacy TEXT DEFAULT NULL,
                image_file_id VARCHAR(64) DEFAULT NULL,
                source_url VARCHAR(500) DEFAULT NULL,
                version INT NOT NULL DEFAULT 0,
                created_at DATETIME DEFAULT NULL,
                updated_at DATETIME DEFAULT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """)
    conn.commit()


def load_data(json_path):
    with open(json_path, "r", encoding="utf-8") as f:
        return json.load(f)


def resolve_upload_dir():
    """Determine data/uploads/ path relative to project root."""
    script_dir = os.path.dirname(os.path.abspath(__file__))
    # Navigate: tools/crawler/ -> project root
    project_root = os.path.abspath(os.path.join(script_dir, "..", ".."))
    upload_dir = os.path.join(project_root, "data", "uploads")
    os.makedirs(upload_dir, exist_ok=True)
    return upload_dir


def copy_images(images_dir, upload_dir, data):
    """Copy image files to data/uploads/ and return mapping {filename: fileId}."""
    image_map = {}
    now = datetime.now().isoformat()

    if not os.path.isdir(images_dir):
        print(f"图片目录不存在: {images_dir}")
        return image_map

    for entry in data:
        image_file = entry.get("imageFile", "")
        if not image_file:
            continue
        src = os.path.join(images_dir, image_file)
        if not os.path.exists(src):
            print(f"  跳过缺失图片: {image_file}")
            continue

        file_id = str(uuid.uuid4())
        ext = os.path.splitext(image_file)[1] or ".jpg"
        dest_name = f"{file_id}{ext}"
        dest = os.path.join(upload_dir, dest_name)

        import shutil
        shutil.copy2(src, dest)

        # Determine file type
        file_type = ext.lstrip(".").lower()
        if file_type in ("jpg", "jpeg"):
            file_type = "image/jpeg"
        elif file_type == "png":
            file_type = "image/png"
        elif file_type == "gif":
            file_type = "image/gif"
        else:
            file_type = "image/jpeg"

        image_map[image_file] = {
            "id": file_id,
            "file_name": dest_name,
            "storage_path": dest,
            "file_type": file_type,
            "file_size": os.path.getsize(src),
            "category": "图片资料",
            "version": 0,
            "created_at": now,
        }
        print(f"  复制图片: {image_file} -> {dest_name}")
    return image_map


def import_data(conn, data, image_map):
    now = datetime.now()
    imported = 0
    skipped = 0

    with conn.cursor() as cur:
        for entry in data:
            name = entry.get("name", "").strip()
            if not name:
                skipped += 1
                continue

            # Check duplicate
            cur.execute("SELECT id FROM herb_encyclopedia WHERE name = %s", (name,))
            if cur.fetchone():
                skipped += 1
                continue

            herb_id = str(uuid.uuid4())
            image_file_id = None

            image_file = entry.get("imageFile", "")
            if image_file and image_file in image_map:
                # Insert file_asset record
                img = image_map[image_file]
                cur.execute(
                    """INSERT INTO file_asset
                       (id, file_name, storage_path, file_type, file_size, category, version, created_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
                    (img["id"], img["file_name"], img["storage_path"],
                     img["file_type"], img["file_size"], img["category"],
                     img["version"], img["created_at"]),
                )
                image_file_id = img["id"]

            # Insert herb_encyclopedia record
            cur.execute(
                """INSERT INTO herb_encyclopedia
                   (id, name, pinyin, english_name, latin_name, category,
                    source_desc, origin_desc, macroscopic, quality_desc,
                    nature_flavor, efficacy, image_file_id, source_url,
                    version, created_at)
                   VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
                (herb_id,
                 name, entry.get("pinyin", ""), entry.get("englishName", ""),
                 entry.get("latinName", ""), entry.get("category", ""),
                 entry.get("sourceDesc", ""), entry.get("originDesc", ""),
                 entry.get("macroscopic", ""), entry.get("qualityDesc", ""),
                 entry.get("natureFlavor", ""), entry.get("efficacy", ""),
                 image_file_id, entry.get("sourceUrl", ""),
                 0, now),
            )
            imported += 1

        conn.commit()

    return imported, skipped


def main():
    parser = argparse.ArgumentParser(description="导入中药材百科数据")
    parser.add_argument("--data", default="data.json", help="data.json 文件路径")
    parser.add_argument("--images", default="./images", help="图片目录路径")
    parser.add_argument("--skip-images", action="store_true", help="跳过图片导入（仅导入文本数据）")
    args = parser.parse_args()

    if not os.path.exists(args.data):
        print(f"错误：找不到数据文件 {args.data}")
        sys.exit(1)

    print(f"读取数据文件: {args.data}")
    data = load_data(args.data)
    print(f"共 {len(data)} 条药材记录")

    upload_dir = resolve_upload_dir()
    print(f"图片目标目录: {upload_dir}")

    image_map = {}
    if not args.skip_images:
        print("复制图片...")
        image_map = copy_images(args.images, upload_dir, data)
        print(f"已处理 {len(image_map)} 张图片")
    else:
        print("跳过图片导入")

    print("连接数据库...")
    conn = get_connection()
    try:
        print("确保数据表存在...")
        ensure_tables(conn)

        print("导入数据...")
        imported, skipped = import_data(conn, data, image_map)
        print(f"\n导入完成：{imported} 条新增，{skipped} 条跳过")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
