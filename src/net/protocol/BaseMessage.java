package net.protocol;

import java.io.Serializable;



@SuppressWarnings("serial")
public abstract class BaseMessage implements Serializable
{
	public final int MAJOR = 1;
	public final int MINOR = 0;
	
	public boolean isCompatibleProtocol(BaseMessage a)
	{
		if (this.MAJOR != a.MAJOR)
			return false;
		if (this.MINOR < a.MINOR)
			return false;
		return true;
	}
	
	public String toString()
	{
		return "Protocol " + MAJOR + "." + MINOR + " ";
	}
}
