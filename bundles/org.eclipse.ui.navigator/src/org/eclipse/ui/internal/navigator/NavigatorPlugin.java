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
package org.eclipse.ui.internal.navigator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class for the Navigator.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static NavigatorPlugin plugin; 
	public static String PLUGIN_ID = "org.eclipse.ui.navigator"; //$NON-NLS-1$

	/**
	 * Creates a new instance of the receiver
	 */
	public NavigatorPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static NavigatorPlugin getDefault() {
		return plugin;
	}
 
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
  
	/**
	 * Logs messages.
	 */
	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
		System.err.println(message);
		//1FTTJKV: ITPCORE:ALL - log(status) does not allow plugin information to be recorded
	}

	/**
	 * Logs errors.
	 */
	public static void log(String message, IStatus status) {
		if (message != null) {
			getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
			System.err.println(message + "\nReason:"); //$NON-NLS-1$
		}
		getDefault().getLog().log(status);
		System.err.println(status.getMessage());
	} 
	

	public static void logError(int aCode, String aMessage, Throwable anException) { 
		getDefault().getLog().log(createErrorStatus(aCode, aMessage, anException)); 
	}

	public static void log(int severity, int aCode, String aMessage, Throwable exception) {
		log(createStatus(severity, aCode, aMessage, exception));
	}

	public static void log(IStatus aStatus) { 
		getDefault().getLog().log(aStatus); 
	} 
	 
	public static IStatus createStatus(int severity, int aCode, String aMessage, Throwable exception) {
		return new Status(severity, PLUGIN_ID, aCode, aMessage != null ? aMessage : "No message.", exception); //$NON-NLS-1$
	}
 
	public static IStatus createErrorStatus(int aCode, String aMessage, Throwable exception) {
		return createStatus(IStatus.ERROR, aCode, aMessage, exception);
	} 
}
