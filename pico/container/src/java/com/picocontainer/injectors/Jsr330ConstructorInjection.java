package com.picocontainer.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.inject.Named;

import com.picocontainer.ComponentMonitor;
import com.picocontainer.Parameter;
import com.picocontainer.containers.JSR330PicoContainer;
import com.picocontainer.parameters.ComponentParameter;
import com.picocontainer.parameters.ConstructorParameters;
import com.picocontainer.parameters.JSR330ComponentParameter;

@SuppressWarnings("serial")
public class Jsr330ConstructorInjection extends ConstructorInjection {

	@Override
	protected <T> ConstructorInjector<T> newConstructorInjector(final ComponentMonitor monitor, final Object key, final Class<T> impl,
			final boolean useNames, final ConstructorParameters parameters) {
		return new ConstructorInjectorWithForcedPublicCtors<T>(rememberChosenConstructor, monitor, useNames, key, impl,
				parameters);
	}

	public static class ConstructorInjectorWithForcedPublicCtors<T> extends ConstructorInjector<T> {
		public ConstructorInjectorWithForcedPublicCtors(final boolean rememberChosenConstructor, final ComponentMonitor monitor,
				final boolean useNames, final Object key, final Class<T> impl, final ConstructorParameters parameters)
				throws NotConcreteRegistrationException {
			super(monitor, useNames, rememberChosenConstructor, key, impl, parameters);
		}

		@Override
		protected boolean hasApplicableConstructorModifiers(final int modifiers) {
			return true;
		}

		@Override
		protected void changeAccessToModifierifNeeded(final Constructor<T> ctor) {
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
		protected Parameter getParameterToUse(final Constructor<?> constructorToExamine, final int constructorParameterIndex,
				final Parameter parameter) {
			if (isDefaultParameter(parameter)) {

				// Search for Named class
				for (Annotation eachAnnotation : constructorToExamine.getParameterAnnotations()[constructorParameterIndex]) {
					if (eachAnnotation.annotationType().equals(Named.class)) {
						return new ComponentParameter(((Named) eachAnnotation).value());
					}
				}

				// Search for a qualifier that isn't @Named annotation.
				Annotation qualifier = JSR330PicoContainer
						.getQualifier(constructorToExamine.getParameterAnnotations()[constructorParameterIndex]);
				if (qualifier != null) {
					return new ComponentParameter(qualifier.annotationType().getName());
				}

			}

			return super.getParameterToUse(constructorToExamine, constructorParameterIndex, parameter);
		}

		private boolean isDefaultParameter(final Parameter parameter) {
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
