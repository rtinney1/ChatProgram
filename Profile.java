import java.util.*;
import java.util.Arrays;
import java.util.Collections;
import java.lang.*;
import java.io.*;

public class Profile
{
	String username;
	String password;
	ArrayList<String> friendList;
	ArrayList<String> pendingAlerts;
	ArrayList<String> sentFriendRequest;
	CTC ctc;

	public Profile(DataInputStream dis)
	{
		int listSize;

		friendList = new ArrayList<String>();
		pendingAlerts = new ArrayList<String>();
		sentFriendRequest = new ArrayList<String>();

		try
		{
			username = dis.readUTF();
			password = dis.readUTF();

			listSize = dis.readInt(); //for friends
			//System.out.println("Size of friend list: " + listSize);

			for(int i = 0; i < listSize; i++)
			{
				friendList.add(dis.readUTF());
			}

			listSize = dis.readInt(); //for alerts

			//System.out.println("Size of pendingAlerts: " + listSize);

			for(int i = 0; i < listSize; i++)
			{
				pendingAlerts.add(dis.readUTF());
			}

			listSize = dis.readInt();

			for(int i = 0; i < listSize; i++)
			{
				sentFriendRequest.add(dis.readUTF());
			}

			//System.out.println("Hereinprofile4");
		}
		catch(Exception e)
		{
		}
	}

	public Profile(String user, String pass, CTC ctc)
	{
		username = user;
		password = pass;
		this.ctc = ctc;

		friendList = new ArrayList<String>();
		pendingAlerts = new ArrayList<String>();
		sentFriendRequest = new ArrayList<String>();
	}

	public Profile(String user)
	{
		username = user;
		password = null;
	}

	public void send(String msg)
	{
		//System.out.println("sending in profile");
		ctc.send(msg);
	}

	public void store(DataOutputStream dos)
	{
		int listSize;

		try
		{
			dos.writeUTF(username);
			//System.out.println("username stored");
			dos.writeUTF(password);
			//System.out.println("password stored");
			listSize = friendList.size();
			dos.writeInt(listSize); //for friends
			//System.out.println("size of friends list: " + listSize);

			for(int i = 0; i < listSize; i++)
			{
				dos.writeUTF(friendList.get(i));
				//System.out.println("friend stored");
			}

			listSize = pendingAlerts.size();
			dos.writeInt(listSize); //for alerts
			//System.out.println("size of pending alerts list: " + listSize);

			for(int i = 0; i < listSize; i++)
			{
				dos.writeUTF(pendingAlerts.get(i));
				//System.out.println("pending alerts list stored");
			}

			listSize = sentFriendRequest.size();
			dos.writeInt(listSize);

			for(int i = 0; i < listSize; i++)
			{
				dos.writeUTF(sentFriendRequest.get(i));
			}
		}
		catch(Exception e)
		{
		}
	}

	public void removeAlert(String name)
	{
		System.out.println("Alert to remove for :" + username + " " + name);
		pendingAlerts.remove(name);

		ctc.clearAlerts();
	}

	public void addAlert(String name)
	{
		String[] request;
		String[] splitName;

		request = new String[2];
		System.out.println(name);
		if(ctc != null)
		{
			splitName = name.split(":");
			request[0] = "FRIEND_REQUEST:";
			request[1] = splitName[1];
			System.out.println("Alert being added: " + splitName[1] + " to " + username);
			pendingAlerts.add("1:" + splitName[1]);

			ctc.updateAlerts();

			try
			{
				//ctc.talker.send(request);
			}
			catch(Exception e)
			{
			}
		}
		else
		{
			System.out.println("Alert being added: " + name + " to " + username);
			pendingAlerts.add(name);
		}
	}

	public void addToSentList(String name)
	{
		sentFriendRequest.add(name);
	}

	public void addFriend(String name)
	{
		friendList.add(name);
	}

	public void setCTC(CTC ctc)
	{
		this.ctc = ctc;
	}
}