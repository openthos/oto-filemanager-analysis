package openos.filemanageropenos.tools;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhu on 2016/7/17.
 */
public class MyTool {
    //不要root权限运行的函数，此处只针对df命令
    public static ArrayList<String> exec2(String[] args) {
        ArrayList<String> result = new ArrayList<String>();
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream inIs = null;
        try {
            process = processBuilder.start();
            inIs = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inIs);
            BufferedReader buff= new BufferedReader(inputStreamReader);
            String line=null;
            while ((line = buff.readLine()) != null) {
                Log.e("line:",line);
                //获取usb的或者sd卡的路径
                if (line.startsWith("/storage/usb")||line.startsWith("/storage/sdcard")){
                    String []strs = line.split("\\s+");
                    result.add(strs[0]);
                }
            }
            buff.close();
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
//    //不要root权限运行的函数
//    public static String exec(String[] args) {
//        String result = "";
//        ProcessBuilder processBuilder = new ProcessBuilder(args);
//        Process process = null;
//        InputStream errIs = null;
//        InputStream inIs = null;
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            int read = -1;
//            process = processBuilder.start();
//            errIs = process.getErrorStream();
//            while ((read = errIs.read()) != -1) {
//                baos.write(read);
//            }
//            baos.write('\n');
//            inIs = process.getInputStream();
//            while ((read = inIs.read()) != -1) {
//                baos.write(read);
//            }
//            byte[] data = baos.toByteArray();
//            result = new String(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (errIs != null) {
//                    errIs.close();
//                }
//                if (inIs != null) {
//                    inIs.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (process != null) {
//                process.destroy();
//            }
//        }
//        return result;
//    }


    public static String exec(String cmd) {
        try {
            if (cmd != null) {
                Runtime rt = Runtime.getRuntime();
                Process process = rt.exec("su");//Root权限   //Process process = rt.exec("sh");//模拟器测试权限
                DataOutputStream dos = new DataOutputStream(process.getOutputStream());
                dos.writeBytes(cmd + "\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                InputStream myin = process.getInputStream();
                InputStreamReader is = new InputStreamReader(myin);
                char[] buffer = new char[1024];
                int bytes_read = is.read(buffer);
                StringBuffer aOutputBuffer = new StringBuffer();
                while (bytes_read > 0) {
                    aOutputBuffer.append(buffer, 0, bytes_read);
                    bytes_read = is.read(buffer);
                }
                Log.e("exec :", aOutputBuffer.toString());
                return aOutputBuffer.toString();
            } else {
                return "Please input true command!";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "operater err!";
        }
    }
    /**
     * 递归查找文件
     * @param baseDirName  查找的文件夹路径
     * @param targetFileName  需要查找的文件名
     * @param fileList  查找到的文件集合
     */
    public static void findFiles(String baseDirName, String targetFileName, List<File> fileList) {
        File baseDir = new File(baseDirName);       // 创建一个File对象
        if (!baseDir.exists() || !baseDir.isDirectory()) {  // 判断目录是否存在
            Log.e("findfiles","文件查找失败：" + baseDirName + "不是一个目录！");
        }
        File[] files = baseDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().toLowerCase().contains(targetFileName.toLowerCase()))
                fileList.add(files[i]);
            if(files[i].isDirectory()){
                findFiles(files[i].getAbsolutePath(), targetFileName, fileList);
            }
        }
    }

    /**
     * 获取指定文件夹的大小
     * @param f
     * @return
     * @throws Exception
     */
    public static long getFileSizes(File f)
    {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++){
            if (flist[i].isDirectory()){
                size = size + getFileSizes(flist[i]);
            }
            else{
                size =size + flist[i].length();
            }
        }
        return size;
    }
    public static String getFileSize(long filesize) {
        DecimalFormat df = new DecimalFormat("#.00");
        StringBuffer mstrbuf = new StringBuffer();

        if (filesize < 1024) {
            mstrbuf.append(filesize);
            mstrbuf.append(" B");
        } else if (filesize < 1048576) {
            mstrbuf.append(df.format((double)filesize / 1024));
            mstrbuf.append(" K");
        } else if (filesize < 1073741824) {
            mstrbuf.append(df.format((double)filesize / 1048576));
            mstrbuf.append(" M");
        } else {
            mstrbuf.append(df.format((double)filesize / 1073741824));
            mstrbuf.append(" G");
        }

        df = null;

        return mstrbuf.toString();
    }


    /**
     * 删除文件或文件夹
     *
     * @param path
     *            待删除的文件的绝对路径
     * @return boolean
     */
    public static boolean deleteGeneralFile(String path) {
        boolean flag = false;

        File file = new File(path);
        if (!file.exists()) { // 文件不存在
            System.out.println("要删除的文件不存在！");
        }

        if (file.isDirectory()) { // 如果是目录，则单独处理
            flag = deleteDirectory(file.getAbsolutePath());
        } else if (file.isFile()) {
            flag = deleteFile(file);
        }
        return flag;
    }

    /**
     * 删除文件
     *
     * @param file
     * @return boolean
     */
    private static boolean deleteFile(File file) {
        return file.delete();
    }

    /**
     * 删除目录及其下面的所有子文件和子文件夹，注意一个目录下如果还有其他文件或文件夹
     * 则直接调用delete方法是不行的，必须待其子文件和子文件夹完全删除了才能够调用delete
     *
     * @param path
     *            path为该目录的路径
     */
    private static boolean deleteDirectory(String path) {
        boolean flag = true;
        File dirFile = new File(path);
        if (!dirFile.isDirectory()) {
            return flag;
        }
        File[] files = dirFile.listFiles();
        for (File file : files) { // 删除该文件夹下的文件和文件夹
            // Delete file.
            if (file.isFile()) {
                flag = deleteFile(file);
            } else if (file.isDirectory()) {// Delete folder
                flag = deleteDirectory(file.getAbsolutePath());
            }
            if (!flag) { // 只要有一个失败就立刻不再继续
                break;
            }
        }
        flag = dirFile.delete(); // 删除空目录
        return flag;
    }
    //新建文件夹
    public static boolean mkdir(String path){
        String name ="新建文件夹";
        File file = new File(path+"/"+name);
        try {
            if (!file.exists()){
                file.mkdirs();
            }else {
                int i=1;
                do{
                    i++;
                    file = new File(path+"/"+name+"("+i+")");
                }while (!file.mkdirs());
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }
    //新建文件夹
    public static boolean createNewFile(String path){
        String name ="新建文件";
        File file = new File(path+"/"+name);
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }else {
            int i=1;
            try {
                do{
                    i++;
                    file = new File(path+"/"+name+"("+i+")");
                }while (!file.createNewFile());
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static String getFileTime(long filetime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String ftime =  formatter.format(new Date(filetime));
        return ftime;
    }
}
