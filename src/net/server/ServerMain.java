package net.server;



public class ServerMain
{

	public static void main(String[] args)
	{
		LobbyServer ls = new LobbyServer(55555);
		ls.start();
	}

}
