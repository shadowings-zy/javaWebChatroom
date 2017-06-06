package Client;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.StringTokenizer;
import java.awt.event.ActionEvent;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class ClientFrame extends JFrame implements ActionListener {

	/**
	 * MiniQQ客户端
	 */
	private static final long serialVersionUID = 1L;

	protected static final String Client = null;
	
	public static int printPort = 8800;
	public static int sendPort = 3000;

	/*
	 * 登录器的变量
	 */
	JPanel pnlLogin;
	JLabel lblServerIP, lblName, lblPassword;
	JTextField txtServerIP, txtName;
	JPasswordField txtPassword;
	JButton btnLogin, btnReg;
	GridBagLayout layout;
	Boolean isReg = true;
	JDialog dialogLogin = new JDialog(this, "MiniQQ登陆", true);

	/*
	 * 主界面变量
	 */
	JFrame frame;
	JPanel pnlBack, pnlTalk;
	JButton butSend, butPic, butExpression;
	JButton lenghan, fanu, zaijian, keai, poqiweixiao, ku, fadai, piezui, weixiao;
	JTextPane textArea;
	JTextField inputText;
	JLabel lblTalk, lblTo;
	JComboBox<Object> listOnline;
	JList<String> listUser;
	DefaultListModel<String> listModel = new DefaultListModel<String>();
	JPopupMenu ListPopupMenu = null, ExpressionMenu = null, TextPopupMenu = null;
	JScrollPane scrollPane = null;
	JMenuItem sendFile, drawPicture, cleanText;

	/*
	 * socket变量
	 */
	Socket socket = null;
	BufferedReader in = null;
	PrintWriter out = null;

	/*
	 * 其他变量
	 */
	String strSend, strReceive, strKey, strStatus;
	String[] userList = new String[50];
	BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
	Graphics graphic = bufferedImage.createGraphics();
	int rgb, onlineNum = 1;
	private StringTokenizer st;
	private JPanel publicChatPanel;
	private JPanel editPanel;
	private StyledDocument doc = null;
	private JButton butScreenCapture;

	/**
	 * 构造器
	 */
	public ClientFrame() {
		userList[0] = "admin";
		listModel.addElement("admin");

		frame = new JFrame("miniQQ");
		frame.setResizable(false);
		frame.setBackground(new Color(255, 215, 0));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setBounds(350, 150, 708, 507);
		frame.getContentPane().setLayout(null);

		JLabel label = new JLabel("\u5728\u7EBF\u4EBA\u5458\u5217\u8868");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("微软雅黑", Font.BOLD, 18));
		label.setBounds(10, 31, 147, 30);
		label.setBackground(Color.BLACK);
		label.setForeground(Color.BLACK);
		label.setVerticalAlignment(SwingConstants.TOP);
		frame.getContentPane().add(label);

		inputText = new JTextField(30);
		inputText.setHorizontalAlignment(SwingConstants.LEFT);
		inputText.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		inputText.setSize(467, 23);
		inputText.setLocation(193, 427);
		frame.getContentPane().add(inputText);

		listUser = new JList<String>(listModel);
		listUser.setSize(106, 384);
		listUser.setLocation(31, 71);
		listUser.setFont(new Font("微软雅黑", 0, 12));
		listUser.setVisibleRowCount(17);
		listUser.setFixedCellWidth(180);
		listUser.setFixedCellHeight(18);
		listUser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane spUser = new JScrollPane();
		spUser.setBackground(Color.cyan);
		spUser.setFont(new Font("微软雅黑", 0, 12));
		spUser.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spUser.getViewport().setView(listUser);

		spUser.setBounds(31, 71, 106, 379);

		frame.getContentPane().add(listUser);
		frame.getContentPane().add(spUser);

		butSend = new JButton("\u53D1\u9001");
		butSend.setForeground(new Color(255, 215, 0));
		butSend.setBackground(new Color(0, 0, 0));
		butSend.addActionListener(this);
		butSend.setFont(new Font("微软雅黑", Font.BOLD, 13));
		butSend.setFocusPainted(false);
		butSend.setBounds(586, 373, 85, 30);
		frame.getContentPane().add(butSend);

		butPic = new JButton("\u56FE\u7247");
		butPic.addActionListener(this);
		butPic.setBackground(new Color(0, 0, 0));
		butPic.setForeground(new Color(255, 215, 0));
		butPic.setFont(new Font("微软雅黑", Font.BOLD, 13));
		butPic.setFocusPainted(false);
		butPic.setBounds(183, 373, 72, 30);
		frame.getContentPane().add(butPic);
		butPic.addActionListener(new ActionListener() {//传输图片
			public void actionPerformed(ActionEvent e) {
				JFileChooser f = new JFileChooser(); // 查找文件
				f.showOpenDialog(null);
				File file = f.getSelectedFile();
				String picture = ClientTools.readPic(file);
				out.println(
						"picture|" + picture + "|" + txtName.getText() + "|" + listOnline.getSelectedItem().toString());
				inputText.setText("");
			}
		});

		butExpression = new JButton("\u8868\u60C5");
		butExpression.setForeground(new Color(255, 215, 0));
		butExpression.setBackground(new Color(0, 0, 0));
		butExpression.setFont(new Font("微软雅黑", Font.BOLD, 13));
		butExpression.setFocusPainted(false);
		butExpression.setBounds(265, 373, 72, 30);
		frame.getContentPane().add(butExpression);
		butExpression.addMouseListener(new MouseAdapter() { // 实现鼠标左击弹出菜单
			public void mousePressed(MouseEvent e) {
				ExpressionMenu.show(butExpression, e.getX(), e.getY());
			}
		});

		lenghan = new JButton();
		lenghan.setToolTipText("冷汗");
		lenghan.setPreferredSize(new Dimension(32, 32));
		lenghan.setIcon(new ImageIcon("material\\lenghan.gif"));
		lenghan.addActionListener(this);
		lenghan.addMouseListener(new MouseAdapter() { // 实现鼠标左击弹出菜单
			public void mousePressed(MouseEvent e) {//传输表情“冷汗”
				out.println("expression|" + "lenghan.gif" + "|" + txtName.getText() + "|"
						+ listOnline.getSelectedItem().toString());
				inputText.setText("");
			}
		});

		fanu = new JButton();
		fanu.setToolTipText("发怒");
		fanu.setPreferredSize(new Dimension(32, 32));
		fanu.setIcon(new ImageIcon("material\\fanu.gif"));
		fanu.addActionListener(this);
		fanu.addMouseListener(new MouseAdapter() { // 实现鼠标左击弹出菜单
			public void mousePressed(MouseEvent e) {
				out.println("expression|" + "fanu.gif" + "|" + txtName.getText() + "|"
						+ listOnline.getSelectedItem().toString());
				inputText.setText("");
			}
		});

		zaijian = new JButton();
		zaijian.setToolTipText("再见");
		zaijian.setPreferredSize(new Dimension(32, 32));
		zaijian.setIcon(new ImageIcon("material\\zaijian.gif"));
		zaijian.addActionListener(this);
		zaijian.addMouseListener(new MouseAdapter() { // 实现鼠标左击弹出菜单
			public void mousePressed(MouseEvent e) {
				out.println("expression|" + "zaijian.gif" + "|" + txtName.getText() + "|"
						+ listOnline.getSelectedItem().toString());
				inputText.setText("");
			}
		});

		weixiao = new JButton();
		weixiao.setToolTipText("微笑");
		weixiao.setPreferredSize(new Dimension(32, 32));
		weixiao.setIcon(new ImageIcon("material\\weixiao.gif"));
		weixiao.addActionListener(this);
		weixiao.addMouseListener(new MouseAdapter() { // 实现鼠标左击弹出菜单
			public void mousePressed(MouseEvent e) {
				out.println("expression|" + "weixiao.gif" + "|" + txtName.getText() + "|"
						+ listOnline.getSelectedItem().toString());
				inputText.setText("");
			}
		});

		keai = new JButton();
		keai.setToolTipText("可爱");
		keai.setPreferredSize(new Dimension(32, 32));
		keai.setIcon(new ImageIcon("material\\keai.gif"));
		keai.addActionListener(this);
		keai.addMouseListener(new MouseAdapter() { // 实现鼠标左击弹出菜单
			public void mousePressed(MouseEvent e) {
				out.println("expression|" + "keai.gif" + "|" + txtName.getText() + "|"
						+ listOnline.getSelectedItem().toString());
				inputText.setText("");
			}
		});

		ku = new JButton();
		ku.setToolTipText("哭");
		ku.setPreferredSize(new Dimension(32, 32));
		ku.setIcon(new ImageIcon("material\\ku.gif"));
		ku.addActionListener(this);
		ku.addMouseListener(new MouseAdapter() { // 实现鼠标左击弹出菜单
			public void mousePressed(MouseEvent e) {
				out.println("expression|" + "ku.gif" + "|" + txtName.getText() + "|"
						+ listOnline.getSelectedItem().toString());
				inputText.setText("");
			}
		});

		ExpressionMenu = new JPopupMenu();
		JPanel[] panel = new JPanel[3];
		panel[0] = new JPanel();
		panel[1] = new JPanel();
		panel[0].add(lenghan);
		panel[0].add(fanu);
		panel[0].add(zaijian);
		panel[1].add(keai);
		panel[1].add(weixiao);
		panel[1].add(ku);
		ExpressionMenu.add(panel[0]);
		ExpressionMenu.add(panel[1]);

		textArea = new JTextPane();
		textArea.setEditable(false);
		doc = textArea.getStyledDocument();

		cleanText = new JMenuItem("清屏");
		TextPopupMenu = new JPopupMenu();
		TextPopupMenu.add(cleanText);
		textArea.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				{
					TextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		cleanText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});

		butScreenCapture = new JButton("\u622A\u56FE");
		butScreenCapture.setForeground(new Color(255, 215, 0));
		butScreenCapture.setBackground(Color.BLACK);
		butScreenCapture.setBounds(347, 373, 72, 30);
		butScreenCapture.setFocusPainted(false);
		frame.getContentPane().add(butScreenCapture);
		butScreenCapture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new CaptureScreen();
			}
		});

		textArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		textArea.setBounds(193, 51, 467, 302);
		textArea.setForeground(Color.blue);
		frame.getContentPane().add(textArea);

		scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(193, 51, 467, 302);
		frame.getContentPane().add(scrollPane);

		listOnline = new JComboBox<Object>();
		listOnline.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		listOnline.setForeground(new Color(255, 215, 0));
		listOnline.setBackground(new Color(0, 0, 0));
		listOnline.setBounds(504, 373, 72, 30);
		listOnline.addItem("All");
		frame.getContentPane().add(listOnline);

		publicChatPanel = new JPanel();
		publicChatPanel.setBackground(new Color(255, 215, 0));
		publicChatPanel.setBorder(
				new TitledBorder(null, "\u7FA4\u804A\u533A", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		publicChatPanel.setBounds(183, 31, 488, 332);
		frame.getContentPane().add(publicChatPanel);

		editPanel = new JPanel();
		editPanel.setBorder(
				new TitledBorder(null, "\u7F16\u8F91\u533A", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		editPanel.setBackground(new Color(255, 215, 0));
		editPanel.setBounds(183, 408, 488, 53);
		frame.getContentPane().add(editPanel);

		lblTo = new JLabel("TO\uFF1A");
		lblTo.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		lblTo.setBounds(463, 366, 44, 42);
		frame.getContentPane().add(lblTo);

		JPanel panel_0 = new JPanel();
		panel_0.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel_0.setBackground(new Color(255, 215, 0));
		panel_0.setBounds(10, 10, 147, 458);
		frame.getContentPane().add(panel_0);

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(255, 215, 0));
		panel_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel_1.setBounds(163, 10, 528, 458);
		frame.getContentPane().add(panel_1);
		ShowLoginInterface();
	}

	/*
	 * 登录框
	 */
	public void ShowLoginInterface() {
		layout = new GridBagLayout();

		pnlLogin = new JPanel();
		pnlLogin.setLayout(layout);

		lblServerIP = new JLabel("服务器IP：");
		lblName = new JLabel("用户昵称：");
		lblPassword = new JLabel("用户密码：");
		txtServerIP = new JTextField(12);
		txtName = new JTextField(12);
		txtPassword = new JPasswordField(12);
		txtServerIP.setText("192.168.206.1");

		btnLogin = new JButton("登陆");
		btnLogin.setBackground(new Color(0, 0, 0));
		btnLogin.setForeground(new Color(255, 215, 0));
		btnLogin.setFont(new Font("微软雅黑", Font.BOLD, 13));
		btnLogin.setFocusPainted(false);

		btnReg = new JButton("注册");
		btnReg.setBackground(new Color(0, 0, 0));
		btnReg.setForeground(new Color(255, 215, 0));
		btnReg.setFont(new Font("微软雅黑", Font.BOLD, 13));
		btnReg.setFocusPainted(false);
		
		btnLogin.addActionListener(this);
		btnReg.addActionListener(this);

		dialogLogin.getContentPane().setLayout(new FlowLayout());
		dialogLogin.getContentPane().add(lblServerIP);
		dialogLogin.getContentPane().add(txtServerIP);
		dialogLogin.getContentPane().add(lblName);
		dialogLogin.getContentPane().add(txtName);
		dialogLogin.getContentPane().add(lblPassword);
		dialogLogin.getContentPane().add(txtPassword);
		dialogLogin.getContentPane().add(btnLogin);
		dialogLogin.getContentPane().add(btnReg);
		dialogLogin.setBounds(500, 300, 240, 157);
		dialogLogin.getContentPane().setBackground(new Color(255, 215, 0));
		dialogLogin.setVisible(true);
	}

	/*
	 * 注册框
	 */
	public void ShowRegInterface() {
		JPanel pnlReg;
		JLabel lblRegServerIP, lblRegName, lblRegPassword;
		JTextField txtRegServerIP, txtRegName;
		JPasswordField txtRegPassword;
		JButton btnToReg;
		GridBagLayout layout;
		JDialog frameReg = new JDialog(this, "MiniQQ注册", true);
		layout = new GridBagLayout();

		pnlReg = new JPanel();
		pnlReg.setLayout(layout);

		lblRegServerIP = new JLabel("服务器IP：");
		lblRegName = new JLabel("设置昵称：");
		lblRegPassword = new JLabel("设置密码：");
		txtRegServerIP = new JTextField(12);
		txtRegName = new JTextField(12);
		txtRegPassword = new JPasswordField(12);
		txtRegServerIP.setText("192.168.206.1");

		btnToReg = new JButton("注册");
		btnToReg.setBackground(new Color(0, 0, 0));
		btnToReg.setForeground(new Color(255, 215, 0));
		btnToReg.setFont(new Font("微软雅黑", Font.BOLD, 13));
		btnToReg.setFocusPainted(false);
		
		btnToReg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((txtRegName.getText().length() > 0)) {
					connectServer();
					strSend = "reg|" + txtRegName.getText() + "|" + String.valueOf(txtRegPassword.getPassword());
					out.println(strSend);
					System.out.println(strSend);
					try {
						initLogin();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				frameReg.setVisible(false);
			}
		});

		frameReg.getContentPane().setLayout(new FlowLayout());
		frameReg.getContentPane().add(lblRegServerIP);
		frameReg.getContentPane().add(txtRegServerIP);
		frameReg.getContentPane().add(lblRegName);
		frameReg.getContentPane().add(txtRegName);
		frameReg.getContentPane().add(lblRegPassword);
		frameReg.getContentPane().add(txtRegPassword);
		frameReg.getContentPane().add(btnToReg);
		frameReg.setBounds(500, 300, 240, 157);
		frameReg.getContentPane().setBackground(new Color(255, 215, 0));
		frameReg.setVisible(true);
	}

	// 建立与服务端通信的套接字
	void connectServer() {
		try {
			socket = new Socket(txtServerIP.getText(), 8888);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(this, "连接服务器失败!", "ERROE", JOptionPane.INFORMATION_MESSAGE);
			txtServerIP.setText("");
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// 弹出窗口
	public void popWindows(String strWarning, String strTitle) {
		JOptionPane.showMessageDialog(this, strWarning, strTitle, JOptionPane.INFORMATION_MESSAGE);
	}

	private void initLogin() throws IOException {
		strReceive = in.readLine();
		st = new StringTokenizer(strReceive, "|");
		strKey = st.nextToken();
		if (strKey.equals("login")) {
			strStatus = st.nextToken();
			if (strStatus.equals("succeed")) {
				btnLogin.setEnabled(false);
				butSend.setEnabled(true);
				pnlLogin.setVisible(false);
				dialogLogin.dispose();
				new ClientThread(socket);
				out.println("init|online");
			}
			popWindows(strKey + " " + strStatus + "!", "Login");
		}
		if (strKey.equals("warning")) {
			strStatus = st.nextToken();
			popWindows(strStatus, "Register");
		}
	}

	/*
	 * 按键设置
	 */
	public void actionPerformed(ActionEvent evt) {
		Object obj = evt.getSource();

		try {
			if (obj.equals(btnLogin)) {
				if ((txtServerIP.getText().length() > 0) && (txtName.getText().length() > 0)) {
					connectServer();
					strSend = "login|" + txtName.getText() + "|" + String.valueOf(txtPassword.getPassword());
					out.println(strSend);
					initLogin();
				} else {
					popWindows("请输入完整信息", "ERROE");
				}

			} else if (obj.equals(btnReg)) {
				ShowRegInterface();

			} else if (obj.equals(butSend)) {
				if (inputText.getText().length() > 0) {
					out.println("talk|" + inputText.getText() + "|" + txtName.getText() + "|"
							+ listOnline.getSelectedItem().toString());
					inputText.setText("");
				}

			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	class ClientThread implements Runnable {
		private Socket socket;
		private BufferedReader in;

		private String strReceive, strKey;
		private Thread threadTalk;
		private StringTokenizer st;

		public ClientThread(Socket s) throws IOException {
			this.socket = s;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			threadTalk = new Thread(this);
			threadTalk.start();
		}

		public void run() {

			while (true) {
				synchronized (this) {
					try {
						strReceive = in.readLine();
						System.out.println(strReceive);
						st = new StringTokenizer(strReceive, "|");
						strKey = st.nextToken();
						System.out.println(strKey);
						if (strKey.equals("talk")) {
							String strTalk = st.nextToken();
							try {
								doc.insertString(doc.getLength(), strTalk + "\n", null);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
						} else if (strKey.equals("expression")) {
							try {
								String strHead = st.nextToken();
								String strExpression = st.nextToken();
								File file = new File("material\\" + strExpression);
								doc.insertString(doc.getLength(), strHead + "\n", null);
								ClientTools.insertIcon(file, textArea, doc); // 插入表情
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (strKey.equals("printPort")) {
							try {
								printPort = Integer.parseInt(st.nextToken());
								printPort --; 
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (strKey.equals("sendPort")) {
							try {
//								sendPort = Integer.parseInt(st.nextToken());
//								sendPort++;
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (strKey.equals("picture")) {
							try {
								String strHead = st.nextToken();
								String strPicture = st.nextToken();
								String location = "F:\\pictures\\chat\\send" + System.currentTimeMillis() + ".jpg";
								ClientTools.drawPicture(strPicture, location);
								File file = new File(location);
								doc.insertString(doc.getLength(), strHead + "\n", null);
								ClientTools.insertIcon(file, textArea, doc); // 插入图片
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (strKey.equals("online")) {
							String strOnline = null;
							while (st.hasMoreTokens()) {
								strOnline = st.nextToken();
								if (ClientTools.position(userList, strOnline, onlineNum) == -1) {
									userList[onlineNum] = strOnline;
									onlineNum++;
									if (txtName.getText().equals(strOnline)) {
										listModel.addElement("[user]" + strOnline);
									} else {
										listOnline.addItem(strOnline);
										listModel.addElement(strOnline);
									}
								}
							}
						} else if (strKey.equals("remove")) {
							String strRemove;
							while (st.hasMoreTokens()) {
								strRemove = st.nextToken();
								if (ClientTools.position(userList, strRemove, onlineNum) >= 0) {
									listOnline.removeItem(strRemove);
									listModel.removeElement(strRemove);
									userList[ClientTools.position(userList, strRemove, onlineNum)] = userList[onlineNum
											- 1];
									onlineNum--;
								}
							}

						} else if (strKey.equals("warning")) {
							String strWarning = st.nextToken();
							popWindows(strWarning, "Warning");
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					} catch (IOException e) {
					}
				}
			}
		}
	}

	/**
	 * Main方法
	 */
	public static void ClientFrameStarter() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientFrame window = new ClientFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}