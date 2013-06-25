package com.picocontainer.parameters;

import java.util.Map;


import com.googlecode.jtype.Generic;
import com.picocontainer.ComponentAdapter;
import com.picocontainer.PicoContainer;

public interface CollectionSearchAlgorithm {

	Map<Object, ComponentAdapter<?>> getMatchingComponentAdapters(PicoContainer container, ComponentAdapter adapter,
                                 Class keyType, Generic<?> valueType);

}
