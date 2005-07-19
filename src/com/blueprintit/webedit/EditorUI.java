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
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JToggleButton;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.HTML;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;

import com.blueprintit.errors.ErrorReporter;
import com.blueprintit.htmlkit.WebEditDocument;
import com.blueprintit.htmlkit.WebEditEditorKit;
import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;
import com.blueprintit.swim.Request;
import com.blueprintit.swim.SwimInterface;

public class EditorUI implements InterfaceListener
{
	private Logger log = Logger.getLogger(this.getClass());
	private transient boolean updating = false;
	
	class ParagraphStyleAction extends StyledTextAction
	{
		public ParagraphStyleAction()
		{
			super("paragraph-style");
		}
		
		public void actionPerformed(ActionEvent ev)
		{
			if (!updating)
			{
				JEditorPane editor = getEditor(ev);
				JComboBox combo = (JComboBox)ev.getSource();
				StyleModel model = (StyleModel)combo.getModel();
				StyleModel.Style style = (StyleModel.Style)model.getSelectedItem();
				if (style!=null)
				{
					MutableAttributeSet attr = new SimpleAttributeSet();
					style.apply(attr);
					this.setParagraphAttributes(editor,attr,true);
			}
			}
		}
	}

	private SwimInterface swim;
	private String resource;
	private String attachments;
	private String htmlPath;
	private String stylePath;

	private HTMLEditorKit editorKit;
	private WebEditDocument document;
	private Element body;
	private StyleSheet stylesheet;

	public JComboBox style;
	
	public JToggleButton link;
	
	public JButton cut;
	public JButton copy;
	public JButton paste;
	
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
	
	public Action dumpAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			System.out.println(editorPane.getText());
		}
	};
	
	public Action cancelAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			context.showDocument(cancelURL);
		}
	};
	
	public Action attachmentAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			AttachmentDialog dlg = new AttachmentDialog(swim,attachments);
			dlg.show();
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
		
	public Action orderedListAction = new WebEditEditorKit.ToggleOrderedListAction() {
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action unorderedListAction = new WebEditEditorKit.ToggleUnorderedListAction() {
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
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

	public Action leftAlignAction = new StyledEditorKit.AlignmentAction("Align Left",StyleConstants.ALIGN_LEFT)
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action centerAlignAction = new StyledEditorKit.AlignmentAction("Align Center",StyleConstants.ALIGN_CENTER)
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action rightAlignAction = new StyledEditorKit.AlignmentAction("Align Right",StyleConstants.ALIGN_RIGHT)
	{
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
		}
	};
	public Action justifiedAlignAction = new StyledEditorKit.AlignmentAction("Align Justified",StyleConstants.ALIGN_JUSTIFIED)
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

	public Action cutAction = new DefaultEditorKit.CutAction();
	public Action copyAction = new DefaultEditorKit.CopyAction();
	public Action pasteAction = new DefaultEditorKit.PasteAction();
	
	private URL cancelURL;

	private URL commitURL;

	private AppletContext context;
	
	public EditorUI(AppletContext context, SwimInterface swim, String resource, String style, URL cancel, URL commit)
	{
		this.swim=swim;
		this.resource=resource;
		this.htmlPath=resource+"/block.html";
		this.attachments=resource+"/attachments";
		this.stylePath=style;
		this.cancelURL=cancel;
		this.commitURL=commit;
		this.context=context;
		
		setupToolbarButton(leftAlignAction,"","Left Align","icons/left-align.gif");
		setupToolbarButton(centerAlignAction,"","Center Align","icons/center-align.gif");
		setupToolbarButton(rightAlignAction,"","Right Align","icons/right-align.gif");
		setupToolbarButton(justifiedAlignAction,"","Justify","icons/justified-align.gif");

		setupToolbarButton(cutAction,"","Cut","icons/cut.gif");
		setupToolbarButton(copyAction,"","Copy","icons/copy.gif");
		setupToolbarButton(pasteAction,"","Paste","icons/paste.gif");

		setupToolbarButton(attachmentAction,"","Attachments","icons/attach.gif");

		setupToolbarButton(orderedListAction,"","Ordered List","icons/ol.gif");
		setupToolbarButton(unorderedListAction,"","Unordered List","icons/ul.gif");

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
		updating=true;
		Caret caret = editorPane.getCaret();
		
		MutableAttributeSet attrs = new SimpleAttributeSet();
		Iterator elements = document.getCharacterElementIterator(caret.getDot(),caret.getMark());
		Element element = (Element)elements.next();
		matchElementAttributes(element,attrs,true);
		while (elements.hasNext())
		{
			element=(Element)elements.next();
			matchElementAttributes(element,attrs,false);
		}
		
		boolean selection = (caret.getDot()!=caret.getMark());
		cut.setEnabled(selection);
		copy.setEnabled(selection);
		
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
		
		elements = document.getParagraphElementIterator(caret.getDot(),caret.getMark());
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
		updating=false;
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
				/*int offset = e.getDot();
				System.out.println("Caret moved to "+offset);
				Element el = document.getCharacterElement(offset);
				while (el!=null)
				{
					System.out.println("Element "+el.getStartOffset()+" "+el.getEndOffset()+" ("+el.getClass().toString()+")");
					HTML.Tag tag = (HTML.Tag)el.getAttributes().getAttribute(StyleConstants.NameAttribute);
					if (tag!=null)
					{
						System.out.println("Element is tag "+tag.toString());
					}
					Enumeration en = el.getAttributes().getAttributeNames();
					while (en.hasMoreElements())
					{
						Object name = en.nextElement();
						if (name!=StyleConstants.NameAttribute)
						{
							System.out.println(name+" = "+el.getAttributes().getAttribute(name));
						}
					}
					el=el.getParentElement();
				}*/
				updateToolbar();
			}
		});
		
		try
		{
			Request req = new Request(swim,"view",stylePath);
			stylesheet = new StyleSheet();
			stylesheet.loadRules(req.openReader(),req.encode());
			
			document=(WebEditDocument)editorKit.createDefaultDocument();
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
