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