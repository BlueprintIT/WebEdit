/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.xui;

import java.awt.Container;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

public class UserInterface extends SwingEngine
{
	private Container container = null;
	private String resource;
	private List listeners = new LinkedList();
	
	private Logger log = Logger.getLogger(this.getClass());

	public UserInterface(String resource) throws Exception
	{
		super();
		this.resource=resource;
		buildUI();
	}
	
	public UserInterface(Object obj) throws Exception
	{
		super(obj);
		resource = obj.getClass().getName();
		if (obj instanceof InterfaceListener)
		{
			addInterfaceListener((InterfaceListener)obj);
		}
		buildUI();
	}
	
	public UserInterface(String resource, Container container) throws Exception
	{
		super();
		this.resource=resource;
		this.container=container;
		buildUI();
	}
	
	public UserInterface(Object obj, Container container) throws Exception
	{
		super(obj);
		this.resource=obj.getClass().getName();
		this.container=container;
		if (obj instanceof InterfaceListener)
		{
			addInterfaceListener((InterfaceListener)obj);
		}
		buildUI();
	}
	
	public void addInterfaceListener(InterfaceListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeInterfaceListener(InterfaceListener listener)
	{
		listeners.remove(listener);
	}
	
	public Container getContainer()
	{
		return container;
	}
	
	private Reader getXUIReader()
	{
		return new InputStreamReader(getClient().getClass().getClassLoader().getResourceAsStream(resource.replace('.','/')+".xml"));
	}
	
	private void fireInterfaceCreated()
	{
		InterfaceEvent ev = new InterfaceEvent(this,InterfaceEvent.INTERFACE_CREATED);
		Iterator i = listeners.iterator();
		while (i.hasNext())
		{
			try
			{
				InterfaceListener l = (InterfaceListener)i.next();
				l.interfaceCreated(ev);
			}
			catch (Throwable t)
			{
				log.warn("Listener threw something",t);
			}
		}
	}
	
	private void buildUI() throws Exception
	{
		if (container==null)
		{
			container=this.render(getXUIReader());
		}
		else
		{
			this.insert(getXUIReader(),container);
		}
		fireInterfaceCreated();
	}
}
