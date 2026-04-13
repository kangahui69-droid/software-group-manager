<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<style>
    .page-title { font-family: var(--font-display); font-size: 1.94rem; font-weight: 600; color: var(--text-dark); }
    .card-design { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); border: none; transition: all 0.3s ease; }
    .card-design:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .data-card { background: var(--bg-white); border-radius: var(--radius-generous); box-shadow: var(--shadow-brand-purple); padding: 24px; text-align: center; transition: all 0.3s ease; }
    .data-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-brand-offset); }
    .stat-value { font-family: var(--font-display); font-size: 2.5rem; font-weight: 600; }
    .stat-value-blue { color: var(--brand-blue); }
    .stat-value-green { color: #10b981; }
    .stat-value-purple { color: #8b5cf6; }
    .stat-value-orange { color: #f59e0b; }
    .stat-label { font-family: var(--font-ui); font-size: 0.88rem; color: var(--text-secondary); margin-top: 4px; }
    .btn-green { background-color: #10b981; color: white; border-radius: var(--radius-standard); padding: 14px 24px; font-weight: 600; border: none; transition: all 0.3s ease; width: 100%; }
    .btn-green:hover { background-color: #059669; color: white; transform: translateY(-2px); }
    .btn-danger { background-color: #ef4444; color: white; border-radius: var(--radius-standard); padding: 14px 24px; font-weight: 600; border: none; transition: all 0.3s ease; width: 100%; }
    .btn-danger:hover { background-color: #dc2626; color: white; transform: translateY(-2px); }
    .avatar-lg { width: 72px; height: 72px; border-radius: var(--radius-comfortable); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: 600; color: white; }
    .progress-bar-green { background-color: #10b981 !important; }
    .badge-green { background-color: #10b981; color: white; }
    .badge-secondary { background-color: #9ca3af; color: white; }
    .info-card { background: linear-gradient(135deg, rgba(20, 86, 240, 0.05), rgba(234, 94, 193, 0.05)); border-radius: var(--radius-comfortable); padding: 24px; border: 1px solid var(--border-light); }
    .info-title { font-family: var(--font-display); font-size: 1.25rem; font-weight: 500; color: var(--text-dark); margin-bottom: 16px; }
    .info-list { list-style: none; padding: 0; margin: 0; }
    .info-list li { display: flex; align-items: center; padding: 8px 0; color: var(--text-secondary); font-size: 0.94rem; }
    .icon-check { color: #10b981; margin-right: 12px; }
    .icon-x { color: #ef4444; margin-right: 12px; }
    .modal-design { border-radius: var(--radius-generous); border: none; padding: 24px; max-width: 500px; width: 90%; box-shadow: var(--shadow-brand-purple); }
</style>

<div class="page-wrapper">
    <div class="page-body">
        <div class="container-xl">
            <div class="page-header d-print-none mb-4">
                <div class="row align-items-center">
                    <div class="col">
                        <h2 class="page-title mb-1">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="28" height="28" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path></svg>
                            学习中心
                        </h2>
                        <div class="text-muted" style="font-size: 0.88rem; margin-top: 4px;">
                            <fmt:formatDate value="${nowDate}" pattern="yyyy年MM月dd日 EEEE"/>
                        </div>
                    </div>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger mb-4" role="alert" style="border-radius: var(--radius-standard); background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 16px;">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                    ${error}
                </div>
            </c:if>
            <c:if test="${not empty autoEnd}">
                <div class="alert alert-success mb-4" role="alert" style="border-radius: var(--radius-standard); background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0; padding: 16px;">
                    <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                    ${autoEnd}
                </div>
            </c:if>

            <div class="row" style="gap: 20px;">
                <div class="col-lg-8">
                    <div class="card-design">
                        <div class="card-body p-4">
                            <h3 class="card-title mb-4" style="font-family: var(--font-display); font-weight: 500; color: var(--text-dark);">学习状态</h3>
                            <div class="row align-items-center">
                                <div class="col-auto">
                                    <div class="avatar-lg" style="background: #11998e;">
                                        ${not empty currentUser.name ? currentUser.name.charAt(0) : currentUser.username.charAt(0)}
                                    </div>
                                </div>
                                <div class="col">
                                    <h4 class="mb-1" style="font-family: var(--font-display); color: var(--text-dark);">${currentUser.name}</h4>
                                    <div class="text-muted" style="font-size: 0.88rem;">
                                        ${currentUser.role == 'MEMBER' ? '成员' : '管理员'}
                                    </div>
                                </div>
                                <div class="col-auto text-end">
                                    <c:choose>
                                        <c:when test="${not empty activeSession}">
                                            <span class="badge badge-green">学习中</span>
                                            <div class="text-muted mt-1" style="font-size: 0.88rem;">
                                                <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="14" height="14" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                                                <fmt:formatDate value="${activeSession.checkInTime}" pattern="HH:mm:ss"/> 开始
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-secondary">空闲中</span>
                                            <div class="text-muted mt-1" style="font-size: 0.88rem;">准备开始学习</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <c:if test="${not empty activeSession}">
                                <div class="progress mt-3" style="height: 8px; border-radius: 4px; background: var(--bg-light-gray);">
                                    <div class="progress-bar progress-bar-green" style="width: 100%; border-radius: 4px;"></div>
                                </div>
                                <div class="mt-3 text-center">
                                    <span class="stat-value text-green" id="currentDuration" style="font-size: 2rem;">计算中...</span>
                                </div>
                            </c:if>

                            <div class="mt-4">
                                <c:choose>
                                    <c:when test="${not empty activeSession}">
                                        <button class="btn btn-danger" id="btnEndStudy" onclick="endStudy()">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>
                                            结束学习
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-green" id="btnStartStudy" onclick="startStudy()">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
                                            开始学习
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <div class="row mt-4" style="gap: 16px;">
                        <div class="col-sm-6 col-lg-3">
                            <div class="data-card">
                                <div class="stat-value stat-value-blue">${todayDuration}</div>
                                <div class="stat-label">今日学习(分钟)</div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-lg-3">
                            <div class="data-card">
                                <div class="stat-value stat-value-green">${completedToday}</div>
                                <div class="stat-label">今日次数</div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-lg-3">
                            <div class="data-card">
                                <div class="stat-value stat-value-purple">${weekDuration}</div>
                                <div class="stat-label">本周学习(分钟)</div>
                            </div>
                        </div>
                        <div class="col-sm-6 col-lg-3">
                            <div class="data-card">
                                <div class="stat-value stat-value-orange">${consecutiveDays}</div>
                                <div class="stat-label">连续学习(天)</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <div class="info-card mb-3">
                        <h3 class="info-title">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-1" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                            学习规则
                        </h3>
                        <h6 style="font-weight: 600; color: var(--text-dark); margin-bottom: 8px;">签到规则</h6>
                        <ul class="info-list">
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-check" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="20 6 9 17 4 12"></polyline></svg>6:00-22:00 可随时开始学习</li>
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-check" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="20 6 9 17 4 12"></polyline></svg>18:00 强制签退</li>
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-check" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="20 6 9 17 4 12"></polyline></svg>6:00-18:00 = 早到</li>
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-check" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="20 6 9 17 4 12"></polyline></svg>18:00-19:00 = 正常</li>
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-x" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>19:00后 = 迟到</li>
                        </ul>
                        <h6 style="font-weight: 600; color: var(--text-dark); margin: 16px 0 8px;">签退规则</h6>
                        <ul class="info-list mb-0">
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-check" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="20 6 9 17 4 12"></polyline></svg>21:30前签退 = 早退</li>
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-check" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><polyline points="20 6 9 17 4 12"></polyline></svg>21:30后签退 = 正常</li>
                            <li><svg xmlns="http://www.w3.org/2000/svg" class="icon-x" width="16" height="16" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>22:00 系统自动结束</li>
                        </ul>
                    </div>

                    <div class="info-card">
                        <a href="${ctx}/study/list" class="btn" style="background: var(--brand-blue); color: white; border-radius: var(--radius-standard); padding: 12px 20px; width: 100%; margin-bottom: 12px; font-weight: 600;">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="18" height="18" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><line x1="8" y1="6" x2="21" y2="6"></line><line x1="8" y1="12" x2="21" y2="12"></line><line x1="8" y1="18" x2="21" y2="18"></line><line x1="3" y1="6" x2="3.01" y2="6"></line><line x1="3" y1="12" x2="3.01" y2="12"></line><line x1="3" y1="18" x2="3.01" y2="18"></line></svg>
                            学习记录
                        </a>
                        <button type="button" class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 12px 20px; width: 100%; font-weight: 500;" onclick="showRules()">
                            <svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="18" height="18" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"></path><circle cx="12" cy="12" r="10"></circle><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
                            详细规则
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<dialog id="rulesModal" class="modal-design">
    <div style="text-align: center; margin-bottom: 20px;">
        <h5 style="font-family: var(--font-display); font-weight: 600; color: var(--text-dark);">学习规则</h5>
    </div>
    <div style="text-align: left;">
        <h6 style="font-weight: 600; color: var(--text-dark); margin-bottom: 12px;">签到规则：</h6>
        <ul style="padding-left: 20px; margin-bottom: 16px; color: var(--text-secondary);">
            <li>6:00 - 22:00 可随时开始学习</li>
            <li>18:00 强制签退（如在学习中）</li>
            <li>6:00 - 18:00 签到 = 早到</li>
            <li>18:00 - 19:00 签到 = 正常</li>
            <li>19:00 后签到 = 迟到</li>
        </ul>
        <h6 style="font-weight: 600; color: var(--text-dark); margin-bottom: 12px;">签退规则：</h6>
        <ul style="padding-left: 20px; margin-bottom: 0; color: var(--text-secondary);">
            <li>21:30 前签退 = 早退</li>
            <li>21:30 后签退 = 正常</li>
            <li>22:00 系统自动结束学习</li>
        </ul>
    </div>
    <div style="text-align: right; margin-top: 20px;">
        <button type="button" class="btn" style="background: var(--bg-light-gray); color: var(--text-dark); border-radius: var(--radius-standard); padding: 10px 20px;" onclick="closeRules()">关闭</button>
    </div>
</dialog>

<script>
function showRules() {
    document.getElementById('rulesModal').showModal();
}
function closeRules() {
    document.getElementById('rulesModal').close();
}
</script>

<script>
var ctx = '${ctx}';
var checkInTime = null;

<c:if test="${not empty activeSession}">
checkInTime = new Date('<fmt:formatDate value="${activeSession.checkInTime}" pattern="yyyy-MM-dd HH:mm:ss"/>').getTime();
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
        el.textContent = text;
    }
}

function startStudy() {
    var btn = document.getElementById('btnStartStudy');
    if (btn.disabled) return;

    if (!confirm('确定要开始学习吗？')) return;

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>开始中...';

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
            btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>开始学习';
        }
    })
    .catch(function(error) {
        console.error('错误:', error);
        alert('网络异常，请重试');
        btn.disabled = false;
        btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>开始学习';
    });
}

function endStudy() {
    var btn = document.getElementById('btnEndStudy');
    if (btn.disabled) return;

    if (!confirm('确定要结束学习吗？')) return;

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>结束中...';

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
            btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>结束学习';
        }
    })
    .catch(function(error) {
        console.error('错误:', error);
        alert('网络异常，请重试');
        btn.disabled = false;
        btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="icon me-2" width="20" height="20" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><rect x="6" y="4" width="4" height="16"></rect><rect x="14" y="4" width="4" height="16"></rect></svg>结束学习';
    });
}
</script>