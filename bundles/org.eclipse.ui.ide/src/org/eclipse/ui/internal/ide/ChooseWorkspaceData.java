/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * This class stores the information behind the "Launch Workspace" dialog. The
 * class is able to read and write itself to a well known configuration file.
 */
public class ChooseWorkspaceData {
	/**
	 * The default max length of the recent workspace mru list.  The values
	 * stored in xml (both the max-length parameter and actual size of the
	 * list) will supersede this value. 
	 */
	private static final int RECENT_MAX_LENGTH = 5;

	/**
	 * The directory within the config area that will be used for the
	 * receiver's persisted data.
	 */
	private static final String PERS_FOLDER = "org.eclipse.ui.ide"; //$NON-NLS-1$
	
	/**
	 * The name of the file within the config area that will be used for
	 * the recever's persisted data.
	 * @see PERS_FOLDER
	 */
	private static final String PERS_FILENAME = "recentWorkspaces.xml"; //$NON-NLS-1$
	private static final int PERS_ENCODING_VERSION = 1;

	private String initialDefault;
	private String selection;
	private String[] recentWorkspaces;

	// xml tags
	private static interface XML {
		public static final String PROTOCOL = "protocol"; //$NON-NLS-1$
		public static final String VERSION = "version"; //$NON-NLS-1$
		public static final String WORKSPACE = "workspace"; //$NON-NLS-1$
		public static final String RECENT_WORKSPACES = "recentWorkspaces"; //$NON-NLS-1$
		public static final String MAX_LENGTH = "maxLength"; //$NON-NLS-1$
		public static final String PATH = "path"; //$NON-NLS-1$
	}	

	/**
	 * Creates a new instance, loading persistent data if its found.
	 */
	public ChooseWorkspaceData(String initialDefault) {
		readPersistedData();
		this.initialDefault = initialDefault;
	}

	/**
	 * Return the folder to be used as a default if no other information
	 * exists. Does not return null.
	 */
	public String getInitialDefault() {
		if (initialDefault == null)
			initialDefault = System.getProperty("user.dir") //$NON-NLS-1$
					+ File.separator + "workspace"; //$NON-NLS-1$
		return initialDefault;
	}

	/**
	 * Return the currently selected workspace or null if nothing is selected.
	 */
	public String getSelection() {
		return selection;
	}

	/**
	 * Return an array of recent workspaces sorted with the most recently used at
	 * the start.
	 */
	public String[] getRecentWorkspaces() {
		return recentWorkspaces;
	}

	/**
	 * The argument workspace has been selected, update the receiver.  Does not
	 * persist the new values.
	 */
	public void workspaceSelected(String dir) {
		// this just stores the selection, it is not inserted and persisted
		// until the workspace is actually selected
		selection = dir;
	}

	/**
	 * Update the persistent store.  Call this function after the currently selected
	 * value has been found to be ok.
	 */
	public void writePersistedData() {
		Location configLoc = Platform.getConfigurationLocation();
		if (configLoc == null || configLoc.isReadOnly())
			return;

		URL persUrl = getPersistenceUrl(configLoc.getURL(), true);
		if (persUrl == null)
			return;

		// move the new selection to the front of the list
		if(selection != null) {
			String oldEntry = recentWorkspaces[0];
			recentWorkspaces[0] = selection;
			for (int i = 1; i < recentWorkspaces.length && oldEntry != null; ++i) {
				if (selection.equals(oldEntry))
					return;
				String tmp = recentWorkspaces[i];
				recentWorkspaces[i] = oldEntry;
				oldEntry = tmp;
			}
		}

		Writer writer = null;
		try {
			writer = new FileWriter(persUrl.getFile());

			// E.g.,
			//	<launchWorkspaceData>
			//		<protocol version="1"/>
			// 		<recentWorkspaces maxLength="5">
			//			<workspace path="C:\eclipse\workspace0"/>
			//			<workspace path="C:\eclipse\workspace1"/>
			//		</recentWorkspaces>
			//	</launchWorkspaceData>

			XMLMemento memento = XMLMemento.createWriteRoot("launchWorkspaceData"); //$NON-NLS-1$

			memento.createChild(XML.PROTOCOL)
				   .putInteger(XML.VERSION, PERS_ENCODING_VERSION);

			IMemento recentMemento = memento.createChild(XML.RECENT_WORKSPACES);
			recentMemento.putInteger(XML.MAX_LENGTH, recentWorkspaces.length);
			for(int i = 0; i < recentWorkspaces.length; ++i) {
				if(recentWorkspaces[i] == null)
					break;
				recentMemento.createChild(XML.WORKSPACE).putString(XML.PATH, recentWorkspaces[i]);
			}
			memento.save(writer);
		} catch (IOException e) {
			IDEWorkbenchPlugin.log("Unable to write recent workspace data", new Status( //$NON-NLS-1$
					IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
					IStatus.ERROR, e.getMessage(), e));
		}
		finally {
			if(writer != null)
				try {
					writer.close();
				} catch (IOException e1) {
					// do nothing
				}
		}
	}

