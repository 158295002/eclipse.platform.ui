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

package org.eclipse.ui.utils;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.dialogs.AbstractSearchItem;

/**
 * @since 3.2
 *
 */
public class ResourceSearchItem extends AbstractSearchItem {
	
	private IResource resource ;

	/**
	 * 
	 */
	public ResourceSearchItem(IResource resource) {
		this.resource = resource;
	}
	
	/**
	 * 
	 */
	public ResourceSearchItem(IResource resource, boolean isHistory) {
		this.resource = resource;
		if (isHistory)
			this.markAsHistory();
	}
	
	/**
	 * Get decorated resource
	 * @return decorated resource
	 */
	public IResource getResource(){
		return this.resource;
	}

}
