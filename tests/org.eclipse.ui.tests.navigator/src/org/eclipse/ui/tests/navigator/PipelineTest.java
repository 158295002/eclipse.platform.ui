/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.filters.UpdateActiveFiltersOperation;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;

public class PipelineTest extends TestCase {

	public static final String COMMON_NAVIGATOR_INSTANCE_ID = "org.eclipse.ui.tests.navigator.PipelineTestView"; //$NON-NLS-1$

	public static final String COMMON_NAVIGATOR_RESOURCE_EXT = "org.eclipse.ui.navigator.resourceContent"; //$NON-NLS-1$
	
	public static final String COMMON_NAVIGATOR_JAVA_EXT = "org.eclipse.jdt.java.ui.javaContent"; //$NON-NLS-1$

	private Set expectedChildren = new HashSet();

	private IProject project;

	private CommonViewer viewer;

	private INavigatorContentService contentService;

	protected void setUp() throws Exception {

		TestWorkspace.init();

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject("Test"); //$NON-NLS-1$

		expectedChildren.add(project.getFolder("src")); //$NON-NLS-1$
		expectedChildren.add(project.getFolder("bin")); //$NON-NLS-1$
		expectedChildren.add(project.getFile(".project")); //$NON-NLS-1$
		expectedChildren.add(project.getFile(".classpath")); //$NON-NLS-1$ 
		expectedChildren.add(project.getFile("model.properties")); //$NON-NLS-1$

		EditorTestHelper.showView(COMMON_NAVIGATOR_INSTANCE_ID, true);

		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();

		IViewPart commonNavigator = activePage
				.findView(COMMON_NAVIGATOR_INSTANCE_ID);
		commonNavigator.setFocus();
		viewer = (CommonViewer) commonNavigator.getAdapter(CommonViewer.class);

		contentService = viewer.getNavigatorContentService();

		IUndoableOperation updateFilters = new UpdateActiveFiltersOperation(
				viewer, new String[0], true);
		updateFilters.execute(null, null);

	}

	public void testNavigatorRootContents() throws Exception {

		assertEquals(
				"There should be no visible extensions for the pipeline viewer.", 0,
				contentService.getVisibleExtensionIds().length);

		contentService.bindExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT, COMMON_NAVIGATOR_JAVA_EXT }, false);

		assertEquals(
				"There should be two visible extension for the pipeline viewer.", 2,
				contentService.getVisibleExtensionIds().length);

		contentService.getActivationService().activateExtensions(
				new String[] { COMMON_NAVIGATOR_RESOURCE_EXT, COMMON_NAVIGATOR_JAVA_EXT }, true);

		viewer.refresh();
		

		// we do this to force the rendering of the children of items[0]
		viewer
				.setSelection(new StructuredSelection(project
						.getFile(".project"))); //$NON-NLS-1$

		TreeItem[] rootItems = viewer.getTree().getItems();
		
		assertEquals("There should be one item.", 1, rootItems.length); //$NON-NLS-1$		
		
		assertTrue("The root object should be an IJavaProject, which is IAdaptable.", rootItems[0].getData() instanceof IAdaptable); //$NON-NLS-1$

		IProject adaptedProject = (IProject) ((IAdaptable)rootItems[0].getData()).getAdapter(IProject.class); 
		assertEquals(project, adaptedProject);
		
		IFolder sourceFolder = project.getFolder(new Path("src"));
		viewer.add(project, sourceFolder);
		  
		TreeItem[] projectChildren = rootItems[0].getItems(); 

		assertTrue("There should be some items.", projectChildren.length > 0); //$NON-NLS-1$
		 
		for (int i = 0; i < projectChildren.length; i++) {
			if(projectChildren[i].getData() == sourceFolder)
				fail("The src folder should not be added as an IFolder.");			
		}
		
		// a new project without a Java nature should add without an issue.
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject("New Project");
		viewer.add(viewer.getInput(), newProject);
		
		rootItems = viewer.getTree().getItems();
		
		assertEquals("There should be two items.", 2, rootItems.length); //$NON-NLS-1$
		
		boolean found = false;
		for (int i = 0; i < rootItems.length; i++) {
			if(rootItems[i].getData() instanceof IProject) {
				IProject newProjectFromTree = (IProject) rootItems[i].getData();
				assertEquals(newProject, newProjectFromTree);
				found = true;
			}	
		}
		assertTrue(found);

		
	}
 

}
