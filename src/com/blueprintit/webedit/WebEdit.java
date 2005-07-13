/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.blueprintit.errors.ErrorReporter;
import com.blueprintit.swim.SwimInterface;
import com.blueprintit.xui.UserInterface;

public class WebEdit extends JApplet
{
	private Logger log = Logger.getLogger(this.getClass());
	
	static
	{
		BasicConfigurator.configure();
	}
	
	public void init()
	{
		try
		{
			try
			{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e)
			{
			}
			String urlbase=getParameter("swim.base");
			String path=getParameter("html");
			String style=getParameter("style");
			try
			{
				SwimInterface swim = new SwimInterface(new URL(urlbase));
				URL cancel = new URL(getParameter("cancel"));
				URL commit = new URL(getParameter("commit"));
				try
				{
					new UserInterface(new EditorUI(getAppletContext(),swim,path,style,cancel,commit),this);
				}
				catch (Exception e)
				{
					log.error("Could not load UI",e);
					ErrorReporter.sendErrorReport(
							"Error loading editor","Due to an unknown reason, the page editor could not be loaded.",
							"Swim","WebEdit","Could not load UI",e);
				}
			}
			catch (MalformedURLException e)
			{
				ErrorReporter.sendErrorReport(
						"Invalid configuration","The website you are trying to edit appears to be misconfigured.",
						"Swim","WebEdit","Bad URLs",e);
			}
		}
		catch (Throwable t)
		{
			ErrorReporter.sendErrorReport(
					"Unknown Error","An unknown error has occured. You should send an error report to Blueprint IT Ltd.",
					"Swim","WebEdit","Unknown error",t);
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
