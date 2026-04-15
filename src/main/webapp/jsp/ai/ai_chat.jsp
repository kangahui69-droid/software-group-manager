<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI助手 - 黄山学院软件小组管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/css/tabler.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
        }
        
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #f0f7ff 0%, #e8f0fe 100%);
            min-height: 100vh;
        }

        .page-wrapper {
            height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .chat-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            max-width: 1200px;
            margin: 0 auto;
            width: 100%;
            padding: 20px;
        }

        .chat-header {
            padding: 24px;
            text-align: center;
        }

        .brand-section {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            margin-bottom: 16px;
        }

        .brand-icon {
            width: 48px;
            height: 48px;
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 14px rgba(59, 130, 246, 0.35);
        }

        .brand-icon i {
            font-size: 24px;
            color: white;
        }

        .brand-name {
            font-size: 24px;
            font-weight: 600;
            color: #1e293b;
        }

        .greeting-text {
            font-size: 20px;
            font-weight: 500;
            color: #1e293b;
            margin-bottom: 4px;
        }

        .greeting-subtext {
            font-size: 14px;
            color: #64748b;
        }

        .chat-messages {
            flex: 1;
            overflow-y: auto;
            padding: 0 24px;
            min-height: 0;
        }

        .message {
            margin-bottom: 20px;
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
            padding: 14px 18px;
            border-radius: 18px;
            max-width: 75%;
            word-wrap: break-word;
            line-height: 1.6;
        }

        .message.assistant .message-content {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 18px;
            text-align: left;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }

        .message.user .message-content {
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            color: white;
            text-align: left;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }

        .message-time {
            font-size: 12px;
            color: #94a3b8;
            margin-top: 6px;
        }

        .message.user .message-time {
            text-align: right;
        }

        .chat-input-area {
            padding: 24px;
        }

        .input-wrapper {
            display: flex;
            align-items: center;
            gap: 12px;
            background: white;
            border-radius: 14px;
            padding: 6px;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
            border: 1px solid #e2e8f0;
        }

        .chat-input {
            flex: 1;
            border: none;
            outline: none;
            padding: 14px 16px;
            font-size: 15px;
            background: transparent;
            color: #1e293b;
        }

        .chat-input::placeholder {
            color: #94a3b8;
        }

        .char-count {
            font-size: 12px;
            color: #94a3b8;
            padding-right: 8px;
        }

        .btn-send {
            width: 48px;
            height: 48px;
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            border: none;
            border-radius: 12px;
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.2s ease;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
        }

        .btn-send:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
        }

        .btn-send:active {
            transform: scale(0.98);
        }

        .quick-questions-section {
            padding: 0 24px 24px;
        }

        .quick-questions-header {
            font-size: 14px;
            font-weight: 500;
            color: #64748b;
            margin-bottom: 12px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

.quick-questions-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 12px;
        }

        @media (max-width: 768px) {
            .quick-questions-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        .quick-btn {
            padding: 14px 16px;
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            text-align: left;
            cursor: pointer;
            transition: all 0.2s ease;
            display: flex;
            flex-direction: column;
            gap: 4px;
        }

        .quick-btn:hover {
            border-color: #3b82f6;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
            transform: translateY(-1px);
        }

        .quick-btn-text {
            font-size: 14px;
            font-weight: 500;
            color: #1e293b;
        }

        .quick-btn-category {
            font-size: 11px;
            color: #94a3b8;
        }

        .typing-indicator {
            display: none;
            padding: 15px 24px;
        }

        .typing-container {
            display: flex;
            align-items: center;
            gap: 8px;
            background: white;
            padding: 12px 16px;
            border-radius: 18px;
            border: 1px solid #e2e8f0;
            display: inline-block;
        }

        .typing-dot {
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #3b82f6;
            animation: typing 1.4s infinite;
            margin: 0 2px;
        }

        .typing-dot:nth-child(2) { animation-delay: 0.2s; }
        .typing-dot:nth-child(3) { animation-delay: 0.4s; }

        @keyframes typing {
            0%, 60%, 100% { transform: translateY(0); }
            30% { transform: translateY(-6px); }
        }

        .typing-text {
            font-size: 14px;
            color: #64748b;
        }

        .role-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            margin-bottom: 8px;
            background: rgba(59, 130, 246, 0.1);
            color: #3b82f6;
            font-weight: 500;
        }

        .decorative-bg {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            pointer-events: none;
            overflow: hidden;
            z-index: -1;
        }

        .decorative-circle {
            position: absolute;
            border-radius: 50%;
            opacity: 0.4;
        }

        .decorative-circle-1 {
            width: 300px;
            height: 300px;
            background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(96, 165, 250, 0.1));
            top: -100px;
            right: -50px;
        }

        .decorative-circle-2 {
            width: 200px;
            height: 200px;
            background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(167, 139, 250, 0.1));
            bottom: -80px;
            left: -60px;
        }

        .decorative-wave {
            position: absolute;
            bottom: 0;
            right: 0;
            width: 500px;
            height: 300px;
            opacity: 0.1;
        }

        .action-result {
            padding: 12px;
            background: #f8fafc;
            border-radius: 8px;
            margin-top: 8px;
        }

        #infoFormPanel {
            position: fixed;
            bottom: 100px;
            right: 30px;
            width: 380px;
            height: 450px;
            max-width: calc(100vw - 40px);
            max-height: calc(100vh - 140px);
            background: white;
            border-radius: 14px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.15);
            z-index: 9999;
            display: none;
            flex-direction: column;
            overflow: hidden;
        }

        #infoFormPanel.show {
            display: flex;
        }

        #infoFormPanel .panel-header {
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
            color: white;
            padding: 14px 16px;
            cursor: move;
            display: flex;
            justify-content: space-between;
            align-items: center;
            user-select: none;
            flex-shrink: 0;
        }

        #infoFormPanel .panel-header .panel-title {
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        #infoFormPanel .panel-header .panel-controls {
            display: flex;
            gap: 8px;
        }

        #infoFormPanel .panel-header .panel-controls span {
            cursor: pointer;
            opacity: 0.8;
            font-size: 14px;
            width: 24px;
            height: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 6px;
        }

        #infoFormPanel .panel-header .panel-controls span:hover {
            opacity: 1;
            background: rgba(255,255,255,0.2);
        }

        #infoFormPanel .panel-body {
            padding: 16px;
            overflow-y: auto;
            flex: 1;
            min-height: 0;
        }

        #infoFormPanel .panel-footer {
            padding: 12px 16px;
            border-top: 1px solid #e2e8f0;
            flex-shrink: 0;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }

        #infoFormPanel .panel-footer .btn {
            padding: 10px 24px;
            border-radius: 10px;
            font-weight: 500;
        }

        #infoFormPanel .resize-handle {
            position: absolute;
            bottom: 0;
            right: 0;
            width: 20px;
            height: 20px;
            cursor: se-resize;
            background: linear-gradient(135deg, transparent 50%, rgba(59, 130, 246, 0.2) 50%);
            border-radius: 0 0 14px 0;
        }

        .info-form-group {
            margin-bottom: 16px;
        }

        .info-form-group label {
            font-weight: 500;
            color: #334155;
            margin-bottom: 6px;
            display: block;
            font-size: 14px;
        }

        .info-form-group input,
        .info-form-group textarea,
        .info-form-group select {
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            padding: 12px 14px;
            width: 100%;
            box-sizing: border-box;
            font-size: 14px;
        }

        .info-form-group input:focus,
        .info-form-group textarea:focus,
        .info-form-group select:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
            outline: none;
        }

        .form-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.3);
            z-index: 9998;
            display: none;
        }

        .form-overlay.show {
            display: block;
        }

        @media (max-width: 768px) {
            .quick-questions-grid {
                grid-template-columns: 1fr;
            }
            
            .chat-container {
                padding: 16px;
            }
            
            .chat-header {
                padding: 16px;
            }
            
            .greeting-text {
                font-size: 18px;
            }
            
            .message-content {
                max-width: 90%;
            }
        }
    </style>
