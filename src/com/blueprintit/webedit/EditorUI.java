/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.applet.AppletContext;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

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
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.log4j.Logger;

import com.blueprintit.errors.ErrorReporter;
import com.blueprintit.htmlkit.WebEditEditorKit;
import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;
import com.blueprintit.swim.Request;
import com.blueprintit.swim.SwimInterface;

public class EditorUI implements InterfaceListener
{
	private Logger log = Logger.getLogger(this.getClass());

	private SwimInterface swim;
	private String htmlPath;
	private String stylePath;

	private HTMLEditorKit editorKit;
	private HTMLDocument document;
	private Element body;
	private StyleSheet stylesheet;

	public JComboBox style;
	
	public JToggleButton leftAlign;
	public JToggleButton rightAlign;
	public JToggleButton centerAlign;
	public JToggleButton justifyAlign;
	
	public JToggleButton bold;
	public JToggleButton italic;
	public JToggleButton underline;

	public JEditorPane editorPane;

	private String resources = "com/blueprintit/webedit";
	
	private void saveWorking()
	{
		StringBuffer text = new StringBuffer(editorPane.getText());
		int pos = text.indexOf("<body");
		int lpos = text.indexOf(">",pos);
		text.delete(0,lpos+1);
		pos = text.indexOf("</body>");
		text.delete(pos,text.length());
		try
		{
			log.debug("Opening writer");
			Request request = swim.getRequest(htmlPath);
			request.addParameter("version","temp");
			Writer writer = request.openWriter();
			log.debug("Writing text");
			writer.write(text.toString());
			log.debug("Closing");
			writer.close();
		}
		catch (IOException e)
		{
			log.error("Could not store",e);
			ErrorReporter.sendErrorReport(
					"Unable to save","The file could not be saved, probably because the server is currently unavailable.",
					"Swim","WebEdit","Could not save",e);
		}
	}
	
