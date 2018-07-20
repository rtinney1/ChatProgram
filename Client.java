import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.String;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.lang.*;
import java.util.*;
import java.util.Formatter;
import javax.swing.table.TableColumn;

public class Client extends JFrame implements ActionListener, MouseListener, ListSelectionListener
{
	MyDialog                    myDialog;

	String                      username;
	//String                      name;
	String                      reply;
	String                      replyFromSomeone;

	JTextField                  msgTF;
	JTextField                  usernameTF;
	JPasswordField              passwordTF;
	JLabel                      replyLBL;

	JButton                     registerBtn;
	JButton                     loginBtn;
	JButton                     exitBtn;
	JButton                     sendBtn;
	JButton                     addFriendBtn;
	JButton                     startChatBtn;

	Talker                      talker;
	CTS                         cts;

	boolean                     startUp;
	boolean                     chatWindowCreated;

	JTable                      chatTable;
	JScrollPane                 chatTableScrollPane;
	DefaultTableModel           chatTableModel;
	JTable                      friendTable;
	JScrollPane                 friendTableScrollPane;
	DefaultTableModel           friendTableModel;
	JTable                      pendingAlertsTable;
	JScrollPane                 pendingAlertsScrollPane;
	DefaultTableModel           pendingAlertsTableModel;

	JMenuItem                   alertsJMI;
	JMenuItem                   logoutJMI;

	ArrayList<String>           friendList;
	ArrayList<String>           onlineList;
	ArrayList<String>           alertsList;
	ArrayList<String>           alreadySentList;
	Hashtable<String, MyDialog> chattingToList;

	Container                   cp;
	Profile                     profile;
	File                        file;
	int[]                       rowSelected;

	public Client()
	{
		JLabel      usernameLBL;
		JLabel      passwordLBL;
		JPanel      btnPanel;
		JPanel      msgPanel;
		JPanel      replyPanel;
		GroupLayout groupLO;

		chatWindowCreated = false;

		file = null;

		startUp = true;

		usernameTF = new JTextField(10);
		usernameLBL = new JLabel("Username");

		passwordTF = new JPasswordField(10);
		passwordLBL = new JLabel("Password");

		replyLBL = new JLabel();

		registerBtn = new JButton("Register");
		registerBtn.addActionListener(this);
		registerBtn.setActionCommand("REG");

		loginBtn = new JButton("Login");
		loginBtn.addActionListener(this);
		loginBtn.setActionCommand("LOGIN");

		btnPanel = new JPanel();
		btnPanel.add(loginBtn);
		btnPanel.add(registerBtn);

		msgPanel = new JPanel();

		groupLO = new GroupLayout(msgPanel);
		msgPanel.setLayout(groupLO);

		GroupLayout.SequentialGroup hGroup = groupLO.createSequentialGroup();

		hGroup.addGroup(groupLO.createParallelGroup().
			addComponent(usernameLBL).
			addComponent(passwordLBL));
		hGroup.addGroup(groupLO.createParallelGroup().
			addComponent(usernameTF).addComponent(passwordTF));

		groupLO.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = groupLO.createSequentialGroup();

		vGroup.addGroup(groupLO.createParallelGroup(GroupLayout.Alignment.BASELINE).
			addComponent(usernameLBL).addComponent(usernameTF));
		vGroup.addGroup(groupLO.createParallelGroup(GroupLayout.Alignment.BASELINE).
			addComponent(passwordLBL).addComponent(passwordTF));

		groupLO.setVerticalGroup(vGroup);

		msgPanel.add(usernameLBL);
		msgPanel.add(usernameTF);
		msgPanel.add(passwordLBL);
		msgPanel.add(passwordTF);

		replyPanel = new JPanel();

		replyPanel.add(replyLBL);

		cp = getContentPane();
		cp.add(replyPanel, BorderLayout.NORTH);
		cp.add(btnPanel, BorderLayout.SOUTH);
		cp.add(msgPanel, BorderLayout.CENTER);

		usernameTF.requestFocus();

		this.getRootPane().setDefaultButton(loginBtn);

		setUpMainFrame();

		talker = new Talker("127.0.0.1", 1234, this);

		cts = new CTS(talker, this);//replyLBL);

		//System.out.println("Created client box");
	}

