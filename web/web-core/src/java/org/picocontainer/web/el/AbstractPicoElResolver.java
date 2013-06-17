/**
 * 
 */
package org.picocontainer.web.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.containers.JSRPicoContainer;

/**
 * Generic component resolution into a PicoContainer resolver.
 * @author Michael Rimov
 *
 */
abstract public class AbstractPicoElResolver extends ELResolver {
	

	/**
	 * 
	 */
	public AbstractPicoElResolver() {
	}

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		if (base == null) {
			Object result = getPicoContainer(context).getComponent(property);
			if (result != null) {
				context.setPropertyResolved(true);
				return result;				
			}
		}
		return null;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		if (base == null) {
			ComponentAdapter<?> result = getPicoContainer(context).getComponentAdapter(property);
			if (result != null) {
				context.setPropertyResolved(true);
				return result.getComponentImplementation();				
			}
		}
		
		return null;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (base == null) {
			throw new UnsupportedOperationException("Pico is read only");
		}
		
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (base == null) {
			return true;
		}
		return false;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return Object.class;
	}
	
	
	abstract protected PicoContainer getPicoContainer(ELContext context);

}
