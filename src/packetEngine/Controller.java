package packetEngine;
/*
 * @(#)AGWController.java Fev 21, 2004
 * LGPL License PKWooster
 */


import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * provides support for a Selector for multiple Selectable channels
 * adds the capability to run arbitrary methods in the controller's thread
 * @author PKWooster
 * @version 0.1, feb 21,2004
 * @since JDK1.4
 */
public class Controller
{
	private Selector selector;	// the NIO selector
	private LinkedList invocations;	// a list of invokations
	boolean running;
	int loopCount = 0;

/**
 * construct and instance of the controller
 */
	public Controller()
	{
		try{selector = Selector.open();}
		catch(IOException e){e.printStackTrace();}
		invocations = new LinkedList();
		running = false;
	}

/**
 * start the controller in a separate thread
 */
	public void start(){start(true);}	// default runs a thread

/**
 * start the controller
 * @param asTread starts in a seperate thread
 */
	public void start(boolean asThread)
	{
		if(asThread)
		{
			Thread th = new Thread(new Runnable(){
				public void run()
				{
					select();
					System.out.println("Controller thread ended");
				}});
			th.setName("AGWController");
			th.start();
		}
		else select();
	}

/**
 * stop the controller
 */
	public void stop()
	{
		running = false;
		selector.wakeup();
	}


/**
 * register a channel with this controller
 * if this is run from another thread after the controller is started this may block,
 * use invoke to prevent that.
 * @param client the client of this controller an <code>PacketTransport</code> 
 * @param channel the channel being controlled
 * @param interest the initial interest setting  
 */
	public SelectionKey enroll(PacketTransport client, SelectableChannel sch, int interest)
	{
		SelectionKey sk = null;
		try
		{
			sch.configureBlocking(false);
			sk=sch.register(selector, interest, client);
		}
		catch(IOException e){e.printStackTrace();}
		// System.out.println("key="+sk+" enrolled interest="+sk.interestOps());
		return sk;
	}

/** 
 * queue an invocation with this controller
 * @param runnable the object that will run the invocation inthe controllers thread
 */
	public synchronized void invoke(Runnable runnable)
	{
		invocations.add(runnable);		// add it to our request queue
		selector.wakeup();		// break out of the select
	}

	// the select loop
	private void select()
	{
		int n;
		Iterator it;
		SelectionKey key;
		Object att;
		PacketTransport c;
		int io;
		running = true;
		int j=0;

		while(running)
		{
			// run any requested invocations
			doInvocations();

			// now we select any pending io
			try{n = selector.select();}	// select
			catch(Exception e){e.printStackTrace(); return;}
			// System.out.println("select n="+n);
			if(n==0)
			{
				loopCount++;
				if(loopCount>10)
				{
					System.out.println("loop detected");
					break;
				}
			}
			else loopCount=0; // **** testing

			// process any selected keys
			Set selectedKeys = selector.selectedKeys();
			it = selectedKeys.iterator();
			while(it.hasNext())
			{
				key = (SelectionKey)it.next();
				c = (PacketTransport)key.attachment(); 	// get the controllable
				c.processSelection(key);// ask it to process its selections
				it.remove();			// remove the key
			}
		}
		System.out.println("select ended");
	}

	// run the invocations in our thread, these probably set the interestOps,
	// or register dispatchables
	// but they could do almost anything
	// this is synchronized with invoke
	private synchronized void doInvocations()
	{
		Runnable r;
		boolean b =true;
		while(invocations.size() > 0)
		{
			loopCount = 0;
			r = (Runnable)invocations.removeFirst();
			r.run();
		}
	}
}