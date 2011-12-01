/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           * 
 * Original code by Centerline Computers, Inc.                               *
 *****************************************************************************/

package org.picocontainer.web.samples.jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;

/**
 * ListCheeseController
 * @author Michael Rimov
 */
public class ListCheeseController implements Serializable {
    

    /**
     * The Cheese Service we handle.
     */
    private final CheeseService service;
    
    /**
     * The list of cheeses last loaded.
     */
    private List cheeses;
    
    
    /**
     * Data grid item that works with the data table
     * of cheeses.
     */
    private UIData cheeseList;
    
    
    public ListCheeseController(CheeseService service) {
       this.service = service;       
    }
    
    public List getCheeses() {
        if (cheeses == null) {
            cheeses = new ArrayList(service.getCheeses());
        }
        return cheeses;
    }
    
    
    public int getNumCheeses() {
        return cheeses.size();
    }

    /**
     * @return the cheeseList
     */
    public UIData getCheeseList() {
        return cheeseList;
    }

    /**
     * @param cheeseList the cheeseList to set
     */
    public void setCheeseList(UIData cheeseList) {
        this.cheeseList = cheeseList;
    }
    
    /**
     * Queries the UIData for the current row of data to get
     * the current cheese.
     * @return String for the cheese name.
     * @throws NullPointerException if the rowdata returns a null
     * Cheese.
     */
    private String getSelectedCheeseName() {
        Cheese cheese = (Cheese) cheeseList.getRowData(); //  make sure it still exists
        if (cheese == null) {
            throw new NullPointerException("cheese");
        }
        
        return cheese.getName();
    }
    
    public String removeCheese() {
        Cheese tempCheese = new Cheese(getSelectedCheeseName(), "");
        Cheese storedCheese = service.find(tempCheese);
        if (storedCheese == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cheese " + tempCheese.getName() + "  Not Found!"));
            return "delete error";
        } 
        
        service.remove(storedCheese);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Cheese Removed"));
        return "ok";        
    }
}
