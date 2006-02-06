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
package org.eclipse.ui.internal.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * This action delegate collapses all expanded elements in a Navigator view.
 * 
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public class CollapseAllAction extends Action implements IAction {

	private final CommonViewer commonViewer;

	public CollapseAllAction(CommonViewer aViewer) {
		super(CommonNavigatorMessages.CollapseAllActionDelegate_0);
		setToolTipText(CommonNavigatorMessages.CollapseAllActionDelegate_0); 
		commonViewer = aViewer;
	}

	public void run() {
		if (commonViewer != null)
			commonViewer.collapseAll();
	}
}
