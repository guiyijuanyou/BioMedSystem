package com.cqutcm.biomed.controller;

import com.cqutcm.biomed.config.AppDataPathResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/map/tiles")
public class MapTileController {
    private static final Map<String, String> SOURCES = Map.of(
            "osm", "https://tile.openstreetmap.org/%d/%d/%d.png",
            "esri", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/%d/%d/%d",
            "hot", "https://a.tile.openstreetmap.fr/hot/%d/%d/%d.png"
    );

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Path cacheRoot;

    public MapTileController(@Value("${app.map-cache-dir:data/map-cache}") String mapCacheDir,
                             AppDataPathResolver pathResolver) {
        this.cacheRoot = pathResolver.resolve(mapCacheDir);
    }

    @GetMapping(value = "/{source}/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> tile(
            @PathVariable String source,
            @PathVariable int z,
            @PathVariable int x,
            @PathVariable int y
    ) {
        String key = source.toLowerCase(Locale.ROOT);
        if (!SOURCES.containsKey(key) || z < 0 || z > 19 || x < 0 || y < 0) {
            throw new IllegalArgumentException("无效的地图瓦片参数");
        }

        try {
            Path target = cacheRoot.resolve(Path.of(key, String.valueOf(z), String.valueOf(x), y + ".png")).normalize();
            if (!target.startsWith(cacheRoot)) {
                throw new IllegalArgumentException("无效的地图瓦片路径");
            }
            if (Files.exists(target)) {
                return image(Files.readAllBytes(target));
            }

            String template = SOURCES.get(key);
            String url = "esri".equals(key)
                    ? template.formatted(z, y, x)
                    : template.formatted(z, x, y);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "BiomedDigitalSystem/1.0 educational-research")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body().length == 0) {
                throw new IllegalStateException("地图瓦片服务返回状态 " + response.statusCode());
            }
            Files.createDirectories(target.getParent());
            Files.write(target, response.body());
            return image(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("地图瓦片请求被中断", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("地图瓦片加载失败: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(), ex);
        }
    }

    private ResponseEntity<byte[]> image(byte[] content) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .contentType(MediaType.IMAGE_PNG)
                .body(content);
    }
}
