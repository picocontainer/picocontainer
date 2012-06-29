package org.picocontainer.parameters;

import java.util.Map;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

import com.googlecode.jtype.Generic;

public interface CollectionSearchAlgorithm {
    
	Map<Object, ComponentAdapter<?>> getMatchingComponentAdapters(PicoContainer container, ComponentAdapter adapter,
                                 Class keyType, Generic<?> valueType);

}
