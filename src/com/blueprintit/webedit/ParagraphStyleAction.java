/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.HTMLTextAction;

public class ParagraphStyleAction extends HTMLTextAction
{
	public ParagraphStyleAction()
	{
		super("paragraph-style");
	}
	
	public void actionPerformed(ActionEvent ev)
	{
		JEditorPane editor = getEditor(ev);
		JComboBox combo = (JComboBox)ev.getSource();
		StyleModel model = (StyleModel)combo.getModel();
		StyleModel.Style style = (StyleModel.Style)model.getSelectedItem();
		MutableAttributeSet attr = new SimpleAttributeSet();
		attr.addAttribute(StyleConstants.NameAttribute,style.getTag());
		if (style.getClassName()!=null)
		{
			attr.addAttribute(HTML.Attribute.CLASS,style.getClassName());
		}
		setParagraphAttributes(editor,attr,false);
	}
}
