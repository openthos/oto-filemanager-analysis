#1、java文件结构及说明
![image](https://github.com/openthos/oto-filemanager/blob/master/doc/summary/pictures/java.png)
#2、布局文件结构
![image](https://github.com/openthos/oto-filemanager/blob/master/doc/summary/pictures/layout.png)
#3、云服务的接口：
###对应网络文件的三个状态，分别是已同步，没有同步，和加号
     int STATUS_SYNCHRONIZED = 1;
     int STATUS_UNSYNCHRONIZED = 2;
     int STATUS_ADD = -1;

云盘获取网络文件列表显示：
initCloudFile()函数里面获取并且显示文件，调用工程师的list命令，获取服务器的文件列表，每一个文件夹信息赋值给Seafile对象，每一个seafile对象分为已同步和未同步，然后加入到mCloudFiles列表中。然后让mCloudFileAdapter更新。
初始或者网络文件列表更新的时候就调用initCloudFile()。

    添加同步文件夹：选择要同步的文件夹，对应的是DialogPathSelector类，onCreate函数里面调用同步命令

    对已同步的文件解除同步：MenuDialog3类，onClick函数里面case R.id.desync处调用解除同步命令

    对未同步的文件夹下载并同步：MenuDialog4 类，onClick函数里面case R.id.download处调用命令下载并同步