	public Action commitAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			saveWorking();
			context.showDocument(commitURL);
		}
	};
	
	public Action saveAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			saveWorking();
		}
	};
	
	public Action cancelAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			context.showDocument(cancelURL);
		}
	};
	
	public Action linkAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			log.info("Attempting to load page browser");
			try
			{
				swim.getPageBrowser().choosePage();
			}
			catch (Exception e)
			{
				log.warn("Error opening page browser",e);
				ErrorReporter.sendErrorReport(
						"Error loading page browser","Due to an unknown reason, the page browser could not be loaded.",
						"Swim","WebEdit","Could not load page browser",e);
			}
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

	private URL cancelURL;

	private URL commitURL;

	private AppletContext context;
	
	public EditorUI(AppletContext context, SwimInterface swim, String path, String style, URL cancel, URL commit)
	{
		this.swim=swim;
		this.htmlPath=path;
		this.stylePath=style;
		this.cancelURL=cancel;
		this.commitURL=commit;
		this.context=context;
		
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
	
	private Iterator getCharacterElementIterator(int start, int end)
	{
		if (end<start)
		{
			int temp = start;
			start=end;
			end=temp;
		}
		LinkedList elements = new LinkedList();
		Element element = document.getCharacterElement(start);
		elements.add(element);
		while (element.getEndOffset()<end)
		{
			int pos=element.getEndOffset();
			element=document.getCharacterElement(pos);
			elements.add(element);
		}
		return elements.iterator();
	}
	
	private Iterator getParagraphElementIterator(int start, int end)
	{
		if (end<start)
		{
			int temp = start;
			start=end;
			end=temp;
		}
		LinkedList elements = new LinkedList();
		Element element = document.getParagraphElement(start);
		elements.add(element);
		while (element.getEndOffset()<end)
		{
			int pos=element.getEndOffset();
			element=document.getParagraphElement(pos);
			elements.add(element);
		}
		return elements.iterator();
	}
	
	private Object findAttribute(Element element, Object attribute)
	{
		AttributeSet attrs = element.getAttributes();
		if (attrs!=null)
		{
			if (attrs.isDefined(attribute))
				return attrs.getAttribute(attribute);

			if (attrs.isDefined(StyleConstants.NameAttribute))
			{
				Object tag = attrs.getAttribute(StyleConstants.NameAttribute);
				if (tag instanceof HTML.Tag)
				{
					attrs = stylesheet.getRule((HTML.Tag)tag,element);
					if ((attrs!=null)&&(attrs.isDefined(attribute)))
						return attrs.getAttribute(attribute);
				}
			}
		}
		element=element.getParentElement();
		if (element!=null)
			return findAttribute(element,attribute);
		
		return null;
	}
	
	private void matchElementAttribute(Element element, MutableAttributeSet attrs, boolean first, Object attribute)
	{
		if ((first)||(attrs.isDefined(attribute)))
		{
			Object value = findAttribute(element,attribute);
			if (value!=null)
			{
				if (first)
				{
					attrs.addAttribute(attribute,value);
				}
				else
				{
					if (!value.equals(attrs.getAttribute(attribute)))
						attrs.removeAttribute(attribute);
				}
			}
		}
	}
	
	private void matchElementAttributes(Element element, MutableAttributeSet attrs, boolean first)
	{
		matchElementAttribute(element,attrs,first,StyleConstants.Alignment);
		matchElementAttribute(element,attrs,first,StyleConstants.Bold);
		matchElementAttribute(element,attrs,first,StyleConstants.Italic);
		matchElementAttribute(element,attrs,first,StyleConstants.Underline);
	}
	
	private void updateToolbar()
	{
		Caret caret = editorPane.getCaret();
		
		MutableAttributeSet attrs = new SimpleAttributeSet();
		Iterator elements = getCharacterElementIterator(caret.getDot(),caret.getMark());
		Element element = (Element)elements.next();
		matchElementAttributes(element,attrs,true);
		while (elements.hasNext())
		{
			element=(Element)elements.next();
			matchElementAttributes(element,attrs,false);
		}
		
		int align = -1;
		if (attrs.isDefined(StyleConstants.Alignment))
		{
			align=StyleConstants.getAlignment(attrs);
		}
		leftAlign.setSelected(align==StyleConstants.ALIGN_LEFT);
		centerAlign.setSelected(align==StyleConstants.ALIGN_CENTER);
		rightAlign.setSelected(align==StyleConstants.ALIGN_RIGHT);
		justifyAlign.setSelected(align==StyleConstants.ALIGN_JUSTIFIED);
		
		bold.setSelected(StyleConstants.isBold(attrs));
		italic.setSelected(StyleConstants.isItalic(attrs));
		underline.setSelected(StyleConstants.isUnderline(attrs));
		
		elements = getParagraphElementIterator(caret.getDot(),caret.getMark());
		StyleModel styles = (StyleModel)style.getModel();
		element = (Element)elements.next();
		StyleModel.Style style = styles.getStyle(element);
		while (elements.hasNext())
		{
			element=(Element)elements.next();
			StyleModel.Style nstyle = styles.getStyle(element);
			if (!style.equals(nstyle))
			{
				style=null;
				break;
			}
		}
		styles.setSelectedItem(style);
	}
	
	private Element findBody(Element element)
	{
		AttributeSet attrs = element.getAttributes();
		if (attrs.getAttribute(StyleConstants.NameAttribute)==HTML.Tag.BODY)
		{
			return element;
		}
		else
		{
			for (int i=0; i<element.getElementCount(); i++)
			{
				Element test = findBody(element.getElement(i));
				if (test!=null)
				{
					return test;
				}
			}
		}
		return null;
	}
	
	public void interfaceCreated(InterfaceEvent ev)
	{
		editorKit = new WebEditEditorKit();
		editorPane.setEditorKit(editorKit);
		editorKit.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		editorPane.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e)
			{
				updateToolbar();
			}
		});
		try
		{
			Request req = new Request(swim,"view",stylePath);
			stylesheet = new StyleSheet();
			stylesheet.loadRules(req.openReader(),req.encode());
			
			document=(HTMLDocument)editorKit.createDefaultDocument();
			document.getStyleSheet().addStyleSheet(stylesheet);
			editorPane.setDocument(document);
			
			body=findBody(document.getDefaultRootElement());
			StringBuffer html = new StringBuffer(swim.getResource(htmlPath,"temp"));
			//html.insert(0,"<div id=\"content\" class=\"block\">\n");
			//html.append("</div>\n");
			document.setInnerHTML(body,html.toString());
		}
		catch (Exception e)
		{
			log.error(e);
			e.printStackTrace();
			ErrorReporter.sendErrorReport(
					"Error loading content","The text to be edited could not be loaded. The server could be down or misconfigured.",
					"Swim","WebEdit","Could not load content",e);
		}
		//updateToolbar();
	}
}
