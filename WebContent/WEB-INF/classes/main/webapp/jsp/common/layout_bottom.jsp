<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <footer class="footer footer-transparent d-print-none">
        <div class="container-xl">
            <div class="row text-center align-items-center flex-row-reverse">
                <div class="col-lg-auto ms-lg-auto">
                    <ul class="list-inline list-inline-dots mb-0">
                        <li class="list-inline-item"><a href="#" class="link-secondary">文档</a></li>
                        <li class="list-inline-item"><a href="#" class="link-secondary">帮助</a></li>
                    </ul>
                </div>
                <div class="col-12 col-lg-auto mt-3 mt-lg-0">
                    <ul class="list-inline list-inline-dots mb-0">
                        <li class="list-inline-item">
                            Copyright &copy; 2026
                            <a href="." class="link-secondary">信息工程学院软件兴趣小组</a>.
                            All rights reserved.
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </footer>
    </div>
    </div>
    <!-- Tabler Core JS -->
    <script src="https://cdn.jsdelivr.net/npm/@tabler/core@1.4.0/dist/js/tabler.min.js"></script>

    <!-- 全局 Loading 控制脚本 -->
    <script>
        // 全局 Loading 控制对象
        window.GlobalLoading = {
            element: document.getElementById('global-loading'),
            
            // 显示 Loading
            show: function(text) {
                if (this.element) {
                    const textElement = this.element.querySelector('.loading-text');
                    if (textElement && text) {
                        textElement.textContent = text;
                    }
                    this.element.classList.add('active');
                }
            },
            
            // 隐藏 Loading
            hide: function() {
                if (this.element) {
                    this.element.classList.remove('active');
                    // 重置文本为默认值
                    const textElement = this.element.querySelector('.loading-text');
                    if (textElement) {
                        textElement.textContent = '加载中...';
                    }
                }
            }
        };

        // 页面加载完成后自动隐藏 Loading（防止页面刷新时显示）
        document.addEventListener('DOMContentLoaded', function() {
            GlobalLoading.hide();
        });

        // 为所有表单添加自动 Loading 功能
        document.addEventListener('DOMContentLoaded', function() {
            // 为所有 form 元素添加 submit 事件监听
            document.querySelectorAll('form').forEach(function(form) {
                form.addEventListener('submit', function(e) {
                    // 检查是否有 data-no-loading 属性，有则不显示 loading
                    if (!form.hasAttribute('data-no-loading')) {
                        GlobalLoading.show('提交中，请稍候...');
                    }
                });
            });

            // 为所有带有 data-ajax 属性的链接添加 loading
            document.querySelectorAll('a[data-ajax="true"]').forEach(function(link) {
                link.addEventListener('click', function() {
                    GlobalLoading.show('加载中...');
                });
            });
        });
    </script>
    </body>

    </html>