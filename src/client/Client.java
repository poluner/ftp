package client;

import java.io.File;

import ios.IOS;

public class Client {
	static String ip_server = "127.0.0.1";// 服务器ip
	static int port_cmd = 21;
	static IOS ios;

	public static void main(String[] args) throws Exception {// 异常则结束程序
		ios = new IOS(ip_server, port_cmd);// 21端口监听命令

		while (true) {
			String op = ios.sin.next();
			if (op.equals("bye")) {// 关闭流
				ios.writeObject(op);
				System.out.println("通信结束");
				ios.close();
				break;
			}
			if (op.startsWith("download") || op.startsWith("upload")) {// 首先获取服务器当前路径
				String model = ios.sin.next();
				String way = ios.sin.next();
				int n = 1;// 一个文件
				if (op.equals("downloadN") || op.equals("uploadN")) {// 多个文件
					n = ios.sin.nextInt();
				}
				String fileName_servers[] = new String[n];// 服务器文件名，由服务器的cd给出路径
				String pathName_clients[] = new String[n];// 客户端文件路径
				int port_files[] = new int[n];// 默认为-1，及被动模式，如果为主动模式则输入墙外端口号，每个文件都输入一次
				for (int i = 0; i < n; i++) {
					fileName_servers[i] = ios.sin.next();
					pathName_clients[i] = ios.sin.next();
					port_files[i] = model.equals("port") ? ios.sin.nextInt() : -1;
				}
				for (int i = 0; i < n; i++) {
					ios = ios.breakpoint(op.startsWith("download") ? "download" : "upload", way, fileName_servers[i],
							pathName_clients[i], port_files[i]);
				}
				System.out.println(op + "完成");

			} else if (op.equals("list")) {// 显示目录
				ios.writeObject(op);
				String cd = (String) ios.readObject();
				System.out.println("当前目录：" + cd);
				File files[] = (File[]) ios.readObject();
				for (File file : files) {
					System.out.println(file.getName());
				}
			} else if (op.equals("cd")) {// 进入目录
				ios.writeObject(op);
				String folder = ios.sin.next();
				ios.writeObject(folder);
			} else if (op.equals("cd\\")) {// 返回上一层目录
				ios.writeObject(op);
			} else if (op.equals("delete")) {
				ios.writeObject(op);
				String rPath = ios.sin.next();
				ios.writeObject(rPath);
			} else if (op.equals("deleteN")) {
				ios.writeObject(op);
				int n = ios.sin.nextInt();// 删除个数
				String rPaths[] = new String[n];
				for (int i = 0; i < n; i++) {
					rPaths[i] = ios.sin.next();
				}
				ios.writeObject(rPaths);
			}
		}
	}
}
