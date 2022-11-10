import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 何佳辉
 * @title读取文件
 * @description 读取指定目录下的所有文件
 * @date 2022/11/8
 **/
public class FileHelper {
    //默认文件夹下
    public static String filePath = "D:\\FTP";

    public static List<String> getAllFileInformation(String path) {
        if(path == null)
            path = filePath;
        List<String> list = new ArrayList<>();
        //确保只能在固定文件夹下进行操作
        if (!path.contains(filePath)) {
            System.out.println(path);
            addErrorInformation(list , "访问目录不合法。");
        }
        File file = new File(path);
        if (file.isFile()) {
            addErrorInformation(list , "访问的是文件。请确认。");
        }
        if (!file.exists()) {
            addErrorInformation(list , "目录不存在。请确认。");
        }else {
            list.add("LIST");
            File[] files = file.listFiles();
            for (File f : files) {
                String name = f.getName();//文件名称
                System.out.println("  文件名为：" + name);
                StringBuilder stringBuilder = new StringBuilder();
                if(f.isFile()) {
                    String sizeStr = null;
                    long  size = f.length();
                    if (size/1024 != 0) {
                        sizeStr = String.valueOf(size/1024) + "kb";
                    }else {
                        sizeStr = String.valueOf(size) + "b";
                    }
                    String fileStr = name + sizeStr;
                    stringBuilder.append(name);
                    stringBuilder.append(" ").append(" ");
                    stringBuilder.append(sizeStr);
                    fileStr = stringBuilder.toString();
                    list.add(fileStr);
                }else {
                    //目录
                    list.add(name);
                }
            }
        }
        return list;
    }
    //
    public static void addErrorInformation(List<String> list , String str) {
        if(list == null) {
            return;
        }
        list.add("ERROR");
        list.add(str);
    }
}

