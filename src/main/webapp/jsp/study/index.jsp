<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="学习中心"/>
    <jsp:param name="active" value="study"/>
</jsp:include>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">

<style>
    .member-hero {
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        border-radius: var(--radius-generous);
        padding: 32px 40px;
        margin-bottom: 32px;
        color: white;
    }

    .member-hero-title {
        font-family: var(--font-display);
        font-size: 1.75rem;
        font-weight: 600;
        margin-bottom: 8px;
    }

    .member-hero-subtitle {
        font-family: var(--font-ui);
        font-size: 0.94rem;
        opacity: 0.9;
    }

    .card-design {
        background: var(--bg-white);
        border-radius: var(--radius-generous);
        box-shadow: var(--shadow-brand-purple);
        border: none;
        overflow: hidden;
    }

    .card-header-design {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-light);
        background: rgba(20, 86, 240, 0.03);
    }

    .card-title-design {
        font-family: var(--font-display);
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--text-dark);
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .card-body-design {
        padding: 24px;
    }

    .stat-card {
        background: var(--bg-white);
        border-radius: var(--radius-comfortable);
        padding: 20px;
        box-shadow: var(--shadow-standard);
        border: 1px solid var(--border-light);
        text-align: center;
        transition: all 0.3s ease;
    }

    .stat-card:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-purple);
    }

    .stat-number {
        font-family: var(--font-display);
        font-size: 2rem;
        font-weight: 600;
        margin-bottom: 4px;
    }

    .stat-label {
        font-family: var(--font-ui);
        font-size: 0.81rem;
        color: var(--text-muted);
    }

    .btn-brand {
        background-color: var(--brand-blue);
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-brand:hover {
        background-color: var(--primary-600);
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-danger {
        background-color: #ef4444;
        color: white;
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 600;
        border: none;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-danger:hover {
        background-color: #dc2626;
        color: white;
        transform: translateY(-2px);
        box-shadow: var(--shadow-standard);
    }

    .btn-outline-brand {
        background: transparent;
        color: var(--brand-blue);
        border: 1px solid var(--brand-blue);
        border-radius: var(--radius-standard);
        padding: 10px 20px;
        font-weight: 500;
        transition: all 0.3s ease;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }

    .btn-outline-brand:hover {
        background: var(--brand-blue);
        color: white;
    }

    .avatar-lg-custom {
        width: 64px;
        height: 64px;
        border-radius: var(--radius-comfortable);
        background: linear-gradient(135deg, var(--brand-blue), var(--primary-light));
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        font-weight: 600;
    }

    .user-info h4 {
        font-family: var(--font-display);
        font-weight: 600;
        color: var(--text-dark);
        margin-bottom: 4px;
    }

    .badge-design {
        font-family: var(--font-ui);
        font-size: 0.75rem;
        font-weight: 500;
        padding: 4px 12px;
        border-radius: var(--radius-pill);
    }

    .back-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: var(--radius-standard);
        background: rgba(255, 255, 255, 0.2);
        color: white;
        text-decoration: none;
        font-size: 0.875rem;
        transition: all 0.2s ease;
    }
    .back-btn:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
    }
</style>

<div class="page-body">
    <div class="container-xl">
        <div class="member-hero">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <a href="${pageContext.request.contextPath}/member/index.jsp" class="back-btn">
                    <i class="bi bi-arrow-left"></i>返回
                </a>
            </div>
            <h1 class="member-hero-title">
                <i class="bi bi-book me-2"></i>学习中心
            </h1>
            <p class="member-hero-subtitle">
                <fmt:formatDate value="${nowDate}" pattern="yyyy年MM月dd日 EEEE"/>
            </p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible">
                <i class="bi bi-exclamation-circle me-2"></i>${error}
            </div>
        </c:if>
        <c:if test="${not empty autoEnd}">
            <div class="alert alert-success alert-dismissible">
                <i class="bi bi-check-circle me-2"></i>${autoEnd}
            </div>
        </c:if>

        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-display"></i>学习状态
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <div class="row align-items-center mb-4">
                            <div class="col-auto">
                                <div class="avatar-lg-custom">
                                    ${not empty sessionScope.user.name ? sessionScope.user.name.charAt(0) : sessionScope.user.username.charAt(0)}
                                </div>
                            </div>
                            <div class="col">
                                <div class="user-info">
                                    <h4>${not empty sessionScope.user.name ? sessionScope.user.name : sessionScope.user.username}</h4>
                                    <div style="color: var(--text-muted);">
                                        ${sessionScope.user.role == 'MEMBER' ? '成员' : (sessionScope.user.role == 'ADMIN' ? '管理员' : '教师')}
                                    </div>
                                </div>
                            </div>
                            <div class="col-auto text-end">
                                <c:choose>
                                    <c:when test="${not empty activeSession}">
                                        <span class="badge-design" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">学习中</span>
                                        <div class="mt-2" style="color: var(--text-muted); font-size: 0.875rem;">
                                            <i class="bi bi-clock me-1"></i>
                                            <fmt:formatDate value="${activeSession.checkInTime}" pattern="HH:mm:ss"/> 开始
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-design" style="background: rgba(100, 116, 139, 0.1); color: #64748b;">空闲中</span>
                                        <div class="mt-2" style="color: var(--text-muted); font-size: 0.875rem;">准备开始学习</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <c:if test="${not empty activeSession}">
                            <div class="text-center mb-4">
                                <div class="stat-number" style="color: #10b981;" id="currentDuration">计算中...</div>
                            </div>
                        </c:if>

                        <c:choose>
                            <c:when test="${not empty activeSession}">
                                <button class="btn-danger w-100" id="btnEndStudy" onclick="endStudy()">
                                    <i class="bi bi-stop-fill"></i>结束学习
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button class="btn-brand w-100" id="btnStartStudy" onclick="startStudy()">
                                    <i class="bi bi-play-fill"></i>开始学习
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="row g-3 mt-3">
                    <div class="col-sm-6 col-lg-3">
                        <div class="stat-card">
                            <div class="stat-number" style="color: var(--brand-blue);">${todayDuration}</div>
                            <div class="stat-label">今日学习(分钟)</div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-lg-3">
                        <div class="stat-card">
                            <div class="stat-number" style="color: #10b981;">${completedToday}</div>
                            <div class="stat-label">今日次数</div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-lg-3">
                        <div class="stat-card">
                            <div class="stat-number" style="color: #8b5cf6;">${weekDuration}</div>
                            <div class="stat-label">本周学习(分钟)</div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-lg-3">
                        <div class="stat-card">
                            <div class="stat-number" style="color: #f59e0b;">${consecutiveDays}</div>
                            <div class="stat-label">连续学习(天)</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card-design">
                    <div class="card-header-design">
                        <h3 class="card-title-design">
                            <i class="bi bi-info-circle"></i>学习规则
                        </h3>
                    </div>
                    <div class="card-body-design">
                        <h5 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 12px; color: var(--text-dark);">签到规则</h5>
                        <ul style="list-style: none; padding: 0; margin: 0 0 20px 0;">
                            <li style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; color: var(--text-secondary);">
                                <i class="bi bi-check2" style="color: #10b981;"></i>6:00-22:00 可随时开始学习
                            </li>
                            <li style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; color: var(--text-secondary);">
                                <i class="bi bi-check2" style="color: #10b981;"></i>18:00 强制签退
                            </li>
                            <li style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; color: var(--text-secondary);">
                                <i class="bi bi-check2" style="color: #10b981;"></i>6:00-18:00 = 早到
                            </li>
                            <li style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; color: var(--text-secondary);">
                                <i class="bi bi-check2" style="color: #10b981;"></i>18:00-19:00 = 正常
                            </li>
                            <li style="display: flex; align-items: center; gap: 8px; color: var(--text-secondary);">
                                <i class="bi bi-x-circle" style="color: #ef4444;"></i>19:00后 = 迟到
                            </li>
                        </ul>
                        <h5 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 12px; color: var(--text-dark);">签退规则</h5>
                        <ul style="list-style: none; padding: 0; margin: 0;">
                            <li style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; color: var(--text-secondary);">
                                <i class="bi bi-check2" style="color: #10b981;"></i>21:30前签退 = 早退
                            </li>
                            <li style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px; color: var(--text-secondary);">
                                <i class="bi bi-check2" style="color: #10b981;"></i>21:30后签退 = 正常
                            </li>
                            <li style="display: flex; align-items: center; gap: 8px; color: var(--text-secondary);">
                                <i class="bi bi-x-circle" style="color: #ef4444;"></i>22:00 系统自动结束
                            </li>
                        </ul>
                    </div>
                </div>

                <div class="mt-3">
                    <a href="${ctx}/study/list" class="btn-outline-brand w-100 mb-2" style="justify-content: center;">
                        <i class="bi bi-list-ul"></i>学习记录
                    </a>
                    <button type="button" class="btn-outline-brand w-100" onclick="showRules()" style="justify-content: center;">
                        <i class="bi bi-question-circle"></i>详细规则
                    </button>
                </div>
            </div>
        </div>

        <dialog id="rulesModal" style="border: 1px solid var(--border-gray); border-radius: var(--radius-comfortable); padding: 24px; min-width: 350px; max-width: 500px; background: var(--bg-white);">
            <div style="text-align: center; margin-bottom: 20px;">
                <h5 style="font-family: var(--font-display); font-weight: 600; color: var(--text-dark); margin: 0;">学习规则</h5>
            </div>
            <div style="text-align: left;">
                <h6 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 12px;">签到规则：</h6>
                <ul style="margin-bottom: 16px; padding-left: 20px;">
                    <li>6:00 - 22:00 可随时开始学习</li>
                    <li>18:00 强制签退（如在学习中）</li>
                    <li>6:00 - 18:00 签到 = 早到</li>
                    <li>18:00 - 19:00 签到 = 正常</li>
                    <li>19:00 后签到 = 迟到</li>
                </ul>
                <h6 style="font-family: var(--font-display); font-weight: 600; margin-bottom: 12px;">签退规则：</h6>
                <ul style="margin-bottom: 0; padding-left: 20px;">
                    <li>21:30 前签退 = 早退</li>
                    <li>21:30 后签退 = 正常</li>
                    <li>22:00 系统自动结束学习</li>
                </ul>
            </div>
            <div style="text-align: right; margin-top: 20px;">
                <button type="button" class="btn-brand" onclick="closeRules()" style="padding: 8px 16px;">关闭</button>
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
    </div>
</div>

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

        console.log('Starting study, ctx:', ctx);
        console.log('Fetching:', ctx + '/study');

        fetch(ctx + '/study', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'action=start'
        })
        .then(function(response) {
            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);
            return response.json();
        })
        .then(function(data) {
            console.log('Response data:', data);
            if (data.success) {
                alert(data.message);
                location.reload(true);
            } else {
                alert(data.message || '开始学习失败');
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-play-fill"></i>开始学习';
            }
        })
        .catch(function(error) {
            console.error('错误:', error);
            alert('网络异常，请重试');
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-play-fill"></i>开始学习';
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
        .then(function(response) {
            console.log('Response status:', response.status);
            console.log('Response headers:', response.headers);
            return response.json();
        })
        .then(function(data) {
            console.log('Response data:', data);
            if (data.success) {
                var msg = '学习结束！';
                if (data.duration) {
                    msg += '\n本次学习时长：' + data.duration + ' 分钟';
                }
                alert(msg);
                location.reload(true);
            } else {
                alert(data.message || '结束学习失败');
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-stop-fill"></i>结束学习';
            }
        })
        .catch(function(error) {
            console.error('错误:', error);
            alert('网络异常，请重试');
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-stop-fill"></i>结束学习';
        });
    }
</script>

<jsp:include page="/jsp/common/layout_bottom.jsp"/>
