package openos.filemanageropenos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import openos.filemanageropenos.Dialog.CopyDialog;
import openos.filemanageropenos.FileCtrol.Constant;
import openos.filemanageropenos.FileCtrol.FileComparator;
import openos.filemanageropenos.FileCtrol.HDBaseAdapter;
import openos.filemanageropenos.cloudservice.CloudFileAdapter;
import openos.filemanageropenos.cloudservice.FileAdapter;
import openos.filemanageropenos.cloudservice.SeafileInfo;
import openos.filemanageropenos.tools.MyTool;
import openos.filemanageropenos.usb.UsbDiskAdapter;
import openos.filemanageropenos.usb.UsbLeftAdapter;

public class MainActivity extends Activity implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemClickListener {
    final private String mMyDiskName = "OpenthOS";
    //home路径
    public static final String homePath = "/sdcard";
    public static MenuDialog1 menuDialog1;
    public static MenuDialog2 menuDialog2;
    public static MenuDialog3 menuDialog3;
    public static MenuDialog4 menuDialog4;

    public static CopyDialog copyDialog;
    private boolean canPaste;
    private long mFileSize;
    private long mCopySize;
    //private File mCopyFile;
    private List<File> mCopyFileList;
    private int mCopyIndex=0;
    private String mCopyTitle;
    private String mCopyOrCut = "";
    //判断是否多选
    private Boolean mIsMutiSelect = false;

    private LinearLayout mDeskLine;
    private LinearLayout mFileLine;
    private LinearLayout mPhotoLine;
    private LinearLayout mMusicLine;
    private LinearLayout mVideoLine;
    private LinearLayout mPCLine;
    //private LinearLayout mUSBLine;
    private LinearLayout mNetNeigborLine;
    private LinearLayout mCloudLine;
    //当前路径
    private File mCurrentPathFile;
    private EditText mCurrentPathEdit;
    //搜索框
    private EditText mSearchEditor;
    //BaseAdapter
    private HDBaseAdapter madapter = null;
    //我的电脑
    private LinearLayout mHomeView;
    //
    private LinearLayout mListViewLine;
    //ListView
    private ListView mListView = null;
    //GridView
    private GridView mGridView = null;
    //TextView
    private TextView mEmptyView01 = null;
    private TextView mEmptyView02 = null;
    //the data source
    private List<File> mfiles = null;
    private Map<Integer, Boolean> isCheckedMap = null;
    private Stack<File> mbackwardfiles = null;
    private Stack<File> mforwardfiles = null;

    //usb数据或者sd卡的路径文件
    private ArrayList<File> mUsbOrSdcard = null;
    private ListView mUsbLeftListView = null;
    private UsbLeftAdapter mUsbLeftAdaper = null;
    private GridView mUsbRightGridView = null;
    private UsbDiskAdapter mUsbDiskAdapter = null;
    private LinearLayout mUsbLeftLine;
    private LinearLayout mUsbRightLine;
    private UsbStatesReceiver usbstates;

    //cloud 文件显示的gridview
    private LinearLayout mCloudDiskLine;
    private GridView mCloudGridView;
    private List<SeafileInfo> mCloudFiles;
    private CloudFileAdapter mCloudFileAdapter;

    //前进后退按钮
    public ImageButton mBackBtn;
    private ImageButton mForwordBtn;

    //列表显示还是grid显示
    private ImageButton mViewListBtn;
    private ImageButton mViewGridBtn;

