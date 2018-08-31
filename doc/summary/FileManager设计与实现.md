## 文件管理器功能模块
- 浏览文件
- 设备操作
  - USB设备的卸载，格式化
  - Auto-mount 挂在本地其他硬盘分区
- 快捷访问（收藏）
- 获取文件信息
- 文件操作
  - 打开
  - 复制
  - 剪贴
  - 粘贴
  - 删除
  - 重命名
  - 压缩
  - 解压缩
  - 发送
- 网络邻居
  - 共享本地目录
  - 远程访问其他用户共享目录
- 云服务
  - 备份本地电脑配置
  - 恢复云端存储的配置到本地
  - 访问一个seafile服务器

## 浏览文件
浏览本地真实存在的文件或目录，目前是以user的uid去访问文件或目录。会根据文件的真实权限来进行展示，并且显示不同的右键菜单
- 获取目录文件 File.listFiles();
- 判断文件权限 File.canWrite();
- 相关代码
  - src/org/openthos/filemanager/fragment/SystemSpaceFragment.java
  - src/org/openthos/filemanager/system/FileViewInteractionHub.java
  - src/org/openthos/filemanager/component/MenuDialog.java

## 设备操作
### USB
文件管理器可以接收到USB设备信息，目前主要显示的是USB存储设备。显示在主页中和左侧快捷条目中

通过StatFs类来获取磁盘信息，
````
    String path = "/storage/usb0/";
    StatFs stat = new StatFs(path);
    long blockSize = stat.getBlockSize();
    long availableBlocks = stat.getAvailableBlocks();
    long totalBlocks = stat.getBlockCount();
````

USB设备卸载与格式化目前上是参考Settings存储页面做的，思路为启动一个service，传递一个bundle对象，里面
放置了StorageVolume对象，MountService会对StorageVolume来进行相应的操作，或是格式化，或是卸载
- 相关代码
  - src/org/openthos/filemanager/fragment/SdStorageFragment.java
  - src/org/openthos/filemanager/MainActivity.java
  - platform-22/src/org/openthos/filemanager/UsbUtils.java

### Auto-Mount
文件管理器可以挂载本地磁盘的其他分区，目前支持的磁盘为标准磁盘和nvme SSD磁盘，如同USB设备，显示在主页和左侧快捷条目中

扫描发现磁盘是通过系统启动完成后，接收系统广播，然后开始扫描所有磁盘，用到的命令有ls, mount, blkid, fdisk,
然后根据一定的规则来过滤到不应该显示的分区，如swap分区，EFI分区等

挂载是通过命令行来挂载，对应的挂在的不同的挂载点上。
- ext2: mount -t ext2 -o rw [other disk dev path] [mount point path] 
- ext3: mount -t ext3 -o rw [other disk dev path] [mount point path] 
- ext4: mount -t ext4 -o rw [other disk dev path] [mount point path] 
- vfat: mount -t vfat -o rw [other disk dev path] [mount point path]
- ntfs: ntfs-3g -o rw [other disk dev path] [mount point path] 

- 相关代码
  - src/org/openthos/filemanager/MainActivity.java
  - src/org/openthos/filemanager/fragment/SdStorageFragment.java
  - src/org/openthos/filemanager/system/BootCompleteReceiver.java

### 光盘
暂不支持
### MTP手机
暂不支持

## 快速访问
快速访问存在于首页和左侧条目，首页中表现形式为个人空间，左侧表现形式为收藏

快速访问主要是为了让用户更方便的访问一些常用文件夹，比如说系统的一些常用目录，或者是一些常用软件的下载目录，图片目录，文件目录等。

设计理念：预设目录存储在一个xml里，该xml后期可以增加更多的预设目录，在文件管理器启动的时候，将会加载该文件，然后检查目录的存在与否，并显示在相应的界面上。

- 相关代码
  - res/xml/personal_folders.xml
  - src/org/openthos/filemanager/MainActivity.java
  - src/org/openthos/filemanager/fragment/PersonalSpaceFragment.java
  
