import java.awt.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class CTS implements Runnable
{
	Talker talker;
	JLabel replyLBL;
	Client client;

	public CTS(Talker t, Client c)//JLabel lbl)
	{
		talker = t;
		client = c;
		//replyLBL = lbl;
		//this.run();
		new Thread(this).start();
	}

	public void run()
	{
		boolean error;

		error = false;

		while(!error)
		{
			final String reply;
			final String[] sendReply;

			try
			{
				reply = talker.receive();
				System.out.println("MESSAGE RECEIVED IN CTS " + reply);

				sendReply = reply.split(":");

				//System.out.println("Size of reply in cts: " + sendReply.length);

				SwingUtilities.invokeLater(
					new Runnable()
					{
						public void run()
						{
							if(sendReply[0].equals("GOOD_LOGGING_IN") || sendReply[0].equals("BAD_CANT_LOG_IN") || sendReply[0].equals("BAD_CREDS"))
							{
								client.sendLoginReply(reply);
							}
							else if(sendReply[0].equals("FILE_TRANS_REQUEST"))
							{
								client.getFileTransferRequest(sendReply[2], sendReply[3], sendReply[4]);
								//who the file is from, filename, filelength
							}
							else if(sendReply[0].equals("ACCEPT_FILE"))
							{
								System.out.println("Inside accept_file of CTS");
								client.createSubClient();
							}
							else if(sendReply[0].equals("SENDING_FRIEND_LIST"))
							{
								//System.out.println("Inside sending friend list cts");
								client.addToFriendList(sendReply[1]);
							}
							else if(sendReply[0].equals("SENDING_ONLINE_LIST") || sendReply[0].equals("USER_ONLINE"))
							{
								System.out.println("inside sending online list");
								client.addToOnlineList(sendReply[1]);
							}
							else if(sendReply[0].equals("SENDING_ALERTS_LIST"))
							{
								//System.out.println("inside sending alerts list");
								System.out.println("Size of reply: " + sendReply.length);
								if(sendReply.length == 4)
									client.addToAlertList(sendReply[1] + ":" + sendReply[2]);
								else
									client.addToAlertList(sendReply[1] + ":" + sendReply[2] + ":" + sendReply[3]);
							}
							else if(sendReply[0].equals("SENDING_ALREADY_SENT_LIST"))
							{
								//System.out.println("Inside sending already sent list");
								client.addToAlreadySentList(sendReply[1]);
							}
							else if(sendReply[0].equals("FRIEND_ADDED"))
							{
								//System.out.println("Inside friend added");
								client.addToFriendList(sendReply[1]);
							}
							else if(sendReply[0].equals("SHOW_ALERTS"))
							{
								//System.out.println("showing alerts");
								client.showAlerts();
							}
							else if(sendReply[0].equals("PASS_MSG"))
							{
								//System.out.println("Inide pass msg");
								client.addToPM(sendReply[1], sendReply[2]);
							}
							else if(sendReply[0].equals("NO_FRIEND"))
							{
								//System.out.println("Inside no friend");
								client.setReplyInDialog("User not found");
								//JOptionPane.showMessageDialog(null, "User doesn't exist", "", JOptionPane.ERROR_MESSAGE);
							}
							else if(sendReply[0].equals("REQUEST_SENT"))
							{
								client.setReplyInDialog("Request sent");
								//System.out.println("name of friend added " +reply[2]);
								client.addToAlreadySentList(sendReply[1]);
							}
							else if(sendReply[0].equals("FRIEND_REQUEST"))
							{
								//System.out.println("Inside friend request");
								client.friendRequest(sendReply[1]);
							}
							else if(sendReply[0].equals("CLEAR_ALERTS"))
							{
								//System.out.println("Inside clear alerts");
								client.clearAlerts();
							}
							else if(sendReply[0].equals("CLEAR_ONLINE"))
							{
								System.out.println("Inside clear online");
								client.clearOnline();
							}
							else if(sendReply[0].equals("CLEAR_FRIENDS"))
							{
								System.out.println("inside clear friends");
								client.clearFriends();
							}
							else if(sendReply[0].equals("LOGGED_OUT"))
							{
								//System.out.println("inside logged out");
								client.quit();
							}
							else
							{
								//reply[1] = reply[0];
								//System.out.println(sendReply[0] + " " + sendReply[1]);
								//System.out.println("inside adding to chat window");
								client.addReplyToChatWindow(sendReply);
							}
							//System.out.println("Are we screwing up here?");
							//client.setReply(reply);
							//System.out.println("I have no clue");
						}
					}
				);//end of invokeLater
			}
			catch(Exception e)
			{
				error = true;
			}
		}

		if(error)
		{
			SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						client.serverError();
					}
				}
			);
		}
	}
}