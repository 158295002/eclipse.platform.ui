/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * The "main program" for the Eclipse IDE.
 * 
 * @since 3.0
 */
public final class IDEApplication implements IPlatformRunnable, IExecutableExtension {

	private static final String METADATA_FOLDER = ".metadata"; //$NON-NLS-1$
	private static final String VERSION_FILENAME = "version.ini"; //$NON-NLS-1$
	private static final String WORKSPACE_VERSION_KEY = "org.eclipse.core.runtime"; //$NON-NLS-1$
	private static final String WORKSPACE_VERSION_VALUE = "1"; //$NON-NLS-1$

	/**
	 * Creates a new IDE application.
	 */
	public IDEApplication() {
		// There is nothing to do for IDEApplication
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		// create and startup the display for the workbench
		Display display = PlatformUI.createDisplay();

		try {
			Shell shell = new Shell(display);
			try {
				if (!checkInstanceLocation(shell)) {
					Platform.endSplash();
					return EXIT_OK;
				}
			} finally {
				if (shell != null)
					shell.dispose();
			}

			// create the workbench with this advisor and run it until it exits
			// N.B. createWorkbench remembers the advisor, and also registers
			// the workbench globally so that all UI plug-ins can find it using
			// PlatformUI.getWorkbench() or AbstractUIPlugin.getWorkbench()
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new IDEWorkbenchAdvisor());

			// exit the application with an appropriate return code
			return returnCode == PlatformUI.RETURN_RESTART
					? EXIT_RESTART
					: EXIT_OK;
		} finally {
			if (display != null)
				display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		// There is nothing to do for IDEApplication
	}

	/**
	 * Return true if a valid workspace path has been set and false otherwise.
	 * Prompt for and set the path if possible and required.
	 * 
	 * @return true if a valid instance location has been set and false
	 *         otherwise
	 */
	private boolean checkInstanceLocation(Shell shell) {
		// -data @none was specified but an ide requires workspace
		Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null) {
			MessageDialog
					.openError(
							shell,
							IDEWorkbenchMessages
									.getString("IDEApplication.workspaceMandatoryTitle"), //$NON-NLS-1$
							IDEWorkbenchMessages
									.getString("IDEApplication.workspaceMandatoryMessage")); //$NON-NLS-1$
			return false;
		}

		// -data "/valid/path", workspace already set
		if (instanceLoc.isSet())
			return true;

		// -data @noDefault or -data not specified, prompt and set
		URL defaultUrl = instanceLoc.getDefault();
		String initialDefault = defaultUrl == null ? null : defaultUrl
				.getFile();
		ChooseWorkspaceData launchData = new ChooseWorkspaceData(
				initialDefault);

