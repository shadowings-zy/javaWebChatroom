package Server;

/*
 * 类名：ServerProcess
 * 描述：接收到客户端socket发来的信息后进行解析、处理、转发。
 */
import java.io.*;
import java.net.*;
import java.util.*;

class ServerProcess extends Thread {
	private Socket socket = null;// 定义客户端套接字

	private BufferedReader in;// 定义输入流
	private PrintWriter out;// 定义输出流

	@SuppressWarnings("rawtypes")
	private static Vector onlineUser = new Vector(10, 5);
	@SuppressWarnings("rawtypes")
	private static Vector socketUser = new Vector(10, 5);

	private String strReceive, strKey;
	private StringTokenizer st;

	private final String USERLIST_FILE = "resourse\\user.txt"; // 设定存放用户信息的文件
	private ServerFrame sFrame = null;

	public ServerProcess(Socket client, ServerFrame frame) throws IOException {
		socket = client;
		sFrame = frame;

		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); // 客户端接收
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);// 客户端输出
		this.start();
	}

	public void run() {
		try {
			while (true) {
				strReceive = in.readLine();// 从服务器端接收一条信息后拆分、解析，并执行相应操作
				st = new StringTokenizer(strReceive, "|");
				strKey = st.nextToken();
				if (strKey.equals("login")) {
					login();
				} else if (strKey.equals("talk")) {
					talk();
				} else if (strKey.equals("init")) {
					freshClientsOnline();
				} else if (strKey.equals("reg")) {
					register();
				} else if (strKey.equals("expression")) {
					sendExpression();
				} else if (strKey.equals("picture")) {
					sendPicture();
				} else if (strKey.equals("printPort")) {
					print();
				} else if (strKey.equals("sendPort")) {
					sendFile();
				}

			}
		} catch (IOException e) { // 用户关闭客户端造成此异常，关闭该用户套接字。
			String leaveUser = closeSocket();
			log("用户 " + leaveUser + " 已经退出。" );
			try {
				freshClientsOnline();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("[SYSTEM] " + leaveUser + " leave chatroom!");
			sendAll("talk|[系统]" + leaveUser + "离开了聊天室");
		}
	}

	// 判断是否有该注册用户
	@SuppressWarnings({ "resource", "deprecation" })
	private boolean isExistUser(String name) {
		String strRead;
		try {
			FileInputStream inputfile = new FileInputStream(USERLIST_FILE);
			DataInputStream inputdata = new DataInputStream(inputfile);
			while ((strRead = inputdata.readLine()) != null) {
				StringTokenizer stUser = new StringTokenizer(strRead, "|");
				if (stUser.nextToken().equals(name)) {
					return true;
				}
			}
		} catch (FileNotFoundException fn) {
			System.out.println("[ERROR] User File has not exist!" + fn);
			out.println("warning|读写文件时出错!");
		} catch (IOException ie) {
			System.out.println("[ERROR] " + ie);
			out.println("warning|读写文件时出错!");
		}
		return false;
	}

	// 判断用户的用户名密码是否正确
	@SuppressWarnings("deprecation")
	private boolean isUserLogin(String name, String password) {
		String strRead;
		try {
			FileInputStream inputfile = new FileInputStream(USERLIST_FILE);
			@SuppressWarnings("resource")
			DataInputStream inputdata = new DataInputStream(inputfile);
			while ((strRead = inputdata.readLine()) != null) {
				if (strRead.equals(name + "|" + password)) {
					return true;
				}
			}
		} catch (FileNotFoundException fn) {
			System.out.println("[ERROR] User File has not exist!" + fn);
			out.println("warning|读写文件时出错!");
		} catch (IOException ie) {
			System.out.println("[ERROR] " + ie);
			out.println("warning|读写文件时出错!");
		}
		return false;
	}

	// 用户注册
	private void register() throws IOException {
		String name = st.nextToken(); // 得到用户名称
		String password = st.nextToken().trim();// 得到用户密码

		if (isExistUser(name)) {
			System.out.println("[ERROR] " + name + " Register fail!");
			out.println("warning|该用户已存在，请改名!");
		} else {
			@SuppressWarnings("resource")
			RandomAccessFile userFile = new RandomAccessFile(USERLIST_FILE, "rw");
			userFile.seek(userFile.length()); // 在文件尾部加入新用户信息
			userFile.writeBytes(name + "|" + password + "\r\n");
			log("用户 " + name + " 注册成功。" );
			userLoginSuccess(name); // 自动登陆聊天室
		}
	}

	// 用户登陆(从登陆框直接登陆)
	private void login() throws IOException {
		String name = st.nextToken(); // 得到用户名称
		String password = st.nextToken().trim();// 得到用户密码
		boolean succeed = false;

		log("用户 " + name + " 正在登陆..." + "\n" + "密码 : " + password );
		System.out.println("[USER LOGIN] " + name + ":" + password + ":" + socket);

		for (int i = 0; i < onlineUser.size(); i++) {
			if (onlineUser.elementAt(i).equals(name)) {
				System.out.println("[ERROR] " + name + " is logined!");
				out.println("warning|" + name + "已经登陆聊天室");
			}
		}
		if (isUserLogin(name, password)) { // 判断用户名和密码
			userLoginSuccess(name);
			succeed = true;
		}
		if (!succeed) {
			out.println("warning|" + name + "登陆失败，请检查您的输入!");
			log("用户 " + name + " 登陆失败！");
			System.out.println("[SYSTEM] " + name + " login fail!");
		}
	}

	// 用户登陆
	@SuppressWarnings({ "unchecked", "deprecation" })
	private void userLoginSuccess(String name) throws IOException {
		Date t = new Date();
		out.println("login|succeed");
		sendAll("online|" + name);

		onlineUser.addElement(name);
		socketUser.addElement(socket);

		log("用户 " + name + " 登录成功， " + "\n登录时间:" + t.toLocaleString());

		freshClientsOnline();
		sendAll("talk|[系统]欢迎" + name + "来到聊天室");
		System.out.println("[SYSTEM] " + name + " login succeed!");
	}

	// 聊天信息处理
	private void talk() throws IOException {
		String strTalkInfo = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[TALK_" + strReceiver + "] " + strTalkInfo);
		Socket socketSend;
		PrintWriter outSend;

		// 得到当前时间
		String strTime = getTime();

		log("用户 " + strSender + " 对  " + strReceiver + " 说: " + strTalkInfo);

		if (strReceiver.equals("All")) {
			sendAll("talk|" + strSender + " " + strTime + " : " + strTalkInfo);
		} else {
			if (strSender.equals(strReceiver)) {
				out.println("talk|[系统]不可以自言自语！");
			} else {
				for (int i = 0; i < onlineUser.size(); i++) {
					if (strReceiver.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println("talk|[私聊]" + strSender + " " + strTime + "：" + strTalkInfo);
					} else if (strSender.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println("talk|[私聊]" + strReceiver + " " + strTime + "：" + strTalkInfo);
					}
				}
			}
		}
	}
	
	//获取时间
	private String getTime(){
		GregorianCalendar calendar = new GregorianCalendar();
		int hour = calendar.get(Calendar.HOUR);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		
		String strHour = "",strMin = "",strSec = "";
		
		if(hour>0&&hour<10){
			strHour = "0" + hour;
		}else if(hour==0){
			hour = hour +12;
			strHour = "" + hour;
		}
		
		if(min<10){
			strMin = "0" + min;
		}else{
			strMin = min + "";
		}
		
		if(sec<10){
			strSec = "0" + sec;
		}else{
			strSec = sec + "";
		}
		String strTime = "(" + strHour + ":" + strMin + ":" + strSec + ")";
		return strTime;
	}

	// 发送表情
	private void sendExpression() throws IOException {
		String strExpression = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[SendExpression_" + strReceiver + "] " + strExpression);
		Socket socketSend;
		PrintWriter outSend;
		new Date();

		// 得到当前时间
		String strTime = getTime();

		log("用户 " + strSender + " 对  " + strReceiver + " 说: " + strExpression);

		if (strReceiver.equals("All")) {
			sendAll("expression|" + strSender + " " + strTime + " : " + "|" + strExpression + "|");
		} else {
			if (strSender.equals(strReceiver)) {
				out.println("talk|[系统]不可以自言自语！");
			} else {
				for (int i = 0; i < onlineUser.size(); i++) {
					if (strReceiver.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println(
								"expression|[私聊]" + strSender + " " + strTime + "：" + "|" + strExpression + "|");
					} else if (strSender.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println(
								"expression|[私聊]" + strReceiver + " " + strTime + "：" + "|" + strExpression + "|");
					}
				}
			}
		}
	}

	// 发送图片
	private void sendPicture() throws IOException {
		String strPicture = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[SendPicture_" + strReceiver + "] 发送图片");
		Socket socketSend;
		PrintWriter outSend;
		new Date();

		// 得到当前时间
		String strTime = getTime();

		log("用户 " + strSender + " 对  " + strReceiver + " 发送了图片 ");

		if (strReceiver.equals("All")) {
			sendAll("picture|" + strSender + " " + strTime + " : " + "|" + strPicture + "|");
		} else {
			if (strSender.equals(strReceiver)) {
				out.println("talk|[系统]不可以自言自语！");
			} else {
				for (int i = 0; i < onlineUser.size(); i++) {
					if (strReceiver.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println("picture|[私聊]" + strSender + " " + strTime + "：" + "|" + strPicture + "|");
					} else if (strSender.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
						outSend.println("picture|[私聊]" + strReceiver + " " + strTime + "：" + "|" + strPicture + "|");
					}
				}
			}
		}
	}

	// 在线用户列表
	@SuppressWarnings("unchecked")
	private void freshClientsOnline() throws IOException {
		String strOnline = "online";
		String[] userList = new String[20];
		String useName = null;

		for (int i = 0; i < onlineUser.size(); i++) {
			strOnline += "|" + onlineUser.elementAt(i);
			useName = " " + onlineUser.elementAt(i);
			userList[i] = useName;
		}

		sFrame.txtNumber.setText("" + onlineUser.size());
		sFrame.lstUser.setListData(userList);
		System.out.println(strOnline);
		out.println(strOnline);
	}

	// 信息群发
	private void sendAll(String strSend) {
		Socket socketSend;
		PrintWriter outSend;
		try {
			for (int i = 0; i < socketUser.size(); i++) {
				socketSend = (Socket) socketUser.elementAt(i);
				outSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())),
						true);
				outSend.println(strSend);
			}
		} catch (IOException e) {
			System.out.println("[ERROR] send all fail!");
		}
	}

	public void log(String log) {
		String newlog = sFrame.taLog.getText() + "\n" + log;
		sFrame.taLog.setText(newlog);
	}

	// 即时画图端口信息处理
	private void print() throws IOException {
		String strTalkInfo = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[PRINT_" + strReceiver + "] " + strTalkInfo);
		log("用户 " + strSender + " 对  " + strReceiver + " 说: " + strTalkInfo);

		if (strReceiver.equals("All")) {
			sendAll("printPort|" + strTalkInfo);
		} else {
				out.println("talk|[系统]即时画图出现错误！");
		}
	}
		
	// 传送文件端口信息处理
	private void sendFile() throws IOException {
		String strTalkInfo = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[PRINT_" + strReceiver + "] " + strTalkInfo);
		log("用户 " + strSender + " 对  " + strReceiver + " 说: " + strTalkInfo);

		if (strReceiver.equals("All")) {
			sendAll("sendPort|" + strTalkInfo);
		} else {
				out.println("talk|[系统]即时画图出现错误！");
		}
	}
	
	// 关闭套接字，并将用户信息从在线列表中删除
	private String closeSocket() {
		String strUser = "";
		for (int i = 0; i < socketUser.size(); i++) {
			if (socket.equals((Socket) socketUser.elementAt(i))) {
				strUser = onlineUser.elementAt(i).toString();
				socketUser.removeElementAt(i);
				onlineUser.removeElementAt(i);
				try {
					freshClientsOnline();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sendAll("remove|" + strUser);
			}
		}
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("[ERROR] " + e);
		}

		return strUser;
	}
}