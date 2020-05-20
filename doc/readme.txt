一、修改jar包中的文本文件
    1、创建一个临时文件夹/abc
    2、mv job-admin.jar /abc
    3、进入到/abc, 解压缩job-admin.jar, 命令如下
        unzip job-admin.jar
    4、删除job-admin.jar
    5、修改想要修改的文件
    6、重新打jar包
        jar -cfM0 job-admin.jar *
    7、结束
二、job-center标题文件在：BOOT-INF/classes/i18n/message.properties
三、网站图标文件：BOOT-INF/classes/static/favicon.ico

