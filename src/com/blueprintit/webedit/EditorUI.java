/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.awt.Cursor;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;

import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;

public class EditorUI implements InterfaceListener
{
	private Logger log = Logger.getLogger(this.getClass());

	public JEditorPane editorPane;
	public HTMLEditorKit editorKit;
	
	public Action boldAction = new StyledEditorKit.BoldAction();
	public Action italicAction = new StyledEditorKit.ItalicAction();
	
	public void interfaceCreated(InterfaceEvent ev)
	{
		log.info("Interface created");
		editorKit = (HTMLEditorKit)editorPane.getEditorKit();
		editorKit.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}
}
