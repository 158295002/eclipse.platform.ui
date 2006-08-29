package org.eclipse.jface.viewers.snippets;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class OwnerDrawExample {

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display, SWT.CLOSE);
		shell.setSize(400, 400);
		shell.setLayout(new GridLayout());

		OwnerDrawExample example = new OwnerDrawExample();
		example.createPartControl(shell);

		shell.open();

		while (!shell.isDisposed()) {
			display.readAndDispatch();
		}
		display.dispose();
	}

	private static int COLUMN_COUNT = 3;

	class OwnerDrawColumn extends ViewerColumn {

		Control control;

		/**
		 * Create a new instance of the receiver with no label provider.
		 * 
		 * @param columnOwner
		 */
		public OwnerDrawColumn(TableColumn columnOwner) {
			super(columnOwner, null);
			control = columnOwner.getParent();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerColumn#refresh(org.eclipse.jface.viewers.ViewerCell)
		 */
		public void refresh(ViewerCell cell) {
			Rectangle cellBounds = cell.getBounds();
			control.redraw(cellBounds.x, cellBounds.y, cellBounds.width,
					cellBounds.height, true);
		}

	}

	class CountryEntry {

		String name;

		String cupYear;

		/**
		 * Create a new instance of the receiver.
		 * 
		 * @param countryName
		 * @param worldCupYear
		 */
		CountryEntry(String countryName, String worldCupYear) {
			name = countryName;
			cupYear = worldCupYear;
		}

		/**
		 * @param index
		 * @return
		 */
		public int getHeight(Event event) {
			switch (event.index) {
			case 0:
				return event.gc.textExtent(name).y;
			case 1:
				return 50;
			case 2:
				return event.gc.textExtent(cupYear).y;
			default:
				return 10;
			}
		}

		/**
		 * @param index
		 * @return
		 */
		public int getWidth(Event event) {

			switch (event.index) {
			case 0:
				return event.gc.textExtent(name).x + 5;

			case 1:
				return 50;

			case 2:
				return event.gc.textExtent(cupYear).x + 5;

			default:
				return 10;
			}
		}

		/**
		 * Draw the flag in bounds.
		 * 
		 * @param event
		 */
		protected void drawFlag(Event event) {
			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_BLUE));

			Rectangle bounds = event.getBounds();
			bounds.width += 100;
			event.gc.fillRectangle(bounds);
		}

		/**
		 * Draw the cup year
		 * 
		 * @param event
		 */
		private void drawCupYear(Event event) {
			event.gc.drawText(cupYear, event.x, event.y);

		}

		/**
		 * Draw the name of the receiver.
		 * 
		 * @param event
		 */
		protected void drawName(Event event) {

			event.gc.drawText(name, event.x, event.y);

		}



		/**
		 * @param event
		 */
		public void draw(Event event) {

			switch (event.index) {
			case 0:
				drawName(event);
				break;
			case 1:
				drawFlag(event);
				break;
			case 2:
				drawCupYear(event);
				break;

			default:
				break;
			}

		}
	}

	private class GermanyEntry extends CountryEntry {

		GermanyEntry() {
			super("Deutschland", "1990");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.tests.viewers.OwnerDrawExample.CountryEntry#drawFlag(org.eclipse.swt.widgets.Event)
		 */
		protected void drawFlag(Event event) {

			Rectangle bounds = event.getBounds();
			bounds.width += 100;
			int stripeHeight = bounds.height / 3;
			Rectangle stripe = new Rectangle(bounds.x, bounds.y, bounds.width,
					stripeHeight);

			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_BLACK));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_YELLOW));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(stripe);

		}

	}

	private class AustriaEntry extends CountryEntry {

		AustriaEntry() {
			super("�sterreich", "TBD");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.tests.viewers.OwnerDrawExample.CountryEntry#drawFlag(org.eclipse.swt.widgets.Event)
		 */
		protected void drawFlag(Event event) {

			Rectangle bounds = event.getBounds();
			bounds.width += 100;
			int stripeHeight = bounds.height / 3;
			Rectangle stripe = new Rectangle(bounds.x, bounds.y, bounds.width,
					stripeHeight);

			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_WHITE));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(stripe);

		}
	}

	private class EnglandEntry extends CountryEntry {
		EnglandEntry() {
			super("England", "1966");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.tests.viewers.OwnerDrawExample.CountryEntry#drawFlag(org.eclipse.swt.widgets.Event)
		 */
		protected void drawFlag(Event event) {

			Rectangle bounds = event.getBounds();
			bounds.width += 100;

			event.gc.setBackground(viewer.getControl().getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(new Rectangle(bounds.width / 2 + bounds.x
					- 5, bounds.y, 10, bounds.height));
			event.gc.fillRectangle(new Rectangle(bounds.x, bounds.height / 2
					+ bounds.y - 5, bounds.width, 10));

		}
	}

	private TableViewer viewer;

	private CountryEntry[] entries;

	public OwnerDrawExample() {
		entries = new CountryEntry[3];
		entries[0] = new AustriaEntry();
		entries[1] = new GermanyEntry();
		entries[2] = new EnglandEntry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent);

		viewer.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
			};

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return entries;
			};

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(org.eclipse.jface.viewers.Viewer viewer,
					Object oldInput, Object newInput) {
			}

		});
		createColumns();

		viewer.setInput(this);

		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH);

		viewer.getControl().setLayoutData(data);

		viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				CountryEntry country = (CountryEntry) item.getData();

				event.setBounds(measure(country, event));
			}
		});

		viewer.getTable().addListener(SWT.PaintItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				CountryEntry entry = (CountryEntry) item.getData();
				entry.draw(event);

			}
		});

		viewer.getTable().addListener(SWT.EraseItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {

				Rectangle bounds = event.getBounds();
				if ((event.detail & SWT.SELECTED) > 0) {

					Color oldForeground = event.gc.getForeground();
					Color oldBackground = event.gc.getBackground();

					event.gc.setBackground(viewer.getControl().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION));
					event.gc.setForeground(viewer.getControl().getDisplay()
							.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
					event.gc.fillRectangle(bounds);
					/* restore the old GC colors */
					event.gc.setForeground(oldForeground);
					event.gc.setBackground(oldBackground);
					/* ensure that default selection is not drawn */
					event.detail &= ~SWT.SELECTED;

				}

			}
		});

		viewer.setSelection(new StructuredSelection(entries[1]));
	}

	/**
	 * Return the size of the entry at CountryEntry.
	 * 
	 * @param entry
	 * @param index
	 * @return Rectangle
	 */
	protected Rectangle measure(CountryEntry entry, Event event) {
		return new Rectangle(0, 0, entry.getWidth(event), entry
				.getHeight(event));
	}

	/**
	 * Create the columns to be used in the tree.
	 */
	private void createColumns() {
		TableLayout layout = new TableLayout();
		viewer.getTable().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		for (int i = 0; i < COLUMN_COUNT; i++) {
			TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
			layout.addColumnData(new ColumnPixelData(100));
			tc.setText(getTitleFor(i));
			OwnerDrawColumn column = new OwnerDrawColumn(tc);
			column.setLabelProvider(new ViewerLabelProvider());
			viewer.setViewerColumn(i, column);
		}
		;
	}

	/**
	 * @param i
	 * @return
	 */
	private String getTitleFor(int i) {
		switch (i) {
		case 0:
			return "Name";
		case 1:
			return "Flag";
		case 2:
			return "World Cup Year";
		}
		return "Unknown";
	}

	public void setFocus() {

	}

}
