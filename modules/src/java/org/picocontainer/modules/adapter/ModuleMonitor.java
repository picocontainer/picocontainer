package org.picocontainer.modules.adapter;

import java.io.Serializable;

import org.apache.commons.vfs.FileObject;
import org.picocontainer.MutablePicoContainer;

public interface ModuleMonitor extends Serializable {

	void noCompositionClassFound(FileObject applicationFolder,
			String className, ClassLoader moduleClassLoader,
			ClassNotFoundException e);

	void compositionClassNotCorrectType(FileObject applicationFolder,
			Class<?> compositionClass, ClassLoader moduleClassLoader);

	void errorPerformingDeploy(FileObject applicationFolder,
			Throwable e);

	void deploySuccess(FileObject applicationFolder,MutablePicoContainer returnValue, long timeInMillis);
}
