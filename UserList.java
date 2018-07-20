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

public class UserList extends Hashtable<String, Profile>
{
	public UserList()
	{
		System.out.println("Created userlist without dis");
	}

	public UserList(DataInputStream dis)
	{
		int     numOfUsers;
		Profile userProfile;

		try
		{
			System.out.println("OK");
			numOfUsers = dis.readInt();

			System.out.println("Number of Users: " + numOfUsers);

			for(int i = 0; i < numOfUsers; i++)
			{
				userProfile = new Profile(dis);
				this.put(userProfile.username, userProfile);
			}

			System.out.println("Creating list with dis");
		}
		catch(IOException ioe)
		{
			System.out.println("Error");
		}
	}

	public void addUser(Profile profile)
	{
		System.out.println("Adding user");
		this.put(profile.username, profile);
		System.out.println("About to store");
		this.store();
		System.out.println("Stored");
	}

	public void store()
	{
		int              numOfUsers;
		DataOutputStream dos;
		Profile          profile;
		Enumeration<Profile> userE;

		userE = this.elements();

		try
		{

			dos = new DataOutputStream(new FileOutputStream("serverconfig.bin"));

			numOfUsers = this.size();

			System.out.println("Size of list: " + numOfUsers);

			dos.writeInt(numOfUsers);

			System.out.println("Storing");

			while(userE.hasMoreElements())
			{
				System.out.println("Getting profile");
				profile = userE.nextElement();
				profile.store(dos);
				System.out.println("Storing profile");
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in storing");
		}
	}
}