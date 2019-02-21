package org.agileclick.portmap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.*;
import java.io.*;
import java.security.Security;
import java.util.Properties;

public class PortMap extends Thread
	{
	public static final int BUFFER_SIZE = 1024 * 8;
	private int m_port;
	
	/**
	arg 0: Listening local port
	arg 1: Destination IP
	arg 2: Destination port
	arg 3: Delay milli sec
	*/
	public static void main(String[] args)
			throws Exception
		{
		if (args.length != 1)
			{
			printHelp();
			return;
			}

		Properties props = new Properties();
		props.load(new FileReader(args[0]));

		int listenPort = Integer.parseInt(props.getProperty("listenPort"));
		String destAddr = props.getProperty("destAddr");
		int destPort = Integer.parseInt(props.getProperty("destPort"));

		int delay = Integer.parseInt(props.getProperty("delay", "0"));

		boolean secure = Boolean.parseBoolean(props.getProperty("destSecure", "false"));

		Security.addProvider(
				new com.sun.net.ssl.internal.ssl.Provider());

		SSLSocketFactory sslFactory =
				(SSLSocketFactory) SSLSocketFactory.getDefault();

		
		//The main thread handles TCP traffic
		ServerSocket server = new ServerSocket(listenPort);
		Socket s;
		while ((s = server.accept()) != null)
			{
			//System.out.println("Connected");
			Socket redirectSocket;

			if (secure)
				redirectSocket = sslFactory.createSocket(destAddr, destPort);
			else
				redirectSocket = new Socket(destAddr, destPort);


			OutputStream sendFile = null;
			OutputStream receiveFile = null;

			if (props.getProperty("sendFile") != null)
				sendFile = new FileOutputStream(props.getProperty("sendFile"));

			if (props.getProperty("receiveFile") != null)
				receiveFile = new FileOutputStream(props.getProperty("receiveFile"));

			StreamPipe spIn = new StreamPipe(s.getInputStream(), redirectSocket.getOutputStream(), receiveFile);
			spIn.setDelay(delay);
			spIn.start();
			StreamPipe spOut = new StreamPipe(redirectSocket.getInputStream(), s.getOutputStream(), sendFile);
			spOut.setDelay(delay);
			spOut.start();
			}
		}
		
	private static void printHelp()
		{
		PrintStream out = System.out;
		
		out.println("PortMap Help");
		out.println("Usage:> java -jar portmap.jar portmap.properties");
		out.println();
		out.println("Properties in portmap.properties");
		out.println("listenPort: Local port to listen on for connections. (required)");
		out.println("destPort: Destination port to connect to. (required)");
		out.println("destAddr: Destination address to connect to. (required)");
		out.println("destSecure: Identifies if the destination connection should be made using ssl (true/false, default: false)");
		out.println("sendFile: Name of file to write all sent data to. (optional)");
		out.println("receiveFile: Name of file to write all received data to. (optional)");
		out.println("delay: Millisecond delay to add to each packet. (optional)");
		}
		
	public PortMap(int port)
		{
		m_port = port;
		start();
		}
		
	public void run()
		{
		try
			{
			//This handles UDP traffic
			FileOutputStream fos = new FileOutputStream("udp.txt");
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramSocket ds = new DatagramSocket(m_port);
			for (;;)
				{
				DatagramPacket p = new DatagramPacket(buffer, BUFFER_SIZE);
				
				ds.receive(p);
				
				fos.write(buffer, p.getOffset(), p.getLength());
				fos.write("\n\nPacket break\n\n".getBytes("UTF-8"));
				fos.flush();
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	}
