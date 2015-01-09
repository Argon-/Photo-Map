package net.protocol;

import java.text.SimpleDateFormat;
import java.util.Date;



public class ChatEvent extends BaseMessage
{
	private static final long	serialVersionUID	= 4655058901055947242L;
	private String msg = "";
	private Date timestamp = null;
	
	public ChatEvent(String msg)
	{
		super();
		this.msg = msg;
		this.timestamp = new Date();
	}
	
	public ChatEvent(String msg, Date timestamp)
	{
		this(msg);
		this.timestamp = timestamp;
	}
	
	public String toString()
	{
		return super.toString() + "[" + new SimpleDateFormat("HH:mm:ss").format(timestamp) + "] " + msg;
	}
}
