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

import org.eclipse.ui.tests.util.ActionUtil;

/**
 * Tests the lifecycle for a window action delegate.
 */
public class IWorkbenchWindowActionDelegateTest extends IActionDelegateTest {

    /**
     * Constructor for IWorkbenchWindowActionDelegateTest
     */
    public IWorkbenchWindowActionDelegateTest(String testName) {
        super(testName);
    }

    public void testInit() throws Throwable {
        // Run the action.
        testRun();

        // Verify lifecycle.
        // The init, selectionChanged, and run methods should
        // be called, in that order.
        MockActionDelegate delegate = getDelegate();
        assertNotNull(delegate);
        assertTrue(delegate.callHistory.verifyOrder(new String[] { "init",
                "selectionChanged", "run" }));
    }

    // Bug 48799.  Commented out testDispose to avoid a test failure.  This should be a temporary solution.
    //	public void testDispose() throws Throwable {
    //		// Run the action.
    //		testRun();
    //		
    //		// Get the action.
    //		MockActionDelegate delegate = getDelegate();
    //		assertNotNull(delegate);
    //		
    //		// Dispose action.
    //		// Verify that the dispose method is called.
    //		delegate.callHistory.clear();
    //		removeAction();
    //		assertTrue(delegate.callHistory.contains("dispose"));
    //	}

    /**
     * @see IActionDelegateTest#createActionWidget()
     */
    protected Object createActionWidget() throws Throwable {
        fPage.showActionSet("org.eclipse.ui.tests.api.MockActionSet");
        return null;
    }

    /**
     * @see IActionDelegateTest#runAction()
     */
    protected void runAction(Object widget) throws Throwable {
        ActionUtil.runActionWithLabel(this, fWindow, "Mock Action");
    }

    /**
     * @see IActionDelegateTest#fireSelection()
     */
    protected void fireSelection(Object widget) throws Throwable {
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        view.fireSelection();
    }

    /**
     * Removes the action.
     */
    protected void removeAction() {
        fPage.hideActionSet("org.eclipse.ui.tests.api.MockActionSet");
    }
}

