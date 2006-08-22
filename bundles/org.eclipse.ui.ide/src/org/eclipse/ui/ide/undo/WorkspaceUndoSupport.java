/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * WorkspaceUndoSupport defines common utility methods and constants used by
 * clients who create undoable workspace operations.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public class WorkspaceUndoSupport {

	private static ObjectUndoContext tasksUndoContext;

	private static ObjectUndoContext bookmarksUndoContext;

	/**
	 * Return the undo context that should be used for workspace-wide operations
	 * 
	 * @return the undo context suitable for workspace-level operations.
	 */
	public static IUndoContext getWorkspaceUndoContext() {
		return WorkbenchPlugin.getDefault().getOperationSupport()
				.getUndoContext();
	}

	/**
	 * Return the workspace.
	 * 
	 * @return the current workspace.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Return the undo context that should be used for operations involving
	 * tasks.
	 * 
	 * @return the tasks undo context
	 */
	public static IUndoContext getTasksUndoContext() {
		if (tasksUndoContext == null) {
			tasksUndoContext = new ObjectUndoContext(new Object(), "Tasks Context"); //$NON-NLS-1$
			tasksUndoContext.addMatch(getWorkspaceUndoContext());
		}
		return tasksUndoContext;
	}
	
	/**
	 * Return the undo context that should be used for operations involving
	 * bookmarks.
	 * 
	 * @return the bookmarks undo context
	 */
	public static IUndoContext getBookmarksUndoContext() {
		if (bookmarksUndoContext == null) {
			bookmarksUndoContext = new ObjectUndoContext(new Object(), "Bookmarks Context"); //$NON-NLS-1$
			bookmarksUndoContext.addMatch(getWorkspaceUndoContext());
		}
		return bookmarksUndoContext;
	}

	/**
	 * This class should never be constructed.
	 */
	private WorkspaceUndoSupport() {
		// Not allowed.
	}
}
