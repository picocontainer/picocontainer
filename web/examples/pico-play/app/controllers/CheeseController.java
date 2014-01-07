package controllers;

import static play.data.Form.form;
import model.Cheese;
import model.CheeseService;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.cheese;


public class CheeseController extends Controller {

	private CheeseService cheeseService;

	public CheeseController(CheeseService cheeseService) {
		this.cheeseService = cheeseService;
	}
	
	public Result index() {
		Form<Cheese> form = form(Cheese.class);

		return ok(cheese.render(form, cheeseService.getCheeses()));
	}
	
	public Result addCheese() {
		Form<Cheese> form = form(Cheese.class).bindFromRequest();
		
		if (form.hasErrors()) {
			return badRequest(cheese.render(form, cheeseService.getCheeses()));
		} 

		Cheese cheeseModel = form.get();

		if (cheeseService.find(cheeseModel) != null) {
			form.reject("A cheese by that name already exists");
			return badRequest(cheese.render(form, cheeseService.getCheeses()));
		}
		
		cheeseService.save(cheeseModel);
		
		flash("message", "Cheese Saved");
		
		return redirect("/cheeses");
	}
	
	
	
	public Result deleteCheese(final String name) {
		Form<Cheese> form = form(Cheese.class).bindFromRequest();
		
		Cheese example = new Cheese();
		example.setName(name);
		
		Cheese result = cheeseService.find(example);
		if (result == null) {
			return (badRequest(cheese.render(form,  cheeseService.getCheeses())));
		}
		
		cheeseService.remove(result);
		flash("message", "Cheese '"+ result.getName() +"' Deleted");
		
		return redirect("/cheeses");
	}

}