    //我的电脑盘
    private LinearLayout mOpenOsLine;
    private TextView mOpenOsTextName;
    private ProgressBar mOpenosProgressBar;
    private TextView mOpenOsTextSize;
    //抢走焦点的控件
    private View mCancelFocused;
    /**
     * 双击事件判断
     **/
    // 双击事件记录最近一次点击的ID
    private String mLastClickId = "";
    // 双击事件记录最近一次点击的时间
    private long mLastClickTime = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {//定义一个Handler，用于处理下载线程与UI间通讯
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 0:
                        copyDialog.setPercent("0%");
                        copyDialog.setProgressBar(0);
                        copyDialog.setTitle(mCopyTitle);
                        mCopyIndex = 0;
                        mCopySize = 0;
                        mFileSize = 0;
                        for (File file : mCopyFileList) {
                            if (file.isDirectory())
                                mFileSize += MyTool.getFileSizes(file);
                            else
                                mFileSize += file.length();
                        }
                        mFileSize += 1;//防止文件夹大小为0,出现除0错误，加一对文件夹的大小无影响

                    case 1:
                        int percent = (int) (mCopySize * 100 / mFileSize);
                        copyDialog.setProgressBar(percent);
                        //copyDialog.setPathdetail("把 "+mCopyFile.getName()+" 复制到 "+mCurrentPathFile.getAbsolutePath().replace(homePath,mMyDiskName));
                        copyDialog.setPercent("已完成 " + percent + "%");
                        copyDialog.setFilesize(MyTool.getFileSize(mCopySize) + "/" + MyTool.getFileSize(mFileSize));
                        break;
                    case 2:
                        Log.e("case2--mCopyOrCut", mCopyOrCut);
                        Log.e("case2--mCopyOrCut@", "2");
                        mCopyOrCut = "";
                        Toast.makeText(MainActivity.this, "文件粘贴完成", Toast.LENGTH_SHORT).show();
                        copyDialog.dismiss();
                        open(mCurrentPathFile, false);//刷新
                        break;
                    case -1:
                        String error = msg.getData().getString("error");
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        copyDialog.dismiss();
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("操作出错,权限不足或源文件已删除.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                        break;
                    case 4://提示文件的复制过程
                        copyDialog.setPathdetail("把 " + mCopyFileList.get(mCopyIndex++).getName() + " 复制到 " + mCurrentPathFile.getAbsolutePath().replace(homePath, mMyDiskName));
                        break;
                    case UsbStatesReceiver.USB_STATE_OFF:
                        // Toast.makeText(MainActivity.this,"USB_STATE_OFF",Toast.LENGTH_SHORT).show();
                        Log.e("USB_STATE_OFF", "USB_STATE_OFF");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        initUsb();
                        //显示主界面
                        if (!mCurrentPathFile.exists() || !mCurrentPathFile.canRead())
                            showHomeView();
                        break;
                    case UsbStatesReceiver.USB_STATE_ON:
                        Log.e("USB_STATE_ON", "USB_STATE_ON");
                        //Toast.makeText(MainActivity.this,"USB_STATE_ON",Toast.LENGTH_SHORT).show();
                        initUsb();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        init();
        initUsb();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        usbstates.registerReceiver();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        usbstates.unregisterReceiver();
    }

    private void init() {
        mDeskLine = (LinearLayout) findViewById(R.id.desk_line);
        mFileLine = (LinearLayout) findViewById(R.id.file_line);
        mPhotoLine = (LinearLayout) findViewById(R.id.photo_line);
        mMusicLine = (LinearLayout) findViewById(R.id.music_line);
        mVideoLine = (LinearLayout) findViewById(R.id.video_line);
        mPCLine = (LinearLayout) findViewById(R.id.pc_line);
        //mUSBLine = (LinearLayout) findViewById(R.id.usb_line);
        mCancelFocused = (View) findViewById(R.id.cancel_focused);
        mUsbLeftListView = (ListView) findViewById(R.id.usb_left_listview);
        mUsbRightGridView = (GridView) findViewById(R.id.usb_gridview);
        mNetNeigborLine = (LinearLayout) findViewById(R.id.netneigbor_line);
        mCloudLine = (LinearLayout) findViewById(R.id.cloud_line);
        mUsbLeftLine = (LinearLayout) findViewById(R.id.usb_left_listview_line);
        mUsbRightLine = (LinearLayout) findViewById(R.id.usb_right_gridview_line);
        mCurrentPathEdit = (EditText) findViewById(R.id.current_path);
        mSearchEditor = (EditText) findViewById(R.id.search_editor);
        mCloudGridView = (GridView) findViewById(R.id.cloud_file_grid);

        mCopyFileList = new ArrayList<>();
        mCopyFileList.add(new File(homePath + "/Download/1.apk"));

        mfiles = new ArrayList<File>();
        isCheckedMap = new HashMap<Integer, Boolean>();
        madapter = new HDBaseAdapter(this, mfiles, isCheckedMap);

        mHomeView = (LinearLayout) findViewById(R.id.home_view);
        mListViewLine = (LinearLayout) findViewById(R.id.list_view);
        //显示文件控件的注册
        mListView = (ListView) findViewById(R.id.list_files);
        mGridView = (GridView) findViewById(R.id.gridview);
        mEmptyView01 = (TextView) findViewById(R.id.empty0);
        mEmptyView02 = (TextView) findViewById(R.id.empty1);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(mEmptyView01);
        registerForContextMenu(mGridView);
        mGridView.setOnItemClickListener(this);
        mGridView.setEmptyView(mEmptyView02);
        mCloudDiskLine = (LinearLayout) findViewById(R.id.cloud_disk);

        mbackwardfiles = new Stack<File>();
        mforwardfiles = new Stack<File>();
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        mForwordBtn = (ImageButton) findViewById(R.id.forword_btn);
        mViewListBtn = (ImageButton) findViewById(R.id.view_list);
        mViewGridBtn = (ImageButton) findViewById(R.id.view_grid);
        mOpenOsLine = (LinearLayout) findViewById(R.id.mydisk).findViewById(R.id.disk);
        mOpenOsTextName = (TextView) findViewById(R.id.mydisk).findViewById(R.id.name);
        mOpenOsTextSize = (TextView) findViewById(R.id.mydisk).findViewById(R.id.info);
        mOpenosProgressBar = (ProgressBar) findViewById(R.id.mydisk).findViewById(R.id.proessbar);

        //注意 OnTouchListener 和 OnClickListener区别
        mDeskLine.setOnTouchListener(this);
        mFileLine.setOnTouchListener(this);
        mPhotoLine.setOnTouchListener(this);
        mMusicLine.setOnTouchListener(this);
        mVideoLine.setOnTouchListener(this);
        mPCLine.setOnTouchListener(this);
        mNetNeigborLine.setOnTouchListener(this);
        mCloudLine.setOnTouchListener(this);

        mBackBtn.setOnClickListener(this);
        mForwordBtn.setOnClickListener(this);
        mViewListBtn.setOnClickListener(this);
        mViewGridBtn.setOnClickListener(this);
        usbstates = new UsbStatesReceiver(this);
        copyDialog = new CopyDialog(this, R.style.dialog);
        menuDialog1 = new MenuDialog1(this, R.style.dialog);
        menuDialog2 = new MenuDialog2(this, R.style.dialog);//设置style
        menuDialog3 = new MenuDialog3(this, R.style.dialog);//设置style
        menuDialog4 = new MenuDialog4(this, R.style.dialog);//设置style

        setViewMode(madapter.getViewMode());
        canPaste = false;
        //初始界面是主界面
        showHomeView();
        //设置对磁盘openthos的点击事件监听
        mOpenOsLine.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mLastClickId.equals("mydisk") && (Math.abs(System.currentTimeMillis() - mLastClickTime) < 1500)) {
                    //mOpenOsLine.requestFocusFromTouch();
                    showGridOrView(madapter.getViewMode());
                    File home = new File(homePath);
                    mforwardfiles.clear();
                    open(home, true);

                } else {
                    //mOpenOsLine.setSelected(true);
                    mOpenOsLine.requestFocusFromTouch();
                    mLastClickTime = System.currentTimeMillis();
                    mLastClickId = "mydisk";
                }

                return false;
            }
        });
        //对云盘进行监听
        mCloudDiskLine.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mLastClickId.equals("myclouddisk") && (Math.abs(System.currentTimeMillis() - mLastClickTime) < 1500)) {
                    initCloudFile();
                    showCloudFileGridView();
                } else {
                    //mOpenOsLine.setSelected(true);
                    mCloudDiskLine.requestFocusFromTouch();
                    mLastClickTime = System.currentTimeMillis();
                    mLastClickId = "myclouddisk";
                }

                return false;
            }
        });
        //对currenpath的editview进行监听
        mCurrentPathEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    String path = mCurrentPathEdit.getText().toString().trim().replace(mMyDiskName, homePath);
                    Log.e("mCurrentPathEdit Click", path);
                    /*隐藏软键盘*/
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager.isActive()) {
                        inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                    File file = new File(path);
                    if (!file.exists()) {
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("系统找不到\"" + path + "\".请检查拼写并重试.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                        return true;
                    }
                    mforwardfiles.clear();
                    showGridOrView(madapter.getViewMode());
                    open(file, true);
                    return true;
                } else
                    return false;
            }
        });
        //搜索框的监听
        mSearchEditor.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
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
                    return true;
                } else
                    return false;
            }
        });


        //对listview的空白地方进行监听
        mListView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                    SharedPreferences sharedPreferences = getSharedPreferences("isfileordir", Context.MODE_PRIVATE);
                    boolean isclickfile = sharedPreferences.getBoolean("isclickfile", false);//判断是否点击的是文件或文件夹
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isclickfile", false);
                    editor.commit();//提交修改
                    if (!isclickfile) {
                        int[] location = new int[2];
                        mBackBtn.getLocationOnScreen(location);
                        if (menuDialog1 != null)
                            menuDialog1.dismiss();
                        //menuDialog2 = new MenuDialog2(MainActivity.this, R.style.dialog);//设置style
                        menuDialog2.showDialog((int) motionEvent.getRawX() - location[0], (int) motionEvent.getRawY() - location[1] + 57, 285, 200);
                        menuDialog2.setEnablePaste(canPaste);
                        return true;
                    }
                }
                return false;
            }
        });
        //对GridView的空白地方进行监听
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
        //对文件夹为空时，设置点击事件
        mEmptyView01.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                    int[] location = new int[2];
                    mBackBtn.getLocationOnScreen(location);
                    menuDialog2.showDialog((int) motionEvent.getRawX() - location[0], (int) motionEvent.getRawY() - location[1] + 57, 285, 200);
                    menuDialog2.setEnablePaste(canPaste);
                    return true;
                }
                return false;
            }
        });
        mEmptyView02.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                if (motionEvent.getButtonState() == MotionEvent.BUTTON_SECONDARY) {
                    int[] location = new int[2];
                    mBackBtn.getLocationOnScreen(location);
                    menuDialog2.showDialog((int) motionEvent.getRawX() - location[0], (int) motionEvent.getRawY() - location[1] + 57, 285, 200);
                    menuDialog2.setEnablePaste(canPaste);
                    return true;
                }
                return false;
            }
        });

    }

    //对usb设备读取处理
    private void initUsb() {
//        String []cmd = {"df"};
//        Log.e("exec df:",MyTool.exec2(cmd).get(0));
        String[] cmd = {"df"};
        ArrayList<String> usbs = MyTool.exec2(cmd);
        //如果没有u盘或者sd卡，就不显示移动设备这个条目
        if (usbs == null || usbs.size() == 0) {
            mUsbLeftLine.setVisibility(View.GONE);
            mUsbRightLine.setVisibility(View.GONE);
            return;
        }
        mUsbLeftLine.setVisibility(View.VISIBLE);
        mUsbRightLine.setVisibility(View.VISIBLE);

        if (mUsbOrSdcard == null) {
            mUsbOrSdcard = new ArrayList<>();
            mUsbLeftAdaper = new UsbLeftAdapter(this, mUsbOrSdcard);
            mUsbLeftListView.setAdapter(mUsbLeftAdaper);
            mUsbDiskAdapter = new UsbDiskAdapter(this, mUsbOrSdcard);
            mUsbRightGridView.setAdapter(mUsbDiskAdapter);

            mUsbLeftListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    view.setSelected(true);
                    mCancelFocused.requestFocusFromTouch();
                    showGridOrView(madapter.getViewMode());
                    mforwardfiles.clear();
                    open(mUsbLeftAdaper.getItem(position), true);
                }
            });
            mUsbRightGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mCancelFocused.requestFocusFromTouch();
                    if (mLastClickId.equals(mUsbDiskAdapter.getItem(i).getName()) && (Math.abs(System.currentTimeMillis() - mLastClickTime) < 1500)) {
                        view.setSelected(false);
                        showGridOrView(madapter.getViewMode());
                        mforwardfiles.clear();
                        open(mUsbDiskAdapter.getItem(i), true);

                    } else {
                        view.setSelected(true);
                        mLastClickTime = System.currentTimeMillis();
                        mLastClickId = mUsbDiskAdapter.getItem(i).getName();
                    }
                }
            });
        } else mUsbOrSdcard.clear();
        for (String path : usbs) {
            File file = new File(path);
            mUsbOrSdcard.add(file);
        }
        mUsbLeftAdaper.notifyDataSetChanged();
        mUsbDiskAdapter.notifyDataSetChanged();

        //动态设置左边usb listview的高度
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mUsbLeftLine.getLayoutParams();
        linearParams.height = usbs.size() * 60;
        mUsbLeftLine.setLayoutParams(linearParams);
    }

    //设置显示模式
    public void setViewMode(int viewmode) {
        switch (viewmode) {
            case HDBaseAdapter.VIEWMODE_LIST_ll:
                showGridOrView(HDBaseAdapter.VIEWMODE_LIST_ll);

                madapter.setViewMode(HDBaseAdapter.VIEWMODE_LIST_ll);
                mListView.setAdapter(madapter);
                mGridView.setAdapter(null);
                madapter.notifyDataSetChanged();
                break;
            case HDBaseAdapter.VIEWMODE_GRID:
                Log.e("setmode VIEWMODE_GRID", "01");
                showGridOrView(HDBaseAdapter.VIEWMODE_GRID);
                madapter.setViewMode(HDBaseAdapter.VIEWMODE_GRID);
                mListView.setAdapter(null);
                mGridView.setAdapter(madapter);
                madapter.notifyDataSetChanged();
                break;
            default:
                break;
        }

    }

    private void open(File f, boolean misAddToBackWardFiles) {
        //Log.e("open", "open");
        //  如果目录不是以homePath开头，就当做不存在，不予以显示， 路径错误是，文件夹为空
        //if (!f.getAbsolutePath().startsWith(homePath) || f == null || !f.exists() || !f.canRead()) {
        if (f == null || !f.exists() || !f.canRead()) {
            mfiles.clear();
            isCheckedMap.clear();
            madapter.notifyDataSetChanged();
            new AlertDialog.Builder(this).setTitle("提示").setMessage("权限不足或路径错误").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
            return;
        }

//        if (!f.getAbsolutePath().startsWith(homePath))
//            return;
//        if(f == null)
//            return;
//        if(!f.exists())
//            return;
//        if(!f.canRead()) {
//            new AlertDialog.Builder(this).setTitle("提示").setMessage("权限不足").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick (DialogInterface dialog, int which) { }
//            }).show();
//            return;
//        }
        if (f.isFile()) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                String type = Constant.getMIMEType(f);
                intent.setDataAndType(Uri.fromFile(f), type);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {

            }

        } else if (f.isDirectory()) {
            mfiles.clear();
            Log.e("f path", f.getAbsolutePath());
            if (misAddToBackWardFiles) {
                Log.e("mbackaddfile", mCurrentPathFile.getAbsolutePath());
                mbackwardfiles.push(mCurrentPathFile);
            }
            mCurrentPathFile = f;
            //设置我的路径，不显示真实的路径
            mCurrentPathEdit.setText(mCurrentPathFile.getAbsolutePath().replace(homePath, mMyDiskName));

            File[] files = f.listFiles();
            // 排序
            Arrays.sort(files, new FileComparator());

            for (File file : files) {
//                if(!misShowHiddenFiles && file.isHidden()){
//                    continue;
//                }
                //不显示隐藏文件
                if (file.isHidden())
                    continue;
                mfiles.add(file);
            }
            isCheckedMap.clear();
            for (int i = 0; i < mfiles.size(); i++)
                isCheckedMap.put(i, false);
            Log.e("open:mfiles", "" + mfiles.size());
            madapter.notifyDataSetChanged();
        }
    }

    //打开方式
    private void openwith(File f) {
        Log.i("openwith", "open");

        if (!f.exists())
            return;

        if (!f.canRead())
            return;

        if (f.isDirectory()) {
            open(f, true);
            return;
        }

        if (f.isFile()) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                //String type = Constant.getMIMEType(f);
                intent.setDataAndType(Uri.fromFile(f), "*/*");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {

            }

        }
    }

    //后退一步
    public void backward() {
        Log.e("backword0", "" + mbackwardfiles.size());
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

    //前进一步
    public void forward() {
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

    //获取根目录下面的所有的数据，然后设置到我们的ListView中让它显示出来。
    //对输入文件夹的移植迭代
    public static ArrayList<File> listChildren(File folder) {
        ArrayList<File> files = new ArrayList<File>();
        File[] filterFiles = folder.listFiles();
        if (filterFiles == null) return files;
        Arrays.sort(filterFiles, new FileComparator());
        if (null != filterFiles && filterFiles.length > 0) {
            for (File file : filterFiles) {
                if (file.isDirectory())
                    files.add(file);
            }
        }
        return files;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.e("onItemClick", "" + i);
//        MenuDialog1 menuDialog1 = new MenuDialog1(this,R.style.dialog,0);
//        menuDialog1.showDialog(600,200,600,320);

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

    //显示主界面
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

    //云服务文件数据
    private void initCloudFile() {
        if (mCloudFiles == null) {
            mCloudFiles = new ArrayList<SeafileInfo>();
            mCloudFileAdapter = new CloudFileAdapter(this, mCloudFiles);
            mCloudGridView.setAdapter(mCloudFileAdapter);
            MyTool.exec("seaf-cli init -d "+homePath);//第一次初始化时配置


            mCloudGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mLastClickId.equals("" + i) && (Math.abs(System.currentTimeMillis() - mLastClickTime) < 1500)) {
                        SeafileInfo mselectedFile = mCloudFileAdapter.getItem(i);
                        view.setSelected(false);
                        //如果同步了，双击打开同步了的文件夹
                        if (mselectedFile.status == SeafileInfo.STATUS_SYNCHRONIZED) {
                            mforwardfiles.clear();
                            showGridOrView(madapter.getViewMode());
                            open(new File(mselectedFile.path), true);
                        } else if (mselectedFile.status == SeafileInfo.STATUS_ADD) {
                            new DialogPathSelector(MainActivity.this).showDialog(1000, 800);
                        }
                    } else {
                        view.setSelected(true);
                        mLastClickTime = System.currentTimeMillis();
                        mLastClickId = "" + i;
                    }
                }
            });
        } else {
            mCloudFiles.clear();
        }
        //一定添加的加号，表示要添加同步文件夹
        mCloudFiles.add(new SeafileInfo());

        MyTool.exec("seaf-cli start");
        /******
         * 调用函数去获取文件列表详细，并保存到SeafileInfo类，加入到
         * mCloudFiles云盘文件列表
         * ****/
        ArrayList<SeafileInfo> seafileList1=MyTool.exec_seafile_list("seaf-cli list-remote -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716");
        ArrayList<SeafileInfo> seafileList2=MyTool.exec_seafile_list("seaf-cli list");
        boolean flag = false;
        for (int i =0; i<seafileList1.size();i++){
            flag = true;
            for (int j =0; j<seafileList2.size();j++){
                if (seafileList1.get(i)==seafileList2.get(j)){
                    mCloudFiles.add(seafileList2.get(j));
                    flag = false;
                }
            }
            if (flag)
                mCloudFiles.add(seafileList1.get(i));
        }


        /******
         * 假数据，自己调用函数去获取文件列表详细，并保存到SeafileInfo类，加入到
         * mCloudFiles云盘文件列表
         * ****/
        //已同步的文件夹
        SeafileInfo seafileInfo1 = new SeafileInfo();
        seafileInfo1.status = SeafileInfo.STATUS_SYNCHRONIZED;
        seafileInfo1.path = "/sdcard/DCIM";
        seafileInfo1.name = "DCIM";
        mCloudFiles.add(seafileInfo1);
        //没有同步的文件夹
        SeafileInfo seafileInfo2 = new SeafileInfo();
        seafileInfo2.status = SeafileInfo.STATUS_UNSYNCHRONIZED;
        //seafileInfo1.path = "/storage/DCIM";
        seafileInfo2.name = "我的资料库";
        mCloudFiles.add(seafileInfo2);

        //刷新
        mCloudFileAdapter.notifyDataSetChanged();
    }

    private void show_ROM_storage() {
        String path = Environment.getDataDirectory().getPath();
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();
        long usedBlocks = totalBlocks - availableBlocks;
        mOpenosProgressBar.setProgress((int) (usedBlocks * 100 / totalBlocks));
        Log.e("percent", "" + (int) (usedBlocks * 100 / totalBlocks));
        mOpenOsTextSize.setText(MyTool.getFileSize(availableBlocks * blockSize) + "可用,共有" + MyTool.getFileSize(totalBlocks * blockSize));

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.e("onclick", "view");
        if (mUsbDiskAdapter != null)
            mUsbDiskAdapter.notifyDataSetChanged();
        if (mUsbLeftAdaper != null)
            mUsbLeftAdaper.notifyDataSetChanged();
        switch (view.getId()) {
            case R.id.desk_line:
                Log.e("onclick", "desk_line");
                mDeskLine.requestFocusFromTouch();
                showGridOrView(madapter.getViewMode());
                File desk = new File(homePath + "/Android");
                mforwardfiles.clear();
                open(desk, true);
                break;
            case R.id.file_line:
                mFileLine.requestFocusFromTouch();
                showGridOrView(madapter.getViewMode());
                File filedir = new File(homePath + "/Download");
                mforwardfiles.clear();
                open(filedir, true);
                break;
            case R.id.photo_line:
                mPhotoLine.requestFocusFromTouch();
                showGridOrView(madapter.getViewMode());
                File photodir = new File(homePath + "/DCIM");
                mforwardfiles.clear();
                open(photodir, true);
                break;
            case R.id.music_line:
                mMusicLine.requestFocusFromTouch();
                showGridOrView(madapter.getViewMode());
                File musicdir = new File(homePath + "/Music");
                mforwardfiles.clear();
                open(musicdir, true);
                break;
            case R.id.video_line:
                mVideoLine.requestFocusFromTouch();
                showGridOrView(madapter.getViewMode());
                File videodir = new File(homePath + "/Movies");
                mforwardfiles.clear();
                open(videodir, true);
                break;
            case R.id.pc_line:
                mPCLine.requestFocusFromTouch();
                showHomeView();
                break;
//            case R.id.usb_line:
//                mUSBLine.setSelected(true);
//                break;
            case R.id.netneigbor_line:
                mNetNeigborLine.requestFocusFromTouch();

                break;
            case R.id.cloud_line:
                mCloudLine.requestFocusFromTouch();
                initCloudFile();
                showCloudFileGridView();
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                backward();
                break;
            case R.id.forword_btn:
                forward();
                break;
            case R.id.view_list:
                showGridOrView(HDBaseAdapter.VIEWMODE_LIST_ll);
                setViewMode(HDBaseAdapter.VIEWMODE_LIST_ll);
                break;
            case R.id.view_grid:
                showGridOrView(HDBaseAdapter.VIEWMODE_GRID);
                setViewMode(HDBaseAdapter.VIEWMODE_GRID);
                break;
            default:
                break;
        }
    }

    //添加热键支持
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        //ctrl+A全选
        if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_A && event.getAction() == KeyEvent.ACTION_DOWN) {
            Set<Integer> set = isCheckedMap.keySet();
            Iterator<Integer> iterator = set.iterator();
            while (iterator.hasNext()) {
                Integer keyId = iterator.next();
                isCheckedMap.put(keyId, true);
            }
            madapter.notifyDataSetChanged();
        } else if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_C && event.getAction() == KeyEvent.ACTION_DOWN) {//ctrl+C复制
            Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
            // copyDialog.setInfo("正在复制","","",0);
            mCopyOrCut = "copy";
            mCopyTitle = "正在复制";
            //mCopyFile = madapter.getItem(position);
            if (mCopyFileList == null)
                mCopyFileList = new ArrayList<>();
            else
                mCopyFileList.clear();
            Set<Integer> set = isCheckedMap.keySet();
            Iterator<Integer> iterator = set.iterator();
            while (iterator.hasNext()) {
                Integer keyId = iterator.next();
                if (isCheckedMap.get(keyId)) {
                    mCopyFileList.add(madapter.getItem(keyId));
                }
            }
            canPaste = true;
        } else if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_X && event.getAction() == KeyEvent.ACTION_DOWN) {//ctrl+X剪切
            Toast.makeText(MainActivity.this, "剪切成功", Toast.LENGTH_SHORT).show();
            mCopyOrCut = "cut";
            mCopyTitle = "正在复制";
            //mCopyFile = madapter.getItem(position);
            if (mCopyFileList == null)
                mCopyFileList = new ArrayList<>();
            else
                mCopyFileList.clear();
            Set<Integer> set2 = isCheckedMap.keySet();
            Iterator<Integer> iterator2 = set2.iterator();
            while (iterator2.hasNext()) {
                Integer keyId = iterator2.next();
                if (isCheckedMap.get(keyId)) {
                    mCopyFileList.add(madapter.getItem(keyId));
                }
            }
            canPaste = true;
        } else if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_V && event.getAction() == KeyEvent.ACTION_DOWN) {//ctrl+V粘贴
            //当前路径是目的地，检查有没有这个名字的文件或文件夹
            for (File file0 : mCopyFileList) {
                File file = new File(mCurrentPathFile.getAbsolutePath() + "/" + file0.getName());
                if (file.exists()) {
                    //menuDialog2.dismiss();
                    new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage(file0.getName() + "已存在，请检查并重试.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                    return false;
                }
            }
            copyDialog.showDialog();
            new Thread() {
                public void run() {
                    Looper.prepare();
                    copyFileOrFolders(mCopyFileList);
                }
            }.start();

            open(mCurrentPathFile, false);//刷新
            if (mCopyOrCut.equals("cut")) {
                canPaste = false;
            }
        }else if (event.isCtrlPressed() && keyCode == KeyEvent.KEYCODE_D && event.getAction() == KeyEvent.ACTION_DOWN) {//ctrl+D删除
            Set<Integer> set = isCheckedMap.keySet();
            Iterator<Integer> iterator = set.iterator();
            while (iterator.hasNext()) {
                Integer keyId = iterator.next();
                if(isCheckedMap.get(keyId)){
                    if (!MyTool.deleteGeneralFile(madapter.getItem(keyId).getAbsolutePath())) {
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("删除失败，权限不足.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                        return false;
                    }
                }
            }
            Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            open(mCurrentPathFile, false);//刷新
        }else if(event.isCtrlPressed()){
            mIsMutiSelect = true;
        }else if ((keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) && event.getAction() == KeyEvent.ACTION_UP){
            mIsMutiSelect = false;
        }

        return super.dispatchKeyEvent(event);
    }

    //對文件右鍵菜單
    public class MenuDialog1 extends Dialog implements View.OnClickListener {
        private boolean flag;

        private TextView open;
        private TextView openwith;
        private TextView copy;
        private TextView cut;
        private TextView del;
        private TextView rename;
        private TextView proper;
        private int position;

        public MenuDialog1(Context context) {
            super(context);
        }

        public MenuDialog1(Context context, int themeResId) {
            super(context, themeResId);
        }

        public void setPosition(int pos) {
            position = pos;
        }

        protected MenuDialog1(Context context, boolean cancelable, OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.right_click_menu1);
            open = (TextView) findViewById(R.id.open);
            openwith = (TextView) findViewById(R.id.openwith);
            copy = (TextView) findViewById(R.id.copy);
            cut = (TextView) findViewById(R.id.cut);
            del = (TextView) findViewById(R.id.delete);
            rename = (TextView) findViewById(R.id.rename);
            proper = (TextView) findViewById(R.id.proper);
            flag = true;

            open.setOnClickListener(this);
            openwith.setOnClickListener(this);
            copy.setOnClickListener(this);
            cut.setOnClickListener(this);
            del.setOnClickListener(this);
            rename.setOnClickListener(this);
            proper.setOnClickListener(this);
        }

        public void setEnableOpenwith(boolean can) {
            flag = can;
            if (can) {
                openwith.setTextColor(Color.parseColor("#000000"));
            } else {
                openwith.setTextColor(Color.parseColor("#b19898"));
            }
        }

        public void showDialog(int x, int y, int height, int width) {
            //show在前面，设置高度，宽度和位置才有效
            show();
            // setContentView可以设置为一个View也可以简单地指定资源ID
            // LayoutInflater
            // li=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            // View v=li.inflate(R.layout.dialog_layout, null);
            // dialog.setContentView(v);
        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置,
         * 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
            Window dialogWindow = getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);

            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        /*
         * lp.x与lp.y表示相对于原始位置的偏移.
         * 当参数值包含Gravity.LEFT时,对话框出现在左边,所以lp.x就表示相对左边的偏移,负值忽略.
         * 当参数值包含Gravity.RIGHT时,对话框出现在右边,所以lp.x就表示相对右边的偏移,负值忽略.
         * 当参数值包含Gravity.TOP时,对话框出现在上边,所以lp.y就表示相对上边的偏移,负值忽略.
         * 当参数值包含Gravity.BOTTOM时,对话框出现在下边,所以lp.y就表示相对下边的偏移,负值忽略.
         * 当参数值包含Gravity.CENTER_HORIZONTAL时
         * ,对话框水平居中,所以lp.x就表示在水平居中的位置移动lp.x像素,正值向右移动,负值向左移动.
         * 当参数值包含Gravity.CENTER_VERTICAL时
         * ,对话框垂直居中,所以lp.y就表示在垂直居中的位置移动lp.y像素,正值向右移动,负值向左移动.
         * gravity的默认值为Gravity.CENTER,即Gravity.CENTER_HORIZONTAL |
         * Gravity.CENTER_VERTICAL.
         *
         * 本来setGravity的参数值为Gravity.LEFT | Gravity.TOP时对话框应出现在程序的左上角,但在
         * 我手机上测试时发现距左边与上边都有一小段距离,而且垂直坐标把程序标题栏也计算在内了,
         * Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM与Gravity.RIGHT都是如此,据边界有一小段距离
         */
            if (x > (d.getWidth() - 200))//判断显示的dialog是否超出屏幕
                lp.x = d.getWidth() - 200;
            else
                lp.x = x; // 新位置X坐标
            if (y > (d.getHeight() - 400))
                lp.y = d.getHeight() - 400;
            else
                lp.y = y - 10;

            lp.width = width; // 宽度
            lp.height = height; // 高度
            lp.alpha = 0.9f; // 透明度

            // 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
            // dialog.onWindowAttributesChanged(lp);
            dialogWindow.setAttributes(lp);

        /*
         * 将对话框的大小按屏幕大小的百分比设置
         */
//        WindowManager m = getWindowManager();
//        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
//        p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
//        dialogWindow.setAttributes(p);


        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.open:
                    open(madapter.getItem(position), true);
                    dismiss();
                    break;
                case R.id.openwith:
                    Log.e("openwith", "1");
                    if (flag) {
                        openwith(madapter.getItem(position));//看标志，确定是否可以点击
                        dismiss();
                    }
                    break;
                case R.id.copy:
                    Log.e("copy", "1");
                    Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                    // copyDialog.setInfo("正在复制","","",0);
                    mCopyOrCut = "copy";
                    mCopyTitle = "正在复制";
                    //mCopyFile = madapter.getItem(position);
                    if (mCopyFileList == null)
                        mCopyFileList = new ArrayList<>();
                    else
                        mCopyFileList.clear();
                    Set<Integer> set = isCheckedMap.keySet();
                    Iterator<Integer> iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Integer keyId = iterator.next();
                        if (isCheckedMap.get(keyId)) {
                            mCopyFileList.add(madapter.getItem(keyId));
                        }
                    }

                    canPaste = true;
                    dismiss();
                    break;
                case R.id.cut:
                    Log.e("cut", "1");
                    Toast.makeText(MainActivity.this, "剪切成功", Toast.LENGTH_SHORT).show();
                    mCopyOrCut = "cut";
                    mCopyTitle = "正在复制";
                    //mCopyFile = madapter.getItem(position);
                    if (mCopyFileList == null)
                        mCopyFileList = new ArrayList<>();
                    else
                        mCopyFileList.clear();
                    Set<Integer> set2 = isCheckedMap.keySet();
                    Iterator<Integer> iterator2 = set2.iterator();
                    while (iterator2.hasNext()) {
                        Integer keyId = iterator2.next();
                        if (isCheckedMap.get(keyId)) {
                            mCopyFileList.add(madapter.getItem(keyId));
                        }
                    }
                    canPaste = true;
                    dismiss();
                    break;
                case R.id.delete:
                    Log.e("delete", "1");
                    dismiss();
                    if (!MyTool.deleteGeneralFile(madapter.getItem(position).getAbsolutePath())) {
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("删除失败，权限不足.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    open(mCurrentPathFile, false);//刷新
                    break;
                case R.id.rename:
                    Log.e("rename", "1");
                    dismiss();
                    getRenameDialog(position).show();

                    break;
                case R.id.proper://文件或者文件夹的属性
                    Log.e("proper", "1");
                    dismiss();
                    getFileAttributions(position).show();
                    break;
            }

        }
    }

    //空白處右鍵菜單
    public class MenuDialog2 extends Dialog implements View.OnClickListener {
        private boolean flag;
        private TextView newdir;
        private TextView newfile;
        private TextView refresh;
        private TextView paste;
        private TextView selectall;

        public void setEnablePaste(boolean can) {
            flag = can;
            if (can) {
                paste.setTextColor(Color.parseColor("#000000"));
            } else {
                paste.setTextColor(Color.parseColor("#b19898"));
            }
        }

        public MenuDialog2(Context context) {
            super(context);
        }

        public MenuDialog2(Context context, int themeResId) {
            super(context, themeResId);
        }

        protected MenuDialog2(Context context, boolean cancelable, OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.right_click_menu2);

            newdir = (TextView) findViewById(R.id.newdir);
            newfile = (TextView) findViewById(R.id.newfile);
            refresh = (TextView) findViewById(R.id.refresh);
            paste = (TextView) findViewById(R.id.paste);
            selectall = (TextView) findViewById(R.id.selectall);
            flag = true;

            newdir.setOnClickListener(this);
            newfile.setOnClickListener(this);
            refresh.setOnClickListener(this);
            paste.setOnClickListener(this);
            selectall.setOnClickListener(this);

        }

        public void showDialog(int x, int y, int height, int width) {
            show();
            // setContentView可以设置为一个View也可以简单地指定资源ID
            // LayoutInflater
            // li=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            // View v=li.inflate(R.layout.dialog_layout, null);
            // dialog.setContentView(v);
        /*
         * 获取圣诞框的窗口对象及参数对象以修改对话框的布局设置,
         * 可以直接调用getWindow(),表示获得这个Activity的Window
         * 对象,这样这可以以同样的方式改变这个Activity的属性.
         */
            Window dialogWindow = getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);

            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        /*
         * lp.x与lp.y表示相对于原始位置的偏移.
         * 当参数值包含Gravity.LEFT时,对话框出现在左边,所以lp.x就表示相对左边的偏移,负值忽略.
         * 当参数值包含Gravity.RIGHT时,对话框出现在右边,所以lp.x就表示相对右边的偏移,负值忽略.
         * 当参数值包含Gravity.TOP时,对话框出现在上边,所以lp.y就表示相对上边的偏移,负值忽略.
         * 当参数值包含Gravity.BOTTOM时,对话框出现在下边,所以lp.y就表示相对下边的偏移,负值忽略.
         * 当参数值包含Gravity.CENTER_HORIZONTAL时
         * ,对话框水平居中,所以lp.x就表示在水平居中的位置移动lp.x像素,正值向右移动,负值向左移动.
         * 当参数值包含Gravity.CENTER_VERTICAL时
         * ,对话框垂直居中,所以lp.y就表示在垂直居中的位置移动lp.y像素,正值向右移动,负值向左移动.
         * gravity的默认值为Gravity.CENTER,即Gravity.CENTER_HORIZONTAL |
         * Gravity.CENTER_VERTICAL.
         *
         * 本来setGravity的参数值为Gravity.LEFT | Gravity.TOP时对话框应出现在程序的左上角,但在
         * 我手机上测试时发现距左边与上边都有一小段距离,而且垂直坐标把程序标题栏也计算在内了,
         * Gravity.LEFT, Gravity.TOP, Gravity.BOTTOM与Gravity.RIGHT都是如此,据边界有一小段距离
         */
            if (x > (d.getWidth() - 200))
                lp.x = d.getWidth() - 200;
            else
                lp.x = x; // 新位置X坐标
            if (y > (d.getHeight() - 285))
                lp.y = d.getHeight() - 285;
            else
                lp.y = y - 10;
            // 新位置Y坐标
            lp.width = width; // 宽度
            lp.height = height; // 高度
            lp.alpha = 0.9f; // 透明度

            // 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
            // dialog.onWindowAttributesChanged(lp);
            dialogWindow.setAttributes(lp);

        /*
         * 将对话框的大小按屏幕大小的百分比设置
         */
