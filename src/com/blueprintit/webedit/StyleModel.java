/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.text.html.HTML;

public class StyleModel extends AbstractListModel implements ComboBoxModel
{
	private List styles = new ArrayList();
	private Object selected;
	
	public StyleModel()
	{
		styles.add(new Style("Heading 1",HTML.Tag.H1,null));
		styles.add(new Style("Heading 2",HTML.Tag.H2,null));
		styles.add(new Style("Heading 3",HTML.Tag.H3,null));
		styles.add(new Style("Heading 4",HTML.Tag.H4,null));
		styles.add(new Style("Heading 5",HTML.Tag.H5,null));
		styles.add(new Style("Heading 6",HTML.Tag.H6,null));
		styles.add(new Style("Normal",HTML.Tag.P,null));
		selected=styles.get(6);
	}
	
	public void setSelectedItem(Object anItem)
	{
		if ((selected!=anItem)&&(styles.contains(anItem)))
		{
			int pos1 = styles.indexOf(selected);
			selected=anItem;
			int pos2 = styles.indexOf(selected);
			this.fireContentsChanged(this,pos1,pos2);
		}
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
	}
}
