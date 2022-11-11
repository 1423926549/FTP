import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 何佳辉
 * @title
 * @description  处理命令
 * @date 2022/11/8
 **/
public class FtpConnection extends Thread {

    //通讯套接字
    private Socket socket;
    private BufferedReader reader = null;//请求的读取
    private BufferedWriter writer = null;//响应的发送
    private String clientIP;

    public FtpConnection(Socket socket){
        this.socket = socket;
        //客户端ip
        this.clientIP = socket.getInetAddress().getHostAddress();
    }

    public void run() {
        String cmd ;
        try {
            System.out.println(clientIP + " connected! ");
            //读取操作阻塞三秒 设置超时时间为5分钟
            //socket.setSoTimeout(300000);  // 超时将断开连接
            //上传文件的输入、输出流
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while(true){
                System.out.println("监听命令中。。。");
                //在这里被阻塞
                cmd = reader.readLine();
                System.out.println("命令为：" + cmd);
                if(cmd.startsWith("EXIT")) {
                    System.out.println(socket.getInetAddress() + "已断开连接。");
                    break;
                }
                handleCmd(cmd);
            }
        } catch (Exception e) {
            System.out.println(socket.getInetAddress() + "连接断开");
            //e.printStackTrace();
        } finally {
            try {
                if (reader!=null)reader.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                if (writer!=null)writer.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                if(this.socket!=null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //发送信息
    private void response(String s) throws Exception {
        writer.write(s);
        writer.newLine();
        //发送
        writer.flush();
    }

    //命令的处理
    private void handleCmd(String s) throws Exception {
        if(s==null || s.equals(""))
            return;

        if(s.startsWith("LIST")) { // 打印当前目录下所有文件
            System.out.println("准备获取文件列表");
            // 用于储存文件名字
            List<String> list = new ArrayList<>();
            // 获取"D:/FTP"下的所有文件
            File file = new File("D:/FTP");
            File[] files = file.listFiles();
            // 寻找所有文件
            for (File f : files) {
                // 判断是否为文件
                if (f.isFile()) {
                    String name = f.getName();
                    System.out.println("文件名为：" + name);
                    list.add(name);
                }
            }

            response(list.toString());
        }

        else if(s.startsWith("UPLOAD")) {
            //System.out.println("上传文件请求开始...");
            //获取文件参数 构建文件及其输入流
            String[] strings = s.split(",");
            String path = "D:/FTP/" + strings[1];
            //----------这里要进行同名验证 如果文件已存在则需要进行同名验证 ---------
            //连接客户端的IP套接字 默认为78接口传输数据
            Socket fileSocket = new Socket(this.clientIP , 78);
            //数据传输最多阻塞 一分钟
            fileSocket.setSoTimeout(60000);
            System.out.println("已连接到客户端");
            //传输存储数据 执行循环直到文件传输完毕  ------ 文件过大时考虑分批次传输 ------
            try(
                    BufferedInputStream inputStream = new BufferedInputStream(fileSocket.getInputStream());
                    //文件输入流
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(path)))
            ) {
                while (inputStream.available() == 0) {
                    //直到有数据发送过来 一秒执行一次
                    System.out.println("等待传输接口发送数据...");
                    Thread.sleep(1000);
                }

                System.out.println("发现数据传来，开始接收。流大小为：" + inputStream.available());
                int i = 0;
                byte[] bytes = new byte[inputStream.available()];
                while ((i = inputStream.read(bytes)) != -1) {
                    out.write(bytes , 0 , i);
                }
                out.flush();
                System.out.println("上传已完成。");
                response("文件上传成功。");
            }catch (IOException e) {
                if(fileSocket != null) fileSocket.close();
                response("ERROR,上传文件失败，遇到未知错误。");
            }finally {
                //关闭连接返回响应
                if(fileSocket != null) fileSocket.close();
            }

        }

        else if(s.startsWith("DOWNLOAD")) {
            //客户端的下载逻辑
            String[] strings = s.split(",");
            String path = "D:/FTP/" + strings[1];
            File file = new File(path);
            //判断文件是否存在
            if(!file.exists()) {
                response("ERROR,文件不存在。");
            } else {
                //以78接口传输下载文件,创建新的Socket，并且阻塞等待连接 --------------判断端口是否被占用，新建端口集合------------------
                ServerSocket socketDownload = new ServerSocket(78);
                Socket fileSocket = socketDownload.accept();
                try(
                        //文件输入流
                        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                        //套接字输出流
                        BufferedOutputStream out = new BufferedOutputStream(fileSocket.getOutputStream())
                ) {
                    // 将字节流传输给客户端
                    int i = 0;
                    byte[] bytes = new byte[in.available()];
                    while ((i = in.read(bytes)) != -1) {
                        out.write(bytes ,0 , i);
                    }
                    out.flush();
                    System.out.println("数据发送完成。。。");
                    response("文件传输成功");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //关闭连接
                    if(socketDownload != null) socketDownload.close();
                }
            }
        }

        else {
            System.out.println("ERROR,没有匹配的命令。。。"); // 没有匹配的命令，输出错误信息
        }
    }

}
