package org.eclipse.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * The primary interface between a workbench part and the outside world.
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 */
public interface IWorkbenchPartSite {
/**
 * Returns the part registry extension id for this workbench site's part.
 * <p>
 * The name comes from the <code>id</code> attribute in the configuration
 * element.
 * </p>
 *
 * @return the registry extension id
 * @see #getConfigurationElement
 */
public String getId();
/**
 * Returns the page containing this workbench site's part.
 *
 * @return the page containing this part
 */
public IWorkbenchPage getPage();
/**
 * Returns the unique identifier of the plug-in that defines this workbench
 * site's part.
 *
 * @return the unique identifier of the declaring plug-in
 * @see org.eclipse.core.runtime.IPluginDescriptor#getUniqueIdentifier
 */
public String getPluginId();
/**
 * Returns the registered name for this workbench site's part.
 * <p>
 * The name comes from the <code>name</code> attribute in the configuration
 * element.
 * </p>
 *
 * @return the part name
 */
public String getRegisteredName();
/**
 * Returns the selection provider for this workbench site's part.
 *
 * @return the selection provider, or <code>null</code> if none
 */
public ISelectionProvider getSelectionProvider();
/**
 * Returns the shell containing this workbench site's part.
 *
 * @return the shell containing the part's controls
 */
public Shell getShell();
/**
 * Returns the workbench window containing this workbench site's part.
 *
 * @return the workbench window containing this part
 */
public IWorkbenchWindow getWorkbenchWindow();
/**
 * Registers a pop-up menu with a particular id for extension.
 * This method should only be called if the target part has more
 * than one context menu to register.
 * <p>
 * For a detailed description of context menu registration see 
 * <code>registerContextMenu(MenuManager, ISelectionProvider);
 * </p>
 *
 * @param menuId the menu id
 * @param menuManager the menu manager
 * @param selectionProvider the selection provider
 */
public void registerContextMenu(String menuId, MenuManager menuManager,
	ISelectionProvider selectionProvider);
/**
 * Registers a pop-up menu with the default id for extension.  
 * The default id is defined as the part id.
 * <p>
 * Within the workbench one plug-in may extend the pop-up menus for a view
 * or editor within another plug-in.  In order to be eligible for extension,
 * the target part must publish each menu by calling <code>registerContextMenu</code>.
 * Once this has been done the workbench will automatically insert any action 
 * extensions which exist.
 * </p>
 * <p>
 * A menu id must be provided for each registered menu.  For consistency across
 * parts the following strategy should be adopted by all part implementors.
 * </p>
 * <ol>
 *		<li>If the target part has only one context menu it should be registered
 *			with <code>id == part id</code>.  This can be done easily by calling
 *			<code>registerContextMenu(MenuManager, ISelectionProvider).  
 *		<li>If the target part has more than one context menu a unique id should be
 *			defined for each.  Prefix each id with the view id and publish these 
 *			id's within the javadoc for the target part.  Register each menu at 
 *			runtime by calling <code>registerContextMenu(String, MenuManager, 
 *			ISelectionProvider)</code>.  </li>
 * </ol>
 * <p>
 * Any pop-up menu which is registered with the workbench should also define a  
 * <code>GroupMarker</code> in the registered menu with id 
 * <code>IWorkbenchActionConstants.MB_ADDITIONS</code>.  Other plug-ins will use this 
 * group as a reference point for insertion.  The marker should be defined at an 
 * appropriate location within the menu for insertion.  
 * </p>
 *
 * @param menuManager the menu manager
 * @param selectionProvider the selection provider
 */
public void registerContextMenu(MenuManager menuManager,
	ISelectionProvider selectionProvider);
/**
 * Sets the selection provider for this workbench site's part.
 *
 * @param provider the selection provider, or <code>null</code> to clear it
 */
public void setSelectionProvider(ISelectionProvider provider);
}
