package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import junit.framework.TestCase;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;

public abstract class StructuredViewerTest extends TestCase {
	Display fDisplay;
	Shell fShell;
	StructuredViewer fViewer;
	TestElement fRootElement;

	public static class TestLabelFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parent, Object element) {
			String label = ((TestElement) element).getLabel();
			int count = label.indexOf("-");
			if (count < 0)
				return false;
			String number = label.substring(count + 1);
			return ((Integer.parseInt(number) % 2) == 0);
		}
		public boolean isFilterProperty(Object element, String property) {
			return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	};

	public static class TestLabelSorter extends ViewerSorter {
		public int compare(Viewer v, Object e1, Object e2) {
			// put greater labels first
			String name1 = ((TestElement) e1).getLabel();
			String name2 = ((TestElement) e2).getLabel();
			return name2.compareTo(name1);
		}
		public boolean isSorterProperty(Object element, String property) {
			return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	};

	public static class TestLabelProvider extends LabelProvider {
		public static String fgSuffix = "";

		static Image fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class, "images/java.gif").createImage();

		public String getText(Object element) {
			return providedString((TestElement) element);
		}

		public Image getImage(Object element) {
			return fgImage;
		}

		public void setSuffix(String suffix) {
			fgSuffix = suffix;
			fireLabelProviderChanged(new LabelProviderChangedEvent(this));
		}
	};

	public TestModel fModel;
	public StructuredViewerTest(String name) {
		super(name);
	}
protected void assertSelectionEquals(String message, TestElement expected) {
	ISelection selection = fViewer.getSelection();
	assertTrue(selection instanceof StructuredSelection);
	StructuredSelection expectedSelection = new StructuredSelection(expected);
	assertEquals("selections", selection, expectedSelection);
}
protected void bulkChange(TestModelChange eventToFire) {
	TestElement first = fRootElement.getFirstChild();
	TestElement newElement = first.getContainer().basicAddChild();
	fRootElement.basicDeleteChild(first);
	fModel.fireModelChanged(eventToFire);
	assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
	assertNull("first child is not visible", fViewer.testFindItem(first));
}
/**
 * Creates the viewer used by this test, under the given parent widget.
 */
protected abstract StructuredViewer createViewer(Composite parent);
	protected abstract int getItemCount();
	protected abstract String getItemText(int at);
	/**
	 * Interacts with a test set up. Call this method from your
	 * test when you want to interactively experiment with a set up.
	 * The interaction terminates when the browser is closed.
	 */
	public void interact() {
		Shell shell= fShell;
		if (shell != null && !shell.isDisposed()) {
			Display display= shell.getDisplay();
			if (display != null) {
				while (shell.isVisible()) 
					display.readAndDispatch();
			}
		}	
	}
	protected void openBrowser() {
		fDisplay = Display.getCurrent();
		if (fDisplay == null) {
			fDisplay = new Display();
		}
		fShell = new Shell(fDisplay);
		fShell.setSize(500, 500);
		fShell.setLayout(new FillLayout());
		fViewer = createViewer(fShell);
		fViewer.setUseHashlookup(true);
		fViewer.setInput(fRootElement);
		fShell.open();
		//processEvents();
	}
public void processEvents() {
	Shell shell = fShell;
	if (shell != null && !shell.isDisposed()) {
		Display display = shell.getDisplay();
		if (display != null) {
			while (display.readAndDispatch())
				;
		}
	}
}
	public static String providedString(String s) {
		return s+"<rendered>"+TestLabelProvider.fgSuffix;
	}
	public static String providedString(TestElement element) {
		return element.getID()+" "+element.getLabel()+"<rendered>"+TestLabelProvider.fgSuffix;
	}
	public void setUp() {
		fRootElement= TestElement.createModel(3, 10);
		fModel= fRootElement.getModel();
		openBrowser();
	}
	/**
	 * Sleeps for the given duration and processes pending
	 * events. Call this method to temporarily suspend a test
	 * see the current state in the browser.
	 */
	void sleep(int d) {
		processEvents();
		try {
			Thread.sleep(d*1000);
		} catch(Exception e) {}
	}
