import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author 何佳辉
 * @title 客户端
 * @description
 * @date 2022/11/8
 **/
public class FtpClient {
    //当前访问目录
    public static String currentPath = "D:\\FTP";
    //通讯套接字
    public static Socket socket = null;
    //通讯的输入输出流
    public static BufferedReader bufferedReader = null;
    public static BufferedWriter bufferedWriter = null;
    //服务端的IP
    public static String serviceIP;

    public static void main(String[] args) throws IOException {
        //在这里设置服务器端的IP
        serviceIP = "localhost";
        try {
            socket = new Socket(serviceIP , 21);
            socket.setSoTimeout(60000);
            System.out.println("连接成功。");
            boolean sign = true;
            Scanner scanner = new Scanner(System.in);
            //用一个死循环阻塞
            while (sign) {
                System.out.println("请输入命令(输入HELP查看命令提示)：");
                String str = scanner.next();
                //在这里进行判断，连接是否中断，如果连接已关闭 则重置连接
//                System.out.println("重置连接。。");
//                resetSocket();
                //获取输入输出流  把这里的操作抽取出来 当输入的数据有错误 则返回错误信息 不进行通信
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                if(str.startsWith("EXIT") || str.startsWith("QUIT")) {
                    //跳出循环
                    request(bufferedWriter , str);
                    break;
                } else if(str.startsWith("HELP")) {
                    System.out.println("ROOT     -- 返回根目录\n" +
                            "CWD      -- 切换到下一级子目录\n" +
                            "RCWD     -- 转到上一级目录\n" +
                            "LIST     -- 列出当前目录下的所有文件信息\n" +
                            "EXIT     -- 退出\n" +
                            "QUIT     -- 退出\n" +
                            "UPLOAD   -- 上传文件 ','后面接文件的绝对路径\n" +
                            "DOWNLOAD -- 下载文件 ','后面接文件名");
                    continue;
                } else if(str.startsWith("CWD")) {
                    //更改当前目录 - 请求是否存在该目录 - 不存在该目录则 操作失败
                    String[] strCwd = str.split(",");
                    setCurrentPath(currentPath + File.separator + strCwd[1]);
                    //传递给服务端的path 这里用空格区分命令和参数
                    str = "CWD " + currentPath;
                    request(bufferedWriter , str);
                } else if(str.startsWith("RCWD")) {
                    //返回上一级目录
                    //至少包含根目录
                    if(getCurrentPath().contains("D:\\FTP")) {
                        String[] rStr = getCurrentPath().split("\\\\");
                        if(rStr.length <= 2) {
                            reSetPath();
                            str = "CWD " + currentPath;
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < rStr.length-1; i++) {
                                if(i == rStr.length - 2) {
                                    sb.append(rStr[i]);
                                }else {
                                    sb.append(rStr[i]).append(File.separator);
                                }
                            }
                            setCurrentPath(sb.toString());
                            System.out.println("返回的目录: "  + sb.toString());
                            str = "CWD " + sb.toString();
                        }
                    } else {
                        str = "ERROR";
                    }
                    request(bufferedWriter , str);
                } else if(str.contains("ROOT")) {
                    //返回根目录
                    reSetPath();
                    str = "CWD " + currentPath;
                    request(bufferedWriter , str);
                } else if (str.startsWith("UPLOAD")){
                    //上传逻辑 上传文件的位置  当前目录
                    System.out.println(str);
                    String[] strings = str.split(",");
                    System.out.println(strings[1]);
                    uploadFile(strings[1]);
                } else if(str.startsWith("DOWNLOAD")) {
                    //下载逻辑 下载文件的路径 存储到本机的位置
                    String[] strings = str.split(",");
                    //下载逻辑
                    downloadFile(strings[1]);
                } else if(str.startsWith("LIST")) {
                    request(bufferedWriter , "LIST," + currentPath);
                } else {
                    System.out.println("命令输入错误");
                    continue;
                }

                //读出操作的返回信息
                String response = bufferedReader.readLine();
                //错误信息的处理
                if(ifError(response))
                    System.out.println(getErrorMsg(response));
                else{
                    //对于命令的返回处理
                    handleRes(response);
                }

            }
            System.out.println("连接关闭。");
        }catch (IOException e) {
            System.out.println("连接失败。");
            e.printStackTrace();
        }finally {
            if(socket!=null) socket.close();
            if(bufferedWriter != null) bufferedWriter.close();
            if(bufferedReader != null) bufferedWriter.close();
        }
    }

    public static void request(BufferedWriter writer , String str) throws IOException {
        System.out.println("request: " + str);
        writer.write(str);
        writer.newLine();
        //将数据发送
        writer.flush();
    }

    //判断是否存在错误信息
    public static boolean ifError(String res) {
        if (res.contains("ERROR")) {
            return true;
        }else {
            return false;
        }
    }

    //获取错误信息
    public static String getErrorMsg(String res) {
        res = res.replace("[" , " ");
        res = res.replace("]", " ");
        res.trim();
        String[] msg = res.split(",");
        return msg[1];
    }

    public static void handleRes(String response) {
        System.out.println("response: " + response);
        //数据传递以ArrayList传递
        response = response.replace("[" , " ");
        response = response.replace("]" , " ");
        response.trim();
        String[] resStr = response.split(",");
        String cmd = resStr[0].toUpperCase().trim();
        //list命令的返回
        if("LIST".equals(cmd)) {
            System.out.println(" 当前目录：" + currentPath);
            for (int i = 1; i <= resStr.length - 1; i++) {
                System.out.println("  " + resStr[i]);
            }
        } if("CWD".equals(cmd)) {
            System.out.println(" 当前目录为： " + getCurrentPath());
            for (int i = 1; i < resStr.length - 1; i++) {
                System.out.println("  " + resStr[i]);
            }
        } if("UPLOAD".equals(cmd)) {
            //返回操作成功的提示
            System.out.println(resStr[1]);
        }if("DOWNLOAD".equals(cmd)) {
            //返回操作成功的提示
            System.out.println(resStr[1]);
        } else {
            //如果是不存在的命令 就不予以发送  暂未完成
        }

    }

    public static void uploadFile(String uploadPath) throws IOException {
        System.out.println("上传文件路径为：" + uploadPath);
        File file = new File(uploadPath);
        if(!file.exists()) {
            System.out.println("文件不存在。");
            return;
        }
        if (!file.isFile()) {
            System.out.println("无法上传文件夹，请确认。");
            return;
        }
        //用于传输数据的套接字
        System.out.println("新建的套接字连接");
        ServerSocket fileSocket = new ServerSocket(78);
        fileSocket.setSoTimeout(60000);
        System.out.println("开始连接");

        //通信Socket输入输出流
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //告知服务器端开始连接78端口  目录及文件名称  1
        //System.out.println("发送真正的请求：");
        request(bufferedWriter , "UPLOAD," + getCurrentPath() + File.separator + file.getName());
        //等待服务器连接
        Socket uploadFileSocket = fileSocket.accept();
        //连接成功显示  2
        //System.out.println("连接成功");
        //如果连接成功 则收取服务端的消息
//        System.out.println(bufferedReader.readLine());
        try(
                //文件输入流
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                //Socket输出流
                //考虑使用新的Socket创建连接实现文件的传输
                BufferedOutputStream outputStream = new BufferedOutputStream(uploadFileSocket.getOutputStream());
        ) {
//            System.out.println("流大小为：" + inputStream.available());
            byte[] bytes = new byte[inputStream.available()];
            int i = 0;
            while ((i = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes , 0 , i);
            }
            //一次发送完所有数据
            outputStream.flush();
            //连接套接字的返回的传输完成信息
//            System.out.println(bufferedReader.readLine());
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileSocket != null) fileSocket.close();
            if(uploadFileSocket != null) uploadFileSocket.close();
        }
    }

    public static void downloadFile(String fileName) throws IOException {
        //拼接文件的下载地址
        String filePath = getCurrentPath() + File.separator + fileName;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream()));
        //下载命令的发送
        System.out.println("DOWNLOAD," + filePath);
        request(bufferedWriter , "DOWNLOAD," + filePath);
        //合法文件 连接对应的接口
        Socket socketDownload = new Socket(serviceIP , 78);
        //数据传输时间不得超过一分钟
        socketDownload.setSoTimeout(60000);
        //System.out.println(bufferedReader.readLine());
        //默认存储路径
        String storePath = "D:" + File.separator + "FTPDownload" + File.separator +  fileName;
        System.out.println("存储路径为：" + storePath);
        File file = new File(storePath);
        if(file.exists())
            file.delete();
        try(
                //将文件存储到本地 默认为F:\FTPDOWNLOAD
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                BufferedInputStream in = new BufferedInputStream(socketDownload.getInputStream())
        ) {
            while (in.available() == 0) {
                Thread.sleep(1000);
                System.out.println("等待服务器端发送数据...");
            }
            System.out.println("文件流大小为：" + in.available());
            int i = 0;
            byte[] bytes = new byte[in.available()];
            while ((i = in.read(bytes)) != -1) {
                out.write(bytes , 0 , i);
            }
            out.flush();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(socketDownload != null) socketDownload.close();
        }

    }

    //重置连接
    public static void resetSocket() throws IOException {
        Socket socket1 = new Socket(serviceIP , 21);
        socket1.setSoTimeout(300000);
        setSocket(socket1);
        System.out.println("连接已经重置。。。");
    }

    //获取当前目录
    public static String getCurrentPath() {
        return currentPath;
    }
    //设置目录
    public static void setCurrentPath(String path) {
        currentPath = path;
    }
    //返回根节点
    public static void reSetPath() {
        currentPath = "D:\\FTP";
    }
    //获取当前的通信套接字
    public static Socket getSocket() {
        return socket;
    }
    //设置通信的套接字
    public static void setSocket(Socket socket) {
        FtpClient.socket = socket;
    }
}

