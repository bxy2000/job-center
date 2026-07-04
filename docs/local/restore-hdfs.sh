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