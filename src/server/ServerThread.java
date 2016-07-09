package server;

import java.io.File;
import java.net.Socket;

import ios.IOS;

public class ServerThread extends Thread {
	String cd = "C:";// 设置初始目录
	IOS ios;

	ServerThread(Socket socket_cmd) throws Exception {
		ios = new IOS(socket_cmd, false);
	}

	public void run() {
		try {
			while (true) {
				String op = (String) ios.ois.readObject();
				if (op.equals("bye")) {
					ios.close();
					break;
				}
				if (op.startsWith("download") || op.startsWith("upload")) {
					String model = (String) ios.ois.readObject();
					String way = (String) ios.ois.readObject();
					if (op.equals("download") || op.equals("upload")) {// 一个
						String pathName = cd + "\\" + (String) ios.ois.readObject();
						Socket socket_file = ios.socket_file(model);
						IOS.load(new File(pathName), socket_file, op.equals("download") ? "upload" : "download", way);// 服务器的上传下载和客户端刚好相反
						socket_file.close();// 用完了就关闭socket流
					} else {// 多个
						String pathNames[] = (String[]) ios.ois.readObject();
						for (String pathName : pathNames) {
							Socket socket_file = ios.socket_file(model);
							IOS.load(new File(cd + "\\" + pathName), socket_file,
									op.equals("downloadN") ? "upload" : "download", way);// 服务器的上传下载和客户端刚好相反
							socket_file.close();// 用完了就关闭socket流
						}
					}

				} else if (op.equals("list")) {// 列出当前目录下的目录结构
					ios.oos.writeObject(cd);
					ios.oos.writeObject(new File(cd).listFiles());
					ios.oos.flush();
				} else if (op.equals("cd\\")) {// 返回上一层
					cd = cd.substring(0, cd.lastIndexOf("\\"));
				} else if (op.equals("cd")) {// 进入文件夹
					String folder = (String) ios.ois.readObject();
					cd += "\\" + folder;
				} else if (op.equals("delete")) {// 删除一个
					String rPath = (String) ios.ois.readObject();
					IOS.deleteDir(cd + "\\" + rPath);
				} else if (op.equals("deleteN")) {
					String rPaths[] = (String[]) ios.ois.readObject();
					for (String rPath : rPaths) {
						IOS.deleteDir(cd + "\\" + rPath);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
