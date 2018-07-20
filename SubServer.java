import java.net.*;
import java.io.*;

public class SubServer implements Runnable
{
	String fileName;
	long   fileSize;

	public SubServer(String fileName, String fileSize)
	{
		System.out.println("Creating subserver");
		this.fileName = fileName;
		this.fileSize = Long.parseLong(fileSize);

		new Thread(this).start();
	}

	public void run()
	{
		InputStream       is;
		FileOutputStream  fos;
		int               numBytesRead;
		int               totNumBytes;
		byte[]            bytesArr;
		ServerSocket      serverSocket;
		Socket            normalSocket;
		boolean           connected;

		try
		{
			serverSocket = new ServerSocket(4321);
			serverSocket.setSoTimeout(1000);
			normalSocket = null;
			connected = false;

			while(!connected)
			{
				try
				{
					normalSocket = serverSocket.accept();
					System.out.println("found a connection");
					connected = true;
				}
				catch(SocketTimeoutException ste)
				{
				}
			}

			is = normalSocket.getInputStream();
			fos = new FileOutputStream(fileName);

			bytesArr = new byte[512];

			totNumBytes = 0;

			numBytesRead = is.read(bytesArr);

			while(numBytesRead != 0 && totNumBytes < fileSize)
			{
				System.out.println("totNumBytes: " + totNumBytes);
				fos.write(bytesArr);
				totNumBytes += numBytesRead;
				numBytesRead = is.read(bytesArr);
			}
		}
		catch(Exception e)
		{
			System.out.println("found an error with reading");
		}
	}
}