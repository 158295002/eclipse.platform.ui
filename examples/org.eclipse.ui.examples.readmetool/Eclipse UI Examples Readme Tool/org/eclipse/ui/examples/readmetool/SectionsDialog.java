/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
/**
 * This dialog is an example of a detached window launched
 * from an action in the desktop.
 */
public class SectionsDialog extends Dialog {
	protected IAdaptable input;
/**
 * Creates a new SectionsDialog.
 */
public SectionsDialog(Shell parentShell, IAdaptable input) {
	super(parentShell);
	this.input = input;
}
/* (non-Javadoc)
 * Method declared on Window.
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	newShell.setText(MessageUtil.getString("Readme_Sections")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(newShell, IReadmeConstants.SECTIONS_DIALOG_CONTEXT);
}
/* (non-Javadoc)
 * Method declared on Dialog
 */
protected Control createDialogArea(Composite parent) {
	Composite composite = (Composite)super.createDialogArea(parent);
	
	List list = new List(composite, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_BOTH);
	list.setLayoutData(data);
	ListViewer viewer = new ListViewer(list);
	viewer.setContentProvider(new WorkbenchContentProvider());
	viewer.setLabelProvider(new WorkbenchLabelProvider());
	viewer.setInput(input);

	return composite;
}
}
