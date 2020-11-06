mysql_host="localhost"
mysql_port="3306"
mysql_user="root"
mysql_pass="root_888"
backup_path="/opt/mysqlbackup"
backup_dump="/usr/local/mysql/bin/mysqldump"
database_name="mysql"

cd $backup_path

dt=`date +%Y%m%d_%H%M`
echo "Backup Begin Date:" $(date +"%Y-%m-%d %H:%M:%s")

$backup_dump -h$mysql_host -P$mysql_port -u$mysql_user -p$mysql_pass $database_name \
--default-character-set=utf8 --opt -Q -R --skip-lock-tables > $database_name$dt.sql
tar -czf $database_name$dt.tar.gz $database_name$dt.sql
rm -rf $database_name$dt.sql
find $backup_path -mtime +15 -type f -name '*.gz' -exec rm -rf {} \;
echo "Backup Succeed Date:" $(date +"%Y-%m-%d %H:%M:%s")

