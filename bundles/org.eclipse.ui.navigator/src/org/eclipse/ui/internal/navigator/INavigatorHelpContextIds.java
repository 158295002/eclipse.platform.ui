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
package org.eclipse.ui.internal.navigator;

/**
 * The help context ids for the Common Navigator.
 * 
 * @since 3.2
 * 
 */
public interface INavigatorHelpContextIds {

	/** */
	public static final String PREFIX = NavigatorPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	/** */
	public static final String TEXT_CUT_ACTION = PREFIX
			+ "text_cut_action_context"; //$NON-NLS-1$

	/** */
	public static final String TEXT_COPY_ACTION = PREFIX
			+ "text_copy_action_context"; //$NON-NLS-1$

	/** */
	public static final String TEXT_PASTE_ACTION = PREFIX
			+ "text_paste_action_context"; //$NON-NLS-1$

	/** */
	public static final String TEXT_DELETE_ACTION = PREFIX
			+ "text_delete_action_context"; //$NON-NLS-1$

	/** */
	public static final String TEXT_SELECT_ALL_ACTION = PREFIX
			+ "text_select_all_action_context"; //$NON-NLS-1$
}
