package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.core.runtime.*;
import java.util.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The default implementation of the interface <code>ICategory</code>.
 * 
 * @see ICategory
 */
public class Category implements ICategory {
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_PARENT = "parentCategory"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	
	private String id;
	private String name;
	private String[] parentPath;
	private String unparsedPath;
	private ArrayList elements;
	
	/**
	 * Creates an instance of <code>Category</code> with
	 * an ID and label.
	 * 
	 * @param id the unique identifier for the category
	 * @param label the presentation label for this category
	 */
	public Category(String id, String label) {
		this.id = id;
		this.name = label;
	}
	
	/**
	 * Creates an instance of <code>Category</code> using the
	 * information from the specified configuration element.
	 * 
	 * @param configElement the <code>IConfigurationElement<code> containing
	 * 		the ID, label, and optional parent category path.
	 * @throws a <code>WorkbenchException</code> if the ID or label is <code>null</code
	 */
	public Category(IConfigurationElement configElement) throws WorkbenchException {
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		unparsedPath = configElement.getAttribute(ATT_PARENT);
		
		if (id == null || name == null)
			throw new WorkbenchException("Invalid category: " + id); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public void addElement(Object element) {
		if (elements == null)
			elements = new ArrayList(5);
		elements.add(element);
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String getLabel() {
		return name;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String[] getParentPath() {
		if (unparsedPath != null) {
			StringTokenizer stok = new StringTokenizer(unparsedPath, "/"); //$NON-NLS-1$
			parentPath = new String[stok.countTokens()];
			for (int i = 0; stok.hasMoreTokens(); i++) {
				parentPath[i] = stok.nextToken();
			}
			unparsedPath = null;
		}
		
		return parentPath;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public String getRootPath() {
		String[] path = getParentPath();
		if (path != null && path.length > 0)
			return path[0];
		else
			return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on ICategory.
	 */
	public ArrayList getElements() {
		return elements;
	}
}