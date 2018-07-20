import java.net.*;
import java.util.*;
import java.util.Arrays;
import java.util.Collections;
import java.lang.*;
import java.io.*;

public class Server
{
	UserList          userList;
	ArrayList<String> onlineList;

	public Server()
	{
		ServerSocket     serverSocket;
		Socket           normalSocket;
		Talker           talker;
		boolean          loggedIn;
		DataInputStream  dis;
		//CTC          ctc;

		onlineList = new ArrayList<String>();

		try
		{
			try
			{
				dis = new DataInputStream(new FileInputStream("serverconfig.bin"));
				userList = new UserList(dis);
				System.out.println("created list in try");
			}
			catch(IOException ioe)
			{
				userList = new UserList();
				System.out.println("Created list in catch");
			}

			serverSocket = new ServerSocket(1234);
			serverSocket.setSoTimeout(1000);
			normalSocket = null;

			while(true)
			{
				loggedIn = false;
				try
				{
					normalSocket = serverSocket.accept();

					new CTC(normalSocket, this);
				}
				catch(SocketTimeoutException ste)
				{
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in server");
		}
	}

	public void addUser(Profile newProfile)
	{
		System.out.println("Inside addUser in server");
		userList.addUser(newProfile);
	}

	/*public boolean checkRetUserCreds(Profile retProfile)
	{
		Profile existingProfile;

		existingProfile = this.findUser(retProfile.username);

		System.out.println("got an existingProfile");

		if(existingProfile.username.equals("null") && existingProfile.password == null)
			return false;
		else
		{
			if(existingProfile.password.equals(retProfile.password))
				return true;
			else
				return false;
		}
	}*/

	public void userOnline(String user)
	{
		Enumeration<Profile> userE;
		ArrayList<Profile> profArray;

		onlineList.add(user);

		//System.out.println("added user to onlineList");
		if(onlineList.size() > 1)
		{
			userE = userList.elements();
			profArray = new ArrayList<Profile>();

			while(userE.hasMoreElements())
			{
				profArray.add(userE.nextElement());
			}
			//System.out.println("Number of users: " + profArray.size());
			for(int i = 0; i < profArray.size(); i++)
			{
				if(onlineList.contains(profArray.get(i).username))
				{
					if(profArray.get(i).friendList.contains(user))
					{
						profArray.get(i).ctc.userOnline(user);
					}
					//System.out.println("Sending this to: " + userE.nextElement().username);
					//profArray.get(i).ctc.clearOnline();
				}
				else
					System.out.println("I have received an error and I am stuck");
			}
		}
	}

	public void broadcast(String msg)
	{
		Enumeration<Profile> userE;

		userE = userList.elements();

		System.out.println("Msg to broadcast " + msg);
		//System.out.println("Trying to broadcast");

		while(userE.hasMoreElements())
		{
			//System.out.println("Looping");
			try
			{
				System.out.println("Sending");
				userE.nextElement().send(msg);
				//profile.ctc.talker.send(msg);
				System.out.println("Sent");
			}
			catch(Exception e)
			{
				System.out.println("error in broadcast");
			}
		}
		//System.out.println("Safe");
	}

	public void sendOnlineUpdate()
	{
		Enumeration<Profile> userE;
		ArrayList<Profile> profArray;

		userE = userList.elements();
		profArray = new ArrayList<Profile>();

		while(userE.hasMoreElements())
		{
			profArray.add(userE.nextElement());
		}

		//System.out.println("Number of users: " + profArray.size());

		for(int i = 0; i < profArray.size(); i++)
		{
			if(onlineList.contains(profArray.get(i).username))
			{
				//System.out.println("Sending this to: " + userE.nextElement().username);
				profArray.get(i).ctc.clearOnline();
			}
			else
				System.out.println("I have received an error and I am stuck");
		}
	}

	public Profile findUser(String name)
	{
		Profile foundProfile;

		foundProfile = userList.get(name);

		return foundProfile;
	}


	public ArrayList<String> getOnlineList(String user)
	{
		ArrayList<String> userCustomizedOnlineList;
		Profile           userProfile;

		userCustomizedOnlineList = new ArrayList<String>();

		userProfile = this.findUser(user);

		for(String friend: userProfile.friendList)
		{
			if(onlineList.contains(friend))
				userCustomizedOnlineList.add(friend);
		}

		return userCustomizedOnlineList;
	}

	public void loggingOut(String user)
	{
		Profile profile;

		onlineList.remove(user);
		System.out.println(user + " removed from onlineList");
		profile = userList.get(user);

		profile.ctc = null;

		if(onlineList.size() != 0)
			this.sendOnlineUpdate();
	}

	public void passOnFileTransferRequest(String sender, String destination, String fileName, String size)
	{
		Profile destinationProfile;
		String  msg;

		destinationProfile = this.findUser(destination);

		msg = "FILE_TRANS_REQUEST:" + destination + ":" + sender + ":" + fileName + ":" + size;

		try
		{
			destinationProfile.ctc.talker.send(msg);
		}
		catch(Exception e)
		{
		}
	}


	public void passOnFriendRequest(String user, String friend)
	{
		Profile friendProfile;
		Profile sendingRequestProfile;
		String confirm;
		String sendingBackConfirm;

		System.out.println("User requesting friend " + user);
		System.out.println("The friends name " + friend);
		friendProfile = this.findUser(friend);
		sendingRequestProfile = this.findUser(user);

		if(friendProfile != null)
		{
			System.out.println("Found friend");
			confirm = "FRIEND_REQUEST:" + user + ":wants to add you as a friend";

			sendingBackConfirm = "REQUEST_SENT:" + friend;

			friendProfile.addAlert("1:" + user);
			sendingRequestProfile.addToSentList(friend);
			userList.store();
			try
			{
				friendProfile.ctc.talker.send(confirm);
			}
			catch(Exception e)
			{
			}
		}
		else
		{
			System.out.println("Friend not found");
			sendingBackConfirm = "NO_FRIEND";
		}

		try
		{
			sendingRequestProfile.ctc.talker.send(sendingBackConfirm);
			System.out.println("Sent msg to sender");
		}
		catch(Exception e)
		{
			System.out.println("Error in sending friend request in server");
		}
	}

	public void acceptFile(String fileReceiver, String fileTransferer)
	{
		Profile fileTransfererProfile;
		String  msg;

		fileTransfererProfile = this.findUser(fileTransferer);

		msg = "ACCEPT_FILE:" + fileReceiver;

		try
		{
			fileTransfererProfile.ctc.talker.send(msg);
		}
		catch(Exception e)
		{
		}
	}

	public void passMsg(String destination, String returnAddress, String message)
	{
		Profile  destinationProfile;
		String msg;

		destinationProfile = this.findUser(destination);

		msg = "PASS_MSG:" + returnAddress + ":" + message;
		if(destinationProfile.ctc != null)
		{
			try
			{
				destinationProfile.ctc.talker.send(msg);
			}
			catch(Exception e)
			{
			}
		}
		else
		{
			System.out.println("Adding alert from msg");
			destinationProfile.addAlert("2:" + returnAddress + ":" + message);
		}
	}

	public void addFriend(String friend1, String friend2)
	{
		Profile  profile1;
		Profile  profile2;
		String setReply;

		profile1 = this.findUser(friend1);
		profile2 = this.findUser(friend2);

		setReply = "CLEAR_FRIENDS";

		profile1.addFriend(friend2);
		profile2.addFriend(friend1);
		userList.store();

		try
		{
			setReply = setReply + ":" + friend2;
			profile1.ctc.talker.send(setReply);
			profile1.ctc.updateOnlineList(friend1);
			setReply = setReply + ":" + friend1;
			profile2.ctc.updateOnlineList(friend2);
			//profile2.ctc.talker.send(setReply);
		}
		catch(Exception e)
		{
		}
	}

	public void removeAlert(String user, String alert)
	{
		Profile profile;

		profile = this.findUser(user);
		System.out.println("Alert to remove for user: " + user + " " + alert);
		profile.removeAlert(alert);
		userList.store();
	}
}