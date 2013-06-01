package org.picocontainer.injectors;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.Parameter;
import org.picocontainer.containers.JSRPicoContainer;
import org.picocontainer.injectors.ConstructorInjection.ConstructorInjector;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstructorParameters;
import org.picocontainer.parameters.JSR330ComponentParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import javax.inject.Named;

@SuppressWarnings("serial")
public class Jsr330ConstructorInjection extends ConstructorInjection {

	@Override
	protected <T> ConstructorInjector<T> newConstructorInjector(ComponentMonitor monitor, Object key, Class<T> impl,
			boolean useNames, ConstructorParameters parameters) {
		return new ConstructorInjectorWithForcedPublicCtors<T>(rememberChosenConstructor, monitor, useNames, key, impl,
				parameters);
	}

	public static class ConstructorInjectorWithForcedPublicCtors<T> extends ConstructorInjector<T> {
		public ConstructorInjectorWithForcedPublicCtors(boolean rememberChosenConstructor, ComponentMonitor monitor,
				boolean useNames, Object key, Class<T> impl, ConstructorParameters parameters)
				throws NotConcreteRegistrationException {
			super(monitor, useNames, rememberChosenConstructor, key, impl, parameters);
		}

		@Override
		protected boolean hasApplicableConstructorModifiers(int modifiers) {
			return true;
		}

		@Override
		protected void changeAccessToModifierifNeeded(Constructor<T> ctor) {
			if ((ctor.getModifiers() & Modifier.PUBLIC) == 0) {
				ctor.setAccessible(true);
			}
		}

		/**
		 * If there is no Parameter defined for the constructor arg, (Other than
		 * Default), then checks to see if there are
		 * {@linkplain javax.inject.Named} annotations or Qualifier annotations
		 * attached to the argument and uses those as ComponentParameters
		 * instead. If not it uses the superclass default behavior.
		 */
		@Override
		protected Parameter getParameterToUse(Constructor<?> constructorToExamine, int constructorParameterIndex,
				Parameter parameter) {
			if (isDefaultParameter(parameter)) {

				// Search for Named class
				for (Annotation eachAnnotation : constructorToExamine.getParameterAnnotations()[constructorParameterIndex]) {
					if (eachAnnotation.annotationType().equals(Named.class)) {
						return new ComponentParameter(((Named) eachAnnotation).value());
					}
				}

				// Search for a qualifier that isn't @Named annotation.
				Annotation qualifier = JSRPicoContainer
						.getQualifier(constructorToExamine.getParameterAnnotations()[constructorParameterIndex]);
				if (qualifier != null) {
					return new ComponentParameter(qualifier.annotationType().getName());
				}

			}

			return super.getParameterToUse(constructorToExamine, constructorParameterIndex, parameter);
		}

		private boolean isDefaultParameter(Parameter parameter) {
			if (parameter == ComponentParameter.DEFAULT || parameter == JSR330ComponentParameter.DEFAULT) {
				return true;
			}

			if (parameter instanceof ComponentParameter) {
				return !((ComponentParameter) parameter).isKeyDefined();

			}
			return false;
		}

		@Override
		protected Parameter constructDefaultComponentParameter() {
			return JSR330ComponentParameter.DEFAULT;
		}

	}
}