</head>
<body>
    <div class="decorative-bg">
        <div class="decorative-circle decorative-circle-1"></div>
        <div class="decorative-circle decorative-circle-2"></div>
        <svg class="decorative-wave" viewBox="0 0 500 300" fill="none">
            <path d="M0,200 Q125,100 250,150 T500,100 L500,300 L0,300 Z" fill="#3b82f6"/>
        </svg>
    </div>

    <div class="page-wrapper">
        <div class="chat-container">
            <div class="chat-header">
                <div class="brand-section">
                    <div class="brand-icon">
                        <i class="bi bi-robot"></i>
                    </div>
                    <span class="brand-name">AI智能助手</span>
                </div>
                <p class="greeting-text">Hi, 下午好！我是您的专属AI助手</p>
                <p class="greeting-subtext">请问有什么可以帮您的？</p>
            </div>

            <div class="chat-messages" id="chatMessages">
                <div class="message assistant">
                    <span class="role-badge"><i class="bi bi-robot me-1"></i>AI助手</span>
                    <div class="message-content">
                        您好！我是软件小组的AI智能助手，很高兴为您服务。<br><br>您可以向我咨询小组相关的各种问题，比如查看新闻、活动、项目等信息。
                    </div>
                    <div class="message-time">AI助手 · 刚刚</div>
                </div>
            </div>

            <div class="typing-indicator" id="typingIndicator">
                <div class="typing-container">
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                    <span class="typing-dot"></span>
                    <span class="typing-text">AI正在思考中...</span>
                </div>
            </div>

            <div class="quick-questions-section">
                <div class="quick-questions-header">
                    <i class="bi bi-lightning"></i>
                    <span>您可以尝试以下问题</span>
                </div>
                <div class="quick-questions-grid">
                    <c:choose>
                        <c:when test="${userRole == 'ADMIN'}">
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]submit_news')">
                                <span class="quick-btn-text">"帮我发布一条新闻"</span>
                                <span class="quick-btn-category">发布新闻</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_pending_news')">
                                <span class="quick-btn-text">"查看待审核新闻"</span>
                                <span class="quick-btn-category">待审核新闻</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_pending_activities')">
                                <span class="quick-btn-text">"查看待审核活动"</span>
                                <span class="quick-btn-category">待审核活动</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_pending_users')">
                                <span class="quick-btn-text">"查看待处理招新"</span>
                                <span class="quick-btn-category">待处理招新</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_all_users')">
                                <span class="quick-btn-text">"查看所有成员"</span>
                                <span class="quick-btn-category">查看成员</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]statistics')">
                                <span class="quick-btn-text">"数据统计概览"</span>
                                <span class="quick-btn-category">数据统计</span>
                            </div>
                        </c:when>
                        <c:when test="${userRole == 'MEMBER'}">
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_latest_activities')">
                                <span class="quick-btn-text">"查看最新活动"</span>
                                <span class="quick-btn-category">活动中心</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]apply_activity')">
                                <span class="quick-btn-text">"报名参加活动"</span>
                                <span class="quick-btn-category">报名活动</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]create_activity_request')">
                                <span class="quick-btn-text">"申请创建活动"</span>
                                <span class="quick-btn-category">申请活动</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_public_projects')">
                                <span class="quick-btn-text">"查看所有项目"</span>
                                <span class="quick-btn-category">查看项目</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]create_project_request')">
                                <span class="quick-btn-text">"申请创建项目"</span>
                                <span class="quick-btn-category">申请项目</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]view_my_activities')">
                                <span class="quick-btn-text">"查看我的活动"</span>
                                <span class="quick-btn-category">我的活动</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]recent_news')">
                                <span class="quick-btn-text">"查看小组动态"</span>
                                <span class="quick-btn-category">小组动态</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="quick-btn" onclick="sendQuickQuestion('软件小组是做什么的？')">
                                <span class="quick-btn-text">"软件小组是做什么的？"</span>
                                <span class="quick-btn-category">小组简介</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_activities')">
                                <span class="quick-btn-text">"查看近期活动"</span>
                                <span class="quick-btn-category">查看活动</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_all_news')">
                                <span class="quick-btn-text">"查看新闻动态"</span>
                                <span class="quick-btn-category">查看新闻</span>
                            </div>
                            <div class="quick-btn" onclick="sendQuickQuestion('如何登录系统？')">
                                <span class="quick-btn-text">"如何登录系统？"</span>
                                <span class="quick-btn-category">如何登录</span>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="chat-input-area">
                <form id="chatForm">
                    <input type="hidden" name="session_id" id="sessionId" value="${sessionId}">
                    <div class="input-wrapper">
                        <input type="text" name="message" id="messageInput" 
                               class="chat-input" placeholder="请告诉我需要协助的事情..." 
                               autocomplete="off" maxlength="1000">
                        <span class="char-count" id="charCount">0/1000</span>
                        <button type="submit" class="btn-send">
                            <i class="bi bi-send"></i>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="form-overlay" id="formOverlay"></div>
    <div id="infoFormPanel">
        <div class="panel-header" id="panelHeader">
            <div class="panel-title">
                <i class="bi bi-pencil-square"></i>
                <span id="panelTitle">填写信息</span>
            </div>
            <div class="panel-controls">
                <span id="btnMinimize" title="最小化">─</span>
                <span id="btnMaximize" title="最大化">□</span>
                <span id="btnClose" title="关闭">×</span>
            </div>
        </div>
        <div class="panel-body">
            <form id="infoCollectForm">
                <div id="formFieldsContainer"></div>
                <input type="hidden" id="pendingActionType" value="">
            </form>
        </div>
        <div class="panel-footer">
            <button type="button" class="btn btn-outline-secondary" id="cancelFormBtn">取消</button>
            <button type="button" class="btn btn-primary" id="submitInfoBtn" style="background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%); border: none;">
                <i class="bi bi-check-lg me-1"></i>确认提交
            </button>
        </div>
        <div class="resize-handle" id="resizeHandle"></div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        var sessionId = document.getElementById('sessionId').value;
        fetch('${pageContext.request.contextPath}/ai?action=init&session_id=' + sessionId, { method: 'POST' });

        var messageInput = document.getElementById('messageInput');
        var charCount = document.getElementById('charCount');
        
        messageInput.addEventListener('input', function() {
            var count = this.value.length;
            charCount.textContent = count + '/1000';
        });
    });

    document.getElementById('chatForm').addEventListener('submit', function(e) {
        e.preventDefault();
        var message = document.getElementById('messageInput').value.trim();
        if (!message) return;

        var sessionId = document.getElementById('sessionId').value;
        var messagesDiv = document.getElementById('chatMessages');
        
        var convertedAction = null;
        
        if (/^\d+$/.test(message)) {
            var messages = messagesDiv.querySelectorAll('.message.assistant');
            for (var i = messages.length - 1; i >= 0; i--) {
                var content = messages[i].querySelector('.message-content').innerText;
                if (content.indexOf('活动ID') !== -1 || content.indexOf('报名') !== -1 || content.indexOf('活动列表') !== -1) {
                    convertedAction = 'apply_activity|activity_id=' + message;
                    break;
                } else if (content.indexOf('项目ID') !== -1 || content.indexOf('项目详情') !== -1) {
                    convertedAction = 'view_project|project_id=' + message;
                    break;
                } else if (content.indexOf('新闻ID') !== -1 || content.indexOf('新闻详情') !== -1) {
                    convertedAction = 'view_news|news_id=' + message;
                    break;
                } else if (content.indexOf('奖项ID') !== -1 || content.indexOf('奖项详情') !== -1) {
                    convertedAction = 'view_award|award_id=' + message;
                    break;
                } else if (content.indexOf('用户ID') !== -1) {
                    convertedAction = 'query_user_info|user_id=' + message;
                    break;
                }
            }
        }
        
        if (!convertedAction) {
            var msgLower = message.toLowerCase();
            if (msgLower.includes('动态') || msgLower === '动态列表') {
                convertedAction = 'recent_news';
            } else if (msgLower === '新闻' || msgLower === '新闻列表' || msgLower === '查看新闻' || msgLower.includes('新闻列表')) {
                convertedAction = 'list_all_news';
            } else if (msgLower.includes('活动新闻')) {
                convertedAction = 'list_activities';
            } else if (msgLower.includes('获奖新闻') || msgLower.includes('奖项新闻')) {
                convertedAction = 'recent_news';
            } else if (msgLower.includes('公告') || msgLower.includes('通知')) {
                convertedAction = 'recent_news';
            } else if (msgLower.includes('活动列表') || msgLower === '查看活动' || msgLower === '活动' || msgLower.includes('有什么活动')) {
                convertedAction = 'list_activities';
            } else if (msgLower.includes('成员') || msgLower.includes('用户') || msgLower.includes('成员列表')) {
                convertedAction = 'list_all_users';
            } else if (msgLower.includes('招新') || msgLower.includes('报名参加')) {
                convertedAction = 'list_activities';
            } else if (msgLower.includes('报名活动')) {
                convertedAction = 'apply_activity';
            } else if (msgLower.includes('申请活动')) {
                convertedAction = 'create_activity_request';
            } else if (msgLower.includes('发布新闻') || msgLower === '新闻发布' || msgLower.includes('提交新闻')) {
                convertedAction = 'submit_news';
            } else if (msgLower.includes('查看项目') || msgLower === '项目列表' || msgLower === '所有项目') {
                convertedAction = 'list_all_projects';
            } else if (msgLower.includes('申请项目') || msgLower.includes('提交项目') || msgLower.includes('创建项目') || msgLower.includes('发起项目')) {
                convertedAction = 'create_project_request';
            } else if (msgLower.includes('奖项申请') || msgLower.includes('提交奖项') || msgLower.includes('申请奖项') || msgLower.includes('获奖')) {
                convertedAction = 'submit_award';
            } else if (msgLower.includes('我的奖项') || msgLower.includes('个人奖项') || msgLower.includes('我的获奖')) {
                convertedAction = 'list_my_awards';
            } else if (msgLower.includes('奖项') || msgLower.includes('获奖')) {
                convertedAction = 'list_my_awards';
            } else if (msgLower.includes('项目')) {
                convertedAction = 'list_all_projects';
            }
        }
        
        if (convertedAction) {
            addMessage('user', message);
            document.getElementById('messageInput').value = '';
            document.getElementById('charCount').textContent = '0/1000';
            
            var assistantMsgDiv = createAssistantMessage();
            messagesDiv.appendChild(assistantMsgDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
            
            var assistantContentDiv = assistantMsgDiv.querySelector('.message-content');
            
            var actionLabels = {
                'recent_news': '正在获取小组动态...',
                'list_activities': '正在获取活动列表...',
                'list_all_users': '正在获取成员列表...',
                'apply_activity': '正在处理您的报名请求...',
                'create_activity_request': '正在创建活动...',
                'submit_news': '正在发布新闻...',
                'submit_award': '正在申请奖项...',
                'list_my_awards': '正在获取您的奖项...',
                'list_all_awards': '正在获取奖项列表...',
                'pending_feature': '正在处理...'
            };
            var loadingText = actionLabels[convertedAction.split('|')[0]] || '正在处理...';
            assistantContentDiv.innerHTML = '<i class="bi bi-hourglass"></i> ' + loadingText;
            
            if (convertedAction === 'pending_feature') {
                assistantContentDiv.innerHTML = '<div class="action-result"><i class="bi bi-tools me-1"></i> 该功能正在开发中，敬请期待！</div>';
                return;
            }
            
            executeActionFromAI(convertedAction, null, sessionId);
            return;
        }

        addMessage('user', message);
        document.getElementById('messageInput').value = '';
        document.getElementById('charCount').textContent = '0/1000';
        
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
                var responseText = data.response;
                console.log("AI response:", responseText);
                console.log("Data object:", JSON.stringify(data));
                var lastActionIdx = responseText.lastIndexOf('[ACTION]');
                if (lastActionIdx !== -1) {
                    var beforeAction = responseText.substring(0, lastActionIdx).trim();
                    var afterAction = responseText.substring(lastActionIdx + 8);
                    var nextNewline = afterAction.indexOf('\n');
                    var actionStr;
                    if (nextNewline !== -1) {
                        actionStr = afterAction.substring(0, nextNewline).trim();
                    } else {
                        actionStr = afterAction.trim();
                    }
                    console.log("Action extracted:", actionStr);
                    
                    if (beforeAction) {
                        assistantContentDiv.innerHTML = beforeAction.replace(/\n/g, '<br>');
                    }
                    
                    if (actionStr.length > 0) {
                        setTimeout(function() {
                            executeActionFromAI(actionStr, null, sessionId);
                        }, 500);
                    }
                } else {
                    assistantContentDiv.innerHTML = responseText.replace(/\n/g, '<br>');
                    var needMore = checkAndShowInfoFormFromText(responseText);
                    if (needMore) {
                        var parent = assistantContentDiv.parentNode;
                        if (parent) parent.remove();
                    } else {
                        assistantContentDiv.innerHTML = responseText.replace(/\n/g, '<br>');
                    }
                }
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
        if (question.startsWith('[ACTION]')) {
            var actionStr = question.substring(8).trim();
            var displayText = '';
            
            if (actionStr === 'list_latest_activities') displayText = '查看最新活动';
            else if (actionStr === 'list_activities') displayText = '查看活动';
            else if (actionStr === 'list_all_news') displayText = '查看新闻';
            else if (actionStr === 'apply_activity') displayText = '报名活动';
            else if (actionStr === 'create_activity_request') displayText = '申请活动';
            else if (actionStr === 'list_all_projects') displayText = '查看项目';
            else if (actionStr === 'list_public_projects') displayText = '查看项目';
            else if (actionStr === 'create_project_request') displayText = '申请项目';
            else if (actionStr === 'view_my_activities') displayText = '我的活动';
            else if (actionStr === 'recent_news') displayText = '小组动态';
            else if (actionStr === 'submit_news') displayText = '发布新闻';
            else if (actionStr === 'list_pending_news') displayText = '待审核新闻';
            else if (actionStr === 'list_pending_activities') displayText = '待审核活动';
            else if (actionStr === 'list_pending_users') displayText = '待处理招新';
            else if (actionStr === 'list_all_users') displayText = '查看成员';
            else if (actionStr === 'statistics') displayText = '数据统计';
            else if (actionStr === 'submit_feedback') displayText = '提交反馈';
            else if (actionStr === 'submit_award') displayText = '申请奖项';
            else if (actionStr === 'list_my_awards') displayText = '我的奖项';
            else if (actionStr === 'view_my_projects') displayText = '我的项目';
            else if (actionStr === 'view_my_groups') displayText = '我的群聊';
            else displayText = actionStr;
            
            var sessionId = document.getElementById('sessionId').value;
            var messagesDiv = document.getElementById('chatMessages');
            
            addMessage('user', displayText);
            
            var assistantMsgDiv = createAssistantMessage();
            messagesDiv.appendChild(assistantMsgDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
            
            var assistantContentDiv = assistantMsgDiv.querySelector('.message-content');
            assistantContentDiv.innerHTML = '<i class="bi bi-hourglass"></i> 处理中...';
            
            executeActionFromAI(actionStr, assistantContentDiv, sessionId);
        } else {
            document.getElementById('messageInput').value = question;
            document.getElementById('charCount').textContent = question.length + '/1000';
            document.getElementById('chatForm').dispatchEvent(new Event('submit'));
        }
    }

    function addMessage(role, content) {
        var messagesDiv = document.getElementById('chatMessages');
        var msgDiv = document.createElement('div');
        msgDiv.className = 'message ' + role;
        
        var time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        var senderName = role === 'assistant' ? 'AI助手' : '${sessionScope.user != null ? sessionScope.user.name : "游客"}';
        
        content = content.replace(/\n/g, '<br>');
        
        msgDiv.innerHTML = '<div class="message-content">' + content + '</div>' +
                          '<div class="message-time">' + senderName + ' · ' + time + '</div>';
        
        messagesDiv.appendChild(msgDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
    
    function createAssistantMessage() {
        var msgDiv = document.createElement('div');
        msgDiv.className = 'message assistant';
        
        var roleBadge = '<span class="role-badge"><i class="bi bi-robot me-1"></i>AI助手</span>';
        
        msgDiv.innerHTML = roleBadge + 
                          '<div class="message-content"><span class="text-muted">正在输入...</span></div>';
        
        return msgDiv;
    }

    document.getElementById('messageInput').addEventListener('keydown', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            document.getElementById('chatForm').dispatchEvent(new Event('submit'));
        }
    });

    var pendingActionInfo = null;

    window.addEventListener('load', function() {
        initDraggablePanel();
    });

    var isMinimized = false;
    var isMaximized = false;
    var prevSize = null;
    var prevPos = null;

    function initDraggablePanel() {
        var panel = document.getElementById('infoFormPanel');
        var header = document.getElementById('panelHeader');
        var resizeHandle = document.getElementById('resizeHandle');
        var isDragging = false;
        var isResizing = false;
        var offsetX, offsetY, startWidth, startHeight;

        header.addEventListener('mousedown', function(e) {
            if (e.target.classList.contains('panel-controls')) return;
            if (isMinimized) return;
            isDragging = true;
            var rect = panel.getBoundingClientRect();
            offsetX = e.clientX - rect.left;
            offsetY = e.clientY - rect.top;
            panel.style.cursor = 'move';
            e.preventDefault();
        });

        resizeHandle.addEventListener('mousedown', function(e) {
            if (isMinimized) return;
            isResizing = true;
            startWidth = panel.offsetWidth;
            startHeight = panel.offsetHeight;
            var rect = panel.getBoundingClientRect();
            offsetX = e.clientX - rect.left;
            offsetY = e.clientY - rect.top;
            e.preventDefault();
            e.stopPropagation();
        });

        document.addEventListener('mousemove', function(e) {
            if (isDragging) {
                var newLeft = e.clientX - offsetX;
                var newTop = e.clientY - offsetY;
                newLeft = Math.max(0, Math.min(newLeft, window.innerWidth - 100));
                newTop = Math.max(0, Math.min(newTop, window.innerHeight - 50));
                panel.style.left = newLeft + 'px';
                panel.style.top = newTop + 'px';
                panel.style.right = 'auto';
                panel.style.bottom = 'auto';
            }
            if (isResizing) {
                var newWidth = startWidth + (e.clientX - offsetX - startWidth + panel.offsetLeft);
                var newHeight = startHeight + (e.clientY - offsetY - startHeight + panel.offsetTop);
                newWidth = Math.max(300, Math.min(newWidth, window.innerWidth - panel.offsetLeft - 20));
                newHeight = Math.max(200, Math.min(newHeight, window.innerHeight - panel.offsetTop - 20));
                panel.style.width = newWidth + 'px';
                panel.style.height = newHeight + 'px';
            }
        });

        document.addEventListener('mouseup', function() {
            isDragging = false;
            isResizing = false;
            panel.style.cursor = 'default';
        });

        document.getElementById('btnClose').addEventListener('click', function() {
            hideFormPanel();
        });

        document.getElementById('cancelFormBtn').addEventListener('click', function() {
            hideFormPanel();
        });

        document.getElementById('btnMinimize').addEventListener('click', function() {
            if (isMaximized) {
                restoreFromMaximize();
            }
            if (isMinimized) {
                panel.style.width = prevSize.width;
                panel.style.height = prevSize.height;
                panel.querySelector('.panel-body').style.display = 'block';
                panel.querySelector('.panel-footer').style.display = 'flex';
                document.getElementById('resizeHandle').style.display = 'block';
                isMinimized = false;
                this.textContent = '─';
            } else {
                prevSize = {
                    width: panel.style.width || panel.offsetWidth + 'px',
                    height: panel.style.height || panel.offsetHeight + 'px'
                };
                panel.style.width = '200px';
                panel.style.height = 'auto';
                panel.querySelector('.panel-body').style.display = 'none';
                panel.querySelector('.panel-footer').style.display = 'none';
                document.getElementById('resizeHandle').style.display = 'none';
                isMinimized = true;
                this.textContent = '□';
            }
        });

        document.getElementById('btnMaximize').addEventListener('click', function() {
            if (isMinimized) {
                panel.querySelector('.panel-body').style.display = 'block';
                panel.querySelector('.panel-footer').style.display = 'flex';
                document.getElementById('resizeHandle').style.display = 'block';
                isMinimized = false;
            }
            if (isMaximized) {
                restoreFromMaximize();
            } else {
                prevSize = {
                    width: panel.style.width || panel.offsetWidth + 'px',
                    height: panel.style.height || panel.offsetHeight + 'px'
                };
                prevPos = {
                    left: panel.style.left || panel.offsetLeft + 'px',
                    top: panel.style.top || panel.offsetTop + 'px'
                };
                panel.style.left = '30px';
                panel.style.top = '30px';
                panel.style.right = '30px';
                panel.style.bottom = '100px';
                panel.style.width = 'auto';
                panel.style.height = 'auto';
                isMaximized = true;
                this.textContent = '❐';
                document.getElementById('btnMinimize').textContent = '─';
            }
        });
    }

    function restoreFromMaximize() {
        var panel = document.getElementById('infoFormPanel');
        panel.style.width = prevSize.width;
        panel.style.height = prevSize.height;
        panel.style.left = prevPos.left;
        panel.style.top = prevPos.top;
        panel.style.right = 'auto';
        panel.style.bottom = 'auto';
        isMaximized = false;
        document.getElementById('btnMaximize').textContent = '□';
    }

    function hideFormPanel() {
        document.getElementById('infoFormPanel').classList.remove('show');
        document.getElementById('formOverlay').classList.remove('show');
    }

    function showFormPanel() {
        document.getElementById('infoFormPanel').classList.add('show');
        document.getElementById('formOverlay').classList.add('show');
    }

    function executeActionFromAI(actionStr, contentDiv, sessionId) {
        console.log("Executing action:", actionStr);
        
        var actionParts = actionStr.split('|');
        var actionType = actionParts[0];
        var actionString = actionParts.length > 1 ? actionParts[1] : '';
        
        fetch('${pageContext.request.contextPath}/ai?action=execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'actionType=' + encodeURIComponent(actionType) + '&actionString=' + encodeURIComponent(actionString) + '&session_id=' + encodeURIComponent(sessionId)
        })
        .then(response => response.json())
        .then(data => {
            console.log("Action result:", data);
            
            if (data.need_more_info) {
                if (contentDiv) {
                    contentDiv.innerHTML = '<span class="text-info">' + (data.message || '请填写以下信息') + '</span>';
                }
                showInfoForm(actionType, sessionId);
                return;
            }
            
            if (contentDiv) {
                if (data.success) {
                    var resultHtml = formatActionResult(data);
                    contentDiv.innerHTML = resultHtml;
                } else {
                    contentDiv.innerHTML = '<span class="text-danger">' + (data.message || '操作失败') + '</span>';
                }
            }
            
            if (data.redirect) {
                window.location.href = data.redirect;
            }
        })
        .catch(err => {
            if (contentDiv) {
                contentDiv.innerHTML = '<span class="text-danger">网络错误，请稍后重试。</span>';
            }
            console.error(err);
        });
    }
    
    function showInfoForm(actionType, sessionId) {
        var container = document.getElementById('formFieldsContainer');
        var title = document.getElementById('panelTitle');
        var pendingActionType = document.getElementById('pendingActionType');
        
        pendingActionType.value = actionType;
        
        if (actionType === 'create_activity_request') {
            title.textContent = '申请活动';
            container.innerHTML = buildActivityForm();
        } else if (actionType === 'create_project_request') {
            title.textContent = '申请项目';
            container.innerHTML = buildProjectForm();
        } else {
            title.textContent = '填写信息';
            container.innerHTML = '<p>该操作暂不支持表单填写</p>';
        }
        
        showFormPanel();
    }
    
    function buildActivityForm() {
        return '<div class="info-form-group">' +
               '<label>活动名称 <span class="text-danger">*</span></label>' +
               '<input type="text" id="act_name" placeholder="请输入活动名称" required>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>活动地点 <span class="text-danger">*</span></label>' +
               '<input type="text" id="act_location" placeholder="请输入活动地点" required>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>活动类型</label>' +
               '<select id="act_type">' +
               '<option value="LECTURE">讲座</option>' +
               '<option value="TRAINING">培训</option>' +
               '<option value="COMPETITION">比赛</option>' +
               '<option value="SEMINAR">讨论会</option>' +
               '<option value="WORKSHOP">工作坊</option>' +
               '<option value="PROJECT_INTRO">项目介绍</option>' +
               '<option value="TEAM_BUILDING">团建</option>' +
               '<option value="OTHER">其他</option>' +
               '</select>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>活动开始时间 <span class="text-danger">*</span></label>' +
               '<input type="datetime-local" id="act_start_time" required>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>活动结束时间 <span class="text-danger">*</span></label>' +
               '<input type="datetime-local" id="act_end_time" required>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>报名开始时间</label>' +
               '<input type="datetime-local" id="act_reg_start">' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>报名截止时间</label>' +
               '<input type="datetime-local" id="act_reg_end">' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>最大参与人数</label>' +
               '<input type="number" id="act_max_participants" placeholder="不填表示无限制">' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>活动描述</label>' +
               '<textarea id="act_description" rows="3" placeholder="请输入活动描述"></textarea>' +
               '</div>';
    }
    
    function buildProjectForm() {
        return '<div class="info-form-group">' +
               '<label>项目名称 <span class="text-danger">*</span></label>' +
               '<input type="text" id="proj_name" placeholder="请输入项目名称" required>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>项目描述 <span class="text-danger">*</span></label>' +
               '<textarea id="proj_description" rows="3" placeholder="请输入项目描述" required></textarea>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>项目分类</label>' +
               '<select id="proj_category">' +
               '<option value="WEB开发">Web开发</option>' +
               '<option value="移动开发">移动开发</option>' +
               '<option value="数据分析">数据分析</option>' +
               '<option value="人工智能">人工智能</option>' +
               '<option value="网络安全">网络安全</option>' +
               '<option value="云计算">云计算</option>' +
               '<option value="其他">其他</option>' +
               '</select>' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>预期开始日期</label>' +
               '<input type="date" id="proj_start_date">' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>预期结束日期</label>' +
               '<input type="date" id="proj_end_date">' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>仓库地址</label>' +
               '<input type="url" id="proj_repo_url" placeholder="如：https://github.com/xxx">' +
               '</div>' +
               '<div class="info-form-group">' +
               '<label>预算</label>' +
               '<input type="number" id="proj_budget" placeholder="请输入预算金额">' +
               '</div>';
    }
    
    document.getElementById('submitInfoBtn').addEventListener('click', function() {
        var actionType = document.getElementById('pendingActionType').value;
        var sessionId = document.getElementById('sessionId').value;
        var params = '';
        
        if (actionType === 'create_activity_request') {
            var name = document.getElementById('act_name').value;
            var location = document.getElementById('act_location').value;
            var type = document.getElementById('act_type').value;
            var startTime = document.getElementById('act_start_time').value;
            var endTime = document.getElementById('act_end_time').value;
            var regStart = document.getElementById('act_reg_start').value;
            var regEnd = document.getElementById('act_reg_end').value;
            var max = document.getElementById('act_max_participants').value;
            var desc = document.getElementById('act_description').value;
            
            if (!name || !location || !startTime || !endTime) {
                alert('请填写必填项（活动名称、活动地点、开始时间、结束时间）');
                return;
            }
            
            params = 'name=' + encodeURIComponent(name);
            params += '|location=' + encodeURIComponent(location);
            params += '|activity_type=' + encodeURIComponent(type);
            params += '|start_time=' + encodeURIComponent(startTime);
            params += '|end_time=' + encodeURIComponent(endTime);
            if (regStart) params += '|reg_start=' + encodeURIComponent(regStart);
            if (regEnd) params += '|reg_end=' + encodeURIComponent(regEnd);
            if (max) params += '|max_participants=' + encodeURIComponent(max);
            if (desc) params += '|description=' + encodeURIComponent(desc);
        } else if (actionType === 'create_project_request') {
            var name = document.getElementById('proj_name').value;
            var desc = document.getElementById('proj_description').value;
            var category = document.getElementById('proj_category').value;
            var startDate = document.getElementById('proj_start_date').value;
            var endDate = document.getElementById('proj_end_date').value;
            var repoUrl = document.getElementById('proj_repo_url').value;
            var budget = document.getElementById('proj_budget').value;
            
            if (!name || !desc) {
                alert('请填写必填项（项目名称、项目描述）');
                return;
            }
            
            params = 'name=' + encodeURIComponent(name);
            params += '|description=' + encodeURIComponent(desc);
            params += '|category=' + category;
            if (startDate) params += '|expected_start_date=' + encodeURIComponent(startDate);
            if (endDate) params += '|expected_end_date=' + encodeURIComponent(endDate);
            if (repoUrl) params += '|repo_url=' + encodeURIComponent(repoUrl);
            if (budget) params += '|budget=' + encodeURIComponent(budget);
        }
        
        fetch('${pageContext.request.contextPath}/ai?action=execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'actionType=' + encodeURIComponent(actionType) + '&actionString=' + encodeURIComponent(params) + '&session_id=' + encodeURIComponent(sessionId)
        })
        .then(response => response.json())
        .then(data => {
            hideFormPanel();
            var messagesDiv = document.getElementById('chatMessages');
            var lastUserMsg = messagesDiv.querySelector('.message.user:last-child');
            if (lastUserMsg) {
                var assistantMsgDiv = createAssistantMessage();
                messagesDiv.appendChild(assistantMsgDiv);
                messagesDiv.scrollTop = messagesDiv.scrollHeight;
                var assistantContentDiv = assistantMsgDiv.querySelector('.message-content');
                if (data.success) {
                    var resultHtml = formatActionResult(data);
                    assistantContentDiv.innerHTML = resultHtml;
                } else {
                    assistantContentDiv.innerHTML = '<span class="text-danger">' + (data.message || '操作失败') + '</span>';
                }
            }
        })
        .catch(err => {
            alert('提交失败，请重试');
            console.error(err);
        });
    });

    function formatActionResult(data) {
        var html = '';
        
        if (data.type === 'news_list') {
            html = '<div class="mb-2"><strong>📰 最新新闻</strong></div>';
            if (data.data && data.data.length > 0) {
                data.data.forEach(function(item, index) {
                    html += '<div style="margin-bottom: 12px; padding: 10px; background: #f8fafc; border-radius: 8px;">';
                    html += '<div style="font-weight: 500; color: #1e293b;">' + (index + 1) + '. ' + item.title + '</div>';
                    html += '<div style="font-size: 12px; color: #64748b; margin-top: 4px;">' + (item.publishTime || item.createTime || '') + '</div>';
                    html += '</div>';
                });
            } else {
                html += '<div style="color: #64748b;">暂无新闻</div>';
            }
        } else if (data.type === 'categorized_news' && data.categories) {
            var allNews = [];
            data.categories.forEach(function(category) {
                if (category.items) {
                    category.items.forEach(function(item) {
                        item.type = category.name;
                        allNews.push(item);
                    });
                }
            });
            if (allNews.length > 0) {
                html = '<div style="overflow-x: auto;"><table style="width: 100%; border-collapse: collapse; font-size: 14px;">';
                html += '<thead><tr>';
                html += '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 500;">编号</th>';
                html += '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 500;">标题</th>';
                html += '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 500;">类型</th>';
                html += '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 500;">发布时间</th>';
                html += '</tr></thead><tbody>';
                allNews.forEach(function(item) {
                    html += '<tr>';
                    html += '<td style="padding: 10px; border-bottom: 1px solid #f1f5f9; color: #1e293b;">' + (item.id || '-') + '</td>';
                    html += '<td style="padding: 10px; border-bottom: 1px solid #f1f5f9; color: #1e293b;">' + (item.title || '-') + '</td>';
                    html += '<td style="padding: 10px; border-bottom: 1px solid #f1f5f9; color: #1e293b;">' + (item.type || '-') + '</td>';
                    html += '<td style="padding: 10px; border-bottom: 1px solid #f1f5f9; color: #1e293b;">' + (item.createdAt || '-') + '</td>';
                    html += '</tr>';
                });
                html += '</tbody></table></div>';
            } else {
                html += '<div style="color: #64748b;">暂无新闻</div>';
            }
        } else if (data.type === 'table' && data.columns && data.data) {
            html = '<div style="overflow-x: auto;"><table style="width: 100%; border-collapse: collapse; font-size: 14px;">';
            html += '<thead><tr>';
            var colMapping = {
                '编号': 'id', '标题': 'title', '类型': 'type', '发布时间': 'createdAt',
                'ID': 'id', '活动名称': 'title', '时间': 'startTime', '地点': 'location',
                '状态': 'status', '姓名': 'name', '角色': 'role', '学号': 'studentId'
            };
            data.columns.forEach(function(col) {
                html += '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #e2e8f0; color: #64748b; font-weight: 500;">' + col + '</th>';
            });
            html += '</tr></thead><tbody>';
            data.data.forEach(function(row) {
                html += '<tr>';
                data.columns.forEach(function(col) {
                    var propName = colMapping[col] || col;
                    var cellValue = row[propName] !== undefined ? row[propName] : '-';
                    if (propName === 'createdAt' && cellValue && cellValue.length > 10) {
                        cellValue = cellValue.substring(0, 10);
                    }
                    html += '<td style="padding: 10px; border-bottom: 1px solid #f1f5f9; color: #1e293b;">' + cellValue + '</td>';
                });
                html += '</tr>';
            });
            html += '</tbody></table></div>';
        } else if (data.type === 'detail') {
            html = '<div style="padding: 12px; background: #f8fafc; border-radius: 8px;">';
            html += '<div style="font-weight: 500; color: #1e293b; margin-bottom: 8px;">' + (data.message || '详情') + '</div>';
            if (data.data) {
                Object.keys(data.data).forEach(function(key) {
                    if (key !== 'id' && key !== 'password' && key !== 'salt') {
                        html += '<div style="display: flex; justify-content: space-between; padding: 4px 0; border-bottom: 1px solid #e2e8f0;">';
                        html += '<span style="color: #64748b;">' + key + '</span>';
                        html += '<span style="color: #1e293b; font-weight: 500;">' + (data.data[key] || '-') + '</span>';
                        html += '</div>';
                    }
                });
            }
            html += '</div>';
        } else if (data.type === 'statistics' && data.data) {
            var statLabels = {
                'total_users': '总用户数',
                'pending_users': '待审核用户',
                'total_activities': '总活动数',
                'pending_activities': '待审核活动',
                'total_news': '总新闻数',
                'pending_news': '待审核新闻',
                'total_problems': '总问题数',
                'pending_problems': '待处理问题'
            };
            html = '<div style="padding: 16px; background: #f8fafc; border-radius: 12px;">';
            html += '<div style="font-weight: 600; color: #1e293b; margin-bottom: 12px;"><i class="bi bi-graph-up me-2"></i>' + (data.message || '系统统计') + '</div>';
            html += '<div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;">';
            Object.keys(data.data).forEach(function(key) {
                var label = statLabels[key] || key;
                var value = data.data[key];
                html += '<div style="padding: 12px; background: white; border-radius: 8px; border: 1px solid #e2e8f0;">';
                html += '<div style="font-size: 12px; color: #64748b; margin-bottom: 4px;">' + label + '</div>';
                html += '<div style="font-size: 20px; font-weight: 600; color: #3b82f6;">' + value + '</div>';
                html += '</div>';
            });
            html += '</div></div>';
        } else if (data.success) {
            html = '<div style="padding: 12px; background: #dcfce7; border-radius: 8px; color: #166534;">';
            html += '<i class="bi bi-check-circle me-2"></i>' + (data.message || '操作成功') + '</div>';
        } else {
            html = '<div style="padding: 12px; background: #fee2e2; border-radius: 8px; color: #991b1b;">';
            html += '<i class="bi bi-x-circle me-2"></i>' + (data.message || '操作失败') + '</div>';
        }
        
        return html;
    }

    function checkAndShowInfoFormFromText(text) {
        return false;
    }
    </script>
</body>
</html>