package com.blueprintit.webedit;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EventObject;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.blueprintit.swim.RemoteDir;
import com.blueprintit.swim.RemoteFile;
import com.blueprintit.swim.SwimInterface;
import com.blueprintit.xui.InterfaceEvent;
import com.blueprintit.xui.InterfaceListener;
import com.blueprintit.xui.UserInterface;

public class AttachmentDialog implements InterfaceListener
{
	private SwimInterface swim;
	private String path;
	private String returnPath = null;
	
	private RemoteDir attachments = null;
	
	public JList fileList;
	public JDialog dialog;

	private Logger log = Logger.getLogger(this.getClass());

	public AttachmentDialog(SwimInterface swim, String path)
	{
		this.swim=swim;
		this.path=path;
	}
	
	public Action okAction = new AbstractAction("OK") {
		public void actionPerformed(ActionEvent ev)
		{
			if (fileList.getSelectedIndex()>=0)
			{
				RemoteFile file = (RemoteFile)fileList.getSelectedValue();
				returnPath=file.getPath();
			}
			dialog.setVisible(false);
		}
	};
	
	public Action addAction = new AbstractAction("Add...") {
		public void actionPerformed(ActionEvent ev)
		{
			JFileChooser dlg = new JFileChooser();
			dlg.setMultiSelectionEnabled(true);
			dlg.setDialogTitle("Select a File");
			if (dlg.showOpenDialog(dialog)==JFileChooser.APPROVE_OPTION)
			{
				File[] files = dlg.getSelectedFiles();
				if (files.length>0)
				{
					try
					{
						UploadProgress uploader = new UploadProgress(swim);
						uploader.addUploadCompleteListener(new UploadCompleteListener() {
							public void uploadComplete(EventObject ev)
							{
								updateList();
							}
						});
						uploader.upload(path,files);
					}
					catch (Exception e)
					{
						log.error(e);
					}
				}
			}
		}
	};
	
	public Action deleteAction = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent ev)
		{
			if (fileList.getSelectedIndex()>=0)
			{
				RemoteFile file = (RemoteFile)fileList.getSelectedValue();
				file.delete();
				updateList();
			}
		}
	};
	
	public String select()
	{
		show();
		return returnPath;
	}
	
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
	
	public void updateList()
	{
		DefaultListModel model = (DefaultListModel)fileList.getModel();
		model.clear();
		if (attachments==null)
		{
			Object entry = swim.getEntry(path,"temp");
			if (entry instanceof RemoteDir)
				attachments=(RemoteDir)entry;
		}
		else
		{
			attachments.refresh();
		}
		if (attachments!=null)
		{
			Iterator it = attachments.files();
			while (it.hasNext())
			{
				RemoteFile file = (RemoteFile)it.next();
				model.addElement(file);
			}
		}
	}
	
	public void interfaceCreated(InterfaceEvent ev)
	{
		updateList();
		deleteAction.setEnabled(fileList.getSelectedIndex()>=0);
		fileList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e)
			{
				deleteAction.setEnabled(fileList.getSelectedIndex()>=0);
			}
		});
	}
}
