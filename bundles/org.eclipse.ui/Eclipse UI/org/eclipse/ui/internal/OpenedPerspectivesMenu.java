package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;

/**
 * A dynamic contribution item which shows all opened perspectives
 * in the window's active page.
 */
public class OpenedPerspectivesMenu extends ContributionItem {
	private WorkbenchWindow window;
	private boolean showSeparator;

	private static final int MAX_TEXT_LENGTH = 40;

	/**
	 * Create a new instance.
	 */
	public OpenedPerspectivesMenu(WorkbenchWindow window, boolean showSeparator) {
		super("Opened perspectives"); //$NON-NLS-1$
		this.window = window;
		this.showSeparator = showSeparator;
	}
	
	/**
	 * Returns the text for a perspective. This may be truncated to fit
	 * within the MAX_TEXT_LENGTH.
	 */
	private String calcText(int number, Perspective persp) {
		StringBuffer sb = new StringBuffer();
		if (number < 10)
			sb.append('&');
		sb.append(number);
		sb.append(' ');
		String suffix = persp.getDesc().getLabel();
		if (suffix.length() <= MAX_TEXT_LENGTH) {
			sb.append(suffix);
		} else {
			sb.append(suffix.substring(0, MAX_TEXT_LENGTH / 2));
			sb.append("..."); //$NON-NLS-1$
			sb.append(suffix.substring(suffix.length() - MAX_TEXT_LENGTH / 2));
		}
		return sb.toString();
	}

	/**
	 * Fills the given menu with menu items for all opened perspectives.
	 */
	public void fill(Menu menu, int index) {
		final WorkbenchPage page = window.getActiveWorkbenchPage();
		if (page == null)
			return;

		// Add separator.
		if (showSeparator) {
			new MenuItem(menu, SWT.SEPARATOR, index);
			++index;
		}

		// Add one item for each opened perspective.
		Perspective activePersp = page.getActivePerspective();
		Iterator enum = page.getOpenedPerspectives();
		int count = 1;
		while (enum.hasNext()) {
			final Perspective persp = (Perspective) enum.next();
			MenuItem mi = new MenuItem(menu, SWT.RADIO, index);
			mi.setSelection(persp == activePersp);
			mi.setText(calcText(count, persp));
			mi.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					page.setPerspective(persp.getDesc());
				}
			});
		
			index++;
			count++;
		}
	}
	
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}
}