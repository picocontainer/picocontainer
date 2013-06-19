/**
 * 
 */
package org.picocontainer.web.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
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
	
    private final String NAME = "picoELResolver";
    private final String DISPLAYNAME = "PicoContainer ELResolver";
    private final String DESCRIPTION = "ELResolver To PicoContainer Bridge";
    private final boolean EXPERT = false;
    private final boolean HIDDEN = true;
    private final boolean PREFERRED = true;

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
	    if (base != null) return null;
	    ArrayList<FeatureDescriptor> list = new ArrayList<FeatureDescriptor>(14);
	    FeatureDescriptor descriptor = new FeatureDescriptor();
	    descriptor.setName(NAME);
	    descriptor.setDisplayName(DISPLAYNAME);
	    descriptor.setShortDescription(DESCRIPTION);
	    descriptor.setExpert(EXPERT);
	    descriptor.setHidden(HIDDEN);
	    descriptor.setPreferred(PREFERRED);
	    list.add(descriptor);
	    
	    return list.iterator();
	  }

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return Object.class;
	}
	
	
	abstract protected PicoContainer getPicoContainer(ELContext context);

}
