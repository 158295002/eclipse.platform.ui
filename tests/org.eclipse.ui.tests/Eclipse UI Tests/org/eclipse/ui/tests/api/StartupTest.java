/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.util.UITestCase;

public class StartupTest extends UITestCase {

	/** 
	 * Construct an instance.
	 */
	public StartupTest(String arg) {
		super(arg);
	}
	
	public void testStartup() {
		assertTrue("Startup - explicit", StartupClass.wasEarlyStartupCalled());
		assertTrue("Startup - implicit", TestPlugin.wasEarlyStartupCalled());
	}
	
	protected void doTearDown() throws Exception {
		super.doTearDown();
		// NOTE:  tearDown will run after each test.  Therefore, we
		// only want one test in this suite (or the values set when
		// this plugin started up will be lost).
		StartupClass.clearEarlyStartup();
		TestPlugin.clearEarlyStartup();
	}
}