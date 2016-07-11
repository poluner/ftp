package server;

import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;

public class MultServer {

	public static void main(String[] args) throws Exception {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		String dbURL = "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=ftp";
		Connection connection = DriverManager.getConnection(dbURL, "sa", "123");
		ServerSocket serverSocket_cmd = new ServerSocket(21);// 21端口监听命令，这里异常则全部退出
		while (true) {
			try {
				new ServerThread(serverSocket_cmd.accept(), connection).start();
				System.out.println("连接服务器成功");
			} catch (Exception e) {
				System.out.println("连接服务器失败");
			}
		}
	}

}
