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
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;

import com.blueprintit.errors.ErrorReporter;
import com.blueprintit.htmlkit.WebEditDocument;
import com.blueprintit.htmlkit.WebEditEditorKit;
import com.blueprintit.htmlkit.WebEditStyleSheet;
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
	private String attachments;
	private String htmlPath;
	private String stylePath;

	private HTMLEditorKit editorKit;
	private WebEditDocument document;
	private Element body;
	private StyleSheet stylesheet;
	
	private File currentdir = null;

	public JComboBox style;
	
	public JToggleButton link;
	public JToggleButton image;
	public JToggleButton floatLeft;
	public JToggleButton floatNone;
	public JToggleButton floatRight;
	
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
	
	private boolean saveWorking()
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
			return true;
		}
		catch (IOException e)
		{
			if (e.getMessage().startsWith("Server returned HTTP response code: 409 for URL"))
			{
				JOptionPane.showMessageDialog(null,"Another user has taken over editing of this resource, you will be unable to save your changes.","Resource Locked",JOptionPane.ERROR_MESSAGE);
			}
			else if (e.getMessage().startsWith("Server returned HTTP response code: 401 for URL"))
			{
				JOptionPane.showMessageDialog(null,"You are no longer logged in to the server, your session probably expired.","Authentication Required",JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				log.error("Could not store",e);
				ErrorReporter.sendErrorReport(
						"Unable to save","The file could not be saved, probably because the server is currently unavailable.",
						"Swim","WebEdit","Could not save",e);
			}
			return false;
		}
	}
	
	public Action commitAction = new AbstractAction() {
		public void actionPerformed(ActionEvent ev)
		{
			if (saveWorking())
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
			try
			{
				System.out.println(editorPane.getText());
			}
			catch (Exception e)
			{
			}
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
			AttachmentDialog dlg = new AttachmentDialog(swim,currentdir,attachments);
			dlg.show();
		}
	};
	
	public Action imageAction = new StyledTextAction("Image") {
		public void actionPerformed(ActionEvent ev)
		{
			int start = editorPane.getSelectionStart();
			int end = editorPane.getSelectionEnd();
			if (start>end)
			{
				int temp=start;
				start=end;
				end=temp;
			}
			Element el = document.getCharacterElement(start);
			String src = null;
			String alternate="";
			if ((el.getStartOffset()==start)&&(el.getEndOffset()==end))
			{
				if ((!el.getAttributes().isDefined(StyleConstants.NameAttribute))||
					(el.getAttributes().getAttribute(StyleConstants.NameAttribute)!=HTML.Tag.IMG))
				{
					el=null;
				}
				else
				{
					src=(String)el.getAttributes().getAttribute(HTML.Attribute.SRC);
					alternate=(String)el.getAttributes().getAttribute(HTML.Attribute.ALT);
				}
			}
			else
			{
				el=null;
			}
			ImageDialog dlg = new ImageDialog(swim,currentdir,attachments,src,alternate);
			dlg.show();
			currentdir=dlg.currentdir;
			
			if (dlg.result==ImageDialog.RESULT_OK)
			{
				MutableAttributeSet newattrs = new SimpleAttributeSet();
				newattrs.addAttribute(StyleConstants.NameAttribute,HTML.Tag.IMG);
				newattrs.addAttribute(HTML.Attribute.SRC,dlg.path);
				newattrs.addAttribute(HTML.Attribute.ALT,dlg.alternate);
				if (el!=null)
				{
					document.setCharacterAttributes(start,end-start,newattrs,false);
				}
				else
				{
					editorPane.replaceSelection(" ");
					document.setCharacterAttributes(start,1,newattrs,true);
				}
			}
			updateToolbar();
		}
	};
	
	public Action floatLeftAction = new WebEditEditorKit.FloatLeftAction("float-left") {

		public void actionPerformed(ActionEvent e)
		{
			super.actionPerformed(e);
			updateToolbar();
		}
	};
	
	public Action floatNoneAction = new WebEditEditorKit.FloatNoneAction("float-none") {

		public void actionPerformed(ActionEvent e)
		{
			super.actionPerformed(e);
			updateToolbar();
		}
	};
	
	public Action floatRightAction = new WebEditEditorKit.FloatRightAction("float-right") {

		public void actionPerformed(ActionEvent e)
		{
			super.actionPerformed(e);
			updateToolbar();
		}
	};
	
	public Action linkAction = new StyledTextAction("Link") {
		
		private int findEarliestElementWithLink(Element el, String link)
		{
			int pos = el.getStartOffset()-1;
			Element test = document.getCharacterElement(pos);
			AttributeSet attrs = test.getAttributes();
			AttributeSet lattr = (AttributeSet)attrs.getAttribute(HTML.Tag.A);
			if ((lattr!=null)&&(lattr.isDefined(HTML.Attribute.HREF)))
			{
				String check = (String)lattr.getAttribute(HTML.Attribute.HREF);
				if (check.equals(link))
				{
					return findEarliestElementWithLink(test,link);
				}
			}
			return el.getStartOffset();
		}
		
		private int findLatestElementWithLink(Element el, String link)
		{
			int pos = el.getEndOffset();
			Element test = document.getCharacterElement(pos);
			AttributeSet attrs = test.getAttributes();
			AttributeSet lattr = (AttributeSet)attrs.getAttribute(HTML.Tag.A);
			if ((lattr!=null)&&(lattr.isDefined(HTML.Attribute.HREF)))
			{
				String check = (String)lattr.getAttribute(HTML.Attribute.HREF);
				if (check.equals(link))
				{
					return findLatestElementWithLink(test,link);
				}
			}
			return el.getEndOffset();
		}
		
		public void actionPerformed(ActionEvent ev)
		{
			int start = editorPane.getSelectionStart();
			int end = editorPane.getSelectionEnd();
			if (start>end)
			{
				int temp=start;
				start=end;
				end=temp;
			}
			Element el = document.getCharacterElement(start);
			
			String link=null;
			AttributeSet attrs = el.getAttributes();
			AttributeSet lattr = (AttributeSet)attrs.getAttribute(HTML.Tag.A);
			if ((lattr!=null)&&(lattr.isDefined(HTML.Attribute.HREF)))
			{
				link=(String)lattr.getAttribute(HTML.Attribute.HREF);
				start=findEarliestElementWithLink(el,link);
				end=findLatestElementWithLink(el,link);
				editorPane.setSelectionStart(start);
				editorPane.setSelectionEnd(end);
			}
			LinkDialog dlg = new LinkDialog(swim,currentdir,attachments,link);
			dlg.show();
			currentdir=dlg.currentdir;
			
			if (dlg.result==LinkDialog.RESULT_CANCEL)
				return;
			
			if (dlg.result==LinkDialog.RESULT_OK)
			{
				MutableAttributeSet tagattrs = new SimpleAttributeSet();
				tagattrs.addAttribute(HTML.Attribute.HREF,dlg.path);
				MutableAttributeSet replacement = new SimpleAttributeSet();
				replacement.addAttribute(HTML.Tag.A,tagattrs);
				document.setCharacterAttributes(start,end-start,replacement,false);
			}
			else
			{
				document.removeCharacterAttribute(start,end-start,HTML.Tag.A);
			}
		}
	};
		
	public Action increaseListAction = new WebEditEditorKit.IncreaseListAction() {
		public void actionPerformed(ActionEvent ev)
		{
			super.actionPerformed(ev);
			updateToolbar();
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
	private String bodyid;
	private String styleml;
	
	public EditorUI(AppletContext context, SwimInterface swim, String resource, String html, String style, String styleml, URL cancel, URL commit, String bodyid)
	{
		this.bodyid=bodyid;
		this.swim=swim;
		this.htmlPath=resource+"/file/"+html;
		this.attachments=resource+"/file/attachments";
		this.stylePath=style;
		this.styleml=styleml;
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

		setupToolbarButton(attachmentAction,"","View attachments","icons/attach.gif");
		setupToolbarButton(linkAction,"","Make the selection a link.","icons/link.gif");

		setupToolbarButton(imageAction,"","Add an image.","icons/image.gif");

		setupToolbarButton(floatLeftAction,"","Float image left","icons/float-left.gif");
		setupToolbarButton(floatRightAction,"","Float image right","icons/float-right.gif");
		setupToolbarButton(floatNoneAction,"","Put image on the text line","icons/float-none.gif");

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
		matchElementAttribute(element,attrs,first,StyleConstants.NameAttribute);
		matchElementAttribute(element,attrs,first,HTML.Tag.A);
		matchElementAttribute(element,attrs,first,CSS.Attribute.FLOAT);
		matchElementAttribute(element,attrs,first,StyleConstants.Alignment);
		matchElementAttribute(element,attrs,first,StyleConstants.Bold);
		matchElementAttribute(element,attrs,first,StyleConstants.Italic);
		matchElementAttribute(element,attrs,first,StyleConstants.Underline);
		/*if (first)
		{
			attrs.addAttributes(element.getAttributes());
		}
		else
		{
			AttributeSet elattrs = element.getAttributes();
			Enumeration en = attrs.getAttributeNames();
			while (en.hasMoreElements())
			{
				Object attr = en.nextElement();
				if (elattrs.isDefined(attr))
				{
					if (!attrs.getAttribute(attr).equals(elattrs.getAttribute(attr)))
					{
						attrs.removeAttribute(attr);
					}
				}
				else
				{
					attrs.removeAttribute(attr);
				}
			}
		}*/
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
		boolean fixed = style!=null ? style.isFixed() : false;

		boolean selection = (caret.getDot()!=caret.getMark());
		boolean singlepara = document.getParagraphElement(caret.getDot())==document.getParagraphElement(caret.getMark());
		
		cut.setEnabled(selection);
		copy.setEnabled(selection);
		
		HTML.Tag tag = null;
		if (attrs.isDefined(StyleConstants.NameAttribute))
		{
			tag=(HTML.Tag)attrs.getAttribute(StyleConstants.NameAttribute);
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
		leftAlignAction.setEnabled(!fixed);
		centerAlignAction.setEnabled(!fixed);
		rightAlignAction.setEnabled(!fixed);
		justifiedAlignAction.setEnabled(!fixed);
		
		bold.setSelected(StyleConstants.isBold(attrs));
		italic.setSelected(StyleConstants.isItalic(attrs));
		underline.setSelected(StyleConstants.isUnderline(attrs));
		boldAction.setEnabled(!fixed);
		italicAction.setEnabled(!fixed);
		underlineAction.setEnabled(!fixed);
		
		link.setSelected(attrs.isDefined(HTML.Tag.A));
		link.setEnabled(link.isSelected()||(selection&&singlepara));
		
		boolean selectedImage = (tag==HTML.Tag.IMG)&&selection;
		image.setSelected(selectedImage);
		if (selectedImage)
		{
			boolean floated=false;
			boolean floatleft=false;
			if (attrs.isDefined(CSS.Attribute.FLOAT))
			{
				String dir = ((String)attrs.getAttribute(CSS.Attribute.FLOAT)).toLowerCase();
				if (dir.equals("left"))
				{
					floatleft=true;
					floated=true;
				}
				else if (dir.equals("right"))
				{
					floatleft=false;
					floated=true;
				}
			}
			floatLeft.setSelected(floated&&floatleft);
			floatNone.setSelected(!floated);
			floatRight.setSelected(floated&&(!floatleft));
		}
		else
		{
			floatLeft.setSelected(false);
			floatNone.setSelected(false);
			floatRight.setSelected(false);
		}
		floatLeft.setEnabled(selectedImage);
		floatNone.setEnabled(selectedImage);
		floatRight.setEnabled(selectedImage);
		
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
				Element par = document.getParagraphElement(offset);
				
				Style style = document.getStyleSheet().getRule((HTML.Tag)par.getAttributes().getAttribute(StyleConstants.NameAttribute),el);
				
				Enumeration enu = style.getAttributeNames();
				while (enu.hasMoreElements())
				{
					Object name = enu.nextElement();
					Object value = style.getAttribute(name);
					System.out.println(name+" - "+value);
				}
				System.out.println();
				
				while (el!=null)
				{
					System.out.println("Element "+el.getStartOffset()+" "
															+el.getEndOffset()+" "+el.getElementCount()+" ("
															+el.getClass().toString()+")");
					HTML.Tag tag = (HTML.Tag) el.getAttributes().getAttribute(
							StyleConstants.NameAttribute);
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
							System.out.println(name.getClass().getName()+" = "
																	+el.getAttributes().getAttribute(name));
						}
					}
					el = el.getParentElement();
				}*/
				updateToolbar();
			}
		});
		
		StyleModel styles = new StyleModel();
		style.setModel(styles);
		if (styleml!=null)
		{
			try
			{
				Request req = new Request(swim, "view", styleml);
				styles.loadFrom(req.openReader());
			}
			catch (Exception e)
			{
				log.warn("Could not load style configuration", e);
			}
		}

		try
		{
			document = ((WebEditEditorKit) editorKit).createDefaultDocument(bodyid);

			List stylesheets = new LinkedList();
			StringTokenizer tokens = new StringTokenizer(stylePath, ",");
			while (tokens.hasMoreElements())
			{
				stylesheets.add(tokens.nextElement());
			}
			Collections.reverse(stylesheets);
			Iterator it = stylesheets.iterator();
			while (it.hasNext())
			{
				String spath = (String)it.next();
				try
				{
					Request req = new Request(swim, "view", spath);
					stylesheet = new WebEditStyleSheet();
					stylesheet.loadRules(req.openReader(), req.encode());
					document.getStyleSheet().addStyleSheet(stylesheet);
					if (spath.endsWith(".css"))
					{
						spath = spath.substring(0, spath.length()-3)+"sml";
						try
						{
							req = new Request(swim, "view", spath);
							styles.loadFrom(req.openReader());
						}
						catch (Exception e)
						{
							log.debug("Could not load style configuration", e);
						}
					}
				}
				catch (Exception e)
				{
					log.warn("Could not load stylesheet "+spath, e);
				}
			}

			Request req = new Request(swim, "view", "version/temp/"+htmlPath);
			document.setBase(req.encode());
			editorPane.setDocument(document);

			body = findBody(document.getDefaultRootElement());
			StringBuffer html = new StringBuffer(swim.getResource(htmlPath, "temp"));
			document.setInnerHTML(body, html.toString());
		}
		catch (Exception e)
		{
			log.error(e);
			e.printStackTrace();
			ErrorReporter
					.sendErrorReport(
							"Error loading content",
							"The text to be edited could not be loaded. The server could be down or misconfigured.",
							"Swim", "WebEdit", "Could not load content", e);
		}
		updateToolbar();
	}
}
