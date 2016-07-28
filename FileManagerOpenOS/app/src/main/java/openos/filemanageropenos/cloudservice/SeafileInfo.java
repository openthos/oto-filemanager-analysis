package openos.filemanageropenos.cloudservice;

/**
 * Created by zhu on 2016/7/25.
 */
public class SeafileInfo {
    //对应的三个状态，分别是已同步，没有同步，和加号
    public static final int STATUS_SYNCHRONIZED = 1;
    public static final int STATUS_UNSYNCHRONIZED = 2;
    public static final int STATUS_ADD = -1;

    public String name;
    public String time;
    public int status;
    public String path;
    public SeafileInfo(){
        name = "添加同步文件夹";
        time="1970-01-01";
        status=STATUS_ADD;
        path = "/storage";
    }
}