public void tearDown() {
	processEvents();
	fViewer = null;
	if (fShell != null) {
		fShell.dispose();
		fShell = null;
	}
	// leave the display
	fRootElement = null;
}
	public void testClearSelection() {
		TestElement first= fRootElement.getFirstChild();
		StructuredSelection selection= new StructuredSelection(first);
		fViewer.setSelection(selection);
		fViewer.setSelection(new StructuredSelection());
		ISelection result= fViewer.getSelection();
		assertTrue(result.isEmpty());
	}
	public void testDeleteChild() {
		TestElement first= fRootElement.getFirstChild();
		TestElement first2= first.getFirstChild();
		first.deleteChild(first2);
		assertNull("first child is not visible", fViewer.testFindItem(first2));
	}
	public void testDeleteInput() {
		TestElement first= fRootElement.getFirstChild();
		TestElement firstfirst= first.getFirstChild();
		fViewer.setInput(first);
		fRootElement.deleteChild(first);
		assertNull("first child is not visible", fViewer.testFindItem(firstfirst));	
	}
	public void testDeleteSibling() {
		TestElement first= fRootElement.getFirstChild();
		assertNotNull("first child is visible", fViewer.testFindItem(first));		
		fRootElement.deleteChild(first);
		assertNull("first child is not visible", fViewer.testFindItem(first));
	}
	public void testFilter() {
		ViewerFilter filter= new TestLabelFilter(); 
		fViewer.addFilter(filter);
		assertTrue("filtered count", getItemCount() == 5);
		fViewer.removeFilter(filter);
		assertTrue("unfiltered count", getItemCount() == 10);

	}
	public void testInsertChild() {
		TestElement first= fRootElement.getFirstChild();
		TestElement newElement= first.addChild(TestModelChange.INSERT);
		assertNull("new sibling is not visible", fViewer.testFindItem(newElement));
	}
	public void testInsertSibling() {
		TestElement newElement= fRootElement.addChild(TestModelChange.INSERT);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
	}
	public void testInsertSiblingReveal() {
		TestElement newElement= fRootElement.addChild(TestModelChange.INSERT | TestModelChange.REVEAL);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
	}
	public void testInsertSiblings() {
		TestElement[] newElements = fRootElement.addChildren(TestModelChange.INSERT);
		for (int i = 0; i < newElements.length; ++i)
			assertNotNull("new siblings are visible", fViewer.testFindItem(newElements[i]));
	}
	public void testInsertSiblingSelectExpanded() {
		TestElement newElement= fRootElement.addChild(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
		assertSelectionEquals("new element is selected", newElement);
	}
	public void testInsertSiblingWithFilterFiltered() {
		fViewer.addFilter(new TestLabelFilter());
		TestElement newElement= new TestElement(fModel, fRootElement);
		newElement.setLabel("name-111");
		fRootElement.addChild(newElement, 
			new TestModelChange(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT, fRootElement, newElement)
		);
		assertNull("new sibling is not visible", fViewer.testFindItem(newElement));
		assertTrue(getItemCount() == 5);
	}
	public void testInsertSiblingWithFilterNotFiltered() {
		fViewer.addFilter(new TestLabelFilter());
		TestElement newElement= new TestElement(fModel, fRootElement);
		newElement.setLabel("name-222");
		fRootElement.addChild(newElement, 
			new TestModelChange(TestModelChange.INSERT | TestModelChange.REVEAL | TestModelChange.SELECT, fRootElement, newElement)
		);
		assertNotNull("new sibling is visible", fViewer.testFindItem(newElement));
		assertTrue(getItemCount() == 6);
	}
	public void testInsertSiblingWithSorter() {
		fViewer.setSorter(new TestLabelSorter());
		TestElement newElement= new TestElement(fModel, fRootElement);
		newElement.setLabel("name-9999");
		fRootElement.addChild(newElement, 
			new TestModelChange(TestModelChange.INSERT |TestModelChange.REVEAL | TestModelChange.SELECT, fRootElement, newElement)
		);
		String newLabel= newElement.toString();
		assertEquals("sorted first", newLabel, getItemText(0));
		assertSelectionEquals("new element is selected", newElement);
	}
	public void testLabelProvider() {
		// BUG: non-polymorphic behaviour
		// if (fViewer instanceof TableViewer || fViewer instanceof TableTreeViewer)
		// 	return;
		fViewer.setLabelProvider(new TestLabelProvider());
		TestElement first= fRootElement.getFirstChild();
		String newLabel= providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
	}
	public void testLabelProviderStateChange() {
		// BUG: non-polymorphic behaviour
		// if (fViewer instanceof TableViewer || fViewer instanceof TableTreeViewer)
		// 	return;
		TestLabelProvider provider= new TestLabelProvider();
		fViewer.setLabelProvider(provider);
		provider.setSuffix("added suffix");
		TestElement first= fRootElement.getFirstChild();
		String newLabel= providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
	}
	public void testRename() {
		TestElement first= fRootElement.getFirstChild();
		String newLabel= first.getLabel()+" changed";
		first.setLabel(newLabel);
		assertEquals("changed label", first.getID()+" "+newLabel, getItemText(0));
	}
	public void testRenameWithFilter() {
		fViewer.addFilter(new TestLabelFilter());
		TestElement first= fRootElement.getFirstChild();
		first.setLabel("name-1111"); // should disappear
		assertNull("changed sibling is not visible", fViewer.testFindItem(first));
		first.setLabel("name-2222"); // should reappear
		fViewer.refresh(); 
		assertNotNull("changed sibling is not visible", fViewer.testFindItem(first));
	}
	public void testRenameWithLabelProvider() {
		if (fViewer instanceof TableViewer || fViewer instanceof TableTreeViewer)
			return;
		fViewer.setLabelProvider(new TestLabelProvider());
		TestElement first= fRootElement.getFirstChild();
		first.setLabel("changed name");
		String newLabel= providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
	}
	public void testRenameWithSorter() {
		fViewer.setSorter(new TestLabelSorter());
		TestElement first= fRootElement.getFirstChild();
		first.setLabel("name-9999");
		String newElementLabel= first.toString();
		assertEquals("sorted first", newElementLabel, getItemText(0));
	}
	public void testSetInput() {
		TestElement first= fRootElement.getFirstChild();
		TestElement firstfirst= first.getFirstChild();

		fViewer.setInput(first);
		assertNotNull("first child is visible", fViewer.testFindItem(firstfirst));		
	}
	public void testSetSelection() {
		TestElement first= fRootElement.getFirstChild();
		StructuredSelection selection= new StructuredSelection(first);
		fViewer.setSelection(selection);
		IStructuredSelection result= (IStructuredSelection)fViewer.getSelection();
		assertTrue(result.size() == 1);
		assertTrue(result.getFirstElement() == first);
	}
	public void testSomeChildrenChanged() {
		bulkChange(new TestModelChange(TestModelChange.STRUCTURE_CHANGE, fRootElement));
	}
	public void testSorter() {
		TestElement first= fRootElement.getFirstChild();
		TestElement last= fRootElement.getLastChild();
		int size= fRootElement.getChildCount();
		
		String firstLabel= first.toString();
		String lastLabel= last.toString();
		assertEquals("unsorted", firstLabel, getItemText(0));
		assertEquals("unsorted", lastLabel, getItemText(size-1));
		fViewer.setSorter(new TestLabelSorter());
		assertEquals("reverse sorted", firstLabel, getItemText(size-1));
		assertEquals("reverse sorted", lastLabel, getItemText(0));
		
		fViewer.setSorter(null);
		assertEquals("unsorted", firstLabel, getItemText(0));
		assertEquals("unsorted", lastLabel, getItemText(size-1));		
	}
	public void testWorldChanged() {
		bulkChange(new TestModelChange(TestModelChange.STRUCTURE_CHANGE, null));
	}
}
