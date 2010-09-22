package net.sf.sdedit.ui.components.configuration;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.sdedit.util.DocUtil;
import net.sf.sdedit.util.DocUtil.XMLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A <tt>Bean</tt> provides a single instance of a &quot;data object&quot;
 * that implements the <tt>T</tt> interface which should only define get-,
 * set-, and is-methods like a Java bean. The instance is returned by
 * {@linkplain #getDataObject()}. For all manipulations of the data object's
 * state (invocations of set-methods) the <tt>Bean</tt> immediately sends
 * notifications to all interested <tt>PropertyChangeListener</tt>s. The
 * state of the data object can be loaded and stored, using XML documents (see
 * {@linkplain #load(Document, String)},
 * {@linkplain #store(Document, String, String)}). The values of the data
 * object can also be accessed by passing their corresponding properties as
 * arguments (see {@linkplain #setValue(PropertyDescriptor, Object)},
 * {@linkplain #getValue(String)}).
 * <p>
 * The values returned by the data object managed by a <tt>Bean</tt> are
 * always, provided the bean has been set up/loaded properly, not null (this can
 * be enforced by the <tt>permitNullValues</tt> property set to false), and
 * legal, i. e. string properties for which there is a set of alternative values
 * are always assigned to a legal value.
 * 
 * 
 * @author Markus Strauch
 * 
 * @param <T>
 *            the interface type of the data object
 */
public class Bean<T extends DataObject> implements InvocationHandler {

	private Set<PropertyChangeListener> listeners;

	private SortedMap<String, PropertyDescriptor> properties;

	private SortedMap<String, String> order;

	// The state of the artificial object accessed by the T proxy
	// (see getDataObject())
	private HashMap<String, Object> values;

	private Class<T> dataClass;

	private T dataObject;

	private StringSelectionProvider<T> ssp;

	private boolean permitNullValues;

	private Map<String, Set<String>> stringSets;

	private Map<String, String> methodToPropertyNameMap;

	private Pattern pattern = Pattern.compile("get|set|is");

	/**
	 * Creates a new bean that provides a single data object. It is not
	 * permitted to set <tt>null</tt> values for this data object, as long as
	 * {@linkplain #setPermitNullValues(boolean)} is not called.
	 * 
	 * @param dataClass
	 *            the interface type of the data object
	 * @param ssp
	 *            a <tt>StringSelectionProvider</tt> that provides an array of
	 *            strings for methods of the data object which are annotated
	 *            {@linkplain Adjustable#stringSelectionProvided()}
	 */
	@SuppressWarnings("unchecked")
	public Bean(Class<T> dataClass, StringSelectionProvider ssp) {
		listeners = new LinkedHashSet<PropertyChangeListener>();
		properties = new TreeMap<String, PropertyDescriptor>();
		order = new TreeMap<String, String>();
		values = new HashMap<String, Object>();
		this.ssp = ssp;
		this.dataClass = dataClass;
		stringSets = new HashMap<String, Set<String>>();
		init();
		dataObject = (T) Proxy.newProxyInstance(dataClass.getClassLoader(),
				new Class[] { dataClass }, this);
		permitNullValues = false;
		methodToPropertyNameMap = new HashMap<String, String>();
	}

	private static String norm(String property) {
		return Character.toUpperCase(property.charAt(0))
				+ property.substring(1);
	}

	private void init() {
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(dataClass);
			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; i++) {
				PropertyDescriptor property = propertyDescriptors[i];
				if (property.getWriteMethod() != null
						&& property.getWriteMethod().isAnnotationPresent(
								Adjustable.class)) {
					String key = property.getWriteMethod().getAnnotation(
							Adjustable.class).key();
					if (key.equals("")) {
						key = norm(property.getName());
					}
					order.put(key, norm(property.getName()));
					properties.put(norm(property.getName()), property);
					
					if (getValue(property) == null) {
						// may not be null when called in the process of deserialization
						// (see readObject)
						setValue(property,NullValueProvider.getNullValue(property.getPropertyType()));
					}
					
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IllegalStateException(
					"FATAL: data class introspection was not successful");
		}
	}

	/**
	 * Returns the synthetic data object implementing the data interface
	 * belonging to this {@linkplain Bean}.
	 * 
	 * @return the synthetic data object implementing the data interface
	 *         belonging to this {@linkplain Bean}
	 */
	public T getDataObject() {
		return dataObject;
	}

	/**
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 *      java.lang.reflect.Method, java.lang.Object[])
	 */
	public final Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		String name = method.getName();
		
		if ("toString".equals(name) && method.getParameterTypes().length == 0) {
			return values.toString();
		}
		
		String property = methodToPropertyNameMap.get(name);
		
		if (property == null) {
			Matcher matcher = pattern.matcher(name);
			property = matcher.replaceFirst("");
			methodToPropertyNameMap.put(name, property);
		}
		
		
//		Class cls = null;
//		try {
//			cls = Class.forName("net.sf.sdedit.config.Configuration");
//		} catch (Exception ignored) {
//			
//		}
//		if (dataClass == cls) {
//			Integer used = usage.get(property);
//			if (used == null) {
//				usage.put(property, 1);
//			} else {
//				usage.put(property, used + 1);
//			}
//			System.out.println(usage);
//		}

		if (name.charAt(0) == 's') {
			// set-method
			setValue(properties.get(property), args[0]);
			return null;
		}
		return getValue(property);
	}

	/**
	 * Returns the properties of this Bean that are annotated with an
	 * {@linkplain Adjustable} annotation.
	 * 
	 * @return the properties of this Bean that are annotated with an
	 *         {@linkplain Adjustable} annotation
	 */
	public Collection<PropertyDescriptor> getProperties() {
		List<PropertyDescriptor> list = new LinkedList<PropertyDescriptor>();
		for (String property : order.values()) {
			list.add(properties.get(property));
		}
		return list;
	}

	/**
	 * Returns the <tt>PropertyDescriptor</tt> for the property with the given
	 * name.
	 * 
	 * @param name
	 *            the name of a property
	 * @return the corresponding <tt>PropertyDescriptor</tt> or <tt>null</tt>
	 *         if there is no property with the name
	 */
	public PropertyDescriptor getProperty(String name) {
		return properties.get(norm(name));
	}

	/**
	 * Adds a listener that is notified when a property is modified via
	 * {@linkplain #setValue(PropertyDescriptor, Object)}.
	 * 
	 * @param listener
	 *            a listener that is notified when a property is modified via
	 *            {@linkplain #setValue(PropertyDescriptor, Object)}
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a property change listener.
	 * 
	 * @param listener
	 *            the listener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Changes this bean's properties' values such that they are equal to the
	 * given bean's properties' values.
	 * 
	 * @param bean
	 *            another bean
	 */
	public void takeValuesFrom(Bean<T> bean) {
		for (PropertyDescriptor property : getProperties()) {
			setValue(property, bean.getValue(property.getName()));
		}
	}

	/**
	 * Returns a shallow copy of this bean.
	 * 
	 * @return a shallow copy of this bean
	 */
	public Bean<T> copy() {
		Bean<T> copy = new Bean<T>(dataClass, ssp);
		copy.takeValuesFrom(this);
		return copy;
	}

	/**
	 * Returns the current value of the given property, represented by its name.
	 * 
	 * @param property
	 *            the name of a property
	 * @return the current value of the property
	 */
	public final Object getValue(String property) {
		return values.get(norm(property));
	}
	
	public final Object getValue (PropertyDescriptor pd) {
		return getValue(pd.getName());
	}

	/**
	 * Changes this bean's properties' values such that they reflect the values
	 * found in the given document. It remains unchanged if the document does
	 * not contain a subtree corresponding to <tt>pathToElement</tt>.
	 * 
	 * @param document
	 *            a document
	 * @param pathToElement
	 *            XPath to the subtree where the properties' values are
	 *            described
	 * @throws XMLException
	 */
	public void load(Document document, String pathToElement)
			throws XMLException {
		Element elem = (Element) DocUtil.evalXPathAsNode(document,
				pathToElement);
		if (elem != null) {
			BeanConverter converter = new BeanConverter(this, document);
			converter.setValues(elem);
		}
	}

	/**
	 * Stores all properties' current values in a newly created subtree of the
	 * given document.
	 * 
	 * @param document
	 *            the document
	 * @param pathToParent
	 *            XPath to the parent of the root of the subtree
	 * @param elementName
	 *            the name of the root of the subtree
	 * @throws XMLException
	 */
	public void store(Document document, String pathToParent, String elementName)
			throws XMLException {
		Element parent = (Element) DocUtil.evalXPathAsNode(document,
				pathToParent);
		BeanConverter converter = new BeanConverter(this, document);
		Element elem = converter.createElement(elementName);
		parent.appendChild(elem);
	}

	/**
	 * Sets a new value for a property and informs all
	 * <tt>PropertyChangeListener</tt>s about that. If the
	 * <tt>admitNullValues</tt> property is false, a new value of
	 * <tt>null</tt> will be ignored and not set. Furthermore, it is not
	 * permitted to set a string value for a property that has a set of
	 * alternative values, if none of these matches. The illegal value will be
	 * silently ignored.
	 * 
	 * @param property
	 *            the descriptor of the property
	 * @param value
	 *            the new value of the property
	 */
	public final void setValue(PropertyDescriptor property, Object value) {
		if (value == null && !permitNullValues) {
			return;
		}
		if (property.getPropertyType() == String.class) {
			Set<String> choices = getStringsForProperty(property);
			if (!choices.isEmpty() && !choices.contains(value)) {
				return;
			}
		}
		String propertyName = norm(property.getName());
		Object oldValue = values.get(propertyName);
		values.put(propertyName, value);
		firePropertyChanged(property, value, oldValue);
	}

	/**
	 * Sends a notification about the change of a property provided both values
	 * are not equal (with respect to the result of <tt>equals(Object)</tt>.
	 * 
	 * @param property
	 *            the descriptor of the property whose value has changed
	 * @param newValue
	 *            the new value of the property
	 * @param oldValue
	 *            the old value of the property
	 */
	private void firePropertyChanged(PropertyDescriptor property,
			Object newValue, Object oldValue) {
		if (newValue == null && oldValue == null) {
			return;
		}
		if (newValue == null || oldValue == null || !newValue.equals(oldValue)) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, property
					.getName(), oldValue, newValue);
			for (PropertyChangeListener listener : listeners) {
				listener.propertyChange(event);
			}
		}
	}

	/**
	 * Returns true if and only if <tt>o</tt> is a reference to a bean with
	 * the same properties that have the same values as this bean's properties.
	 * 
	 * @return true if and only if <tt>o</tt> is a reference to a bean with
	 *         the same properties that have the same values as this bean's
	 *         properties
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		Bean<? extends DataObject> bean = (Bean<? extends DataObject>) o;
		for (PropertyDescriptor property : getProperties()) {
			Object myVal = getValue(property.getName());
			Object yourVal = bean.getValue(property.getName());
			if (myVal == null && yourVal == null) {
				// check next property if both are null
				continue;
			}
			if (myVal == null || yourVal == null) {
				// if one is null, the other is not, so return false
				return false;
			}
			if (!myVal.equals(yourVal)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		StringBuffer code = new StringBuffer();
		for (PropertyDescriptor property : getProperties()) {
			Object val = getValue(property.getName());
			code.append(val);
		}
		return code.hashCode();
	}

	/**
	 * Returns a set of strings representing all alternative values for the
	 * given String property. If there is no alternative, i. e. all values are
	 * possible, the set is empty.
	 * 
	 * @param property
	 *            a String property
	 * @return an array of strings representing all alternative values for the
	 *         given String property
	 */
	public Set<String> getStringsForProperty(PropertyDescriptor property) {
		String propName = norm(property.getName());
		Set<String> strings = stringSets.get(propName);
		if (strings == null) {
			strings = new LinkedHashSet<String>();
			Adjustable adj = property.getWriteMethod().getAnnotation(
					Adjustable.class);
			String[] choices = adj.choices();
			if (choices.length == 0 && adj.stringSelectionProvided()) {
				choices = ssp.getStringSelection(property.getName());
			}
			for (String choice : choices) {
				strings.add(choice);
			}
			stringSets.put(norm(property.getName()), strings);
		}
		return strings;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (PropertyDescriptor property : getProperties()) {
			buffer.append(property.getName() + "=");
			buffer.append(getValue(property.getName()));
			buffer.append("\n");
		}
		return buffer.toString();
	}

	/**
	 * Returns a flag denoting if <tt>null</tt> values can be used as
	 * parameters of the data object's set methods.
	 * 
	 * @return a flag denoting if <tt>null</tt> values can be used as
	 *         parameters of the data object's set methods
	 */
	public boolean isPermitNullValues() {
		return permitNullValues;
	}

	/**
	 * Sets a flag denoting if <tt>null</tt> values can be used as parameters
	 * of the data object's set methods.
	 * 
	 * @param permitNullValues
	 *            a flag denoting if <tt>null</tt> values can be used as
	 *            parameters of the data object's set methods
	 */
	public void setPermitNullValues(boolean permitNullValues) {
		this.permitNullValues = permitNullValues;
	}

}
