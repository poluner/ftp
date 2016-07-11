package server;

import java.io.File;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

import ios.IOS;

public class ServerThread extends Thread {// 一旦断网，服务器的这个线程就会立刻结束，所以这里不用做特别的改动
	IOS ios;

	ServerThread(Socket socket_cmd, Connection connection) throws Exception {
		ios = new IOS(socket_cmd, connection);
	}

	public void run() {
		try {
			while (ios != null) {
				String op = (String) ios.readObject();
				if (op.equals("bye")) {
					ios.close();
					System.out.println("通信结束");
					break;
				}

				// 首次非断点下载情况下，获取所选的服务器的所有文件和文件夹下的所有子文件，这些待传输的文件将暂时保存在客户端，直到这些文件全部下载
				if (op.equals("downloadFirst")) {// 只有下载才断点，上传不分是否断点
					String files[] = (String[]) ios.readObject();
					Vector<String> vrPath = ios.vrPath(files);
					ios.writeObject(vrPath);
				}

				if (op.equals("download") || op.equals("upload")) {// 一个文件，多个文件会多次执行这里
					String way = (String) ios.readObject();
					String pathName = (String) ios.readObject();// 绝对路径
					String path = pathName.substring(pathName.lastIndexOf("\\"));
					int port_file = ios.readInt();
					Socket socket_file = ios.socket_file(port_file, true);
					ios.load(new File(pathName), socket_file, op.equals("download") ? "upload" : "download", way);// 服务器的上传下载和客户端刚好相反
					socket_file.close();// 用完了就关闭socket流
				} else if (op.equals("list")) {// 列出当前目录下的目录结构
					ios.writeObject(new File(ios.cd).listFiles());
				} else if (op.startsWith("cd")) {// 改变当前目录
					ios.cd = (String) ios.readObject();
				} else if (op.equals("delete")) {// 删除多个
					String pathNames[] = (String[]) ios.readObject();
					for (String pathName : pathNames) {
						IOS.delete(new File(pathName));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("异常断开一个连接");
		}
	}

}