	public void actionPerformed(ActionEvent ae)
	{
		char[]   passArray;
		String   password;
		String   reply;
		String   sendMsg;
		String   user;
		MyDialog dialog;

		if(ae.getActionCommand().equals("REG"))
		{
			username = usernameTF.getText().trim();
			passArray = passwordTF.getPassword();
			password = new String(passArray);

			for(int i = 0; i < passArray.length; i++)
				passArray[i] = 0;

			try
			{
				if(username.equals(""))
					replyLBL.setText("Invalid Username");
				if(username.contains(":"))
					replyLBL.setText("Invalid Username");
				else if(password.equals(""))
					replyLBL.setText("Invalid Password");
				else if(password.contains(":"))
					replyLBL.setText("Invalid Password");
				else if(!username.equals("") && !password.equals(""))
				{
					sendMsg = "REGISTER:" + username;

					talker.send(sendMsg);

					sendMsg = "REGISTER:" + password;

					talker.send(sendMsg);
				}
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null, "Error", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
		}//end register
		else if(ae.getActionCommand().equals("LOGIN"))
		{
			username = usernameTF.getText().trim();
			passArray = passwordTF.getPassword();
			password = new String(passArray);

			for(int i = 0; i < passArray.length; i++)
				passArray[i] = 0;

			try
			{
				if(username.equals(""))
					replyLBL.setText("Invalid Username");
				else if(password.equals(""))
					replyLBL.setText("Invalid Password");
				else if(!username.equals("") && !password.equals(""))
				{
					sendMsg = "LOGIN:" + username;

					talker.send(sendMsg);

					sendMsg = "LOGIN:" + password;

					talker.send(sendMsg);
				}
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null, "Error", "ERROR", JOptionPane.ERROR_MESSAGE);
				  e.printStackTrace();
			}
		}//end login
		else if(ae.getActionCommand().equals("SEND"))
		{
			sendMsg = msgTF.getText().trim();
			msgTF.setText("");

			try
			{
				if(!sendMsg.equals(""))
				{
					sendMsg = "TOALL:" + sendMsg;

					//sendMsg[2] = sendMsg[1];

					talker.send(sendMsg);

					//this.addReplyToChatWindow(sendMsg);
				}
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null, "Error", "ERROR", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}//end send
		else if(ae.getActionCommand().equals("ADD"))
		{
			if(myDialog == null)
			{
				System.out.println("Clicked the add friend btn");
				myDialog = new MyDialog(this);
			}
		}
		else if(ae.getActionCommand().equals("START"))
		{
			if(friendTableModel.getValueAt(rowSelected[0], 1).equals("Online"))
			{
				user = (String)friendTableModel.getValueAt(rowSelected[0], 0);

				if(chattingToList.containsKey(user))
				{
					dialog = chattingToList.get(user);
					dialog.toFront();
				}
				else
				{
					chattingToList.put(user, new MyDialog(username, user, this));
				}
			}
			else
				JOptionPane.showMessageDialog(null, "Please wait for user to be online before starting chat.", "", JOptionPane.DEFAULT_OPTION);
		}
		else if(ae.getActionCommand().equals("ALERTS"))
		{
			if(myDialog == null)
			{
				System.out.println("Clicked the add friend btn");
				myDialog = new MyDialog(alertsList, this);
			}
		}
		else if(ae.getActionCommand().equals("LOGOUT"))
		{
			sendMsg = "LOGOUT:" + username;

			try
			{
				talker.send(sendMsg);
			}
			catch(Exception e)
			{
			}
		}
	}

	public void getRidOfDialog()
	{
		myDialog = null;
	}

	public void mouseClicked(MouseEvent e)
	{
		int    tableIndex;
		String user;
		MyDialog dialog;

		if(e.getButton() == MouseEvent.BUTTON1)
		{
			if(e.getClickCount() == 2)
			{
				tableIndex = friendTable.getSelectedRow();

				if(friendTableModel.getValueAt(tableIndex, 1).equals("Online"))
				{
					user = (String)friendTableModel.getValueAt(tableIndex, 0);

					if(chattingToList.containsKey(user))
					{
						dialog = chattingToList.get(user);
						dialog.toFront();
					}
					else
					{
						chattingToList.put(user, new MyDialog(username, user, this));
					}
				}
				else
					JOptionPane.showMessageDialog(null, "Please wait for user to be online before starting chat.", "", JOptionPane.DEFAULT_OPTION);
			}
		}
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	private void createChatWindow()
	{
		JPanel      mainPanel;
		JPanel      chatPanel;
		JPanel      friendPanel;
		JPanel      alertsPanel;
		TableColumn column;

		myDialog = null;

		friendList = new ArrayList<String>();
		onlineList = new ArrayList<String>();
		alertsList = new ArrayList<String>();
		alreadySentList = new ArrayList<String>();
		chattingToList = new Hashtable<String, MyDialog>();

		alertsJMI = newItem("Pending Alerts", "ALERTS", this, KeyEvent.VK_E, KeyEvent.VK_E, "See list of pending alerts");
		logoutJMI = newItem("Logout", "LOGOUT", this, KeyEvent.VK_E, KeyEvent.VK_E, "Logout of client");

		setJMenuBar(newMenuBar());

		chatTableModel = new DefaultTableModel(0, 2);
		chatTable = new JTable(chatTableModel);
		chatTableScrollPane = new JScrollPane(chatTable);

		chatTable.getColumnModel().getColumn(0).setHeaderValue("");
		chatTable.getColumnModel().getColumn(1).setHeaderValue("");

		chatTable.getColumnModel().getColumn(0).setPreferredWidth(10);

		chatTable.setDragEnabled(false);
		chatTable.setEnabled(false);

		chatPanel = new JPanel();
		chatPanel.add(chatTableScrollPane);
		chatPanel.setPreferredSize(new Dimension(500, 500));

		friendTableModel = new DefaultTableModel(0, 2)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		friendTable = new JTable(friendTableModel);
		friendTableScrollPane = new JScrollPane(friendTable);

		//friendTable.setColumnSelectionAllowed(false);
		//friendTable.setRowSelectionAllowed(false);

		friendTable.getColumnModel().getColumn(0).setHeaderValue("Friends");
		friendTable.getColumnModel().getColumn(1).setHeaderValue("List");

		friendTable.addMouseListener(this);
		friendTable.getSelectionModel().addListSelectionListener(this);

		addFriendBtn = new JButton("+Add Friend");
		addFriendBtn.addActionListener(this);
		addFriendBtn.setActionCommand("ADD");

		startChatBtn = new JButton("Start chat");
		startChatBtn.addActionListener(this);
		startChatBtn.setActionCommand("START");
		startChatBtn.setEnabled(false);

		friendPanel = new JPanel(new BorderLayout());
		friendPanel.add(addFriendBtn, BorderLayout.NORTH);
		friendPanel.add(friendTableScrollPane, BorderLayout.CENTER);
		friendPanel.add(startChatBtn, BorderLayout.SOUTH);
		friendPanel.setPreferredSize(new Dimension(200, 200));

		msgTF = new JTextField(10);

		sendBtn = new JButton("Send");
		sendBtn.addActionListener(this);
		sendBtn.setActionCommand("SEND");

		mainPanel = new JPanel();
		mainPanel.add(msgTF);
		mainPanel.add(sendBtn);

		cp.add(friendPanel, BorderLayout.WEST);
		cp.add(chatTableScrollPane, BorderLayout.CENTER);
		cp.add(mainPanel, BorderLayout.SOUTH);

		msgTF.requestFocus();

		this.getRootPane().setDefaultButton(sendBtn);

		this.revalidate();
		this.repaint();
		this.resetMainFrame();
		this.pack();
	}

	private JMenuBar newMenuBar()
	{
		JMenuBar menuBar;
		JMenu    subMenu;

		menuBar = new JMenuBar();

		subMenu = new JMenu("Alerts");
		subMenu.add(alertsJMI);

		menuBar.add(subMenu);

		subMenu = new JMenu("Logout");
		subMenu.add(logoutJMI);

		menuBar.add(subMenu);

		return menuBar;
	}//end newBar

	private JMenuItem newItem(String label, String actionCommand, ActionListener menuListener, int mnemonic, int keyEvent, String toolTip)
	{
		JMenuItem jmt;

		jmt = new JMenuItem(label, mnemonic);
		jmt.setAccelerator(KeyStroke.getKeyStroke(keyEvent, ActionEvent.ALT_MASK));
		jmt.getAccessibleContext().setAccessibleDescription(toolTip);
		jmt.setActionCommand(actionCommand);
		jmt.addActionListener(menuListener);

		return jmt;
	}//end newItem

	public void addToPM(String friendName, String msg)
	{
		MyDialog myDialog;

		if(chattingToList.containsKey(friendName))
		{
			myDialog = chattingToList.get(friendName);
			myDialog.addReply(friendName, msg);
		}
		else
		{
			myDialog = new MyDialog(username, friendName, this);
			chattingToList.put(friendName, myDialog);
			myDialog.addReply(friendName, msg);
		}
	}

	public void valueChanged(ListSelectionEvent lse)
	{
		rowSelected = friendTable.getSelectedRows();

		if(rowSelected == null)
		{
			startChatBtn.setEnabled(false);
		}
		else if(rowSelected.length > 1)
		{
			startChatBtn.setEnabled(false);
		}
		else
		{
			startChatBtn.setEnabled(true);
		}
	}

	public void closingPMWith(String friend)
	{
		chattingToList.remove(friend);
	}

	public void sendMsgToFriend(String friend, String msg)
	{
		String msgToPass;

		msgToPass = "SEND_MSG:" + friend + ":" + msg;

		try
		{
			talker.send(msgToPass);
		}
		catch(Exception e)
		{
		}
	}

	public void sendLoginReply(String reply)
	{
		System.out.println("Inside sendLoginReply");

		if(reply.startsWith("GOOD_LOGGING_IN"))
		{
			//System.out.println("Still good num2");
			cp.removeAll();
			//System.out.println("Still good num3");
			createChatWindow();
			//System.out.println("Still good num4");
			chatWindowCreated = true;
			//System.out.println("Still good num5");
			cts.talker.setId(username);
		}
		else if(reply.startsWith("BAD_CREDS"))
			replyLBL.setText("Username/Password incorrect");
		else
			replyLBL.setText("Username Not Available. Try Again.");
	}

	public void addToAlertList(String alert)
	{
		alertsList.add(alert);
	}

	public void addToAlreadySentList(String sent)
	{
		alreadySentList.add(sent);
	}

	public void addReplyToChatWindow(String[] reply)
	{
		String[]    setReply;
		JScrollBar  vertical;

		setReply = new String[2];

		vertical = chatTableScrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());

		//System.out.println(reply[0] + " " + reply[1]);
		//setReply[0] = reply[0];
		setReply[0] = reply[0];
		setReply[1] = reply[1];
		System.out.println("Trying to add to the chatTable msg " + setReply[0] + " " + setReply[1]);
		chatTableModel.addRow(setReply);
		this.repaint();
		//System.out.println("Finished adding to chatTable");
		//replyLBL.setText(reply);
	}

	public void addToFriendListWithReply(String username)
	{
		String setReply;

		this.addToFriendList(username);
		this.repaint();

		setReply = "FRIEND_ADDED:" + username;

		try
		{
			talker.send(setReply);
		}
		catch(Exception e)
		{
		}
	}

	public void addToFriendList(String username)
	{
		friendList.add(username);
		this.clearFriendListTable();
		this.setUpFriendList();
	}

	public void clearAlerts()
	{
		String replyMsg;

		alertsList.clear();

		replyMsg = "ALERTS_CLEARED";

		try
		{
			talker.send(replyMsg);
		}
		catch(Exception e)
		{
		}
	}

	public void clearOnline()
	{
		String replyMsg;

		onlineList.clear();

		System.out.println("Size of onlineList: " + onlineList.size());

		replyMsg = "ONLINE_CLEARED";

		try
		{
			talker.send(replyMsg);
		}
		catch(Exception e)
		{
			System.out.println("Error in clearing online on client");
		}
	}

	public void clearFriends()
	{
		String replyMsg;

		friendList.clear();

		replyMsg = "FRIENDS_CLEARED";

		try
		{
			talker.send(replyMsg);
		}
		catch(Exception e)
		{
		}
	}

	public void removeRequest(String username)
	{
		String setReply;

		System.out.println("alert to remove: " + username);
		alertsList.remove(username);

		setReply = "REMOVE_REQUEST:" + username;

		try
		{
			talker.send(setReply);
		}
		catch(Exception e)
		{
		}
	}

	public void showAlerts()
	{
		//System.out.println("HERE");
		String[] msg;

		msg = new String[3];

		msg[0] = "YOU HAVE " + alertsList.size();
		msg[1] = " PENDING ALERTS";

		this.addReplyToChatWindow(msg);
	}

	public void setReplyInDialog(String reply)
	{
		myDialog.setReply(reply);
	}

	public void friendRequest(String username)
	{
		String[] postRequest;

		postRequest = new String[2];

		postRequest[0] = "NEW FRIEND REQUEST FROM";
		postRequest[1] = username;

		this.addReplyToChatWindow(postRequest);
		//alertsList.add(username);
	}

	public void sendFriendRequest(String username)
	{
		String msg;

		msg = "ADD_FRIEND:" + username;

		if(!alreadySentList.contains(username))
		{
			if(friendList.contains(username))
				this.setReplyInDialog("You are already friends with this user");
			else
			{
				try
				{
					talker.send(msg);
				}
				catch(Exception e)
				{
				}//friendList.add(username);
			}
		}
		else
			this.setReplyInDialog("Already sent friend request to this user");
	}

	public void addToOnlineList(String username)
	{
		//System.out.println("updating online list");
		onlineList.add(username);
		if(friendList.size() > 0)
			this.updateFriendsList();
	}

	public void serverError()
	{
		JOptionPane.showMessageDialog(null, "Error with server. Logging out", "WARNING", JOptionPane.ERROR_MESSAGE);
		try
		{
			Thread.sleep(1000);
			this.dispose();
		}
		catch(Exception e)
		{
		}
	}

	private void updateFriendsList()
	{
		int rowCount;

		rowCount = friendTableModel.getRowCount();

		for(int i = 0; i < rowCount; i++)
		{
			friendTableModel.removeRow(0);
		}

		this.setUpFriendList();
	}

	public void sendFileTransferRequest(String owner, String friend, File file)
	{
		//if(file == null)
		//{
			this.file = file;

			try
			{
				talker.send("FILE_TRANS_REQUEST:" + owner + ":" + friend + ":" + file.getName() + ":" + file.length());
			}
			catch(Exception e)
			{
			}
		//}
		//else
		//	JOptionPane.showMessageDialog(null, "Please wait", "", JOptionPane.DEFAULT_OPTION);
	}

	public void getFileTransferRequest(String friendName, String fileName, String fileSize)
	{
		int returnVal;

		returnVal = JOptionPane.showConfirmDialog(null, "Would you like to accept file: " + fileName + " size: " + fileSize + " from " + friendName, "", JOptionPane.YES_NO_CANCEL_OPTION);

		if(returnVal == JOptionPane.YES_OPTION)
		{
			this.acceptFile(friendName, fileName, fileSize);
		}
		else
			this.rejectFile(friendName);
	}

	public void acceptFile(String friendName, String fileName, String fileSize)
	{
		String msg;

		msg = "ACCEPT_FILE:" + username + ":" + friendName;

		try
		{
			talker.send(msg);

			new SubServer(fileName, fileSize);
		}
		catch(Exception e)
		{
		}
	}

	public void rejectFile(String friendName)
	{
		this.sendMsgToFriend(friendName, "File rejected");
	}

	public void createSubClient()
	{
		new SubClient(file);
	}

	public void quit()
	{
		this.dispose();
	}

	private void setUpFriendList()
	{
		String[]    friendStatus;

		friendStatus = new String[3];

		//this.clearFriendListTable();

		//System.out.println("Size of friendList: " + friendList.size());
		for(String friend: friendList)
		{
			friendStatus[0] = friend;

			if(onlineList.contains(friend))
				friendStatus[1] = "Online";
			else
				friendStatus[1] = "Offline";

			friendTableModel.addRow(friendStatus);
			//System.out.println("frined added");
			this.repaint();
		}
	}

	public void clearFriendListTable()
	{
		int numRows;

		numRows = friendTableModel.getRowCount();

		for(int i = 0; i < numRows; i++)
			friendTableModel.removeRow(0);
	}

	void resetMainFrame()
	{
		Toolkit tk;
		Dimension d;

		tk = Toolkit.getDefaultToolkit();
		d = tk.getScreenSize();
		setSize(d.width/2, d.height/2);
	    setLocation(d.width/4, d.height/4);

	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	void setUpMainFrame()
	{
		Toolkit tk;
	    Dimension d;

	    tk = Toolkit.getDefaultToolkit();
	    d = tk.getScreenSize();
	    setSize(d.width/4, d.height/4);
	    setLocation(d.width/4, d.height/4);

	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    setTitle("Client");

	    setVisible(true);
    }//end setupMainFrame()
}


