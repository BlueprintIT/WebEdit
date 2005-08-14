package com.blueprintit.webedit;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.blueprintit.swim.RemoteFile;
import com.blueprintit.swim.SwimInterface;
import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;
import com.blueprintit.xui.UserInterface;

public class ImageDialog implements InterfaceListener
{
	private Logger log = Logger.getLogger(this.getClass());

	public JTextField textAttachment;
	public JTextField textExternal;
	public JTextField textAlternate;

	public JRadioButton radioExternal;
	public JRadioButton radioAttachment;
	
	public JDialog dialog;
	
	public ButtonGroup group;
	
	private SwimInterface swim;
	
	public String path;
	private String attachments;
	public String alternate;
	
	public int result = RESULT_CANCEL;
	
	public static int RESULT_CANCEL = 0;
	public static int RESULT_OK = 1;
	public static int RESULT_DELETE = 2;
	
	public File currentdir;

	public ImageDialog(SwimInterface swim, File current, String attachments, String path, String alternate)
	{
		this.swim=swim;
		this.path=path;
		this.attachments=attachments;
		this.currentdir=current;
		this.alternate=alternate;
	}
	
	private void radioSelectionChanged()
	{
		changeExternalAction.setEnabled(radioExternal.isSelected());
		changeAttachmentAction.setEnabled(radioAttachment.isSelected());
	}

	public Action changeExternalAction = new AbstractAction("Change...") {
		public void actionPerformed(ActionEvent e)
		{
			String text = JOptionPane.showInputDialog("Enter a URL for this link:",textExternal.getText());
			if (text!=null)
			{
				textExternal.setText(text);
				path=text;
			}
		}
	};
	
	public Action radioExternalAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e)
		{
			radioSelectionChanged();
		}
	};
	
	public Action changeAttachmentAction = new AbstractAction("Change...") {
		public void actionPerformed(ActionEvent e)
		{
			AttachmentDialog dlg = new AttachmentDialog(swim,currentdir,attachments);
			RemoteFile file = dlg.select();
			currentdir=dlg.getCurrentDirectory();
			if (file!=null)
			{
				textAttachment.setText(file.getName());
				path="attachments/"+file.getName();
			}
		}
	};
	
	public Action radioAttachmentAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e)
		{
			radioSelectionChanged();
		}
	};
	
	public Action okAction = new AbstractAction("OK") {
		public void actionPerformed(ActionEvent e)
		{
			result=RESULT_OK;
			alternate=textAlternate.getText();
			dialog.setVisible(false);
		}
	};
	
	public Action cancelAction = new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent e)
		{
			result=RESULT_CANCEL;
			dialog.setVisible(false);
		}
	};
	
	public void show()
	{
		try
		{
			UserInterface ui = new UserInterface(this);
			ui.showModal();
		}
		catch (Exception e)
		{
			log.error(e);
		}
	}

	public void interfaceCreated(InterfaceEvent ev)
	{
		textAlternate.setText(alternate);
		if (path!=null)
		{
			if (path.indexOf("://")>0)
			{
				radioExternal.setSelected(true);
				textExternal.setText(path);
			}
			else if (path.startsWith("attachments/"))
			{
				radioAttachment.setSelected(true);
				textAttachment.setText(path.substring(12));
			}
		}
		radioSelectionChanged();
	}
}
