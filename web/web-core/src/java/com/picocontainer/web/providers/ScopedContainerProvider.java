package com.picocontainer.web.providers;

import com.picocontainer.web.ScopedContainers;

public interface ScopedContainerProvider {

	ScopedContainers makeScopedContainers(boolean stateless);
}
