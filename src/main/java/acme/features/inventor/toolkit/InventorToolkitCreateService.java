
package acme.features.inventor.toolkit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.SystemConfiguration;
import acme.entities.Toolkit;
import acme.features.administrator.systemConfiguration.AdministratorSystemConfigurationRepository;
import acme.features.spam.SpamDetector;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.services.AbstractCreateService;
import acme.roles.Inventor;

@Service
public class InventorToolkitCreateService implements AbstractCreateService<Inventor, Toolkit> {

	@Autowired
	protected InventorToolkitRepository								repository;

	@Autowired
	protected AdministratorSystemConfigurationRepository	scRepo;


	@Override
	public boolean authorise(final Request<Toolkit> request) {
		assert request != null;

		return true;
	}

	@Override
	public void bind(final Request<Toolkit> request, final Toolkit entity, final Errors errors) {

		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "code", "title", "description", "assemblyNotes", "moreInfo");	}

	@Override
	public void unbind(final Request<Toolkit> request, final Toolkit entity, final Model model) {

		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "code", "title", "description", "assemblyNotes", "totalPrice","moreInfo", "draftMode");
		model.setAttribute("readonly", false);
	}

	@Override
	public Toolkit instantiate(final Request<Toolkit> request) {

		assert request != null;

		Toolkit toolkit;
		toolkit = new Toolkit();

		toolkit.setCode("");
		toolkit.setTitle("");
		toolkit.setDescription("");
		toolkit.setAssemblyNotes("");
		toolkit.setMoreInfo("");

		Inventor inventor;
		inventor = this.repository.findInventorById(request.getPrincipal().getActiveRoleId());
		toolkit.setInventor(inventor);

		return toolkit;
	}

	@Override
	public void validate(final Request<Toolkit> request, final Toolkit entity, final Errors errors) {

		assert request != null;
		assert entity != null;
		assert errors != null;
		
		if (!errors.hasErrors("code")) {
			final Toolkit alreadyExists = this.repository.findOneToolkitByCode(entity.getCode());
			errors.state(request, alreadyExists == null || alreadyExists.getId() == entity.getId(), "code", "inventor.toolkit.form.error.duplicated");
		}

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
		
		if (!entity.getMoreInfo().equals("") && entity.getMoreInfo() != null) {
			final boolean spam2 = SpamDetector.validateNoSpam(entity.getMoreInfo(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getMoreInfo(), strongSpam, sc.getStrongThreshold());

			errors.state(request, spam2, "moreInfo", "inventor.toolkit.form.label.spam", "spam");
		}

	}

	@Override
	public void create(final Request<Toolkit> request, final Toolkit entity) {
		assert request != null;
		assert entity != null;

		entity.setDraftMode(true);
		this.repository.save(entity);
	}

}
