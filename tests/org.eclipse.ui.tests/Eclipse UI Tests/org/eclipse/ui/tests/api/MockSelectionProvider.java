package org.eclipse.ui.tests.api;

import java.util.*;

import org.eclipse.jface.viewers.*;

public class MockSelectionProvider implements ISelectionProvider {

	private List listeners = new ArrayList(3);

	/**
	 * Fires out a selection to all listeners.
	 */
	public void fireSelection() {
		fireSelection(new SelectionChangedEvent(this,
			StructuredSelection.EMPTY));
	}
		
	/**
	 * Fires out a selection to all listeners.
	 */
	public void fireSelection(SelectionChangedEvent event) {
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			((ISelectionChangedListener)iter.next()).selectionChanged(event);
		}
	}
		
	/**
	 * @see ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * @see ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	/**
	 * @see ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @see ISelectionProvider#setSelection(ISelection)
	 */
	public void setSelection(ISelection selection) {
	}
}

