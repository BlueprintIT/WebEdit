package com.blueprintit.webedit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.blueprintit.swim.Request;
import com.blueprintit.swim.SwimInterface;
import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;
import com.blueprintit.xui.UserInterface;

public class UploadProgress implements InterfaceListener, Runnable
{
	private File[] files = null;
	private String path;
	private UserInterface ui;
	private SwimInterface swim;
	private transient boolean cancelled = false;
	private Collection completeListeners = new ArrayList();
	
	public JLabel label;
	public JProgressBar fileProgress;
	public JProgressBar totalProgress;
	
	public UploadProgress(SwimInterface swim) throws Exception
	{
		this.swim=swim;
		ui = new UserInterface(this);
	}
	
	public void upload(String path, File[] files)
	{
		this.files=files;
		this.path=path;
		(new Thread(this)).start();
	}
	
	public void interfaceCreated(InterfaceEvent ev)
	{
	}

	private void fireUploadComplete()
	{
		EventObject event = new EventObject(this);
		Iterator it = completeListeners.iterator();
		while (it.hasNext())
		{
			UploadCompleteListener listener = (UploadCompleteListener)it.next();
			listener.uploadComplete(event);
		}
	}
	
	public void run()
	{
		ui.show();
		int total=0;
		for (int i=0; i<files.length; i++)
		{
			total+=(int)files[i].length();
		}
		int totalpos=0;
		totalProgress.setValue(0);
		totalProgress.setMaximum(total);
		int i=0;
		while ((i<files.length)&&(!cancelled))
		{
			File file = files[i];
			label.setText("Uploading file "+(i+1)+" of "+files.length+" - "+file.getName());
			fileProgress.setMaximum((int)file.length());
			fileProgress.setValue(0);
			totalProgress.setValue(totalpos);
			try
			{
				Request request = swim.getRequest("view",path+"/"+file.getName());
				request.addParameter("version","temp");
				InputStream in = new BufferedInputStream(new FileInputStream(file));
				try
				{
					OutputStream out = request.openOutputStream();
					byte[] buffer = new byte[1024];
					int filepos = 0;
					int count = in.read(buffer);
					while ((count>0)&&(!cancelled))
					{
						try
						{
							Thread.sleep(0);
						}
						catch (InterruptedException e)
						{
						}
						out.write(buffer,0,count);
						filepos+=count;
						totalProgress.setValue(totalpos+filepos);
						fileProgress.setValue(filepos);
						count = in.read(buffer);
					}
					out.close();
					in.close();
				}
				catch (IOException ioe)
				{
				}
			}
			catch (FileNotFoundException e)
			{
				totalProgress.setValue(totalProgress.getValue()+(int)file.length());
			}
			totalpos+=(int)file.length();
			i++;
		}
		ui.hide();
		fireUploadComplete();
	}

	public void addUploadCompleteListener(UploadCompleteListener listener)
	{
		completeListeners.add(listener);
	}
}
