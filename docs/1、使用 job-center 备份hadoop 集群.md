1、集群配置
```shell
192.168.1.105 ddp201
192.168.1.106 ddp202
192.168.1.104 ddp203
```
ddp203 为管理机，部署 datasophon 服务，并运行 job-center 执行器。

## 2、每小时定时备份 hive 元数据 MySQL

### 2.1 脚本位置
在 ddp203 管理机上创建 `/opt/backup/hive/backup-mysql.sh` 脚本：

```bash
#!/bin/bash
# hive 元数据 MySQL 备份脚本
# 每小时执行一次，保留最近 15 天的备份

mysql_host="localhost"
mysql_port="3306"
mysql_user="hive"
mysql_pass="hive_password"
database_name="hive_metadata"
backup_path="/opt/backup/hive/mysql"
backup_dump="/usr/bin/mysqldump"
# 保留天数
retention_days=15

mkdir -p $backup_path
cd $backup_path

dt=`date +%Y%m%d_%H%M`
echo "Backup Begin Date:" $(date +"%Y-%m-%d %H:%M:%S")

# mysqldump 备份
$backup_dump -h$mysql_host -P$mysql_port -u$mysql_user -p$mysql_pass $database_name \
--default-character-set=utf8 --opt -Q -R --skip-lock-tables > ${database_name}_${dt}.sql

# 压缩
tar -czf ${database_name}_${dt}.tar.gz ${database_name}_${dt}.sql
rm -rf ${database_name}_${dt}.sql

# 清理过期备份
find $backup_path -mtime +$retention_days -type f -name '*.gz' -exec rm -rf {} \;

echo "Backup Succeed Date:" $(date +"%Y-%m-%d %H:%M:%S")
echo "Backup file: ${backup_path}/${database_name}_${dt}.tar.gz"
```

### 2.2 添加执行权限
```bash
chmod +x /opt/backup/hive/backup-mysql.sh
```

### 2.3 在 job-center 中配置任务
1. 访问 job-center 管理界面
2. 用户名/密码：admin/123456
3. 调度任务 → 新增调度任务
4. **运行模式**：选择 SHELL
5. **Cron 表达式**：`0 0 * * * ? *` (每小时整点执行)
6. GLUE IDE 中编写：
```bash
#!/bin/bash
sh /opt/backup/hive/backup-mysql.sh
echo "Hive MySQL metadata backup completed"
exit 0
```

## 3、HDFS 数据备份（每天执行）

### 3.1 脚本位置
在 ddp203 管理机上创建 `/opt/backup/hive/backup-hdfs.sh` 脚本：

```bash
#!/bin/bash
# HDFS 数据备份脚本
# 备份 Hive 数据到本地压缩，然后同步到异地备份服务器
# 每天凌晨 1 点执行

# 配置
hive_data_path="/user/hive/warehouse"
local_backup_path="/opt/backup/hive/hdfs"
remote_server="root@192.168.1.38"
remote_backup_path="/opt/backup/hive"
retention_days=30
hdfs_cmd="/usr/bin/hdfs"
tar_cmd="/usr/bin/tar"

mkdir -p $local_backup_path
cd $local_backup_path

dt=`date +%Y%m%d`
backup_file="hive_data_${dt}.tar.gz"

echo "HDFS Backup Begin Date:" $(date +"%Y-%m-%d %H:%M:%S")
echo "Source: hdfs://${hive_data_path}"
echo "Local target: ${local_backup_path}/${backup_file}"

# 从 HDFS 拷贝到本地并压缩
echo "Copying data from HDFS to local..."
$hdfs_cmd dfs -get $hive_data_path ./hive_data_${dt}

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to get data from HDFS"
    exit 1
fi

# 压缩
echo "Compressing data..."
$tar_cmd -czf $backup_file ./hive_data_${dt}

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to compress data"
    rm -rf ./hive_data_${dt}
    exit 1
fi

# 清理临时文件
rm -rf ./hive_data_${dt}

# 同步到远程备份服务器
echo "Syncing to remote backup server ${remote_server}..."
scp $backup_file ${remote_server}:${remote_backup_path}/

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to sync to remote server"
    exit 1
fi

# 清理本地过期备份
find $local_backup_path -mtime +7 -type f -name '*.tar.gz' -exec rm -rf {} \;

echo "HDFS Backup Succeed Date:" $(date +"%Y-%m-%d %H:%M:%S")
echo "Backup file: ${local_backup_path}/${backup_file}"
echo "Remote: ${remote_server}:${remote_backup_path}/${backup_file}"
exit 0
```