		while (true) {
			URL workspaceUrl = promptForWorkspace(shell, launchData);
			if (workspaceUrl == null)
				return false;

			try {
				// the operation will fail if the url is not a valid
				// instance data area, so other checking is unneeded
				if (instanceLoc.setURL(workspaceUrl, true)) {
					launchData.writePersistedData();
					writeWorkspaceVersion();
					return true;
				}
			} catch (IllegalStateException e) {
				MessageDialog
						.openError(
								shell,
								IDEWorkbenchMessages
										.getString("IDEApplication.workspaceCannotBeSetTitle"), //$NON-NLS-1$
								IDEWorkbenchMessages
										.getString("IDEApplication.workspaceCannotBeSetMessage")); //$NON-NLS-1$
				return false;
			}

			// by this point it has been determined that the workspace is already
			// in use -- force the user to choose again
			MessageDialog
					.openError(
							shell,
							IDEWorkbenchMessages
									.getString("IDEApplication.workspaceInUseTitle"), //$NON-NLS-1$
							IDEWorkbenchMessages
									.getString("IDEApplication.workspaceInUseMessage")); //$NON-NLS-1$
		}
	}

	/**
	 * Open a workspace selection dialog on the argument shell, populating the
	 * argument data with the user's selection. Perform first level validation
	 * on the selection by comparing the version information. This method does
	 * not examine the runtime state (e.g., is the workspace already locked?).
	 * 
	 * @param shell
	 * @param launchData
	 * @return An URL storing the selected workspace or null if the user has
	 *         canceled the launch operation.
	 */
	private URL promptForWorkspace(Shell shell, ChooseWorkspaceData launchData) {
		URL url = null;
		do {
			new ChooseWorkspaceDialog(shell, launchData).open();
			String instancePath = launchData.getSelection();
			if (instancePath == null)
				return null;

			// create the workspace if it does not already exist
			File workspace = new File(instancePath);
			if(!workspace.exists())
				workspace.mkdir();

			try {
			    // Don't use File.toURL() since it adds a leading slash that Platform does not
			    // handle properly.  See bug 54081 for more details.  
			    String path = workspace.getAbsolutePath().replace(File.separatorChar, '/');
				url = new URL("file", null, path); //$NON-NLS-1$
			} catch (MalformedURLException e) {
				MessageDialog
					.openError(
						shell,
						IDEWorkbenchMessages
							.getString("IDEApplication.workspaceInvalidTitle"), //$NON-NLS-1$
						IDEWorkbenchMessages
							.getString("IDEApplication.workspaceInvalidMessage")); //$NON-NLS-1$
				continue;
			}
		} while (!isValidWorkspace(shell, url));

		return url;
	}

	/**
	 * Return true if the argument directory is ok to use as a workspace and
	 * false otherwise. A version check will be performed, and a confirmation
	 * box may be displayed on the argument shell if an older version is
	 * detected.
	 */
	private boolean isValidWorkspace(Shell shell, URL url) {
		String version = readWorkspaceVersion(url);

		// if the version could not be read, then there is not any existing
		// workspace data to trample, e.g., perhaps its a new directory that
		// is just starting to be used as a workspace
		if (version == null)
			return true;

		final int ide_version = Integer.parseInt(WORKSPACE_VERSION_VALUE);
		int workspace_version = Integer.parseInt(version);

		// equality test is required since any version difference (newer
		// or older) may result in data being trampled
		if (workspace_version == ide_version)
			return true;

		// At this point workspace has been detected to be from a version
		// other than the current ide version -- find out if the user wants
		// to use it anyhow.
		String title = IDEWorkbenchMessages
				.getString("IDEApplication.versionTitle"); //$NON-NLS-1$
		String message = IDEWorkbenchMessages.format(
				"IDEApplication.versionMessage", //$NON-NLS-1$
				new Object[]{url.getFile()});

		MessageBox mbox = new MessageBox(shell, SWT.OK | SWT.CANCEL
				| SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
		mbox.setText(title);
		mbox.setMessage(message);
		return mbox.open() == SWT.OK;
	}

	/**
	 * Look at the argument URL for the workspace's version information. Return
	 * that version if found and null otherwise.
	 */
	private static String readWorkspaceVersion(URL workspace) {
		File versionFile = getVersionFile(workspace, false);
		if (versionFile == null || !versionFile.exists())
			return null;

		try {
			// Although the version file is not spec'ed to be a Java properties
			// file, it happens to follow the same format currently, so using
			// Properties to read it is convenient.
			Properties props = new Properties();
			FileInputStream is = new FileInputStream(versionFile);
			try {
				props.load(is);
			} finally {
				is.close();
			}

			return props.getProperty(WORKSPACE_VERSION_KEY);
		} catch (IOException e) {
			IDEWorkbenchPlugin.log("Could not read version file", new Status( //$NON-NLS-1$
					IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
					IStatus.ERROR, e.getMessage(), e));
			return null;
		}
	}

	/**
	 * Write the version of the metadata into a known file overwriting any
	 * existing file contents. Writing the version file isn't really crucial,
	 * so the function is silent about failure
	 */
	private static void writeWorkspaceVersion() {
		Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null || instanceLoc.isReadOnly())
			return;

		File versionFile = getVersionFile(instanceLoc.getURL(), true);
		if (versionFile == null)
			return;

		OutputStream output = null;
		try {
			String versionLine = WORKSPACE_VERSION_KEY + '=' + WORKSPACE_VERSION_VALUE;

			output = new FileOutputStream(versionFile);
			output.write(versionLine.getBytes("UTF-8")); //$NON-NLS-1$
		} catch (IOException e) {
			IDEWorkbenchPlugin.log("Could not write version file", new Status( //$NON-NLS-1$
					IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
					IStatus.ERROR, e.getMessage(), e));
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * The version file is stored in the metadata area of the workspace. This
	 * method returns an URL to the file or null if the directory or file does
	 * not exist (and the create parameter is false).
	 * 
	 * @param create
	 *            If the directory and file does not exist this parameter
	 *            controls whether it will be created.
	 * @return An url to the file or null if the version file does not exist or
	 *         could not be created.
	 */
	private static File getVersionFile(URL workspaceUrl, boolean create) {
		if (workspaceUrl == null)
			return null;

		try {
			// make sure the directory exists
			URL metaUrl = new URL(workspaceUrl, METADATA_FOLDER);
			File metaDir = new File(metaUrl.getFile());
			if (!metaDir.exists() && (!create || !metaDir.mkdir()))
				return null;

			// make sure the file exists
			URL versionUrl = new URL(metaDir.toURL(), VERSION_FILENAME);
			File versionFile = new File(versionUrl.getFile());
			if (!versionFile.exists()
					&& (!create || !versionFile.createNewFile()))
				return null;

			return versionFile;
		} catch (IOException e) {
			// cannot log because instance area has not been set
			return null;
		}
	}
}
