#!/bin/bash

APP_NAME=job-admin.jar

usage() {
  echo "Usage: sh job-admin.sh [start|stop|restart|status]"
}

exists() {
  pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}'`

  if [ -z "${pid}" ]; then
    return 1
  else
    return 0
  fi
}

start() {
  exists
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} is already running. pid=${pid} ."
  else
    nohup java -jar $APP_NAME > ${APP_NAME}.log 2>&1 &
    echo "${APP_NAME} start success"
  fi
}

stop() {
  exists
  if [ $? -eq "0" ]; then
    kill -9 $pid
  else
    echo "${APP_NAME} is not running"
  fi
}

status() {
  exists
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} is running. pid is ${pid}"
  else
    echo "${APP_NAME} is not running."
  fi
}

restart() {
  stop
  start
}

case "$1" in
  "start")
    start
    ;;
  "stop")
    stop
    ;;
  "status")
    status
    ;;
  "restart")
    restart
    ;;
  *)
    usage
    ;;
esac
