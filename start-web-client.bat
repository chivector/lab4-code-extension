@echo off
cd /d "%~dp0"
echo Starting CNCD Web Chat at http://127.0.0.1:8088/
start "CNCD Web Chat" java -cp . com.cncd.ch04.web.WebChatLauncher 8088
