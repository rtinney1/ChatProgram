import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Vector;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.lang.*;
import java.util.*;
import java.util.Formatter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

public class MyDialog extends JDialog implements ActionListener, MouseListener, DropTargetListener
{
	String            friend;
	Client            client;
	JButton           addBtn;
	JButton           closeBtn;
	JButton           sendBtn;
	JTextField        friendTF;
	JTextField        replyTF;
	JLabel            replyLBL;
	JTable            alertsTable;
	JScrollPane       alertsScrollPane;
	DefaultTableModel alertsTableModel;
	JTable            chatTable;
	JScrollPane       chatTableScrollPane;
	DefaultTableModel chatTableModel;
	String            ownersUsername;
	DropTarget        dropTarget;
	File              file;

	public MyDialog(Client c)
	{
		//System.out.println("Inside mydialog constructor");
		JPanel    replyPanel;
		JPanel    tfPanel;
		JPanel    btnPanel;
		JLabel    friendLBL;
		Container cp;

		client = c;

		ownersUsername = client.username;

		addBtn = new JButton("Add");
		addBtn.addActionListener(this);
		addBtn.setActionCommand("ADD");

		closeBtn = new JButton("Close");
		closeBtn.addActionListener(this);
		closeBtn.setActionCommand("CLOSE");

		replyLBL = new JLabel();

		friendTF = new JTextField(10);

		friendLBL = new JLabel("Enter Username");

		replyPanel = new JPanel();
		replyPanel.add(replyLBL);

		tfPanel = new JPanel();
		tfPanel.add(friendLBL);
		tfPanel.add(friendTF);

		btnPanel = new JPanel();
		btnPanel.add(addBtn);
		btnPanel.add(closeBtn);

		cp = getContentPane();
		cp.add(replyPanel, BorderLayout.NORTH);
		cp.add(tfPanel, BorderLayout.CENTER);
		cp.add(btnPanel, BorderLayout.SOUTH);

		friendTF.requestFocus();

		this.getRootPane().setDefaultButton(addBtn);

		this.setUpMainFrame();
		this.setTitle("Add Friend");
	}

	public MyDialog(ArrayList<String> alertList, Client c)
	{
		String[]  alerts;
		String[]  splitAlerts;
		Container cp;
		JPanel    btnPanel;

		client = c;

		ownersUsername = client.username;

		alertsTableModel = new DefaultTableModel(0, 2)
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		alertsTable = new JTable(alertsTableModel);
		alertsScrollPane = new JScrollPane(alertsTable);

		alertsTable.getColumnModel().getColumn(0).setHeaderValue("");
		alertsTable.getColumnModel().getColumn(1).setHeaderValue("");

		alertsTable.addMouseListener(this);

		alerts = new String[2];

		for(String msg: alertList)
		{
			System.out.println(msg);
			splitAlerts = msg.split(":");

			if(splitAlerts[0].equals("1"))
			{
				alerts[0] = "NEW FRIEND REQUEST";
				alerts[1] = splitAlerts[1];
			}
			else if(splitAlerts[0].equals("2"))
			{
				alerts[0] = "NEW MESSAGE FROM: " + splitAlerts[1];
				alerts[1] = splitAlerts[2];

				friend = splitAlerts[1];
			}

			alertsTableModel.addRow(alerts);
		}

		closeBtn = new JButton("Close");
		closeBtn.addActionListener(this);
		closeBtn.setActionCommand("CLOSE");

		btnPanel = new JPanel();
		btnPanel.add(closeBtn);

		cp = getContentPane();
		cp.add(alertsScrollPane, BorderLayout.CENTER);
		cp.add(btnPanel, BorderLayout.SOUTH);

		this.setUpMainFrame();
		this.setTitle("Alerts");
	}

	public MyDialog(String username, String name, Client c)
	{
		JPanel     replyPanel;
		Container  cp;

		client = c;
		friend = name;
		ownersUsername = username;

		chatTableModel = new DefaultTableModel(0, 2);
		chatTable = new JTable(chatTableModel);
		chatTableScrollPane = new JScrollPane(chatTable);

		chatTable.getColumnModel().getColumn(0).setHeaderValue("");
		chatTable.getColumnModel().getColumn(1).setHeaderValue("");

		chatTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		chatTable.setDefaultRenderer(chatTable.getColumnClass(0), new TableCellRenderer(ownersUsername, Color.RED));

		chatTable.setDragEnabled(false);
		chatTable.setEnabled(false);

		dropTarget = new DropTarget(chatTableScrollPane, this);

		replyTF = new JTextField(10);

		sendBtn = new JButton("Send");
		sendBtn.addActionListener(this);
		sendBtn.setActionCommand("SEND");

		closeBtn = new JButton("Close");
		closeBtn.addActionListener(this);
		closeBtn.setActionCommand("CLOSEPM");

		replyPanel = new JPanel();
		replyPanel.add(replyTF);
		replyPanel.add(sendBtn);
		replyPanel.add(closeBtn);

		cp = getContentPane();
		cp.add(chatTableScrollPane, BorderLayout.CENTER);
		cp.add(replyPanel, BorderLayout.SOUTH);


		this.getRootPane().setDefaultButton(sendBtn);

		this.setUpMainFrame();
		this.setTitle("Chatting with " + friend);
	}

