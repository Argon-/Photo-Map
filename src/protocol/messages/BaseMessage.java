package protocol.messages;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class BaseMessage implements Serializable
{
	public final int MAJOR = 0;
	public final int MINOR = 0;
	
	public boolean isCompatibleProtocolVersion(BaseMessage a)
	{
		if (this.MAJOR != a.MAJOR)
			return false;
		if (this.MINOR < a.MINOR)
			return false;
		return true;
	}
}
