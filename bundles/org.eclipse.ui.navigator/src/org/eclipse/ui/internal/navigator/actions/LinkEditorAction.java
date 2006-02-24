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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.extensions.LinkHelperRegistry;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.progress.UIJob;

/**
 * This action links the activate editor with the Navigator selection.
 *  
 * @since 3.2
 */
public class LinkEditorAction extends Action implements ISelectionChangedListener, IAction, IPropertyListener {

	private IPartListener partListener;
	private final CommonViewer commonViewer;
	private final CommonNavigator commonNavigator;
	private LinkHelperRegistry linkHelperRegistry;

	/**
	 * Create a LinkEditorAction for the given navigator and viewer. 
	 * @param aNavigator The navigator which defines whether linking is enabled and implements {@link ISetSelectionTarget}.
	 * @param aViewer The common viewer instance with a {@link INavigatorContentService}.
	 */
	public LinkEditorAction(CommonNavigator aNavigator, CommonViewer aViewer) {
		super(CommonNavigatorMessages.LinkEditorActionDelegate_0);
		setToolTipText(CommonNavigatorMessages.LinkEditorActionDelegate_1); 
		commonNavigator = aNavigator;
		commonViewer = aViewer;
		init();
	}

	protected void activateEditor() {
		ISelection selection = commonViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			activateEditor((IStructuredSelection) selection);
		}
	}

	/**
	 * Update the active editor based on the current selection in the Navigator.
	 */
	protected void activateEditor(final IStructuredSelection aSelection) {
		if (aSelection == null || aSelection.size() != 1) {
			return;
		}

		final Runnable activateEditor = new Runnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				ILinkHelper[] helpers = linkHelperRegistry.getLinkHelpersFor(aSelection);
				if (helpers.length > 0) {
					helpers[0].activateEditor(commonNavigator.getSite().getPage(), aSelection);
				}
			}
		};

		if (Display.getCurrent() != null) {
			activateEditor.run();
		} else {
			/* Create and schedule a UI Job to activate the editor in a valid Display thread */
			new UIJob(CommonNavigatorMessages.Link_With_Editor_Job_) {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					activateEditor.run();
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	protected void init() {
		partListener = new IPartListener() {

			public void partActivated(IWorkbenchPart part) {
				if (part instanceof IEditorPart) {
					linkToEditor((IEditorPart) part);
				}
			}

			public void partBroughtToTop(IWorkbenchPart part) {
				if (part instanceof IEditorPart) {
					linkToEditor((IEditorPart) part);
				}
			}

			public void partClosed(IWorkbenchPart part) {
				 
			}

			public void partDeactivated(IWorkbenchPart part) {
			}

			public void partOpened(IWorkbenchPart part) {
			}
		};

		updateLinkingEnabled(commonNavigator.isLinkingEnabled());

		commonNavigator.addPropertyListener(this);
		 
		
		linkHelperRegistry = new LinkHelperRegistry(commonViewer.getNavigatorContentService());
	}
	
	/**
	 * 
	 */
	public void dispose() {
		commonNavigator.removePropertyListener(this);
		if(isChecked()) {
			commonViewer.removeSelectionChangedListener(this);
			commonNavigator.getSite().getPage().removePartListener(partListener);
		}

	}

	/**
	 * Link the Navigator to the current open editor. Do this by updating the Navigator's selection.
	 */
	private void linkToEditor(IEditorPart anEditor) {
		if (anEditor != null) {

			IEditorInput input = anEditor.getEditorInput();
			ILinkHelper[] helpers = linkHelperRegistry.getLinkHelpersFor(input);

			IStructuredSelection selection = StructuredSelection.EMPTY;
			IStructuredSelection newSelection = StructuredSelection.EMPTY;

			for (int i = 0; i < helpers.length; i++) {
				selection = helpers[i].findSelection(input);
				if (selection != null && !selection.isEmpty()) {
					newSelection = mergeSelection(newSelection, selection);
				}
			}

			commonNavigator.selectReveal(newSelection);
		}
	}

	private IStructuredSelection mergeSelection(IStructuredSelection aBase, IStructuredSelection aSelectionToAppend) {
		if (aBase == null || aBase.isEmpty()) {
			return (aSelectionToAppend != null) ? aSelectionToAppend : StructuredSelection.EMPTY;
		} else if (aSelectionToAppend == null || aSelectionToAppend.isEmpty()) {
			return aBase;
		} else {
			List newItems = new ArrayList(aBase.toList());
			newItems.addAll(aSelectionToAppend.toList());
			return new StructuredSelection(newItems);
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run() {
		commonNavigator.setLinkingEnabled(!commonNavigator.isLinkingEnabled());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if (commonNavigator.isLinkingEnabled()) {
			activateEditor((IStructuredSelection) event.getSelection());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object aSource, int aPropertyId) {
		switch (aPropertyId) {
			case CommonNavigator.IS_LINKING_ENABLED_PROPERTY :
				updateLinkingEnabled(((CommonNavigator) aSource).isLinkingEnabled());
		}
	}

	/**
	 * @param toEnableLinking
	 */
	private void updateLinkingEnabled(boolean toEnableLinking) {
		setChecked(toEnableLinking);

		if (toEnableLinking) {
			IEditorPart editor = commonNavigator.getSite().getPage().getActiveEditor();
			linkToEditor(editor);

			commonViewer.addSelectionChangedListener(this);
			commonNavigator.getSite().getPage().addPartListener(partListener);
		} else {
			commonViewer.removeSelectionChangedListener(this);
			commonNavigator.getSite().getPage().removePartListener(partListener);
		}
	}
	
	

}
