/*
 * $Author$
 * $RCSfile$
 * $Date$
 * $Revision$
 */
package com.blueprintit.swim;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;

import org.apache.log4j.Logger;

public class SwimInterface
{
	private Logger log = Logger.getLogger(this.getClass());

	private URL url;
	
	public SwimInterface(URL url)
	{
		this.url=url;
	}
	
	public URL getURL()
	{
		return url;
	}
	
	public String getResource(String path) throws IOException
	{
		BufferedReader reader = new BufferedReader(openResourceReader(path));
		StringBuffer result = new StringBuffer();
		String line=reader.readLine();
		while (line!=null)
		{
			result.append(line);
			line=reader.readLine();
		}
		reader.close();
		return result.toString();
	}
	
	public void setResource(String path, String data) throws IOException
	{
		Writer writer = openResourceWriter(path);
		writer.write(data);
		writer.close();
	}
	
	public Reader openResourceReader(String path) throws IOException
	{
		return new InputStreamReader(openResourceInputStream(path));
	}
	
	public Writer openResourceWriter(String path) throws IOException
	{
		return new OutputStreamWriter(openResourceOutputStream(path));
	}
	
	public InputStream openResourceInputStream(String path) throws IOException
	{
		Request request = new Request(this,"view",path);
		return openConnection("GET",request).getInputStream();
	}
	
	public OutputStream openResourceOutputStream(String path) throws IOException
	{
		Request request = new Request(this,"view",path);
		return openConnection("PUT",request).getOutputStream();
	}
	
	private HttpURLConnection openConnection(String method, Request request) throws IOException
	{
		URL url = request.encode();
		log.info("Opening connection to "+url.toString());
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod(method);
		connection.connect();
		return connection;
	}
}
