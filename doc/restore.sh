#!/bin/sh
export ORACLE_BASE=/home/app/oracle
export ORACLE_HOME=$ORACLE_BASE/product/11.2.0/dbhome_1
export ORACLE_SID=orcl  
export PATH=$ORACLE_HOME/bin:$HOME/bin:$PATH
export LD_LIBRARY_PATH=$ORACLE_HOME/lib:/lib:/usr/lib
export LANG=en_US
orowner=ZJK
recovery=recovery
days=15
bakdata=$orowner"_"$(date +%Y%m%d).dmp
recoverylog=$recovery"_"$(date +%Y%m%d).log
bakdir=/opt/oracle
cd $bakdir
imp  usr_zjk/kfdx#2018@orcl  file=$bakdir/$bakdata full=y log=$bakdir/$baklog

