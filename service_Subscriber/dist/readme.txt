运行
java  -Xms256m -Xmx8192m  -Dfile.encoding=UTF-8 -Duser.timezone=GMT+08 -Dlog4j.configuration=file:E:/vagrant/php5/cloudnetlotclient/service_Subscriber/dist/log4j.properties -jar cloudnetlotclient-2.1.jar

-Xms256m 初始大小

-Xmx8192m  最终大小
注意：使用过程中结合自己电脑实际来设置Xms，Xmx大小


打包记录：
cloudnetlotclient-2.0.1.jar
	初始测试包
cloudnetlotclient-2.0.2.jar
	排查mqtt重连出现内存溢出问题时，去掉mqtt发布消息代码。怀疑mqtt本身重连与发布代码可能会导致客户端id冲突，测试下来同样存在问题。
cloudnetlotclient-2.1jar
	第二阶段测试包
