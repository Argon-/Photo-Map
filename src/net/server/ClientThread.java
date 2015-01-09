package net.server;


import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;

import net.protocol.BaseMessage;
import net.util.ReceiverThread;
import net.util.SenderThread;



public class ClientThread extends Thread
{
	private Socket socket;
	private LinkedList<BaseMessage> out = (LinkedList) Collections.synchronizedList(new LinkedList<BaseMessage>());
	private LinkedList<BaseMessage> in = (LinkedList) Collections.synchronizedList(new LinkedList<BaseMessage>());
	
	public ClientThread(Socket socket)
	{
		this.socket = socket;
	}
	
	
	public void run()
	{
		ReceiverThread recv = new ReceiverThread(socket, in);
		SenderThread   send = new SenderThread(socket, out);
		recv.start();
		send.start();
		
		while (!interrupted())
		{
			try 
			{

				if (!in.isEmpty())
				{
					BaseMessage msg = in.pop();
					System.out.println("[ClientThread] received: " + msg);
				}
			
				Thread.sleep(1);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	

}
