/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.navigator.ShowInNavigatorAction;

/**
 * Main class for the bookmark navigator for displaying bookmarks on
 * resources and opening an editor on the bookmarked resource when the user
 * commands.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.BookmarkNavigator"</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a bookmark
 * navigator is needed for a workbench window. This class is not intended
 * to be instantiated or subclassed by clients.
 * </p>
 */
public class BookmarkNavigator extends ViewPart {
	private Table table;
	private TableViewer viewer;
	private OpenBookmarkAction openAction;
	private CopyBookmarkAction copyAction;
	private PasteBookmarkAction pasteAction;
	private RemoveBookmarkAction removeAction;
	private SelectAllAction selectAllAction;
	private ShowInNavigatorAction showInNavigatorAction;
	private SortByAction sortByDescriptionAction;
	private SortByAction sortByResourceAction;
	private SortByAction sortByFolderAction;
	private SortByAction sortByLineAction;
	private ChangeSortDirectionAction sortAscendingAction;
	private ChangeSortDirectionAction sortDescendingAction;	 
	private IMemento memento;
	private BookmarkSorter sorter;
	private Clipboard clipboard;
	
	private final String columnHeaders[] = {
		BookmarkMessages.getString("ColumnIcon.header"),//$NON-NLS-1$
		BookmarkMessages.getString("ColumnDescription.header"),//$NON-NLS-1$
		BookmarkMessages.getString("ColumnResource.header"),//$NON-NLS-1$
		BookmarkMessages.getString("ColumnFolder.header"),//$NON-NLS-1$
		BookmarkMessages.getString("ColumnLocation.header")};//$NON-NLS-1$
												
	private ColumnLayoutData columnLayouts[] = {
		new ColumnPixelData(19, false),
		new ColumnWeightData(200),
		new ColumnWeightData(75),
		new ColumnWeightData(150),
		new ColumnWeightData(60)};												
	
	// Persistance tags.
	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	private static final String TAG_ID = "id";//$NON-NLS-1$
	private static final String TAG_MARKER = "marker";//$NON-NLS-1$
	private static final String TAG_RESOURCE = "resource";//$NON-NLS-1$
	private static final String TAG_VERTICAL_POSITION = "verticalPosition";//$NON-NLS-1$
	private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition";//$NON-NLS-1$
	
	class SortByAction extends Action {
		
		private int column;
		
		public SortByAction(int column) {
			if (column < BookmarkConstants.COLUMN_DESCRIPTION || column > BookmarkConstants.COLUMN_LOCATION)
				column = BookmarkConstants.COLUMN_FOLDER;
			else 
				this.column = column;
		}

		public void run() {
			sorter.setTopPriority(column);
			updateSortState();
			viewer.refresh();
			IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
			IDialogSettings settings = workbenchSettings.getSection("BookmarkSortState");//$NON-NLS-1$
			if (settings == null)
				settings = workbenchSettings.addNewSection("BookmarkSortState");//$NON-NLS-1$
			sorter.saveState(settings);
		}
	}

	class ChangeSortDirectionAction extends Action {
		
		private int direction;
		
		public ChangeSortDirectionAction(int direction) {
			if (direction == BookmarkConstants.SORT_ASCENDING || direction == BookmarkConstants.SORT_DESCENDING)
				this.direction = direction;
			else 
				this.direction = BookmarkConstants.SORT_ASCENDING;
		}

		public void run() {
			sorter.setDirection(direction);
			updateSortState();
			viewer.refresh();
			IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
			IDialogSettings settings = workbenchSettings.getSection("BookmarkSortState");//$NON-NLS-1$
			if (settings == null)
				settings = workbenchSettings.addNewSection("BookmarkSortState");//$NON-NLS-1$
			sorter.saveState(settings);
		}
	}

