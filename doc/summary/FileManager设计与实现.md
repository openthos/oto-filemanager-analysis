## 主界面（U盘）
## 左侧导航条（点击逻辑）
## 地址栏
## 搜索栏
```
private void excuSearch(TextView input) {
        progressDialog.show();
        mInputText = input.getText().toString().trim();
        if (mFileList != null && mFileList.size() > 0) {
            mFileList.clear();
        }
        new Thread() {
            @Override
            public void run() {
                startSearch(mInputText);
            }
        }.start();
    }
    
        public void startSearch(String text_search) {
        File curFile = new File(mCurPath);
        if (curFile.exists() && curFile.isDirectory()) {
            final File[] currentFiles = curFile.listFiles();
            if (currentFiles != null && currentFiles.length != 0) {
                getFiles(text_search, currentFiles);
            }
            mMainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mFileList != null && mFileList.size() > 0) {
                        startSearchFragment();
                    } else {
                        showEmptyView();
                    }
                }
            });
        }
    }

    private void getFiles(String text_search, File[] files) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String fileName = file.getName();
            if (fileName.contains(text_search)) {
                SearchInfo searchInfo = new SearchInfo();
                searchInfo.setFileName(fileName);
                searchInfo.setFilePath(file.getPath());
                searchInfo.fileAbsolutePath = file.getAbsolutePath();
                searchInfo.IsDir = file.isDirectory();
                if (mFileList != null && mFileList.contains(fileName)
                                      && mFileList.contains(file.getPath())) {
                    continue;
                } else {
                    mFileList.add(searchInfo);
                }
            }
            if (file.isDirectory()) {
                if (file.listFiles() != null) {
                    getFiles(text_search, file.listFiles());
                }
            }
        }
    }
    
```
## 云服务
### 需要的命令: seaf-cli
### 参数说明<br>
```
            -s 服务器地址  
            -n 资料库名称  
            -u 用户名  
            -p 密码   
            -l 资料库id   
            -d 本地文件夹
```
### 命令格式及说明<br>
```
init   初始化seafile配置文件夹
seaf-cli init -d /home/zhu/test

start  启动seafile服务
seaf-cli start

list-remote获取远程服务器的资料库列表，命令例子：
seaf-cli list-remote -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716

list 获取本地已同步的文件夹，命令例子：
seaf-cli list

download下载远程资料库，并且会默认的同步,命令例子：
seaf-cli download -l f3c0b8e3-3644-43af-afa7-4b7e63869e87 -s  https://dev.openthos.org/ -d /home/zhu/  -u 1799858922@qq.com -p 279716

create 创建资料库，命令例子：
seaf-cli create -n zhangsan -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716

sync把本地文件夹和远程资料库绑定同步,命令例子：
seaf-cli sync -l 7fd8f246-2fa5-4868-b565-45098e7f52b4 -s  https://dev.openthos.org/ -d /home/zhu/wang/  -u 1799858922@qq.com -p 279716

desync 解除同步，命令例子：
seaf-cli desync -d /home/zhu/wang/
```
### 真正的命令：
#### /data/sea/proot.sh -b /data/seafile-config/:/data/seafile-config/ seaf-cli init -d /home/zhu/test<br />
需要在每个seaf-cli前面加     */data/sea/proot.sh -b /data/seafile-config/:/data/seafile-config/*

## 回收站
主要用的的android的四大组件之一COntentPrivoder
```
创建表
create table recycle(id integer primary key autoincrement, source text not null, filename text not null);

设定Uri
private static final int CONTACT = 1;
private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
static {
    uriMatcher.addURI("com.openthos.filemanager", "recycle", CONTACT);
}

通过uri访问:
Uri mUri = Uri.parse("content://com.openthos.filemanager/recycle");
```

## 文件监听
## 文件预览
## 快捷键
