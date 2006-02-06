/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ui.internal.navigator.dnd;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;


/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorDropSelectionDialog extends Dialog {

	public static final String SKIP_ON_SINGLE_SELECTION = "NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION";  //$NON-NLS-1$

	private DropHandlerDescriptor[] descriptors;
	private Button[] radios;
	private Button skipDialogOnSingleSelection;
	private Text descriptionText;
	private DropHandlerDescriptor selectedDescriptor;
	private boolean checkedDefault = false;

	public NavigatorDropSelectionDialog(Shell parentShell, DropHandlerDescriptor[] descriptors) {
		super(parentShell);
		this.descriptors = descriptors;
	}

	protected Control createDialogArea(Composite parent) {
		getShell().setText(CommonNavigatorMessages.NavigatorDropSelectionDialog_1);
		Composite superComposite = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(superComposite, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		radios = new Button[descriptors.length];

		Group radioGroup = new Group(composite, SWT.SHADOW_NONE);
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginHeight = 0;
		groupLayout.marginWidth = 0;
		groupLayout.verticalSpacing = 0;
		groupLayout.horizontalSpacing = 0;
		groupLayout.numColumns = 1;
		radioGroup.setLayout(groupLayout);

		final int arrayLength = descriptors.length; // = radios.length
		for (int i = 0; i < arrayLength; i++) {
			radios[i] = new Button(radioGroup, SWT.RADIO);
			radios[i].setText(descriptors[i].getName());

			radios[i].addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Object source = e.getSource();
					for (int j = 0; j < arrayLength; j++) {
						if (source == radios[j]) {
							selectedDescriptor = descriptors[j];
							descriptionText.setText(selectedDescriptor.getDescription());
							return;
						}
					}
					selectedDescriptor = null;
					descriptionText.setText(""); //$NON-NLS-1$
				}
			});
		}

		descriptionText = new Text(composite, SWT.BORDER | SWT.WRAP);
		GridData descriptionTextGridData = new GridData(GridData.FILL_HORIZONTAL);
		descriptionTextGridData.heightHint = convertHeightInCharsToPixels(3);
		descriptionText.setLayoutData(descriptionTextGridData);
		descriptionText.setBackground(superComposite.getBackground());

		skipDialogOnSingleSelection = new Button(composite, SWT.CHECK);
		skipDialogOnSingleSelection.setText(CommonNavigatorMessages.NavigatorDropSelectionDialog_3);
		skipDialogOnSingleSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		checkedDefault = NavigatorPlugin.getDefault().getDialogSettings().getBoolean(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION);
		skipDialogOnSingleSelection.setSelection(checkedDefault);

		setDefaultSelection();
		return composite;
	}

	protected void okPressed() {
		if (checkedDefault != skipDialogOnSingleSelection.getSelection()) {
			NavigatorPlugin.getDefault().getDialogSettings().put(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION, skipDialogOnSingleSelection.getSelection());
		}
		super.okPressed();
	}

	private void setDefaultSelection() {
		radios[0].setSelection(true);
		selectedDescriptor = descriptors[0];
		descriptionText.setText(selectedDescriptor.getDescription());
	}

	/**
	 * @return Returns the selectedDescriptor.
	 */
	public DropHandlerDescriptor getSelectedDescriptor() {
		return selectedDescriptor;
	}
}
