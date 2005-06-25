/*
 * $Author$
 * $RCSfile$
 * $Date$
 * $Revision$
 */
package com.blueprintit.swim;

import java.util.Map;
import java.util.Hashtable;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

public class Request
{
	private Logger log = Logger.getLogger(this.getClass());

	private SwimInterface swim;
	private String method;
	private String resource;
	private Map query;
	
	private Request(SwimInterface swim)
	{
		this.swim=swim;
		query = new Hashtable();
	}
	
	public Request(SwimInterface swim, String method, String resource)
	{
		this(swim);
		this.method=method;
		if (resource.startsWith("/"))
		{
			resource=resource.substring(1);
		}
		this.resource=resource;
	}
	
	public Request(SwimInterface swim, String method, String resource, Map params)
	{
		this(swim,method,resource);
	}
	
	public static Request decode(SwimInterface swim, URL url)
	{
		Request request = new Request(swim);
		return request;
	}
	
	private String generateQuery()
	{
		if (query.size()>0)
		{
			return null;
		}
		else
		{
			return null;
		}
	}
	
	public URL encode()
	{
		String full=swim.getURL().toString()+"/"+method+"/"+resource;
		String query=generateQuery();
		if (query!=null)
		{
			full+="?"+query;
		}
		try
		{
			return new URL(full);
		}
		catch (MalformedURLException e)
		{
			log.error("Could not build url from "+full);
			throw new IllegalArgumentException(e);
		}
	}
}
