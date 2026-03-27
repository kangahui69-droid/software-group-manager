<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
    <meta http-equiv="X-UA-Compatible" content="ie=edge" />
    <title>问题反馈 - 软件小组管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        :root {
            --tblr-font-sans-serif: 'Inter', -apple-system, BlinkMacSystemFont, San Francisco, Segoe UI, Roboto, Helvetica Neue, sans-serif;
        }
    </style>
</head>
<body class="d-flex flex-column bg-light">
    <div class="page page-center">
        <div class="container container-tight py-4">
            <div class="text-center mb-4">
                <a href="${pageContext.request.contextPath}/index.jsp" class="navbar-brand navbar-brand-autodark">
                    <i class="bi bi-cpu text-primary me-2 h1 mb-0"></i>
                    <span class="h1 mb-0">软件小组</span>
                </a>
            </div>
            <div class="card card-md">
                <div class="card-body">
                    <h2 class="h2 text-center mb-4">问题反馈</h2>
                    
                    <div id="errorAlert" class="alert alert-danger" style="display: none;" role="alert"></div>
                    <div id="successAlert" class="alert alert-success" style="display: none;" role="alert"></div>
                    
                    <form id="problemForm">
                        <div class="mb-3">
                            <label class="form-label required">问题标题</label>
                            <input type="text" name="title" class="form-control" placeholder="请简要描述您的问题" required maxlength="200">
                        </div>
                        <div class="mb-3">
                            <label class="form-label required">问题详情</label>
                            <textarea name="content" class="form-control" rows="5" placeholder="请详细描述您遇到的问题" required></textarea>
                        </div>
                        
                        <div class="form-footer">
                            <button type="submit" class="btn btn-primary w-100">提交反馈</button>
                        </div>
                    </form>
                </div>
            </div>
            <div class="text-center text-muted mt-3">
                <a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house me-1"></i>返回首页</a>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/js/tabler.min.js"></script>
    <script>
        document.getElementById('problemForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            var formData = new FormData(this);
            var params = new URLSearchParams();
            params.append('action', 'submit');
            params.append('title', formData.get('title'));
            params.append('content', formData.get('content'));
            
            fetch('${pageContext.request.contextPath}/problem', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params.toString()
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById('successAlert').textContent = data.message;
                    document.getElementById('successAlert').style.display = 'block';
                    document.getElementById('errorAlert').style.display = 'none';
                    document.getElementById('problemForm').reset();
                    setTimeout(function() {
                        window.location.href = '${pageContext.request.contextPath}/index.jsp';
                    }, 2000);
                } else {
                    document.getElementById('errorAlert').textContent = data.message;
                    document.getElementById('errorAlert').style.display = 'block';
                    document.getElementById('successAlert').style.display = 'none';
                }
            })
            .catch(error => {
                document.getElementById('errorAlert').textContent = '提交失败，请稍后重试';
                document.getElementById('errorAlert').style.display = 'block';
                document.getElementById('successAlert').style.display = 'none';
            });
        });
    </script>
</body>
</html>