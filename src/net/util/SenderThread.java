package net.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

import net.protocol.BaseMessage;



public class SenderThread extends Thread
{
	private Socket socket;
	private LinkedList<BaseMessage> messages = null;
	
	public SenderThread(Socket socket, LinkedList<BaseMessage> out)
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
						oos.writeObject(messages.pop());
					}

					Thread.sleep(10);	
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
