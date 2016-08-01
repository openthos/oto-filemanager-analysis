#FileManager设计与实现

##1、导航栏部分
###1.1、前进，后退
  实现是利用两个栈mbackwardfiles，mforwardfiles来存储打开的文件路径，点击前进后退按钮调用forward()， backward()函数来具体实现前进后退。

``` 
public void forward() {//前进一步
        Log.e("mforwardfiles0", "" + mforwardfiles.size());
        if (mforwardfiles.size() > 0) {
            File forwardpathFile = mforwardfiles.pop();
            Log.e("forwardpathFile", "" + forwardpathFile.getAbsolutePath());
            if (forwardpathFile.getAbsolutePath().equals("/")) {
                mbackwardfiles.add(mCurrentPathFile);
                showHomeView();
            } else {
                showGridOrView(madapter.getViewMode());
                open(forwardpathFile, true);
            }
        }
        Log.e("mforwardfiles1", "" + mforwardfiles.size());
    }

public void backward() {//后退一步
        if (mbackwardfiles.size() > 0) {
            File backpathFile = mbackwardfiles.pop();
            mforwardfiles.push(mCurrentPathFile);
            Log.e("backpathFile", backpathFile.getAbsolutePath());
            //如果是根目录，就显示主界面
            if (backpathFile.getAbsolutePath().equals("/")) {
                showHomeView();
            } else {
                showGridOrView(madapter.getViewMode());
                open(backpathFile, false);//后退的时候不加路径了,open函数里面已经加了一次
            }
        }
        Log.e("backword1", "" + mbackwardfiles.size());
    }
```
###1.2、显示当前路径
mCurrentPathEdit显示当前路径，mCurrentPathFile记录当前文件夹。定义变量homePath = "/sdcard";在显示当前路径的时候，没有显示真实的路径，把"/sdcard"替换为了“OpenthOS”。
###1.3、搜索
mSearchEditor是Editor控件来获取输入的关键字，然后对mSearchEditor进行监听，调用MyTool类的findFiles()函数来实现搜索。搜索当前文件夹下，包含关键字的文件及文件夹。

```
if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    mfiles.clear();
                    MyTool.findFiles(mCurrentPathFile.getAbsolutePath(), mSearchEditor.getText().toString().trim(), mfiles);
                    /*隐藏软键盘*/
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager.isActive()) {
                        inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                    if (mfiles.size() == 0) {
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("寻找的文件或文件夹不存在.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                        return true;
                    }
                    isCheckedMap.clear();
                    for (int i = 0; i < mfiles.size(); i++) {
                        isCheckedMap.put(i, false);
                    }
                    showGridOrView(madapter.getViewMode());
                    madapter.notifyDataSetChanged();
```
##2、左边菜单部分
界面对应的是left.xml文件，点击改变颜色是对应的控件获取焦点是时候，改变颜色。
##3、右边界面的切换
右边显示部分的界面主要分为四种：显示磁盘界面home_view.xml,显示文件列表界面是activity_main.xml里面的listview控件，已网格显示的是activity_main.xml里面的GridView控件gridview，显示云服务界面的是activity_main.xml里面的GridView控件cloud_file_grid。通过对每个部分的显示和隐藏来对界面进行变换。

```
    //显示主界面,即显示磁盘信息
    private void showHomeView() {
        mViewGridBtn.setEnabled(false);
        mViewListBtn.setEnabled(false);
        mCurrentPathEdit.setText("");
        mCurrentPathFile = new File("/");
        mOpenOsTextName.setText(mMyDiskName);
        //mOpenOsTextSize.setText(getRomSpace());
        show_ROM_storage();
        mHomeView.setVisibility(View.VISIBLE);
        mListViewLine.setVisibility(View.GONE);
        mGridView.setVisibility(View.GONE);
        mSearchEditor.setEnabled(false);
    }
	//浏览文件的界面，并判断是以列表显示还是网格形式显示。
    private void showGridOrView(int view_mode) {
        mSearchEditor.setEnabled(true);
        mViewGridBtn.setEnabled(true);
        mViewListBtn.setEnabled(true);
        if (view_mode == HDBaseAdapter.VIEWMODE_GRID) {
            mHomeView.setVisibility(View.GONE);
            mListViewLine.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            mEmptyView02.setVisibility(View.VISIBLE);
            mCloudGridView.setVisibility(View.GONE);
        } else {
            mHomeView.setVisibility(View.GONE);
            mListViewLine.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
        }
    }

    //显示云服务文件列表界面
    private void showCloudFileGridView() {
        mSearchEditor.setEnabled(false);
        mViewGridBtn.setEnabled(false);
        mViewListBtn.setEnabled(false);
        mHomeView.setVisibility(View.GONE);
        mListViewLine.setVisibility(View.GONE);
        mGridView.setVisibility(View.GONE);
        mEmptyView02.setVisibility(View.GONE);
        mCloudGridView.setVisibility(View.VISIBLE);
        mCurrentPathEdit.setText("云服务");
    }
```

