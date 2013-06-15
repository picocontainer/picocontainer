/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.picocontainer.Characteristics;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.AbstractBehavior;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.FieldParameters;
import org.picocontainer.parameters.MethodParameters;

@SuppressWarnings("serial")
public class AnnotatedStaticInjection extends AbstractBehavior {

	private final StaticsInitializedReferenceSet referenceSet;


	public AnnotatedStaticInjection() {
		this (new StaticsInitializedReferenceSet());
	}

	public AnnotatedStaticInjection(final StaticsInitializedReferenceSet referenceSet) {
		this.referenceSet = referenceSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ComponentAdapter<T> createComponentAdapter(final ComponentMonitor monitor, final LifecycleStrategy lifecycle,
			final Properties componentProps, final Object key, final Class<T> impl, final ConstructorParameters constructorParams,
			final FieldParameters[] fieldParams, final MethodParameters[] methodParams) throws PicoCompositionException {

		boolean useNames = AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.USE_NAMES, true);
        boolean requireConsumptionOfAllParameters = !(AbstractBehavior.arePropertiesPresent(componentProps, Characteristics.ALLOW_UNUSED_PARAMETERS, false));

		ComponentAdapter<T> result = null;
		boolean noStatic = removePropertiesIfPresent(componentProps, Characteristics.NO_STATIC_INJECTION);


		//NO_STATIC_INJECTION takes precedence
		if (removePropertiesIfPresent(componentProps, Characteristics.STATIC_INJECTION) && !noStatic) {
	        result = monitor.changedBehavior(new StaticInjection<T>(referenceSet,
	                super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams)
	                , useNames,
	                requireConsumptionOfAllParameters,
	                fieldParams, methodParams));
		}

		if (result == null) {
			//static injection wasn't specified
			result = super.createComponentAdapter(monitor, lifecycle, componentProps, key, impl, constructorParams, fieldParams, methodParams);
		}

		return result;
	}

	public class StaticInjection<T> extends AbstractChangedBehavior<T> {

		private transient StaticsInitializedReferenceSet referenceSet;


		private boolean useNames;

		private final boolean consumeAllParameters;

		private final FieldParameters[] fieldParams;

		private final MethodParameters[] methodParams;


		private final List<StaticInjector<?>> wrappedInjectors;



		public StaticInjection(final StaticsInitializedReferenceSet referenceSet, final ComponentAdapter<T> delegate, final boolean usenames, final boolean consumeAllParameters, final FieldParameters fieldParams[], final MethodParameters[] methodParams) {
			super(delegate);
			this.referenceSet = referenceSet;
			this.useNames = usenames;
			this.consumeAllParameters = consumeAllParameters;
			useNames = consumeAllParameters;
			this.fieldParams = fieldParams;
			this.methodParams = methodParams;

			wrappedInjectors = createListOfStaticInjectors(getComponentImplementation());
		}

		private List<StaticInjector<?>> createListOfStaticInjectors(final Class<?> componentImplementation) {
			List<StaticInjector<?>> injectors = new ArrayList<StaticInjector<?>>();
			Class<?> currentClass = componentImplementation;
			Class<? extends Annotation> injectionAnnotation = AnnotatedMethodInjection.getInjectionAnnotation("javax.inject.Inject");

			while(!currentClass.equals(Object.class)) {
				//
				//Method first because we're going to reverse the entire collection
				//after building.
				//
				StaticInjector<?> methodInjector = constructStaticMethodInjections(injectionAnnotation, currentClass);
				if (methodInjector != null) {
					injectors.add(methodInjector);
				}


				StaticInjector<?> fieldInjector = constructStaticFieldInjections(injectionAnnotation, currentClass);
				if (fieldInjector != null) {
					injectors.add(fieldInjector);
				}


				currentClass = currentClass.getSuperclass();

			}


			Collections.reverse(injectors);
			return injectors;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private StaticInjector<?> constructStaticMethodInjections(final Class<? extends Annotation> injectionAnnotation,
				final Class<?> currentClass) {

			List<Method> methodsToInject = null;
			for(Method eachMethod : currentClass.getDeclaredMethods()) {
				if (!Modifier.isStatic(eachMethod.getModifiers())) {
					continue;
				}

				if (this.getReferenceSet().isMemberAlreadyInitialized(eachMethod)) {
					continue;
				}

				if (eachMethod.isAnnotationPresent(injectionAnnotation)) {
					if (methodsToInject == null) {
						methodsToInject = new ArrayList<Method>();
					}
					methodsToInject.add(eachMethod);
				}
			}

			if (methodsToInject == null || methodsToInject.size() == 0) {
				return null;
			}

			return new SpecificMethodInjector(this.getComponentKey(),
					this.getComponentImplementation(),
					currentMonitor(),
					this.useNames,
					this.consumeAllParameters,
					this.methodParams,
					methodsToInject.toArray(new Method[methodsToInject.size()]));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private StaticInjector<?> constructStaticFieldInjections(final Class<? extends Annotation> injectionAnnotation,
				final Class<?> currentClass) {
			List<Field> fieldsToInject = null;
			for(Field eachField : currentClass.getDeclaredFields()) {
				if (!Modifier.isStatic(eachField.getModifiers())) {
					continue;
				}

				if (this.getReferenceSet().isMemberAlreadyInitialized(eachField)) {
					continue;
				}

				if (eachField.isAnnotationPresent(injectionAnnotation)) {
					if (fieldsToInject == null) {
						fieldsToInject = new ArrayList<Field>();
					}
					fieldsToInject.add(eachField);
				}
			}

			if (fieldsToInject == null || fieldsToInject.size() == 0) {
				return null;
			}
			return new SpecificFieldInjector(this.getComponentKey(),
					this.getComponentImplementation(),
					currentMonitor(),
					this.useNames,
					this.consumeAllParameters,
					this.fieldParams,
					fieldsToInject.toArray(new Field[fieldsToInject.size()]));
		}

		@Override
		public T getComponentInstance(final PicoContainer container, final Type into) {
			if (getReferenceSet() != null) {

				//The individual static injectors decide
				//if a static member has already been injected or not.
				for (StaticInjector<?> staticInjectors : wrappedInjectors) {
					staticInjectors.injectStatics(container, into, getReferenceSet());
				}
			}
			return super.getComponentInstance(container, into);
		}


		public String getDescriptor() {
			return "StaticAnnotationInjector";
		}

		/**
		 * If we've been serialized, we'll have to recreate from scratch and reinject static members.
		 * @return
		 */
		private StaticsInitializedReferenceSet getReferenceSet() {
			if (referenceSet == null) {
				referenceSet = new StaticsInitializedReferenceSet();
			}
			return referenceSet;
		}

	}
}
