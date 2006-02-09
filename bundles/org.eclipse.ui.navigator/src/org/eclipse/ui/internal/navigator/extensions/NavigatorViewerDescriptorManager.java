/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorViewerDescriptorManager {

	private static final NavigatorViewerDescriptorManager INSTANCE = new NavigatorViewerDescriptorManager();

	private final Map viewerDescriptors = new HashMap();

	/**
	 * @return The intialized singleton instance of the viewer descriptor
	 *         registry.
	 */
	public static NavigatorViewerDescriptorManager getInstance() {
		return INSTANCE;
	}

	protected NavigatorViewerDescriptorManager() {
		new NavigatorViewerDescriptorRegistry().readRegistry();
	}

	/**
	 * 
	 * @param aViewerId
	 *            The viewer id for the viewer configuration
	 * @return The viewer descriptor for the given viewer id.
	 */
	public NavigatorViewerDescriptor getNavigatorViewerDescriptor(
			String aViewerId) {

		NavigatorViewerDescriptor viewerDescriptor = (NavigatorViewerDescriptor) viewerDescriptors
				.get(aViewerId);
		if (viewerDescriptor != null)
			return viewerDescriptor;

		synchronized (viewerDescriptors) {
			viewerDescriptor = (NavigatorViewerDescriptor) viewerDescriptors
					.get(aViewerId);
			if (viewerDescriptor == null) {
				viewerDescriptor = new NavigatorViewerDescriptor(aViewerId);
				viewerDescriptors.put(viewerDescriptor.getViewerId(),
						viewerDescriptor);
			}
		}
		return viewerDescriptor;

	}

	private class NavigatorViewerDescriptorRegistry extends RegistryReader
			implements IViewerExtPtConstants {

		protected NavigatorViewerDescriptorRegistry() {
			super(NavigatorPlugin.PLUGIN_ID, TAG_VIEWER);
		}

		protected boolean readElement(IConfigurationElement element) {
			if (TAG_VIEWER.equals(element.getName())) {
				String viewerId = element.getAttribute(ATT_VIEWER_ID);
				NavigatorViewerDescriptor descriptor = getNavigatorViewerDescriptor(viewerId);

				String attPopupMenuId = element.getAttribute(ATT_POPUP_MENU_ID);
				IConfigurationElement[] tagPopupMenu = element
						.getChildren(TAG_POPUP_MENU);
				if (tagPopupMenu.length == 0 && attPopupMenuId != null)
					descriptor.setPopupMenuId(attPopupMenuId);
				else {
					if (attPopupMenuId != null)
						NavigatorPlugin
								.logError(
										0,
										"A popupMenuId attribute and popupMenu element may NOT be concurrently specified. (see " + element.getNamespace() + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
					else if (tagPopupMenu.length > 1)
						NavigatorPlugin
								.logError(
										0,
										"Only one \"popupMenu\" child of \"viewer\" may be specified. (see " + element.getNamespace() + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
					else { // valid case

						String popupMenuId = tagPopupMenu[0]
								.getAttribute(ATT_ID);
						String allowsPlatformContributions = tagPopupMenu[0]
								.getAttribute(ATT_ALLOWS_PLATFORM_CONTRIBUTIONS);

						if (popupMenuId != null)
							descriptor.setPopupMenuId(popupMenuId);

						if (allowsPlatformContributions != null)
							descriptor.setAllowsPlatformContributions(Boolean
									.valueOf(allowsPlatformContributions)
									.booleanValue());

						IConfigurationElement[] insertionPointElements = tagPopupMenu[0]
								.getChildren(TAG_INSERTION_POINT);
						MenuInsertionPoint[] insertionPoints = new MenuInsertionPoint[insertionPointElements.length];
						String name;
						String stringAttSeparator;

						boolean isSeparator;
						for (int indx = 0; indx < insertionPointElements.length; indx++) {
							name = insertionPointElements[indx]
									.getAttribute(ATT_NAME);
							stringAttSeparator = insertionPointElements[indx]
									.getAttribute(ATT_SEPARATOR);
							isSeparator = stringAttSeparator != null ? Boolean
									.valueOf(stringAttSeparator).booleanValue()
									: false;
							insertionPoints[indx] = new MenuInsertionPoint(name,
									isSeparator);
						}
						descriptor.setCustomInsertionPoints(insertionPoints);
					}
				}

				IConfigurationElement[] options = element
						.getChildren(TAG_OPTIONS);
				if (options.length == 1) {
					IConfigurationElement[] properties = options[0]
							.getChildren(TAG_PROPERTY);
					String name;
					String value;
					for (int i = 0; i < properties.length; i++) {
						name = properties[i].getAttribute(ATT_NAME);
						if (name != null) {
							value = properties[i].getAttribute(ATT_VALUE);
							descriptor.setProperty(name, value);
						}
					}
				} else if (options.length > 1) {
					NavigatorPlugin
							.logError(
									0,
									"Only one \"options\" child of \"viewer\" may be specified. (see " + element.getNamespace() + ")", null); //$NON-NLS-1$ //$NON-NLS-2$

				}
				return true;
			}
			if (TAG_VIEWER_CONTENT_BINDING.equals(element.getName())) {
				String viewerId = element.getAttribute(ATT_VIEWER_ID);
				NavigatorViewerDescriptor descriptor = getNavigatorViewerDescriptor(viewerId);
				descriptor.consumeContentBinding(element);
				return true;
			}
			if (TAG_VIEWER_ACTION_BINDING.equals(element.getName())) {
				String viewerId = element.getAttribute(ATT_VIEWER_ID);
				NavigatorViewerDescriptor descriptor = getNavigatorViewerDescriptor(viewerId);
				descriptor.consumeActionBinding(element);
				return true;
			} if (TAG_DRAG_ASSISTANT.equals(element.getName())) {
				String viewerId = element.getAttribute(ATT_VIEWER_ID);
				NavigatorViewerDescriptor descriptor = getNavigatorViewerDescriptor(viewerId);
				descriptor.addDragAssistant(new CommonDragAssistantDescriptor(element));
				return true;
			}
			return false;
		}
	}

}
