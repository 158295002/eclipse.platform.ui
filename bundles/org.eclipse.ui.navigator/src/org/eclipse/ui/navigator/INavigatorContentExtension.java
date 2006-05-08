/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * The content extension represents the components of a <b>navigatorContent</b>
 * extension. These handles are managed by a content service
 * {@link INavigatorContentService}.
 * 
 * <p>
 * There is one {@link INavigatorContentExtension} for each content service.
 * There is only one {@link INavigatorContentDescriptor} for each extension.
 * </p>
 *  
 * 
 *<p>
 * This interface is not intended to be implemented by clients.
 *</p>
 * @since 3.2
 * 
 */
public interface INavigatorContentExtension extends IAdaptable {

	/**
	 * 
	 * @return The id attribute of the navigatorContent extension.
	 */
	String getId();

	/**
	 * There is one descriptor for all instances of a
	 * INavigatorContentExtension.
	 * 
	 * 
	 * @return A handle to the descriptor used to manage this extension.
	 */
	INavigatorContentDescriptor getDescriptor();

	/**
	 * Clients may choose to implement {@link ICommonContentProvider}, but are
	 * only required to supply an implementation of {@link ITreeContentProvider}.
	 * 
	 * @return The content provider defined by the <b>navigatorContent</b>
	 *         extension.
	 * @see ICommonContentProvider
	 * @see ITreeContentProvider
	 */
	ITreeContentProvider getContentProvider();

	/**
	 * The real underlying implementation may only support the
	 * {@link ILabelProvider} interface, but a simple delegate is used when this
	 * is the case to ensure that clients may anticpate an
	 * {@link ICommonLabelProvider} interface.
	 * 
	 * @return The content provider defined by the <b>navigatorContent</b>
	 *         extension.
	 * @see ICommonLabelProvider
	 * @see ILabelProvider
	 */
	ICommonLabelProvider getLabelProvider(); 

	/**
	 * 
	 * @return True if any class has been instantiated by this extension.
	 */
	boolean isLoaded();

	/**
	 * 
	 * @return The state model associated with this content extension.
	 * @see IExtensionStateModel
	 */
	IExtensionStateModel getStateModel();

}