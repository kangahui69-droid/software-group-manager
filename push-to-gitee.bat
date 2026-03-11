@echo off
chcp 65001
echo ========================================
echo 推送到Gitee - 交互式推送
echo ========================================
echo.
echo 即将执行: git push -u origin master:develop
echo.
echo 提示：推送过程中会要求输入：
echo   - Username: 你的Gitee用户名
echo   - Password: 你的Gitee密码或个人访问令牌
echo.
echo 按任意键开始推送...
pause > nul

cd /d "E:\java\Software_group"
echo.
echo ========================================
echo 开始推送...
echo ========================================
echo.

git push -u origin master:develop

echo.
echo ========================================
if %errorlevel% == 0 (
    echo 推送成功！
    echo 访问地址：https://gitee.com/hsuniversity/softwaregroup/tree/develop/
) else (
    echo 推送失败，请检查错误信息
    echo 常见原因：
    echo   1. 用户名或密码错误
    echo   2. 网络连接问题
    echo   3. 远程仓库不存在或无权限
    echo.
    echo 查看详细帮助：打开 push-gitee.txt
)
echo ========================================
echo.
pause
