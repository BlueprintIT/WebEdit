package com.blueprintit.webedit;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.blueprintit.swim.Page;
import com.blueprintit.swim.PageBrowser;
import com.blueprintit.swim.RemoteFile;
import com.blueprintit.swim.SwimInterface;
import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;
import com.blueprintit.xui.UserInterface;

public class LinkDialog implements InterfaceListener
{
	private Logger log = Logger.getLogger(this.getClass());

	public JTextField textAttachment;
	public JTextField textInternal;
	public JTextField textExternal;

	public JRadioButton radioInternal;
	public JRadioButton radioExternal;
	public JRadioButton radioAttachment;
	
	public JDialog dialog;
	
	public ButtonGroup group;
	
	public JButton btnDelete;

	private SwimInterface swim;
	
	public String path;
	private String attachments;
	
	File currentdir;

	public int result = RESULT_CANCEL;
	
	public static int RESULT_CANCEL = 0;
	public static int RESULT_OK = 1;
	public static int RESULT_DELETE = 2;
	
	public LinkDialog(SwimInterface swim, File current, String attachments, String path)
	{
		this.swim=swim;
		this.path=path;
		this.attachments=attachments;
		this.currentdir=current;
	}
	
	private void radioSelectionChanged()
	{
		browseInternalAction.setEnabled(radioInternal.isSelected());
		changeExternalAction.setEnabled(radioExternal.isSelected());
		changeAttachmentAction.setEnabled(radioAttachment.isSelected());
	}

	public Action browseInternalAction = new AbstractAction("Browse...") {
		public void actionPerformed(ActionEvent e)
		{
			PageBrowser dlg = swim.getPageBrowser();
			Page page = dlg.choosePage(path);
			if (page!=null)
			{
				path="/"+page.getResource();
				textInternal.setText(page.getTitle());
			}
		}
	};
	
	public Action radioInternalAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e)
		{
			radioSelectionChanged();
		}
	};
	
	public Action changeExternalAction = new AbstractAction("Change...") {
		public void actionPerformed(ActionEvent e)
		{
			String text = JOptionPane.showInputDialog("Enter a URL for this link:",textExternal.getText());
			if (text!=null)
			{
				path=text;
				textExternal.setText(text);
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
				path="attachments/"+file.getName();
				textAttachment.setText(file.getName());
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
	
	public Action deleteAction = new AbstractAction("Delete Link") {
		public void actionPerformed(ActionEvent e)
		{
			result=RESULT_DELETE;
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
		if (path!=null)
		{
			if (path.startsWith("/"))
			{
				Page page = swim.getPage(path.substring(1));
				if (page!=null)
				{
					textInternal.setText(page.getTitle());
				}
				radioInternal.setSelected(true);
				btnDelete.setVisible(true);
			}
			else if (path.indexOf("://")>0)
			{
				radioExternal.setSelected(true);
				textExternal.setText(path);
				btnDelete.setVisible(true);
			}
			else if (path.startsWith("attachments/"))
			{
				radioAttachment.setSelected(true);
				textAttachment.setText(path.substring(12));
				btnDelete.setVisible(true);
			}
		}
		radioSelectionChanged();
	}
}
