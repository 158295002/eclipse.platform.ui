package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Copies a task to the clipboard.
 */
/*package*/ class CopyTaskAction extends TaskAction {
	/**
	 * Creates the action.
	 */
	public CopyTaskAction(TaskList tasklist, String id) {
		super(tasklist, id);
		WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.COPY_TASK_ACTION);
	}
	
	/**
	 * Performs this action.
	 */
	public void run() {
		// Get the selected markers
		TaskList taskList = getTaskList();
		TableViewer viewer = taskList.getTableViewer();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		taskList.cancelEditing();
		List list = selection.toList();
		IMarker[] markers = new IMarker[list.size()];
		list.toArray(markers);

		setClipboard(markers, TaskList.createMarkerReport(markers));

		//Update paste enablement
		taskList.updatePasteEnablement();
	}

	private void setClipboard(IMarker[] markers, String markerReport) {
		try {
			// Place the markers on the clipboard
			Object[] data = new Object[] {
				markers,
				markerReport};				
			Transfer[] transferTypes = new Transfer[] {
				MarkerTransfer.getInstance(),
				TextTransfer.getInstance()};
			
			// set the clipboard contents
			getTaskList().getClipboard().setContents(data, transferTypes);
		} catch (SWTError e){
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw e;
			if (MessageDialog.openQuestion(getShell(), WorkbenchMessages.getString("CopyToClipboardProblemDialog.title"), WorkbenchMessages.getString("CopyToClipboardProblemDialog.message"))) //$NON-NLS-1$ //$NON-NLS-2$
				setClipboard(markers, markerReport);
		}	
	}
}

