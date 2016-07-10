package ios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class IOS {// 关于IO流的处理，大多是指令流
	public Socket socket_cmd;
	public ObjectOutputStream oos;
	public ObjectInputStream ois;
	public Scanner sin;

	public IOS(Socket socket, boolean flag) throws Exception {// 建立流
		socket_cmd = socket;
		if (flag) {// 先建立输出流
			oos = new ObjectOutputStream(socket_cmd.getOutputStream());
			ois = new ObjectInputStream(socket_cmd.getInputStream());
		} else {// 先建立输入流
			ois = new ObjectInputStream(socket_cmd.getInputStream());
			oos = new ObjectOutputStream(socket_cmd.getOutputStream());
		}
		sin = new Scanner(System.in);
	}

	public void close() throws Exception {// 关闭流
		sin.close();
		ois.close();
		oos.close();
		socket_cmd.close();
	}

	// 主动模式：客户端指定墙外端口，服务器通过客户端的ip找到客户端并将自己的20端口连接至服务器的指定端口
	// 被动模式：服务器随机生成一个高端端口，客户端通过服务器的ip找到服务器并连接至此端口
	public Socket socket_file(String model) throws Exception {// 服务器
		if (model.equals("port")) {// 主动
			String ip = (String) ois.readObject();// 客户端ip
			int port = ois.readInt();// 客户端的墙外端口
			return new Socket(ip, port, InetAddress.getLocalHost(), 20);// 将本机20端口连接客户端的指定端口
		} else {// 被动
			// 高端端口号范围[1025,65535]，万一获取的端口正忙怎么办？
			int port = new Random().nextInt(65535 - 1025 + 1) + 1025;// 服务器随机生成的高端端口
			oos.writeInt(port);
			oos.flush();
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket = serverSocket.accept();// 等待客户端的连接
			serverSocket.close();// 连接之后一定要关闭以释放该端口！！！
			return socket;
		}
	}

	public Socket socket_file(String model, String ip) throws Exception {// 客户端
		if (model.equals("port")) {// 主动
			oos.writeObject(InetAddress.getLocalHost().getHostAddress());// 客户端ip
			int port = sin.nextInt();// 客户端的墙外端口
			oos.writeInt(port);
			oos.flush();
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket = serverSocket.accept();// 等待服务器的连接
			serverSocket.close();// 连接之后一定要关闭以释放该端口！！！
			return socket;
		} else {// 被动
			int port = ois.readInt();// 服务器的高端端口号
			return new Socket(ip, port);// 通过服务器的ip连接至此高端端口
		}
	}

	public static void load(File file, Socket socket, String op, String way) throws Exception {// 相对于客户端
		if (op.equals("upload")) {
			trans(new FileInputStream(file), socket.getOutputStream(), way);
		} else {
			trans(socket.getInputStream(), new FileOutputStream(file), way);
		}
	}

	public static void trans(InputStream is, OutputStream os, String way) throws Exception {
		if (way.equals("binary")) {
			int c;
			while ((c = is.read()) != -1) {
				os.write(c);
			}
		} else {// ascii只能传文本文件，传二进制文件导致8位字节转换成16位字符而破坏文件
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			PrintWriter pw = new PrintWriter(os);
			String s;
			while ((s = br.readLine()) != null) {
				pw.println(s);
			}
			br.close();// 先关闭这两个流在关闭is,os流
			pw.close();
		}
		is.close();
		os.close();
	}

	public static void deleteDir(String aPath) {// 删除文件、子文件
		File file = new File(aPath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isDirectory())
					deleteDir(f.getAbsolutePath());
				else
					f.delete();
			}
		}
		file.delete();
	}
}
