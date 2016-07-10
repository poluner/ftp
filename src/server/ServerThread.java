package server;

import java.io.File;
import java.net.Socket;

import ios.IOS;

public class ServerThread extends Thread {// 一旦断网，服务器的这个线程就会立刻结束，所以这里不用做特别的改动
	String cd = "B:\\Desktop";// 设置初始目录
	IOS ios;

	ServerThread(Socket socket_cmd) throws Exception {
		ios = new IOS(socket_cmd);
	}

	public void run() {
		try {
			while (true) {
				String op = (String) ios.readObject();
				if (op.equals("bye")) {
					ios.close();
					System.out.println("通信结束");
					break;
				}
				if (op.equals("download") || op.equals("upload")) {// 一个文件，多个文件会多次执行这里
					ios.writeObject(cd);// 首先传输当前路径
					String way = (String) ios.readObject();
					String pathName = (String) ios.readObject();// 绝对路径
					int port_file = ios.readInt();
					Socket socket_file = ios.socket_file(port_file, true);
					ios.load(new File(pathName), socket_file, op.equals("download") ? "upload" : "download", way);// 服务器的上传下载和客户端刚好相反
					socket_file.close();// 用完了就关闭socket流
				} else if (op.equals("list")) {// 列出当前目录下的目录结构
					ios.writeObject(cd);
					ios.writeObject(new File(cd).listFiles());
				} else if (op.equals("cd\\")) {// 返回上一层
					cd = cd.substring(0, cd.lastIndexOf("\\"));
				} else if (op.equals("cd")) {// 进入文件夹
					String folder = (String) ios.readObject();
					cd += "\\" + folder;
				} else if (op.equals("delete")) {// 删除多个
					String rPaths[] = (String[]) ios.readObject();
					for (String rPath : rPaths) {
						IOS.deleteDir(cd + "\\" + rPath);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("断开一个连接");
		}
	}

}
