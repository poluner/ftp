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
			ios.writeObject(op);
			if (op.equals("bye")) {// 关闭流
				System.out.println("通信结束");
				ios.close();
				break;
			}
			if (op.startsWith("download") || op.startsWith("upload")) {// 首先获取服务器当前路径
				String cd = (String) ios.readObject();// 服务器当前路径
				String model = ios.sin.next();
				String way = ios.sin.next();
				ios.writeObject(model);
				ios.writeObject(way);
				if (op.equals("download") || op.equals("upload")) {// 一个文件
					String pathName_server = cd + "\\" + ios.sin.next();// 服务器文件路径
					String pathName_client = ios.sin.next();// 客户端文件路径
					ios.writeObject(pathName_server);// 传输绝对路径
					ios = ios.breakpoint(pathName_server, pathName_client, model, op, way);
				} else {// 多个文件
					int n = ios.sin.nextInt();
					String pathName_servers[] = new String[n];// 服务器文件名，由服务器的cd给出路径
					String pathName_clients[] = new String[n];// 客户端文件路径
					for (int i = 0; i < n; i++) {
						pathName_servers[i] = cd + "\\" + ios.sin.next();
						pathName_clients[i] = ios.sin.next();
					}
					ios.writeObject(pathName_servers);
					for (int i = 0; i < n; i++) {
						ios = ios.breakpoint(pathName_servers[i], pathName_clients[i], model,
								op.equals("downloadN") ? "download" : "upload", way);
					}
				}
				System.out.println(op + "完成");

			} else if (op.equals("list")) {// 显示目录
				String cd = (String) ios.readObject();
				System.out.println("当前目录：" + cd);
				File files[] = (File[]) ios.readObject();
				for (File file : files) {
					System.out.println(file.getName());
				}
			} else if (op.equals("cd")) {// 更该当前目录
				String folder = ios.sin.next();
				ios.writeObject(folder);
			} else if (op.equals("delete")) {
				String rPath = ios.sin.next();
				ios.writeObject(rPath);
			} else if (op.equals("deleteN")) {
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
