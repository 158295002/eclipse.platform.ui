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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IPreferenceCustomization;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPreferenceConstants;


/**
 * Implementation of the UI plugin's preference extension's customization element.
 * This is needed in order to force the UI plugin's preferences to be initialized
 * properly when running without org.eclipse.core.runtime.compatibility.
 * For more details, see bug 58975 - New preference mechanism does not properly initialize defaults.
 * 
 * @since 3.0
 */
public class UIPreferenceInitializer implements IPreferenceCustomization {

    public void initializeDefaultPreferences() {
        IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
		store.setDefault(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		//Deprecated but kept for backwards compatibility
		store.setDefault(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		
		// Although there is no longer any item on the preference pages 
		// for setting the linking preference, since it is now a per-part setting, 
		// it remains as a preference to allow product overrides of the 
		// initial state of linking in the Navigator.
		// By default, linking is off.
		store.setDefault(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR, false);

		store.setDefault(IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID, "org.eclipse.ui.presentations.default"); //$NON-NLS-1$
		
		store.addPropertyChangeListener(new PlatformUIPreferenceListener());
    }

}