	/**
	 * Creates the bookmarks view.
	 */
	public BookmarkNavigator() {
		super();
	}
	/**
	 * Adds this views contributions to the workbench.
	 */
	void addContributions() {
		// Create the actions.
		openAction = new OpenBookmarkAction(this);
		openAction.setHoverImageDescriptor(getImageDescriptor("clcl16/gotoobj_tsk.gif"));//$NON-NLS-1$
		openAction.setImageDescriptor(getImageDescriptor("elcl16/gotoobj_tsk.gif"));//$NON-NLS-1$
	
		copyAction = new CopyBookmarkAction(this);
		copyAction.setImageDescriptor(getImageDescriptor("ctool16/copy_edit.gif"));//$NON-NLS-1$
		
		pasteAction = new PasteBookmarkAction(this);
		pasteAction.setImageDescriptor(getImageDescriptor("ctool16/paste_edit.gif"));//$NON-NLS-1$
		
		removeAction = new RemoveBookmarkAction(this);
		removeAction.setHoverImageDescriptor(getImageDescriptor("clcl16/remtsk_tsk.gif"));//$NON-NLS-1$
		removeAction.setImageDescriptor(getImageDescriptor("elcl16/remtsk_tsk.gif"));//$NON-NLS-1$
		removeAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/remtsk_tsk.gif"));//$NON-NLS-1$
		
		selectAllAction = new SelectAllAction(this);
		showInNavigatorAction = new ShowInNavigatorAction(getViewSite().getPage(), viewer);
	
		// initializes action enabled state
		handleSelectionChanged(StructuredSelection.EMPTY);
	
		// Create dynamic menu mgr.  Dynamic is currently required to
		// support action contributions.
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu = mgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(mgr, viewer);
		
		// Add actions to the local tool bar
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(removeAction);
		tbm.add(openAction);
		tbm.update(false);
		
		// Register with action service.
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.PASTE, pasteAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, removeAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, selectAllAction);
		
		// Set the double click action.
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				openAction.run();
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged((IStructuredSelection) event.getSelection());
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void createPartControl(Composite parent) {
		clipboard = new Clipboard(parent.getDisplay());
		createTable(parent);
		viewer = new TableViewer(table);
		createColumns();
		
		sorter = new BookmarkSorter();
		viewer.setContentProvider(new BookmarkContentProvider(this));
		viewer.setLabelProvider(new BookmarkLabelProvider(this));
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		viewer.setSorter(sorter);
	
		IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
		IDialogSettings settings = workbenchSettings.getSection("BookmarkSortState");//$NON-NLS-1$
		if(settings == null)
			settings = workbenchSettings.addNewSection("BookmarkSortState");//$NON-NLS-1$
		else
			sorter.restoreState(settings);
	
		addContributions();
		initDragAndDrop();
		createSortActions();
		fillActionBars();
		updateSortState();
		updatePasteEnablement();
	
		getSite().setSelectionProvider(viewer);
		
		if(memento != null) restoreState(memento);
		memento = null;
	
		WorkbenchHelp.setHelp(viewer.getControl(), IBookmarkHelpContextIds.BOOKMARK_VIEW);
	}
	
	public void dispose() {
		if (clipboard != null)
			clipboard.dispose();
	}
	
