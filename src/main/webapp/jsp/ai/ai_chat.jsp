<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI助手 - 黄山学院软件小组管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/dist/css/tabler.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
        }
        .page-wrapper {
            height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .chat-fullscreen {
            flex: 1;
            display: flex;
            flex-direction: column;
            max-width: 100%;
            margin: 0;
            padding: 0;
        }
        .chat-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 20px;
            flex-shrink: 0;
        }
        .chat-messages {
            flex: 1;
            overflow-y: auto;
            padding: 20px;
            background: #f8f9fa;
            min-height: 0;
        }
        .message {
            margin-bottom: 15px;
            animation: fadeIn 0.3s ease;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .message.user {
            text-align: right;
        }
        .message-content {
            display: inline-block;
            padding: 12px 16px;
            border-radius: 15px;
            max-width: 80%;
            word-wrap: break-word;
        }
        .message.assistant .message-content {
            background: white;
            border: 1px solid #dee2e6;
            text-align: left;
        }
        .message.user .message-content {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-align: left;
        }
        .message-time {
            font-size: 11px;
            color: #999;
            margin-top: 5px;
        }
        .chat-input-area {
            padding: 15px 20px;
            background: white;
            border-top: 1px solid #dee2e6;
            flex-shrink: 0;
        }
        .input-group {
            display: flex;
            gap: 10px;
        }
        .chat-input {
            flex: 1;
            border-radius: 25px;
            padding: 12px 20px;
            border: 1px solid #dee2e6;
        }
        .chat-input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
        .btn-send {
            border-radius: 25px;
            padding: 12px 25px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            color: white;
        }
        .btn-send:hover {
            opacity: 0.9;
            color: white;
        }
        .role-badge {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 12px;
            margin-bottom: 10px;
        }
        .role-badge.admin { background: #dc3545; color: white; }
        .role-badge.member { background: #28a745; color: white; }
        .role-badge.guest { background: #6c757d; color: white; }
        .quick-questions {
            padding: 12px 15px;
            background: #fff;
            border-radius: 10px;
            margin-bottom: 15px;
            border: 1px solid #e0e0e0;
        }
        .quick-questions h6 {
            margin-bottom: 8px;
            color: #666;
            font-size: 14px;
        }
        .quick-btn {
            display: inline-block;
            padding: 5px 10px;
            margin: 2px;
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 15px;
            font-size: 12px;
            color: #666;
            cursor: pointer;
            transition: all 0.2s;
        }
        .quick-btn:hover {
            background: #667eea;
            color: white;
            border-color: #667eea;
        }
        .typing-indicator {
            display: none;
            padding: 15px;
            text-align: center;
        }
        .typing-dot {
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #667eea;
            animation: typing 1.4s infinite;
            margin: 0 2px;
        }
        .typing-dot:nth-child(2) { animation-delay: 0.2s; }
        .typing-dot:nth-child(3) { animation-delay: 0.4s; }
        @keyframes typing {
            0%, 60%, 100% { transform: translateY(0); }
            30% { transform: translateY(-10px); }
        }
    </style>
</head>
<body>
    <div class="page-wrapper">
        <div class="chat-fullscreen">
            <div class="chat-header">
                <div class="d-flex justify-content-between align-items-center">
                    <h4 class="mb-0"><i class="bi bi-robot me-2"></i>AI智能助手</h4>
                    <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-sm btn-outline-light">
                        <i class="bi bi-house me-1"></i>返回首页
                    </a>
                </div>
                <p class="mb-0 mt-1 small opacity-75">黄山学院软件小组管理系统 - 您的智能帮手</p>
            </div>
            
            <div class="chat-messages" id="chatMessages">
                <div class="quick-questions">
                    <h6><i class="bi bi-lightning me-1"></i>快捷问题</h6>
                    <c:choose>
                        <c:when test="${userRole == 'ADMIN'}">
                            <span class="quick-btn" onclick="sendQuickQuestion('如何发布新闻？')">如何发布新闻</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何审核奖项申请？')">如何审核奖项</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何创建活动？')">如何创建活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何处理招新报名？')">如何处理招新</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何重置成员密码？')">如何重置密码</span>
                        </c:when>
                        <c:when test="${userRole == 'MEMBER'}">
                            <span class="quick-btn" onclick="sendQuickQuestion('如何修改个人资料？')">如何修改资料</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何提交奖项申请？')">如何提交奖项</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何报名参加活动？')">如何报名活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何创建项目？')">如何创建项目</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何编辑个人简历？')">如何编辑简历</span>
                        </c:when>
                        <c:otherwise>
                            <span class="quick-btn" onclick="sendQuickQuestion('软件小组是做什么的？')">小组简介</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何提交招新报名？')">如何报名</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('有哪些新闻和活动？')">查看动态</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何登录系统？')">如何登录</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('忘记了密码怎么办？')">忘记密码</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <div class="message assistant">
                    <span class="role-badge" style="background: #667eea; color: white;">
                        <i class="bi bi-robot me-1"></i>AI助手
                    </span>
                    <div class="message-content">
                        您好！有什么可以帮您？
                    </div>
                    <div class="message-time"><i class="bi bi-robot me-1"></i>AI助手 · 刚刚</div>
                </div>
            </div>
            
            <div class="typing-indicator" id="typingIndicator">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span style="margin-left: 10px; color: #999;">AI正在思考中...</span>
            </div>
            
            <div class="chat-input-area">
                <form id="chatForm">
                    <input type="hidden" name="session_id" id="sessionId" value="${sessionId}">
                    <div class="input-group">
                        <input type="text" name="message" id="messageInput" 
                               class="chat-input" placeholder="请输入您的问题..." autocomplete="off">
                        <button type="submit" class="btn btn-send">
                            <i class="bi bi-send me-1"></i>发送
                        </button>
                    </div>
                </form>
                <div class="text-center mt-2">
                    <small class="text-muted">
                        <i class="bi bi-info-circle me-1"></i>
                        按 Enter 发送，Shift + Enter 换行
                    </small>
                </div>
            </div>
        </div>
    </div>

    <script>
    window.onload = function() {
        var sessionId = document.getElementById('sessionId').value;
        fetch('${pageContext.request.contextPath}/ai?action=init&session_id=' + sessionId, { method: 'POST' });
    };
    
    document.getElementById('chatForm').addEventListener('submit', function(e) {
        e.preventDefault();
        var message = document.getElementById('messageInput').value.trim();
        if (!message) return;

        addMessage('user', message);
        document.getElementById('messageInput').value = '';
        
        var sessionId = document.getElementById('sessionId').value;
        var messagesDiv = document.getElementById('chatMessages');
        
        var assistantMsgDiv = createAssistantMessage();
        messagesDiv.appendChild(assistantMsgDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
        
        var assistantContentDiv = assistantMsgDiv.querySelector('.message-content');
        
        fetch('${pageContext.request.contextPath}/ai?action=send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'message=' + encodeURIComponent(message) + '&session_id=' + encodeURIComponent(sessionId)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                assistantContentDiv.innerHTML = data.response.replace(/\n/g, '<br>');
            } else {
                assistantContentDiv.innerHTML = '<span class="text-danger">' + (data.error || '回复失败') + '</span>';
            }
        })
        .catch(err => {
            assistantContentDiv.innerHTML = '<span class="text-danger">网络错误，请稍后重试。</span>';
            console.error(err);
        });
    });

    function sendQuickQuestion(question) {
        document.getElementById('messageInput').value = question;
        document.getElementById('chatForm').dispatchEvent(new Event('submit'));
    }

    function addMessage(role, content) {
        var messagesDiv = document.getElementById('chatMessages');
        var msgDiv = document.createElement('div');
        msgDiv.className = 'message ' + role;
        
        var time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        var senderName = role === 'assistant' ? '<i class="bi bi-robot me-1"></i>AI助手' : '${sessionScope.user != null ? sessionScope.user.name : "游客"}';
        
        content = content.replace(/\n/g, '<br>');
        
        msgDiv.innerHTML = '<div class="message-content">' + content + '</div>' +
                          '<div class="message-time">' + senderName + ' · ' + time + '</div>';
        
        messagesDiv.appendChild(msgDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
    
    function createAssistantMessage() {
        var msgDiv = document.createElement('div');
        msgDiv.className = 'message assistant';
        
        var time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        var roleBadge = '<span class="role-badge" style="background: #667eea; color: white;"><i class="bi bi-robot me-1"></i>AI助手</span>';
        
        msgDiv.innerHTML = roleBadge + 
                          '<div class="message-content"><span class="text-muted">正在输入...</span></div>' +
                          '<div class="message-time"><i class="bi bi-robot me-1"></i>AI助手 · ' + time + '</div>';
        
        return msgDiv;
    }

    document.getElementById('messageInput').addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            document.getElementById('chatForm').dispatchEvent(new Event('submit'));
        }
    });
    </script>
</body>
</html>