##4、USB的动态识别
###4.1、USB显示
USB获取挂载的U盘，是调用MyTool类的 exec2（）函数运行“ df ”命令，获取当前的所有U盘的路径。USB的动态显示分为左边部分和右边部分。左边菜单部分，利用Listview显示U盘列表。右边部分利用gridview控件以硬盘形式显示USB信息。
###4.2、USB动态识别
MainActivity里面注册UsbStatesReceiver类，对USB的插入和拔出广播进行监听。当UsbStatesReceiver收到U盘插入和拔出的广播后，调用sendMSG发送信息给handler对象，handler收到信息后调用initUsb()函数重新读取系统挂载的U盘信息。
###4.3、USB的手动弹出
调用MyTool类的exec函数，运行remount命令解除对应的U盘的挂载。此过程需要root权限。

##5、右键菜单
右键菜单分为四个：对文件或文件夹的MenuDialog1，对空白部分的MenuDialog2，对云服务已同步文件夹的MenuDialog3，对云服务未同步文件夹的MenuDialog4。

```
//對文件或者文件夹的右鍵菜單，此菜单栏包括的功能：打开，打开方式，复制，剪切，重命名，删除，属性
public class MenuDialog1 extends Dialog implements View.OnClickListener;
//设置右键监听事件
			convertView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
				@Override
				public boolean onGenericMotion(View view, MotionEvent motionEvent) {
					if (motionEvent.getButtonState()==MotionEvent.BUTTON_SECONDARY){
						if (position<0||position>=mfiles.size())
							return false;
						showMenuDialog1(position,motionEvent);
						return true;
					}
					return false;
				}
			});
//對空白处的右鍵菜單，此菜单栏包括的功能：新建文件夹，新建文件，刷新，粘贴，全选
    public class MenuDialog2 extends Dialog implements View.OnClickListener；
//对GridView的空白地方进行监听，对listview部分也是这样，sharedPreferences是判断是否是对文件进行点击，如果不是对文件点击，就显示MenuDialog2 ，否则显示MenuDialog1
        mGridView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                    SharedPreferences sharedPreferences = getSharedPreferences("isfileordir", Context.MODE_PRIVATE);
                    boolean isclickfile = sharedPreferences.getBoolean("isclickfile", false);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isclickfile", false);
                    editor.commit();//提交修改
                    if (!isclickfile) {
                        int[] location = new int[2];
                        mBackBtn.getLocationOnScreen(location);
                        if (menuDialog1 != null)
                            menuDialog1.dismiss();
                        menuDialog2.showDialog((int) motionEvent.getRawX() - location[0], (int) motionEvent.getRawY() - location[1] + 57, 285, 200);
                        menuDialog2.setEnablePaste(canPaste);
                        return true;
                    }
                }
                return false;
            }
        });
//對已同步的文件夾右鍵菜單，具体功能：打开文件夹、解除同步、属性
    public class MenuDialog3 extends Dialog implements View.OnClickListener；
//對沒有同步的文件夾右鍵菜單，具体功能：下载并同步、去网上看看、属性
    public class MenuDialog4 extends Dialog implements View.OnClickListener ；
//对云文件进行的右键菜单，在CloudFileAdapter里面设置了监听事件，不同的文件设置不同的监听
if (f.status==STATUS_SYNCHRONIZED){
            mListHolder.icon.setImageResource(R.drawable.sync);
            convertView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
                @Override
                public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                    if (motionEvent.getButtonState()==MotionEvent.BUTTON_SECONDARY){
                        MainActivity.menuDialog3.setPosition(position);
                        MainActivity.menuDialog3.showDialog((int)motionEvent.getRawX(),(int)motionEvent.getRawY(),171,200);
                        return true;
                    }
                    return false;
                }
            });

        }else if (f.status==STATUS_UNSYNCHRONIZED){
            mListHolder.icon.setImageResource(R.drawable.unsync);
            convertView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
                @Override
                public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                    if (motionEvent.getButtonState()==MotionEvent.BUTTON_SECONDARY){
                        MainActivity.menuDialog4.setPosition(position);
                        MainActivity.menuDialog4.showDialog((int)motionEvent.getRawX(),(int)motionEvent.getRawY(),171,200);
                        return true;
                    }
                    return false;
                }
            });
        }

```

##6、文件复制进度对话框
文件复制进度对话框是CopyDialog类，动态显示文件复制或者剪切的百分比，对应的文件或文件夹，复制文件的总体大小。

