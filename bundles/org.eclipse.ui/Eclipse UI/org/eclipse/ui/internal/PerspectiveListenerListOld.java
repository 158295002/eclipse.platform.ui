package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.core.runtime.Platform;

/**
 * Perspective listener list.
 */
public class PerspectiveListenerListOld {
	private ListenerList listeners = new ListenerList();
/**
 * PerspectiveListenerList constructor comment.
 */
public PerspectiveListenerListOld() {
	super();
}
/**
 * Adds an IInternalPerspectiveListener to the perspective service.
 */
public void addPerspectiveListener(IPerspectiveListener l) {
	listeners.add(l);
}
/**
 * Notifies the listener that a perspective has been activated.
 */
public void firePerspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPerspectiveListener l = (IPerspectiveListener)array[nX];
		Platform.run(new SafeRunnableAdapter() {
			public void run() {
				l.perspectiveActivated(page, perspective);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePerspectiveListener(l);
			}
		});
	}
}
/**
 * Notifies the listener that a perspective has been changed.
 */
public void firePerspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective, final String changeId) {
	Object [] array = listeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPerspectiveListener l = (IPerspectiveListener)array[nX];
		Platform.run(new SafeRunnableAdapter() {
			public void run() {
				l.perspectiveChanged(page, perspective, changeId);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				removePerspectiveListener(l);
			}
		});
	}
}
/**
 * Removes an IInternalPerspectiveListener from the perspective service.
 */
public void removePerspectiveListener(IPerspectiveListener l) {
	listeners.remove(l);
}
}
