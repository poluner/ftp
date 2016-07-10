package server;

import java.net.ServerSocket;

public class MultServer {

	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket_cmd = new ServerSocket(21);// 21端口监听命令，这里异常则全部退出
		while (true) {
			new ServerThread(serverSocket_cmd.accept()).start();
			System.out.println("服务器响应");
		}
	}

}
