/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The suite of tests for multi-page editors.
 * 
 * @since 3.0
 */
public class MultiPageEditorTestSuite extends TestSuite {

    /**
     * Returns the suite. This is required to use the JUnit Launcher.
     * @return A new test suite; never <code>null</code>.;
     */
    public static Test suite() {
        return new MultiPageEditorTestSuite();
    }

    /**
     * Construct the test suite.
     */
    public MultiPageEditorTestSuite() {
        // Focus issues
        // addTest(new TestSuite(MultiPageKeyBindingTest.class));
    }
}
