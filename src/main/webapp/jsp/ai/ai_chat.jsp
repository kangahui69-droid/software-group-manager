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
        #infoFormPanel {
            position: fixed;
            bottom: 100px;
            right: 30px;
            width: 380px;
            height: 450px;
            max-width: calc(100vw - 40px);
            max-height: calc(100vh - 140px);
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            z-index: 9999;
            display: none;
            flex-direction: column;
            overflow: hidden;
        }
        #infoFormPanel.show {
            display: flex;
        }
        #infoFormPanel .panel-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 12px 15px;
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
            font-size: 16px;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 4px;
        }
        #infoFormPanel .panel-header .panel-controls span:hover {
            opacity: 1;
            background: rgba(255,255,255,0.2);
        }
        #infoFormPanel .panel-body {
            padding: 15px;
            overflow-y: auto;
            flex: 1;
            min-height: 0;
        }
        #infoFormPanel .panel-footer {
            padding: 12px 15px;
            border-top: 1px solid #eee;
            flex-shrink: 0;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
        #infoFormPanel .panel-footer .btn {
            padding: 8px 20px;
            border-radius: 20px;
        }
        #infoFormPanel .resize-handle {
            position: absolute;
            bottom: 0;
            right: 0;
            width: 20px;
            height: 20px;
            cursor: se-resize;
            background: linear-gradient(135deg, transparent 50%, rgba(102,126,234,0.3) 50%);
            border-radius: 0 0 12px 0;
        }
        .info-form-group {
            margin-bottom: 15px;
        }
        .info-form-group label {
            font-weight: 500;
            color: #333;
            margin-bottom: 5px;
            display: block;
        }
        .info-form-group input,
        .info-form-group textarea,
        .info-form-group select {
            border-radius: 8px;
            border: 1px solid #dee2e6;
            padding: 10px 12px;
            width: 100%;
            box-sizing: border-box;
        }
        .info-form-group input:focus,
        .info-form-group textarea:focus,
        .info-form-group select:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]submit_news')">发布新闻</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_pending_news')">待审核新闻</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_pending_activities')">待审核活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_pending_users')">待处理招新</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_all_users')">查看成员</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]statistics')">数据统计</span>
                        </c:when>
                        <c:when test="${userRole == 'MEMBER'}">
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_latest_activities')">查看最新活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]apply_activity')">报名活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]create_activity_request')">申请活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_all_projects')">查看项目</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]create_project_request')">申请项目</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]view_my_activities')">我的活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]recent_news')">小组动态</span>
                        </c:when>
                        <c:otherwise>
                            <span class="quick-btn" onclick="sendQuickQuestion('软件小组是做什么的？')">小组简介</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_activities')">查看活动</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]list_all_news')">查看新闻</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('[ACTION]recent_news')">小组动态</span>
                            <span class="quick-btn" onclick="sendQuickQuestion('如何登录系统？')">如何登录</span>
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
            <button type="button" class="btn btn-primary" id="submitInfoBtn" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border: none;">
                <i class="bi bi-check-lg me-1"></i>确认提交
            </button>
        </div>
        <div class="resize-handle" id="resizeHandle"></div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
    window.onload = function() {
        var sessionId = document.getElementById('sessionId').value;
        fetch('${pageContext.request.contextPath}/ai?action=init&session_id=' + sessionId, { method: 'POST' });
    };
    
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
        // 检查是否是[ACTION]格式，直接执行操作
        if (question.startsWith('[ACTION]')) {
            var actionStr = question.substring(8).trim();
            var displayText = '';
            
            // 根据action显示友好文字
            if (actionStr === 'list_latest_activities') displayText = '查看最新活动';
            else if (actionStr === 'list_activities') displayText = '查看活动';
            else if (actionStr === 'list_all_news') displayText = '查看新闻';
            else if (actionStr === 'apply_activity') displayText = '报名活动';
            else if (actionStr === 'create_activity_request') displayText = '申请活动';
            else if (actionStr === 'list_all_projects') displayText = '查看项目';
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
            
            // 添加用户消息
            addMessage('user', displayText);
            
            // 创建AI消息占位
            var assistantMsgDiv = createAssistantMessage();
            messagesDiv.appendChild(assistantMsgDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
            
            var assistantContentDiv = assistantMsgDiv.querySelector('.message-content');
            assistantContentDiv.innerHTML = '<i class="bi bi-hourglass"></i> 处理中...';
            
            // 直接执行操作
            executeActionFromAI(actionStr, assistantContentDiv, sessionId);
        } else {
            document.getElementById('messageInput').value = question;
            document.getElementById('chatForm').dispatchEvent(new Event('submit'));
        }
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
        if (prevSize) {
            panel.style.width = prevSize.width;
            panel.style.height = prevSize.height;
        }
        if (prevPos) {
            panel.style.left = prevPos.left;
            panel.style.top = prevPos.top;
            panel.style.right = 'auto';
            panel.style.bottom = 'auto';
        }
        isMaximized = false;
        document.getElementById('btnMaximize').textContent = '□';
    }

    function showFormPanel() {
        var panel = document.getElementById('infoFormPanel');
        var overlay = document.getElementById('formOverlay');
        panel.classList.add('show');
        overlay.classList.add('show');
        panel.querySelector('.panel-body').style.display = 'block';
        panel.querySelector('.panel-footer').style.display = 'flex';
    }

    function hideFormPanel() {
        var panel = document.getElementById('infoFormPanel');
        var overlay = document.getElementById('formOverlay');
        
        panel.style.display = 'none';
        overlay.style.display = 'none';
        panel.classList.remove('show');
        overlay.classList.remove('show');
        
        isMinimized = false;
        isMaximized = false;
        
        panel.style.width = '380px';
        panel.style.height = '450px';
        panel.style.left = 'auto';
        panel.style.top = 'auto';
        panel.style.right = '30px';
        panel.style.bottom = '100px';
        
        panel.querySelector('.panel-body').style.display = 'block';
        panel.querySelector('.panel-footer').style.display = 'flex';
        document.getElementById('resizeHandle').style.display = 'block';
        document.getElementById('btnMinimize').textContent = '─';
        document.getElementById('btnMaximize').textContent = '□';
    }

    function showFormPanel() {
        var panel = document.getElementById('infoFormPanel');
        var overlay = document.getElementById('formOverlay');
        
        panel.style.display = 'flex';
        overlay.style.display = 'block';
        panel.classList.add('show');
        overlay.classList.add('show');
        
        panel.querySelector('.panel-body').style.display = 'block';
        panel.querySelector('.panel-footer').style.display = 'flex';
    }

    function checkAndShowInfoFormFromText(responseText) {
        var needMoreInfoPatterns = [
            /请提供以下信息[：:]\s*\n?(.+)/,
            /请提供(.+?)[：:]/,
            /请填写(.+?)[：:]/,
            /请输入(.+?)[：:]/,
            /缺少以下信息[：:]\s*\n(.+)/
        ];

        var matchedPattern = null;
        var missingInfoList = [];

        for (var i = 0; i < needMoreInfoPatterns.length; i++) {
            var match = responseText.match(needMoreInfoPatterns[i]);
            if (match) {
                matchedPattern = match;
                break;
            }
        }

        if (!matchedPattern || !matchedPattern[1]) return false;

        var infoStr = matchedPattern[1].trim();
        if (infoStr.indexOf('、') !== -1) {
            missingInfoList = infoStr.split('、');
        } else if (infoStr.indexOf('\n') !== -1) {
            missingInfoList = infoStr.split('\n').filter(function(s) { return s.trim(); });
        } else {
            missingInfoList = [infoStr];
        }

        if (missingInfoList.length === 0) return false;

        var actionType = 'create_activity_request';
        if (responseText.indexOf('报名') !== -1 || responseText.indexOf('活动ID') !== -1) {
            actionType = 'apply_activity';
        } else if (responseText.indexOf('新闻') !== -1) {
            actionType = 'submit_news';
        } else if (responseText.indexOf('反馈') !== -1 || responseText.indexOf('问题') !== -1) {
            actionType = 'submit_feedback';
        } else if (responseText.indexOf('用户') !== -1) {
            actionType = 'query_user';
        } else if (responseText.indexOf('项目') !== -1) {
            actionType = 'create_project_request';
        }

        pendingActionInfo = {
            actionType: actionType,
            missingFields: missingInfoList,
            originalResponse: responseText
        };

        buildAndShowForm(actionType, missingInfoList);
        return true;
    }

    function checkAndShowInfoForm(responseText, currentActionType) {
        var needMoreInfoPatterns = [
            /请提供以下信息[：:]\s*\n(.+)/,
            /请提供(.+?)[：:]/,
            /请填写(.+?)[：:]/,
            /请输入(.+?)[：:]/,
            /缺少以下信息[：:]\s*\n(.+)/
        ];

        var matchedPattern = null;
        var missingInfoList = [];

        for (var i = 0; i < needMoreInfoPatterns.length; i++) {
            var match = responseText.match(needMoreInfoPatterns[i]);
            if (match) {
                matchedPattern = match;
                break;
            }
        }

        if (!matchedPattern) return false;

        if (matchedPattern[1]) {
            var infoStr = matchedPattern[1];
            if (infoStr.indexOf('、') !== -1) {
                missingInfoList = infoStr.split('、');
            } else if (infoStr.indexOf('\n') !== -1) {
                missingInfoList = infoStr.split('\n').filter(function(s) { return s.trim(); });
            } else {
                missingInfoList = [infoStr];
            }
        }

        if (missingInfoList.length === 0) return false;

        pendingActionInfo = {
            actionType: currentActionType,
            missingFields: missingInfoList,
            originalResponse: responseText
        };

        buildAndShowForm(currentActionType, missingInfoList);
        return true;
    }

    function buildAndShowForm(actionType, missingFields) {
        var container = document.getElementById('formFieldsContainer');
        container.innerHTML = '';

        var actionLabels = {
            'create_activity_request': '创建活动',
            'apply_activity': '报名活动',
            'signup_activity': '报名活动',
            'submit_news': '发布新闻',
            'submit_feedback': '提交反馈',
            'create_project_request': '创建项目'
        };

        var allFields = {
            'create_activity_request': [
                { label: '活动名称', key: 'name', type: 'text', placeholder: '例如：Java技术分享会' },
                { label: '活动地点', key: 'location', type: 'text', placeholder: '例如：文渊楼301' },
                { label: '活动描述', key: 'description', type: 'textarea', placeholder: '请描述活动内容...' },
                { label: '开始时间', key: 'start_time', type: 'datetime-local', placeholder: '' },
                { label: '结束时间', key: 'end_time', type: 'datetime-local', placeholder: '' },
                { label: '报名开始时间', key: 'reg_start', type: 'datetime-local', placeholder: '' },
                { label: '报名截止时间', key: 'reg_end', type: 'datetime-local', placeholder: '' },
                { label: '最大参与人数', key: 'max_participants', type: 'number', placeholder: '不填则不限制' }
            ],
            'apply_activity': [
                { label: '活动ID', key: 'activity_id', type: 'text', placeholder: '请输入活动ID' }
            ],
            'signup_activity': [
                { label: '活动ID', key: 'activity_id', type: 'text', placeholder: '请输入活动ID' }
            ],
            'submit_news': [
                { label: '标题', key: 'title', type: 'text', placeholder: '请输入新闻标题' },
                { label: '摘要', key: 'summary', type: 'textarea', placeholder: '请输入新闻摘要...' },
                { label: '内容', key: 'content', type: 'textarea', placeholder: '请输入新闻详细内容...' },
                { label: '分类', key: 'type', type: 'select', options: [
                    { value: 'activity', label: '活动新闻' },
                    { value: 'notice', label: '通知公告' },
                    { value: 'tech', label: '技术分享' },
                    { value: 'award', label: '获奖荣誉' },
                    { value: 'recruit', label: '招新招聘' }
                ]}
            ],
            'submit_feedback': [
                { label: '标题', key: 'title', type: 'text', placeholder: '请输入标题' },
                { label: '内容', key: 'content', type: 'textarea', placeholder: '请输入内容...' },
                { label: '分类', key: 'category', type: 'select', options: ['技术', '学术', '文艺', '体育', '其他'] }
            ],
            'create_project_request': [
                { label: '项目名称', key: 'name', type: 'text', placeholder: '例如：校园二手平台' },
                { label: '项目描述', key: 'description', type: 'textarea', placeholder: '请描述项目内容...' },
                { label: '项目分类', key: 'category', type: 'select', options: ['技术', '学术', '创业', '文艺', '其他'] },
                { label: '预计开始日期', key: 'expected_start_date', type: 'date', placeholder: '例如：2026-05-01' },
                { label: '预计结束日期', key: 'expected_end_date', type: 'date', placeholder: '例如：2026-12-31' },
                { label: '仓库地址', key: 'repo_url', type: 'text', placeholder: '例如：https://github.com/...' },
                { label: '项目预算', key: 'budget', type: 'number', placeholder: '预算金额（元）' }
            ],
            'submit_award': [
                { label: '比赛名称', key: 'competition', type: 'text', placeholder: '例如：ACM国际大学生程序设计竞赛' },
                { label: '比赛时间', key: 'compTime', type: 'date', placeholder: '例如：2026-05-01' },
                { label: '比赛地点', key: 'compLocation', type: 'text', placeholder: '比赛举办地点' },
                { label: '比赛届别', key: 'compSession', type: 'text', placeholder: '例如：第12届' },
                { label: '比赛等级', key: 'compLevel', type: 'select', options: [
                    { value: '2', label: '国家级' },
                    { value: '1', label: '省级' },
                    { value: '3', label: '地区级别' },
                    { value: '4', label: '其他级别' }
                ]},
                { label: '奖项类型', key: 'awardType', type: 'select', options: [
                    { value: '5', label: '个人' },
                    { value: '6', label: '团队' }
                ]},
                { label: '奖项类别', key: 'awardCategory', type: 'select', options: [
                    { value: '7', label: '项目' },
                    { value: '8', label: '经典算法' },
                    { value: '9', label: '人工智能' },
                    { value: '10', label: '文档类' },
                    { value: '11', label: '其他' }
                ]},
                { label: '团队名称', key: 'teamName', type: 'text', placeholder: '团队奖项请填写团队名称' },
                { label: '奖项等级', key: 'awardLevel', type: 'select', options: [
                    { value: '21', label: '一等奖' },
                    { value: '22', label: '二等奖' },
                    { value: '23', label: '三等奖' },
                    { value: '24', label: '优胜奖' },
                    { value: '25', label: '参与奖' }
                ]}
            ]
        };

        var displayFields = allFields[actionType] || missingFields;

        for (var i = 0; i < displayFields.length; i++) {
            var field = displayFields[i];
            var fieldLabel = field.label || field;
            var fieldKey = field.key || field;
            var fieldConfig = {
                type: field.type || 'text',
                placeholder: field.placeholder || '',
                options: field.options || []
            };

            var groupDiv = document.createElement('div');
            groupDiv.className = 'info-form-group';

            var label = document.createElement('label');
            label.className = 'form-label';
            label.textContent = fieldLabel;
            if (fieldConfig.type === 'datetime-local') {
                label.textContent += '（格式：2026-04-15 09:00）';
            }
            groupDiv.appendChild(label);

            if (fieldConfig.type === 'textarea') {
                var textarea = document.createElement('textarea');
                textarea.className = 'form-control';
                textarea.id = 'field_' + i;
                textarea.name = fieldKey;
                textarea.placeholder = fieldConfig.placeholder;
                textarea.rows = 3;
                groupDiv.appendChild(textarea);
            } else if (fieldConfig.type === 'select') {
                var select = document.createElement('select');
                select.className = 'form-select';
                select.id = 'field_' + i;
                select.name = fieldKey;
                var defaultOpt = document.createElement('option');
                defaultOpt.value = '';
                defaultOpt.textContent = '请选择';
                select.appendChild(defaultOpt);
                for (var j = 0; j < fieldConfig.options.length; j++) {
                    var opt = document.createElement('option');
                    var optData = fieldConfig.options[j];
                    if (typeof optData === 'object') {
                        opt.value = optData.value;
                        opt.textContent = optData.label;
                    } else {
                        opt.value = optData;
                        opt.textContent = optData;
                    }
                    select.appendChild(opt);
                }
                groupDiv.appendChild(select);
            } else if (fieldConfig.type === 'number') {
                var input = document.createElement('input');
                input.type = 'number';
                input.className = 'form-control';
                input.id = 'field_' + i;
                input.name = fieldKey;
                input.placeholder = fieldConfig.placeholder;
                input.min = 1;
                groupDiv.appendChild(input);
            } else {
                var input = document.createElement('input');
                input.type = fieldConfig.type;
                input.className = 'form-control';
                input.id = 'field_' + i;
                input.name = fieldKey;
                input.placeholder = fieldConfig.placeholder;
                groupDiv.appendChild(input);
            }

            container.appendChild(groupDiv);
        }

        document.getElementById('pendingActionType').value = actionType || pendingActionInfo.actionType;
        document.getElementById('panelTitle').textContent = (actionLabels[actionType] || actionType) + ' - 填写信息';
        showFormPanel();
    }

    document.getElementById('submitInfoBtn').addEventListener('click', function() {
        var actionType = document.getElementById('pendingActionType').value;
        if (!actionType) {
            console.error("No action type found!");
            return;
        }

        var form = document.getElementById('infoCollectForm');
        var formData = new FormData(form);
        var paramsStr = '';
        var first = true;
        var displayParts = [];
        var keyToLabelMap = {
            'name': '奖项名称', 'location': '活动地点', 'description': '获奖描述',
            'start_time': '开始时间', 'end_time': '结束时间',
            'reg_start': '报名开始时间', 'reg_end': '报名截止时间',
            'max_participants': '最大参与人数', 'activity_id': '活动ID',
            'title': '标题', 'content': '内容', 'category': '分类',
            'expected_start_date': '预计开始日期', 'expected_end_date': '预计结束日期',
            'repo_url': '仓库地址', 'budget': '项目预算',
            'competition': '比赛名称', 'compTime': '比赛时间',
            'compLocation': '比赛地点', 'compSession': '比赛届别',
            'compLevel': '比赛等级', 'awardType': '奖项类型',
            'awardCategory': '奖项类别', 'teamName': '团队名称', 'awardLevel': '奖项等级'
        };
        
        for (var pair of formData.entries()) {
            if (pair[1] && pair[1].trim()) {
                if (!first) paramsStr += '|';
                paramsStr += pair[0] + '=' + pair[1].trim();
                var label = keyToLabelMap[pair[0]] || pair[0];
                displayParts.push(label + '：' + pair[1].trim());
                first = false;
            }
        }

        var collectedInfo = '提供了信息：' + displayParts.join('，');
        var sessionId = document.getElementById('sessionId').value;
        var messagesDiv = document.getElementById('chatMessages');

        var userMsgDiv = document.createElement('div');
        userMsgDiv.className = 'message user';
        userMsgDiv.innerHTML = '<div class="message-content">' + collectedInfo + '</div>' +
            '<div class="message-time">付朝阳 · ' + new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) + '</div>';
        messagesDiv.appendChild(userMsgDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;

        var assistantMsgDiv = createAssistantMessage();
        messagesDiv.appendChild(assistantMsgDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
        var assistantContentDiv = assistantMsgDiv.querySelector('.message-content');
        assistantContentDiv.innerHTML = '<i class="bi bi-hourglass"></i> 正在处理您的请求...</div>';

        var panel = document.getElementById('infoFormPanel');
        var overlay = document.getElementById('formOverlay');
        
        panel.style.display = 'none';
        overlay.style.display = 'none';
        panel.classList.remove('show');
        overlay.classList.remove('show');
        
        form.reset();
        pendingActionInfo = null;
        document.getElementById('pendingActionType').value = '';

         fetch('${pageContext.request.contextPath}/ai?action=execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'actionType=' + encodeURIComponent(actionType) + '&actionString=' + encodeURIComponent(paramsStr) + '&session_id=' + encodeURIComponent(sessionId)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                var resultHtml = '<div class="action-result success"><i class="bi bi-check-circle text-success"></i> ' + data.message + '</div>';

                if (data.type === 'table' && data.data && data.columns) {
                    var colToKey = {
                        '编号': 'id', '标题': 'title', '类型': 'type', '摘要': 'summary', 
                        '状态': 'status', '发布时间': 'createdAt', '申请人': 'authorId',
                        'ID': 'ID', '活动名称': '活动名称', '时间': '时间', '地点': '地点'
                    };
                    resultHtml += '<div class="table-responsive mt-2"><table class="table table-sm table-bordered"><thead><tr>';
                    var columns = data.columns;
                    for (var i = 0; i < columns.length; i++) {
                        resultHtml += '<th>' + columns[i] + '</th>';
                    }
                    resultHtml += '</tr></thead><tbody>';
                    var rows = data.data;
                    for (var i = 0; i < rows.length; i++) {
                        resultHtml += '<tr>';
                        for (var j = 0; j < columns.length; j++) {
                            var key = colToKey[columns[j]] || columns[j];
                            var val = rows[i][key];
                            if (val == null) val = '-';
                            resultHtml += '<td>' + val + '</td>';
                        }
                        resultHtml += '</tr>';
                    }
                    resultHtml += '</tbody></table></div>';
                } else if (data.type === 'redirect' && data.redirectUrl) {
                    resultHtml += '<div class="mt-2"><a href="${pageContext.request.contextPath}' + data.redirectUrl + '" class="btn btn-sm btn-primary">立即跳转</a></div>';
                }
                assistantContentDiv.innerHTML = resultHtml;
            } else {
                if (data.need_more_info) {
                    checkAndShowInfoForm(data.message, actionType);
                    var parent = assistantContentDiv.parentNode;
                    if (parent) parent.remove();
                } else {
                    assistantContentDiv.innerHTML = '<div class="action-result error"><i class="bi bi-x-circle text-danger"></i> ' + data.message + '</div>';
                }
            }
        })
        .catch(err => {
            assistantContentDiv.innerHTML = '<span class="text-danger">操作执行失败，请稍后重试。</span>';
            console.error(err);
        });
    });

    function executeActionFromAI(actionStr, contentDiv, sessionId) {
        console.log("executeActionFromAI called with:", actionStr);
        var firstPipeIndex = actionStr.indexOf('|');
        var actionType, params;

        if (firstPipeIndex === -1) {
            actionType = actionStr.trim();
            params = '';
        } else {
            actionType = actionStr.substring(0, firstPipeIndex).trim();
            params = actionStr.substring(firstPipeIndex + 1).trim();
        }

        console.log("actionType:", actionType, "params:", params);

        var targetDiv = contentDiv;
        if (!targetDiv) {
            var messagesDiv = document.getElementById('chatMessages');
            var assistantMsgDiv = createAssistantMessage();
            messagesDiv.appendChild(assistantMsgDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
            targetDiv = assistantMsgDiv.querySelector('.message-content');
        }
        targetDiv.innerHTML = '<div class="action-executing"><i class="bi bi-hourglass"></i> 正在执行操作...</div>';

        fetch('${pageContext.request.contextPath}/ai?action=execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'actionType=' + encodeURIComponent(actionType) + '&actionString=' + encodeURIComponent(params) + '&session_id=' + encodeURIComponent(sessionId)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                var resultHtml = '<div class="action-result success"><i class="bi bi-check-circle text-success"></i> ' + data.message + '</div>';

                if (data.type === 'table' && data.data && data.columns) {
                    var colToKey = {
                        '编号': 'id', '标题': 'title', '类型': 'type', '摘要': 'summary', 
                        '状态': 'status', '发布时间': 'createdAt', '申请人': 'authorId',
                        'ID': 'ID', '活动名称': '活动名称', '时间': '时间', '地点': '地点'
                    };
                    resultHtml += '<div class="table-responsive mt-2"><table class="table table-sm table-bordered"><thead><tr>';
                    var columns = data.columns;
                    for (var i = 0; i < columns.length; i++) {
                        resultHtml += '<th>' + columns[i] + '</th>';
                    }
                    resultHtml += '</tr></thead><tbody>';
                    var rows = data.data;
                    for (var i = 0; i < rows.length; i++) {
                        resultHtml += '<tr>';
                        for (var j = 0; j < columns.length; j++) {
                            var key = colToKey[columns[j]] || columns[j];
                            var val = rows[i][key];
                            if (val == null) val = '-';
                            resultHtml += '<td>' + val + '</td>';
                        }
                        resultHtml += '</tr>';
                    }
                    resultHtml += '</tbody></table></div>';
                } else if (data.type === 'statistics' && data.data) {
                    resultHtml += '<div class="mt-2"><ul class="list-group">';
                    for (var key in data.data) {
                        resultHtml += '<li class="list-group-item d-flex justify-content-between align-items-center">' + key + '<span class="badge bg-primary">' + data.data[key] + '</span></li>';
                    }
                    resultHtml += '</ul></div>';
                } else if (data.type === 'participants' && data.data) {
                    resultHtml += '<div class="mt-2"><ul class="list-group">';
                    for (var i = 0; i < data.data.length; i++) {
                        var u = data.data[i];
                        resultHtml += '<li class="list-group-item">' + u.name + ' (' + u.username + ')</li>';
                    }
                    resultHtml += '</ul></div>';
                } else if (data.type === 'redirect' && data.redirectUrl) {
                    resultHtml += '<div class="mt-2"><a href="${pageContext.request.contextPath}' + data.redirectUrl + '" class="btn btn-sm btn-primary">立即跳转</a></div>';
                } else if (data.type === 'view_list' && data.data) {
                    resultHtml += '<div class="mt-2"><a href="${pageContext.request.contextPath}/activity?action=myActivities" class="btn btn-sm btn-outline-primary">查看详情</a></div>';
                } else if (data.type === 'categorized_news' && data.categories) {
                    console.log("Categorized news data:", data);
                    var categories = data.categories;
                    for (var i = 0; i < categories.length; i++) {
                        var cat = categories[i];
                        console.log("Category:", cat.name, "Count:", cat.count, "Items:", cat.items);
                        var items = cat.items;
                        if (items && items.length > 0) {
                            resultHtml += '<div class="mt-3"><h6><i class="bi bi-folder-fill me-1"></i>' + cat.name + ' (' + items.length + ')</h6>';
                            resultHtml += '<div class="list-group list-group-flush">';
                            for (var j = 0; j < items.length; j++) {
                                var news = items[j];
                                resultHtml += '<div class="list-group-item px-0 py-2">';
                                resultHtml += '<div class="d-flex justify-content-between align-items-start">';
                                resultHtml += '<div><h6 class="mb-1">' + news.title + '</h6>';
                                if (news.summary) {
                                    resultHtml += '<small class="text-muted">' + news.summary.substring(0, 50) + (news.summary.length > 50 ? '...' : '') + '</small>';
                                }
                                resultHtml += '</div>';
                                resultHtml += '<small class="text-muted">' + (news.createdAt || '') + '</small>';
                                resultHtml += '</div></div>';
                            }
                            resultHtml += '</div></div>';
                        }
                    }
                }
                targetDiv.innerHTML = resultHtml;
            } else {
                if (data.need_more_info || (data.message && data.message.indexOf('请提供') !== -1)) {
                    var needMore = checkAndShowInfoForm(data.message, actionType);
                    if (needMore) {
                        targetDiv.innerHTML = '<div class="action-result"><i class="bi bi-clipboard me-1"></i> ' + data.message.replace(/\n/g, '<br>') + '</div>';
                        showFormPanel();
                        return;
                    }
                }
                targetDiv.innerHTML = '<div class="action-result error"><i class="bi bi-x-circle text-danger"></i> ' + data.message + '</div>';
            }
        })
        .catch(err => {
            targetDiv.innerHTML = '<span class="text-danger">操作执行失败，请稍后重试。</span>';
            console.error(err);
        });
    }
    </script>
</body>
</html>