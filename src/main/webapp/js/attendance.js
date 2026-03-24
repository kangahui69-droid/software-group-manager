// 签到签退交互脚本
(function() {
    'use strict';

    // 签到
    var btnCheckIn = document.getElementById('btnCheckIn');
    if (btnCheckIn) {
        btnCheckIn.addEventListener('click', function() {
            if (this.disabled) return;

            if (!confirm('确定现在签到吗？')) return;

            this.disabled = true;
            this.querySelector('.btn-text').textContent = '签到中...';

            fetch(ctx + '/attendance', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=checkin'
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    alert(data.message);
                    location.reload();
                } else {
                    alert(data.message || '签到失败');
                    btnCheckIn.disabled = false;
                    btnCheckIn.querySelector('.btn-text').textContent = '签到';
                }
            })
            .catch(function(error) {
                console.error('签到错误:', error);
                alert('网络异常，签到失败，请重试');
                btnCheckIn.disabled = false;
                btnCheckIn.querySelector('.btn-text').textContent = '签到';
            });
        });
    }

    // 签退
    var btnCheckOut = document.getElementById('btnCheckOut');
    if (btnCheckOut) {
        btnCheckOut.addEventListener('click', function() {
            if (this.disabled) return;

            if (!confirm('确定现在签退吗？')) return;

            this.disabled = true;
            this.querySelector('.btn-text').textContent = '签退中...';

            fetch(ctx + '/attendance', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'action=checkout'
            })
            .then(function(response) { return response.json(); })
            .then(function(data) {
                if (data.success) {
                    var msg = data.message;
                    if (data.workDuration) {
                        msg += '\n今日工作时长：' + data.workDuration + '分钟';
                    }
                    alert(msg);
                    location.reload();
                } else {
                    alert(data.message || '签退失败');
                    btnCheckOut.disabled = false;
                    btnCheckOut.querySelector('.btn-text').textContent = '签退';
                }
            })
            .catch(function(error) {
                console.error('签退错误:', error);
                alert('网络异常，签退失败，请重试');
                btnCheckOut.disabled = false;
                btnCheckOut.querySelector('.btn-text').textContent = '签退';
            });
        });
    }

    // 首页快捷签到（如果在首页有快捷按钮）
    var quickCheckInBtn = document.getElementById('quickCheckInBtn');
    if (quickCheckInBtn) {
        quickCheckInBtn.addEventListener('click', function() {
            if (this.disabled) return;
            if (!confirm('确定签到吗？')) return;

            this.disabled = true;

            fetch(ctx + '/attendance', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=checkin'
            })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                alert(data.message);
                if (data.success) location.reload();
                else quickCheckInBtn.disabled = false;
            });
        });
    }

    var quickCheckOutBtn = document.getElementById('quickCheckOutBtn');
    if (quickCheckOutBtn) {
        quickCheckOutBtn.addEventListener('click', function() {
            if (this.disabled) return;
            if (!confirm('确定签退吗？')) return;

            this.disabled = true;

            fetch(ctx + '/attendance', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=checkout'
            })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                alert(data.message);
                if (data.success) location.reload();
                else quickCheckOutBtn.disabled = false;
            });
        });
    }

})();