	/**
	 * Notifies this listener that the menu is about to be shown by
	 * the given menu manager.
	 *
	 * @param manager the menu manager
	 */
	void fillContextMenu(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		
		manager.add(openAction);
		if (selection.size() == 1 && selection.getFirstElement() instanceof IMarker) {
			IMarker marker = (IMarker) selection.getFirstElement();
			IResource resource = marker.getResource();
			if (resource instanceof IFile) {
				MenuManager submenu = new MenuManager(BookmarkMessages.getString("OpenWithMenu.text"), null);//$NON-NLS-1$
				submenu.add(new OpenBookmarkWithMenu(getSite().getPage(), marker));
				manager.add(submenu);
			}
		}
		manager.add(copyAction);
		updatePasteEnablement();
		manager.add(pasteAction);
		manager.add(removeAction);
		manager.add(selectAllAction);
		manager.add(showInNavigatorAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IShowInSource.class) {
			return new IShowInSource() {
				public ShowInContext getShowInContext() {
					return new ShowInContext(null, getViewer().getSelection());
				}
			};
		}
		if (adapter == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { IPageLayout.ID_RES_NAV };
				}

			};
		}
		return super.getAdapter(adapter);
	}
	
	/**
	 * Returns the image descriptor with the given relative path.
	 */
	ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/";//$NON-NLS-1$
		try {
			URL installURL = getPlugin().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		}
		catch (MalformedURLException e) {
			Assert.isTrue(false);
			return null;
		}
	}
	/**
	 * Returns the UI plugin for the bookmarks view.
	 */
	static AbstractUIPlugin getPlugin() {
		return (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	}
	/**
	 * Returns the shell.
	 */
	Shell getShell() {
		return getViewSite().getShell();
	}
	/**
	 * Returns the viewer used to display bookmarks.
	 *
	 * @return the viewer, or <code>null</code> if this view's controls
	 *  have not been created yet
	 */
	StructuredViewer getViewer() {
		return viewer;
	}
	/**
	 * Returns the workspace.
	 */
	IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	/**
	 * Handles key events in viewer.
	 */
	void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 
			&& removeAction.isEnabled())
			removeAction.run();
	}
	/**
	 * Handles a selection change.
	 *
	 * @param selection the new selection
	 */
	void handleSelectionChanged(IStructuredSelection selection) {
		//update the actions
		openAction.selectionChanged(selection);
		removeAction.selectionChanged(selection);
		selectAllAction.selectionChanged(selection);
		showInNavigatorAction.selectionChanged(selection);
	}
	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void init(IViewSite site,IMemento memento) throws PartInitException {
		super.init(site,memento);
		this.memento = memento;
	}
	/**
	 * Adds drag and drop support to the bookmark navigator.
	 */
	protected void initDragAndDrop() {
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[]{
			MarkerTransfer.getInstance(), 
			TextTransfer.getInstance()};
		DragSourceListener listener = new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event){
				performDragSetData(event);
			}
			public void dragFinished(DragSourceEvent event){
			}
		};
		viewer.addDragSupport(operations, transferTypes, listener);	
	}
	/**
	 * The user is attempting to drag marker data.  Add the appropriate
	 * data to the event depending on the transfer type.
	 */
	void performDragSetData(DragSourceEvent event) {
		if (MarkerTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = ((IStructuredSelection) viewer.getSelection()).toArray();
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			Object[] markers = ((IStructuredSelection) viewer.getSelection()).toArray();
			if (markers != null) {
				StringBuffer buffer = new StringBuffer();
				ILabelProvider provider = (ILabelProvider)getViewer().getLabelProvider();
				for (int i = 0; i < markers.length; i++) {
					if (i > 0)
						buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
					buffer.append(provider.getText((IMarker)markers[i]));
				} 
				event.data = buffer.toString();
			}
			return;
		}
	}
	void restoreState(IMemento memento) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IMemento selectionMem = memento.getChild(TAG_SELECTION);
		if(selectionMem != null) {
			ArrayList selectionList = new ArrayList();
			IMemento markerMems[] = selectionMem.getChildren(TAG_MARKER);
			for (int i = 0; i < markerMems.length; i++){
				try {
					long id = new Long(markerMems[i].getString(TAG_ID)).longValue();
					IResource resource = root.findMember(markerMems[i].getString(TAG_RESOURCE));
					if(resource != null) {
						IMarker marker = resource.findMarker(id);
						if(marker != null)
							selectionList.add(marker);
					}
				} catch (CoreException e) {}
			}
			viewer.setSelection(new StructuredSelection(selectionList));
		}
	
		Scrollable scrollable = (Scrollable)viewer.getControl();
		//save vertical position
		ScrollBar bar = scrollable.getVerticalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_VERTICAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e){}
		}
		bar = scrollable.getHorizontalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e){}
		}
		
		updateSortState();
		viewer.refresh();
	}
	public void saveState(IMemento memento) {
		if(viewer == null) {
			if(this.memento != null) //Keep the old state;
				memento.putMemento(this.memento);
			return;
		}
			
		Scrollable scrollable = (Scrollable)viewer.getControl();
	 	Object markers[] = ((IStructuredSelection)viewer.getSelection()).toArray();
	 	if(markers.length > 0) {
	 		IMemento selectionMem = memento.createChild(TAG_SELECTION);
	 		for (int i = 0; i < markers.length; i++) {
		 		IMemento elementMem = selectionMem.createChild(TAG_MARKER);
		 		IMarker marker = (IMarker)markers[i];
	 			elementMem.putString(TAG_RESOURCE,marker.getResource().getFullPath().toString());
	 			elementMem.putString(TAG_ID,String.valueOf(marker.getId()));
	 		}
	 	}
	
	 	//save vertical position
		ScrollBar bar = scrollable.getVerticalBar();
		int position = bar != null ? bar.getSelection():0;
		memento.putString(TAG_VERTICAL_POSITION,String.valueOf(position));
		//save horizontal position
		bar = scrollable.getHorizontalBar();
		position = bar != null ? bar.getSelection():0;
		memento.putString(TAG_HORIZONTAL_POSITION,String.valueOf(position));
		
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void setFocus() {
		if (viewer != null) 
			viewer.getControl().setFocus();
	}
	
	void createColumns() {
		SelectionListener headerListener = new SelectionAdapter() {
			/**
			 * Handles the case of user selecting the
			 * header area.
			 * <p>If the column has not been selected previously,
			 * it will set the sorter of that column to be
			 * the current tasklist sorter. Repeated
			 * presses on the same column header will
			 * toggle sorting order (ascending/descending).
			 */
			public void widgetSelected(SelectionEvent e) {
				// column selected - need to sort
				int column = table.indexOf((TableColumn) e.widget);
				if (column == sorter.getTopPriority())
					sorter.reverse();
				else {
					sorter.setTopPriority(column);
					sorter.setDirection(BookmarkConstants.SORT_ASCENDING);
				}
				updateSortState();
				viewer.refresh();
				IDialogSettings workbenchSettings = getPlugin().getDialogSettings();
				IDialogSettings settings = workbenchSettings.getSection("BookmarkSortState");//$NON-NLS-1$
				if (settings == null)
					settings = workbenchSettings.addNewSection("BookmarkSortState");//$NON-NLS-1$
				sorter.saveState(settings);
			}
		};
		
		
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		for (int i = 0; i < columnHeaders.length; i++) {
			layout.addColumnData(columnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE,i);
			tc.setResizable(columnLayouts[i].resizable);
			tc.setText(columnHeaders[i]);
			tc.addSelectionListener(headerListener);
		}
	}
	
	/**
	 * Creates the table control.
	 */
	void createTable(Composite parent) {
		table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		//table.setLayout(new TableLayout());
	}
	
	/**
	 * Fills the local tool bar and menu manager with actions.
	 */
	void fillActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager menu = actionBars.getMenuManager();
		IMenuManager submenu =
			new MenuManager(BookmarkMessages.getString("SortMenuGroup.text")); //$NON-NLS-1$
		menu.add(submenu);
		submenu.add(sortByDescriptionAction);
		submenu.add(sortByResourceAction);
		submenu.add(sortByFolderAction);
		submenu.add(sortByLineAction);
		submenu.add(new Separator());
		submenu.add(sortAscendingAction);
		submenu.add(sortDescendingAction);
	}
	
	void createSortActions() {
		sortByDescriptionAction = new SortByAction(BookmarkConstants.COLUMN_DESCRIPTION);
		sortByDescriptionAction.setText(columnHeaders[BookmarkConstants.COLUMN_DESCRIPTION]);
		sortByResourceAction = new SortByAction(BookmarkConstants.COLUMN_RESOURCE);
		sortByResourceAction.setText(columnHeaders[BookmarkConstants.COLUMN_RESOURCE]);
		sortByFolderAction = new SortByAction(BookmarkConstants.COLUMN_FOLDER);
		sortByFolderAction.setText(columnHeaders[BookmarkConstants.COLUMN_FOLDER]);
		sortByLineAction = new SortByAction(BookmarkConstants.COLUMN_LOCATION);
		sortByLineAction.setText(columnHeaders[BookmarkConstants.COLUMN_LOCATION]);
		sortAscendingAction = new ChangeSortDirectionAction(BookmarkConstants.SORT_ASCENDING);
		sortAscendingAction.setText(BookmarkMessages.getString("SortDirectionAscending.text"));//$NON-NLS-1$
		sortDescendingAction = new ChangeSortDirectionAction(BookmarkConstants.SORT_DESCENDING);
		sortDescendingAction.setText(BookmarkMessages.getString("SortDirectionDescending.text"));//$NON-NLS-1$
	}
	
	void updateSortState() {
		int column = sorter.getTopPriority();
		sortByDescriptionAction.setChecked(false);
		sortByResourceAction.setChecked(false);
		sortByFolderAction.setChecked(false);
		sortByLineAction.setChecked(false);
		if (column == BookmarkConstants.COLUMN_DESCRIPTION)
			sortByDescriptionAction.setChecked(true);
		else if (column == BookmarkConstants.COLUMN_RESOURCE)
			sortByResourceAction.setChecked(true);
		else if (column == BookmarkConstants.COLUMN_FOLDER)
			sortByFolderAction.setChecked(true);
		else
			sortByLineAction.setChecked(true);
		
		int direction = sorter.getDirection();
		if (direction == BookmarkConstants.SORT_ASCENDING) {
			sortAscendingAction.setChecked(true);
			sortDescendingAction.setChecked(false);
		}
		else {
			sortDescendingAction.setChecked(true);
			sortAscendingAction.setChecked(false);
		}
	}

	/**
	 * Updates the enablement of the paste action
	 */
	void updatePasteEnablement() {
		// Paste if clipboard contains tasks
		MarkerTransfer transfer = MarkerTransfer.getInstance();
		IMarker[] markerData = (IMarker[]) getClipboard().getContents(transfer);
		boolean canPaste = false;
		if (markerData != null) {
			for (int i = 0; i < markerData.length; i++) {
				try {
					if (markerData[i].getType().equals(IMarker.BOOKMARK)) {
						canPaste = true;	
						break;
					}
				}
				catch (CoreException e) {
					canPaste = false;
				}
			}
		}
		pasteAction.setEnabled(canPaste);
	}
	
	Clipboard getClipboard() {
		return clipboard;
	}

}
 