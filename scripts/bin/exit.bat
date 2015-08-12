rem kill app on windows
@echo off
set FILE="bin\twatcher.pid"
if exist %FILE% (
  setlocal EnableDelayedExpansion
  set /P PID=<%FILE%
  taskkill /F /PID !PID!
  del %FILE%
  endlocal
)
