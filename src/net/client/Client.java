package net.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.protocol.BaseMessage;
import net.protocol.ChatEvent;
import net.util.SenderThread;



public class Client extends Thread
{
	private int port = 0;
	private final int id;
	private InetAddress addr;
	private Socket socket;
	
	private List<BaseMessage> out = Collections.synchronizedList(new LinkedList<BaseMessage>());

	
	public Client(InetAddress addr, int port, int id)
	{
		this.addr = addr;
		this.port = port;
		this.id = id;
	}
	

	public void run()
	{
		System.out.println("[Client] starting client " + id);
		Random r = new Random();
		SenderThread sender;
		
		try {
			socket = new Socket(addr, port);
			sender = new SenderThread(socket, out);
			sender.start();
		}
		catch (IOException e) {
			System.out.println("[Client] client " + id + " is unable to connect");
			return;
		}
		
		
		while (!interrupted())
		{
			try {
				Thread.sleep(r.nextInt(1000) + 500);
				//System.out.println("[Client] client " + id + " sending message...");
				out.add(new ChatEvent("hello from client " + id + "!"));
				
				if (!sender.isAlive()) {
					System.out.println("[Client] SenderThread shut down");
					return;
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
