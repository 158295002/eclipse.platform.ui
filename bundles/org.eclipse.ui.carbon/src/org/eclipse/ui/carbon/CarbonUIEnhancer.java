/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.carbon;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.carbon.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


public class CarbonUIEnhancer implements IStartup {
		
	private static final int kHICommandPreferences= ('p'<<24) + ('r'<<16) + ('e'<<8) + 'f';
	private static final int kHICommandAbout= ('a'<<24) + ('b'<<16) + ('o'<<8) + 'u';
	private static final int kHICommandServices= ('s'<<24) + ('e'<<16) + ('r'<<8) + 'v';

	private static final String RESOURCE_BUNDLE= "org.eclipse.ui.carbon.Messages"; //$NON-NLS-1$
	private static String fgAboutActionName;

	public CarbonUIEnhancer() {
		if (fgAboutActionName == null) {
			ResourceBundle resourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);
			try {
				fgAboutActionName= resourceBundle.getString("AboutAction.name");	//$NON-NLS-1$
			} catch (MissingResourceException e) {
				fgAboutActionName= "About Eclipse...";	//$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		final Display display= Display.getDefault();
		display.syncExec(
			new Runnable() {
				public void run() {
					hookApplicationMenu(display);
				}
			}
		);
	}
	
	/**
	 * See Apple Technical Q&A 1079 (http://developer.apple.com/qa/qa2001/qa1079.html)
	 */
	private void hookApplicationMenu(Display display) {
		
		// Callback target
		Object target= new Object() {
			int commandProc(int nextHandler, int theEvent, int userData) {
				if (OS.GetEventKind(theEvent) == OS.kEventProcessCommand) {
					HICommand command= new HICommand();
					OS.GetEventParameter(theEvent, OS.kEventParamDirectObject, OS.typeHICommand, null, HICommand.sizeof, null, command);
					switch (command.commandID) {
					case kHICommandPreferences:
						return runAction("preferences"); //$NON-NLS-1$
					case kHICommandAbout:
						return runAction("about"); //$NON-NLS-1$
					default:
						break;
					}
				}
				return OS.eventNotHandledErr;
			}
		};
		
		final Callback commandCallback= new Callback(target, "commandProc", 3); //$NON-NLS-1$
		int commandProc= commandCallback.getAddress();
		if (commandProc == 0) {
			commandCallback.dispose();
			return; 	// give up
		}

		// Install event handler for commands
		int[] mask= new int[] {
			OS.kEventClassCommand, OS.kEventProcessCommand
		};
		OS.InstallEventHandler(OS.GetApplicationEventTarget(), commandProc, mask.length / 2, mask, 0, null);

		// create About Eclipse menu command
		int[] outMenu= new int[1];
		short[] outIndex= new short[1];
		if (OS.GetIndMenuItemWithCommandID(0, kHICommandPreferences, 1, outMenu, outIndex) == OS.noErr && outMenu[0] != 0) {
			int menu= outMenu[0];

			int l= fgAboutActionName.length();
			char buffer[]= new char[l];
			fgAboutActionName.getChars(0, l, buffer, 0);
			int str= OS.CFStringCreateWithCharacters(OS.kCFAllocatorDefault, buffer, l);
			OS.InsertMenuItemTextWithCFString(menu, str, (short) 0, 0, kHICommandAbout);
			OS.CFRelease(str);
			
			// add separator between About & Preferences
			OS.InsertMenuItemTextWithCFString(menu, 0, (short) 1, OS.kMenuItemAttrSeparator, 0);

			// enable pref menu
			OS.EnableMenuCommand(menu, kHICommandPreferences);
		
			// disable services menu
			OS.DisableMenuCommand(menu, kHICommandServices);
		}

		// schedule disposal of callback object
		display.disposeExec(
			new Runnable() {
				public void run() {
					commandCallback.dispose();
				}
			}
		);
	}
	
	/**
	 * Locate and run an action in the current menubar by name.
	 */
	private int runAction(String name) {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			Shell shell= window.getShell();
			Menu menubar= shell.getMenuBar();
			for (int i= 0; i < menubar.getItemCount(); i++) {
				MenuItem mi= menubar.getItem(i);
				Menu m= mi.getMenu();
				for (int j= 0; j < m.getItemCount(); j++) {
					MenuItem mi2= m.getItem(j);
					Object o= mi2.getData();
					if (o instanceof ActionContributionItem) {
						ActionContributionItem aci= (ActionContributionItem) o;
						String id= aci.getId();
						if (id != null && id.equals(name)) {
							IAction action= aci.getAction();
							if (action != null && action.isEnabled()) {
								action.run();
								return OS.noErr;
							}
						}
					}
				}
			}
		}
		return OS.eventNotHandledErr;
	}
}
