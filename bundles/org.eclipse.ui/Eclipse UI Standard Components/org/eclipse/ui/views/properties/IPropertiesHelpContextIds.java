package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the properties view.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
/*package*/ interface IPropertiesHelpContextIds {
	public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	public static final String CATEGORIES_ACTION = PREFIX + "properties_categories_action_context"; //$NON-NLS-1$
	public static final String DEFAULTS_ACTION = PREFIX + "properties_defaults_action_context"; //$NON-NLS-1$
	public static final String FILTER_ACTION = PREFIX + "properties_filter_action_context"; //$NON-NLS-1$
	public static final String COPY_PROPERTY_ACTION = PREFIX + "properties_copy_action_context"; //$NON-NLS-1$

	// Views
	public static final String PROPERTY_SHEET_VIEW = PREFIX + "property_sheet_view_context"; //$NON-NLS-1$
}