//        WindowManager m = getWindowManager();
//        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
//        p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
//        dialogWindow.setAttributes(p);


        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.newdir:
                    Log.e("newdir", "1");
                    dismiss();
                    if (!MyTool.mkdir(mCurrentPathFile.getAbsolutePath())) {
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("文件夹创建失败，权限不足.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                        return;
                    }

                    open(mCurrentPathFile, false);//刷新
                    break;
                case R.id.newfile:
                    Log.e("newfile", "1");
                    dismiss();
                    if (!MyTool.createNewFile(mCurrentPathFile.getAbsolutePath())) {
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("文件创建失败，权限不足.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                        return;
                    }
                    open(mCurrentPathFile, false);//刷新
                    break;
                case R.id.refresh:
                    Log.e("refresh", "1");
                    Toast.makeText(MainActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                    open(mCurrentPathFile, false);//刷新
                    dismiss();
                    break;
                case R.id.paste:
                    Log.e("paste", "1");
                    if (flag) {//看标志，确定是否可以点击
                        //当前路径是目的地，检查有没有这个名字的文件或文件夹
                        for (File file0 : mCopyFileList) {
                            File file = new File(mCurrentPathFile.getAbsolutePath() + "/" + file0.getName());
                            if (file.exists()) {
                                menuDialog2.dismiss();
                                new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage(file0.getName() + "已存在，请检查并重试.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                                return;
                            }
                        }
                        copyDialog.showDialog();
                        new Thread() {
                            public void run() {
                                Looper.prepare();
                                copyFileOrFolders(mCopyFileList);
                            }
                        }.start();

                        open(mCurrentPathFile, false);//刷新
                        if (mCopyOrCut.equals("cut")) {
                            canPaste = false;
                        }
                        dismiss();
                    }
                    break;
                case R.id.selectall:
                    Log.e("selectall", "1");
                    Set<Integer> set = isCheckedMap.keySet();
                    Iterator<Integer> iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Integer keyId = iterator.next();
                        isCheckedMap.put(keyId, true);
                    }
                    madapter.notifyDataSetChanged();
                    dismiss();
                    break;
            }
        }
    }

    //重命名的dialog
    public Dialog getRenameDialog(int pos) {
        final File mRenameFile = madapter.getItem(pos);

        AlertDialog.Builder mrenamedialog = new AlertDialog.Builder(this);
        View renamelayout = LayoutInflater.from(this).inflate(R.layout.file_rename, null);
        final EditText renametext = (EditText) renamelayout.findViewById(R.id.file_name);
        renametext.setText(mRenameFile.getName());
        mrenamedialog.setTitle("重命名");
        mrenamedialog.setView(renamelayout);
        mrenamedialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                String path = mRenameFile.getParentFile().getPath();
                String newName = renametext.getText().toString().trim();
                if (newName.equalsIgnoreCase(mRenameFile.getName())) {
                    return;
                }
                if (newName.length() == 0) {
                    //Toast.makeText(activity, R.string.file_namecannotempty, Toast.LENGTH_SHORT)
                    //		.show();
                    new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("文件名不能为空，请重试.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                    return;
                }
                String fullFileName = path + "/" + newName;

                File newFile = new File(fullFileName);
                if (newFile.exists()) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("文件资源管理器").setMessage("文件已存在，请检查并重试.").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                } else {
                    try {
                        if (mRenameFile.renameTo(newFile)) {
                            Toast.makeText(MainActivity.this, "重命名成功", Toast.LENGTH_SHORT).show();
                            open(mCurrentPathFile, false);//刷新
                        } else {
                            Toast.makeText(MainActivity.this, "重命名失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        //Toast.makeText(activity, ex.getLocalizedMessage(), Toast.LENGTH_SHORT)
                        //		.show();
                    }
                }
            }

        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return mrenamedialog.create();
    }

    //對已同步的文件夾右鍵菜單
    public class MenuDialog3 extends Dialog implements View.OnClickListener {
        private TextView opendir;
        private TextView desync;
        private TextView detail;
        private int position;

        public MenuDialog3(Context context) {
            super(context);
        }

        public MenuDialog3(Context context, int themeResId) {
            super(context, themeResId);
        }

        protected MenuDialog3(Context context, boolean cancelable, OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        public void setPosition(int pos) {
            position = pos;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.right_cloud_dialog_sync);

            opendir = (TextView) findViewById(R.id.open_dir);
            desync = (TextView) findViewById(R.id.desync);
            detail = (TextView) findViewById(R.id.detail);

            opendir.setOnClickListener(this);
            desync.setOnClickListener(this);
            detail.setOnClickListener(this);
        }

        public void showDialog(int x, int y, int height, int width) {
            show();

            Window dialogWindow = getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);

            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用

            if (x > (d.getWidth() - 200))
                lp.x = d.getWidth() - 200;
            else
                lp.x = x; // 新位置X坐标
            if (y > (d.getHeight() - 171))
                lp.y = d.getHeight() - 171;
            else
                lp.y = y - 10;
            // 新位置Y坐标
            lp.width = width; // 宽度
            lp.height = height; // 高度
            lp.alpha = 0.9f; // 透明度


            dialogWindow.setAttributes(lp);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.open_dir:
                    Log.e("open_dir", "1");
                    dismiss();
                    mforwardfiles.clear();
                    showGridOrView(madapter.getViewMode());
                    open(new File(mCloudFileAdapter.getItem(position).path), true);
                    break;
                case R.id.desync://解除同步
                    dismiss();
                    //调用解除同步的命令
                    //  mCloudFiles.get(position) 就是此时点击的文件
                    MyTool.exec("seaf-cli desync -d "+mCloudFiles.get(position).path);
                    initCloudFile();//刷新

                    break;
                case R.id.detail:
                    Log.e("detail", "1");
                    dismiss();
                    getFileAttributions(position);
                    break;
            }
        }
    }

    //對沒有同步的文件夾右鍵菜單
    public class MenuDialog4 extends Dialog implements View.OnClickListener {
        private TextView download;
        private TextView surfaceOnline;
        private TextView detail;
        private int position;

        public MenuDialog4(Context context) {
            super(context);
        }

        public MenuDialog4(Context context, int themeResId) {
            super(context, themeResId);
        }

        protected MenuDialog4(Context context, boolean cancelable, OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        public void setPosition(int pos) {
            position = pos;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.right_cloud_dialog_unsync);

            download = (TextView) findViewById(R.id.download);
            surfaceOnline = (TextView) findViewById(R.id.surface_online);
            detail = (TextView) findViewById(R.id.detail);

            download.setOnClickListener(this);
            surfaceOnline.setOnClickListener(this);
            detail.setOnClickListener(this);
        }

        public void showDialog(int x, int y, int height, int width) {
            show();

            Window dialogWindow = getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);

            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用

            if (x > (d.getWidth() - 200))
                lp.x = d.getWidth() - 200;
            else
                lp.x = x; // 新位置X坐标
            if (y > (d.getHeight() - 171))
                lp.y = d.getHeight() - 171;
            else
                lp.y = y - 10;
            // 新位置Y坐标
            lp.width = width; // 宽度
            lp.height = height; // 高度
            lp.alpha = 0.9f; // 透明度


            dialogWindow.setAttributes(lp);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.download:
                    Log.e("download", "1");
                    dismiss();
                    //下载并且同步文件文件，默认路径 homePath
                    MyTool.exec("seaf-cli download -l "+mCloudFiles.get(position).id+" -s  https://dev.openthos.org/ -d "+homePath+"  -u 1799858922@qq.com -p 279716");
                    initCloudFile();//刷新
                    break;
                case R.id.surface_online://上網去看看
                    Log.e("surface_online", "1");
                    dismiss();
                    //调用默认浏览器打开指定Url
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse("https://dev.openthos.org/");
                    intent.setData(content_url);
                    startActivity(intent);

                    break;
                case R.id.detail:
                    Log.e("detail", "1");
                    dismiss();

                    break;
            }
        }
    }

    //获取属性的dialog
    public Dialog getFileAttributions(int pos) {
        File mDetailFile = madapter.getItem(pos);
        AlertDialog.Builder mdetaildialog = new AlertDialog.Builder(this);
        View detaillayout = LayoutInflater.from(this).inflate(R.layout.file_info, null);

        ((TextView) detaillayout.findViewById(R.id.file_name)).setText(mDetailFile.getName());
        ((TextView) detaillayout.findViewById(R.id.file_lastmodified)).setText(MyTool.getFileTime(mDetailFile.lastModified()));
        String filesize = "";
        //判断是文件还是文件夹
        if (mDetailFile.isDirectory()) {
            filesize = MyTool.getFileSize(MyTool.getFileSizes(mDetailFile));
        } else filesize = MyTool.getFileSize(mDetailFile.length());

        ((TextView) detaillayout.findViewById(R.id.file_size))
                .setText(filesize);
        ((TextView) detaillayout.findViewById(R.id.file_path))
                .setText(mDetailFile.getAbsolutePath().replace(homePath, mMyDiskName));

        mdetaildialog.setTitle("属性");
        mdetaildialog.setView(detaillayout);
        mdetaildialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialoginterface.cancel();
            }
        });
        return mdetaildialog.create();
    }

    //文件夾路徑選擇器的對話框
    public class DialogPathSelector extends Dialog {
        private List<String> items = null;
        private List<String> paths = null;
        private String rootPath = "/sdcard";
        private TextView mPath;
        private ListView list;
        private FileAdapter m_FileAdapter;

        private void getFileDir(String filePath) {
            File f = new File(filePath);
            if (f.exists() && f.canWrite()) {
                mPath.setText(filePath);
                items = new ArrayList<String>();
                paths = new ArrayList<String>();
                File[] files = f.listFiles();
                if (!filePath.equals(rootPath)) {
                    items.add("goroot");
                    paths.add(rootPath);
                    items.add("goparent");
                    paths.add(f.getParent());
                }
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        items.add(file.getName());
                        paths.add(file.getPath());
                    }
                }
                m_FileAdapter = new FileAdapter(MainActivity.this, items, paths);
                list.setAdapter(m_FileAdapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        if (items.get(position).toString().equals("goparent")) {
                            getFileDir(paths.get(position));
                        } else if (items.get(position).toString().equals("goroot")) {
                            getFileDir(paths.get(position));
                            return;
                        } else {
                            File file = new File(paths.get(position));
                            if (file.canWrite()) {
                                if (file.isDirectory()) {
                                    getFileDir(paths.get(position));
                                }
                            }
                        }
                    }
                });
            }
        }

        public DialogPathSelector(Context context) {
            super(context);
        }

        public DialogPathSelector(Context context, int themeResId) {
            super(context, themeResId);
        }

        protected DialogPathSelector(Context context, boolean cancelable, OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.filelist);
            mPath = (TextView) this.findViewById(R.id.mPath);
            list = (ListView) this.findViewById(R.id.filelist);
            getFileDir(rootPath);
            //mPath.setTextColor(this.getResources().getColor(R.color.text_color));
            this.setTitle("请选择同步文件夾:");
            Button ok = (Button) this.findViewById(R.id.fileok);
            ok.setPadding(0, 5, 0, 5);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    Toast.makeText(MainActivity.this, mPath.getText().toString(), Toast.LENGTH_SHORT).show();
                    //mPath.getText().toString()是获取的文件夹的路径，在这里调用命令去同步该文件夹
                    File file = new File(mPath.getText().toString());
                    String id=MyTool.exec("seaf-cli create -n "+file.getName()+" -s https://dev.openthos.org/ -u 1799858922@qq.com -p 279716");
                    MyTool.exec("seaf-cli sync -l "+id+" -s  https://dev.openthos.org/ -d "+file.getAbsolutePath()+"  -u 1799858922@qq.com -p 279716");
                    initCloudFile();//刷新
                }
            });
            Button cancel = (Button) this.findViewById(R.id.filecancel);
            cancel.setPadding(0, 5, 0, 5);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        }

        public void showDialog(int height, int width) {
            show();
            Window dialogWindow = getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = width; // 宽度
            lp.height = height; // 高度
            lp.alpha = 0.9f; // 透明度
            dialogWindow.setAttributes(lp);
        }
    }

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

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public boolean copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024*5];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                    mCopySize += byteread;
                    sendMsg(1);//更新进度条
                }
                inStream.close();
            }
            return true;
        } catch (Exception e) {
            sendMsg(3);
            return false;
        }
    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：/fqf
     * @param newPath String 复制后路径 如：/fqf/ff
     * @return boolean
     */
    public boolean copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;

            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024*5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                        mCopySize += len;
                        sendMsg(1);//发送信息,更新进度条
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            return false;
        }
    }

    public void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }

    public class UsbStatesReceiver extends BroadcastReceiver {
        MainActivity execactivity;

        public static final int USB_STATE_MSG = 0x00020;
        public static final int USB_STATE_ON = 0x00021;
        public static final int USB_STATE_OFF = 0x00022;
        public IntentFilter filter = new IntentFilter();

        public UsbStatesReceiver(Context context) {
            execactivity = (MainActivity) context;
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);

            filter.addDataScheme("file");
        }

        public Intent registerReceiver() {
            return execactivity.registerReceiver(this, filter);
        }

        public void unregisterReceiver() {
            execactivity.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) ||
                    intent.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {
                sendMsg(USB_STATE_ON);
            } else {
                sendMsg(USB_STATE_OFF);
            }
        }

    }
}