### 3.2 添加执行权限
```bash
chmod +x /opt/backup/hive/backup-hdfs.sh
```

### 3.3 在 job-center 中配置任务
1. 调度任务 → 新增调度任务
2. **运行模式**：选择 SHELL
3. **Cron 表达式**：`0 0 1 * * ? *` (每天凌晨 1 点执行)
4. GLUE IDE 中编写：
```bash
#!/bin/bash
sh /opt/backup/hive/backup-hdfs.sh
echo "HDFS data backup completed"
exit 0
```

**注意**：需要配置 ddp203 到 192.168.1.38 的 ssh 免密登录：
```bash
# 在 ddp203 上执行
ssh-keygen -t rsa
ssh-copy-id root@192.168.1.38
```

## 4、数据恢复脚本

### 4.1 恢复 MySQL 元数据
创建 `/opt/backup/hive/restore-mysql.sh`：

```bash
#!/bin/bash
# 恢复 Hive MySQL 元数据
# 使用方法: ./restore-mysql.sh backup_file.tar.gz

if [ $# -ne 1 ]; then
    echo "Usage: $0 <backup_file.tar.gz>"
    exit 1
fi

backup_file=$1
mysql_host="localhost"
mysql_port="3306"
mysql_user="hive"
mysql_pass="hive_password"
database_name="hive_metadata"
mysql_cmd="/usr/bin/mysql"

if [ ! -f $backup_file ]; then
    echo "ERROR: Backup file $backup_file not found"
    exit 1
fi

echo "Starting restore MySQL from $backup_file..."
echo "WARNING: This will overwrite existing database $database_name"
echo "Press Ctrl+C to abort..."
sleep 5

# 解压
temp_dir=$(mktemp -d)
tar -xzf $backup_file -C $temp_dir

sql_file=$(find $temp_dir -name "*.sql" | head -1)

if [ -z "$sql_file" ]; then
    echo "ERROR: No SQL file found in archive"
    rm -rf $temp_dir
    exit 1
fi

# 恢复
$mysql_cmd -h$mysql_host -P$mysql_port -u$mysql_user -p$mysql_pass $database_name < $sql_file

if [ $? -eq 0 ]; then
    echo "Restore completed successfully"
else
    echo "Restore failed"
fi

# 清理
rm -rf $temp_dir
exit $?
```

### 4.2 恢复 HDFS 数据
创建 `/opt/backup/hive/restore-hdfs.sh`：

```bash
#!/bin/bash
# 恢复 HDFS Hive 数据
# 使用方法: ./restore-hdfs.sh backup_file.tar.gz

if [ $# -ne 1 ]; then
    echo "Usage: $0 <backup_file.tar.gz>"
    exit 1
fi

backup_file=$1
hive_data_path="/user/hive/warehouse"
hdfs_cmd="/usr/bin/hdfs"

if [ ! -f $backup_file ]; then
    echo "ERROR: Backup file $backup_file not found"
    exit 1
fi

echo "Starting restore HDFS from $backup_file..."
echo "WARNING: This will overwrite existing data in $hive_data_path"
echo "Press Ctrl+C to abort..."
sleep 5

# 解压到临时目录
temp_dir=$(mktemp -d)
tar -xzf $backup_file -C $temp_dir

# 查找解压后的目录
data_dir=$(find $temp_dir -type d | grep -E "hive_data_[0-9]+$" | head -1)

if [ -z "$data_dir" ]; then
    echo "ERROR: No data directory found in archive"
    rm -rf $temp_dir
    exit 1
fi

# 删除原有数据
echo "Clearing existing HDFS directory..."
$hdfs_cmd dfs -rm -r -f $hive_data_path

# 上传数据
echo "Uploading data to HDFS..."
$hdfs_cmd dfs -put $data_dir $hive_data_path

if [ $? -eq 0 ]; then
    echo "Restore completed successfully"
else
    echo "Restore failed"
fi

# 清理
rm -rf $temp_dir
exit $?
```

