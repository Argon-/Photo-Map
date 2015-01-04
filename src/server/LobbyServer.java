package server;

import game.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;



public class LobbyServer extends Thread
{
	private int port = 0;
	private HashMap<String, ReceiverThread> potentialPlayerConnections = new HashMap<String, ReceiverThread>();
	
	
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
