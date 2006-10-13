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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.ide.undo.ContainerDescription;
import org.eclipse.ui.internal.ide.undo.FolderDescription;
import org.eclipse.ui.internal.ide.undo.ResourceDescription;

/**
 * A CreateFolderOperation represents an undoable operation for creating a
 * folder in the workspace. If a link location is specified, the folder is
 * considered to be linked to the specified location. If a link location is not
 * specified, the folder will be created in the location specified by the
 * handle, and the entire containment path of the folder will be created if it
 * does not exist. Clients may call the public API from a background thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public class CreateFolderOperation extends AbstractCreateResourcesOperation {

	/**
	 * Create a CreateFolderOperation
	 * 
	 * @param folderHandle
	 *            the folder to be created
	 * @param linkLocation
	 *            the location of the folder if it is to be linked
	 * @param label
	 *            the label of the operation
	 */
	public CreateFolderOperation(IFolder folderHandle, IPath linkLocation,
			String label) {
		super(null, label);
		ContainerDescription containerDescription;
		if (linkLocation == null) {
			containerDescription = ContainerDescription
					.fromContainer(folderHandle);
		} else {
			// create a linked folder description
			containerDescription = new FolderDescription(folderHandle,
					linkLocation);
		}
		setResourceDescriptions(new ResourceDescription[] { containerDescription });
	}
}
