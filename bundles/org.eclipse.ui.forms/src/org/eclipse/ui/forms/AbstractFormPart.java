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
package org.eclipse.ui.forms;
/**
 * AbstractFormPart implements IFormPart interface and can be used as a
 * convinient base class for concrete form parts.
 * 
 * @see Section
 */
public abstract class AbstractFormPart implements IFormPart {
	protected IManagedForm managedForm;
	private boolean dirty = false;
	private boolean stale = true;
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}
	/**
	 * Returns the form that manages this part.
	 * 
	 * @return the managed form
	 */
	public IManagedForm getForm() {
		return managedForm;
	}
	/**
	 * Disposes the part. Subclasses should override to release any system
	 * resources.
	 */
	public void dispose() {
	}
	/**
	 * Commits the part. Subclasses should call 'super' when overriding.
	 * 
	 * @param onSave
	 *            <code>true</code> if the request to commit has arrived as a
	 *            result of the 'save' action.
	 */
	public void commit(boolean onSave) {
		dirty = false;
	}
	/**
	 * Sets the overal form input. Subclases may elect to override the method
	 * and adjust according to the form input.
	 * 
	 * @param input
	 *            the form input object
	 */
	public void setFormInput(Object input) {
	}
	/**
	 * Instructs the part to grab keyboard focus.
	 */
	public void setFocus() {
	}
	/**
	 * Refreshes the section after becoming stale (falling behind data in the
	 * model). Subclasses must call 'super' when overriding this method.
	 */
	public void refresh() {
		stale = false;
	}
	/**
	 * Marks the part dirty. Subclasses should call this method as a result of
	 * user interaction with the widgets in the section.
	 */
	public void markDirty() {
		dirty = true;
		managedForm.dirtyStateChanged();
	}
	/**
	 * Tests whether the part is dirty i.e. its widgets have state that is
	 * newer than the data in the model.
	 * 
	 * @return <code>true</code> if the part is dirty, <code>false</code>
	 *         otherwise.
	 */
	public boolean isDirty() {
		return dirty;
	}
	/**
	 * Tests whether the part is stale i.e. its widgets have state that is
	 * older than the data in the model.
	 * 
	 * @return <code>true</code> if the part is stale, <code>false</code>
	 *         otherwise.
	 */
	public boolean isStale() {
		return stale;
	}
	/**
	 * Marks the part stale. Subclasses should call this method as a result of
	 * model notification that indicates that the content of the section is no
	 * longer in sync with the model.
	 */
	public void markStale() {
		stale = true;
		managedForm.staleStateChanged();
	}
}