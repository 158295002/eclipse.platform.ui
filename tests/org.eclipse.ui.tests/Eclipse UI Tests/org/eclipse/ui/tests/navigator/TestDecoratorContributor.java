package org.eclipse.ui.tests.navigator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.misc.Assert;

public class TestDecoratorContributor implements ILabelDecorator {

	public static TestDecoratorContributor contributor;
	private Set listeners = new HashSet();
	public static String DECORATOR_SUFFIX = "_SUFFIX";

	public TestDecoratorContributor() {
		contributor = this;
	}

	/*
	 * @see ILabelDecorator#decorateText(String, Object)
	 */
	public String decorateText(String text, Object element) {
		//Check that the element is adapted to IResource
		Assert.isTrue(element instanceof IResource);
		return text + DECORATOR_SUFFIX;
	}
	/*
	 * @see ILabelDecorator#decorateImage(Image, Object)
	 */
	public Image decorateImage(Image image, Object element) {
		Assert.isTrue(element instanceof IResource);
		return image;
	}

	/*
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/*
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		contributor = null;
		listeners = new HashSet();
	}

	/*
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Refresh the listeners to update the decorators for 
	 * element.
	 */

	public void refreshListeners(Object element) {
		Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, element);
			((ILabelProviderListener) iterator.next()).labelProviderChanged(event);
		}
	}

}