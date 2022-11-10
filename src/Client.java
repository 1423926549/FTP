import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author 何佳辉
 * @title 客户端
 * @description
 * @date 2022/11/8
 **/
public class Client {

    // 界面组件参数
    @FXML
    private Pane connectPage;
    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private Pane mainPage;
    @FXML
    private ListView<RadioButton> fileList;


    //通讯套接字
    public static Socket socket = null;
    //通讯的输入输出流
    public static BufferedReader bufferedReader = null;
    public static BufferedWriter bufferedWriter = null;
    //服务端的IP
    public static String serviceIP;
    // 连接端口
    public static int port;
    // 传输数据的套接字
    public ServerSocket fileSocket;
    // 文件上传服务器连接
    public Socket uploadFileSocket;

    /**
     * 连接到服务器
     */
    public void connectServer() {
        // 将输入的IP和端口给赋值
        serviceIP = ipField.getText();
        port = Integer.parseInt(portField.getText());
        try {
            socket = new Socket(serviceIP, port);
            System.out.println("连接成功");
            // 切换到主界面
            connectPage.setVisible(false);
            mainPage.setVisible(true);

            showList();

        } catch (Exception e) {
            System.out.println("连接失败!!!");
        }
    }

    /**
     * 文件上传
     */
    public void uploadFile() throws IOException {
        try {
            // 打开文件管理器，选择需要上传的文件
            Stage stage = new Stage();  // 创建一个文件选择界面
            //创建文件选择器
            FileChooser fileChooser = new FileChooser();
            // 选择哪些文件
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(stage);
            System.out.println("文件的上传路径为：" + file.getPath());

            //用于传输数据的套接字
            //System.out.println("新建的套接字连接");
            fileSocket = new ServerSocket(78);
            fileSocket.setSoTimeout(30000);  // 连接超时30秒
            System.out.println("开始连接");

            //通信Socket输入输出流
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // 发送请求
            System.out.println("文件上传路径为：" + "D:/FTP/" + file.getName());
            request(bufferedWriter, "UPLOAD," + "D:/FTP/" + file.getName());
            //等待服务器连接
            uploadFileSocket = fileSocket.accept();
            try {
                //文件输入流
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                //Socket输出流
                //考虑使用新的Socket创建连接实现文件的传输
                BufferedOutputStream outputStream = new BufferedOutputStream(uploadFileSocket.getOutputStream());

                byte[] bytes = new byte[inputStream.available()];
                int i = 0;
                while ((i = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, i);
                }
                //一次发送完所有数据
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (RuntimeException re) {
            System.out.println("取消上传。");
        } finally {
            if (fileSocket != null) fileSocket.close();
            if (uploadFileSocket != null) uploadFileSocket.close();
        }
    }

    public void downloadFile() {
        try {
            Stage stage = new Stage();
            // 创建目录选择器
            DirectoryChooser directoryChooser = new DirectoryChooser();
            // 选择下载到的目录
            File dir = directoryChooser.showDialog(stage);
            System.out.println(dir.getPath());

            // 下载文件
            //拼接文件的下载地址
            String fileName = "";
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //下载命令的发送
            request(bufferedWriter , "DOWNLOAD," + fileName);
            //合法文件 连接对应的接口
            Socket  socketDownload = new Socket(serviceIP , 78);
            //数据传输时间不得超过一分钟
            socketDownload.setSoTimeout(60000);
            //System.out.println(bufferedReader.readLine());
            //默认存储路径
            String storePath = "D:" + File.separator + "FTPDOWNLOAD" + File.separator +  fileName;
            System.out.println("存储路径为：" + storePath);
        } catch (Exception e) {
            System.out.println("取消上传");
        }

    }

    /**
     * 展示可下载文件列表
     * @throws IOException
     */
    public void showList() throws IOException {
        // 获取Socket的输入输出流
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // 向服务器发送一个查询列表信息
        request(bufferedWriter, "LIST");
        // 接收服务器返回的列表信息  列表形式的字符串
        String response = bufferedReader.readLine();
        System.out.println("resp: " + response);

        // 处理接收的信息
        // 去掉左右两边括号
        response = response.replace("[" , "");
        response = response.replace("]" , "");
        response.trim();
        // 将每一个文件名字给分割出来
        String[] resStr = response.split(",");
        // 用于存储文件列表的单选框列表
        ObservableList<RadioButton> items = FXCollections.observableArrayList();
        ToggleGroup toggleGroup = new ToggleGroup();  //单选组

        for (String fileName : resStr) {
            //System.out.println(fileName);
            RadioButton radioButton = new RadioButton(fileName);
            radioButton.setToggleGroup(toggleGroup);  // 加入同一个单选泽，否则会有多选效果
            items.add(radioButton);
        }

        fileList.setItems(items);
    }

    /**
     * 传输数据给服务端
     * @param writer
     * @param str
     * @throws IOException
     */
    public void request(BufferedWriter writer, String str) throws IOException {
        //System.out.println(str);
        writer.write(str);
        writer.newLine();
        //将数据发送
        writer.flush();
    }

    /**
     * 退出主界面
     */
    public void exit() throws IOException {
        connectPage.setVisible(true);
        mainPage.setVisible(false);
        socket.close();
    }
}
