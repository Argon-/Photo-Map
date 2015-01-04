package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import game.Player;



public class ReceiverThread extends Thread
{
	//private String id; dieser thread hier ist die id?
	private Socket socket;

	
	ReceiverThread(Socket socket)
	{
		this.socket = socket;
	}
	
	
	public void run() 
	{
		try 
		{
			ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
		
			while (!interrupted())
			{
				
			}
		
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}
	
	
	//public Player createPlayer()
	//{
	//	return new Player();
	//}
}
