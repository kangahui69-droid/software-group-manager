<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, viewport-fit=cover" />
    <meta http-equiv="X-UA-Compatible" content="ie=edge" />
    <title>问题反馈 - 软件小组管理系统</title>
    <link rel="icon" href="${pageContext.request.contextPath}/images/IMG_20260419_175314.jpg" type="image/jpeg">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Outfit:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        :root {
            --brand-blue: #1456f0;
            --font-display: 'Outfit', sans-serif;
            --font-ui: 'DM Sans', sans-serif;
            --radius-generous: 16px;
            --radius-standard: 12px;
            --radius-comfortable: 10px;
            --radius-pill: 9999px;
            --shadow-brand-purple: 0 4px 20px rgba(20, 85, 240, 0.15);
            --shadow-brand-offset: 0 8px 32px rgba(20, 85, 240, 0.12);
            --shadow-standard: 0 2px 8px rgba(0, 0, 0, 0.06);
            --bg-white: #ffffff;
            --text-dark: #1a1a2e;
            --text-muted: #6b7280;
            --border-gray: #e5e7eb;
            --border-light: #f3f4f6;
            --primary-light: #eff6ff;
            --primary-600: #2563eb;
            --tblr-font-sans-serif: 'DM Sans', -apple-system, BlinkMacSystemFont, San Francisco, Segoe UI, Roboto, Helvetica Neue, sans-serif;
        }
        body {
            font-family: var(--font-ui);
            background: linear-gradient(135deg, #f8fafc 0%, #e8f0fe 100%);
            min-height: 100vh;
        }
        .page-center {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            padding: 24px;
        }
        .brand-card {
            background: var(--bg-white);
            border-radius: var(--radius-generous);
            box-shadow: var(--shadow-brand-offset);
            border: 1px solid var(--border-light);
            overflow: hidden;
        }
        .brand-header {
            background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 100%);
            padding: 40px 32px;
            text-align: center;
        }
        .brand-header .brand-icon {
            width: 64px;
            height: 64px;
            border-radius: var(--radius-standard);
            object-fit: cover;
            margin: 0 auto 16px;
        }
        .brand-header h1 {
            font-family: var(--font-display);
            font-size: 28px;
            font-weight: 700;
            color: #ffffff;
            margin: 0;
        }
        .brand-body {
            padding: 32px;
        }
        .brand-title {
            font-family: var(--font-display);
            font-size: 20px;
            font-weight: 600;
            color: var(--text-dark);
            text-align: center;
            margin-bottom: 24px;
        }
        .form-label {
            font-weight: 500;
            color: var(--text-dark);
            margin-bottom: 8px;
        }
        .form-control {
            border: 1px solid var(--border-gray);
            border-radius: var(--radius-comfortable);
            padding: 12px 16px;
            font-size: 15px;
            transition: all 0.2s ease;
        }
        .form-control:focus {
            border-color: var(--brand-blue);
            box-shadow: 0 0 0 3px rgba(20, 85, 240, 0.1);
            outline: none;
        }
        textarea.form-control {
            resize: vertical;
            min-height: 120px;
        }
        .btn-brand {
            background: linear-gradient(135deg, var(--brand-blue) 0%, #1d4ed8 100%);
            border: none;
            border-radius: var(--radius-comfortable);
            padding: 14px 24px;
            font-weight: 600;
            font-size: 15px;
            color: #ffffff;
            width: 100%;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .btn-brand:hover {
            transform: translateY(-1px);
            box-shadow: var(--shadow-brand-purple);
        }
        .alert-custom {
            border-radius: var(--radius-comfortable);
            padding: 16px;
            margin-bottom: 20px;
            display: none;
        }
        .alert-success {
            background: #ecfdf5;
            border: 1px solid #d1fae5;
            color: #065f46;
        }
        .alert-danger {
            background: #fef2f2;
            border: 1px solid #fee2e2;
            color: #991b1b;
        }
        .back-link {
            text-align: center;
            margin-top: 24px;
        }
        .back-link a {
            color: var(--brand-blue);
            text-decoration: none;
            font-weight: 500;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .back-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="page page-center">
        <div class="container container-tight py-4">
            <div class="brand-card">
                <div class="brand-header">
                    <img src="${pageContext.request.contextPath}/images/IMG_20260419_175314.jpg" alt="Logo" class="brand-icon">
                    <h1>软件小组</h1>
                </div>
                <div class="brand-body">
                    <h2 class="brand-title">问题反馈</h2>
                    
                    <div id="errorAlert" class="alert alert-custom alert-danger" role="alert"></div>
                    <div id="successAlert" class="alert alert-custom alert-success" role="alert"></div>
                    
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
                            <button type="submit" class="btn btn-brand">提交反馈</button>
                        </div>
                    </form>
                </div>
            </div>
            <div class="back-link">
                <a href="${pageContext.request.contextPath}/index.jsp"><i class="bi bi-house"></i>返回首页</a>
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