	/**
	 * Look for and read data that might have been persisted from some previous
	 * run. Leave the receiver in a default state if no persistent data is
	 * found.
	 */
	private void readPersistedData() {
		URL persUrl = null;

		Location configLoc = Platform.getConfigurationLocation();
		if(configLoc != null)
			persUrl = getPersistenceUrl(configLoc.getURL(), false);

		try {
			// inside try to get the safe default creation in the finally
			// clause
			if (persUrl == null)
				return;

			// E.g.,
			//	<launchWorkspaceData>
			//		<protocol version="1"/>
			// 		<recentWorkspaces maxLength="5">
			//			<workspace path="C:\eclipse\workspace0"/>
			//			<workspace path="C:\eclipse\workspace1"/>
			//		</recentWorkspaces>
			//	</launchWorkspaceData>

			Reader reader = new FileReader(persUrl.getFile());
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			if (memento == null || !compatibleProtocol(memento))
				return;

			IMemento recent = memento.getChild(XML.RECENT_WORKSPACES);
			if(recent == null)
				return;

			Integer maxLength = recent.getInteger(XML.MAX_LENGTH);
			int max = RECENT_MAX_LENGTH;
			if(maxLength != null)
				max = maxLength.intValue();

			IMemento indices[] = recent.getChildren(XML.WORKSPACE);
			if(indices == null || indices.length <= 0)
				return;

			// if a user has edited maxLength to be shorter than the listed
			// indices, accept the list (its tougher for them to retype a long
			// list of paths than to update a max number)
			max = Math.max(max, indices.length);

			recentWorkspaces = new String[max];
			for(int i = 0; i < indices.length; ++i) {
				String path = indices[i].getString(XML.PATH);
				if(path == null)
					break;
				recentWorkspaces[i] = path; 
			}
			return;
		} catch (IOException e) {
			// do nothing -- cannot log because instance area has not been set
		} catch (WorkbenchException e) {
			// do nothing -- cannot log because instance area has not been set
		}
		finally {
			// create safe default if needed
			if(recentWorkspaces == null)
				recentWorkspaces = new String[RECENT_MAX_LENGTH];
		}
	}

	/**
	 * Return true if the protocol used to encode the argument memento is compatible
	 * with the receiver's implementation and false otherwise.
	 */
	private static boolean compatibleProtocol(IMemento memento) {
		IMemento protocolMemento = memento.getChild(XML.PROTOCOL);
		if(protocolMemento == null)
			return false;

		Integer version = protocolMemento.getInteger(XML.VERSION);
		return version != null && version.intValue() == PERS_ENCODING_VERSION;
	}

	/**
	 * The workspace data is stored in the well known file pointed to by the result
	 * of this method.
	 * @param create If the directory and file does not exist this parameter
	 *               controls whether it will be created.
	 * @return An url to the file and null if it does not exist or could not
	 *         be created.
	 */
	private static URL getPersistenceUrl(URL baseUrl, boolean create) {
		if(baseUrl == null)
			return null;

		try {
			// make sure the directory exists
			URL url = new URL(baseUrl, PERS_FOLDER);
			File dir = new File(url.getFile());
			if (!dir.exists() && (!create || !dir.mkdir()))
				return null;

			// make sure the file exists
			url = new URL(dir.toURL(), PERS_FILENAME);
			File persFile = new File(url.getFile());
			if (!persFile.exists() && (!create || !persFile.createNewFile()))
				return null;

			return persFile.toURL();
		} catch (IOException e) {
			// cannot log because instance area has not been set
			return null;
		}
	}
}
