package com.blueprintit.webedit.htmlkit;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;

import org.apache.log4j.Logger;

public class WebEditHTMLWriter extends HTMLWriter
{
	private Logger log = Logger.getLogger(this.getClass());

	public WebEditHTMLWriter(Writer w, HTMLDocument doc)
	{
		super(w,doc);
	}
	
	public WebEditHTMLWriter(Writer w, HTMLDocument doc, int pos, int len)
	{
		super(w,doc,pos,len);
	}

  /**
   * Writes out the attribute set.  Ignores all
   * attributes with a key of type HTML.Tag,
   * attributes with a key of type StyleConstants,
   * and attributes with a key of type
   * HTML.Attribute.ENDTAG.
   *
   * @param attr   an AttributeSet
   * @exception IOException on any I/O error
   *
   */
  protected void writeAttributes(AttributeSet attr) throws IOException
  {
  	SimpleAttributeSet conv = new SimpleAttributeSet(attr);
		
  	StringBuffer style = new StringBuffer();
		Enumeration names = conv.getAttributeNames();
		while (names.hasMoreElements())
		{
			Object name = names.nextElement();
			log.debug("Outputing attribute "+name);
			if (name instanceof CSS.Attribute)
			{
				style.append(name.toString()+": "+conv.getAttribute(name));
				conv.removeAttribute(name);
			}
		}
		if (style.length()>0)
		{
			write(" style=\""+style.toString()+"\"");
		}
		if (conv.getAttributeCount()>0)
		{
			super.writeAttributes(conv);
		}
  }
}
