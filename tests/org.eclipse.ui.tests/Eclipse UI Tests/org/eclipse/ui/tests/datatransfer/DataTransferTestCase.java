package org.eclipse.ui.tests.datatransfer;

import java.io.File;

import org.eclipse.ui.tests.harness.util.UITestCase;

public abstract class DataTransferTestCase extends UITestCase {

	public DataTransferTestCase(String testName) {
		super(testName);
	}
	/**
	 * Delete all files and folders in the given directory.
	 * This method does not delete the root folder of the
	 * given directory. 
	 * 
	 * @param file
	 */
	protected void deleteDirectory(File directory){
		if (directory.exists()){
	        File[] children = directory.listFiles();
	        if (children != null){
		        for (int i = 0; i < children.length; i++) {
		            if (children[i].isDirectory()){
		                deleteDirectory(children[i]);
		            }
		            else{
		            	// the first  two checks were added for bug 133541
		            	if (!children[i].exists())
		            		fail("Could not delete (file does not exist) " + children[i].getAbsolutePath());
		            	if (!children[i].canWrite())
		            		fail("Could not delete (cannot write to file) " + children[i].getAbsolutePath());
		                if (!children[i].delete())
		                	fail("Could not delete " + children[i].getAbsolutePath());
		            }
		        }
	        }
	        if (!directory.delete())
	        	fail("Could not delete " + directory.getAbsolutePath());
		}
	}
}