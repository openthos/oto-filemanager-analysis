#1 程序组成部分
FileManager应用可以分为3个部分：界面布局、本地文件管理的实现、云服务的实现，具体请参考代码结构及功能说明：<br>
主要布局文件位于FileManagerOpenOS\app\src\main\res中，内文件目录示意：<br>
```
drawable  drawable-hdpi  drawable-ldpi  drawable-mdpi  drawable-xhdpi  layout  mipmap-hdpi  mipmap-mdpi  mipmap-xhdpi  mipmap-xxhdpi  mipmap-xxxhdpi  values  values-w820dp  
```
主要代码文件位于FileManagerOpenOS\app\src\main\java\openos\filemanageropenos中，内文件目录示意：<br>
```
Dialog  FileCtrol  cloudservice  tools  usb  MainActivity.java
```
##1.1 添加自定义权限
####FileManager应用需要对硬盘及USB设备进行读写，但在Android系统中默认第三方应用没有对USB设备写的权限，需要添加自定义权限或者将FileManager加入系统应用中。<br>
在AndroidMinifest.xml中添加对USB的读写权限：
``` xml 
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
在AndroidMinifest.xml中添加允许挂载和反挂载可移动存储文件系统的权限：
``` xml
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
```
##1.2 程序
###FileManager应用提供 Openthos 系统用户空间的本地文件管理以及Seafile云存储支持。
使用Android studio开发的程序代码位于https://github.com/openthos/oto-filemanager.git 仓库的FileManagerOpenOS文件夹中，这是Android studio的工程文件，直接导入就可以运行。
可直接运行的程序及源代码压缩包的详细地址位于https://github.com/openthos/oto-filemanager/tree/master/app
#2 开发
##2.1 构建Android Studio2.0开发环境
参考Android Studio构建教程：http://www.open-open.com/lib/view/open1433387390635.html
##2.2 Android Studio导入该项目
程序位于 https://github.com/openthos/oto-filemanager.git 的app分支，需要使用的代码是其中的FileManagerOpenOS，这是一个压缩包。 程序详细地址： https://github.com/openthos/oto-filemanager/tree/master/app；解压后可直接导入Android Studio。
#3 运行
1、直接安装运行https://github.com/openthos/oto-filemanager/tree/master/app 下面的OpenThos_FileManager.apk文件<br>
2、也可下载https://github.com/openthos/oto-filemanager 下面的FileManagerOpenOS,然后导入Android Studio后运行
