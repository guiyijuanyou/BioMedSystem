import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

public class BioMedServer {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8088"));
    private static final Path DATA_DIR = Path.of("data");
    private static final Path UPLOAD_DIR = DATA_DIR.resolve("uploads");

    private static final String[] RESOURCES = {
            "herbs", "growth-records", "courses", "projects", "trainings",
            "evaluations", "achievements", "users", "standards", "files"
    };

    private final Map<String, List<Map<String, String>>> store = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        new BioMedServer().start();
    }

    private void start() throws Exception {
        Files.createDirectories(DATA_DIR);
        Files.createDirectories(UPLOAD_DIR);
        for (String resource : RESOURCES) {
            store.put(resource, load(resource));
        }
        seedIfEmpty();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", this::route);
        server.setExecutor(Executors.newFixedThreadPool(12));
        server.start();
        System.out.println("BioMed Digital Information System started at http://localhost:" + PORT);
    }

    private void route(HttpExchange exchange) throws IOException {
        try {
            addCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                send(exchange, 204, "");
                return;
            }

            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            if (path.equals("/") || path.startsWith("/assets/") || path.equals("/app.js") || path.equals("/styles.css")) {
                serveStatic(exchange, path);
                return;
            }

            if (path.equals("/api/summary")) {
                summary(exchange);
                return;
            }
            if (path.equals("/api/backup")) {
                backup(exchange);
                return;
            }
            if (path.equals("/api/soap/school")) {
                soap(exchange);
                return;
            }
            if (path.matches("/api/files/[^/]+/download")) {
                download(exchange, path.split("/")[3]);
                return;
            }
            if (path.equals("/api/files/upload")) {
                upload(exchange);
                return;
            }
            if (path.startsWith("/api/")) {
                resource(exchange, path.substring("/api/".length()));
                return;
            }
            sendJson(exchange, 404, Map.of("error", "未找到接口"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendJson(exchange, 500, Map.of("error", ex.getMessage()));
        }
    }

    private void resource(HttpExchange exchange, String resource) throws IOException {
        resource = resource.split("/")[0];
        if (!store.containsKey(resource)) {
            sendJson(exchange, 404, Map.of("error", "未知资源: " + resource));
            return;
        }
        String method = exchange.getRequestMethod();
        if ("GET".equals(method)) {
            sendJson(exchange, 200, Map.of("items", store.get(resource)));
            return;
        }
        if ("POST".equals(method)) {
            Map<String, String> item = Json.parseObject(readBody(exchange));
            item.putIfAbsent("id", UUID.randomUUID().toString());
            item.putIfAbsent("createdAt", LocalDateTime.now().toString());
            store.get(resource).add(item);
            save(resource);
            sendJson(exchange, 201, item);
            return;
        }
        if ("PUT".equals(method)) {
            Map<String, String> item = Json.parseObject(readBody(exchange));
            String id = item.get("id");
            for (Map<String, String> existing : store.get(resource)) {
                if (Objects.equals(existing.get("id"), id)) {
                    existing.putAll(item);
                    existing.put("updatedAt", LocalDateTime.now().toString());
                    save(resource);
                    sendJson(exchange, 200, existing);
                    return;
                }
            }
            sendJson(exchange, 404, Map.of("error", "未找到要更新的数据"));
            return;
        }
        sendJson(exchange, 405, Map.of("error", "不支持的请求方式"));
    }

    private void summary(HttpExchange exchange) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("herbCount", store.get("herbs").size());
        result.put("growthRecordCount", store.get("growth-records").size());
        result.put("courseCount", store.get("courses").size());
        result.put("projectCount", store.get("projects").size());
        result.put("evaluationCount", store.get("evaluations").size());
        result.put("achievementCount", store.get("achievements").size());
        result.put("latestBackup", LocalDateTime.now().toString());
        sendJson(exchange, 200, result);
    }

    private void upload(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "文件上传请使用 POST"));
            return;
        }
        Map<String, String> body = Json.parseObject(readBody(exchange));
        String id = UUID.randomUUID().toString();
        String fileName = body.getOrDefault("fileName", "upload.bin").replaceAll("[\\\\/:*?\"<>|]", "_");
        byte[] bytes = Base64.getDecoder().decode(body.getOrDefault("contentBase64", ""));
        Path target = UPLOAD_DIR.resolve(id + "-" + fileName);
        Files.write(target, bytes);

        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("id", id);
        meta.put("fileName", fileName);
        meta.put("category", body.getOrDefault("category", "教学资料"));
        meta.put("size", String.valueOf(bytes.length));
        meta.put("path", target.toString());
        meta.put("createdAt", LocalDateTime.now().toString());
        store.get("files").add(meta);
        save("files");
        sendJson(exchange, 201, meta);
    }

    private void download(HttpExchange exchange, String id) throws IOException {
        for (Map<String, String> file : store.get("files")) {
            if (Objects.equals(file.get("id"), id)) {
                Path path = Path.of(file.get("path"));
                if (!Files.exists(path)) {
                    sendJson(exchange, 404, Map.of("error", "文件不存在"));
                    return;
                }
                Headers headers = exchange.getResponseHeaders();
                headers.add("Content-Type", "application/octet-stream");
                headers.add("Content-Disposition", "attachment; filename=\"" + file.get("fileName") + "\"");
                exchange.sendResponseHeaders(200, Files.size(path));
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(path, os);
                }
                return;
            }
        }
        sendJson(exchange, 404, Map.of("error", "未找到文件"));
    }

    private void backup(HttpExchange exchange) throws IOException {
        Path backup = DATA_DIR.resolve("backup-" + LocalDate.now() + "-" + System.currentTimeMillis() + ".json");
        Files.writeString(backup, Json.stringify(store), StandardCharsets.UTF_8);
        sendJson(exchange, 200, Map.of("message", "备份完成", "file", backup.toString()));
    }

    private void soap(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String keyword = extractXmlValue(body, "keyword");
        List<Map<String, String>> herbs = store.get("herbs").stream()
                .filter(item -> keyword.isBlank()
                        || item.getOrDefault("name", "").contains(keyword)
                        || item.getOrDefault("district", "").contains(keyword))
                .toList();
        String payload = Json.stringify(Map.of("items", herbs));
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <queryGrowthDataResponse>
                      <result>%s</result>
                    </queryGrowthDataResponse>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(escapeXml(payload));
        exchange.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
        send(exchange, 200, xml);
    }

    private String extractXmlValue(String xml, String name) {
        String open = "<" + name + ">";
        String close = "</" + name + ">";
        int start = xml.indexOf(open);
        int end = xml.indexOf(close);
        if (start >= 0 && end > start) {
            return xml.substring(start + open.length(), end).trim();
        }
        String namespacedOpen = ":" + name + ">";
        String namespacedClose = ":" + name + ">";
        int nsStart = xml.indexOf(namespacedOpen);
        int nsEnd = xml.indexOf(namespacedClose, nsStart + namespacedOpen.length());
        if (nsStart >= 0 && nsEnd > nsStart) {
            return xml.substring(nsStart + namespacedOpen.length(), xml.lastIndexOf("</", nsEnd)).trim();
        }
        return "";
    }

    private void serveStatic(HttpExchange exchange, String path) throws IOException {
        Path file = switch (path) {
            case "/", "/index.html" -> Path.of("frontend", "index.html");
            case "/app.js" -> Path.of("frontend", "app.js");
            case "/styles.css" -> Path.of("frontend", "styles.css");
            default -> Path.of("frontend", path.substring(1));
        };
        if (!Files.exists(file)) {
            send(exchange, 404, "Not found");
            return;
        }
        String contentType = file.toString().endsWith(".css") ? "text/css; charset=utf-8"
                : file.toString().endsWith(".js") ? "application/javascript; charset=utf-8"
                : "text/html; charset=utf-8";
        exchange.getResponseHeaders().add("Content-Type", contentType);
        send(exchange, 200, Files.readString(file, StandardCharsets.UTF_8));
    }

    private List<Map<String, String>> load(String resource) throws IOException {
        Path file = DATA_DIR.resolve(resource + ".jsonl");
        List<Map<String, String>> rows = new ArrayList<>();
        if (Files.exists(file)) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) rows.add(Json.parseObject(line));
            }
        }
        return rows;
    }

    private void save(String resource) throws IOException {
        List<String> lines = store.get(resource).stream().map(Json::stringify).toList();
        Files.write(DATA_DIR.resolve(resource + ".jsonl"), lines, StandardCharsets.UTF_8);
    }

    private void seedIfEmpty() throws IOException {
        if (store.get("herbs").isEmpty()) {
            add("herbs", Map.of("name", "\u9ec4\u8fde", "district", "\u77f3\u67f1\u53bf", "longitude", "108.12", "latitude", "30.00", "scale", "3200\u4ea9", "environment", "\u6d77\u62d4\u9ad8\u3001\u6e7f\u6da6\u9634\u51c9", "traceCode", "CQ-HL-001"));
            add("herbs", Map.of("name", "\u91d1\u94f6\u82b1", "district", "\u79c0\u5c71\u53bf", "longitude", "109.00", "latitude", "28.45", "scale", "1800\u4ea9", "environment", "\u4e18\u9675\u5761\u5730\u3001\u65e5\u7167\u5145\u8db3", "traceCode", "CQ-JYH-002"));
            add("herbs", Map.of("name", "\u5929\u9ebb", "district", "\u5deb\u6eaa\u53bf", "longitude", "109.63", "latitude", "31.40", "scale", "950\u4ea9", "environment", "\u6797\u4e0b\u4eff\u91ce\u751f\u79cd\u690d", "traceCode", "CQ-TM-003"));
            save("herbs");
        }
        if (store.get("growth-records").isEmpty()) {
            add("growth-records", Map.of("herbName", "\u9ec4\u8fde", "district", "\u77f3\u67f1\u53bf", "temperature", "19.6", "humidity", "83", "soilPh", "6.2", "collector", "\u624b\u673aAPP\u91c7\u96c6", "recordedAt", LocalDateTime.now().minusHours(2).toString()));
            add("growth-records", Map.of("herbName", "\u5929\u9ebb", "district", "\u5deb\u6eaa\u53bf", "temperature", "17.1", "humidity", "78", "soilPh", "6.8", "collector", "\u4f20\u611f\u5668\u7f51\u5173", "recordedAt", LocalDateTime.now().minusHours(1).toString()));
            save("growth-records");
        }
        if (store.get("courses").isEmpty()) {
            add("courses", Map.of("title", "\u4e2d\u836f\u6750\u663e\u5fae\u9274\u5b9a\u5b9e\u9a8c", "teacher", "\u5f20\u8001\u5e08", "hours", "4", "materialType", "\u89c6\u9891+\u8bb2\u4e49", "status", "\u5df2\u53d1\u5e03"));
            save("courses");
        }
        if (store.get("projects").isEmpty()) {
            add("projects", Map.of("title", "\u91cd\u5e86\u9053\u5730\u836f\u6750\u751f\u6001\u9002\u5e94\u6027\u7814\u7a76", "leader", "\u674e\u8001\u5e08", "stage", "\u6570\u636e\u91c7\u96c6\u4e2d", "transformation", "\u79cd\u690d\u89c4\u8303\u8f6c\u5316"));
            save("projects");
        }
        if (store.get("trainings").isEmpty()) {
            add("trainings", Map.of("title", "\u4e2d\u836f\u6750\u89c4\u8303\u5316\u91c7\u6536\u57f9\u8bad", "trainer", "\u738b\u8001\u5e08", "audience", "\u57fa\u5c42\u6280\u672f\u4eba\u5458", "tracking", "\u7b7e\u5230\u3001\u89c6\u9891\u3001\u8003\u6838\u8bb0\u5f55\u5b8c\u6574"));
            save("trainings");
        }
        if (store.get("evaluations").isEmpty()) {
            add("evaluations", Map.of("herbName", "\u9ec4\u8fde", "indicator", "\u6027\u72b6\u3001\u542b\u91cf\u3001\u4ea7\u5730\u751f\u6001\u3001\u4f20\u627f\u5de5\u827a", "score", "91", "result", "\u4f18\u79c0", "applicationMaterial", "\u53ef\u7528\u4e8e\u975e\u9057\u53ca\u54c1\u724c\u7533\u62a5\u6750\u6599"));
            save("evaluations");
        }
        if (store.get("achievements").isEmpty()) {
            add("achievements", Map.of("title", "\u9ec4\u8fde\u79cd\u690d\u6280\u672f\u63a8\u5e7f", "owner", "\u4e2d\u836f\u5b66\u9662", "category", "\u793e\u4f1a\u670d\u52a1", "level", "\u6821\u7ea7\u91cd\u70b9", "status", "\u5f85\u5ba1\u6838"));
            save("achievements");
        }
        if (store.get("users").isEmpty()) {
            add("users", Map.of("name", "\u7cfb\u7edf\u7ba1\u7406\u5458", "role", "\u7ba1\u7406\u5458", "department", "\u4fe1\u606f\u4e2d\u5fc3", "level", "\u4e00\u7ea7"));
            add("users", Map.of("name", "\u5f20\u8001\u5e08", "role", "\u6559\u5e08", "department", "\u4e2d\u836f\u5b66\u9662", "level", "\u4e8c\u7ea7"));
            add("users", Map.of("name", "\u5b66\u751f\u7528\u6237", "role", "\u5b66\u751f", "department", "\u4e2d\u836f\u5b66\u9662", "level", "\u4e09\u7ea7"));
            save("users");
        }
        if (store.get("standards").isEmpty()) {
            add("standards", Map.of("name", "\u5b66\u6821\u4e1a\u7ee9\u5206\u7c7b\u8ba4\u5b9a\u529e\u6cd5", "category", "\u6559\u5b66\u79d1\u7814\u4e1a\u7ee9", "levelRule", "\u56fd\u5bb6\u7ea7/\u7701\u90e8\u7ea7/\u6821\u7ea7/\u9662\u7ea7", "effectiveDate", LocalDate.now().toString()));
            save("standards");
        }
    }

    private void add(String resource, Map<String, String> values) {
        Map<String, String> item = new LinkedHashMap<>(values);
        item.putIfAbsent("id", UUID.randomUUID().toString());
        item.putIfAbsent("createdAt", LocalDateTime.now().toString());
        store.get(resource).add(item);
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        send(exchange, status, Json.stringify(body));
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void addCors(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static class Json {
        static Map<String, String> parseObject(String json) {
            Map<String, String> map = new LinkedHashMap<>();
            String body = json.trim();
            if (body.startsWith("{")) body = body.substring(1);
            if (body.endsWith("}")) body = body.substring(0, body.length() - 1);
            int i = 0;
            while (i < body.length()) {
                while (i < body.length() && Character.isWhitespace(body.charAt(i))) i++;
                if (i < body.length() && body.charAt(i) == ',') i++;
                while (i < body.length() && Character.isWhitespace(body.charAt(i))) i++;
                if (i >= body.length()) break;
                Parsed key = parseString(body, i);
                i = key.next;
                while (i < body.length() && (Character.isWhitespace(body.charAt(i)) || body.charAt(i) == ':')) i++;
                Parsed value = body.charAt(i) == '"' ? parseString(body, i) : parseBare(body, i);
                map.put(key.value, value.value);
                i = value.next;
            }
            return map;
        }

        private static Parsed parseString(String text, int start) {
            StringBuilder sb = new StringBuilder();
            int i = start + 1;
            while (i < text.length()) {
                char ch = text.charAt(i++);
                if (ch == '\\' && i < text.length()) {
                    char next = text.charAt(i++);
                    if (next == 'n') sb.append('\n');
                    else if (next == 't') sb.append('\t');
                    else sb.append(next);
                } else if (ch == '"') {
                    break;
                } else {
                    sb.append(ch);
                }
            }
            return new Parsed(sb.toString(), i);
        }

        private static Parsed parseBare(String text, int start) {
            int i = start;
            while (i < text.length() && text.charAt(i) != ',') i++;
            return new Parsed(text.substring(start, i).trim(), i);
        }

        static String stringify(Object value) {
            if (value instanceof Map<?, ?> map) {
                List<String> parts = new ArrayList<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    parts.add("\"" + escape(String.valueOf(entry.getKey())) + "\":" + stringify(entry.getValue()));
                }
                return "{" + String.join(",", parts) + "}";
            }
            if (value instanceof List<?> list) {
                List<String> parts = new ArrayList<>();
                for (Object item : list) parts.add(stringify(item));
                return "[" + String.join(",", parts) + "]";
            }
            if (value instanceof Number || value instanceof Boolean) {
                return String.valueOf(value);
            }
            return "\"" + escape(String.valueOf(value)) + "\"";
        }

        private static String escape(String value) {
            return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        }

        record Parsed(String value, int next) {}
    }
}