## 获取文件信息
该功能是获取文件信息，或者是修改文件权限，用到的命令为du, stat和chmod，这块功能还不是很完善，后面应当显示用户组等信息

- 相关代码
  - src/org/openthos/filemanager/component/PropertyDialog.java

## 文件操作
覆盖了常见的文件基本操作，主要有复制，剪贴，粘贴，删除，永久删除，压缩，解压缩，重命名，打开

涉及到的命令为cp, mv, rm, 7z

打开用的系统api new Intent(Intent.ACTION_VIEW);这样会动态的调用系统的界面，用户可以自主的选择什么应用来打开应用。

- 相关代码
  - src/org/openthos/filemanager/fragment/SystemSpaceFragment.java
  - src/org/openthos/filemanager/system/FileOperationHelper.java
  - src/org/openthos/filemanager/system/FileViewInteractionHub.java

# Samba Server
用于将本机设备共享出去，让局域网内其他设备来访问本机

## 实现流程
###开启/关闭Samba Server进程

发起调用：OtoFileManager-->PopOnClickLintener.java的onClick()，由pop_share_toggle开关控制

实际调用：OtoFileManager-->SambaUtils.java中startLocalNetworkShare()来开启，restartLocalNetworkShare()来重启，stopLocalNetworkShare()来关闭

ubuntu下操作指令

    service smbd/nmbd start-------------------------------开启主进程/守护进程
    service smbd/nmbd stop--------------------------------关闭主进程/守护进程
    service smbd/nmbd restart-----------------------------重启主进程/守护进程

openthos封装后的脚本调用

    /data/data/samba/samba.sh start------------------------开启
    /data/data/samba/samba.sh stop-------------------------关闭
    /data/data/samba/samba.sh restart----------------------重启
    
### 共享文件目录

发起调用：OtoFileManager-->MenuDialog.java的onItemClick()

最终调用：OtoFileManager-->ShareMenuDialog.java来写入配置文件，然后重启进程

配置文件实际位置：/data/data/samba/etc/smb.conf

共享方式分为匿名访问与帐户密码访问

配置参数说明:

```
匿名访问
[global]
workgroup  = WORKGROUP    //如果需要跟windows交互，工作组建议设置为WORKGROUP
public = yes
security = user    //定义安全级别为user
map to guest = bad user    //如使用不存在的用户登录,映射为guest用户;如使用正确的用户而密码错误,则禁止连接
server string = Samba Server    //对Samba Server的描述
server role = standalone server    //独立服务器
[share]    //自定义共享名
comment = share    //描述
path = /storage/emulated/legacy/Pictures/wallpaper    //共享目录路径
public = yes    //是否公开，也就是通过网上邻居客户端能看到该共享
writable = yes    //是否可写
browseable = yes    //是否可以浏览
guest ok = yes    //是否允许来宾用户访问
```
   
```
用户密码访问
[global]
workgroup  = WORKGROUP
public = yes
server string = Samba Server
server role = standalone server
[share]
comment = share
path = /storage/emulated/legacy/Pictures/wallpaper
public = yes
writable = yes
browseable = yes
```

### 用户管理

包含用户列表、添加用户、删除用户、修改密码

#### 用户列表

发起调用：UserManagementDialog.java中

实际调用：SambaUtils.java的getAllUsers()

Ubuntu下操作指令

    pdbedit -L

openthos封装后的脚本调用

    /data/data/samba/pdbedit.sh -L



#### 添加用户

发起调用：在AddUsersDialog.java中

实际调用：SambaUtils.java的addUserAndPasswd()

ubuntu下操作指令

    pdbedit -a 用户名

openthos封装后的脚本调用

    /data/data/samba/smbpasswd.sh" + " " + 用户名 + " " + 密码
    
#### 删除用户

发起调用：在UserManagementDialog.java中

实际调用：SambaUtils.java的removeUser()

