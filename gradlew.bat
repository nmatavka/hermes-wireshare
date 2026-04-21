@echo off
setlocal
set ROOT_DIR=%~dp0
call "%ROOT_DIR%jlibtorrent\gradlew.bat" -p "%ROOT_DIR%" %*
