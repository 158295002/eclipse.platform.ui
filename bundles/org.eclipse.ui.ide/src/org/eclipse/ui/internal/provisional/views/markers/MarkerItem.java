/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.resources.IMarker;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;


/**
 * The MarkerItem class is the class that represents the objects displayed
 * in the ExtendedMarkersView.
 *
 */
public abstract class MarkerItem {

	private CollationKey collationKey;

	/**
	 * Get the children of the node.
	 * @return MarkerNode[]
	 */
	public abstract MarkerItem[] getChildren();

	/**
	 * Return the parent node or <code>null</code> if this is a top
	 * level element.
	 * @return MarkerNode
	 */
	public abstract MarkerItem getParent() ;

	/**
	 * Return whether or not this is a concrete node
	 * @return boolean
	 */
	public abstract boolean isConcrete();

	/**
	 * Return the description of the receiver.
	 * @return String
	 */
	public abstract String getDescription() ;

	/**
	 * Get a concrete marker from the receiver. If the receiver
	 * is concrete return the receiver otherwise return one of the
	 * concrete markers it contains.
	 * @return MarkerEntry
	 */
	public abstract MarkerEntry getConcreteRepresentative();
	
	/**
	 * Get the value of the attribute in the enclosed marker.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return int
	 */
	public int getAttributeValue(String attribute, int defaultValue) {
		//There are no integer values by default
		return defaultValue;
	
	}

	/**
	 * Get the String value of the attribute in the enclosed marker.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return String
	 */
	public String getAttributeValue(String attribute, String defaultValue) {
		//All items have messages
		if (attribute == IMarker.MESSAGE)
			return getDescription();
		return defaultValue;
	
	}

	/**
	 * Get the CollationKey for the string attribute.
	 * 
	 * @param attribute
	 * @param defaultValue
	 *            the defaultValue if the value is not set
	 * @return
	 */
	public CollationKey getCollationKey(String attribute, String defaultValue) {
		if (collationKey == null)
			collationKey = Collator.getInstance().getCollationKey(getDescription());
		return collationKey;
	}

	/**
	 * @return
	 */
	public IMarker getMarker() {
		// TODO Auto-generated method stub
		return null;
	}



}