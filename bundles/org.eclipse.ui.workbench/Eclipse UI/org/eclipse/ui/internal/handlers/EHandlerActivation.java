/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.services.LegacyEvalContext;
import org.eclipse.ui.internal.services.SourcePriorityNameMapping;

/**
 * @since 3.5
 * 
 */
public class EHandlerActivation implements IHandlerActivation, IRunAndTrack {
	IEclipseContext context;
	private String commandId;
	private IHandler handler;
	E4HandlerProxy proxy;
	private Expression activeWhen;
	private boolean active;
	private int sourcePriority;
	boolean participating = true;

	public EHandlerActivation(IEclipseContext context, String cmdId, IHandler handler,
			E4HandlerProxy handlerProxy, Expression expr) {
		this.context = context;
		this.commandId = cmdId;
		this.handler = handler;
		this.proxy = handlerProxy;
		this.activeWhen = expr;
		this.sourcePriority = SourcePriorityNameMapping.computeSourcePriority(activeWhen);
		proxy.activation = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#clearResult()
	 */
	public void clearResult() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#getExpression()
	 */
	public Expression getExpression() {
		return activeWhen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#getSourcePriority
	 * ()
	 */
	public int getSourcePriority() {
		return sourcePriority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#evaluate(org.
	 * eclipse.core.expressions.IEvaluationContext)
	 */
	public boolean evaluate(IEvaluationContext context) {
		if (activeWhen == null) {
			active = true;
		} else {
			try {
				active = activeWhen.evaluate(context) != EvaluationResult.FALSE;
			} catch (CoreException e) {
				Activator.trace(Policy.DEBUG_CMDS, "Failed to calculate active", e); //$NON-NLS-1$
			}
		}
		return active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#setResult(boolean
	 * )
	 */
	public void setResult(boolean result) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		EHandlerActivation activation = (EHandlerActivation) o;
		int difference;

		// Check the priorities
		int thisPriority = this.getSourcePriority();
		int thatPriority = activation.getSourcePriority();

		// rogue bit problem - ISources.ACTIVE_MENU
		int thisLsb = 0;
		int thatLsb = 0;

		if (((thisPriority & ISources.ACTIVE_MENU) | (thatPriority & ISources.ACTIVE_MENU)) != 0) {
			thisLsb = thisPriority & 1;
			thisPriority = (thisPriority >> 1) & 0x7fffffff;
			thatLsb = thatPriority & 1;
			thatPriority = (thatPriority >> 1) & 0x7fffffff;
		}

		difference = thisPriority - thatPriority;
		if (difference != 0) {
			return difference;
		}

		// if all of the higher bits are the same, check the
		// difference of the LSB
		difference = thisLsb - thatLsb;
		if (difference != 0) {
			return difference;
		}

		// Check depth
		final int thisDepth = this.getDepth();
		final int thatDepth = activation.getDepth();
		difference = thisDepth - thatDepth;
		return difference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.IRunAndTrack#notify(org.eclipse.
	 * e4.core.services.context.ContextChangeEvent)
	 */
	public boolean notify(ContextChangeEvent event) {
		if (event.getEventType() == ContextChangeEvent.DISPOSE) {
			return false;
		}
		if (!participating) {
			return false;
		}
		final EHandlerService hs = (EHandlerService) context.get(EHandlerService.class.getName());
		Object obj = HandlerServiceImpl.lookUpHandler(context, commandId);

		if (evaluate(new LegacyEvalContext(context))) {
			if (obj instanceof E4HandlerProxy) {
				final EHandlerActivation bestActivation = ((E4HandlerProxy) obj).activation;
				final int comparison = bestActivation.compareTo(this);
				if (comparison < 0) {
					hs.activateHandler(commandId, proxy);
				}
			} else if (obj == null) {
				hs.activateHandler(commandId, proxy);
			}
		} else {
			if (obj == proxy) {
				hs.deactivateHandler(commandId, proxy);
			}
		}
		return participating;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerActivation#clearActive()
	 */
	public void clearActive() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerActivation#getCommandId()
	 */
	public String getCommandId() {
		return commandId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerActivation#getDepth()
	 */
	public int getDepth() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerActivation#getHandler()
	 */
	public IHandler getHandler() {
		return handler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.handlers.IHandlerActivation#getHandlerService()
	 */
	public IHandlerService getHandlerService() {
		return (IHandlerService) context.get(IHandlerService.class.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.handlers.IHandlerActivation#isActive(org.eclipse.core.
	 * expressions.IEvaluationContext)
	 */
	public boolean isActive(IEvaluationContext context) {
		return active;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EHA: " + active + ":" + sourcePriority + ":" + commandId + ": " + proxy //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ ": " + handler + ": " + context; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
