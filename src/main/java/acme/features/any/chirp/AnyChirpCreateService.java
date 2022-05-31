
package acme.features.any.chirp;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Chirp;
import acme.entities.SystemConfiguration;
import acme.features.administrator.systemConfiguration.AdministratorSystemConfigurationRepository;
import acme.features.spam.SpamDetector;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.roles.Any;
import acme.framework.services.AbstractCreateService;

@Service
public class AnyChirpCreateService implements AbstractCreateService<Any, Chirp> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected AnyChirpRepository							repository;

	@Autowired
	protected AdministratorSystemConfigurationRepository	scRepo;

	// AbstractCreateService<Any, Chirp> interface -------------------------


	@Override
	public boolean authorise(final Request<Chirp> request) {
		assert request != null;

		return true;
	}

	@Override
	public void bind(final Request<Chirp> request, final Chirp entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "title", "author", "body", "email");

	}

	@Override
	public void unbind(final Request<Chirp> request, final Chirp entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "title", "author", "body", "email");
		model.setAttribute("confirmation", false);
	}

	@Override
	public Chirp instantiate(final Request<Chirp> request) {
		assert request != null;

		Chirp result;

		result = new Chirp();

		Date moment;

		moment = new Date(System.currentTimeMillis() - 1);

		result.setCreationMoment(moment);

		return result;
	}

	@Override
	public void validate(final Request<Chirp> request, final Chirp entity, final Errors errors) {
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

		if (entity.getAuthor() != null && !entity.getAuthor().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getAuthor(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getAuthor(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "author", "any.chirp.form.label.spam", "spam");
		}

		if (entity.getBody() != null && !entity.getBody().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getBody(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getBody(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "body", "any.chirp.form.label.spam", "spam");
		}

		if (entity.getTitle() != null && !entity.getTitle().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getTitle(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getTitle(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "title", "any.chirp.form.label.spam", "spam");
		}
		
		if (!entity.getEmail().equals("") && entity.getEmail() != null) {
			final boolean spam2 = SpamDetector.validateNoSpam(entity.getEmail(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getEmail(), strongSpam, sc.getStrongThreshold());

			errors.state(request, spam2, "email", "any.chirp.form.label.spam", "spam");
		}

		boolean confirmation;

		confirmation = request.getModel().getBoolean("confirmation");
		errors.state(request, confirmation, "confirmation", "javax.validation.constraints.AssertTrue.message");
	}

	@Override
	public void create(final Request<Chirp> request, final Chirp entity) {
		assert request != null;
		assert entity != null;

		Date moment;

		moment = new Date(System.currentTimeMillis() - 1);
		entity.setCreationMoment(moment);

		this.repository.save(entity);

	}

}
