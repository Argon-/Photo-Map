package net.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import net.protocol.BaseMessage;



public class ReceiverThread extends Thread
{
	private Socket socket;
	private List<BaseMessage> messages = null;
	
	public ReceiverThread(Socket socket, List<BaseMessage> in)
	{
		this.socket = socket;
		this.messages = in;
	}
	
	
	public void run() 
	{
		try 
		{
			ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
		
			while (!interrupted())
			{
				try {
					Object o = ois.readObject();
					if (o instanceof BaseMessage) {
						messages.add((BaseMessage) o); 

					Thread.sleep(10);
					}
				}
				catch (SocketException e) {
					System.out.println("[ReceiverThread] Broken pipe, shutting down");
					return;
				}

				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}	
}
