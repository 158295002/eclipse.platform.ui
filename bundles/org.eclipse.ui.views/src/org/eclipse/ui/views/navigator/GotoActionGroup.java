package org.eclipse.ui.views.navigator;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved. � This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
�
Contributors:
**********************************************************************/

import org.eclipse.core.resources.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.views.framelist.*;

/**
 * This is the action group for the goto actions.
 */
public class GotoActionGroup extends ResourceNavigatorActionGroup {

	private BackAction backAction;
	private ForwardAction forwardAction;
	private GoIntoAction goIntoAction;
	private UpAction upAction;
	private GotoResourceAction goToResourceAction;
	private IResourceChangeListener resourceChangeListener;
	
	public GotoActionGroup(IResourceNavigator navigator) {
		super(navigator);
	
		// Listen for project open/close changes. Fixes bug 5958
		resourceChangeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				updateActionBars();
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
	}

	/**
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		super.dispose();
	}

	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		if (selection.size() == 1) {
			if (ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FOLDER)) {
				menu.add(goIntoAction);
			} else {
				IStructuredSelection resourceSelection = ResourceSelectionUtil.allResources(selection, IResource.PROJECT);
				if (resourceSelection != null && !resourceSelection.isEmpty()) {
					IProject project = (IProject)resourceSelection.getFirstElement();
					if (project.isOpen())
						menu.add(goIntoAction);
				}
			}
		}
	}
	
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.GO_INTO,
			goIntoAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.BACK,
			backAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.FORWARD,
			forwardAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.UP,
			upAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.GO_TO_RESOURCE,
			goToResourceAction);
			
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
	}

	protected void makeActions() {
		FrameList frameList = navigator.getFrameList();
		goIntoAction = new GoIntoAction(frameList);
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);
		goToResourceAction = new GotoResourceAction(navigator, ResourceNavigatorMessages.getString("GoToResource.label"));
	}	

	public void updateActionBars() {
		ActionContext context = getContext();
		boolean enable = false;

		// Fix for bug 26126. Resource change listener could call
		// updateActionBars without a context being set.
		// This should never happen because resource navigator sets
		// context immediately after this group is created.
		if (context != null) {
			IStructuredSelection selection =
				(IStructuredSelection) context.getSelection();
	
			if (selection.size() == 1) {
				Object object = selection.getFirstElement();
				if (object instanceof IProject) {
					enable = ((IProject) object).isOpen();
				}
				else
				if (object instanceof IFolder) {
					enable = true;
				}
			}
		}	
		goIntoAction.setEnabled(enable);
		// the rest of the actions update by listening to frame list changes
	}
}