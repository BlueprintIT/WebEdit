/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit.htmlkit;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ParagraphView;

public class WebEditEditorKit extends HTMLEditorKit
{
	private class WebEditFactory extends HTMLEditorKit.HTMLFactory
	{
		public View create(Element el)
		{
			View view = super.create(el);
			if (view instanceof ParagraphView)
			{
				return new WebEditParagraphView(el);
			}
			return view;
		}
	}
	
	private ViewFactory defaultFactory = new WebEditFactory();
	
	public WebEditEditorKit()
	{
		super();
	}
	
	public ViewFactory getViewFactory()
	{
		return defaultFactory;
	}
}
