import java.net.*;

/**
 * @author 何佳辉
 * @title
 * @description 服务端
 * @date 2022/11/8
 **/
public class FtpServer extends Thread {

    public static int ftpPort = 21;//定义服务器端口
    ServerSocket serverSocket = null;

    public static void main(String[] args) {
        System.out.println("FTP地址为 D:\\FTP");
        new FtpServer().start();
    }

    @Override
    public void run() {
        Socket socket;
        try {
            serverSocket = new ServerSocket(ftpPort);
            System.out.println("开始监听的端口: " + ftpPort);
            while(true){
                //每一个新的连接 对应一个线程
                socket = serverSocket.accept();
                new FtpConnection(socket).start();
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

}
