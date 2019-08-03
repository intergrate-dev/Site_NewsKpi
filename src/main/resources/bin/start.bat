@echo off

title  大屏数据服务
java -Xms256m -Xmx512m -DAPP_HOME=./ -jar ./Site_NewsKpi-0.0.1-SNAPSHOT-exec.jar
pause
