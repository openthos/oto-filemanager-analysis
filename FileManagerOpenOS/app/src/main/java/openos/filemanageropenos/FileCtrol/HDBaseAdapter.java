/*
 * Copyright (c) 2011 yang hui <yanghui1986527@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License v2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 021110-1307, USA.
 */
package openos.filemanageropenos.FileCtrol;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import openos.filemanageropenos.MainActivity;
import openos.filemanageropenos.R;
import openos.filemanageropenos.tools.MyTool;
public class HDBaseAdapter extends BaseAdapter {
	// 列表模式：详细信息列表，文件列表不显示具体详细，grid列表
	public static final int VIEWMODE_LIST_ll = 0;
	public static final int VIEWMODE_GRID = 2;

	private int mViewMode = VIEWMODE_GRID;

	private Context mcontext = null;
	private List<File> mfiles = null;
	private Map<Integer,Boolean> isCheckedMap;


	public HDBaseAdapter(Context context,List<File> files,Map<Integer,Boolean> isCheckedMap) {
		mfiles = files;
		mcontext = context;
		this.isCheckedMap = isCheckedMap;
	}
//	public void setFiles(List<File> files){
//		if (mfiles!=null&&mfiles.size()>0)
//			this.mfiles.clear();
//		this.mfiles = files;
//	}

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
		ListHolder mListHolder = null;
		GridHolder mGridHolder = null;

