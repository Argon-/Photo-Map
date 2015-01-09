package net.server;


import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.protocol.BaseMessage;
import net.util.ReceiverThread;
import net.util.SenderThread;



public class ClientThread extends Thread
{
	private Socket socket;
	private final int id;
	private List<BaseMessage> out = Collections.synchronizedList(new LinkedList<BaseMessage>());
	private List<BaseMessage> in  = Collections.synchronizedList(new LinkedList<BaseMessage>());
	
	public ClientThread(Socket socket, int id)
	{
		this.socket = socket;
		this.id = id;
	}
	
	
	public void run()
	{
		System.out.println("[ClientThread] starting up for client " + id);
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
					BaseMessage msg = in.remove(0);
					System.out.println("[ClientThread] " + id + " received: " + msg);
				}
			
				Thread.sleep(1);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	

}
