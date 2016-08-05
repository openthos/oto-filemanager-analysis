package openos.filemanageropenos.usb;

import android.content.Context;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;
import java.util.List;
import openos.filemanageropenos.R;
import openos.filemanageropenos.tools.MyTool;

/**
 * Created by zhu on 2016/7/18.
 */
public class UsbDiskAdapter extends BaseAdapter{
    private Context mcontext = null;
    private List<File> mfiles = null;
    private ListHolder mListHolder = null;


    public UsbDiskAdapter(Context context,List<File> files) {
        mfiles = files;
        mcontext = context;
    }
    @Override
    public int getCount() {
        int msize = 0;

        if(mfiles != null)
            msize = mfiles.size();

        return msize;
    }

    @Override
    public File getItem(int position) {

        if((position >= 0) && (position < this.getCount()))
            return mfiles.get(position);

        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }


    @Override
    public void notifyDataSetChanged() {

        super.notifyDataSetChanged();

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //Log.e("VIEWMODE_LIST_ll","size ="+getCount());
        //Log.e("VIEWMODE_LIST_ll","position ="+position);
        if(convertView==null)
        {
            convertView= LayoutInflater.from(mcontext).inflate(R.layout.disk_item_gridview, null);
            mListHolder=new ListHolder();
            mListHolder.mfileIcon=(ImageView) convertView.findViewById(R.id.icon);
            mListHolder.mfileName=(TextView) convertView.findViewById(R.id.name);
            mListHolder.mfileSize=(TextView) convertView.findViewById(R.id.info);
            mListHolder.progressBar=(ProgressBar) convertView.findViewById(R.id.proessbar);
            convertView.setTag(mListHolder);
        }else
        {
            mListHolder=(ListHolder) convertView.getTag();
        }

        //update the holder
        File f = this.getItem(position);
        if(f != null){
            mListHolder.mfileIcon.setImageResource(R.drawable.ic_file_manager_usb_services);
            mListHolder.mfileName.setText(f.getName());
           show_USB_storage(f);
        }

        //convertView.setLayoutParams(new GridView.LayoutParams(mWindowWidth / 3, mWidowWidth / 3));

        return convertView;
    }

    private void show_USB_storage(File file) {
        String path = file.getAbsolutePath();
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availableBlocks = statFs.getAvailableBlocks();
        long usedBlocks = totalBlocks - availableBlocks;
        mListHolder.progressBar.setProgress((int) (usedBlocks * 100 / totalBlocks));
        Log.e("percent", "" + (int) (usedBlocks * 100 / totalBlocks));
        mListHolder.mfileSize.setText(MyTool.getFileSize(availableBlocks * blockSize) + "可用,共有" + MyTool.getFileSize(totalBlocks * blockSize));
    }

    static class ListHolder{
        ImageView mfileIcon;
        TextView mfileName;
        TextView mfileSize;
        ProgressBar progressBar;
    }
}
