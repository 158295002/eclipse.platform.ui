/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IModelProviderDescriptor;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ModelStatus;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorManager;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistryReader;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Collection of IDE-specific APIs factored out of existing workbench. This
 * class cannot be instantiated; all functionality is provided by static methods
 * and fields.
 * 
 * @since 3.0
 */
public final class IDE {
	/**
	 * The persistent property key used on IFile resources to contain the
	 * preferred editor ID to use.
	 * <p>
	 * Example of retrieving the persisted editor id:
	 * 
	 * <pre><code>
	 *  IFile file = ...
	 *  IEditorDescriptor editorDesc = null;
	 *  try {
	 *  	String editorID = file.getPersistentProperty(EDITOR_KEY);
	 *  	if (editorID != null) {
	 *  		editorDesc = editorReg.findEditor(editorID);
	 *  	}
	 *  } catch (CoreException e) {
	 *  	// handle problem accessing persistent property here
	 *  }
	 * </code></pre>
	 * 
	 * </p>
	 * <p>
	 * Example of persisting the editor id:
	 * 
	 * <pre><code>
	 *  IFile file = ...
	 *  try {
	 *  	file.setPersistentProperty(EDITOR_KEY, editorDesc.getId());
	 *  } catch (CoreException e) {
	 *  	// handle problem setting persistent property here
	 *  }
	 * </code></pre>
	 * 
	 * </p>
	 */
	public static final QualifiedName EDITOR_KEY = new QualifiedName(
			"org.eclipse.ui.internal.registry.ResourceEditorRegistry", "EditorProperty");//$NON-NLS-2$//$NON-NLS-1$

	/**
	 * An optional attribute within a workspace marker (<code>IMarker</code>)
	 * which identifies the preferred editor type to be opened.
	 */
	public static final String EDITOR_ID_ATTR = "org.eclipse.ui.editorID"; //$NON-NLS-1$

	/**
	 * The resource based perspective identifier.
	 */
	public static final String RESOURCE_PERSPECTIVE_ID = "org.eclipse.ui.resourcePerspective"; //$NON-NLS-1$

	/**
	 * Marker help registry mapping markers to help context ids and resolutions;
	 * lazily initialized on fist access.
	 */
	private static MarkerHelpRegistry markerHelpRegistry = null;

	/**
	 * Standard shared images defined by the IDE. These are over and above the
	 * standard workbench images declared in {@link org.eclipse.ui.ISharedImages
	 * ISharedImages}.
	 * <p>
	 * This interface is not intended to be implemented by clients.
	 * </p>
	 * 
	 * @see org.eclipse.ui.ISharedImages
	 */
	public interface SharedImages {
		/**
		 * Identifies a project image.
		 */
		public final static String IMG_OBJ_PROJECT = "IMG_OBJ_PROJECT"; //$NON-NLS-1$

		/**
		 * Identifies a closed project image.
		 */
		public final static String IMG_OBJ_PROJECT_CLOSED = "IMG_OBJ_PROJECT_CLOSED"; //$NON-NLS-1$

		/**
		 * Identifies the image used for "open marker".
		 */
		public final static String IMG_OPEN_MARKER = "IMG_OPEN_MARKER"; //$NON-NLS-1$

		/**
		 * Identifies the default image used to indicate a task.
		 */
		public final static String IMG_OBJS_TASK_TSK = "IMG_OBJS_TASK_TSK"; //$NON-NLS-1$

		/**
		 * Identifies the default image used to indicate a bookmark.
		 */
		public final static String IMG_OBJS_BKMRK_TSK = "IMG_OBJS_BKMRK_TSK"; //$NON-NLS-1$
	}

	/**
	 * Preferences defined by the IDE workbench.
	 * <p>
	 * This interface is not intended to be implemented by clients.
	 * </p>
	 */
	public interface Preferences {