```
 void copyFileOrFolders(List<File> files) {
        sendMsg(0);//发送开始复制信息
        for (File file : files) {
            //copyDialog.setPathdetail("把 " + file.getName() + " 复制到 " + mCurrentPathFile.getAbsolutePath().replace(homePath, mMyDiskName));
            sendMsg(4);//提示复制的是那个文件夹或者文件
            if (file.isDirectory()) {
                if (!copyFolder(file.getAbsolutePath(), mCurrentPathFile.getAbsolutePath() + "/" + file.getName()))
                    return;
            } else {
                if (!copyFile(file.getAbsolutePath(), mCurrentPathFile.getAbsolutePath() + "/" + file.getName()))
                    return;
            }
            //如果是剪切，就需要删除原文件
            if (mCopyOrCut.equals("cut")) {
                MyTool.deleteGeneralFile(file.getAbsolutePath());
                Log.e("if-is-cut", file.getAbsolutePath());
            }
        }
        sendMsg(2);//通知已经完成
    }
```
##7、系统双击
用Map isCheckedMap记录文件是否被选择，mLastClickId记录最后一次点击的ID，mLastClickTime记录当前点击的系统时间，如果两次点击的ID系统，点击时间小于1.5秒并且不是多选状态，就判断是打开文件或者文件夹。

```
 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mLastClickId.equals("" + i) && (Math.abs(System.currentTimeMillis() - mLastClickTime) < 1500) &&!mIsMutiSelect) {
            File mselectedFile = madapter.getItem(i);
            //view.setSelected(false);
            isCheckedMap.put(i, false);
            if (mselectedFile != null) {
                mforwardfiles.clear();
                open(mselectedFile, true);
            }
        } else {
            //取消其他选择，单选
            if (!mIsMutiSelect){
                for (int k = 0; k < mfiles.size(); k++) {
                    isCheckedMap.put(k, false);
                }
                isCheckedMap.put(i, true);
            }else{
                if (isCheckedMap.get(i))
                    isCheckedMap.put(i,false);
                else
                    isCheckedMap.put(i, true);
            }
            madapter.notifyDataSetChanged();

            mLastClickTime = System.currentTimeMillis();
            mLastClickId = "" + i;
        }
    }
```

##8、文件多选
文件多选是按住ctrl键，然后鼠标可以进行多选。mIsMutiSelect标记当前是否多选状态。

```
else if(event.isCtrlPressed()){
           mIsMutiSelect = true;
}
else if ((keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) && event.getAction() == KeyEvent.ACTION_UP)
{
      mIsMutiSelect = false;
 }
```
##9、文件热键

```
MainActivity里面重写public boolean dispatchKeyEvent(KeyEvent event)函数，对键盘按键进行监听：event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_A && event.getAction() == KeyEvent.ACTION_DOWN   //ctrl+A全选

event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_C && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+C复制
event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_X && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+X剪切
event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_V && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+V粘贴
event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_D && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+D删除
```
##10、文件显示类型
 文件显示类型分为：列表形式、网格形式

```
// 列表模式：详细信息列表，文件列表不显示具体详细，grid列表
	public static final int VIEWMODE_LIST_ll = 0;
	public static final int VIEWMODE_GRID = 2;

 private void showGridOrView(int view_mode) {
        mSearchEditor.setEnabled(true);
        mViewGridBtn.setEnabled(true);
        mViewListBtn.setEnabled(true);
        if (view_mode == HDBaseAdapter.VIEWMODE_GRID) {
            mHomeView.setVisibility(View.GONE);
            mListViewLine.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            mEmptyView02.setVisibility(View.VISIBLE);
            mCloudGridView.setVisibility(View.GONE);
        } else {
            mHomeView.setVisibility(View.GONE);
            mListViewLine.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
        }
    }
```

##11、云服务
###11.1、云服务文件类型
对应网络文件的三个状态，分别是已同步，没有同步，和加号（也就是添加本地同步文件）

```
int STATUS_SYNCHRONIZED = 1;
int STATUS_UNSYNCHRONIZED = 2;
int STATUS_ADD = -1;

云盘获取网络文件列表显示：
initCloudFile()函数里面获取并且显示文件，调用工程师的list命令，获取服务器的文件列表，每一个文件夹信息赋值给Seafile对象，每一个seafile对象分为已同步和未同步，然后加入到mCloudFiles列表中。然后让mCloudFileAdapter更新。
初始或者网络文件列表更新的时候就调用initCloudFile()。

添加同步文件夹：选择要同步的文件夹，对应的是DialogPathSelector类，onCreate函数里面调用同步命令

对已同步的文件解除同步：MenuDialog3类，onClick函数里面case R.id.desync: 处调用解除同步命令

对未同步的文件夹：
下载并同步：MenuDialog4 类，onClick函数里面case R.id.download: 处调用命令下载并同步
```
###11.2、选择文件路径
文件夹路径选择器：
    public class DialogPathSelector extends Dialog；

