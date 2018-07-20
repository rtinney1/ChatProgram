import java.net.*;
import java.io.*;

public class SubClient implements Runnable
{
	File file;

	public SubClient(File file)
	{
		System.out.println("creating subclient");
		this.file = file;
		new Thread(this).start();
	}

	public void run()
	{
		Socket           mainSocket;
		FileInputStream  fis;
		OutputStream     os;
		long             fileLength;
		int              totNumBytes;
		int              numBytesRead;
		byte[]           bytesArr;

		try
		{
			mainSocket = new Socket("127.0.1.2", 4321);
			System.out.println("found a connection");

			fileLength = file.length();
			totNumBytes = 0;
			bytesArr = new byte[512];

			fis = new FileInputStream(file);
			os = mainSocket.getOutputStream();

			numBytesRead = fis.read(bytesArr);

			while(numBytesRead != 0)
			{
				System.out.println("numBytesRead: " + numBytesRead);
				os.write(bytesArr);
				//totNumBytes += numBytesRead;
				numBytesRead = fis.read(bytesArr);
			}
		}
		catch(Exception e)
		{
			System.out.println("error");
		}
	}
}