package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.*;
import org.eclipse.jface.action.*;
import java.util.*;

/**
 * This class reads the registry for extensions that plug into
 * 'editorActions' extension point.
 */
public class EditorActionBuilder extends PluginActionBuilder {
	private static final String TAG_CONTRIBUTION_TYPE = "editorContribution";
	public class ExternalContributor implements IEditorActionBarContributor {
		private List cache;
		public ExternalContributor(List cache) {
			this.cache = cache;
		}
		public void init(IActionBars bars) {
			contributeToMenu(bars.getMenuManager());
			contributeToToolBar(bars.getToolBarManager());
			contributeToStatusLine(bars.getStatusLineManager());
		}
		public void contributeToMenu(IMenuManager menu) {
			for (int i = 0; i < cache.size(); i++) {
				Object obj = cache.get(i);
				if (obj instanceof IConfigurationElement) {
					IConfigurationElement menuElement = (IConfigurationElement) obj;
					PluginActionBuilder.processMenu(menuElement, menu, false);
				} else
					if (obj instanceof ActionDescriptor) {
						ActionDescriptor ad = (ActionDescriptor) obj;
						contributeMenuAction(ad, menu, false);
					}
			}
		}
		public void contributeToToolBar(IToolBarManager manager) {
			for (int i = 0; i < cache.size(); i++) {
				Object obj = cache.get(i);
				if (obj instanceof ActionDescriptor) {
					ActionDescriptor ad = (ActionDescriptor) obj;
					contributeToolbarAction(ad, manager, false);
				}
			}
		}
		public void setActiveEditor(IEditorPart editor) {
			for (int i=0; i<cache.size(); i++) {
				Object obj = cache.get(i);
				if (obj instanceof ActionDescriptor) {
					ActionDescriptor ad = (ActionDescriptor) obj;
					EditorPluginAction action = (EditorPluginAction)ad.getAction();
					action.editorChanged(editor);
				}
			}
		}
		public void contributeToStatusLine(IStatusLineManager manager) {
		}
	}
/**
 * The constructor.
 */
public EditorActionBuilder() {
}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  It should be implemented by subclasses.
 */
protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
	return new ActionDescriptor(element, ActionDescriptor.T_EDITOR);
}
/**
 * Reads editor contributor if specified directly in the 'editor' extension point,
 * and all external contributions for this editor's ID registered in
 * 'editorActions' extension point. 
 */
public IEditorActionBarContributor readActionExtensions(IEditorDescriptor desc, IActionBars bars)
{
	ExternalContributor ext = null;
	readContributions(desc.getId(), TAG_CONTRIBUTION_TYPE, 
		IWorkbenchConstants.PL_EDITOR_ACTIONS);
	if (cache != null) {
		ext = new ExternalContributor(cache);
		cache = null;
	}
	return ext;
}
}
