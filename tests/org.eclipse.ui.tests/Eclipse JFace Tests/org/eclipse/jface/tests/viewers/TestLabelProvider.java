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
package org.eclipse.jface.tests.viewers;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;

public class TestLabelProvider extends LabelProvider {
	
	static Image fgImage= null;
	
/**
 *
 */
public static Image getImage() {
	if (fgImage == null)
		fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class, "images/java.gif").createImage();
	return fgImage;
}
	public Image getImage(Object element) {
		return getImage();
	}
	public String getText(Object element) {
		String label= element.toString();
		return label+ " <rendered>";
	}
}
