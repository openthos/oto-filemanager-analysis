#经过调研seafile客户端，并于王琪工程师沟通，我们确定了以下seafile功能和命令
##1、预计实现的功能：<br>
对已同步的资料库的右键菜单：1、打开文件夹；2、解除同步；3、查看详情<br>
对未同步的资料库的右键菜单：1、下载（自动绑定本地文件夹）；2、到网上去查看（在浏览器打开网址）；3、查看详情<br>
加号：1、添加同步文件夹（在服务器端创建一个新的资料库，并绑定本地文件夹）<br>
##2、预计需要的命令
Seafile命令格式：[参考Seafile官网](https://seacloud.cc/group/3/wiki/seafile-cli-manual)<br>
```
list:       list local libraries
seaf-cli list
```
```
status:     show syncing status
seaf-cli status
```
```
download:   download a library from seafile server
seaf-cli download -l <library-id> -s <seahub-server-url> -d <parent-directory> -u <username> [-p <password>]
```
```
sync:       synchronize an existing folder with a library in seafile server
seaf-cli sync -l <library-id> -s <seahub-server-url> -d <existing-folder> -u <username> [-p <password>]
```
```
desync:     desynchronize a library with seafile server
seaf-cli desync -d <existing-folder>
```
```
Create      create a new library
seaf-cli create -s <seahub-server-url> -n <library-name> -u <username> -p <password> -t <description> [-e <library-password>]
```
##3、Seafile命令行里面缺少的命令
1、获取已存在的资料库信息列表<br>
