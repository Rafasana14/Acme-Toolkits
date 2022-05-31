package acme.features.administrator.announcement;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Announcement;
import acme.entities.SystemConfiguration;
import acme.features.administrator.systemConfiguration.AdministratorSystemConfigurationRepository;
import acme.features.spam.SpamDetector;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.roles.Administrator;
import acme.framework.services.AbstractCreateService;

@Service
public class AdministratorAnnouncementCreateService implements AbstractCreateService<Administrator, Announcement> {
	
	// Internal state ---------------------------------------------------------

		@Autowired
		protected AdministratorAnnouncementRepository repository;
		
		@Autowired
		protected AdministratorSystemConfigurationRepository	scRepo;

		// AbstractCreateService<Administrator, Announcement> interface --------------


		@Override
		public boolean authorise(final Request<Announcement> request) {
			assert request != null;

			return true;
		}

		@Override
		public void bind(final Request<Announcement> request, final Announcement entity, final Errors errors) {
			assert request != null;
			assert entity != null;
			assert errors != null;

			request.bind(entity, errors ,"title", "body", "critical", "email", "moreInfo");
		}

		@Override
		public void unbind(final Request<Announcement> request, final Announcement entity, final Model model) {
			assert request != null;
			assert entity != null;
			assert model != null;

			request.unbind(entity, model ,"title", "body", "critical", "email", "moreInfo");
			model.setAttribute("confirmation", false);
			model.setAttribute("readonly", false);
		}

		@Override
		public Announcement instantiate(final Request<Announcement> request) {
			assert request != null;

			Announcement result;
			Date moment;

			moment = new Date(System.currentTimeMillis() - 1);

			result = new Announcement();
			result.setTitle("");
			result.setCreationMoment(moment);
			result.setBody("");
			result.setEmail("");

			return result;
		}

		@Override
		public void validate(final Request<Announcement> request, final Announcement entity, final Errors errors) {
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

			if (entity.getBody() != null && !entity.getBody().equals("")) {
				final boolean spam1 = SpamDetector.validateNoSpam(entity.getBody(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getBody(), strongSpam, sc.getStrongThreshold());
				errors.state(request, spam1, "body", "administrator.announcement.form.label.spam", "spam");
			}
			
			if (entity.getTitle() != null && !entity.getTitle().equals("")) {
				final boolean spam1 = SpamDetector.validateNoSpam(entity.getTitle(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getTitle(), strongSpam, sc.getStrongThreshold());
				errors.state(request, spam1, "title", "administrator.announcement.form.label.spam", "spam");
			}
			
			if (entity.getEmail() != null && !entity.getEmail().equals("")) {
				final boolean spam1 = SpamDetector.validateNoSpam(entity.getEmail(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getEmail(), strongSpam, sc.getStrongThreshold());
				errors.state(request, spam1, "email", "administrator.announcement.form.label.spam", "spam");
			}
			
			if (entity.getMoreInfo() != null && !entity.getMoreInfo().equals("")) {
				final boolean spam2 = SpamDetector.validateNoSpam(entity.getMoreInfo(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getMoreInfo(), strongSpam, sc.getStrongThreshold());

				errors.state(request, spam2, "moreInfo", "administrator.announcement.form.label.spam", "spam");
			}

			boolean confirmation;

			confirmation = request.getModel().getBoolean("confirmation");
			errors.state(request, confirmation, "confirmation", "javax.validation.constraints.AssertTrue.message");
		}

		@Override
		public void create(final Request<Announcement> request, final Announcement entity) {
			assert request != null;
			assert entity != null;

			Date moment;

			moment = new Date(System.currentTimeMillis() - 1);
			entity.setCreationMoment(moment);
			
			this.repository.save(entity);
		}

}
