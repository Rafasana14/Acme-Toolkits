package acme.features.inventor.toolkit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Item;
import acme.entities.ItemQuantity;
import acme.entities.SystemConfiguration;
import acme.entities.Toolkit;
import acme.features.administrator.systemConfiguration.AdministratorSystemConfigurationRepository;
import acme.features.spam.SpamDetector;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.services.AbstractUpdateService;
import acme.roles.Inventor;

@Service	
public class InventorToolkitPublishService implements AbstractUpdateService<Inventor, Toolkit>{

	
	@Autowired
	protected InventorToolkitRepository repository;
	
	@Autowired
	protected AdministratorSystemConfigurationRepository	scRepo;
	
	
	@Override
	public boolean authorise(final Request<Toolkit> request) {
		assert request != null;
		
		boolean checkPublished = false;
		final int id;
		Toolkit toolkit;
		
		id = request.getModel().getInteger("id");
		toolkit = this.repository.findOneToolkitById(id);
		checkPublished = (toolkit != null && toolkit.isDraftMode()) && request.isPrincipal(toolkit.getInventor());
		
		return checkPublished;
	}

	@Override
	public void bind(final Request<Toolkit> request, final Toolkit entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;
		
		request.bind(entity, errors,"title", "description", "assemblyNotes", "moreInfo");
		
	}

	@Override
	public void unbind(final Request<Toolkit> request, final Toolkit entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;
		
		request.unbind(entity, model, "code", "title", "description", "assemblyNotes", "moreInfo", "draftMode","totalPrice");
	}

	@Override
	public Toolkit findOne(final Request<Toolkit> request) {
		assert request != null;
		
		final int id = request.getModel().getInteger("id");
		return this.repository.findOneToolkitById(id);
	}

	@Override
	public void validate(final Request<Toolkit> request, final Toolkit entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;
		
		final SystemConfiguration sc = this.scRepo.findSystemConfigurationById();
		final String[] parts = sc.getStrongSpam().split(";");
		final String[] parts2 = sc.getWeakSpam().split(";");
		final List<String> strongSpam = new LinkedList<>();
		final List<String> weakSpam = new LinkedList<>();
		Collections.addAll(strongSpam, parts);
		Collections.addAll(weakSpam, parts2);

		if (entity.getDescription() != null && !entity.getDescription().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getDescription(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getDescription(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "description", "inventor.toolkit.form.label.spam", "spam");
		}

		if (entity.getTitle() != null && !entity.getTitle().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getTitle(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getTitle(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "title", "inventor.toolkit.form.label.spam", "spam");
		}

		if (entity.getAssemblyNotes() != null && !entity.getAssemblyNotes().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getAssemblyNotes(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getAssemblyNotes(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "assemblyNotes", "inventor.toolkit.form.label.spam", "spam");
		}
		
		if (!errors.hasErrors("code")) {
			final Toolkit alreadyExists = this.repository.findOneToolkitByCode(entity.getCode());
			errors.state(request, alreadyExists == null || alreadyExists.getId() == entity.getId(), "code", "inventor.toolkit.form.error.duplicated");
		}
		
		final Collection<ItemQuantity> quantities = this.repository.findItemQuantitiesOfToolkit(entity.getId());
		final long types = quantities.stream().map(ItemQuantity::getItem).map(Item::getType).distinct().count();
		errors.state(request, types == 2, "*", "inventor.toolkit.form.error.invalid-items");
		
	}

	@Override
	public void update(final Request<Toolkit> request, final Toolkit entity) {
		assert request != null;
		assert entity != null;
		
		entity.setDraftMode(false);
		this.repository.save(entity);
		
	}

}
