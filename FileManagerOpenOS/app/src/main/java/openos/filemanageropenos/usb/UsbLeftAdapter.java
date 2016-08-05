package openos.filemanageropenos.usb;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import openos.filemanageropenos.MainActivity;
import openos.filemanageropenos.R;
import openos.filemanageropenos.tools.MyTool;

/**
 * Created by zhu on 2016/7/18.
 */
public class UsbLeftAdapter extends BaseAdapter {
    private List<File> mfiles = null;
    private Context mcontext = null;
    public UsbLeftAdapter(Context context,List<File> files) {
        mfiles = files;
        mcontext = context;
    }
    @Override
    public int getCount() {
        return mfiles.size();
    }

    @Override
    public File getItem(int i) {
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
            convertView= LayoutInflater.from(mcontext).inflate(R.layout.usb_left_list_item, null);
            mListHolder=new ListHolder();
            mListHolder.button=(Button) convertView.findViewById(R.id.button);
            mListHolder.mfileName=(TextView) convertView.findViewById(R.id.name);
            convertView.setTag(mListHolder);
        }else
        {
            mListHolder=(ListHolder) convertView.getTag();
        }

        //update the holder
        File f = this.getItem(position);
        if(f != null){
            mListHolder.mfileName.setText(f.getName());
        }

        mListHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyTool.exec("umount "+getItem(position).getAbsolutePath());
                Toast.makeText(mcontext, "U盘已弹出",Toast.LENGTH_SHORT).show();
                ((MainActivity)mcontext).sendMsg(0x00022);
            }
        });

        return convertView;
    }

    static class ListHolder{
        Button button;
        TextView mfileName;
    }
}
