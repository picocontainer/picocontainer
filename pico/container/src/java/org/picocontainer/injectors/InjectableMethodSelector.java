package org.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InjectableMethodSelector {
	
	private Class<Annotation>[] annotation;


	@SuppressWarnings("unchecked")
	public InjectableMethodSelector(Class<? extends Annotation>... searchingAnnotation) {
		//this.annotation = searchingAnnotation;
		
		
		//
		//Add javax.inject.Inject method if its available.
		//
		
		ArrayList<Class<? extends Annotation>> annotations = new ArrayList<Class<? extends Annotation>>(Arrays.asList(searchingAnnotation));
		try {
			Class<? extends Annotation> javaxInject = (Class<? extends Annotation>) Class.forName("javax.inject.Inject");
			annotations.add(javaxInject);
		} catch (ClassNotFoundException e) {
			//Ignore
		}
		
		annotation = annotations.toArray(new Class[annotations.size()]);
	}


	public List<Method> retreiveAllInjectableMethods(Class<?> type) {

        HashMap<String, Set<Method>> allMethodsAnalyzed = new HashMap<String,Set<Method>>();
        List<Method> methodz = new ArrayList<Method>();
        recursiveCheckInjectorMethods(type, type, methodz, allMethodsAnalyzed);
        return methodz;
		
	}
	
	
    protected void recursiveCheckInjectorMethods(Class<?> originalType, Class<?> type, List<Method> receiver, HashMap<String, Set<Method>> allMethodsAnalyzed) {
    	//Ignore interfaces for this.
    	if (originalType.isInterface()) {
    		return;
    	}
    	
    	
    	if (type.isAssignableFrom(Object.class)) {
    		return;
    	}
    	

    	for (Method eachMethod : type.getDeclaredMethods()) {
    		if(alreadyAnalyzedChildClassMethod(originalType, eachMethod, allMethodsAnalyzed)) {
    			//This method was defined in a child class, what the child class says, goes.
    			continue;
    		} 

    		if (isInjectorMethod(originalType, eachMethod, allMethodsAnalyzed)) {
    			receiver.add(eachMethod);
    		}
    		
    		addToMethodsAnalyzed(allMethodsAnalyzed, eachMethod);
    		
    	}
    	
    	recursiveCheckInjectorMethods(originalType, type.getSuperclass(), receiver, allMethodsAnalyzed);
    }

    
    private void addToMethodsAnalyzed(HashMap<String, Set<Method>> allMethodsAnalyzed, Method eachMethod) {
    	if (!allMethodsAnalyzed.containsKey(eachMethod.getName())) {
    		allMethodsAnalyzed.put(eachMethod.getName(), new HashSet<Method>());
    	} 
		
    	allMethodsAnalyzed.get(eachMethod.getName()).add(eachMethod);
	}
    
    protected final boolean isInjectorMethod(Class<?> originalType, Method method, HashMap<String, Set<Method>> allMethodsAnalyzed) {
    	
    	boolean returnResult = false;
        for (Class<? extends Annotation> injectionAnnotation : annotation) {
            if (method.isAnnotationPresent(injectionAnnotation)) {
            	returnResult = true;
            }
        }
        
        if (returnResult) {
        	returnResult = isStillViableGivenOverrides(originalType, method, allMethodsAnalyzed);
        }
        
        return returnResult;
    }
    
    
    /**
     * Returns a declared child member of the same times or null if there is no
     * child member declared the same way.
     * @param childClass
     * @param examiningMethod
     * @return
     */
    private Method getChildMethodIfDeclared(Class<?> childClass, Method examiningMethod) {
    	if (childClass.equals(examiningMethod.getDeclaringClass())) {
    		return null;
    	}
    	
    	try {
			Method m = childClass.getDeclaredMethod(examiningMethod.getName(), examiningMethod.getParameterTypes());
			return m;
		} catch (NoSuchMethodException e) {
			return getChildMethodIfDeclared(childClass.getSuperclass(), examiningMethod);
		}
    }

    /**
     * Returns true if a child method has been already declared with this signature.  
     * @param baseClass
     * @param eachMethod
     * @param allMethodsAnalyzed
     * @return true if the child method has already been found, ignore the new one, the child
     * one overrides.
     */
	private boolean alreadyAnalyzedChildClassMethod(Class<?> baseClass, Method eachMethod,
			HashMap<String, Set<Method>> allMethodsAnalyzed) {
		
		Set<Method> methodsByName = allMethodsAnalyzed.get(eachMethod.getName());
		if (methodsByName == null) {
			methodsByName = new HashSet<Method>();
			allMethodsAnalyzed.put(eachMethod.getName(), methodsByName);
		}
		

		if (methodsByName.contains(eachMethod)) {
			return true;
		}
				
		Method m = getChildMethodIfDeclared(baseClass, eachMethod);
		if (m != null) {
			return true;
		}
		

		return false;
	}
	
	
    private boolean isStillViableGivenOverrides(Class<?> originalType, Method method, HashMap<String, Set<Method>> allMethodsAnalyzed) {
    	
    	
    	if (Modifier.isPublic(method.getModifiers()) || isPackagePrivate(method)) {
    		if (alreadyAnalyzedChildClassMethod(originalType, method, allMethodsAnalyzed)) {
    			return false;
    		}
    		
    	}
    	
    	
    	return true;
    }
	
    protected boolean isPackagePrivate(Method m) {
    	int methodModifiers = m.getModifiers();
    	return (!Modifier.isPrivate(methodModifiers) && !Modifier.isPublic(methodModifiers) && !Modifier.isProtected(methodModifiers));
    }
    
	
}
