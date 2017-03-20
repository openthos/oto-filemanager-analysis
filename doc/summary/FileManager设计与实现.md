## 主界面（U盘）
## 左侧导航条（点击逻辑）
## 地址栏
```
    //根据当前路径展示地址栏内容
    public void setNavigationPath(String displayPath) {
        mEt_nivagation.setText(displayPath);
        mPath = null;
        mPathList.clear();
        if (displayPath == null || displayPath.equals("")) {
            mAddressListView.setVisibility(View.GONE);
            mEt_nivagation.setVisibility(View.VISIBLE);
        } else {
            updateAddressButton(displayPath);
            mAddressListView.setVisibility(View.VISIBLE);
            mEt_nivagation.setVisibility(View.GONE);
        }
        mPathAdapter.notifyDataSetChanged();
    }
    
    //更新地址栏按钮内容
    private void updateAddressButton(String displayPath) {
        if (displayPath.equals(Constants.SD_PATH)) {
            mPath = new String[]{Constants.SD_PATH};
            mPathList.add(Constants.SD_PATH);
        } else {
            mPath = displayPath.split(Constants.SD_PATH);
            for (String s : mPath) {
            mPathList.add(s);
            }
        }
        if (!mPathList.get(0).equals(getString(R.string.path_sd_eng))) {
            mPath[0] = Constants.SD_PATH;
            mPathList.set(0, Constants.SD_PATH);
        }
    }
    
    //地址栏按钮点击事件处理,跳转到对应目录
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            if (getVisibleFragment() != null
                && getVisibleFragment() instanceof SystemSpaceFragment
                && view.getTag() instanceof PathAdapter.ViewHolder) {
                int pos = (int) ((PathAdapter.ViewHolder) view.getTag()).path.getTag();
                if (pos == mPath.length - 1) {
                    ((IFileInteractionListener) getVisibleFragment()).
                        onRefreshFileList(mCurPath, getFileSortHelper()); 
                } else {
                    mClickPath = "";
                    for (int j = 0; j <= pos; j++) {
                        if ((j == 0 && mPath[0].equals(Constants.SD_PATH)) || j == pos) {
                            mClickPath += mPath[j];
                        } else {
                            mClickPath += mPath[j] + Constants.SD_PATH;
                        }
                    }
                    mClickPath = mClickPath.replaceAll(
                    getResources().getString(R.string.path_sd_eng), Util.getSdDirectory());
                    ((SystemSpaceFragment) getVisibleFragment()).
                        mFileViewInteractionHub.openSelectFolder(mClickPath);
                }
            } else {
                mAddressListView.setVisibility(View.GONE);
                mEt_nivagation.setVisibility(View.VISIBLE);
                mEt_nivagation.requestFocus();
                mEt_nivagation.setSelection(mEt_nivagation.getText().length());
            }
        }
        return true;
    }    
```

## 搜索栏
```
    // 拿到搜索栏输入框的内容，然后开启子线程进行搜索
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
    
    // 拿到搜索完成后的文件集合，回到主线程来更新界面
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

    // 递归遍历当前路径下所有的文件和文件夹，将匹配到的文件或文件夹添加到文件集合中
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
