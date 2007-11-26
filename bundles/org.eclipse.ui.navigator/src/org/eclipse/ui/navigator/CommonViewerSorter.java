/***************************************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.ui.navigator;

import java.util.Set;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.navigator.NavigatorContentService;

/**
 * 
 * Provides an implementation of TreeViewerSorter that uses the given parent to determine the
 * correct sort order based on the defined
 * <b>org.eclipse.ui.navigator.navigatorContent/navigatorContent/commonSorter</b> elements
 * available in the set of <i>visible</i> content extensions.
 * 
 * <p>
 * The CommonViewerSorter must be assigned a {@link INavigatorContentService} to drive its sorting
 * algorithm. Without a vaild content service, the sorter will return the default ordering.
 * </p>
 * <p>
 * A CommonViewerSorter may not be attached to more than one CommonViewer.
 * </p>
 * 
 * <p>
 * Clients may not extend this class.
 * </p>
 * 
 * 
 * 
 * @since 3.2
 * 
 */
public final class CommonViewerSorter extends TreePathViewerSorter {
	
	private static final int LEFT_UNDERSTANDS = 1;
	private static final int RIGHT_UNDERSTANDS = 2; 
	private static final int BOTH_UNDERSTAND = LEFT_UNDERSTANDS | RIGHT_UNDERSTANDS; 

	private NavigatorContentService contentService;

	private INavigatorSorterService sorterService;

	/**
	 * Create a sorter service attached to the given content service.
	 * 
	 * @param aContentService
	 *            The content service used by the viewer that will use this sorter service.
	 * @since 3.3
	 */
	public void setContentService(INavigatorContentService aContentService) {
		contentService = (NavigatorContentService) aContentService;
		sorterService = contentService.getSorterService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
	 */
	public int category(Object element) {
		if (contentService == null)
			return 0;

		INavigatorContentDescriptor source = contentService.getSourceOfContribution(element);
		if (source == null)
			source = getSource(element);
		return source != null ? source.getPriority() : Priority.NORMAL_PRIORITY_VALUE;
	}

	public int compare(Viewer viewer, TreePath parentPath, Object e1, Object e2) {
		if (contentService == null)
			return -1;
		INavigatorContentDescriptor sourceOfLvalue = contentService.getSourceOfContribution(e1);
		INavigatorContentDescriptor sourceOfRvalue = contentService.getSourceOfContribution(e2);
		
		Object parent;
		if (parentPath == null) {
			parent = viewer.getInput();
		} else {
			parent = parentPath.getLastSegment();
		}
		
		// shortcut if contributed by same source
		if(sourceOfLvalue == sourceOfRvalue) {
			ViewerSorter sorter = sorterService.findSorter(sourceOfLvalue, parent, e1, e2);
			if (sorter != null) {
				return sorter.compare(viewer, e1, e2);
			}
		} 

		if (sourceOfLvalue == null)
			sourceOfLvalue = getSource(e1);
		if (sourceOfRvalue == null)
			sourceOfRvalue = getSource(e2);		

		boolean flags[] = new boolean[4];
		flags[0] = sourceOfLvalue.isPossibleChild(e1);
		flags[1] = sourceOfLvalue.isPossibleChild(e2);
		flags[2] = sourceOfRvalue.isPossibleChild(e1);
		flags[3] = sourceOfRvalue.isPossibleChild(e2);
		
		int whoknows  = 0;		 
		whoknows  = whoknows  | (flags[0] & flags[1] ? LEFT_UNDERSTANDS : 0); 
		whoknows  = whoknows  | (flags[2] & flags[3] ? RIGHT_UNDERSTANDS : 0); 
		

		ViewerSorter sorter = null;
		
		switch(whoknows) {
			case BOTH_UNDERSTAND: 
				sorter = sourceOfLvalue.getPriority() > sourceOfRvalue.getPriority() ? 
						sorterService.findSorter(sourceOfLvalue, parent, e1, e2) : 
						sorterService.findSorter(sourceOfRvalue, parent, e1, e2);
				break;
			case LEFT_UNDERSTANDS: 
				sorter =  sorterService.findSorter(sourceOfLvalue,
					parent, e1, e2) ;
					break;
			case RIGHT_UNDERSTANDS: 
				sorter =  sorterService.findSorter(sourceOfRvalue,
					parent, e1, e2) ;
					break;
		}
		 
		if (sorter != null) {
			return sorter.compare(viewer, e1, e2);
		}
 
		int categoryDelta = category(e1) - category(e2);
		if (categoryDelta == 0) {
			return super.compare(viewer, e1, e2);
		}
		return categoryDelta;
	}

	private INavigatorContentDescriptor getSource(Object o) {
		Set descriptors = contentService.findDescriptorsWithPossibleChild(o);
		if (descriptors != null && descriptors.size() > 0) {
			return (INavigatorContentDescriptor) descriptors.iterator().next();
		}
		return null;
	}

}
