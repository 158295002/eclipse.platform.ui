package org.eclipse.ui.tests.api;

import org.eclipse.ui.tests.util.UITestCase;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.tests.SelectionProviderView;

/**
 * Tests the ISelectionService class.
 */
public class ISelectionServiceTest extends UITestCase 
	implements ISelectionListener
{
	private IWorkbenchWindow fWindow;
	private IWorkbenchPage fPage;
	
	// Event state.
	private boolean eventReceived;
	private ISelection eventSelection;
	private IWorkbenchPart eventPart;
	
	public ISelectionServiceTest(String testName) {
		super(testName);
	}

	protected void setUp() {
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}
	
	/**
	 * Tests the addSelectionListener method.
	 */	
	public void testAddSelectionListener() throws Throwable {
		// From Javadoc: "Adds the given selection listener.
		// Has no effect if an identical listener is already registered."
		
		// Add listener.
		fPage.addSelectionListener(this);
		
		// Open a view and select something.
		// Verify events are received.
		clearEventState();
		SelectionProviderView view = (SelectionProviderView)
			fPage.showView(SelectionProviderView.ID);
		view.setSelection("Selection");
		assertTrue("EventReceived", eventReceived);
	}
	
	/**
	 * Tests the removePageListener method.
	 */
	public void testRemoveSelectionListener() throws Throwable {
		// From Javadoc: "Removes the given selection listener.
 		// Has no affect if an identical listener is not registered."
		
		// Add and remove listener.
		fPage.addSelectionListener(this);
		fPage.removeSelectionListener(this);		
		
		// Open a view and select something.
		// Verify no events are received.
		clearEventState();
		SelectionProviderView view = (SelectionProviderView)
			fPage.showView(SelectionProviderView.ID);
		view.setSelection("Selection");
		assertTrue("EventReceived", !eventReceived);
	}

	/**
	 * Tests getActivePage.
	 */
	public void testGetSelection() throws Throwable {
		// From Javadoc: "Returns the current selection in the active part.  
		// If the selection in the active part is <em>undefined</em> (the 
		// active part has no selection provider) the result will be 
		// <code>null</code>"
		Object actualSel, sel1 = "Selection 1", sel2 = "Selection 2";
		
		// Open view.
		SelectionProviderView view = (SelectionProviderView)
			fPage.showView(SelectionProviderView.ID);
			
		// Fire selection and verify.
		view.setSelection(sel1);
		actualSel = unwrapSelection(fPage.getSelection());
		assertEquals("Selection", sel1, actualSel);
		
		// Fire selection and verify.
		view.setSelection(sel2);
		actualSel = unwrapSelection(fPage.getSelection());
		assertEquals("Selection", sel2, actualSel);
		
		// Close view and verify.
		fPage.hideView(view);
		assertNull("getSelection", fPage.getSelection());
	}

	/**
	 * Test event firing for inactive parts.  In this scenario 
	 * the event should not be fired.
	 */
	public void testSelectionEventWhenInactive() throws Throwable {
		Object sel1 = "Selection 1", sel2 = "Selection 2";
		
		// Add listener.
		fPage.addSelectionListener(this);

		// Open two views.
		SelectionProviderView view1 = (SelectionProviderView)
			fPage.showView(SelectionProviderView.ID);
		SelectionProviderView view2 = (SelectionProviderView)
			fPage.showView(SelectionProviderView.ID_2);
			
		// Fire selection from the second.
		// Verify it is received.
		clearEventState();
		view2.setSelection(sel2);
		assertTrue("EventReceived", eventReceived);
		assertEquals("EventPart", view2, eventPart);
		assertEquals("Event Selection", sel2, unwrapSelection(eventSelection));

		// Fire selection from the first.
		// Verify it is NOT received.
		clearEventState();
		view1.setSelection(sel1);
		assertTrue("Unexpected selection events received", !eventReceived);
	}
		
	/**
	 * Test event firing when activated.  
	 */
	public void testSelectionEventWhenActivated() throws Throwable {
		// From Javadoc: "Adds the given selection listener.
		// Has no effect if an identical listener is already registered."
		Object sel1 = "Selection 1", sel2 = "Selection 2";
		
		// Add listener.
		fPage.addSelectionListener(this);
		
		// Open a view and select something.
		SelectionProviderView view1 = (SelectionProviderView)
			fPage.showView(SelectionProviderView.ID);
		view1.setSelection(sel1);
		
		// Open another view and select something.
		SelectionProviderView view2 = (SelectionProviderView)
			fPage.showView(SelectionProviderView.ID_2);
		view2.setSelection(sel2);
		
		// Activate the first view.
		// Verify that selection events are fired.
		clearEventState();
		fPage.activate(view1);
		assertTrue("EventReceived", eventReceived);
		assertEquals("EventPart", view1, eventPart);
		assertEquals("Event Selection", sel1, unwrapSelection(eventSelection));
		
		// Activate the second view.
		// Verify that selection events are fired.
		clearEventState();
		fPage.activate(view2);
		assertTrue("EventReceived", eventReceived);
		assertEquals("EventPart", view2, eventPart);
		assertEquals("Event Selection", sel2, unwrapSelection(eventSelection));
	}
		
	/**
	 * Unwrap a selection.
	 */
	private Object unwrapSelection(ISelection sel) {
		if (sel instanceof StructuredSelection) {
			StructuredSelection struct = (StructuredSelection)sel;
			if (struct.size() == 1)
				return struct.getFirstElement();
		}
		return null;
	}
	
	/**
	 * Clear the event state.
	 */
	private void clearEventState() {
		eventReceived = false;
		eventPart = null;
		eventSelection = null;
	}
		
	/*
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		eventReceived = true;
		eventPart = part;
		eventSelection = selection;
	}

}