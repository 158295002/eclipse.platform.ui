/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Example implementation of the person service.
 * 
 * @since 3.4
 */
public class PersonService implements IPersonService, IDisposable {

	private Map people = new TreeMap();
	private IServiceLocator serviceLocator;
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);

	public PersonService(IServiceLocator locator) {
		serviceLocator = locator;
		serviceLocator.hasService(IHandlerService.class);
		fillModel();
	}

	private static final String[] datafill = { "Doe", "John", "Doe", "Jane", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Public", "John", "Public", "Jane" }; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$

	private void fillModel() {
		int i = 1114;
		for (int j = 0; j < datafill.length; j += 2) {
			Integer iid = new Integer(i++);
			Person p = new Person(iid.intValue(), datafill[j], datafill[j + 1]);
			people.put(iid, p);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.examples.contributions.model.IPersonService#addPersonChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPersonChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.examples.contributions.model.IPersonService#getPeople()
	 */
	public Collection getPeople() {
		return Collections.unmodifiableCollection(people.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.examples.contributions.model.IPersonService#getPerson(int)
	 */
	public Person getPerson(int id) {
		Person p = (Person) people.get(new Integer(id));
		if (p == null) {
			return null;
		}
		return p.copy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.examples.contributions.model.IPersonService#removePersonChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePersonChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.examples.contributions.model.IPersonService#updatePerson(org.eclipse.ui.examples.contributions.model.Person)
	 */
	public void updatePerson(Person person) {
		Assert.isNotNull(person);
		Person p = (Person) people.get(new Integer(person.getId()));
		if (p == null) {
			Assert.isNotNull(p, "Must update a real person"); //$NON-NLS-1$
		}
		if (person.equals(p)) {
			return;
		}
		Person oldVal = p.copy();
		p.setGivenname(person.getGivenname());
		p.setSurname(person.getSurname());
		firePersonChange(PROP_CHANGE, oldVal, person);
	}

	/**
	 * @param oldVal
	 * @param person
	 */
	private void firePersonChange(String property, Person oldVal, Person person) {
		if (listeners.isEmpty()) {
			return;
		}
		PropertyChangeEvent event = new PropertyChangeEvent(this, property,
				oldVal, person);
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			((IPropertyChangeListener) array[i]).propertyChange(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.examples.contributions.model.IPersonService#createPerson(int)
	 */
	public Person createPerson(int id) {
		Integer iid = new Integer(id);
		if (people.containsKey(iid)) {
			return null;
		}
		Person person = new Person(id, "surname", "givenname"); //$NON-NLS-1$//$NON-NLS-2$
		people.put(iid, person);
		Person copy = person.copy();
		firePersonChange(PROP_ADD, null, copy);
		return copy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		// we'd save stuff here, maybe, if we cared
		listeners.clear();
		serviceLocator = null;
	}
}