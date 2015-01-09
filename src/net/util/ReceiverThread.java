package net.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;

import net.protocol.BaseMessage;



public class ReceiverThread extends Thread
{
	private Socket socket;
	private LinkedList<BaseMessage> messages = null;
	
	public ReceiverThread(Socket socket, LinkedList<BaseMessage> in)
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
