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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.util.CallHistory;
import org.eclipse.ui.tests.util.UITestCase;


/**
 * This is a test for IWorkbenchPart.  Since IWorkbenchPart is an
 * interface this test verifies the IWorkbenchPart lifecycle rather
 * than the implementation.
 */
public abstract class IWorkbenchPartTest extends UITestCase {

	protected IWorkbenchWindow fWindow;
	protected IWorkbenchPage fPage;
	
	/**
	 * Constructor for IActionDelegateTest
	 */
	public IWorkbenchPartTest(String testName) {
		super(testName);
	}
	
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}
	
	public void testOpenAndClose() throws Throwable {
		// Open a part.
		MockWorkbenchPart part = openPart(fPage);
		CallHistory history = part.getCallHistory();
		assertTrue(history.verifyOrder(new String[] {
			"init", "createPartControl", "setFocus" }));
		
		// Close the part.
		closePart(fPage, part);
		assertTrue(history.verifyOrder(new String[] {
			"init", "createPartControl", "setFocus", "dispose"}));
	}
	
	/**
	 * Opens a part.  Subclasses should override
	 */
	protected abstract MockWorkbenchPart openPart(IWorkbenchPage page) 
		throws Throwable;
	
	/**
	 * Closes a part.  Subclasses should override
	 */
	protected abstract void closePart(IWorkbenchPage page, MockWorkbenchPart part) 
		throws Throwable;
}

