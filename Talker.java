import java.net.*;
import java.io.*;
import java.lang.Exception;

public class Talker
{
	Socket           mainSocket;
	BufferedReader   bufferedReader;
	DataOutputStream dos;
	String           owner;
	Client           client;

	public Talker(String domain, int port, Client client)
	{
		try
		{
			mainSocket = new Socket(domain, port);
			bufferedReader = new BufferedReader(new InputStreamReader(mainSocket.getInputStream()));
			dos = new DataOutputStream(mainSocket.getOutputStream());
			this.client = client;
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Error in Talker constructor for client: UnknownHostException");
		}
		catch(IOException ioe)
		{
			System.out.println("Error in Talker constructor for client: IOException");
		}
	}

	public Talker(Socket socket)
	{
		try
		{
			mainSocket = socket;
			bufferedReader = new BufferedReader(new InputStreamReader(mainSocket.getInputStream()));
			dos = new DataOutputStream(mainSocket.getOutputStream());
		}
		catch(UnknownHostException uhe)
		{
			System.out.println("Error in Talker constructor for client: UnknownHostException");
		}
		catch(IOException ioe)
		{
			System.out.println("Error in Talker constructor for client: IOException");
		}
	}

	public void send(String string) throws Exception
	{
		try
		{
			dos.writeBytes(string + ":" + owner + "\n");

			System.out.println("SENDING MESSAGE " + string + ":"  + owner);
		}
		catch(IOException ioe)
		{
			//System.out.println("Error in sending in talker");
			throw new Exception();
		}
	}

	public String receive() throws Exception
	{
		String msg;

		try
		{
			msg = bufferedReader.readLine();
			//fullMsg = msg.split(": ");
			System.out.println("RECEIVING MSG " + msg);

			return msg;
		}
		catch(IOException ioe)
		{
			//System.out.println("Error in receiving in talker");
			throw new Exception();

			//return msg;
		}
	}

	public void setId(String id)
	{
		owner = id;
	}
}
