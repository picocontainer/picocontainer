package org.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        Map<String, List<Method>> allMethodsAnalyzed = new HashMap<String, List<Method>>();
        List<Method> methodz = new ArrayList<Method>();
        recursiveCheckInjectorMethods(type, type, methodz, allMethodsAnalyzed);
        return methodz;
		
	}
	
	
    protected void recursiveCheckInjectorMethods(Class<?> originalType, 
    			Class<?> type, List<Method> receiver, 
    			Map<String, List<Method>> allMethodsAnalyzed) {
    	
    	//Ignore interfaces for this.
    	if (originalType.isInterface()) {
    		return;
    	}
    	
    	
    	if (type.isAssignableFrom(Object.class)) {
    		return;
    	}
    	

    	for (Method eachMethod : type.getDeclaredMethods()) {
    		if(isChildClassMethodOverridingCurrentMethod(eachMethod, allMethodsAnalyzed)) {
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

    
    private void addToMethodsAnalyzed(Map<String, List<Method>> allMethodsAnalyzed, Method eachMethod) {
    	final String signature = getMethodSignature(eachMethod);
    	List<Method> methodsWithThisSignature = allMethodsAnalyzed.get(signature);
    	if (methodsWithThisSignature == null) {
    		methodsWithThisSignature = new LinkedList<Method>();
    		allMethodsAnalyzed.put(signature, methodsWithThisSignature);
    	}
    	
    	//Bottom of the hierarchy methods should be last in the list.
    	methodsWithThisSignature.add(0,eachMethod);
	}
    
    protected final boolean isInjectorMethod(Class<?> originalType, Method method, Map<String, List<Method>> allMethodsAnalyzed) {
    	
    	boolean returnResult = false;
        for (Class<? extends Annotation> injectionAnnotation : annotation) {
            if (method.isAnnotationPresent(injectionAnnotation)) {
            	returnResult = true;
            }
        }
        
        if (returnResult) {
        	returnResult = isStillViableGivenOverrides(method, allMethodsAnalyzed);
        }
        
        return returnResult;
    }
    

    /**
     * Returns a string showing the method signature.
     * @param eachMethod
     * @return
     */
    private String getMethodSignature(Method eachMethod) {
    	//At this point going to ignore return type since covarient return types (I think) would
    	//qualify as the same method.
    	return eachMethod.getName() + "(" + Arrays.deepToString(eachMethod.getParameterTypes()) + ")";
    }
    
    /**
     * Returns true if a child method has been already declared with this signature.  
     * @param eachMethod
     * @param allMethodsAnalyzed
     * @return true if the child method has already been found, ignore the new one, the child
     * one overrides.
     */
	private boolean isChildClassMethodOverridingCurrentMethod(Method currentMethod, Map<String, List<Method>> allMethodsAnalyzed) {
		
    	/**
    	 * Private methods can't be overridden.
    	 */
    	if (Modifier.isPrivate(currentMethod.getModifiers())) {
    		return false;
    	}
		
    	
    	if (allMethodsAnalyzed.containsKey(getMethodSignature(currentMethod))) {
    		
    		//Go through the list of ones to potentially show that we haven't already analyzed it.
    		for (Method eachMethod : allMethodsAnalyzed.get(getMethodSignature(currentMethod))) {
    			
    			//We don't count private methods.
    			if (Modifier.isPrivate(eachMethod.getModifiers())) {
    				continue;
    			}
    			
    			if (this.isPackagePrivate(eachMethod) && isClassDefinitionsInDifferentPackages(currentMethod, eachMethod)) {
    				continue;
    			}
    			
    			return true;
    		}

    		//If we went through all the methods and none met the overriding criteria 
    		//Then there is no child class method overriding this one.
    	}
    	
    	return false;
	}
	
	
	/**
	 * Compares method's class' defining packages and returns true if the packages are different.
	 * @param currentMethod
	 * @param eachMethod
	 * @return
	 */
    private boolean isClassDefinitionsInDifferentPackages(Method currentMethod, Method eachMethod) {
    	Package currentMethodPackage = currentMethod.getDeclaringClass().getPackage();
    	Package testMethodPackage = eachMethod.getDeclaringClass().getPackage();

    	return !(currentMethodPackage.getName().equals(testMethodPackage.getName()));
    
    }


	private boolean isStillViableGivenOverrides(Method method, Map<String, List<Method>> allMethodsAnalyzed) {
    	
    	if (Modifier.isPublic(method.getModifiers()) || isPackagePrivate(method)) {
    		if (isChildClassMethodOverridingCurrentMethod(method, allMethodsAnalyzed)) {
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
