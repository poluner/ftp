package client;

import java.io.File;
import java.net.Socket;

import ios.IOS;

public class Client {
	static String ip_server = "127.0.0.1";// 服务器ip

	public static void main(String[] args) throws Exception {// 异常则结束程序
		IOS ios = new IOS(new Socket(ip_server, 21), true);// 21端口监听命令
		while (true) {
			String op = ios.sin.next();
			ios.oos.writeObject(op);
			ios.oos.flush();
			if (op.equals("bye")) {// 关闭流
				System.out.println("通信结束");
				ios.close();
				break;
			}
			if (op.startsWith("download") || op.startsWith("upload")) {
				String model = ios.sin.next();
				String way = ios.sin.next();
				ios.oos.writeObject(model);
				ios.oos.writeObject(way);
				if (op.equals("download") || op.equals("upload")) {// 一个文件
					String fileName_server = ios.sin.next();// 服务器文件名，由服务器的cd给出路径
					String pathName_client = ios.sin.next();// 客户端文件路径
					ios.oos.writeObject(fileName_server);
					ios.oos.flush();
					Socket socket_file = ios.socket_file(model, ip_server);
					IOS.load(new File(pathName_client), socket_file, op, way);
					socket_file.close();// 用完就关闭
				} else {// 多个文件
					int n = ios.sin.nextInt();
					String fileName_servers[] = new String[n];// 服务器文件名，由服务器的cd给出路径
					String pathName_clients[] = new String[n];// 客户端文件路径
					for (int i = 0; i < n; i++) {
						fileName_servers[i] = ios.sin.next();
						pathName_clients[i] = ios.sin.next();
					}
					ios.oos.writeObject(fileName_servers);
					ios.oos.flush();
					for (int i = 0; i < n; i++) {
						Socket socket_file = ios.socket_file(model, ip_server);// 每一个文件都开一个端口
						IOS.load(new File(pathName_clients[i]), socket_file, op.equals("downloadN")?"download":"upload", way);//一个文件
						socket_file.close();// 用完就关闭
					}
				}
				System.out.println(op + "完成");
			} else if (op.equals("list")) {// 显示目录
				String cd = (String) ios.ois.readObject();
				System.out.println("当前目录：" + cd);
				File files[] = (File[]) ios.ois.readObject();
				for (File file : files) {
					System.out.println(file.getName());
				}
			} else if (op.equals("cd")) {// 更该当前目录
				String folder = ios.sin.next();
				ios.oos.writeObject(folder);
				ios.oos.flush();
			} else if (op.equals("delete")) {
				String rPath = ios.sin.next();
				ios.oos.writeObject(rPath);
				ios.oos.flush();
			} else if (op.equals("deleteN")) {
				int n = ios.sin.nextInt();// 删除个数
				String rPaths[] = new String[n];
				for (int i = 0; i < n; i++) {
					rPaths[i] = ios.sin.next();
				}
				ios.oos.writeObject(rPaths);
				ios.oos.flush();
			}
		}

	}

}
