package net.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import net.protocol.BaseMessage;



public class SenderThread extends Thread
{
	private Socket socket;
	private List<BaseMessage> messages = null;
	
	public SenderThread(Socket socket, List<BaseMessage> out)
	{
		this.socket = socket;
		this.messages = out;
	}
	
	
	public void run() 
	{
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
		
			while (!interrupted())
			{
				try {
					if (!messages.isEmpty()) {
						oos.writeObject(messages.remove(0));
					}

					Thread.sleep(10);	
				}
				catch (SocketException e) {
					System.out.println("[SenderThread] Broken pipe, shutting down");
					return;
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
