@echo off

if "%1" == "start" (
    echo start job admin
    start "job admin" java -jar job-admin.jar --spring.config.local=application.yml
) else if "%1" == "stop" (
    echo stop job admin
    taskkill /fi "WINDOWTITLE eq job admin"
) else (
    echo please use "job-admin.bat start" or "job-admin.bat stop"
)

pause
