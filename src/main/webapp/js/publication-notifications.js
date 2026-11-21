(function () {
    'use strict';

    var bell = document.getElementById('publicationNotificationBell');
    if (!bell) return;
    var context = bell.getAttribute('data-context');
    var badge = document.getElementById('publicationNotificationCount');
    var previous = null;

    function showNewNotificationHint() {
        var hint = document.createElement('div');
        hint.setAttribute('role', 'status');
        hint.className = 'alert alert-info shadow position-fixed end-0 bottom-0 m-3';
        hint.style.zIndex = '1100';
        hint.textContent = '有新的新闻或活动通知，点击铃铛查看。';
        document.body.appendChild(hint);
        window.setTimeout(function () { hint.remove(); }, 5000);
    }

    function refreshCount() {
        fetch(context + '/notifications/count', {credentials: 'same-origin', cache: 'no-store'})
            .then(function (response) {
                if (!response.ok) throw new Error('notification count request failed');
                return response.json();
            })
            .then(function (data) {
                var count = Number(data.unread) || 0;
                badge.textContent = count > 99 ? '99+' : String(count);
                badge.classList.toggle('d-none', count === 0);
                bell.setAttribute('aria-label', '我的通知，' + count + '条未读');
                if (previous !== null && count > previous) showNewNotificationHint();
                previous = count;
            })
            .catch(function () { /* The page remains usable if notifications are unavailable. */ });
    }

    window.markPublicationRead = function (type, id) {
        var token = document.querySelector('meta[name="csrf-token"]');
        if (!token || !id || (type !== 'NEWS' && type !== 'ACTIVITY')) return;
        fetch(context + '/notifications/read-source', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                'X-CSRF-TOKEN': token.content
            },
            body: new URLSearchParams({type: type, id: String(id)}).toString()
        }).then(function (response) {
            if (response.ok) refreshCount();
        }).catch(function () { /* A later visit can retry the read receipt. */ });
    };

    refreshCount();
    window.setInterval(function () { if (!document.hidden) refreshCount(); }, 30000);
    document.addEventListener('visibilitychange', function () {
        if (!document.hidden) refreshCount();
    });
})();
