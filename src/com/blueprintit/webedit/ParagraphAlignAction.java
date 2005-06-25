/*
 * $HeadURL$
 * $LastChangedBy$
 * $Date$
 * $Revision$
 */
package com.blueprintit.webedit;

import javax.swing.text.StyledEditorKit.AlignmentAction;

public class ParagraphAlignAction extends AlignmentAction
{
	public ParagraphAlignAction(int alignment)
	{
		super("paragraph-align",alignment);
	}
	
	/*public void actionPerformed(ActionEvent ev)
	{
		JEditorPane editor = getEditor(ev);
		MutableAttributeSet attr = new SimpleAttributeSet();
		switch (alignment)
		{
			case StyleConstants.ALIGN_LEFT:
				attr.addAttribute(HTML.Attribute.STYLE,"text-align: left");
				break;
			case StyleConstants.ALIGN_CENTER:
				attr.addAttribute(HTML.Attribute.STYLE,"text-align: center");
				break;
			case StyleConstants.ALIGN_RIGHT:
				attr.addAttribute(HTML.Attribute.STYLE,"text-align: right");
				break;
			case StyleConstants.ALIGN_JUSTIFIED:
				attr.addAttribute(HTML.Attribute.STYLE,"text-align: justified");
				break;
		}
		setParagraphAttributes(editor,attr,false);
	}*/
}
