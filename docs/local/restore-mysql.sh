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