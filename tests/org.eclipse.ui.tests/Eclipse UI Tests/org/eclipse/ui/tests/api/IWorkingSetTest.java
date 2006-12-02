/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.tests.harness.util.ArrayUtil;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.IA;

public class IWorkingSetTest extends UITestCase {
    final static String WORKING_SET_NAME_1 = "ws1";

    final static String WORKING_SET_NAME_2 = "ws2";

    IWorkspace fWorkspace;

    IWorkingSet fWorkingSet;

    public IWorkingSetTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        IWorkingSetManager workingSetManager = fWorkbench
                .getWorkingSetManager();

        fWorkspace = ResourcesPlugin.getWorkspace();
        fWorkingSet = workingSetManager.createWorkingSet(WORKING_SET_NAME_1,
                new IAdaptable[] { fWorkspace.getRoot() });
    }

    public void testGetElements() throws Throwable {
        assertEquals(fWorkspace.getRoot(), fWorkingSet.getElements()[0]);
    }

    public void testGetId() throws Throwable {
        assertEquals(null, fWorkingSet.getId());
        fWorkingSet.setId("bogusId");
        assertEquals("bogusId", fWorkingSet.getId());
        fWorkingSet.setId(null);
        assertEquals(null, fWorkingSet.getId());
    }

    public void testGetName() throws Throwable {
        assertEquals(WORKING_SET_NAME_1, fWorkingSet.getName());
    }

    public void testSetElements() throws Throwable {
        boolean exceptionThrown = false;

        try {
            fWorkingSet.setElements(null);
        } catch (RuntimeException exception) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        IProject p1 = FileUtil.createProject("TP1");
        IFile f1 = FileUtil.createFile("f1.txt", p1);
        IAdaptable[] elements = new IAdaptable[] { f1, p1 };
        fWorkingSet.setElements(elements);
        assertTrue(ArrayUtil.equals(elements, fWorkingSet.getElements()));

        fWorkingSet.setElements(new IAdaptable[] { f1 });
        assertEquals(f1, fWorkingSet.getElements()[0]);

        fWorkingSet.setElements(new IAdaptable[] {});
        assertEquals(0, fWorkingSet.getElements().length);
    }

    public void testSetId() throws Throwable {
        assertEquals(null, fWorkingSet.getId());
        fWorkingSet.setId("bogusId");
        assertEquals("bogusId", fWorkingSet.getId());
        fWorkingSet.setId(null);
        assertEquals(null, fWorkingSet.getId());
    }

    public void testSetName() throws Throwable {
        boolean exceptionThrown = false;

        try {
            fWorkingSet.setName(null);
        } catch (RuntimeException exception) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        fWorkingSet.setName(WORKING_SET_NAME_2);
        assertEquals(WORKING_SET_NAME_2, fWorkingSet.getName());

        fWorkingSet.setName("");
        assertEquals("", fWorkingSet.getName());

        fWorkingSet.setName(" ");
        assertEquals(" ", fWorkingSet.getName());
    }
    
    public void testIsEmpty() {
		fWorkingSet.setElements(new IAdaptable[] {});
		assertTrue(fWorkingSet.isEmpty());
		fWorkingSet.setElements(new IAdaptable[] { new IAdaptable() {
			public Object getAdapter(Class adapter) {
				return null;
			}
		} });
		assertFalse(fWorkingSet.isEmpty());
	}
    
    
    public void testApplicableTo_ResourceWorkingSet() {
		fWorkingSet.setId("org.eclipse.ui.resourceWorkingSetPage");
		assertEquals("org.eclipse.ui.resourceWorkingSetPage", fWorkingSet
				.getId());
		assertTrue(fWorkingSet.isApplicable(ResourcesPlugin.getWorkspace()
				.getRoot()));
    }
    
    public void testApplicableTo_DirectComparison() {

		fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		Foo myFoo = new Foo();
		assertTrue(fWorkingSet.isApplicable(myFoo));
    }
    
    public void testApplicableTo_Inheritance() {
    	fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
		Bar myBar = new Bar();
		
		assertTrue(fWorkingSet.isApplicable(myBar));
	}
    
    public void testApplicableTo_AdapterManager() {
    	fWorkingSet.setId("org.eclipse.ui.tests.api.MockWorkingSet");
    	IAImpl ia = new IAImpl();
    	assertTrue(fWorkingSet.isApplicable(ia));
    }
    
    public static class Foo implements IAdaptable {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			// TODO Auto-generated method stub
			return null;
		}	
    }
    
    public static class Bar extends Foo {
    	
    }
    
    public static class IAImpl implements IA, IAdaptable {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			// TODO Auto-generated method stub
			return null;
		}	
    }
}
