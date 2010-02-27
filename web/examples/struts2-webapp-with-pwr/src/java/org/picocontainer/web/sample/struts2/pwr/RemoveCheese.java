package org.picocontainer.web.sample.struts2.pwr;

import com.opensymphony.xwork2.ActionSupport;

public class RemoveCheese extends ActionSupport {

    private Cheese cheese = new Cheese();
    private CheeseService cheeseService;

    public RemoveCheese(CheeseService cheeseService) {
        this.cheeseService = cheeseService;
    }

    public Cheese getCheese() {
        return cheese;
    }

    public String execute() throws Exception {
        System.out.println("Removing cheese "+cheese);
        cheeseService.remove(cheese);
        return SUCCESS;
    }

}
