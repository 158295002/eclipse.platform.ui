package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.*;


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
	
	public void setUp() {
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

