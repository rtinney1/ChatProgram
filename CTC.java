import java.net.*;
import java.util.*;
import java.util.Arrays;
import java.util.Collections;
import java.lang.*;

public class CTC implements Runnable
{
	Talker            talker;
	Server            chatServer;
	Socket            normalSocket;
	Profile           currentProfile;
	//ArrayList<CTC> currentUserList;

	//public CTC()
	//{
	//	normalSocket = null;
	//	this.run();
	//}

	public CTC(Socket socket, Server s)
	{
		normalSocket = socket;
		chatServer = s;
		talker = new Talker(normalSocket);

		//currentUserList = new ArrayList<CTC>();
		new Thread(this).start();
	}

	public void run()
	{
		String[]            username;
		String[]            password;
		String              getCreds;
		String[]            reply;
		String              replyMsg;
		boolean             loggedIn;
		boolean             stillLoggedIn;
		Profile             friendProfile;

		username = null;
		reply = null;

		try
		{
			loggedIn = false;

			currentProfile = null;

			while(!loggedIn)
			{
				getCreds = talker.receive();

				System.out.println("Message received in CTC " + getCreds);
				username = getCreds.split(":");

				System.out.println("Size of username: " + username.length);

				getCreds = talker.receive();
				password = getCreds.split(":");

				currentProfile = chatServer.findUser(username[1]);

				if(username[0].equals("REGISTER"))
				{
					//System.out.println("Trying to register in");

					if(currentProfile != null)
					{
						//System.out.println("Inside false outcome");
						replyMsg = "BAD_CANT_LOG_IN";
						talker.send(replyMsg);
					}
					else
					{
						currentProfile = new Profile(username[1], password[1], this);
						loggedIn = true;
						chatServer.addUser(currentProfile);
						//System.out.println("Inside true outcome");
						replyMsg = "GOOD_LOGGING_IN";
						talker.send(replyMsg);
						//System.out.println("Reply sent");
					}
				}
				else if(username[0].equals("LOGIN"))
				{
					if(currentProfile == null)
					{
						replyMsg = "BAD_CREDS";
						talker.send(replyMsg);
					}
					else
					{
						if(!chatServer.onlineList.contains(currentProfile.username))
						{
							if(currentProfile.password.equals(password[1]))
							{
								replyMsg = "GOOD_LOGGING_IN";
								currentProfile.setCTC(this);
								loggedIn = true;
								talker.send(replyMsg);
								//System.out.println("Reply sent");
							}
							else
							{
								//System.out.println("Bad password");
								replyMsg = "BAD_CREDS";
								talker.send(replyMsg);
							}
						}
						else
						{
							//System.out.println("Already logged in");
							replyMsg = "BAD_CREDS";
							talker.send(replyMsg);
						}
					}
				}
			}//end first while loop

			//System.out.println("Outside first while loop in CTC");
			talker.setId(username[1]);
			//System.out.println("Setting CTC talker id");
			chatServer.userOnline(username[1]);
			//System.out.println("Telling chatServer that user is online");
			this.updateFriends();
			//System.out.println("Updating friends");
			this.updateAlerts();
			//System.out.println("Updating alerts");
			this.updateOnlineList(talker.owner);
			//System.out.println("Sending onlinelist");
			this.updateSentFriendRequestList();
			//System.out.println("Updating sentFriendRequestList");
			//System.out.println("Done and Welcome");
			replyMsg = "WELCOME TO CHATROOM";
			talker.send(replyMsg);

			//System.out.println("About to show messages");
			replyMsg = "SHOW_ALERTS";

			talker.send(replyMsg);
			//System.out.println("Messages shown");
		}
		catch(Exception e)
		{
		}

		//System.out.println("About to go into never ending loop");
		stillLoggedIn = true;

		while(stillLoggedIn)
		{
			try
			{
				replyMsg = talker.receive();

				//System.out.println("MESSAGE RECEIVED IN CTC " + replyMsg);

				reply = replyMsg.split(":");

				//System.out.println("Size of reply in CTC: " + reply.length);

				if(reply[0].equals("TOALL"))
				{
					//System.out.println("IN TOALL");
					//System.out.println("Msg to send " + reply[2] + " " + reply[1]);
					chatServer.broadcast(reply[2] + ":" + reply[1]);
				}
				else if(reply[0].equals("ADD_FRIEND"))
				{
					//System.out.println("IN ADD_FRIEND");
					//System.out.println("Messaging beings sent to passOnFriendRequest " + reply[2] + " " + reply[1]);
					chatServer.passOnFriendRequest(reply[2], reply[1]);
				}
				else if(reply[0].equals("LOGOUT"))
				{
					//System.out.println("IN LOGOUT");
					stillLoggedIn = false;
				}
				else if(reply[0].equals("ACCEPT_FILE"))
				{
					chatServer.acceptFile(reply[1], reply[2]);
				}
				else if(reply[0].startsWith("FRIEND_ADDED"))
				{
					//System.out.println("IN FRIEND_ADDED");
					chatServer.addFriend(reply[1], reply[2]);
				}
				else if(reply[0].equals("FILE_TRANS_REQUEST"))
				{
					chatServer.passOnFileTransferRequest(reply[1], reply[2], reply[3], reply[4]);
					//sender, destination, filename, filelength
				}
				else if(reply[0].equals("SEND_MSG"))
				{
					chatServer.passMsg(reply[1], reply[3], reply[2]);
				}
				else if(reply[0].equals("REMOVE_REQUEST"))
				{
					//System.out.println("IN REMOVE_REQUEST");
					if(reply.length == 4)
						chatServer.removeAlert(reply[3], reply[1] + ":" + reply[2]);
					else
						chatServer.removeAlert(reply[4], reply[1] + ":" + reply[2] + ":" + reply[3]);
				}
				else if(reply[0].equals("ALERTS_CLEARED"))
				{
					//System.out.println("IN ALERTS_CLEARED");
					this.updateAlerts();
				}
				else if(reply[0].equals("FRIENDS_CLEARED"))
				{
					//System.out.println("IN FRIENDS_CLEARED");
					this.updateFriends();
				}
				else if(reply[0].equals("ONLINE_CLEARED"))
				{
					//System.out.println("IN ONLINE_CLEARED");
					this.updateOnlineList(talker.owner);
				}
			}
			catch(Exception e)
			{
				chatServer.loggingOut(talker.owner);
				stillLoggedIn = false;
			}
		}

		//System.out.println("Outside of while loop");
		try
		{
			replyMsg = "LOGGED_OUT";
			talker.send(replyMsg);
			chatServer.loggingOut(reply[1]);
		}
		catch(Exception e)
		{
		}
	}//end run

