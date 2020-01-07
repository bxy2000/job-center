#!/bin/sh
export ORACLE_BASE=/home/app/oracle
export ORACLE_HOME=$ORACLE_BASE/product/11.2.0/dbhome_1
export ORACLE_SID=orcl
export ORACLE_TERM=xterm  
export PATH=$ORACLE_HOME/bin:$HOME/bin:$PATH
export LD_LIBRARY_PATH=$ORACLE_HOME/lib:/lib:/usr/lib
export LANG=en_US
orowner=ZJK
days=15
bakdata=$orowner"_"$(date +%Y%m%d).dmp
baklog=$orowner"_"$(date +%Y%m%d).log
ordatabak=$orowner"_"$(date +%Y%m%d).zip
bakdir=/opt/oracle
remotePath=/home/DataBak 
cd $bakdir
exp usr_zjk/kfdx#2018@orcl grants=y file=$bakdir/$bakdata log=$bakdir/$baklog
zip  -r  $ordatabak  $bakdata  
#find $bakdir  -type f -name "*.log" -exec rm {} \;
find $bakdir  -type f -name "*.dmp" -mtime +3  -exec rm {} \;
find $bakdir  -type f -name "*.zip" -mtime +$days -exec rm -rf {} \;
#scp  -P 22 $bakdir/$ordatabak root@211.69.6.12:$remotePath 
