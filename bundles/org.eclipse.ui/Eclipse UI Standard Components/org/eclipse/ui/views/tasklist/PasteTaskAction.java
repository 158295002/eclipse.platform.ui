package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Standard action for pasting tasks from the clipboard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
/*package*/ class PasteTaskAction extends TaskAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".TaskPasteAction";//$NON-NLS-1$
	
	/**
	 * System clipboard
	 */
	private Clipboard clipboard;

/**
 * Creates a new action.
 */
public PasteTaskAction(TaskList tasklist, String id) {
	super(tasklist, id);
	clipboard = new Clipboard(Display.getCurrent());
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {
	// Get the markers from the clipboard
	MarkerTransfer transfer = MarkerTransfer.getInstance();
	final IMarker[] markerData = (IMarker[])clipboard.getContents(transfer);
	
	if (markerData == null) 
		return;

 	final ArrayList newMarkers = new ArrayList();

	try {
		getTaskList().getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				TaskList taskList = getTaskList();
				for (int i = 0; i < markerData.length; i++) {
					// Only paste tasks (not problems)
					if (!markerData[i].getType().equals(IMarker.TASK))
						continue;
						
					// Paste to the same resource as the original
					IResource resource = markerData[i].getResource();
					Map attributes = markerData[i].getAttributes();
					IMarker marker = resource.createMarker(IMarker.TASK);
					marker.setAttributes(attributes);
					newMarkers.add(marker);
				}
			}
		}, null);
	} catch (CoreException e) {
		ErrorDialog.openError(
			getShell(),
			TaskListMessages.getString("NewTask.errorMessage"), //$NON-NLS-1$
			null,
			e.getStatus());
		return;
	}

	// Need to do this in an asyncExec, even though we're in the UI thread here,
	// since the task list updates itself with the addition in an asyncExec,
	// which hasn't been processed yet.
	// Must be done outside IWorkspaceRunnable above since notification for add is
	// sent after IWorkspaceRunnable is run.
	if (newMarkers.size() > 0) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				TaskList taskList = getTaskList();
				taskList.setSelection(new StructuredSelection(newMarkers), true);	
			}
		});
	}
}


}

