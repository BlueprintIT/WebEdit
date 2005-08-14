/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

public class StyleModel extends AbstractListModel implements ComboBoxModel
{
	private List styles = new ArrayList();
	private Object selected;
	
	private Logger log = Logger.getLogger(this.getClass());

	public StyleModel()
	{
	}
	
	public void loadFrom(Reader reader) throws Exception
	{
		SAXBuilder builder = new SAXBuilder();
		Document list = builder.build(reader);
		Iterator it = list.getDescendants(new ElementFilter("style"));
		while (it.hasNext())
		{
			styles.add(new Style((Element)it.next()));
		}
	}

	public void setSelectedItem(Object anItem)
	{
		if ((selected!=anItem)&&((anItem==null)||(styles.contains(anItem))))
		{
			int pos1=-1;
			int pos2=-1;
			if (selected!=null)
				pos1 = styles.indexOf(selected);
			selected=anItem;
			if (selected!=null)
				pos2 = styles.indexOf(selected);
			this.fireContentsChanged(this,pos1,pos2);
		}
	}
	
	public Style getStyle(javax.swing.text.Element element)
	{
		AttributeSet attrs = element.getAttributes();
		if (attrs!=null)
		{
			Object tag = attrs.getAttribute(StyleConstants.NameAttribute);
			if ((tag!=null)&&(tag instanceof HTML.Tag))
			{
				String classname = null;
				if (attrs.isDefined(HTML.Attribute.CLASS))
				{
					classname=(String)attrs.getAttribute(HTML.Attribute.CLASS);
				}
				Iterator it = styles.iterator();
				while (it.hasNext())
				{
					Style style = (Style)it.next();
					if (style.getTag().equals(tag))
					{
						if ((classname==null)&&(style.getClassName()==null))
							return style;
						if ((classname!=null)&&(classname.equals(style.getClassName())))
							return style;
					}
				}
			}
		}
		return null;
	}

	public Object getSelectedItem()
	{
		return selected;
	}

	public int getSize()
	{
		return styles.size();
	}

	public Object getElementAt(int index)
	{
		return styles.get(index);
	}
	
	public class Style
	{
		private String name;
		private HTML.Tag tag;
		private String classname;
		private boolean fixed;
		
		public Style(Element el)
		{
			name=el.getAttributeValue("name");
			String tgname = el.getAttributeValue("tag");
			if (tgname!=null)
			{
				try
				{
					Field field = HTML.Tag.class.getField(tgname);
					tag=(HTML.Tag)field.get(null);
				}
				catch (Exception e)
				{
					log.info("Invalid tag specified - "+tag);
				}
			}
			classname = el.getAttributeValue("class");
			if (el.getAttribute("fixed")!=null)
			{
				fixed=Boolean.valueOf(el.getAttributeValue("fixed")).booleanValue();
			}
		}
		
		public Style(String name, HTML.Tag tag, String classname)
		{
			this.name=name;
			this.tag=tag;
			this.classname=classname;
		}
		
		public String getName()
		{
			return name;
		}
		
		public HTML.Tag getTag()
		{
			return tag;
		}
		
		public String getClassName()
		{
			return classname;
		}
		
		public String toString()
		{
			return getName();
		}
		
		public void apply(MutableAttributeSet attr)
		{
			attr.addAttribute(StyleConstants.NameAttribute,tag);
			if (classname!=null)
			{
				attr.addAttribute(HTML.Attribute.CLASS,classname);
			}
		}

		public boolean isFixed()
		{
			return fixed;
		}
	}
}
