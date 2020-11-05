@echo off

if "%1" == "start" (
    echo start job executor of kettle
    start "job executor kettle" java -Djava.ext.dirs=".;%JAVA_HOME%\jre\lib\ext;..\..\lib" -jar job-executor-kettle.jar --spring.config.local=application.yml
) else if "%1" == "stop" (
    echo stop job executor of kettle
    taskkill /fi "WINDOWTITLE eq job executor kettle"
) else (
    echo please use "job-executor.bat start" or "job-executor.bat stop"
)

