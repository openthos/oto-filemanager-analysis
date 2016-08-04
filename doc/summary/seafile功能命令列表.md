#经过调研seafile客户端，并于王琪工程师沟通，我们确定了以下seafile功能和命令
##1、预计实现的功能：<br>

```
(1)、对已同步的资料库的右键菜单：
1、打开文件夹；
2、解除同步；
3、查看详情
(2)、对未同步的资料库的右键菜单：
1、下载（自动绑定本地文件夹）；
2、到网上去查看（在浏览器打开网址）；
3、查看详情
(3)、加号：添加同步文件夹（在服务器端创建一个新的资料库，并绑定本地文件夹）
(4)、网络云盘信息：获取网络云盘总容量和已使用空间。
```
##2、预计需要的命令
###Seafile命令(均已测试)：（网址：https://seacloud.cc/group/3/wiki/seafile-cli-manual）

```
参数：-s 网址  -n 资料库名称  -u 用户名  -p 密码   -l 资料库id   -d 本地文件夹

list-remote获取远程服务器的资料库列表，命令例子：
seaf-cli list-remote -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716

create 创建资料库，命令例子：
seaf-cli create -n zhangsan -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716

list 获取本地已同步的文件夹，命令例子：
seaf-cli list

download下载远程资料库，并且会默认的同步,命令例子：
seaf-cli download -l f3c0b8e3-3644-43af-afa7-4b7e63869e87 -s  https://dev.openthos.org/ -d /home/zhu/  -u 1799858922@qq.com -p 279716

sync把本地文件夹和远程资料库绑定同步,命令例子：
seaf-cli sync -l 7fd8f246-2fa5-4868-b565-45098e7f52b4 -s  https://dev.openthos.org/ -d /home/zhu/wang/  -u 1799858922@qq.com -p 279716

desync 解除同步，命令例子：
seaf-cli desync -d /home/zhu/wang/
```
###Seafile命令行里面缺少的命令
1、获取账号云盘总容量和已使用容量<br>
2、获取远程没有同步资料库的信息，如大小等等。
###Seafile命令行的所有命令

```
{init,start,stop,list,list-remote,status,download,download-by-name,sync,desync,create,config}
    init                Initialize config directory
    start               Start ccnet and seafile daemon
    stop                Stop ccnet and seafile daemon
    list                List local libraries
    list-remote         List remote libraries
    status              Show syncing status
    download            Download a library from seafile server
    download-by-name    Download a library defined by name from seafile server
    sync                Sync a library with an existing foler
    desync              Desync a library with seafile server
    create              Create a library
    config              Configure seafile client

```
