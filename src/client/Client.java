package client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ios.IOS;

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class Client extends JFrame implements MouseListener {
	public IOS ios;// 如果断网的话，ios会指向新的对象，因此必须使用最新的ios才行
	public File files_table[];// 保存列表中的文件
	public boolean isFile[];// 由于isFile是在线判断，所以要保存下来

	private JPanel contentPane;
	private JTable table;
	private JTextField textField_port;
	private JButton button_list;
	private JButton button_delete;
	private JButton button_upload;
	private JButton button_download;
	private JRadioButton radioButton_port;
	private JRadioButton radioButton_passive;
	private JRadioButton radioButton_binary;
	private JRadioButton radioButton_ascii;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());// 设置为系统风格
					new Client(new IOS("127.0.0.1", 21, "1", "1"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Client(IOS ios_tmp) {
		this.ios = ios_tmp;
		setTitle("hello" + ios.id);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {// 窗口关闭事件
			public void windowClosing(WindowEvent e) {
				try {
					ios.writeObject("bye");// 这里的ios必须是最新的，因此构造函数中的ios不可同名
					ios.close();
					JOptionPane.showMessageDialog(null, "退出正常");
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "退出异常");
				}
			}
		});

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 631, 379);
		contentPane = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon ii = new ImageIcon("background.jpg");
				g.drawImage(ii.getImage(), 0, 0, getWidth(), getHeight(), ii.getImageObserver());
			}
		};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(getWidth() / 2);

		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);

		table = new JTable();
		refreshTable();
		scrollPane.setViewportView(table);

		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(null);

		button_list = new JButton("刷新");
		button_list.setBounds(10, 156, 93, 23);
		panel.add(button_list);

		button_delete = new JButton("删除");
		button_delete.setBounds(10, 202, 93, 23);
		panel.add(button_delete);

		button_upload = new JButton("上传到当前目录");
		button_upload.setBounds(141, 156, 132, 23);
		panel.add(button_upload);

		button_download = new JButton("下载选定文件");
		button_download.setBounds(141, 202, 132, 23);
		panel.add(button_download);

		radioButton_port = new JRadioButton("主动");
		radioButton_port.setBounds(31, 24, 62, 23);
		panel.add(radioButton_port);

		radioButton_passive = new JRadioButton("被动");
		radioButton_passive.setBounds(31, 64, 59, 23);
		panel.add(radioButton_passive);
		ButtonGroup bgpp = new ButtonGroup();
		bgpp.add(radioButton_port);
		bgpp.add(radioButton_passive);

		textField_port = new JTextField();
		textField_port.setBounds(99, 25, 66, 21);
		panel.add(textField_port);
		textField_port.setColumns(10);

		radioButton_binary = new JRadioButton("binary");
		radioButton_binary.setBounds(193, 24, 62, 23);
		panel.add(radioButton_binary);

		radioButton_ascii = new JRadioButton("ascii");
		radioButton_ascii.setBounds(193, 64, 62, 23);
		panel.add(radioButton_ascii);
		ButtonGroup bgba = new ButtonGroup();
		bgba.add(radioButton_binary);
		bgba.add(radioButton_ascii);

		table.addMouseListener(this);
		button_list.addMouseListener(this);
		button_delete.addMouseListener(this);
		button_upload.addMouseListener(this);
		button_download.addMouseListener(this);

		// 设置透明
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		splitPane.setOpaque(false);
		panel.setOpaque(false);
		radioButton_ascii.setOpaque(false);
		radioButton_binary.setOpaque(false);
		radioButton_passive.setOpaque(false);
		radioButton_port.setOpaque(false);

		setVisible(true);
	}

	public void refreshTable() {
		try {
			String op = "list";
			ios.writeObject(op);
			files_table = (File[]) ios.readObject();// 只有其中的文件名/文件夹名可以使用，不可用于判断文件类型
			isFile = (boolean[]) ios.readObject();// 保存是否是文件
			table.setModel(new DefaultTableModel(files_table(), new String[] { "当前目录：" + ios.cd }) {
				public boolean isCellEditable(int row, int column) {// 返回true表示能编辑，false表示不能编辑
					return false;
				}
			});
		} catch (Exception e) {// 断网结束程序
			exit();
		}
	}

	public Object[][] files_table() {
		Object o[][] = new Object[files_table.length + 1][1];
		for (int i = 0; i < files_table.length; i++) {
			o[i][0] = files_table[i].getName();
		}
		o[files_table.length][0] = "返回上一层目录";
		return o;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		try {
			if (e.getSource() == button_list) {// 刷新
				refreshTable();
			} else if (e.getSource() == table) {
				int row = table.getSelectedRow();
				if (e.getClickCount() == 2) {// 只有文件夹可双击
					if (row == files_table.length) {// 返回上一层
						int p = ios.cd.lastIndexOf("\\");
						if (p >= 0) // 如果没有上一层就不可返回了
							ios.cd = ios.cd.substring(0, p);
					} else if (isFile[row] == false) {// 是文件夹
						ios.cd += "\\" + (String) table.getValueAt(row, 0);
					}
					ios.writeObject("cd");
					ios.writeObject(ios.cd);
					refreshTable();
				}
			} else if (e.getSource() == button_delete && table.isRowSelected(files_table.length) == false) {// 删除且最后一行没有被选
				String op = "delete";
				ios.writeObject(op);
				int rows[] = table.getSelectedRows();
				String pathNames[] = new String[rows.length];
				for (int i = 0; i < rows.length; i++) {
					pathNames[i] = ios.cd + "\\" + (String) table.getValueAt(rows[i], 0);
				}
				ios.writeObject(pathNames);
				refreshTable();
			} else if (e.getSource() == button_upload || e.getSource() == button_download) {// 上传下载
				int port_file = port();// 被动就是-1
				if (((radioButton_port.isSelected() && port_file != -1 || radioButton_passive.isSelected())
						&& (radioButton_binary.isSelected() || radioButton_ascii.isSelected())) == false)
					return;

				String pathNames_server[];
				String pathNames_client[];
				if (e.getSource() == button_upload) {// 上传
					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(true);// 设置多选
					fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);// 可以同时选择文件和文件夹
					if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
						return;

					// 如果没有这句话，点击了返回也会选择文件
					File files[] = fc.getSelectedFiles();// 多选的文件和文件夹
					Vector<String> vrPath_client = IOS.vrPath(files);// 获取客户端的这些文件夹下的所有文件
					pathNames_client = new String[vrPath_client.size()];
					pathNames_server = new String[vrPath_client.size()];
					for (int i = 0; i < vrPath_client.size(); i++) {
						pathNames_client[i] = files[0].getParent() + vrPath_client.elementAt(i);
						pathNames_server[i] = ios.cd + vrPath_client.elementAt(i);
					}

				} else {// 下载，没有点到
					// 首先获取服务器所有文件，因为服务器不稳定，信息都要保存在客户端
					int rows[] = table.getSelectedRows();
					if (rows.length == 0)
						return;// 没有选择
					if (table.isRowSelected(files_table.length) == true)
						return;// 点到“返回上一层”
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 设置只能打开目录
					if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
						return;

					String cd_client = fc.getSelectedFile().getPath();

					String files[] = new String[rows.length];
					for (int i = 0; i < rows.length; i++) {
						files[i] = ios.cd + "\\" + (String) table.getValueAt(rows[i], 0);
					}
					ios.writeObject("downloadFirst");// 非断点下载
					ios.writeObject(files);
					Vector<File> vrPath_server = (Vector<File>) ios.readObject();// 获取且暂时保存服务器所选的所有文件的路径

					pathNames_server = new String[vrPath_server.size()];
					pathNames_client = new String[vrPath_server.size()];
					for (int i = 0; i < vrPath_server.size(); i++) {
						pathNames_server[i] = ios.cd + vrPath_server.elementAt(i);
						pathNames_client[i] = cd_client + vrPath_server.elementAt(i);
					}

				}

				for (int i = 0; i < pathNames_server.length; i++) {// 多次执行单文件传输
					setTitle("正在传输文件" + pathNames_server[i]);
					ios = ios.keepTrans(e.getSource() == button_upload ? "upload" : "download",
							radioButton_binary.isSelected() ? "binary" : "ascii", pathNames_server[i],
							pathNames_client[i], port_file);
					setTitle("文件传输完毕" + pathNames_server[i]);
				}

				if (ios != null) {
					JOptionPane.showMessageDialog(null, pathNames_server.length + "个文件传输结束");
				}
				setTitle("hello" + ios.id);
				refreshTable();// 因为断网后服务器的当前路径可能发生改变，所以传输完成后都要刷新

			}
		} catch (Exception e1) {
			exit();
		}
	}

	public void exit() {// 断网后操作将抛出异常
		if (ios != null) {// 先询问用户选择是否连接
			ios = ios.keepConnect();
			refreshTable();// 每一次重新连接都要刷新列表
		} else {// 用户选择放弃连接
			JOptionPane.showMessageDialog(null, "退出程序", "网络断开", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public int port() {
		try {
			if (radioButton_passive.isSelected())
				return -1;// 被动则返回-1
			int port = Integer.parseInt(textField_port.getText());
			if (port >= 0 && port <= 65535) // 合法
				return port;
		} catch (Exception e) {// 失败
		}
		return -1;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

}
