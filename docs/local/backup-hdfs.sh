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