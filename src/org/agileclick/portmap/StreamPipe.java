package org.agileclick.portmap;

import java.io.*;
import java.sql.Timestamp;

class StreamPipe extends Thread
	{
	private InputStream	 		m_input;
	private OutputStream	 		m_output;
	private OutputStream			m_file;
	private boolean				m_sender;
	private volatile int			m_delay;

	
	public StreamPipe(InputStream input, OutputStream output, OutputStream file)
			throws IOException
		{
		//m_sender = sender;
		m_input = input;
		m_output = output;
		m_file = file;
		m_delay = 0;
		}
		
	public void setDelay(int delay)
		{
		m_delay = delay;
		}
		
	public synchronized void run()
		{
		int dataSize;
		byte[] buffer = new byte[1024 * 8];
		int ch;
		
		//System.out.println("Starting pipe");
		try
			{
			while ((dataSize = m_input.read(buffer)) != -1)
				{
				if (m_delay != 0)
					Thread.sleep(m_delay);
					
				m_output.write(buffer, 0, dataSize);
				m_output.flush();
				if (m_file != null)
					printPacket(buffer, dataSize, m_file);
				}
			}
		catch (IOException ioe)
			{
			//ioe.printStackTrace();
			}
		catch (InterruptedException ie)
			{
			ie.printStackTrace();
			}
			
		try
			{
			m_input.close();
			}
		catch (IOException ioe) {}
		
		try
			{
			m_output.close();
			}
		catch (IOException ioe) {}
		
		try
			{
			if (m_file != null)
				m_file.close();
			}
		catch(IOException ioe){}
		//System.out.println("Pipe dead");
		}
		
	private void printPacket(byte[] buffer, int size, OutputStream os)
			throws IOException
		{
		synchronized (os)
			{
			byte[] spacer = new byte[4];
			spacer[0] = 32;
			spacer[1] = 32;
			spacer[2] = 32;
			spacer[3] = 32;
			
			byte[] linebreak = new byte[2];
			linebreak[0] = 13;
			linebreak[1] = 10;
			os.write((new Timestamp(System.currentTimeMillis())).toString().getBytes("UTF-8"));
			os.write(linebreak);
			for (int I = 0; I < size; I++)
				{
				if ((buffer[I] >= 33) && (buffer[I] <= 126))
					{
					os.write(buffer, I, 1);
					os.write(spacer, 0, 2);
					}
				else
					os.write(spacer, 0, 3);
				}
			
			os.write(linebreak);
			byte[] zero = new byte[1];
			zero[0] = 48;
			for (int I = 0; I < size; I++)
				{
				if ((buffer[I] >= 0) && (buffer[I] <= 0x0A))
					{
					os.write(zero);
					os.write(Integer.toHexString(buffer[I] & 0xFF).getBytes("UTF-8"));
					}
				else
					os.write(Integer.toHexString(buffer[I] & 0xFF).getBytes("UTF-8"));
				os.write(spacer, 0, 1);
				}
				
			os.write(linebreak);
			os.flush();
			}
		}
	}