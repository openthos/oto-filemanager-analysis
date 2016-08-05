package openos.filemanageropenos.cloudservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import openos.filemanageropenos.R;

//路径选择器 DialogPathSelector 的 adpater
public class FileAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private Bitmap mIcon1;
    private List<String> items;
    private List<String> paths;


    public FileAdapter(Context context, List<String> it, List<String> pa){
        mInflater = LayoutInflater.from(context);
        items = it;
        paths = pa;
        mIcon1 = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.icon_folder);
    }

    @Override
    public int getCount(){
        return items.size();
    }

    @Override
    public Object getItem(int position){
        return items.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_file_item, null);
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.name);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        File f=new File(paths.get(position).toString());
        if(items.get(position).toString().equals("goroot")){
            holder.text.setText("返回根目录");
            holder.icon.setImageBitmap(mIcon1);
        }else if(items.get(position).toString().equals("goparent")){
            holder.text.setText("返回上一级");
            holder.icon.setImageBitmap(mIcon1);
        }else{
            holder.text.setText(f.getName());
            holder.icon.setImageBitmap(mIcon1);
        }
        return convertView;
    }

    public final class ViewHolder
    {
        public TextView text;
        public ImageView icon;
    }
}