		switch (mViewMode) {
		case VIEWMODE_LIST_ll:
		{
			//Log.e("VIEWMODE_LIST_ll","size ="+getCount());
			//Log.e("VIEWMODE_LIST_ll","position ="+position);
			if(convertView==null)
			{
				convertView= LayoutInflater.from(mcontext).inflate(R.layout.list_file_item, null);
				mListHolder=new ListHolder();
				mListHolder.mfileIcon=(ImageView) convertView.findViewById(R.id.icon);
				mListHolder.mfileType=(TextView) convertView.findViewById(R.id.type);
				mListHolder.mfileName=(TextView) convertView.findViewById(R.id.name);
				mListHolder.mfileSize=(TextView) convertView.findViewById(R.id.size);
				mListHolder.mfileTime=(TextView) convertView.findViewById(R.id.time);
				convertView.setTag(mListHolder);
			}else
			{
				mListHolder=(ListHolder) convertView.getTag();
			}

			//update the holder
			File f = this.getItem(position);
			if(f != null){
				int icon = this.getFileIcon(f);
				if(icon == -1){
					Drawable drawable = this.getApkIcon(f.getAbsolutePath());
					if(drawable != null){
						mListHolder.mfileIcon.setImageDrawable(drawable);
					}
					else{
						mListHolder.mfileIcon.setImageResource(R.drawable.icon_file);
					}
				}else{
					mListHolder.mfileIcon.setImageResource(icon);
				}
				mListHolder.mfileName.setText(f.getName());
				if(f.isFile()){
					mListHolder.mfileSize.setText(MyTool.getFileSize(f.length()));
					mListHolder.mfileType.setText("文件");
				}else {
					mListHolder.mfileSize.setText("");
					mListHolder.mfileType.setText("文件夹");
				}
				mListHolder.mfileTime.setText(MyTool.getFileTime(f.lastModified()));
			}
			//Log.e("VIEWMODE_LIST_ll","@@@@@@");
			//设置右键监听事件，对文件名称这一栏监听
			((LinearLayout)convertView.findViewById(R.id.img_linear)).setOnGenericMotionListener(new View.OnGenericMotionListener() {
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
//			//设置右键监听事件,对名称以外的地方进行监听
//			convertView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
//				@Override
//				public boolean onGenericMotion(View view, MotionEvent motionEvent) {
//					if (motionEvent.getButtonState()==MotionEvent.BUTTON_SECONDARY){
//						view.setSelected(false);
//						MenuDialog2 menuDialog2 = new MenuDialog2(mcontext,R.style.dialog);//设置style
//						menuDialog2.showDialog((int)motionEvent.getRawX(),(int)motionEvent.getRawY(),285,200);
//						return true;
//					}
//					return false;
//				}
//			});
		}
		break;
		case VIEWMODE_GRID:
		{
			//Log.e("VIEWMODE_GRID,size","="+getCount());
			//Log.e("VIEWMODE_GRID,position","="+position);
			if(convertView==null)
			{
				convertView= LayoutInflater.from(mcontext).inflate(R.layout.item_gridview, null);
				mGridHolder=new GridHolder();
				mGridHolder.mfileIcon=(ImageView) convertView.findViewById(R.id.gridview_fileicon);
				mGridHolder.mfileName=(TextView) convertView.findViewById(R.id.gridview_filename);
				convertView.setTag(mGridHolder);
			}else
			{
				mGridHolder=(GridHolder) convertView.getTag();
			}

			//update the holder
			File f = this.getItem(position);
			if(f != null){
				int icon = this.getFileIcon(f);
				if(icon == -1){
					Drawable drawable = this.getApkIcon(f.getAbsolutePath());
					if(drawable != null){
						mGridHolder.mfileIcon.setImageDrawable(drawable);
					}
					else{
						mGridHolder.mfileIcon.setImageResource(R.drawable.icon_file);
					}
				}else{
					mGridHolder.mfileIcon.setImageResource(icon);
				}
				mGridHolder.mfileName.setText(f.getName());
			}

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
		}
		break;
		default:
			break;
		}
		if (isCheckedMap.get(position))
			convertView.setBackgroundResource(R.color.litter_blue);
		else convertView.setBackgroundResource(R.color.white);
//		convertView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				//测试dialog1
//				MainActivity.menuDialog1.setPosition(position);
//				MainActivity.menuDialog1.showDialog(300,200,600,300);
//				if (getItem(position).isDirectory())
//					MainActivity.menuDialog1.setEnableOpenwith(false);
//				else
//					MainActivity.menuDialog1.setEnableOpenwith(true);
//
//				//测试dialog2
////				MainActivity.menuDialog2.showDialog(300,200,800,600);
////				MainActivity.menuDialog2.setEnablePaste(true);
//			}
//		});
//		convertView.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View view, MotionEvent motionEvent) {
//				MainActivity.menuDialog2.showDialog((int)motionEvent.getRawX(),(int)motionEvent.getRawY(),800,600);
//				MainActivity.menuDialog2.setEnablePaste(true);
//				return false;
//			}
//		});

		return convertView;
	}

	public int getFileIcon(File f) {
		int icon = 0;

		String str = Constant.getFileIcon(f);
		if(str == null)
		{
			icon = -1;
		}
		else{
			Resources res = mcontext.getResources();
			icon =res.getIdentifier(str,"drawable",mcontext.getPackageName());  

			if(icon <= 0 )
				icon = R.drawable.icon_file;
		}

		return icon;
	}
	private void showMenuDialog1(int position,MotionEvent motionEvent){
		SharedPreferences sharedPreferences = mcontext.getSharedPreferences("isfileordir", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isclickfile",true);
		editor.commit();//提交修改

		MainActivity.menuDialog1.setPosition(position);
		//获取多窗口的右顶点坐标
		int[] location = new int[2];
		((MainActivity)mcontext).mBackBtn.getLocationOnScreen(location);
		MainActivity.menuDialog1.showDialog((int)motionEvent.getRawX()-location[0],(int)motionEvent.getRawY()-location[1]+57,400,200);
		if (getItem(position).isDirectory())
			MainActivity.menuDialog1.setEnableOpenwith(false);
		else
			MainActivity.menuDialog1.setEnableOpenwith(true);
	}

	public Drawable getApkIcon(String path){
		PackageManager pm = mcontext.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
		if(info != null){      
			ApplicationInfo appInfo = info.applicationInfo;

			if(Build.VERSION.SDK_INT >= 8){
				appInfo.sourceDir = path;
				appInfo.publicSourceDir = path;
			}

			return appInfo.loadIcon(pm);
		}   		
		return null;
	}

	public int getViewMode()	{
		return mViewMode;
	}

	public void setViewMode(int ViewMode){
		mViewMode = ViewMode;
	}

	static class ListHolder{
		ImageView mfileIcon;
		TextView mfileName;
		TextView mfileSize;
		TextView mfileType;
		TextView mfileTime;
	}

	static class GridHolder{
		ImageView mfileIcon;
		TextView mfileName;
	}



}

