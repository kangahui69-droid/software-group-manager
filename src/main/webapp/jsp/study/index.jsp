<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>学习中心 - 软件小组</title>
    <link rel="stylesheet" href="${ctx}/css/study.css">
</head>
<body>
    <div class="study-page">
        <div class="slogan-container">
            <div class="slogan-left">好好学习</div>

            <div class="study-card">
            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <i class="icon">&#10060;</i> ${error}
                </div>
            </c:if>

            <c:if test="${not empty autoEnd}">
                <div class="alert alert-success">
                    <i class="icon">&#10004;</i> ${autoEnd}
                </div>
            </c:if>

            <!-- 日期显示 -->
            <div class="date-header">
                <div class="date-main">
                    <span class="day"><fmt:formatDate value="${nowDate}" pattern="dd"/></span>
                    <div class="date-info">
                        <span class="year-month"><fmt:formatDate value="${nowDate}" pattern="yyyy年MM月"/></span>
                        <span class="weekday"><fmt:formatDate value="${nowDate}" pattern="EEEE"/></span>
                    </div>
                </div>
            </div>

            <!-- 用户信息 -->
            <div class="user-section">
                <div class="user-avatar">
                    <img src="${ctx}/file/avatar/${currentUser.id}"
                         onerror="this.src='${ctx}/images/avatar/default-avatar.svg'"
                         alt="头像">
                </div>
                <div class="user-details">
                    <span class="user-name">${currentUser.name}</span>
                    <span class="user-role">${currentUser.role == 'MEMBER' ? '成员' : '管理员'}</span>
                </div>
            </div>

            <!-- 学习状态 -->
            <div class="status-section">
                <c:choose>
                    <c:when test="${not empty activeSession}">
                        <div class="status-active">
                            <div class="status-icon studying">&#128218;</div>
                            <div class="status-text">
                                <span class="status-label">正在进行学习</span>
                                <span class="status-time">开始时间: <fmt:formatDate value="${activeSession.checkInTime}" pattern="HH:mm:ss"/></span>
                                <span class="status-duration" id="currentDuration">已学习: 计算中...</span>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="status-idle">
                            <div class="status-icon idle">&#128152;</div>
                            <div class="status-text">
                                <span class="status-label">准备开始学习</span>
                                <span class="status-time">点击下方按钮开始计时</span>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- 今日学习时长 -->
            <div class="duration-section">
                <div class="duration-item">
                    <span class="duration-label">今日学习时长</span>
                    <span class="duration-value">
                        <c:choose>
                            <c:when test="${todayDuration > 0}">
                                ${todayDuration} 分钟
                            </c:when>
                            <c:otherwise>
                                <span style="font-size: 16px; color: #999;">今日还未学习</span>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="duration-item">
                    <span class="duration-label">学习时段</span>
                    <span class="duration-value time-range">19:00 - 22:00</span>
                </div>
            </div>

            <!-- 统计信息 -->
            <div class="today-stats">
                <div class="stat-row">
                    <span class="stat-label">今日学习次数:</span>
                    <span class="stat-value">${completedToday} 次</span>
                </div>
                <div class="stat-row">
                    <span class="stat-label">本周学习时长:</span>
                    <span class="stat-value">${weekDuration} 分钟</span>
                </div>
                <div class="stat-row">
                    <span class="stat-label">连续学习:</span>
                    <span class="stat-value">${consecutiveDays} 天</span>
                </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-section">
                <c:choose>
                    <c:when test="${not empty activeSession}">
                        <button class="btn btn-study btn-end" id="btnEndStudy" onclick="endStudy()">
                            <span class="btn-icon">&#9209;</span>
                            <span class="btn-text">结束学习</span>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button class="btn btn-study btn-start" id="btnStartStudy" onclick="startStudy()">
                            <span class="btn-icon">&#9654;</span>
                            <span class="btn-text">开始学习</span>
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- 温馨提示 -->
            <div class="tips-section">
                <h4>&#128161; 学习提醒</h4>
                <ul>
                    <li>可多次学习，系统会自动累计时长</li>
                    <li><a href="javascript:void(0)" onclick="showRules()" style="color: #11998e; text-decoration: underline;">点击查看学习规则</a></li>
                </ul>
            </div>

            <!-- 学习规则弹窗 -->
            <div id="rulesModal" class="modal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000;">
                <div class="modal-content" style="background: #fff; margin: 10% auto; padding: 25px; border-radius: 12px; max-width: 400px; position: relative;">
                    <span onclick="closeRules()" style="position: absolute; right: 15px; top: 10px; font-size: 24px; cursor: pointer; color: #999;">&times;</span>
                    <h3 style="margin: 0 0 15px; color: #333; text-align: center;">学习规则</h3>
                    <div style="text-align: left; color: #666; font-size: 14px; line-height: 1.8;">
                        <p><strong>签到规则：</strong></p>
                        <ul style="margin: 10px 0 15px 20px;">
                            <li>白天可随时开始学习</li>
                            <li>18:00 强制签退（如在学习中）</li>
                            <li>18:30 - 19:00 签到 = 早到</li>
                            <li>19:00 - 19:30 签到 = 正常</li>
                            <li>19:30 后签到 = 迟到</li>
                        </ul>
                        <p><strong>签退规则：</strong></p>
                        <ul style="margin: 10px 0 15px 20px;">
                            <li>21:30 前签退 = 早退</li>
                            <li>21:30 后签退 = 正常</li>
                            <li>22:00 系统自动结束学习</li>
                        </ul>
                    </div>
                </div>
            </div>

            <script>
                function showRules() {
                    document.getElementById('rulesModal').style.display = 'block';
                }
                function closeRules() {
                    document.getElementById('rulesModal').style.display = 'none';
                }
                // 点击弹窗外部关闭
                window.onclick = function(event) {
                    var modal = document.getElementById('rulesModal');
                    if (event.target == modal) {
                        modal.style.display = 'none';
                    }
                }
            </script>

            <!-- 底部链接 -->
            <div class="footer-links">
                <a href="${ctx}/study/list" class="link-item">
                    <span class="link-icon">&#128203;</span>
                    <span>学习记录</span>
                </a>
                <a href="${ctx}/index.jsp" class="link-item">
                    <span class="link-icon">&#127968;</span>
                    <span>返回首页</span>
                </a>
            </div>
        </div>
            <div class="slogan-right">天天向上</div>
        </div>
    </div>

    <script>
        var ctx = '${ctx}';
        var checkInTime = null;

        <c:if test="${not empty activeSession}">
            checkInTime = new Date('<fmt:formatDate value="${activeSession.checkInTime}" pattern="yyyy-MM-dd HH:mm:ss"/>').getTime();
            // 每秒更新当前学习时长
            setInterval(updateCurrentDuration, 1000);
        </c:if>

        function updateCurrentDuration() {
            if (!checkInTime) return;

            var now = new Date().getTime();
            var duration = Math.floor((now - checkInTime) / 1000 / 60);
            var hours = Math.floor(duration / 60);
            var minutes = duration % 60;
            var seconds = Math.floor((now - checkInTime) / 1000) % 60;

            var text = '';
            if (hours > 0) {
                text = hours + ' 小时 ' + minutes + ' 分 ' + seconds + ' 秒';
            } else if (minutes > 0) {
                text = minutes + ' 分 ' + seconds + ' 秒';
            } else {
                text = seconds + ' 秒';
            }

            var el = document.getElementById('currentDuration');
            if (el) {
                el.textContent = '已学习: ' + text;
            }
        }

        function startStudy() {
            var btn = document.getElementById('btnStartStudy');
            if (btn.disabled) return;

            if (!confirm('确定要开始学习吗？')) return;

            btn.disabled = true;
            btn.querySelector('.btn-text').textContent = '开始中...';

            fetch(ctx + '/study', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=start'
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    alert(data.message);
                    location.reload();
                } else {
                    alert(data.message || '开始学习失败');
                    btn.disabled = false;
                    btn.querySelector('.btn-text').textContent = '开始学习';
                }
            })
            .catch(function(error) {
                console.error('错误:', error);
                alert('网络异常，请重试');
                btn.disabled = false;
                btn.querySelector('.btn-text').textContent = '开始学习';
            });
        }

        function endStudy() {
            var btn = document.getElementById('btnEndStudy');
            if (btn.disabled) return;

            if (!confirm('确定要结束学习吗？')) return;

            btn.disabled = true;
            btn.querySelector('.btn-text').textContent = '结束中...';

            fetch(ctx + '/study', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=end'
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    var msg = '学习结束！';
                    if (data.duration) {
                        msg += '\n本次学习时长：' + data.duration + ' 分钟';
                    }
                    alert(msg);
                    location.reload();
                } else {
                    alert(data.message || '结束学习失败');
                    btn.disabled = false;
                    btn.querySelector('.btn-text').textContent = '结束学习';
                }
            })
            .catch(function(error) {
                console.error('错误:', error);
                alert('网络异常，请重试');
                btn.disabled = false;
                btn.querySelector('.btn-text').textContent = '结束学习';
            });
        }
    </script>
</body>
</html>
