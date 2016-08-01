#导航栏部分
##前进，后退
  实现是利用两个栈mbackwardfiles，mforwardfiles来存储打开的文件路径，点击前进后退按钮调用forward()， backward()函数来具体实现前进后退。
##显示当前路径
mCurrentPathEdit显示当前路径，mCurrentPathFile记录当前路径
##搜索
mSearchEditor是Editor控件来获取输入的关键字，然后对mSearchEditor进行监听，调用MyTool类的findFiles()函数来实现搜索。
#左边菜单部分
界面对应的是left.xml文件，点击改变颜色是对应的控件获取焦点是时候，改变颜色。
#右边界面的切换
右边显示部分的界面主要分为四种：显示磁盘界面home_view.xml,显示文件列表界面是activity_main.xml里面的listview控件，已网格显示的是activity_main.xml里面的GridView控件gridview，显示云服务界面的是activity_main.xml里面的GridView控件cloud_file_grid。通过对每个部分的显示和隐藏来对界面进行变换。
#USB的动态识别
##USB显示
USB获取挂载的U盘，是调用MyTool类的exec2函数运行df命令，获取当前的所有U盘的路径。USB的动态显示分为左边部分和右边部分。左边菜单部分，利用Listview显示U盘列表。右边部分利用gridview控件以硬盘形式显示USB信息。
##USB动态识别
MainActivity里面注册UsbStatesReceiver类，对USB的插入和拔出广播进行监听。
##USB的手动弹出
调用MyTool类的exec函数，运行remount命令解除对应的U盘的挂载。

#右键菜单
右键菜单分为四个：对文件或文件夹的MenuDialog1，对空白部分的MenuDialog2，对云服务已同步文件夹的MenuDialog3，对云服务未同步文件夹的MenuDialog4。
#文件复制进度对话框
文件复制进度对话框是CopyDialog类，动态显示文件复制或者剪切的百分比，对应的文件或文件夹，复制文件的总体大小。

#系统双击
用Map isCheckedMap记录文件是否被选择，mLastClickId记录最后一次点击的ID，mLastClickTime记录当前点击的系统时间，如果两次点击的ID系统，点击时间小于1.5秒并且不是多选状态，就判断是打开文件或者文件夹。

#文件多选
文件多选是按住ctrl键，然后鼠标可以进行多选。mIsMutiSelect标记当前是否多选状态。
#文件热键
MainActivity里面重写public boolean dispatchKeyEvent(KeyEvent event) {}函数，对键盘按键进行监听：event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_A && event.getAction() == KeyEvent.ACTION_DOWN   //ctrl+A全选

event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_C && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+C复制
event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_X && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+X剪切
event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_V && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+V粘贴
event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_D && event.getAction() == KeyEvent.ACTION_DOWN//ctrl+D删除
<br>//ctrl判断是否多选
<br>
else if(event.isCtrlPressed())<br>{
         <br>    mIsMutiSelect = true;
        }
else if ((keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) && event.getAction() == KeyEvent.ACTION_UP)<br>
{
      mIsMutiSelect = false;
 }


