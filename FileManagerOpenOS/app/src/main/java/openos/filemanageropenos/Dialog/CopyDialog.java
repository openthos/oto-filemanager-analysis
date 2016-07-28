package openos.filemanageropenos.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import openos.filemanageropenos.R;

/**
 * Created by zhu on 2016/7/17.
 */

public class CopyDialog extends Dialog{
    private TextView title;
    private TextView pathdetail;
    private TextView percent;
    private TextView filesize;
    private ProgressBar progressBar;

    public CopyDialog(Context context) {
        super(context);
    }
    public CopyDialog(Context context, int themeResId) {
        super(context, themeResId);
    }
    protected CopyDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.copydialog_layout);

        title = (TextView) findViewById(R.id.title);
        pathdetail = (TextView) findViewById(R.id.path_detail);
        percent = (TextView) findViewById(R.id.percent);
        filesize = (TextView) findViewById(R.id.size);
        progressBar = (ProgressBar) findViewById(R.id.progressbar_percent);

    }

    public void showDialog() {
        show();
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = 700; // 宽度
        lp.height = 400; // 高度
        lp.alpha = 0.9f; // 透明度
        dialogWindow.setAttributes(lp);
    }
    public void setInfo(String title,String path,String filesize,int barpercent){
        this.title.setText(title);
        this.pathdetail.setText(path);
        this.filesize.setText(filesize);
        this.percent.setText(barpercent+"%");
        progressBar.setProgress(barpercent);
    }

    public void setTitle(String title) {
        this.title.setText(title); ;
    }

    public void setPathdetail(String pathdetail) {
        this.pathdetail.setText(pathdetail);
    }

    public void setPercent(String percent) {
        this.percent.setText(percent);
    }

    public void setFilesize(String filesize) {
        this.filesize .setText(filesize);
    }

    public void setProgressBar(int progressBar) {
        this.progressBar.setProgress(progressBar);
    }
}