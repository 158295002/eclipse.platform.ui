package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * 
 */
public class ActionDefinitionRegistryReader extends RegistryReader {
	private static final String TAG_ACTION_DEF = "actionDefinition";
	private static final String ATT_ID = "id";
	private static final String ATT_LABEL = "label";
	private static final String ATT_ICON = "icon";
	private static final String ATT_MENUBAR_PATH = "menubarPath";
	private static final String ATT_TOOLBAR_PATH = "toolbarPath";
	private static final String ATT_TOOLTIP = "tooltip";
	private static final String ATT_HELP_CONTEXT_ID = "helpContextId";
	private static final String ATT_STATE = "state";
	
	private ActionDefinitionRegistry actionDefinitions;
	
	protected boolean readElement(IConfigurationElement element) {
		if (!element.getName().equals(TAG_ACTION_DEF))
			return false;
		String id = element.getAttribute(ATT_ID);
		String label = element.getAttribute(ATT_LABEL);
		String icon = element.getAttribute(ATT_ICON);
		String menubarPath = element.getAttribute(ATT_MENUBAR_PATH);
		String toolbarPath = element.getAttribute(ATT_TOOLBAR_PATH);
		String tooltip = element.getAttribute(ATT_TOOLTIP);
		String helpContextId = element.getAttribute(ATT_HELP_CONTEXT_ID);
		String state = element.getAttribute(ATT_STATE);
		
		if (id==null) {
			logMissingAttribute(element, ATT_ID);
		}
		if (label==null) {
			logMissingAttribute(element, ATT_LABEL);
		}
		if (icon==null) {
			logMissingAttribute(element, ATT_ICON);
		}
		if (menubarPath==null) {
			logMissingAttribute(element, ATT_MENUBAR_PATH);
		}
		if (toolbarPath==null) {
			logMissingAttribute(element, ATT_TOOLBAR_PATH);
		}
		if (tooltip==null) {
			logMissingAttribute(element, ATT_TOOLTIP);
		}
		if (helpContextId==null) {
			logMissingAttribute(element, ATT_HELP_CONTEXT_ID);
		}
		if (state==null) {
			logMissingAttribute(element, ATT_STATE);
		}
		
		ActionDefinition a = new ActionDefinition(id, label, icon, menubarPath,
			toolbarPath, tooltip, helpContextId, state);
		actionDefinitions.add(a);
		return true;
	}
	
	public void readActionDefinitions(IPluginRegistry registry, ActionDefinitionRegistry out) {
		actionDefinitions = out;
		readRegistry(registry, IWorkbenchConstants.PLUGIN_ID, IWorkbenchConstants.PL_ACTION_DEFINITIONS);
	}
}
