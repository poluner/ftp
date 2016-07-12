package server;

import java.awt.FileDialog;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class MultServer extends Thread {
	ServerSocket serverSocket_cmd;
	static Connection connection;// 全局connection
	static String cd = null;// 全局的cd

	public MultServer() throws Exception {
		serverSocket_cmd = new ServerSocket(21);// 21端口监听命令，这里异常则全部退出
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());// 设置为系统风格
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		String dbURL = "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=ftp";
		connection = DriverManager.getConnection(dbURL, "sa", "123");

		// 确定当前目录
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			cd = fc.getSelectedFile().getPath();
		if (cd == null) // 获取路径失败就退出
			System.exit(0);

		// 运行服务器，除非强制退出
		MultServer multServer = new MultServer();
		multServer.start();
		while (JOptionPane.showConfirmDialog(null, "强制退出？", "服务器正在运行",
				JOptionPane.CLOSED_OPTION) != JOptionPane.OK_OPTION)
			;
		multServer.serverSocket_cmd.close();// 关闭已释放该端口
	}

	public void run() {
		while (serverSocket_cmd.isClosed() == false) {
			try {
				new ServerThread(serverSocket_cmd.accept(), connection, cd).start();
			} catch (Exception e) {
			}
		}
	}

}
