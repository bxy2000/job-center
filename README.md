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
unzip job-center-dist.zip
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
./job-executor.sh start
停止
./job-executor.sh stop
```
## 5 配置文件（关键配置）
### 1 管理平台（job-admin/application.properties)
#### （1） 服务器端口
如果和已有端口冲突可以修改，默认8080端口
```
server.port=8080
```
#### （2） 服务上下文
一般无需修改，调度中心可以访问http://www.xxx.com:8080/job-admin
```
server.context-path=/job-admin
```
#### （3） 数据库连接
根据实际情况修改
数据库名：job_center
用户名：root
密码：root_888
```
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/job_center?Unicode=true&characterEncoding=UTF-8&useSSL=false
spring.datasource.username=root
spring.datasource.password=root_888
```
### 2 执行器配置（job-executor/application.properties）
#### （1） 执行器web端口
```
server.port=8081
```
#### （2） 管理平台地址
如果修改了管理平台相关配置，这里需要相应修改
```
job.admin.addresses=http://127.0.0.1:8080/job-admin
```
#### （3） 执行器地址
一般无需修改
如果管理平台和执行器部署在不同的机器上，这里需要提供ip地址，否则为空
```
job.executor.ip=
job.executor.port=9999
```
## 6 工作原理
#### 1. 执行器运行起来，会将自己注册到管理平台
#### 2. 管理平台制作调度任务，会根据一定的调度策略指定执行器执行任务
## 7 示例一（备份oracle数据库，linux平台）
```
1. 访问http://localhost:8080/job-admin
2. 用户名/密码  admin/123456
3. 调度任务
4. 新增调度任务
5. 运行模式
   windows下选择powershell
   linux下选择shell
6. cron编辑任务定时策略
7. 其他按要求填写
8. 保存任务
9. 点击操作下拉框，选择GLUE IDE
10. 编写以下脚本并保存
    #!/bin/bash
    sh /opt/oracle/backup.sh
    echo "Good bye!"
    exit 0
11. 运行调度任务
```
backup.sh
restore.sh 在doc目录下

## 7 示例二（执行kettle转换,windows平台）
```
1. 访问http://localhost:8080/job-admin
2. 用户名/密码  admin/123456
3. 调度任务
4. 新增调度任务
5. 运行模式
   windows下选择powershell
   linux下选择shell
6. cron编辑任务定时策略
7. 其他按要求填写
8. 保存任务
9. 点击操作下拉框，选择GLUE IDE
10. 编写以下脚本并保存
    e:
    cd /
    cd data-integration
    ./Pan.bat /file F:\kettle\bxy\aaa.ktr /level Detailed /logfile D:\aaa\aaa-bbb-1.log
    exit 0
11. 运行调度任务
```

