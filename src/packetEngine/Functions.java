package packetEngine;
/**
 * @(#)Functions.java Feb 22, 2004
 * LGPL License PKWooster
 */


import java.util.*;
import java.text.*;
import java.io.*;

/**
 * some handy functions
 */
public class Functions
{
	private static int debugLevel=8;
	private static final String HexValue[]={"0","1","2","3","4","5","6","7",
		"8","9","A","B","C","D","E","F"};

/**
 * debug output, dispaly string dependent on debug level
 * @param level the level for this display, only print if greater aor equal to debugLevel
 * @param text a string to print
 */
	public static void dout(int level, String text)
	{
		if(level >= debugLevel)System.out.println(level+": "+text);
	}

/**
 * set the debug level
 * @param dbl the debug level
 */
	public static void setDebugLevel(int dbl)
	{
		debugLevel = dbl;
	}
	

/**
 * get an integer from a string with default value 
 * @param s a string to convert
 * @param er the default to use if not convertable
 */
	public static int toInt(String s, int er)
	{
		int i;

		try{i = new Integer(s).intValue();}
		catch(NumberFormatException exc){i =er;}
		return i;
	}


/**
 * print the hexadecimal representation of a byte array
 * @param ba the byte array
 */
	public static void printhex(byte[]ba)
	{
		printhex(ba,0,ba.length);
	}
		
/**
 * print the hexadecimal representation of a byte array
 * @param ba the byte array
 * @param ofs the offset to start at
 * @param len the length to print
 */
	public static void printhex(byte[]ba, int ofs, int len)
	{
		println(atohex(ba,ofs,len));
	}

/**
 * print the hexadecimal and character representation of a byte array
 * @param ba the byte array
 */	
	public static void display(byte[]ba)
	{
		display(ba,0,ba.length);
	}
	
/**
 * print the hexadecimal and character representation of a byte array
 * @param ba the byte array
 * @param ofs the offset to start at
 * @param len the length to print
 */
	public static void display(byte[]ba, int ofs, int len)
	{
		int dlen;
		String dstr;

		while(len>0)
		{
			if(len < 16)
			{
				dlen = len;
				char[] c48 = new char[48];
				Arrays.fill(c48, ' ');
				String fill48 = String.valueOf(c48);
				dstr = fill48.substring(0,3*(16-dlen));
			}
			else
			{
				dlen = 16;
				dstr = "";
			}
			dstr = tohex4(ofs)+": "+atohex(ba,ofs,dlen)+dstr+": "+atochar(ba,ofs,dlen);
			println(dstr);
			ofs+=dlen;
			len-=dlen;
		}

	}

/**
 * convert a byte array to char substituting . for non dispaly characters
 * @param ba the byte array
 * @param ofs the offset to start at
 * @param len the length to print
 */
	public static String atochar(byte[]ba, int ofs, int len)
	{
		String s="";
		char ch;
		byte b;
		for(int i=ofs;i<(ofs+len);i++)
		{
			b = ba[i];
			if(b < 32 || b >126)ch = '.';
			else ch = (char)b;
			s+=ch;
		}
		return s;
	}

/**
 * convert a byte array to hexadecimal representaion
 * @param ba the byte array
 * @param ofs the offset to start at
 * @param len the length to print
 */
	public static String atohex(byte[]ba, int ofs, int len)
	{
		String s="";
		for(int i=ofs;i<(ofs+len);i++)
		{
			s += (tohex2(ba[i])+" ");
		}
		return s;
	}

/**
 * convert a byte to 2 hex digits
 * @param b the byte
 */
	public static String tohex2(int b)
	{
		int i = b;
		if(i<0)i+=256;
		return HexValue[i/16]+HexValue[i%16];
	}

/**
 * convert an integer to 4 hex digits
 * @param i the integer
 */
 	public static String tohex4(int i)
	{
		if(i<0)i+=32768;
		i = i%32768;
		return tohex2(i/256)+tohex2(i%256);
	}

/**
 * print a line
 * @param str the string to print
 */
	public static void println(String str)
	{
		System.out.println(str);
	}

	static int javaver = 0;
	private static boolean debug=false;



	public static boolean toBool(String s)
	{
		return Boolean.valueOf(s).booleanValue();
	}

	public static Thread threadNamed(String tnm)
	{
		Thread[] threads=new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		for(int i=0;i<threads.length;i++)
		{
			if(threads[i].getName().equalsIgnoreCase(tnm))
			{
				return threads[i];
			}
		}
		return null;
	}

	public static String javaVersion()
	{
		return System.getProperty("java.vm.version");
	}

	public static int javaVersionNumber()
	{
		if(javaver == 0)
		{
			String [] s = javaVersion().split("_");

			if (s.length > 0)
			{
				String vs = s[0]+".0.0";
				s = vs.split("\\.");
				for(int i = 0; i < 3; i++){javaver*=10; javaver+= toInt(s[i],0);}
			}
		}
		return javaver;
	}

	public static void listThreads()
	{
		Thread[] threads=new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		int n = 0;

		for(int i=0;i<threads.length;i++)
		{
			if(threads[i] == null)n++;
			else System.out.println(threads[i]);
		}
		System.out.println(n+" null thread"+((n!=1)?"s":""));
	}
}

