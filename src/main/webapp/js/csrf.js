(function () {
    'use strict';

    var meta = document.querySelector('meta[name="csrf-token"]');
    if (!meta || !meta.content) return;
    var token = meta.content;

    function isLocal(url) {
        try {
            return new URL(url, window.location.href).origin === window.location.origin;
        } catch (_) {
            return false;
        }
    }

    function isUnsafe(method) {
        return /^(POST|PUT|PATCH|DELETE)$/i.test(method || '');
    }

    function protectForm(form) {
        if (!isUnsafe(form.method) || !isLocal(form.action)) return;
        var field = form.querySelector('input[name="_csrf"]');
        if (!field) {
            field = document.createElement('input');
            field.type = 'hidden';
            field.name = '_csrf';
            form.appendChild(field);
        }
        field.value = token;
    }

    document.addEventListener('submit', function (event) {
        protectForm(event.target);
    }, true);
    document.addEventListener('formdata', function (event) {
        if (isUnsafe(event.target.method) && isLocal(event.target.action)) {
            event.formData.set('_csrf', token);
        }
    });
    var nativeSubmit = HTMLFormElement.prototype.submit;
    HTMLFormElement.prototype.submit = function () {
        protectForm(this);
        return nativeSubmit.call(this);
    };
    function protectExistingForms() {
        document.querySelectorAll('form').forEach(protectForm);
    }

    function postLink(url) {
        if (!isLocal(url)) return;
        var form = document.createElement('form');
        form.method = 'POST';
        form.action = url;
        form.style.display = 'none';
        document.body.appendChild(form);
        protectForm(form);
        nativeSubmit.call(form);
    }
    window.csrfPost = postLink;
    var postActions = new Set([
        'delete', 'deleteGroup', 'deletePlan', 'deleteFile', 'deleteRegistration',
        'deleteMessage', 'approve', 'reject', 'approveActivity', 'rejectActivity',
        'joinAsAdmin', 'setDefault', 'restore', 'hardDelete', 'enable', 'disable'
    ]);
    document.addEventListener('click', function (event) {
        if (event.defaultPrevented || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey) return;
        var link = event.target.closest('a[href]');
        if (!link || !isLocal(link.href)) return;
        var url = new URL(link.href);
        if (url.pathname.endsWith('/logout') || postActions.has(url.searchParams.get('action'))) {
            event.preventDefault();
            postLink(url.href);
        }
    });
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', protectExistingForms);
    } else {
        protectExistingForms();
    }

    var nativeFetch = window.fetch;
    if (nativeFetch) {
        window.fetch = function (input, init) {
            var url = input instanceof Request ? input.url : input;
            var method = (init && init.method) || (input instanceof Request ? input.method : 'GET');
            if (isUnsafe(method) && isLocal(url)) {
                var options = Object.assign({}, init);
                var headers = new Headers(options.headers || (input instanceof Request ? input.headers : undefined));
                headers.set('X-CSRF-TOKEN', token);
                options.headers = headers;
                return nativeFetch.call(this, input, options);
            }
            return nativeFetch.call(this, input, init);
        };
    }

    var nativeOpen = XMLHttpRequest.prototype.open;
    var nativeSend = XMLHttpRequest.prototype.send;
    XMLHttpRequest.prototype.open = function (method, url) {
        this.__csrfProtect = isUnsafe(method) && isLocal(url);
        return nativeOpen.apply(this, arguments);
    };
    XMLHttpRequest.prototype.send = function () {
        if (this.__csrfProtect) this.setRequestHeader('X-CSRF-TOKEN', token);
        return nativeSend.apply(this, arguments);
    };
})();
