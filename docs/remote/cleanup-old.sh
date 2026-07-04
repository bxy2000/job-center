#!/bin/bash
# 清理远程服务器超过 30 天的备份

backup_path="/opt/backup/hive"
retention_days=30

echo "Cleaning up backup files older than $retention_days days in $backup_path..."
find $backup_path -mtime +$retention_days -type f -name '*.tar.gz' -exec rm -rf {} \;
echo "Cleanup completed"