#代码部署
1.打包压缩当前目录下代码
2.将代码包上传并解压到服务器的/usr/local/cloudnetlot/www/service_Subscriber目录下
3.拷贝代码包中java_server文件到/etc/init.d目录下，并修改文件权限为777
cp /usr/local/cloudnetlot/www/service_Subscriber/java_server /etc/init.d/
chmod 777 /etc/init.d/java_server
#服务启动与停止
1.启动
/etc/init.d/java_server start
2.停止
/etc/init.d/java_server stop