ubuntu下操作指令

    pdbedit -X 用户名
    
openthos封装后的脚本调用

    data/data/samba/pdbedit.sh" + " -x " +  用户名
    
#### 修改密码

发起调用：在UserManagementDialog.java中

实际调用：SambaUtils.java的modifyPasswd()

ubuntu下操作指令

    smbpasswd  用户名
    
openthos封装后的脚本调用

    /data/data/samba/smbpasswd.sh" + " " + 用户名 + " " + 密码
    
# Seafile other library 
需求原因：原先OtoFileManager(文件管理器)上已经集成了OtoCloudService(云服务)的部分功能，以命令行的形式实现了本地与服务器DATA资料库的同步，为了改善用户体验，满足多样化需求，需要增加其他资源库的支持。

## 整体思路
使用纯java代码的形式实现其他资料库的支持，方便二次开发与后期维护。另外，因android依赖库的jar包在跨版本(如5.1与8.1)使用时，兼容性难以满足，故而采用放置依赖库源码的方式。

## 代码位置
在packages/apps/OtoCloudService/src/org/openthos/seafile/seaapp下

## 主要的类
SeafileActivity.java------------------------------------------界面处理，消息处理，数据加载与展示

GenericListener.java----------------------------------------单击、双击、右键等各类点击事件的处理

SeafRepo.java----------------------------------------------资料库的bean类

SeafDirent.java---------------------------------------------资料库里边文件或目录的bean类

SeafLink.java-----------------------------------------------分享链接的bean类

Account.java-----------------------------------------------云服务账户的bean类 

DeleteFileDialog.java---------------------------------------删除文件(夹)对话框

DeleteRepoDialog.java-------------------------------------删除资料库对话框

DownloadFileDialog---------------------------------------下载文件对话框

GetShareLinkDialog.java-----------------------------------获取分享链接对话框

NewDirDialog.java-----------------------------------------创建文件夹对话框

NewFileDialog.java----------------------------------------创建文件对话框

NewRepoDialog.java--------------------------------------创建资料库对话框

RenameFileDialog.java------------------------------------重命名文件(夹)对话框

RenameRepoDialog.java----------------------------------重命名资料库对话框

SeafConnection.java--------------------------------------网络连接处理

UploadFileDialog.java------------------------------------上传文件(夹)对话框

MeunDialog.java-----------------------------------------右键菜单

NavContext.java-------------------------------------------网络路径信息的bean类

