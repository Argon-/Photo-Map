package net.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;



public class LobbyServer extends Thread
{
	private int port = 0;
	private AtomicInteger id = new AtomicInteger(0);
	
	
	public LobbyServer(int port)
	{
		this.port = port;
	}
	

	@Override
	public void run()
	{
		ServerSocket s;
		
		try {
			s = new ServerSocket(this.port);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			return;
		}
		
		
		while (!interrupted())
		{
			try {
				Socket p = s.accept();
				System.out.println("[DispatchServer] ");
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	
		
		try {
			s.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}