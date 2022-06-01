
package acme.features.inventor.itemQuantity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.ItemQuantity;
import acme.entities.ItemType;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.services.AbstractUpdateService;
import acme.roles.Inventor;

@Service
public class InventorItemQuantityUpdateService implements AbstractUpdateService<Inventor, ItemQuantity> {

	// Internal state ---------------------------------------------------------

	@Autowired
	InventorItemQuantityRepository repository;

	// AbstractUpdateService<Inventor,Item> interface -----------------

	@Override
	public void validate(final Request<ItemQuantity> request, final ItemQuantity entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		if(!errors.hasErrors("quantity")) {
			errors.state(request, entity.getQuantity() > 0, "quantity", "inventor.item-quantity.form.error.negative-number");
		}
					
		if(!errors.hasErrors("quantity") && entity.getItem().getType().equals(ItemType.TOOL)) {
			errors.state(request, entity.getQuantity() == 1, "quantity", "inventor.item-quantity.form.error.incorrect-tool-quantity");
		}	

	}


	@Override
	public boolean authorise(final Request<ItemQuantity> request) {
		assert request != null;
		boolean result;

		ItemQuantity quantity;
		int id;

		id = request.getModel().getInteger("id");
		quantity = this.repository.findItemQuantityById(id);
		result = quantity.getToolkit().isDraftMode() && request.isPrincipal(quantity.getToolkit().getInventor());
		return result;
	}

	@Override
	public void bind(final Request<ItemQuantity> request, final ItemQuantity entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "quantity");
	}

	@Override
	public void unbind(final Request<ItemQuantity> request, final ItemQuantity entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "quantity", "item.name", "item.type","item.code","item.description", "item.technology", "item.retailPrice");
		model.setAttribute("type", entity.getItem().getType().toString());
		model.setAttribute("draftMode", entity.getToolkit().isDraftMode());
	}

	@Override
	public ItemQuantity findOne(final Request<ItemQuantity> request) {
		assert request != null;

		ItemQuantity result;
		int id;

		id = request.getModel().getInteger("id");
		result = this.repository.findItemQuantityById(id);

		return result;

	}

	@Override
	public void update(final Request<ItemQuantity> request, final ItemQuantity entity) {
		assert request != null;
		assert entity != null;

		this.repository.save(entity);
	}


}
