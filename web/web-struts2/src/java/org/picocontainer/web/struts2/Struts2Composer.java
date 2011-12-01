/*******************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.
 * --------------------------------------------------------------------------
 * The software in this package is published under the terms of the BSD style
 * license a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 ******************************************************************************/
package org.picocontainer.web.struts2;

import com.opensymphony.xwork2.interceptor.AliasInterceptor;
import com.opensymphony.xwork2.interceptor.ChainingInterceptor;
import com.opensymphony.xwork2.interceptor.DefaultWorkflowInterceptor;
import com.opensymphony.xwork2.interceptor.ExceptionMappingInterceptor;
import com.opensymphony.xwork2.interceptor.I18nInterceptor;
import com.opensymphony.xwork2.interceptor.ModelDrivenInterceptor;
import com.opensymphony.xwork2.interceptor.ParametersInterceptor;
import com.opensymphony.xwork2.interceptor.PrepareInterceptor;
import com.opensymphony.xwork2.interceptor.ScopedModelDrivenInterceptor;
import com.opensymphony.xwork2.interceptor.StaticParametersInterceptor;
import org.apache.struts2.interceptor.CheckboxInterceptor;
import org.apache.struts2.interceptor.ExecuteAndWaitInterceptor;
import org.apache.struts2.interceptor.FileUploadInterceptor;
import org.apache.struts2.interceptor.ProfilingActivationInterceptor;
import org.apache.struts2.interceptor.ServletConfigInterceptor;
import org.apache.struts2.interceptor.StrutsConversionErrorInterceptor;
import org.apache.struts2.interceptor.debugging.DebuggingInterceptor;
import org.apache.struts2.interceptor.validation.AnnotationValidationInterceptor;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.web.WebappComposer;

import javax.servlet.ServletContext;

import ognl.OgnlRuntime;

public abstract class Struts2Composer implements WebappComposer {

    public void composeApplication(MutablePicoContainer container, ServletContext servletContext) {

        container.addComponent(ExceptionMappingInterceptor.class);
        container.addComponent(ServletConfigInterceptor.class);
        container.addComponent(PrepareInterceptor.class);
        container.addComponent(CheckboxInterceptor.class);
        container.addComponent(ParametersInterceptor.class);
        container.addComponent(StrutsConversionErrorInterceptor.class);
        container.addComponent(AnnotationValidationInterceptor.class);
        container.addComponent(DefaultWorkflowInterceptor.class);
        container.addComponent(FileUploadInterceptor.class);
        container.addComponent(ModelDrivenInterceptor.class);
        container.addComponent(ChainingInterceptor.class);
        container.addComponent(I18nInterceptor.class);
        container.addComponent(AliasInterceptor.class);
        container.addComponent(StaticParametersInterceptor.class);
        container.addComponent(DebuggingInterceptor.class);
        container.addComponent(ProfilingActivationInterceptor.class);
        container.addComponent(ScopedModelDrivenInterceptor.class);
        container.addComponent(ExecuteAndWaitInterceptor.class);
    }
}
