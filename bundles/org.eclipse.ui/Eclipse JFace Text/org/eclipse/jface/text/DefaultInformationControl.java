package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IInformationControl;


/**
 * Text based implementation of <code>IInformationControl</code>. 
 * Displays information in a styled text widget. Before displaying, the 
 * information is processed by an <code>IInformationPresenter</code>. 
 */
public class DefaultInformationControl implements IInformationControl {
	
	/**
	 * A information presenter determines the presentation
	 * of information displayed in the default information control. 
	 * The interface can be implemented by clients.
	 */
	public static interface IInformationPresenter {
		
		/**
		 * Updates the given presentation of the given information and
		 * thus manipulates the information to be displayed. Returns the 
		 * manipulated
		 *
		 * @param display the display of the information control
		 * @param information the information to be presented
		 * @param presentation the presentation to be updated
		 * @param maxWidth the maximal width in pixels
		 * 
		 * @return the manipulated information
		 */
		String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight);
	};
	
	
	/** The control's shell */
	private Shell fShell;
	/** The control's text widget */
	private StyledText fText;
	/** The information presenter */
	private IInformationPresenter fPresenter;
	/** A cached text presentation */
	private TextPresentation fPresentation= new TextPresentation();
	/** The control width constraint */
	private int fMaxWidth= -1;
	/** The control height constraint */
	private int fMaxHeight= -1;
	


	/**
	 * Creates a default information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created styled text widget.
	 * 
	 * @param parent the parent shell
	 * @param presenter the presenter to be used
	 * @param style the additional styles for the styled text widget
	 */
	public DefaultInformationControl(Shell parent, int style, IInformationPresenter presenter) {
		
		fShell= new Shell(parent, SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		fText= new StyledText(fShell, SWT.MULTI | SWT.READ_ONLY | style);
		
		Display display= fShell.getDisplay();
		
		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		
		fText.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fText.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		fText.addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e)  {
				if (e.character == 0x1B) // ESC
					fShell.dispose();
			}
			
			public void keyReleased(KeyEvent e) {}
		});
		
		fPresenter= presenter;
	}
	
	/**
	 * Creates a default information control with the given shell as parent.
	 * No information presenter is used to process the information
	 * to be displayed. No additional styles are applied to the styled text widget.
	 * 
	 * @param parent the parent shell
	 */
	public DefaultInformationControl(Shell parent) {
		this(parent, SWT.NONE, null);
	}
	
	/**
	 * Creates a default information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed.
	 * No additional styles are applied to the styled text widget.
	 * 
	 * @param parent the parent shell
	 * @param presenter the presenter to be used
	 */
	public DefaultInformationControl(Shell parent, IInformationPresenter presenter) {
		this(parent, SWT.NONE, presenter);
	}
	
	/*
	 * @see IInformationControl#setInformation(String)
	 */
	public void setInformation(String content) {
		if (fPresenter == null) {
			fText.setText(content);
		} else {
			fPresentation.clear();
			content= fPresenter.updatePresentation(fShell.getDisplay(), content, fPresentation, fMaxWidth, fMaxHeight);
			if (content != null) {
				fText.setText(content);
				TextPresentation.applyTextPresentation(fPresentation, fText);
			} else {
				fText.setText("");
			}
		}
	}
	
	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
			fShell.setVisible(visible);
	}
	
	/*
	 * @see IInformationControl#dispose()
	 */
	public void dispose() {
		if (fShell != null) {
			if (!fShell.isDisposed())
				fShell.dispose();
			fShell= null;
			fText= null;
		}
	}
	
	/*
	 * @see IInformationControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		fText.setSize(width + 3, height);
		fShell.setSize(width + 5, height + 2);
	}
	
	/*
	 * @see IInformationControl#setLocation(Point)
	 */
	public void setLocation(Point location) {
		fText.setLocation(1,1);
		fShell.setLocation(location);		
	}
	
	/*
	 * @see IInformationControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
	}
	
	/*
	 * @see IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		return fText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}
	
	/*
	 * @see IInformationControl#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}
	
	/*
	 * @see IInformationControl#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}
	
	/*
	 * @see IInformationControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		fText.setForeground(foreground);
	}
	
	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		fText.setBackground(background);
	}
	
	/*
	 * @see IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		return (fShell.isFocusControl() || fText.isFocusControl());
	}
	
	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		fText.setFocus();
	}
}

