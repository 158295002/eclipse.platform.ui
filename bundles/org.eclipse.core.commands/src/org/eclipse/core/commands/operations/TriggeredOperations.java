/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * Triggered operations are a special implementation for a composite that keeps
 * track of operations triggered by the execution of some primary operation. The
 * operation knows which operation was the trigger, and adds all triggered
 * operations as children. When execution, undo, or redo is performed, only the
 * triggered operation is executed, undone, or redone if it is still present. If
 * the trigger is no longer present, than the operation is invalid.
 * </p>
 * 
 * @since 3.1
 */
public class TriggeredOperations extends AbstractOperation implements
		ICompositeOperation, IUndoableAffectedObjects, IHistoryNotificationAwareOperation {

	private IUndoableOperation triggeringOperation;

	private IOperationHistory history;

	private List children = new ArrayList();

	/**
	 * Construct a composite of triggered operation using the specified
	 * operation as the trigger. Use the label of this trigger as the label of
	 * the operation.
	 * 
	 * @param operation -
	 *            the operation triggering other operations.
	 * @param history -
	 *            the operation history containing the triggered operations
	 */
	public TriggeredOperations(IUndoableOperation operation,
			IOperationHistory history) {
		super(operation.getLabel());
		triggeringOperation = operation;
		recomputeContexts();
		this.history = history;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#add(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void add(IUndoableOperation operation) {
		children.add(operation);
		IUndoContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			if (!hasContext(contexts[i])) {
				addContext(contexts[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#remove(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void remove(IUndoableOperation operation) {
		if (operation == triggeringOperation) {
			triggeringOperation = null;
			history.replaceOperation(this, (IUndoableOperation[]) children
					.toArray(new IUndoableOperation[children.size()]));
		} else {
			children.remove(operation);
			operation.dispose();
			recomputeContexts();
		}
	}

	/**
	 * Remove the specified context from the receiver. This method is typically
	 * invoked when the history is being flushed for a certain context. In the
	 * case of triggered operations, if the context for the triggering operation
	 * is being removed, then the triggering operation must be replaced with 
	 * the atomic operations that it triggered.
	 * 
	 * @param context -
	 *            the undo context being removed from the receiver.
	 */
	public void removeContext(IUndoContext context) {
		
		// first check to see if we are removing the only context of the
		// triggering operation
		if (triggeringOperation != null && triggeringOperation.hasContext(context)) {
			if (triggeringOperation.getContexts().length == 1) {
				remove(triggeringOperation);
				return;
			}
			triggeringOperation.removeContext(context);
			recomputeContexts();
		}
		// the triggering operation remains, check all the children
		ArrayList toBeRemoved = new ArrayList();
		for (int i = 0; i < children.size(); i++) {
			IUndoableOperation child = (IUndoableOperation) children.get(i);
			if (child.hasContext(context)) {
				if (child.getContexts().length == 1) {
					toBeRemoved.add(child);
				} else {
					child.removeContext(context);
					recomputeContexts();
				}
			}
		}
		for (int i = 0; i < toBeRemoved.size(); i++) {
			remove((IUndoableOperation) toBeRemoved.get(i));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (triggeringOperation != null) {
			history.openOperation(this, IOperationHistory.EXECUTE);
			try {
				IStatus status = triggeringOperation.execute(monitor, info);
				history.closeOperation(status.isOK(), false,
						IOperationHistory.EXECUTE);
				return status;
			} catch (ExecutionException e) {
				history.closeOperation(false, false, IOperationHistory.EXECUTE);
				throw e;
			}
		}
		return IOperationHistory.OPERATION_INVALID_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (triggeringOperation != null) {
			history.openOperation(this, IOperationHistory.REDO);
			try {
				removeAllChildren();
				IStatus status = triggeringOperation.redo(monitor, info);
				history.closeOperation(status.isOK(), false,
						IOperationHistory.REDO);
				return status;
			} catch (ExecutionException e) {
				history.closeOperation(false, false, IOperationHistory.REDO);
				throw e;
			}
		}
		return IOperationHistory.OPERATION_INVALID_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (triggeringOperation != null) {
			history.openOperation(this, IOperationHistory.UNDO);
			try {
				removeAllChildren();
				IStatus status = triggeringOperation.undo(monitor, info);
				history.closeOperation(status.isOK(), false,
						IOperationHistory.UNDO);
				return status;
			} catch (ExecutionException e) {
				history.closeOperation(false, false, IOperationHistory.UNDO);
				throw e;
			}
		}
		return IOperationHistory.OPERATION_INVALID_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
	 */
	public boolean canUndo() {
		if (triggeringOperation != null) {
			return triggeringOperation.canUndo();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
	 */
	public boolean canExecute() {
		if (triggeringOperation != null) {
			return triggeringOperation.canExecute();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canRedo()
	 */
	public boolean canRedo() {
		if (triggeringOperation != null) {
			return triggeringOperation.canRedo();
		}
		return false;
	}

	/*
	 * Dispose all operations in the receiver.
	 */
	public void dispose() {
		for (int i = 0; i < children.size(); i++) {
			((IUndoableOperation) (children.get(i))).dispose();
		}
		if (triggeringOperation != null) {
			triggeringOperation.dispose();
		}
	}

	/*
	 * Recompute contexts in light of some change in the children
	 */
	private void recomputeContexts() {
		ArrayList allContexts = new ArrayList();
		if (triggeringOperation != null) {
			IUndoContext[] contexts = triggeringOperation.getContexts();
			for (int i = 0; i < contexts.length; i++)
				allContexts.add(contexts[i]);
		}
		for (int i = 0; i < children.size(); i++) {
			IUndoContext[] contexts = ((IUndoableOperation) children.get(i))
					.getContexts();
			for (int j = 0; j < contexts.length; j++) {
				if (!allContexts.contains(contexts[j])) {
					allContexts.add(contexts[j]);
				}
			}
		}
		contexts = allContexts;

	}

	/*
	 * Remove all non-triggering children
	 */
	private void removeAllChildren() {
		IUndoableOperation[] nonTriggers = (IUndoableOperation[]) children
				.toArray(new IUndoableOperation[children.size()]);
		for (int i = 0; i < nonTriggers.length; i++) {
			children.remove(nonTriggers[i]);
			nonTriggers[i].dispose();
		}
	}

	/**
	 * Return the operation that triggered the other operations in this
	 * composite.
	 * 
	 * @return - the IUndoableOperation that triggered the other children.
	 */
	public IUndoableOperation getTriggeringOperation() {
		return triggeringOperation;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableAffectedObjects#getAffectedObjects()
	 */
	public Object [] getAffectedObjects() {
		if (triggeringOperation instanceof IUndoableAffectedObjects)
			return ((IUndoableAffectedObjects)triggeringOperation).getAffectedObjects();
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IHistoryNotificationAwareOperation#aboutToNotify(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 */
	public void aboutToNotify(OperationHistoryEvent event) {
		if (triggeringOperation instanceof IHistoryNotificationAwareOperation)
			((IHistoryNotificationAwareOperation)triggeringOperation).aboutToNotify(event);
	}
}
