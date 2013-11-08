package packetEngine;
/**
 * @(#)PacketTransport.java Fev 21, 2004
 * LGPL License PKWooster
 */


import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.net.*;
 

/**
 * an <code>PacketTransport</code>provides support for a single connection to the AGW Packet Engine 
 * Feb 2004 LGPL license
 * @author PKWooster
 * @version 0.0, feb 21,2004
 * @since JDK1.4
 */
public class PacketTransport implements PacketUser
{
	public static int MAX_PACKET_SIZE = 1024;
	private SocketChannel sch;
	private int bufsz;
	private SelectionKey key=null;
	private Controller controller=null;
	private int interest = 0;
	private boolean sendReady = false;
	private PacketUser client=null;			// a reference to our client
	private String name ="";
	private int state = Packet.CLOSED;
	private String address;				// our partners IP address
	private int port;				// our partners port
	private LinkedList sendQ = new LinkedList();	// our send queue
	private Packet recvPacket = null;		// our current receive packet

/**
 * construct a client socket 
 * using a new Controller
 */ 
	public PacketTransport(){this(null);}
	
/**
 * construct a client socket 
 * providing the controller and buffer size	
 * @param controller a <code>Controller</code> instance or null to create one
 */ 	
	public PacketTransport(Controller controller)
	{
		if(controller == null)
		{
			this.controller = new Controller();
			this.controller.start();
		}
		else this.controller = controller;
	}

/**
 * connect to a remote server
 * @param client an <code>PacketUser</code> that will receive messages about network events
 * @param address an IP address
 * @param port an IP port 
 */
	public boolean connect(PacketUser client, String address, int port)
	{
		if(state != Packet.CLOSED)return false; // can't reopen if not closed
		this.client = client;
		this.address = address;
		this.port = port;
		name = address+":"+port;
		sendReady = false;
		changeState(Packet.OPENING);		// opening
		key = null;
		// connect to address and port
		try
		{
			sch = SocketChannel.open();
			recvPacket = new Packet(client, Packet.RECEIVE, null, null);	// build an empty receive packet 
			invoke(Packet.OPEN);
		}
		catch(Exception e){return fail(e,"Connection failed to="+name);}
		dout(1,"connecting");
		return true;
	}

/**
 * disconnect from the remote server
 */	
	public void disconnect()
	{
		invoke(Packet.CLOSE);
	}

/**
 * send a <code>ByteBuffer</code> to the remote server
 * you will encode a packet into this
 */
	public void send(Packet pkt)
	{
		pkt.setClient(this);
		controller.invoke(pkt);		// this will call back
	}

/**
 * returns the controller for this object
 */
 	public Controller getController()
	{
		return controller;
	}
	
/**
 * returns the underlying socket
 */
 	public Socket getSocket()
	{
		return sch.socket();
	}
	
	
	
//======================================================================
// public methods called from the Controller's thread

