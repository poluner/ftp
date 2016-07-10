package ios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	public String ip_server;
	public int port_cmd;
	public static final int timeout=10000;

	public IOS(Socket socket_cmd) throws Exception {// 服务器
		this.socket_cmd = socket_cmd;
		ois = new ObjectInputStream(socket_cmd.getInputStream());
		oos = new ObjectOutputStream(socket_cmd.getOutputStream());
		sin = new Scanner(System.in);
	}

	public IOS(String ip_server, int port_cmd) throws Exception {// 客户端
		this.ip_server = ip_server;
		this.port_cmd = port_cmd;
		socket_cmd = new Socket(ip_server, port_cmd);
		oos = new ObjectOutputStream(socket_cmd.getOutputStream());
		ois = new ObjectInputStream(socket_cmd.getInputStream());
		sin = new Scanner(System.in);
	}

	public void close() throws Exception {// 关闭流
		sin.close();
		ois.close();
		oos.close();
		socket_cmd.close();
	}

	// 输出自带刷新
	public void writeObject(Object obj) throws IOException {
		oos.writeObject(obj);
		oos.flush();
	}

	public void writeInt(int val) throws IOException {
		oos.writeInt(val);
		oos.flush();
	}

	public void writeLong(long val) throws IOException {
		oos.writeLong(val);
		oos.flush();
	}

	public Object readObject() throws ClassNotFoundException, IOException {
		return ois.readObject();
	}

	public int readInt() throws IOException {
		return ois.readInt();
	}

	public long readLong() throws IOException {
		return ois.readLong();
	}

	// 主动模式：客户端指定墙外端口，服务器通过客户端的ip找到客户端并将自己的20端口连接至服务器的指定端口
	// 被动模式：服务器随机生成一个高端端口，客户端通过服务器的ip找到服务器并连接至此端口
	public Socket socket_file(int port, boolean isServer) throws Exception {// 服务器
		if (isServer) {
			if (port != -1) {// 主动就直接用这个port
				String ip = (String) readObject();// 客户端ip
				return new Socket(ip, port, InetAddress.getLocalHost(), 20);// 将本机20端口连接客户端的指定端口
			} else {// 被动
				// 高端端口号范围[1025,65535]，万一获取的端口正忙怎么办？
				port = new Random().nextInt(65535 - 1025 + 1) + 1025;// 服务器随机生成的高端端口
				writeInt(port);
				ServerSocket serverSocket = new ServerSocket(port);
				Socket socket = serverSocket.accept();// 等待客户端的连接
				serverSocket.close();// 连接之后一定要关闭以释放该端口！！！
				return socket;
			}
		} else {
			if (port != -1) {// 主动
				writeObject(InetAddress.getLocalHost().getHostAddress());// 客户端ip
				ServerSocket serverSocket = new ServerSocket(port);
				Socket socket = serverSocket.accept();// 等待服务器的连接
				serverSocket.close();// 连接之后一定要关闭以释放该端口！！！
				return socket;
			} else {// 被动
				port = readInt();// 服务器的高端端口号
				return new Socket(ip_server, port);// 通过服务器的ip连接至此高端端口
			}
		}
	}

	public IOS keepConnect() {// 客户端，坚持传完文件，除非用户主动放弃！！！
		try {
			return new IOS(ip_server, port_cmd);// 连接成功
		} catch (Exception e) {
			System.out.println("连接断开，是否尝试连接？y/n");
			if (sin.next().equals("y")) {
				return keepConnect();// 失败后继续连接
			}
			return null;// 用户放弃连接
		}
	}

	public IOS keepTrans(boolean first, String op, String way, String fileName_server, String pathName_client,
			int port_file) {// 记录了服务器文件路径，即使传输断了也可以由路径找到破损文件并继续传输
		try {
			writeObject(op);
			String cd = (String) readObject();// 服务器当前路径
			if (first) // 在当且仅当第一次的时候确定了绝对路径
				fileName_server = cd + "\\" + fileName_server;
			writeObject(way);
			writeObject(fileName_server);
			writeInt(port_file);
			Socket socket_file = socket_file(port_file, false);
			socket_file.setSoTimeout(timeout);// 设置文件传输超时时间
			load(new File(pathName_client), socket_file, op, way);// 传输
			socket_file.close();// 成功传输并且用完才关闭，失败的话会自动关闭
			return this;// 成功则返回自己
		} catch (Exception e) {
			IOS ios = keepConnect();// 失败则返回新建立的
			if (ios != null) {
				System.out.println("再次成功连接，是否继续刚才文件的传输？y/n");
				if (sin.next().equals("y")) {
					// 注意下面这句话是ios=ios.breakpoint(...)而不是ios=this.breakpoint(...)，因为这里要用新对象ios，旧的对象this因为断网而废掉了
					ios = ios.keepTrans(false, op, way, fileName_server, pathName_client, port_file);// 再次传输恐怕又出现断网，所以用递归
				}
				return ios;// 返回最新的连接
			}
			return null;
		}
	}

	public void load(File file, Socket socket, String op, String way) throws Exception {// 相对于客户端，一个文件
		if (op.equals("upload")) {
			FileInputStream fis = new FileInputStream(file);
			fis.skip(readLong());
			trans(fis, socket.getOutputStream(), way);
		} else {
			file.createNewFile();
			writeLong(file.length());
			trans(socket.getInputStream(), new FileOutputStream(file, true), way);// 追加文件
		}
	}

	public void trans(InputStream is, OutputStream os, String way) throws Exception {
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
