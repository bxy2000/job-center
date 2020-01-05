@echo off

if "%1" == "start" (
    echo start job executor
    start "job executor" java -jar job-executor.jar --spring.config.local=application.yml
) else if "%1" == "stop" (
    echo stop job executor
    taskkill /fi "WINDOWTITLE eq job executor"
) else (
    echo please use "job-executor.bat start" or "job-executor.bat stop"
)

pause