		/**
		 * A named preference for how a new perspective should be opened when a
		 * new project is created.
		 * <p>
		 * Value is of type <code>String</code>. The possible values are
		 * defined by the constants
		 * <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE, 
		 * OPEN_PERSPECTIVE_REPLACE, and NO_NEW_PERSPECTIVE</code>.
		 * </p>
		 * 
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_WINDOW
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_PAGE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_REPLACE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#NO_NEW_PERSPECTIVE
		 */
		public static final String PROJECT_OPEN_NEW_PERSPECTIVE = "PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

		/**
		 * <p>
		 * Specifies whether or not the workspace selection dialog should be
		 * shown on startup.
		 * </p>
		 * <p>
		 * The default value for this preference is <code>true</code>.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String SHOW_WORKSPACE_SELECTION_DIALOG = "SHOW_WORKSPACE_SELECTION_DIALOG"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores the maximum number of workspaces that should be displayed in
		 * the ChooseWorkspaceDialog.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String MAX_RECENT_WORKSPACES = "MAX_RECENT_WORKSPACES"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores a comma separated list of the recently used workspace paths.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String RECENT_WORKSPACES = "RECENT_WORKSPACES"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores the version of the protocol used to decode/encode the list of
		 * recent workspaces.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String RECENT_WORKSPACES_PROTOCOL = "RECENT_WORKSPACES_PROTOCOL"; //$NON-NLS-1$

	}

	/**
	 * Block instantiation.
	 */
	private IDE() {
		// do nothing
	}

	/**
	 * Returns the marker help registry for the workbench.
	 * 
	 * @return the marker help registry
	 */
	public static IMarkerHelpRegistry getMarkerHelpRegistry() {
		if (markerHelpRegistry == null) {
			markerHelpRegistry = new MarkerHelpRegistry();
			new MarkerHelpRegistryReader().addHelp(markerHelpRegistry);
		}
		return markerHelpRegistry;
	}

	/**
	 * Sets the cursor and selection state for the given editor to reveal the
	 * position of the given marker. This is done on a best effort basis. If the
	 * editor does not provide an <code>IGotoMarker</code> interface (either
	 * directly or via <code>IAdaptable.getAdapter</code>), this has no
	 * effect.
	 * 
	 * @param editor
	 *            the editor
	 * @param marker
	 *            the marker
	 */
	public static void gotoMarker(IEditorPart editor, IMarker marker) {
		IGotoMarker gotoMarker = null;
		if (editor instanceof IGotoMarker) {
			gotoMarker = (IGotoMarker) editor;
		} else {
			gotoMarker = (IGotoMarker) editor.getAdapter(IGotoMarker.class);
		}
		if (gotoMarker != null) {
			gotoMarker.gotoMarker(marker);
		}
	}

	/**
	 * Opens an editor on the given object.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened.
	 * <p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param input
	 *            the editor input
	 * @param editorId
	 *            the id of the editor extension to use
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page,
			IEditorInput input, String editorId) throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(input, editorId);
	}

	/**
	 * Opens an editor on the given IFileStore object.
	 * <p>
	 * Unlike the other <code>openEditor</code> methods, this one can be used
	 * to open files that reside outside the workspace resource set.
	 * </p>
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened.
	 * </p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param uri
	 *            the URI of the file store representing the file to open
	 * @param editorId
	 *            the id of the editor extension to use
	 * @param activate
	 *            if <code>true</code> the editor will be activated opened
	 * @return an open editor or <code>null</code> if an external editor was
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * 
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 * @see EFS#getStore(URI)
	 * 
	 * @since 3.3
	 */
	public static IEditorPart openEditor(IWorkbenchPage page,
			URI uri, String editorId, boolean activate)
			throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}
		
		IFileStore fileStore;
		try {
			fileStore = EFS.getStore(uri);
		} catch (CoreException e) {
			throw new PartInitException(
					IDEWorkbenchMessages.IDE_coreExceptionFileStore,e);
		}

		IEditorInput input = getEditorInput(fileStore);