	public void clearAlerts()
	{
		String replyMsg;

		replyMsg = "CLEAR_ALERTS";

		try
		{
			talker.send(replyMsg);
		}
		catch(Exception e)
		{
		}
	}

	public void clearFriends()
	{
		String replyMsg;

		replyMsg = "CLEAR_FRIENDS";

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

		replyMsg = "CLEAR_ONLINE";

		System.out.println("Clearing online");
		try
		{
			talker.send(replyMsg);
			System.out.println("online cleared");
		}
		catch(Exception e)
		{
			System.out.println("Error in sending msg in clearOnline");
		}
	}

	public void updateAlerts()
	{
		String replyMsg;
		String command;

		command = "SENDING_ALERTS_LIST";

		//System.out.println("Sending alerts list");
		try
		{
			for(String alert: currentProfile.pendingAlerts)
			{
				replyMsg = command + ":" + alert;
				talker.send(replyMsg);
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in update alerts");
		}
	}

	public void updateSentFriendRequestList()
	{
		String replyMsg;

		replyMsg = "SENDING_ALREADY_SENT_LIST";
		//System.out.println("Sending already sent list");
		try
		{
			for(String sent: currentProfile.sentFriendRequest)
			{
				replyMsg = replyMsg + ":" + sent;
				talker.send(replyMsg);
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in updateFriendRequest");
		}
	}

	public void updateFriends()
	{
		String replyMsg;
		String instruction;
		int friendListSize;

		instruction = "SENDING_FRIEND_LIST";
		//System.out.println("updating friends");
		try
		{
			friendListSize = currentProfile.friendList.size();

			//System.out.println("Size of friends: " + friendListSize);
			for(int i = 0; i < friendListSize; i++)
			{
				replyMsg = instruction + ":" + currentProfile.friendList.get(i);;
				talker.send(replyMsg);
				//System.out.println("friend sent");
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in update friends");
		}
	}

	public void userOnline(String user)
	{
		String replyMsg;

		replyMsg = "USER_ONLINE:" + user;

		try
		{
			talker.send(replyMsg);
		}
		catch(Exception e)
		{
		}
	}

	public void updateOnlineList(String currentUser)
	{
		String            replyMsg;
		String            instruction;
		ArrayList<String> currOnlineList;

		instruction = "SENDING_ONLINE_LIST";
		//replyMsg[2] = "";

		//System.out.println("sending online list");
		currOnlineList = chatServer.getOnlineList(currentUser);

		//System.out.println("size of online list: " + currOnlineList.size());
		try
		{
			if(currOnlineList.size() > 0)
			{
				for(String user: currOnlineList)
				{
					replyMsg = instruction + ":" + user;
					//System.out.println(replyMsg[0] + " " + replyMsg[1]);
					talker.send(replyMsg);
				}
			}
			else
				talker.send(instruction + ":" );
			System.out.println("Finished updatingOnlineList");
		}
		catch(Exception e)
		{
			System.out.println("Error in update online");
		}
	}

	public void send(String msg)
	{
		try
		{
			//System.out.println("Sending in CTC");
			talker.send(msg);
		}
		catch(Exception e)
		{
		}
	}
}