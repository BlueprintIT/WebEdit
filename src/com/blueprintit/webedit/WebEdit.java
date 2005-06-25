/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.net.URL;

import javax.swing.JApplet;
import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.blueprintit.swim.SwimInterface;
import com.blueprintit.xui.UserInterface;

public class WebEdit extends JApplet
{
	private Logger log = Logger.getLogger(this.getClass());
	
	static
	{
		BasicConfigurator.configure();
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
	}
	
	public void init()
	{
		try
		{
			String urlbase="http://localhost"+getParameter("swim.base");
			SwimInterface swim = new SwimInterface(new URL(urlbase));
			String path=getParameter("html");
			new UserInterface(new EditorUI(swim,path),this);
		}
		catch (Exception e)
		{
			log.error("Could not load UI",e);
		}
	}
	
	public void start()
	{
		
	}
	
	public void stop()
	{
		
	}
	
	public void destroy()
	{
		
	}
}
