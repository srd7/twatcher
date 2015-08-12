rem start bin\twatcher.bat -Dtwatcher.mode=setting -Dpidfile.path=bin/twatcher.pid
rem Play 2.3 prod app needs "."
start bin\twatcher.bat . -Dtwatcher.mode=setting -Dpidfile.path=bin/twatcher.pid

rem Wait for 5 sec so that server can boot.
timeout /T 5

start http://localhost:9000
