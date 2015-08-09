rem kill app on windows
@echo off
if exist RUNNING_PID (
  setlocal EnableDelayedExpansion
  set /P PID=<RUNNING_PID
  taskkill /F /PID !PID!
  del RUNNING_PID
  endlocal
)