	// process a selection key
	public void processSelection(SelectionKey sk)
	{
		int kro = sk.readyOps();
		dout(0,"kro="+kro);
		if((kro & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT)
				doConnect();
		if((kro & SelectionKey.OP_READ) == SelectionKey.OP_READ)
				doRead();
		if((kro & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
				doWriteReady();
		showInterest();				// safe to call this directly here
	}

	// return the connection state
	public int getState(){return state;}

	// run a packet sent to controller.invoke()  
	// this runs in the Controller's thread
	public void runPacket(Packet nm)
	{
		dout(0,"runPacket type="+nm.getType());
		
		switch(nm.getType())
		{
			case Packet.OPEN:
				enroll();
				break;

			case Packet.CLOSE:
				doRequestClose();
				break;

			case Packet.SEND:
				doSend(nm);
				break;
		}
		showInterest();
	}
	
	public void postPacket(Packet pkt){}	// required, but not used
	
//============================================================================
// private methods

	// change state and report it to client
	private void changeState(int s)
	{
		state = s;
		new Packet(client, Packet.STATE, new Integer(state)).post(); // tell our client
	}		

	// close this remote user
	private void close()
	{
		try{sch.close();}catch(IOException e){e.printStackTrace();}
		key = null;
		changeState(Packet.CLOSED);
	}


	//  set interestOps
	private void showInterest()
	{
		if(key != null)
		{
			int i = key.interestOps();
			if(i != interest)
			{
				dout(12,"changing interest to "+interest);
				key.interestOps(interest);
			}
		}
	}

	// enroll our channel
	private void enroll()
	{
		interest = 0;
		try
		{
			sch.connect(new InetSocketAddress(address,port));
			if(sch.isConnected())connectComplete();
			else if(sch.isConnectionPending())interest = SelectionKey.OP_CONNECT;
		}
		catch(Exception e){fail (e,"Connection failed to="+name); } // unexpected connect failure
		
		if(interest != 0)key = controller.enroll(this, sch, interest);
	}

	// invoke an action in the controller's thread
	private void invoke(int action)
	{
		Packet pkt = new Packet(this,action,null);
		controller.invoke(pkt);
	}
	
	// report failure to client and close
	private boolean fail(Exception e, String str)
	{
		if(str != null)
		{
			System.out.println(str);
			new Packet(client,Packet.ERROR,str).post();
		}
		if(e != null)e.printStackTrace();
		close();
		return false;
	}

	private void dout(int level, String text){Functions.dout(level,text);}


	//--------------------------------------------------------------------
	// connection support

	// finish a connection
	private void doConnect()
	{
		dout(1,"finishing connection");
		try{sch.finishConnect();}
		catch(IOException e){fail (null,"Connection failed to="+name);}
		connectComplete();
	}


	private void connectComplete()
	{
		dout(1,"connected");
		interest = SelectionKey.OP_READ+SelectionKey.OP_WRITE;
	}

	private boolean doRequestClose()
	{
		flush(sendReady);		// force out any data
		close();
		return true;
	}

	// reception support
	private void doRead()
	{
		int len = 0;
		dout(0,"recv");		
		ByteBuffer recvHeader = recvPacket.getHeader();
		ByteBuffer recvData = recvPacket.getData();
		
		if(recvHeader.position() < recvHeader.limit())
		{
			try{len = sch.read(recvHeader);}
			catch(Exception e){e.printStackTrace(); len = -1;}
			if(len < 0){close(); return;}	// socket has closed
			
			if(recvHeader.position() == recvHeader.limit())	// header is complete
			{
				int dl = recvPacket.getDataLength();
				if(dl > MAX_PACKET_SIZE)
				{
					System.out.println("Protocol error data length too long");
					close();
					return;
				}
				if(dl == 0)
				{
					recvHeader.flip();
					recvPacket.setData(null);
					recvPacket.post();
					recvPacket = new Packet(client, Packet.RECEIVE, null, null);	// build an empty receive packet 
				}
				else
				{
					recvData = ByteBuffer.allocate(dl);
					recvPacket.setData(recvData);
				}
			}
		}
		else
		{
			if(recvData.position() < recvData.limit())
			{
				try{len = sch.read(recvData);}
				catch(Exception e){e.printStackTrace(); len = -1;}
				if(len < 0){close(); return;}	// socket has closed
				
				if(recvData.position() == recvData.limit())
				{
					recvHeader.flip();
					recvData.flip();
					recvPacket.post();
					recvPacket = new Packet(client, Packet.RECEIVE, null, null);	// build an empty receive packet 
				}
			}
		}
	}

	// transmission support

	// send out packet 
	private void doSend(Packet pkt)
	{
		if(null == pkt.getData())pkt.setDataLength(0);	// no data gets zero length
		sendQ.add(pkt.getHeader());			// send the header
		if(0 < pkt.getDataLength())sendQ.add(pkt.getData());	// send the data if not empty
		dout(1,"send");
		flush(sendReady);			// attempt to send it
	}

	// write ready has been signalled
	private void doWriteReady()
	{
		if(state == Packet.OPENING)changeState(Packet.OPENED);
				
		dout(1,"write ready");
		flush(true);
	}

	// attempt to write out all queued data
	private void flush(boolean sr)
	{
		boolean oldsr=sendReady;
		sendReady = sr;
		dout(0,"flush sendReady="+sendReady+" sendQ="+sendQ.size());
		while(sendReady && 0 != sendQ.size())	// send all data in queue
		{
			ByteBuffer buf = (ByteBuffer)sendQ.removeFirst();
			int n = buf.remaining();
			dout(0,"buf remaining="+n+" position="+buf.position()+" limit="+buf.limit());
			
			int s = 0;
			if(n > 0)
			{
				dout(0,"writing "+n+" bytes");
				try{s = sch.write(buf);}
				catch(Exception e){e.printStackTrace();}
				if(s < n)
				{
					dout(0,"Write blocked");
					sendReady = false;	// unable to send
					sendQ.addFirst(buf);	// put data back on top of queue
				}
			}
		}
		
		if(sendReady != oldsr) // has send ready state changed?
		{
			new Packet(client,Packet.FLOW,new Boolean(sendReady)).post();
			if(sendReady)interest = SelectionKey.OP_READ;
			else interest = SelectionKey.OP_WRITE | SelectionKey.OP_READ;
		}
	}
}
