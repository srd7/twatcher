cd bin
start twatcher.bat -Dtwatcher.mode="setting"

rem Wait for 5 sec so that server can boot.
timeout /T 5

start http://localhost:9000
