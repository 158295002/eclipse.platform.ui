package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.*;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An EditorPane is a subclass of PartPane offering extended
 * behavior for workbench editors.
 */
public class EditorPane extends PartPane {
	private EditorWorkbook workbook;
/**
 * Constructs an editor pane for an editor part.
 */
public EditorPane(IEditorPart part, WorkbenchPage page, EditorWorkbook workbook) {
	super(part, page);
	this.workbook = workbook;
}
protected WorkbenchPart createErrorPart(WorkbenchPart oldPart) {
	class ErrorEditorPart extends EditorPart {
		private Text text;
		public void doSave(IProgressMonitor monitor) {}
		public void doSaveAs() {}
		public void gotoMarker(IMarker marker) {}
		public void init(IEditorSite site, IEditorInput input) {
			setSite(site);
			setInput(input);
		}
		public boolean isDirty() {return false;}
		public boolean isSaveAsAllowed() {return false;}
		public void createPartControl(Composite parent) {
			text = new Text(parent,SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
			text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
			text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_GRAY));
			text.setText(WorkbenchMessages.getString("EditorPane.errorMessage")); //$NON-NLS-1$
		}
		public void setFocus() {
			if (text != null) text.setFocus();
		}
	}
	IEditorPart oldEditorPart = (IEditorPart)oldPart;
	EditorSite oldEditorSite = (EditorSite)oldEditorPart.getEditorSite();
	ErrorEditorPart newPart = new ErrorEditorPart();
	oldEditorSite.setPart(newPart);
	newPart.init(oldEditorSite, oldEditorPart.getEditorInput());
	return newPart;
}
/**
 * Editor panes do not need a title bar. The editor
 * title and close icon are part of the tab containing
 * the editor. Tools and menus are added directly into
 * the workbench toolbar and menu bar.
 */
protected void createTitleBar() {
	// do nothing
}
/**
 * @see PartPane::doHide
 */
public void doHide() {
	IWorkbenchPage page = getPart().getSite().getPage();
	page.closeEditor(getEditorPart(), true);
}
/**
 * Answer the editor part child.
 */
public IEditorPart getEditorPart() {
	return (IEditorPart)getPart();
}
/**
 * Answer the SWT widget style.
 */
int getStyle() {
	return SWT.NONE;
}
/**
 * Answer the editor workbook container
 */
public EditorWorkbook getWorkbook() {
	return workbook;
}
/**
 * See LayoutPart
 */
public boolean isDragAllowed(Point p) {
	return workbook.isDragAllowed(p) && super.isDragAllowed(p);
}

/**
 * Notify the workbook page that the part pane has
 * been activated by the user.
 */
protected void requestActivation() {
	// By clearing the active workbook if its not the one
	// associated with the editor, we reduce draw flicker
	if (!getWorkbook().isActiveWorkbook())
		getWorkbook().getEditorArea().setActiveWorkbook(null, false);
		
	super.requestActivation();
}
/**
 * Set the editor workbook container
 */
public void setWorkbook(EditorWorkbook editorWorkbook) {
	workbook = editorWorkbook;
}
/* (non-Javadoc)
 * Method declared on PartPane.
 */
/* package */ void shellActivated() {
	this.workbook.drawGradient();
}

/* (non-Javadoc)
 * Method declared on PartPane.
 */
/* package */ void shellDeactivated() {
	this.workbook.drawGradient();
}
/**
 * Indicate focus in part.
 */
public void showFocus(boolean inFocus) {
	if (inFocus)
		this.workbook.becomeActiveWorkbook(true);
	else
		this.workbook.tabFocusHide();
}
/**
 * Add the Editor and Tab Group items to the Move menu.
 */
protected void addMoveItems(Menu moveMenu) {
	MenuItem item = new MenuItem(moveMenu, SWT.NONE);
	item.setText(WorkbenchMessages.getString("EditorPane.moveEditor")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			ILayoutContainer container = getContainer();
			if (container instanceof EditorWorkbook)
				((EditorWorkbook)container).openTracker(EditorPane.this);
		}
	});
	item.setEnabled(!isZoomed());
	item = new MenuItem(moveMenu, SWT.NONE);
	item.setText(WorkbenchMessages.getString("EditorPane.moveFolder")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			ILayoutContainer container = getContainer();
			if (container instanceof EditorWorkbook)
				((EditorWorkbook)container).openTracker((EditorWorkbook)container);
		}
	});
	item.setEnabled(!isZoomed() && (getContainer() instanceof EditorWorkbook));
}

/**
 * Return the sashes around this part.
 */
protected Sashes findSashes() {
	Sashes result = new Sashes();
	workbook.getEditorArea().findSashes(workbook,result);
	return result;
}
/**
 * Update the title attributes for the pane.
 */
public void updateTitles() {
	getWorkbook().updateEditorTab(getEditorPart());
}
/**
 * Show a title label menu for this pane.
 */
public void showPaneMenu() {
	workbook.showPaneMenu();
}
/**
 * Show the context menu for this part.
 */
public void showViewMenu(){
	//Do nothing. Editors do not have menus
}
}
