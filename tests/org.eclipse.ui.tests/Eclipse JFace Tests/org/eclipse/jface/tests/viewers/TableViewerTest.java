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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableViewerTest extends StructuredItemViewerTest {
    public static class TableTestLabelProvider extends TestLabelProvider
            implements ITableLabelProvider {
        public boolean fExtended = false;

        public String getText(Object element) {
            if (fExtended)
                return providedString((String) element);
            return element.toString();
        }

        public String getColumnText(Object element, int index) {
            if (fExtended)
                return providedString((TestElement) element);
            return element.toString();
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    public TableViewerTest(String name) {
        super(name);
    }

    /**
     * Creates the viewer used by this test, under the given parent widget.
     */
    protected StructuredViewer createViewer(Composite parent) {
        TableViewer viewer = createTableViewer(parent);
        viewer.setContentProvider(new TestModelContentProvider());
        viewer.setLabelProvider(new TableTestLabelProvider());
        viewer.getTable().setLinesVisible(true);

        TableLayout layout = new TableLayout();
        viewer.getTable().setLayout(layout);
        viewer.getTable().setHeaderVisible(true);
        String headers[] = { "column 1 header", "column 2 header" };

        ColumnLayoutData layouts[] = { new ColumnWeightData(100),
                new ColumnWeightData(100) };

        final TableColumn columns[] = new TableColumn[headers.length];

        for (int i = 0; i < headers.length; i++) {
            layout.addColumnData(layouts[i]);
            TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
            tc.setResizable(layouts[i].resizable);
            tc.setText(headers[i]);
            columns[i] = tc;
        }

        return viewer;
    }

    /**
     * Create the table viewer for the test
	 * @param parent
	 * @return
	 */
	protected TableViewer createTableViewer(Composite parent) {
		return new TableViewer(parent);
	}

	protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        TableItem ti = (TableItem) fViewer.testFindItem(first);
        Table table = ti.getParent();
        return table.getItemCount();
    }

    protected String getItemText(int at) {
        Table table = (Table) fViewer.getControl();
        return table.getItem(at).getText();
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TableViewerTest.class);
    }

    public void testLabelProvider() {

        TableViewer viewer = (TableViewer) fViewer;
        TableTestLabelProvider provider = (TableTestLabelProvider) viewer
                .getLabelProvider();

        provider.fExtended = true;
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
        fViewer.refresh();
        TestElement first = fRootElement.getFirstChild();
        String newLabel = providedString(first);
        assertEquals("rendered label", newLabel, getItemText(0));
        provider.fExtended = false;
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
    }

    public void testLabelProviderStateChange() {
        TableViewer tableviewer = (TableViewer) fViewer;
        TableTestLabelProvider provider = (TableTestLabelProvider) tableviewer
                .getLabelProvider();

        provider.fExtended = true;
        provider.setSuffix("added suffix");
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
        tableviewer.refresh();
        TestElement first = fRootElement.getFirstChild();
        String newLabel = providedString(first);
        assertEquals("rendered label", newLabel, getItemText(0));
        provider.fExtended = false;
        // BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
        fViewer.refresh();
    }
}