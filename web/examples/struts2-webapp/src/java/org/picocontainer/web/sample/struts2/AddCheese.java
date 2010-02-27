package org.picocontainer.web.sample.struts2;

import com.opensymphony.xwork2.ActionSupport;

public class AddCheese extends ActionSupport {

    private Cheese cheese = new Cheese();
    private CheeseService cheeseService;
    private final Brand brand;

    public AddCheese(CheeseService cheeseService, Brand brand) {
        this.cheeseService = cheeseService;
        this.brand = brand;
    }

    public Cheese getCheese() {
        return cheese;
    }

    public String getBrand() {
        return "Brand:" + brand;
    }

    public String execute() throws Exception {
        System.out.println("Adding cheese "+cheese + " for " + brand.getName());
        cheeseService.save(cheese);
        return SUCCESS;
    }

}
