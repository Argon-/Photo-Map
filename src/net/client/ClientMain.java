package net.client;

import java.net.InetAddress;
import java.net.UnknownHostException;



public class ClientMain
{

	public static void main(String[] args) throws UnknownHostException, InterruptedException
	{
		Client c1 = new Client(InetAddress.getByName("localhost"), 55555, 0);
		c1.start();
		Thread.sleep(100);
		Client c2 = new Client(InetAddress.getByName("localhost"), 55555, 1);
		c2.start();
		Thread.sleep(100);
		Client c3 = new Client(InetAddress.getByName("localhost"), 55555, 2);
		c3.start();
	}
}
