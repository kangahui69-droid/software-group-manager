<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<jsp:include page="/jsp/common/layout_top.jsp">
    <jsp:param name="title" value="学习中心"/>
    <jsp:param name="active" value="study"/>
</jsp:include>

            <div class="page-wrapper">
                <div class="page-body">
                    <div class="container-xl">
                        <!-- 页面标题 -->
                        <div class="page-header d-print-none">
                            <div class="row align-items-center">
                                <div class="col">
                                    <h2 class="page-title">
                                        <i class="bi bi-book me-2"></i>学习中心
                                    </h2>
                                    <div class="text-muted mt-1">
                                        <fmt:formatDate value="${nowDate}" pattern="yyyy年MM月dd日 EEEE"/>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 提示信息 -->
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

                        <div class="row row-cards">
                            <!-- 学习状态卡片 -->
                            <div class="col-lg-8">
                                <div class="card">
                                    <div class="card-header">
                                        <h3 class="card-title">学习状态</h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="row align-items-center">
                                            <!-- 用户信息 -->
                                            <div class="col-auto">
                                                <span class="avatar avatar-lg" style="background: #11998e;">
                                                    ${not empty currentUser.name ? currentUser.name.charAt(0) : currentUser.username.charAt(0)}
                                                </span>
                                            </div>
                                            <div class="col">
                                                <h4 class="mb-1">${currentUser.name}</h4>
                                                <div class="text-muted">
                                                    ${currentUser.role == 'MEMBER' ? '成员' : '管理员'}
                                                </div>
                                            </div>
                                            <!-- 学习状态 -->
                                            <div class="col-auto text-end">
                                                <c:choose>
                                                    <c:when test="${not empty activeSession}">
                                                        <div class="badge bg-green">学习中</div>
                                                        <div class="text-muted mt-1">
                                                            <i class="bi bi-clock me-1"></i>
                                                            <fmt:formatDate value="${activeSession.checkInTime}" pattern="HH:mm:ss"/> 开始
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="badge bg-secondary">空闲中</div>
                                                        <div class="text-muted mt-1">准备开始学习</div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>

                                        <!-- 当前学习时长 -->
                                        <c:if test="${not empty activeSession}">
                                            <div class="progress mt-3" style="height: 8px;">
                                                <div class="progress-bar bg-green" style="width: 100%"></div>
                                            </div>
                                            <div class="mt-2 text-center">
                                                <span class="h3 text-green" id="currentDuration">计算中...</span>
                                            </div>
                                        </c:if>

                                        <!-- 操作按钮 -->
                                        <div class="mt-4">
                                            <c:choose>
                                                <c:when test="${not empty activeSession}">
                                                    <button class="btn btn-danger w-100" id="btnEndStudy" onclick="endStudy()">
                                                        <i class="bi bi-stop-fill me-2"></i>结束学习
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button class="btn btn-green w-100" id="btnStartStudy" onclick="startStudy()">
                                                        <i class="bi bi-play-fill me-2"></i>开始学习
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>

                                <!-- 学习统计 -->
                                <div class="row mt-3">
                                    <div class="col-sm-6 col-lg-3">
                                        <div class="card">
                                            <div class="card-body text-center">
                                                <div class="h1 text-blue mb-1">${todayDuration}</div>
                                                <div class="text-muted">今日学习(分钟)</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-6 col-lg-3">
                                        <div class="card">
                                            <div class="card-body text-center">
                                                <div class="h1 text-green mb-1">${completedToday}</div>
                                                <div class="text-muted">今日次数</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-6 col-lg-3">
                                        <div class="card">
                                            <div class="card-body text-center">
                                                <div class="h1 text-purple mb-1">${weekDuration}</div>
                                                <div class="text-muted">本周学习(分钟)</div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-sm-6 col-lg-3">
                                        <div class="card">
                                            <div class="card-body text-center">
                                                <div class="h1 text-orange mb-1">${consecutiveDays}</div>
                                                <div class="text-muted">连续学习(天)</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 侧边栏 -->
                            <div class="col-lg-4">
                                <!-- 学习规则 -->
                                <div class="card">
                                    <div class="card-header">
                                        <h3 class="card-title">
                                            <i class="bi bi-info-circle me-1"></i>学习规则
                                        </h3>
                                    </div>
                                    <div class="card-body">
                                        <h5 class="mb-2">签到规则</h5>
                                        <ul class="list-unstyled text-muted mb-3">
                                            <li class="mb-1"><i class="bi bi-check2 text-green me-2"></i>6:00-22:00 可随时开始学习</li>
                                            <li class="mb-1"><i class="bi bi-check2 text-green me-2"></i>18:00 强制签退</li>
                                            <li class="mb-1"><i class="bi bi-check2 text-green me-2"></i>6:00-18:00 = 早到</li>
                                            <li class="mb-1"><i class="bi bi-check2 text-green me-2"></i>18:00-19:00 = 正常</li>
                                            <li class="mb-1"><i class="bi bi-check2 text-orange me-2"></i>19:00后 = 迟到</li>
                                        </ul>
                                        <h5 class="mb-2">签退规则</h5>
                                        <ul class="list-unstyled text-muted">
                                            <li class="mb-1"><i class="bi bi-check2 text-green me-2"></i>21:30前签退 = 早退</li>
                                            <li class="mb-1"><i class="bi bi-check2 text-green me-2"></i>21:30后签退 = 正常</li>
                                            <li class="mb-1"><i class="bi bi-x-circle text-red me-2"></i>22:00 系统自动结束</li>
                                        </ul>
                                    </div>
                                </div>

                                <!-- 快捷链接 -->
                                <div class="card mt-3">
                                    <div class="card-body">
                                        <a href="${ctx}/study/list" class="btn btn-outline-primary w-100 mb-2">
                                            <i class="bi bi-list-ul me-2"></i>学习记录
                                        </a>
                                        <button type="button" class="btn btn-outline-secondary w-100" onclick="showRules()">
                                            <i class="bi bi-question-circle me-2"></i>详细规则
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 学习规则弹窗 -->
                        <div id="rulesModal" class="modal modal-blur fade" style="display: none;" tabindex="-1">
                            <div class="modal-dialog modal-sm modal-dialog-centered" role="document">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">学习规则</h5>
                                        <button type="button" class="btn-close" onclick="closeRules()"></button>
                                    </div>
                                    <div class="modal-body">
                                        <h6>签到规则：</h6>
                                        <ul class="mb-3">
                                            <li>6:00 - 22:00 可随时开始学习</li>
                                            <li>18:00 强制签退（如在学习中）</li>
                                            <li>6:00 - 18:00 签到 = 早到</li>
                                            <li>18:00 - 19:00 签到 = 正常</li>
                                            <li>19:00 后签到 = 迟到</li>
                                        </ul>
                                        <h6>签退规则：</h6>
                                        <ul class="mb-0">
                                            <li>21:30 前签退 = 早退</li>
                                            <li>21:30 后签退 = 正常</li>
                                            <li>22:00 系统自动结束学习</li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <script>
                            function showRules() {
                                var modal = new bootstrap.Modal(document.getElementById('rulesModal'));
                                modal.show();
                            }
                            function closeRules() {
                                var modal = bootstrap.Modal.getInstance(document.getElementById('rulesModal'));
                                if (modal) modal.hide();
                            }
                        </script>
                    </div>
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
                    btn.innerHTML = '<i class="bi bi-play-fill me-2"></i>开始学习';
                }
            })
            .catch(function(error) {
                console.error('错误:', error);
                alert('网络异常，请重试');
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-play-fill me-2"></i>开始学习';
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
                    btn.innerHTML = '<i class="bi bi-stop-fill me-2"></i>结束学习';
                }
            })
            .catch(function(error) {
                console.error('错误:', error);
                alert('网络异常，请重试');
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-stop-fill me-2"></i>结束学习';
            });
        }
    </script>

<jsp:include page="/jsp/common/layout_bottom.jsp"/>