### 4.3 远程服务器备份清理
在远程备份服务器 192.168.1.38 `/opt/backup/hive/cleanup-old.sh`：

```bash
#!/bin/bash
# 清理远程服务器超过 30 天的备份

backup_path="/opt/backup/hive"
retention_days=30

echo "Cleaning up backup files older than $retention_days days in $backup_path..."
find $backup_path -mtime +$retention_days -type f -name '*.tar.gz' -exec rm -rf {} \;
echo "Cleanup completed"
```

### 4.4 添加执行权限
```bash
chmod +x /opt/backup/hive/restore-mysql.sh
chmod +x /opt/backup/hive/restore-hdfs.sh
```

## 5、使用说明

### 恢复 MySQL 元数据
```bash
# 查看可用备份
ls -lh /opt/backup/hive/mysql/

# 恢复指定备份
/opt/backup/hive/restore-mysql.sh /opt/backup/hive/mysql/hive_metadata_20240101_0100.tar.gz
```

### 恢复 HDFS 数据
```bash
# 从远程服务器获取备份
scp root@192.168.1.38:/opt/backup/hive/hive_data_20240101.tar.gz /tmp/

# 恢复
/opt/backup/hive/restore-hdfs.sh /tmp/hive_data_20240101.tar.gz
```

## 6、备份目录结构
```
/opt/backup/hive/
├── backup-mysql.sh          # MySQL 备份脚本（每小时）
├── backup-hdfs.sh           # HDFS 备份脚本（每天）
├── restore-mysql.sh         # MySQL 恢复脚本
├── restore-hdfs.sh          # HDFS 恢复脚本
├── mysql/                   # MySQL 备份目录（保留15天）
│   └── hive_metadata_*.tar.gz
└── hdfs/                    # HDFS 本地备份目录（保留7天）
    └── hive_data_*.tar.gz
```

远程服务器 `192.168.1.38:/opt/backup/hive/`:
```
/opt/backup/hive/
├── hive_data_*.tar.gz       # 完整 HDFS 备份（保留30天）
└── cleanup-old.sh           # 清理脚本
```

## 7、job-center 开机自启（systemd）

将 job-center 部署到 /opt/job-center 目录后，使用 systemd 实现开机自启。

### 7.1 service 文件位置

项目中已提供 service 模板文件：
- [job-admin.service](file:///Users/bxy/IdeaProjects/production/job-center/bin/job-admin/job-admin.service)
- [job-executor.service](file:///Users/bxy/IdeaProjects/production/job-center/bin/job-executor/job-executor.service)

### 7.2 部署步骤

```bash
# 1. 将 service 文件复制到系统目录
cp /opt/job-center/bin/job-admin/job-admin.service /etc/systemd/system/
cp /opt/job-center/bin/job-executor/job-executor.service /etc/systemd/system/

# 2. 重新加载 systemd 配置
systemctl daemon-reload

# 3. 启用开机自启
systemctl enable job-admin
systemctl enable job-executor

# 4. 启动服务
systemctl start job-admin
systemctl start job-executor

# 5. 查看状态
systemctl status job-admin
systemctl status job-executor
```

### 7.3 常用管理命令

```bash
# 查看状态
systemctl status job-admin
systemctl status job-executor

# 停止服务
systemctl stop job-admin
systemctl stop job-executor

# 重启服务
systemctl restart job-admin
systemctl restart job-executor

# 查看日志
journalctl -u job-admin -f
journalctl -u job-executor -f
```

### 7.4 service 文件说明

两个 service 文件内容如下：

**job-admin.service**
```ini
[Unit]
Description=Job Center Admin
After=network.target syslog.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/job-center/bin/job-admin
ExecStart=/usr/bin/java -jar job-admin.jar --spring.config.local=application.yml
ExecStop=/bin/kill -s TERM $MAINPID
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**job-executor.service**
```ini
[Unit]
Description=Job Center Executor
After=network.target syslog.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/job-center/bin/job-executor
ExecStart=/usr/bin/java -jar job-executor.jar --spring.config.local=application.yml
ExecStop=/bin/kill -s TERM $MAINPID
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

> **说明**：`Restart=always` 表示服务意外退出后会自动重启，`RestartSec=10` 表示重启前等待 10 秒。如果 Java 路径不是 `/usr/bin/java`，请根据实际环境修改 `ExecStart` 中的路径。