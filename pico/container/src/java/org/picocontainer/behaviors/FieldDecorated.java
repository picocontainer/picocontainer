package org.picocontainer.behaviors;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

@SuppressWarnings("serial")
public class FieldDecorated extends AbstractBehavior {
    private final Class<?> fieldClass;
    private final Decorator decorator;

    public FieldDecorated(ComponentAdapter delegate, Class<?> fieldClass, Decorator decorator) {
        super(delegate);
        this.fieldClass = fieldClass;
        this.decorator = decorator;
    }

    public Object getComponentInstance(final PicoContainer container, Type into)
            throws PicoCompositionException {
        Object instance = super.getComponentInstance(container, into);
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == fieldClass) {
                Object value = decorator.decorate(instance);
                field.setAccessible(true);
                try {
                    field.set(instance, value);
                } catch (IllegalAccessException e) {
                    throw new PicoCompositionException(e);
                }
            }
        }
        return instance;
    }


    public String getDescriptor() {
        return "FieldDecorated";
    }

    public interface Decorator {

        Object decorate(Object instance);


    }

}
