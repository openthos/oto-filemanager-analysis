package openos.filemanageropenos.cloudservice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import openos.filemanageropenos.MainActivity;
import openos.filemanageropenos.R;

/**
 * Created by zhu on 2016/7/25.
 */
public class CloudFileAdapter extends BaseAdapter {
    //对应的三个状态，分别是已同步，没有同步，和加号
    public final int STATUS_SYNCHRONIZED = 1;
    public final int STATUS_UNSYNCHRONIZED = 2;
    public final int STATUS_ADD = -1;

    private List<SeafileInfo> mfiles = null;
    private Context mcontext = null;
    private String mSelectPath;

    public CloudFileAdapter(Context context,List<SeafileInfo> files) {
        mfiles = files;
        mcontext = context;
    }
    @Override
    public int getCount() {
        return mfiles.size();
    }

    @Override
    public SeafileInfo getItem(int i) {
        return mfiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public void notifyDataSetChanged() {

        super.notifyDataSetChanged();

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ListHolder mListHolder = null;
        if(convertView==null)
        {
            convertView= LayoutInflater.from(mcontext).inflate(R.layout.item_gridview, null);
            mListHolder=new ListHolder();
            mListHolder.icon=(ImageView) convertView.findViewById(R.id.gridview_fileicon);
            mListHolder.mfileName=(TextView) convertView.findViewById(R.id.gridview_filename);
            convertView.setTag(mListHolder);
        }else
        {
            mListHolder=(ListHolder) convertView.getTag();
        }
        Log.e("CloudfileAdpater",""+getCount());
        //update the holder
        SeafileInfo f = this.getItem(position);
        if(f != null){
            mListHolder.mfileName.setText(f.name);
        }
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
        }else if (f.status==STATUS_ADD){
            mListHolder.icon.setImageResource(R.drawable.adddir);
        }
        return convertView;
    }

    static class ListHolder{
        ImageView icon;
        TextView mfileName;
    }

}