		// open the editor on the file
		return page.openEditor(input, editorId, activate);
	}

	/**
	 * Create the Editor Input appropriate for the given <code>IFileStore</code>.
	 * The result is a normal file editor input if the file exists in the
	 * workspace and, if not, we create a wrapper capable of managing an
	 * 'external' file using its <code>IFileStore</code>.
	 * 
	 * @param fileStore
	 *            The file store to provide the editor input for
	 * @return The editor input associated with the given file store
	 */
	private static IEditorInput getEditorInput(IFileStore fileStore) {
		IFile workspaceFile = getWorkspaceFile(fileStore);
		if (workspaceFile != null)
			return new FileEditorInput(workspaceFile);
		return new FileStoreEditorInput(fileStore);
	}

	/**
	 * Determine whether or not the <code>IFileStore</code> represents a file
	 * currently in the workspace.
	 * 
	 * @param fileStore
	 *            The <code>IFileStore</code> to test
	 * @return The workspace's <code>IFile</code> if it exists or
	 *         <code>null</code> if not
	 */
	private static IFile getWorkspaceFile(IFileStore fileStore) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile[] files = workspace.getRoot().findFilesForLocation(
				new Path(fileStore.toURI().getPath()));
		files = filterNonExistentFiles(files);
		if (files == null || files.length == 0)
			return null;

		// for now only return the first file
		return files[0];
	}

	/**
	 * Filter the incoming array of <code>IFile</code> elements by removing
	 * any that do not currently exist in the workspace.
	 * 
	 * @param files
	 *            The array of <code>IFile</code> elements
	 * @return The filtered array
	 */
	private static IFile[] filterNonExistentFiles(IFile[] files) {
		if (files == null)
			return null;

		int length = files.length;
		ArrayList existentFiles = new ArrayList(length);
		for (int i = 0; i < length; i++) {
			if (files[i].exists())
				existentFiles.add(files[i]);
		}
		return (IFile[]) existentFiles.toArray(new IFile[existentFiles.size()]);
	}

	/**
	 * Opens an editor on the given object.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * <p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param input
	 *            the editor input
	 * @param editorId
	 *            the id of the editor extension to use
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String,
	 *      boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page,
			IEditorInput input, String editorId, boolean activate)
			throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(input, editorId, activate);
	}

	/**
	 * Opens an editor on the given file resource. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * <p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param input
	 *            the editor input
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput,
	 *      String, boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			boolean activate) throws PartInitException {
		return openEditor(page, input, activate, true);
	}

	/**
	 * Opens an editor on the given file resource. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings if <code>determineContentType</code> is
	 * <code>true</code>.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * <p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param input
	 *            the editor input
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @param determineContentType
	 *            attempt to resolve the content type for this file
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput,
	 *      String, boolean)
	 * @since 3.1
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			boolean activate, boolean determineContentType)
			throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		IEditorDescriptor editorDesc = getEditorDescriptor(input,
				determineContentType);
		return page.openEditor(new FileEditorInput(input), editorDesc.getId(),
				activate);
	}

	/**
	 * Opens an editor on the given file resource. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened.
	 * <p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param input
	 *            the editor input
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input)
			throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		IEditorDescriptor editorDesc = getEditorDescriptor(input);
		return page.openEditor(new FileEditorInput(input), editorDesc.getId());
	}

	/**
	 * Opens an editor on the given file resource.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened.
	 * <p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param input
	 *            the editor input
	 * @param editorId
	 *            the id of the editor extension to use
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			String editorId) throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(new FileEditorInput(input), editorId);
	}

	/**
	 * Opens an editor on the given file resource.
	 * <p>
	 * If the page already has an editor open on the target object then that
	 * editor is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated.
	 * <p>
	 * 
	 * @param page
	 *            the page in which the editor will be opened
	 * @param input
	 *            the editor input
	 * @param editorId
	 *            the id of the editor extension to use
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String,
	 *      boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IFile input,
			String editorId, boolean activate) throws PartInitException {
		// sanity checks
		if (page == null) {
			throw new IllegalArgumentException();
		}

		// open the editor on the file
		return page.openEditor(new FileEditorInput(input), editorId, activate);
	}

	/**
	 * Returns an editor descriptor appropriate for opening the given file
	 * resource.
	 * <p>
	 * The editor descriptor is determined using a multistep process. This
	 * method will attempt to resolve the editor based on content-type bindings
	 * as well as traditional name/extension bindings.
	 * </p>
	 * <ol>
	 * <li>The file is consulted for a persistent property named
	 * <code>IDE.EDITOR_KEY</code> containing the preferred editor id to be
	 * used.</li>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of
	 * the editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The operating system is consulted to determine if an in-place
	 * component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor
	 * is available.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param file
	 *            the file
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 */
	public static IEditorDescriptor getEditorDescriptor(IFile file)
			throws PartInitException {
		return getEditorDescriptor(file, true);
	}

	/**
	 * Returns an editor descriptor appropriate for opening the given file
	 * resource.
	 * <p>
	 * The editor descriptor is determined using a multistep process. This
	 * method will attempt to resolve the editor based on content-type bindings
	 * as well as traditional name/extension bindings if
	 * <code>determineContentType</code>is <code>true</code>.
	 * </p>
	 * <ol>
	 * <li>The file is consulted for a persistent property named
	 * <code>IDE.EDITOR_KEY</code> containing the preferred editor id to be
	 * used.</li>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of
	 * the editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The operating system is consulted to determine if an in-place
	 * component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor
	 * is available.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param file
	 *            the file
	 * @param determineContentType
	 *            query the content type system for the content type of the file
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 * @since 3.1
	 */
	public static IEditorDescriptor getEditorDescriptor(IFile file,
			boolean determineContentType) throws PartInitException {

		if (file == null) {
			throw new IllegalArgumentException();
		}

		return getEditorDescriptor(file.getName(), PlatformUI.getWorkbench()
				.getEditorRegistry(), getDefaultEditor(file,
				determineContentType));
	}

	/**
	 * Returns an editor descriptor appropriate for opening a file resource with
	 * the given name.
	 * <p>
	 * The editor descriptor is determined using a multistep process. This
	 * method will attempt to infer content type from the file name.
	 * </p>
	 * <ol>
	 * <li>The file is consulted for a persistent property named
	 * <code>IDE.EDITOR_KEY</code> containing the preferred editor id to be
	 * used.</li>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of
	 * the editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The operating system is consulted to determine if an in-place
	 * component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor
	 * is available.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param name
	 *            the file name
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 * @since 3.1
	 */
	public static IEditorDescriptor getEditorDescriptor(String name)
			throws PartInitException {
		return getEditorDescriptor(name, true);
	}

	/**
	 * Returns an editor descriptor appropriate for opening a file resource with
	 * the given name.
	 * <p>
	 * The editor descriptor is determined using a multistep process. This
	 * method will attempt to infer the content type of the file if
	 * <code>inferContentType</code> is <code>true</code>.
	 * </p>
	 * <ol>
	 * <li>The file is consulted for a persistent property named
	 * <code>IDE.EDITOR_KEY</code> containing the preferred editor id to be
	 * used.</li>
	 * <li>The workbench editor registry is consulted to determine if an editor
	 * extension has been registered for the file type. If so, an instance of
	 * the editor extension is opened on the file. See
	 * <code>IEditorRegistry.getDefaultEditor(String)</code>.</li>
	 * <li>The operating system is consulted to determine if an in-place
	 * component editor is available (e.g. OLE editor on Win32 platforms).</li>
	 * <li>The operating system is consulted to determine if an external editor
	 * is available.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param name
	 *            the file name
	 * @param inferContentType
	 *            attempt to infer the content type from the file name if this
	 *            is <code>true</code>
	 * @return an editor descriptor, appropriate for opening the file
	 * @throws PartInitException
	 *             if no editor can be found
	 * @since 3.1
	 */
	public static IEditorDescriptor getEditorDescriptor(String name,
			boolean inferContentType) throws PartInitException {

		if (name == null) {
			throw new IllegalArgumentException();
		}

		IContentType contentType = inferContentType ? Platform
				.getContentTypeManager().findContentTypeFor(name) : null;
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();

		return getEditorDescriptor(name, editorReg, editorReg.getDefaultEditor(
				name, contentType));
	}

	/**
	 * Get the editor descriptor for a given name using the editorDescriptor
	 * passed in as a default as a starting point.
	 * 
	 * @param name
	 *            The name of the element to open.
	 * @param editorReg
	 *            The editor registry to do the lookups from.
	 * @param defaultDescriptor
	 *            IEditorDescriptor or <code>null</code>
	 * @return IEditorDescriptor
	 * @throws PartInitException
	 *             if no valid editor can be found
	 * 
	 * @since 3.1
	 */
	private static IEditorDescriptor getEditorDescriptor(String name,
			IEditorRegistry editorReg, IEditorDescriptor defaultDescriptor)
			throws PartInitException {

		if (defaultDescriptor != null) {
			return defaultDescriptor;
		}

		IEditorDescriptor editorDesc = defaultDescriptor;

		// next check the OS for in-place editor (OLE on Win32)
		if (editorReg.isSystemInPlaceEditorAvailable(name)) {
			editorDesc = editorReg
					.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
		}

		// next check with the OS for an external editor
		if (editorDesc == null
				&& editorReg.isSystemExternalEditorAvailable(name)) {
			editorDesc = editorReg
					.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}

		// next lookup the default text editor
		if (editorDesc == null) {
			editorDesc = editorReg
					.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);
		}

		// if no valid editor found, bail out
		if (editorDesc == null) {
			throw new PartInitException(
					IDEWorkbenchMessages.IDE_noFileEditorFound);
		}

		return editorDesc;
	}

	/**
	 * Opens an editor on the file resource of the given marker.
	 * <p>
	 * If this page already has an editor open on the marker resource file that
	 * editor is brought to front; otherwise, a new editor is opened.The cursor
	 * and selection state of the editor are then updated from information
	 * recorded in the marker.
	 * </p>
	 * <p>
	 * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute the
	 * attribute value will be used to determine the editor type to be opened.
	 * If not, the registered editor for the marker resource file will be used.
	 * </p>
	 * 
	 * @param page
	 *            the workbench page to open the editor in
	 * @param marker
	 *            the marker to open
	 * @return an open editor or <code>null</code> not possible
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 * @see #openEditor(org.eclipse.ui.IWorkbenchPage,
	 *      org.eclipse.core.resources.IMarker, boolean)
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IMarker marker)
			throws PartInitException {
		return openEditor(page, marker, true);
	}

	/**
	 * Opens an editor on the file resource of the given marker.
	 * <p>
	 * If this page already has an editor open on the marker resource file that
	 * editor is brought to front; otherwise, a new editor is opened. If
	 * <code>activate == true</code> the editor will be activated. The cursor
	 * and selection state of the editor are then updated from information
	 * recorded in the marker.
	 * </p>
	 * <p>
	 * If the marker contains an <code>EDITOR_ID_ATTR</code> attribute the
	 * attribute value will be used to determine the editor type to be opened.
	 * If not, the registered editor for the marker resource file will be used.
	 * </p>
	 * 
	 * @param page
	 *            the workbench page to open the editor in
	 * @param marker
	 *            the marker to open
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @return an open editor or <code>null</code> not possible
	 * @exception PartInitException
	 *                if the editor could not be initialized
	 */
	public static IEditorPart openEditor(IWorkbenchPage page, IMarker marker,
			boolean activate) throws PartInitException {
		// sanity checks
		if (page == null || marker == null) {
			throw new IllegalArgumentException();
		}

		// get the marker resource file
		if (!(marker.getResource() instanceof IFile)) {
			IDEWorkbenchPlugin
					.log("Open editor on marker failed; marker resource not an IFile"); //$NON-NLS-1$
			return null;
		}
		IFile file = (IFile) marker.getResource();

		// get the preferred editor id from the marker
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();
		IEditorDescriptor editorDesc = null;
		try {
			String editorID = (String) marker.getAttribute(EDITOR_ID_ATTR);
			if (editorID != null) {
				editorDesc = editorReg.findEditor(editorID);
			}
		} catch (CoreException e) {
			// ignore this
		}

		// open the editor on the marker resource file
		IEditorPart editor = null;
		if (editorDesc == null) {
			editor = openEditor(page, file, activate);
		} else {
			editor = page.openEditor(new FileEditorInput(file), editorDesc
					.getId(), activate);
		}

		// get the editor to update its position based on the marker
		if (editor != null) {
			gotoMarker(editor, marker);
		}

		return editor;
	}

	/**
	 * Save all dirty editors in the workbench whose editor input is a child
	 * resource of one of the <code>IResource</code>'s provided. Opens a
	 * dialog to prompt the user if <code>confirm</code> is true. Return true
	 * if successful. Return false if the user has cancelled the command.
	 * 
	 * @since 3.0
	 * 
	 * @param resourceRoots
	 *            the resource roots under which editor input should be saved,
	 *            other will be left dirty
	 * @param confirm
	 *            prompt the user if true
	 * @return boolean false if the operation was cancelled.
	 */
	public static boolean saveAllEditors(IResource[] resourceRoots,
			boolean confirm) {
		final IResource[] finalResources = resourceRoots;
		final boolean finalConfirm = confirm;
		final boolean[] result = new boolean[1];
		result[0] = true;

		if (resourceRoots.length == 0) {
			return result[0];
		}

		Platform.run(new SafeRunnable(IDEWorkbenchMessages.ErrorClosing) {
			public void run() {
				// Collect dirtyEditors
				ArrayList dirtyEditors = new ArrayList();

				IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
						.getWorkbenchWindows();
				for (int i = 0; i < windows.length; i++) {
					IWorkbenchWindow window = windows[i];
					IWorkbenchPage[] pages = window.getPages();
					for (int j = 0; j < pages.length; j++) {
						IWorkbenchPage page = pages[j];
						IEditorPart[] dirty = page.getDirtyEditors();
						for (int k = 0; k < dirty.length; k++) {
							IEditorPart part = dirty[k];
							IFile file = ResourceUtil.getFile(part
									.getEditorInput());
							if (file != null) {
								for (int l = 0; l < finalResources.length; l++) {
									IResource resource = finalResources[l];
									if (resource.getFullPath().isPrefixOf(
											file.getFullPath())) {
										dirtyEditors.add(part);
										break;
									}
								}
							}
						}
					}

				}
				if (dirtyEditors.size() > 0) {
					IWorkbenchWindow w = Workbench.getInstance()
							.getActiveWorkbenchWindow();
					if (w == null) {
						w = windows[0];
					}
					result[0] = EditorManager.saveAll(dirtyEditors,
							finalConfirm, false, false, w);
				}
			}
		});
		return result[0];
	}

	/**
	 * Sets the default editor id for a given file. This value will be used to
	 * determine the default editor descriptor for the file in future calls to
	 * <code>getDefaultEditor(IFile)</code>.
	 * 
	 * @param file
	 *            the file
	 * @param editorID
	 *            the editor id
	 */
	public static void setDefaultEditor(IFile file, String editorID) {
		try {
			file.setPersistentProperty(EDITOR_KEY, editorID);
		} catch (CoreException e) {
			// do nothing
		}
	}

	/**
	 * Returns the default editor for a given file. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings.
	 * <p>
	 * A default editor id may be registered for a specific file using
	 * <code>setDefaultEditor</code>. If the given file has a registered
	 * default editor id the default editor will derived from it. If not, the
	 * default editor is determined by taking the file name for the file and
	 * obtaining the default editor for that name.
	 * </p>
	 * 
	 * @param file
	 *            the file
	 * @return the descriptor of the default editor, or <code>null</code> if
	 *         not found
	 */
	public static IEditorDescriptor getDefaultEditor(IFile file) {
		return getDefaultEditor(file, true);
	}

	/**
	 * Returns the default editor for a given file. This method will attempt to
	 * resolve the editor based on content-type bindings as well as traditional
	 * name/extension bindings if <code>determineContentType</code> is
	 * <code>true</code>.
	 * <p>
	 * A default editor id may be registered for a specific file using
	 * <code>setDefaultEditor</code>. If the given file has a registered
	 * default editor id the default editor will derived from it. If not, the
	 * default editor is determined by taking the file name for the file and
	 * obtaining the default editor for that name.
	 * </p>
	 * 
	 * @param file
	 *            the file
	 * @param determineContentType
	 *            determine the content type for the given file
	 * @return the descriptor of the default editor, or <code>null</code> if
	 *         not found
	 * @since 3.1
	 */
	public static IEditorDescriptor getDefaultEditor(IFile file,
			boolean determineContentType) {
		// Try file specific editor.
		IEditorRegistry editorReg = PlatformUI.getWorkbench()
				.getEditorRegistry();
		try {
			String editorID = file.getPersistentProperty(EDITOR_KEY);
			if (editorID != null) {
				IEditorDescriptor desc = editorReg.findEditor(editorID);
				if (desc != null) {
					return desc;
				}
			}
		} catch (CoreException e) {
			// do nothing
		}

		IContentType contentType = null;
		if (determineContentType) {
			contentType = getContentType(file);
		}
		// Try lookup with filename
		return editorReg.getDefaultEditor(file.getName(), contentType);
	}

	/**
	 * Extracts and returns the <code>IResource</code>s in the given
	 * selection or the resource objects they adapts to.
	 * 
	 * @param originalSelection
	 *            the original selection, possibly empty
	 * @return list of resources (element type: <code>IResource</code>),
	 *         possibly empty
	 */
	public static List computeSelectedResources(
			IStructuredSelection originalSelection) {
		List resources = null;
		for (Iterator e = originalSelection.iterator(); e.hasNext();) {
			Object next = e.next();
			Object resource = null;
			if (next instanceof IResource) {
				resource = next;
			} else if (next instanceof IAdaptable) {
				resource = ((IAdaptable) next).getAdapter(IResource.class);
			}
			if (resource != null) {
				if (resources == null) {
					// lazy init to avoid creating empty lists
					// assume selection contains mostly resources most times
					resources = new ArrayList(originalSelection.size());
				}
				resources.add(resource);
			}
		}
		if (resources == null) {
			return Collections.EMPTY_LIST;
		}
		return resources;

	}

	/**
	 * Return the content type for the given file.
	 * 
	 * @param file
	 *            the file to test
	 * @return the content type, or <code>null</code> if it cannot be
	 *         determined.
	 * @since 3.1
	 */
	public static IContentType getContentType(IFile file) {
		try {
			UIStats.start(UIStats.CONTENT_TYPE_LOOKUP, file.getName());
			IContentDescription contentDescription = file
					.getContentDescription();
			if (contentDescription == null) {
				return null;
			}
			return contentDescription.getContentType();
		} catch (CoreException e) {
			return null;
		} finally {
			UIStats.end(UIStats.CONTENT_TYPE_LOOKUP, file, file.getName());
		}
	}

	/**
	 * Guess at the content type of the given file based on the filename.
	 * 
	 * @param file
	 *            the file to test
	 * @return the content type, or <code>null</code> if it cannot be
	 *         determined.
	 * @since 3.2
	 */
	public static IContentType guessContentType(IFile file) {
		String fileName = file.getName();
		try {
			UIStats.start(UIStats.CONTENT_TYPE_LOOKUP, fileName);
			IContentTypeMatcher matcher = file.getProject()
					.getContentTypeMatcher();
			return matcher.findContentTypeFor(fileName);
		} catch (CoreException e) {
			return null;
		} finally {
			UIStats.end(UIStats.CONTENT_TYPE_LOOKUP, file, fileName);
		}
	}

	/**
	 * Prompt the user to inform them of the possible side effects of an
	 * operation on resources. Do not prompt for side effects from ignored model
	 * providers. A model provider can be ignored if it is the client calling
	 * this API. Any message from the provided model provider id or any model
	 * providers it extends will be ignored.
	 * 
	 * @param shell
	 *            the shell to parent the prompt dialog
	 * @param title
	 *            the title of the dialog
	 * @param message
	 *            the message for the dialog
	 * @param delta
	 *            a delta built using an
	 *            {@link IResourceChangeDescriptionFactory}
	 * @param ignoreModelProviderIds
	 *            model providers to be ignored
	 * @param syncExec
	 *            prompt in a sync exec (required when called from a non-UI
	 *            thread)
	 * @return whether the user chose to continue
	 * @since 3.2
	 */
	public static boolean promptToConfirm(final Shell shell,
			final String title, String message, IResourceDelta delta,
			String[] ignoreModelProviderIds, boolean syncExec) {
		IStatus status = ResourceChangeValidator.getValidator().validateChange(
				delta, null);
		if (status.isOK()) {
			return true;
		}
		final IStatus displayStatus;
		if (status.isMultiStatus()) {
			List result = new ArrayList();
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				IStatus child = children[i];
				if (!isIgnoredStatus(child, ignoreModelProviderIds)) {
					result.add(child);
				}
			}
			if (result.isEmpty()) {
				return true;
			}
			if (result.size() == 1) {
				displayStatus = (IStatus) result.get(0);
			} else {
				displayStatus = new MultiStatus(status.getPlugin(), status
						.getCode(), (IStatus[]) result
						.toArray(new IStatus[result.size()]), status
						.getMessage(), status.getException());
			}
		} else {
			if (isIgnoredStatus(status, ignoreModelProviderIds)) {
				return true;
			}
			displayStatus = status;
		}

		if (message == null) {
			message = IDEWorkbenchMessages.IDE_sideEffectWarning;
		}
		final String dialogMessage = NLS.bind(
				IDEWorkbenchMessages.IDE_areYouSure, message);

		final boolean[] result = new boolean[] { false };
		Runnable runnable = new Runnable() {
			public void run() {
				ErrorDialog dialog = new ErrorDialog(shell, title,
						dialogMessage, displayStatus, IStatus.ERROR
								| IStatus.WARNING | IStatus.INFO) {
					protected void createButtonsForButtonBar(Composite parent) {
						createButton(parent, IDialogConstants.YES_ID,
								IDialogConstants.YES_LABEL, false);
						createButton(parent, IDialogConstants.NO_ID,
								IDialogConstants.NO_LABEL, true);
						createDetailsButton(parent);
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.dialogs.ErrorDialog#buttonPressed(int)
					 */
					protected void buttonPressed(int id) {
						if (id == IDialogConstants.YES_ID) {
							super.buttonPressed(IDialogConstants.OK_ID);
						} else if (id == IDialogConstants.NO_ID) {
							super.buttonPressed(IDialogConstants.CANCEL_ID);
						}
						super.buttonPressed(id);
					}
				};
				int code = dialog.open();
				result[0] = code == 0;
			}
		};
		if (syncExec) {
			shell.getDisplay().syncExec(runnable);
		} else {
			runnable.run();
		}
		return result[0];
	}

	private static boolean isIgnoredStatus(IStatus status,
			String[] ignoreModelProviderIds) {
		if (ignoreModelProviderIds == null) {
			return false;
		}
		if (status instanceof ModelStatus) {
			ModelStatus ms = (ModelStatus) status;
			for (int i = 0; i < ignoreModelProviderIds.length; i++) {
				String id = ignoreModelProviderIds[i];
				if (ms.getModelProviderId().equals(id)) {
					return true;
				}
				IModelProviderDescriptor desc = ModelProvider
						.getModelProviderDescriptor(id);
				String[] extended = desc.getExtendedModels();
				if (isIgnoredStatus(status, extended)) {
					return true;
				}
			}
		}
		return false;
	}
}
