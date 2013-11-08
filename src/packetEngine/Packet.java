package packetEngine;

import java.nio.*;

/**
 * this describes an AGW packet
 * the state is the state of the AGWPacketSocket
 * the header is 36 bytes of control information
 * the data is an arbitrary amount of user data
 */
public class Packet implements Runnable
{
	// message type constants
	public static final int STATE = 0;		// the state has changed
	public static final int FLOW = 1;		// transmit flow control change
	public static final int RECEIVE = 2;		// receive data
	public static final int SEND = 3;		// send data
	public static final int OPEN = 4;		// request open
	public static final int CLOSE = 5;		// request close
	public static final int ERROR = 6;		// report error
	
	// state constants, used as arg in state change packets
	public static final int CLOSED = 0;
	public static final int OPENING = 1;
	public static final int OPENED = 2;
	public static final int CLOSING =3;


	private PacketUser client;
	private int type;			// AGWPacket type
	private Object arg;			// an arbitrary object argument, varies with type
	private ByteBuffer header = null;	// packet header
	private ByteBuffer data = null;		// packet data
	private boolean hasHeader = false;	

	// constructors
	/**
	 * construnct an action packet
	 */
	public Packet(PacketUser client, int type)
	{
		this(client,type,null);
	}
	
	/**
	 * construct a state change packet
	 */
	public Packet(PacketUser client, int type, Object arg)
	{
		this.client = client;
		this.arg = arg;
		this.type = type;
	}

	/**
	 * construct a data transfer packet
	 */
	public Packet(PacketUser client, int type, ByteBuffer header, ByteBuffer data)
	{
		this.client = client;
		this.arg = null;
		this.type = type;
		this.data = data;
		hasHeader = (header != null && header.position() == 0 && header.limit() == 36);

		if(header == null)
		{
			this.header =  ByteBuffer.allocate(36);
			for(int i=0;i<36;i++)this.header.put((byte)0); // fill with zeros		
		}
		else this.header = header;
		this.header.order(ByteOrder.LITTLE_ENDIAN);
		
		// Added by mikep 2004-03-06
		if((type==SEND) && (data != null)) setDataLength(data.limit());
		//if(type==SEND)setDataLength(data.limit());

		this.header.position(0);
		this.header.limit(36);
	}

	public PacketUser getClient(){return client;}
	public void setClient(PacketUser client){this.client=client;}
	public Object getArg(){return arg;}
	public void setArg(Object arg){this.arg = arg;}
	public int getType(){return type;}
	public void setType(int type){this.type = type;}

	public ByteBuffer getData(){return data;}
	public void setData(ByteBuffer data)
	{
		this.data = data;
		setDataLength(data.limit());
	}
	
	
	// header field getters
	public ByteBuffer getHeader(){return header;}
	public int getPort(){return header.getInt(0);}
	public int getDataKind(){return header.getInt(4);}
	public byte[] getCallFrom()
	{
		byte [] dat = new byte[10];
		for(int i = 0; i < 10; i++)dat[i] = header.get(8+i);
		return dat;
	}
	public byte[] getCallTo()
	{
		byte [] dat = new byte[10];
		for(int i = 0; i < 10; i++)dat[i] = header.get(18+i);
		return dat;
	}
	public int getDataLength(){return header.getInt(28);}
	public int getUser(){return header.getInt(32);}

	// header field setters
	public void setPort(int p){header.putInt(0,p);}
	public void setDataKind(int p){header.putInt(4,p);}
	
	public void setCallFrom(byte[] dat)
	{
		// Added by mikep 2004-03-06
		if (dat != null){
			int len = (dat.length > 10)?10:dat.length;
			for(int i = 0; i < len; i++)header.put(8+i, dat[i]);
		}
	}
	public void setCallTo(byte[] dat)
	{
		// Added by mikep 2004-03-06
		if (dat != null){
			int len = (dat.length > 10)?10:dat.length;
			for(int i = 0; i < len; i++)header.put(18+i, dat[i]);
		}
	}
	public void setDataLength(int p){header.putInt(28,p);}
	public void setUser(int p){header.putInt(32,p);}
	public void run(){client.runPacket(this);}
	public void post(){client.postPacket(this);}
}
