package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.NewsAssetDAO;
import dao.NewsDAO;
import model.News;
import model.NewsAsset;
import model.User;
import service.NewsMediaService;
import util.AuthHelper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NewsMediaServlet extends HttpServlet {
    private final NewsAssetDAO assets = new NewsAssetDAO();
    private final NewsDAO news = new NewsDAO();
    private final NewsMediaService storage = new NewsMediaService();
    private final ObjectMapper json = new ObjectMapper();

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        if (!AuthHelper.isAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            json.writeValue(response.getWriter(), Map.of("error", "仅管理员可以上传新闻媒体"));
            return;
        }
        if (!"/upload".equals(request.getPathInfo())) { response.sendError(404); return; }
        try {
            User user = AuthHelper.getCurrentUser(request);
            NewsAsset asset = storage.upload(request.getPart("file"), request.getParameter("kind"), user.getId(), request.getParameter("token"));
            response.setStatus(201);
            json.writeValue(response.getWriter(), Map.of("id", asset.getId(), "name", asset.getOriginalName(),
                    "size", asset.getFileSize(), "kind", asset.getKind(), "url", request.getContextPath() + "/news/media/" + asset.getId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.setStatus(400);
            json.writeValue(response.getWriter(), Map.of("error", e.getMessage()));
        }
    }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null || !path.matches("/[1-9][0-9]*(/download)?")) { response.sendError(404); return; }
        int id;
        try { id = Integer.parseInt(path.split("/")[1]); }
        catch (NumberFormatException e) { response.sendError(404); return; }
        NewsAsset asset = assets.find(id);
        if (asset == null) { response.sendError(404); return; }
        User user = AuthHelper.getCurrentUser(request);
        boolean admin = user != null && "ADMIN".equalsIgnoreCase(user.getRole());
        boolean publicNews = false;
        if (asset.getNewsId() != null) {
            News article = news.findById(asset.getNewsId());
            publicNews = article != null && Integer.valueOf(1).equals(article.getStatus());
        }
        if (!publicNews && !(admin && (asset.getNewsId() != null || asset.getUploadedBy() == user.getId()))) {
            response.sendError(404); return;
        }
        Path file = storage.path(asset);
        if (!Files.isRegularFile(file)) { response.sendError(404); return; }
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "private, no-store");
        response.setContentType(asset.getMimeType());
        response.setContentLengthLong(Files.size(file));
        boolean download = path.endsWith("/download") || !"IMAGE".equals(asset.getKind());
        if (download) {
            String encoded = URLEncoder.encode(asset.getOriginalName(), StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"download\"; filename*=UTF-8''" + encoded);
        } else {
            response.setHeader("Content-Security-Policy", "default-src 'none'; sandbox");
        }
        Files.copy(file, response.getOutputStream());
    }
}
