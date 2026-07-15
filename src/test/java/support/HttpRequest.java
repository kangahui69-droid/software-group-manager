package support;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequest {

    private final String url;
    private final String method;
    private final Map<String, String> params = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, String> cookies = new LinkedHashMap<>();
    private boolean followRedirects = false;

    private HttpRequest(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public static HttpRequest get(String url) { return new HttpRequest(url, "GET"); }
    public static HttpRequest post(String url) { return new HttpRequest(url, "POST"); }

    public HttpRequest param(String key, String value) { params.put(key, value); return this; }
    public HttpRequest header(String key, String value) { headers.put(key, value); return this; }
    public HttpRequest cookie(String name, String value) { cookies.put(name, value); return this; }
    public HttpRequest withCookies(Map<String, String> c) { this.cookies.putAll(c); return this; }
    public HttpRequest followRedirects(boolean f) { this.followRedirects = f; return this; }

    public Response execute() throws IOException {
        String fullUrl = url;
        byte[] body = null;

        if (method.equals("GET") && !params.isEmpty()) {
            fullUrl = url + "?" + buildFormString();
        } else if (method.equals("POST")) {
            body = buildFormString().getBytes(StandardCharsets.UTF_8);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
        conn.setRequestMethod(method);
        conn.setInstanceFollowRedirects(followRedirects);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        for (Map.Entry<String, String> h : headers.entrySet()) {
            conn.setRequestProperty(h.getKey(), h.getValue());
        }
        if (!cookies.isEmpty()) {
            conn.setRequestProperty("Cookie", buildCookieString());
        }
        if (body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            try (OutputStream os = conn.getOutputStream()) { os.write(body); }
        }

        int code = conn.getResponseCode();
        Map<String, String> respCookies = new LinkedHashMap<>();
        List<String> setCookies = conn.getHeaderFields().getOrDefault("Set-Cookie", Collections.emptyList());
        for (String sc : setCookies) {
            String part = sc.split(";")[0];
            int eq = part.indexOf('=');
            if (eq > 0) respCookies.put(part.substring(0, eq), part.substring(eq + 1));
        }
        String location = conn.getHeaderField("Location");

        String respBody;
        try {
            InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) { respBody = ""; }
            else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder(); String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                    respBody = sb.toString();
                }
            }
        } catch (IOException e) { respBody = ""; }
        conn.disconnect();
        return new Response(code, respBody, location, respCookies);
    }

    private String buildFormString() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(); boolean first = true;
        for (Map.Entry<String, String> p : params.entrySet()) {
            if (!first) sb.append('&');
            sb.append(URLEncoder.encode(p.getKey(), "UTF-8")).append('=')
              .append(URLEncoder.encode(p.getValue() == null ? "" : p.getValue(), "UTF-8"));
            first = false;
        }
        return sb.toString();
    }

    private String buildCookieString() {
        StringBuilder sb = new StringBuilder(); boolean first = true;
        for (Map.Entry<String, String> c : cookies.entrySet()) {
            if (!first) sb.append("; ");
            sb.append(c.getKey()).append('=').append(c.getValue());
            first = false;
        }
        return sb.toString();
    }

    public static class Response {
        private final int code;
        private final String body;
        private final String location;
        private final Map<String, String> cookies;
        public Response(int code, String body, String location, Map<String, String> cookies) {
            this.code = code; this.body = body; this.location = location; this.cookies = cookies;
        }
        public int code() { return code; }
        public String body() { return body; }
        public String header(String name) { return "Location".equalsIgnoreCase(name) ? location : null; }
        public String location() { return location; }
        public Map<String, String> cookies() { return cookies; }
        public String cookie(String name) { return cookies.get(name); }
        public String sessionId() { return cookies.get("JSESSIONID"); }
    }
}
