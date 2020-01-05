# job-center
任务调度中心
## 1 编译
```
mvn clean package
```
## 2 部署
```
安装 jdk1.8.*
安装 mysql 5.7.*
运行建库脚本
doc/db/create-job-center.sql
unzip assembly-2.2.0-SNAPSHOT-dist.zip
```
## 3 WINDOWS下运行
### 1 管理平台
```
cd bin/job-admin
启动
job-admin.bat start
停止
job-admin.bat stop
```
### 2 执行器
```
cd bin/job-executor
启动
job-executor start
停止
job-executor stop
```
## 4 LINUX下运行
### 1 管理平台
```
cd bin/job-admin
启动
job-admin.bat start
停止
job-admin.bat stop
```
### 2 执行器
```
cd bin/job-executor
启动
job-executor start
停止
job-executor stop
```