## 主要的内部类及方法
SeafileActivity-->getAccountAndLogin()-------------------------------获取帐户相关信息并加载其他资料库进行展示的方法

    public void getAccountAndLogin() {
        mAccount = new Account(mServerURL, mUserId, null, false, null);
        mDataManager = new DataManager(mAccount, this);
        ConcurrentAsyncTask.execute(new LoginTask(mAccount, mPassword, null,false));
        if (!Utils.isNetworkOn(this)) {
            ToastUtil.showSingletonToast(this, getString(R.string.network_down));
            showRepoError();
            } else {
                switchView(TAG_LIST);
                mLoadingDialog = new LoadingDialog(this);
                mLoadingDialog.show();
                ConcurrentAsyncTask.execute(new LoginTask(mAccount, mPassword, null, false));
  
SeafileActivity-->onBackPressed()-------------------------------------处理界面回退的方法

    @Override
    public void onBackPressed() {
        if (mStoredViews.size() > 1) {
            if (mNavContext.inRepo()) {
                if (mNavContext.isRepoRoot()) {
                    mNavContext.setRepoID(null);
                } else {
                    String parentPath = Utils.getParentPath(mNavContext.getDirPath());
                    mNavContext.setDir(parentPath, null);
                }
            }
            mStoredViews.remove(mStoredViews.size() - 1);
            Object o = mStoredViews.get(mStoredViews.size() - 1);
            mAdapter.setItemsAndRefresh((List) o);
        }
        if (mStoredViews.size() <= 1) {
            removeBackTag();
        }
    }

SeafileActivity-->LoadTask---------------------------------------------异步任务类，用于加载首页其他资料库数据

SeafileActivity-->LoadDirTask------------------------------------------异步任务类，用于加载其他资料库内文件目录的数据

GenericListener-->openLibrary()----------------------------------------打开资料库的方法

    private void openLibrary(SeafRepo seafRepo) {
        mActivity.mLoadingDialog = new LoadingDialog(mActivity);
        mActivity.mLoadingDialog.show();
        mCurParent = seafRepo;
        NavContext navContext = mActivity.getNavContext();
        navContext.setDirPermission(seafRepo.permission);
        navContext.setRepoID(seafRepo.id);
        navContext.setRepoName(seafRepo.getName());
        navContext.setDir("/", seafRepo.root);
        loadDir();
    }

GenericListener-->openFile()-------------------------------------------打开资料库内的文件或文件夹的方法

    private void openFile(SeafDirent seafDirent) {
        NavContext navContext = mActivity.getNavContext();
        DataManager dataManager = mActivity.getDataManager();
        if (seafDirent.isDir()) {
            mActivity.mLoadingDialog = new LoadingDialog(mActivity);
            mActivity.mLoadingDialog.show();
            mCurParent = seafDirent;
            String currentPath = navContext.getDirPath();
            String newPath = currentPath.endsWith("/") ?
            currentPath + seafDirent.name : currentPath + "/" + seafDirent.name;
            navContext.setDir(newPath, seafDirent.id);
            navContext.setDirPermission(seafDirent.permission);
            loadDir();
        } else {
            String fileName= seafDirent.name;
            navContext.fileName = fileName;
            long fileSize = seafDirent.size;
            String repoName = navContext.getRepoName();
            String repoID = navContext.getRepoID();
            String dirPath = navContext.getDirPath();
            String filePath = Utils.pathJoin(navContext.getDirPath(), fileName);
            String localPath = Utils.pathJoin(dataManager.getRepoDir(repoName, repoID), filePath);
            File localFile = new File(localPath);
            if (localFile.exists() && (localFile.length() == fileSize)) {
                IntentBuilder.viewFile(mActivity, localPath);
            } else {
                if (localFile.exists()) {
                    localFile.delete();
                }
                mActivity.showDownloadFileDialog(filePath);
                
# seafile功能实现流程

#### 参数说明
```
            -s 服务器地址  
            -n 资料库名称  
            -u 用户名  
            -p 密码   
            -l 资料库id   
            -d 本地文件夹
```
#### 实现流程
1、初始化seafilie环境<br>
```
seaf-cli init -d /home/zhu/test   初始化seafile配置文件夹，/home/zhu/test是本地的一个文件夹，用来存储配置文件
```
2、启动seafile服务<br>
```
seaf-cli start    启动seafile服务
```
3、显示服务器端资料库<br>
```
seaf-cli list-remote -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716    显示服务器端的所有资料库
```
4、显示本地已同步的资料库境<br>
```
seaf-cli list   显示本地已经同步的资料库
```
5、从服务器下载资料库境<br>
```
seaf-cli download -l f3c0b8e3-3644-43af-afa7-4b7e63869e87 -s  https://dev.openthos.org/ -d /home/zhu/  -u 1799858922@qq.com -p 279716   从服务器下载制定的资料库
```
6、同步本地文件夹境<br>
```
上传本地的文件夹需要经过两步：在服务器端创建对应的资料库、建立相应的同步关系并同步到服务器<br>
seaf-cli create -n zhangsan -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716   在服务器端创建对应的资料库<br>
seaf-cli sync -l 7fd8f246-2fa5-4868-b565-45098e7f52b4 -s  https://dev.openthos.org/ -d /home/zhu/wang/  -u 1799858922@qq.com -p 279716    建立对应的同步关系并同步到服务器
```
7、解除同步境<br>
```
seaf-cli desync -d /home/zhu/wang/    解除同步关系
```