	public void setReply(String reply)
	{
		replyLBL.setText(reply);
	}

	public void actionPerformed(ActionEvent ae)
	{
		String      user;
		String      msg;
		JScrollBar  vertical;

		if(ae.getActionCommand().equals("ADD"))
		{
			user = friendTF.getText().trim();

			System.out.println("friend to add: " + user);
			System.out.println("owners name: " + ownersUsername);
			if(user.equals(ownersUsername))
			{
				this.setReply("You cannot add yourself as a friend");
			}
			else
				client.sendFriendRequest(user);
		}
		else if(ae.getActionCommand().equals("CLOSE"))
		{
			client.getRidOfDialog();
			this.dispose();
		}
		else if(ae.getActionCommand().equals("CLOSEPM"))
		{
			client.closingPMWith(friend);
			this.dispose();
		}
		else if(ae.getActionCommand().equals("SEND"))
		{
			msg = replyTF.getText().trim();
			replyTF.setText("");

			if(!msg.equals(""))
			{
				this.addReply(ownersUsername, msg);
				vertical = chatTableScrollPane.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());

				client.sendMsgToFriend(friend, msg);
			}
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		int    tableIndex;
		String user;
		String instruction;
		int    returnVal;

		if(e.getButton() == MouseEvent.BUTTON1)
		{
			if(e.getClickCount() == 2)
			{
				tableIndex = alertsTable.getSelectedRow();
				instruction = (String)alertsTableModel.getValueAt(tableIndex, 0);
				user = (String)alertsTableModel.getValueAt(tableIndex, 1);

				if(instruction.equals("NEW FRIEND REQUEST"))
				{
					returnVal = JOptionPane.showConfirmDialog(null, "Accept friend request?", "", JOptionPane.YES_NO_CANCEL_OPTION);

					if(returnVal == JOptionPane.YES_OPTION)
					{
						client.clearFriendListTable();
						client.addToFriendListWithReply(user);
						client.removeRequest("1:" + user);
						alertsTableModel.removeRow(tableIndex);
						this.repaint();
					}
					else if(returnVal == JOptionPane.NO_OPTION)
					{
						client.removeRequest("1:" + user);
						alertsTableModel.removeRow(tableIndex);
						this.repaint();
					}
				}
				else
				{
					returnVal = JOptionPane.showConfirmDialog(null, "Remove alert?", "", JOptionPane.YES_NO_CANCEL_OPTION);

					if(returnVal == JOptionPane.YES_OPTION)
					{
						client.removeRequest("2:" + friend + ":" + user);
						alertsTableModel.removeRow(tableIndex);
						this.repaint();
					}
				}
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

	public void dragEnter(DropTargetDragEvent dtde)
	{}

	public void dragExit(DropTargetEvent dtde)
	{}

	public void dragOver(DropTargetDragEvent dtde)
	{}

	public void dropActionChanged(DropTargetDragEvent dtde)
	{}

	public void drop(DropTargetDropEvent dtde)
	{
		java.util.List<File> fileList;
		Transferable         transferableData;
		DataInputStream      dis;

		transferableData = dtde.getTransferable();

		try
		{
			if(transferableData.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);

				fileList = (java.util.List<File>)(transferableData.getTransferData(DataFlavor.javaFileListFlavor));

				if(fileList.size() == 1)
				{
					client.sendFileTransferRequest(ownersUsername, friend, fileList.get(0));
				}
				else
					JOptionPane.showMessageDialog(null, "Cannot send more than one file at a time", "Error", JOptionPane.ERROR_MESSAGE);

			}
			else
				JOptionPane.showMessageDialog(null, "Cannot open more than one file at a time", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(UnsupportedFlavorException ufe)
		{
			System.out.println("File list flavor not supported");
		}
		catch(IOException ioe)
		{
			System.out.println("I/O Exception");
		}
	}//end drop

	public void addReply(String username, String msg)
	{
		String[]   fullChat;
		JScrollBar vertical;

		vertical = chatTableScrollPane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());

		fullChat = new String[2];
		fullChat[0] = username;
		fullChat[1] = msg;

		chatTableModel.addRow(fullChat);
		this.repaint();
	}

	void setUpMainFrame()
	{
		Toolkit tk;
	    Dimension d;

	    tk = Toolkit.getDefaultToolkit();
	    d = tk.getScreenSize();
	    setSize(d.width/4, d.height/4);
	    setLocation(d.width/4, d.height/4);

	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

	    //setTitle("Add Friend");

	    setVisible(true);
    }//end setupMainFrame()
}