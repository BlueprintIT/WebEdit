/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.awt.Cursor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JToggleButton;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;

import org.apache.log4j.Logger;

import com.blueprintit.webedit.htmlkit.WebEditEditorKit;
import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;
import com.blueprintit.xui.UserInterface;

public class EditorUI implements InterfaceListener
{
	private Logger log = Logger.getLogger(this.getClass());

	public JEditorPane editorPane;
	public HTMLEditorKit editorKit;
	public UserInterface ui;
	
	public JComboBox style;
	
	public JToggleButton leftAlign;
	public JToggleButton rightAlign;
	public JToggleButton centerAlign;
	public JToggleButton justifyAlign;
	
	public JToggleButton bold;
	public JToggleButton italic;
	public JToggleButton underline;
	
	private String resources = "com/blueprintit/webedit";
	
	public Action saveAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			log.info(editorPane.getText());
		}
	};
	
	public Action applyStyleAction = new ParagraphStyleAction()
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};

	public Action leftAlignAction = new ParagraphAlignAction(StyleConstants.ALIGN_LEFT)
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action centerAlignAction = new ParagraphAlignAction(StyleConstants.ALIGN_CENTER)
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action rightAlignAction = new ParagraphAlignAction(StyleConstants.ALIGN_RIGHT)
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action justifiedAlignAction = new ParagraphAlignAction(StyleConstants.ALIGN_JUSTIFIED)
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};

	public Action boldAction = new StyledEditorKit.BoldAction()
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action italicAction = new StyledEditorKit.ItalicAction()
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action underlineAction = new StyledEditorKit.UnderlineAction()
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	
	public EditorUI()
	{
		setupToolbarButton(leftAlignAction,"","Left Align","icons/left-align.gif");
		setupToolbarButton(centerAlignAction,"","Center Align","icons/center-align.gif");
		setupToolbarButton(rightAlignAction,"","Right Align","icons/right-align.gif");
		setupToolbarButton(justifiedAlignAction,"","Justify","icons/justified-align.gif");

		setupToolbarButton(boldAction,"","Bold","icons/bold.gif");
		setupToolbarButton(italicAction,"","Italic","icons/italic.gif");
		setupToolbarButton(underlineAction,"","Underline","icons/underline.gif");
	}
	
	private void setupToolbarButton(Action action, String name, String tooltip, String icon)
	{
		Icon ic = new ImageIcon(this.getClass().getClassLoader().getResource(resources+"/"+icon));
		action.putValue(Action.SMALL_ICON,ic);
		action.putValue(Action.SHORT_DESCRIPTION,tooltip);
		action.putValue(Action.NAME,name);
	}
	
	private void compareBlockAttribute(MutableAttributeSet attrs, Element el, Object key, boolean first)
	{
		compareAttribute(attrs,findBlockElement(el),key,first);
	}
	
	private void compareAttribute(MutableAttributeSet attrs, Element el, Object key, boolean first)
	{
		Object value = attrs.getAttribute(key);
		if ((first)||(value!=null))
		{
			Object newvalue = el.getAttributes().getAttribute(key);
			if (newvalue!=null)
			{
				if (first)
				{
					attrs.addAttribute(key,newvalue);
				}
				else if (newvalue!=value)
				{
					attrs.removeAttribute(key);
				}
			}
		}
	}
	
	private Element findBlockElement(Element el)
	{
		HTML.Tag tag = (HTML.Tag)el.getAttributes().getAttribute(StyleConstants.NameAttribute);
		if (tag.isBlock())
		{
			return el;
		}
		else
		{
			return findBlockElement(el.getParentElement());
		}
	}
	
	public void build(MutableAttributeSet attr, Element el, boolean first)
	{
		compareBlockAttribute(attr,el,StyleConstants.NameAttribute,first);
		compareBlockAttribute(attr,el,HTML.Attribute.CLASS,first);
		compareAttribute(attr,el,StyleConstants.Alignment,first);
		compareAttribute(attr,el,StyleConstants.Bold,first);
		compareAttribute(attr,el,StyleConstants.Italic,first);
		compareAttribute(attr,el,StyleConstants.Underline,first);
	}
	
	public AttributeSet buildAttributeSet(Element[] elements)
	{
		if (elements.length>0)
		{
			MutableAttributeSet attr = new SimpleAttributeSet();
			build(attr,elements[0],true);
			for (int i=1; i<elements.length; i++)
			{
				build(attr,elements[i],false);
			}
			return attr;
		}
		else
		{
			return new SimpleAttributeSet();
		}
	}
	
	public void updateToolbar()
	{
		Caret caret = editorPane.getCaret();
		AttributeSet attr;
		if (caret.getDot()==caret.getMark())
		{
			Element el = ((HTMLDocument)editorPane.getDocument()).getCharacterElement(caret.getDot());
			Element blockel = findBlockElement(el);
			AttributeSet style = ((HTMLDocument)editorPane.getDocument()).getStyleSheet().getRule((HTML.Tag)blockel.getAttributes().getAttribute(StyleConstants.NameAttribute),blockel);
			if (style.isDefined(StyleConstants.Alignment))
			{
				System.out.println("Alignment is "+StyleConstants.getAlignment(style));
			}
			attr = buildAttributeSet(new Element[] {el});
		}
		else
		{
			attr = new SimpleAttributeSet();
		}
		
		int align = -1;
		if (attr.isDefined(StyleConstants.Alignment))
		{
			align=StyleConstants.getAlignment(attr);
		}
		leftAlign.setSelected(align==StyleConstants.ALIGN_LEFT);
		centerAlign.setSelected(align==StyleConstants.ALIGN_CENTER);
		rightAlign.setSelected(align==StyleConstants.ALIGN_RIGHT);
		justifyAlign.setSelected(align==StyleConstants.ALIGN_JUSTIFIED);
		
		bold.setSelected(StyleConstants.isBold(attr));
		italic.setSelected(StyleConstants.isItalic(attr));
		underline.setSelected(StyleConstants.isUnderline(attr));
		
		boolean selected=false;
		HTML.Tag tag = (Tag)attr.getAttribute(StyleConstants.NameAttribute);
		String classname = (String)attr.getAttribute(HTML.Attribute.CLASS);
		StyleModel styles = (StyleModel)style.getModel();
		if (tag!=null)
		{
			for (int i=0; i<styles.getSize(); i++)
			{
				StyleModel.Style style = (StyleModel.Style)styles.getElementAt(i);
				if ((tag.equals(style.getTag()))&&(classname==null ? style.getClassName()==null : classname.equals(style.getClassName())))
				{
					styles.setSelectedItem(style);
					selected=true;
					break;
				}
			}
		}
		if (!selected)
		{
			styles.setSelectedItem(null);
		}
	}
	
	public void interfaceCreated(InterfaceEvent ev)
	{
		ui=ev.getUserInterface();
		editorKit = new WebEditEditorKit();
		editorPane.setEditorKit(editorKit);
		editorKit.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		editorPane.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e)
			{
				updateToolbar();
			}
		});
		//updateToolbar();
	}
}
