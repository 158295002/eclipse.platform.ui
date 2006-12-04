/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.VisibilityAssistant;
import org.eclipse.ui.internal.navigator.VisibilityAssistant.VisibilityListener;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorContentDescriptorManager {

	private static final NavigatorContentDescriptorManager INSTANCE = new NavigatorContentDescriptorManager();
 
	private final Map firstClassDescriptorsMap = new HashMap();

	private final Map allDescriptors = new HashMap();

	private class EvaluationCache implements VisibilityListener {

		private final Map evaluations/*<Object, NavigatorContentDescriptor[]>*/ = new HashMap();
		private final Map evaluationsWithOverrides/*<Object, NavigatorContentDescriptor[]>*/ = new HashMap();

		EvaluationCache(VisibilityAssistant anAssistant) {
			anAssistant.addListener(this);
		}

		protected final NavigatorContentDescriptor[] getDescriptors(Object anElement) {
			return getDescriptors(anElement, true);
		}

		protected final void setDescriptors(Object anElement, NavigatorContentDescriptor[] theDescriptors) {
			setDescriptors(anElement, theDescriptors, true);		
		}
		
		protected final NavigatorContentDescriptor[] getDescriptors(Object anElement, boolean toComputeOverrides) {
			
			if(anElement == null)
				return null;
			
			NavigatorContentDescriptor[] cachedDescriptors = null;
			if(toComputeOverrides) {
				SoftReference cache = (SoftReference) evaluations.get(anElement);
				if( cache != null && (cachedDescriptors = (NavigatorContentDescriptor[]) cache.get()) == null) 
					evaluations.remove(anElement);
				return cachedDescriptors;
			}
			SoftReference cache = (SoftReference) evaluationsWithOverrides.get(anElement);
			if( cache != null && (cachedDescriptors = (NavigatorContentDescriptor[]) cache.get()) == null) 
				evaluationsWithOverrides.remove(anElement);
			return cachedDescriptors;
			 
		}

		protected final void setDescriptors(Object anElement, NavigatorContentDescriptor[] theDescriptors, boolean toComputeOverrides) {
			if(anElement != null) {
				if(toComputeOverrides)
					evaluations.put(new EvalutationReference(anElement), new SoftReference(theDescriptors));
				else 
					evaluationsWithOverrides.put(new EvalutationReference(anElement), new SoftReference(theDescriptors));
			}
		}
	  
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.navigator.VisibilityAssistant.VisibilityListener#onVisibilityOrActivationChange()
		 */
		public void onVisibilityOrActivationChange() {
			evaluations.clear();
			evaluationsWithOverrides.clear();
		}
	}

	/* Map of (VisibilityAssistant, EvaluationCache)-pairs */
	private final Map cachedTriggerPointEvaluations = new WeakHashMap();

	/* Map of (VisibilityAssistant, EvaluationCache)-pairs */
	private final Map cachedPossibleChildrenEvaluations = new WeakHashMap();

	private ImageRegistry imageRegistry;

	private final Set overridingDescriptors = new HashSet();

	private final Set saveablesProviderDescriptors = new HashSet();
	
	private final Set firstClassDescriptorsSet = new HashSet();

	/**
	 * @return the singleton instance of the manager
	 */
	public static NavigatorContentDescriptorManager getInstance() {
		return INSTANCE;
	}

	private NavigatorContentDescriptorManager() {
		new NavigatorContentDescriptorRegistry().readRegistry();
	}

	/**
	 * 
	 * @return Returns all content descriptor(s).
	 */
	public NavigatorContentDescriptor[] getAllContentDescriptors() {
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[allDescriptors
				.size()];
		finalDescriptors = (NavigatorContentDescriptor[]) allDescriptors.values().toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, ExtensionPriorityComparator.INSTANCE);
		return finalDescriptors;
	}

	/**
	 * 
	 * @return Returns all content descriptors that provide saveables.
	 */
	public NavigatorContentDescriptor[] getContentDescriptorsWithSaveables() {
		NavigatorContentDescriptor[] finalDescriptors = new NavigatorContentDescriptor[saveablesProviderDescriptors
		                                                                               .size()];
		saveablesProviderDescriptors.toArray(finalDescriptors);
		Arrays.sort(finalDescriptors, ExtensionPriorityComparator.INSTANCE);
		return finalDescriptors;
	}
	
	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * 
	 * @param aVisibilityAssistant
	 *            The relevant viewer assistant; used to filter out unbound
	 *            content descriptors.
	 * @return the best content descriptor for the given element.
	 */
	public Set findDescriptorsForTriggerPoint(Object anElement,
			VisibilityAssistant aVisibilityAssistant) {
		EvaluationCache cache = getEvaluationCache(
				cachedTriggerPointEvaluations, aVisibilityAssistant);

		Set descriptors = new TreeSet(ExtensionPriorityComparator.INSTANCE);
		NavigatorContentDescriptor[] cachedDescriptors = null;
		if ( (cachedDescriptors = cache.getDescriptors(anElement)) != null) {
			descriptors.addAll(Arrays.asList(cachedDescriptors));
		} 

	 	/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = firstClassDescriptorsMap.values()
				.iterator(); contentDescriptorsItr.hasNext();) {
			NavigatorContentDescriptor descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			if (aVisibilityAssistant.isActive(descriptor)
					&& aVisibilityAssistant.isVisible(descriptor)
					&& descriptor.isTriggerPoint(anElement)) {
				descriptors.add(descriptor);
			}
		}

		cache.setDescriptors(anElement, (NavigatorContentDescriptor[]) descriptors.toArray(new NavigatorContentDescriptor[descriptors.size()]));

		return descriptors;
	}

	private EvaluationCache getEvaluationCache(Map anEvaluationMap,
			VisibilityAssistant aVisibilityAssistant) {
		EvaluationCache c = (EvaluationCache) anEvaluationMap
				.get(aVisibilityAssistant);
		if (c == null) {
			anEvaluationMap.put(aVisibilityAssistant, c = new EvaluationCache(
					aVisibilityAssistant));
		}
		return c;

	}
	
	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * 
	 * @param aVisibilityAssistant
	 *            The relevant viewer assistant; used to filter out unbound
	 *            content descriptors.
	 * @return the best content descriptor for the given element.
	 */
	public Set findDescriptorsForPossibleChild(Object anElement,
			VisibilityAssistant aVisibilityAssistant) {
		return findDescriptorsForPossibleChild(anElement, aVisibilityAssistant, true);
	}

	/**
	 * 
	 * Returns all content descriptor(s) which enable for the given element.
	 * 
	 * @param anElement
	 *            the element to return the best content descriptor for
	 * 
	 * @param aVisibilityAssistant
	 *            The relevant viewer assistant; used to filter out unbound
	 *            content descriptors.
	 * @return the best content descriptor for the given element.
	 */
	public Set findDescriptorsForPossibleChild(Object anElement,
			VisibilityAssistant aVisibilityAssistant, boolean toComputeOverrides) {

		EvaluationCache cache = getEvaluationCache(
				cachedPossibleChildrenEvaluations, aVisibilityAssistant);
		
		Set descriptors = new TreeSet(ExtensionPriorityComparator.INSTANCE);
		NavigatorContentDescriptor[] cachedDescriptors = null;
		if ( (cachedDescriptors = cache.getDescriptors(anElement, toComputeOverrides)) != null) {
			descriptors.addAll(Arrays.asList(cachedDescriptors));
		} 
		
		if(toComputeOverrides) {
			addDescriptorsForPossibleChild(anElement, firstClassDescriptorsSet,
				aVisibilityAssistant, descriptors);
		} else {

			NavigatorContentDescriptor descriptor;
			/* Find other ContentProviders which enable for this object */
			for (Iterator contentDescriptorsItr = allDescriptors.values().iterator(); contentDescriptorsItr
					.hasNext();) {
				descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
						.next();

				boolean isApplicable = aVisibilityAssistant.isActive(descriptor)
						&& aVisibilityAssistant.isVisible(descriptor)
						&& descriptor.isPossibleChild(anElement);

				 if (isApplicable) {
					descriptors.add(descriptor);
				}

			}
		} 
		cache.setDescriptors(anElement, (NavigatorContentDescriptor[]) descriptors.toArray(new NavigatorContentDescriptor[descriptors.size()]), toComputeOverrides);

		return descriptors;
	}

	private boolean addDescriptorsForPossibleChild(Object anElement,
			Set theChildDescriptors, VisibilityAssistant aVisibilityAssistant,
			Set theFoundDescriptors) {
		int initialSize = theFoundDescriptors.size();

		NavigatorContentDescriptor descriptor;
		/* Find other ContentProviders which enable for this object */
		for (Iterator contentDescriptorsItr = theChildDescriptors.iterator(); contentDescriptorsItr
				.hasNext();) {
			descriptor = (NavigatorContentDescriptor) contentDescriptorsItr
					.next();

			boolean isApplicable = aVisibilityAssistant.isActive(descriptor)
					&& aVisibilityAssistant.isVisible(descriptor)
					&& descriptor.isPossibleChild(anElement);

			if (descriptor.hasOverridingExtensions()) {

				boolean isOverridden = addDescriptorsForPossibleChild(
						anElement, descriptor.getOverriddingExtensions(),
						aVisibilityAssistant, theFoundDescriptors);

				if (!isOverridden && isApplicable) {
					theFoundDescriptors.add(descriptor);
				}

			} else if (isApplicable) {
				theFoundDescriptors.add(descriptor);
			}

		}
		return initialSize < theFoundDescriptors.size();

	}

	/**
	 * Returns the navigator content descriptor with the given id.
	 * 
	 * @param id
	 *            The id of the content descriptor that should be returned
	 * @return The content descriptor of the given id
	 */
	public NavigatorContentDescriptor getContentDescriptor(String id) {
		return (NavigatorContentDescriptor) allDescriptors.get(id);
	}

	/**
	 * 
	 * @param descriptorId
	 *            The unique id of a particular descriptor
	 * @return The name (value of the 'name' attribute) of the given descriptor
	 */
	public String getText(String descriptorId) {
		INavigatorContentDescriptor descriptor = getContentDescriptor(descriptorId);
		if (descriptor != null) {
			return descriptor.getName();
		}
		return descriptorId;
	}

	/**
	 * 
	 * @param descriptorId
	 *            The unique id of a particular descriptor
	 * @return The image (corresponding to the value of the 'icon' attribute) of
	 *         the given descriptor
	 */
	public Image getImage(String descriptorId) {
		return retrieveAndStoreImage(descriptorId);
	}

	protected Image retrieveAndStoreImage(String descriptorId) {
		NavigatorContentDescriptor contentDescriptor = getContentDescriptor(descriptorId);

		Image image = null;
		if (contentDescriptor != null) {
			String icon = contentDescriptor.getIcon();
			if (icon != null) {
				image = getImageRegistry().get(icon);
				if (image == null || image.isDisposed()) {
					ImageDescriptor imageDescriptor = AbstractUIPlugin
							.imageDescriptorFromPlugin(contentDescriptor
									.getContribution().getPluginId(), icon);
					if (imageDescriptor != null) {
						image = imageDescriptor.createImage();
						if (image != null) {
							getImageRegistry().put(icon, image);
						}
					}
				}
			}
		}
		return image;
	}

	/**
	 * @param desc
	 */
	private void addNavigatorContentDescriptor(NavigatorContentDescriptor desc) {
		if (desc == null) {
			return;
		}
		synchronized (firstClassDescriptorsMap) {
			if (firstClassDescriptorsMap.containsKey(desc.getId())) {
				NavigatorPlugin
						.logError(
								0,
								"An extension already exists with id \"" + desc.getId() + "\".", null); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				if (desc.getSuppressedExtensionId() == null) {
					firstClassDescriptorsMap.put(desc.getId(), desc);
					firstClassDescriptorsSet.add(desc);
				} else {
					overridingDescriptors.add(desc);
				}
				allDescriptors.put(desc.getId(), desc);
				if (desc.hasSaveablesProvider()) {
					saveablesProviderDescriptors.add(desc);
				}
			}
		}
	}

	/**
	 * 
	 */
	private void computeOverrides() {
		if (overridingDescriptors.size() > 0) {
			NavigatorContentDescriptor descriptor;
			NavigatorContentDescriptor overriddenDescriptor;
			for (Iterator overridingIterator = overridingDescriptors.iterator(); overridingIterator
					.hasNext();) {
				descriptor = (NavigatorContentDescriptor) overridingIterator
						.next();
				overriddenDescriptor = (NavigatorContentDescriptor) allDescriptors
						.get(descriptor.getSuppressedExtensionId());
				if (overriddenDescriptor != null) {

					/*
					 * add the descriptor as an overriding extension for its
					 * suppressed extension
					 */
					overriddenDescriptor.getOverriddingExtensions().add(
							descriptor);
					descriptor.setOverriddenDescriptor(overriddenDescriptor);
					/*
					 * the always policy implies this is also a top-level
					 * extension
					 */
					if (descriptor.getOverridePolicy() == OverridePolicy.InvokeAlwaysRegardlessOfSuppressedExt) {
						firstClassDescriptorsMap.put(descriptor.getId(),
								descriptor);
						firstClassDescriptorsSet.add(descriptor);
					}

				} else {
					NavigatorPlugin.logError(0,
							"Invalid suppressedExtensionId (\"" //$NON-NLS-1$
									+ descriptor.getSuppressedExtensionId()
									+ "\" specified from " //$NON-NLS-1$
									+ descriptor.getContribution()
											.getPluginId()
									+ ". No extension with matching id found.", //$NON-NLS-1$
							null);
				}
			}
		}
	}
 
	private ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	private class NavigatorContentDescriptorRegistry extends
			NavigatorContentRegistryReader {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.navigator.extensions.RegistryReader#readRegistry()
		 */
		public void readRegistry() {
			super.readRegistry();
			computeOverrides();
		}

		protected boolean readElement(IConfigurationElement anElement) {
			if (TAG_NAVIGATOR_CONTENT.equals(anElement.getName())) {
				try {
					addNavigatorContentDescriptor(new NavigatorContentDescriptor(
							anElement));

				} catch (WorkbenchException e) {
					// log an error since its not safe to open a dialog here
					NavigatorPlugin.log(e.getStatus());
				}
			}
			return super.readElement(anElement);
		}
	}

}
