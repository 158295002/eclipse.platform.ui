package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * This is a test for IViewPart.  Since IViewPart is an
 * interface this test verifies the IViewPart lifecycle rather
 * than the implementation.
 */
public class IViewPartTest extends IWorkbenchPartTest {

	/**
	 * Constructor for IEditorPartTest
	 */
	public IViewPartTest(String testName) {
		super(testName);
	}

	/**
	 * @see IWorkbenchPartTest#openPart(IWorkbenchPage)
	 */
	protected MockWorkbenchPart openPart(IWorkbenchPage page) throws Throwable {
		return (MockWorkbenchPart)page.showView(MockViewPart.ID);
	}

	/**
	 * @see IWorkbenchPartTest#closePart(IWorkbenchPage, MockWorkbenchPart)
	 */
	protected void closePart(IWorkbenchPage page, MockWorkbenchPart part)
		throws Throwable 
	{
		page.hideView((IViewPart)part);
	}
}

