/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.themes;

import java.util.Iterator;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Tests the pushing down of current theme changes into JFace.
 * 
 * @since 3.0
 */
public class JFaceThemeTest extends ThemeTest {
    
    public JFaceThemeTest(String testName) {
        super(testName);
    }
    
    private void setAndTest(String themeId, IPropertyChangeListener listener) {
        JFaceResources.getFontRegistry().addListener(listener);
        JFaceResources.getColorRegistry().addListener(listener);
        fManager.setCurrentTheme(themeId);
        ITheme theme = fManager.getTheme(themeId);
        assertEquals(theme, fManager.getCurrentTheme()); 
        {
	        FontRegistry jfaceFonts = JFaceResources.getFontRegistry();
	        FontRegistry themeFonts = theme.getFontRegistry();
	        assertEquals(themeFonts.getKeySet(), jfaceFonts.getKeySet());	        
	        for (Iterator i = jfaceFonts.getKeySet().iterator(); i.hasNext();) {
	            String key = (String) i.next();
	            assertArrayEquals(themeFonts.getFontData(key), jfaceFonts.getFontData(key));
	        }
        }
        {
            ColorRegistry jfaceColors = JFaceResources.getColorRegistry();
            ColorRegistry themeColors = theme.getColorRegistry();
            assertEquals(themeColors.getKeySet(), jfaceColors.getKeySet());
            for (Iterator i = jfaceColors.getKeySet().iterator(); i.hasNext();) {
	            String key = (String) i.next();
	            assertEquals(themeColors.getRGB(key), jfaceColors.getRGB(key));
	        }            
        }
        JFaceResources.getFontRegistry().removeListener(listener);
        JFaceResources.getColorRegistry().removeListener(listener);
    }
    
    /**
     * TODO: detailed checking of the events
     */
    public void testPushdown() {
        ThemePropertyListener listener = new ThemePropertyListener();
        setAndTest(THEME1, listener);
        // ten changes, not the apparent 6 - remember the changes for the defaulted elements
        assertEquals(10, listener.getEvents().size());
        listener.getEvents().clear();
        setAndTest(IThemeManager.DEFAULT_THEME, listener);
        assertEquals(10, listener.getEvents().size());
    }
}
