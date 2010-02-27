/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 *****************************************************************************/
package org.picocontainer.web.sample.struts;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * @author Stephen Molitor
 * @author Mauro Talevi
 */
public class CheeseAction extends Action {

    private final CheeseService cheeseService;

    public CheeseAction(CheeseService cheeseService) {
        this.cheeseService = cheeseService;
    }

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) {
        CheeseForm cheeseForm = (CheeseForm) form;

        if (!isEmpty(cheeseForm.getName())) {
            Cheese cheese = new Cheese(cheeseForm.getName(), cheeseForm.getCountry());
            cheeseService.save(cheese);
        }

        Collection cheeses = cheeseService.getCheeses();
        request.setAttribute("cheesesOfTheWord", cheeses);

        return mapping.findForward("next");
    }

    private boolean isEmpty(String s) {
        return s == null || "".equals(s.trim());
    }

}