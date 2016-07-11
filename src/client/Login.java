package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ios.IOS;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Login extends JFrame {
	public IOS ios = null;

	private JPanel contentPane;
	private JTextField textField_id;
	private JPasswordField passwordField;
	private JTextField textField_ip;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());// 设置为系统风格
					new Login();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel label = new JLabel("用户名：");
		label.setBounds(100, 90, 54, 15);
		contentPane.add(label);

		JLabel label_1 = new JLabel("密码：");
		label_1.setBounds(100, 136, 54, 15);
		contentPane.add(label_1);

		textField_id = new JTextField();
		textField_id.setBounds(185, 87, 120, 21);
		contentPane.add(textField_id);
		textField_id.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setBounds(185, 133, 120, 21);
		contentPane.add(passwordField);

		JLabel lblip = new JLabel("服务器IP：");
		lblip.setBounds(100, 45, 65, 15);
		contentPane.add(lblip);

		textField_ip = new JTextField();
		textField_ip.setBounds(185, 42, 120, 21);
		contentPane.add(textField_ip);
		textField_ip.setColumns(10);

		JButton button = new JButton("确定");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String ip = textField_ip.getText().replace(" ", "");
				String id = textField_id.getText().replace(" ", "");
				String pw = new String(passwordField.getPassword()).replace(" ", "");
				if (ip.length() == 0 || id.length() == 0 || pw.length() == 0)
					return;
				try {
					ios = new IOS(ip, 21, id, pw);
					new Client(ios);
					dispose();// 销毁窗口
					return;
				} catch (Exception e) {
				}
				setTitle("连接失败");
			}
		});
		button.setBounds(212, 198, 93, 23);
		contentPane.add(button);
		setVisible(true);
	}
}
