
package acme.features.inventor.chimpum;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Chimpum;
import acme.entities.Item;
import acme.entities.SystemConfiguration;
import acme.features.administrator.systemConfiguration.AdministratorSystemConfigurationRepository;
import acme.features.spam.SpamDetector;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.datatypes.Money;
import acme.framework.services.AbstractUpdateService;
import acme.roles.Inventor;

@Service

public class InventorChimpumUpdateService implements AbstractUpdateService<Inventor, Chimpum> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected InventorChimpumRepository						repository;

	@Autowired
	protected AdministratorSystemConfigurationRepository	scRepo;

	// AbstractUpdateService<Inventor,Item> interface -----------------


	@Override
	public boolean authorise(final Request<Chimpum> request) {
		assert request != null;
		boolean result = false;

		int id;
		Item item;
		Inventor inventor;

		id = request.getModel().getInteger("id");
		item = this.repository.findOneItemByChimpumId(id);
		inventor = item.getInventor();
		result = item.isPublished();

		return !result && request.isPrincipal(inventor);
	}

	@Override
	public void bind(final Request<Chimpum> request, final Chimpum entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "title", "description", "startDate", "endDate", "budget", "moreInfo");
	}

	@Override
	public void validate(final Request<Chimpum> request, final Chimpum entity, final Errors errors) {
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
			errors.state(request, spam1, "description", "inventor.chimpum.form.label.spam", "spam");
		}

		if (entity.getTitle() != null && !entity.getTitle().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getTitle(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getTitle(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "title", "inventor.chimpum.form.label.spam", "spam");
		}

		if (!errors.hasErrors("startDate")) {
			final Date oneMonthAfterCreationDate = DateUtils.addMonths(entity.getCreationMoment(), 1);
			errors.state(request, entity.getStartDate().equals(oneMonthAfterCreationDate) || entity.getStartDate().after(oneMonthAfterCreationDate), "startDate", "inventor.chimpum.form.error.too-close",oneMonthAfterCreationDate);
		}

		if (!errors.hasErrors("endDate") && !errors.hasErrors("startDate")) {
			final Date oneWeekAfterStartDate = DateUtils.addWeeks(entity.getStartDate(), 1);
			errors.state(request, entity.getEndDate().equals(oneWeekAfterStartDate) || entity.getEndDate().after(oneWeekAfterStartDate), "endDate", "inventor.chimpum.form.error.insufficient-duration",oneWeekAfterStartDate);
		}

		if (!errors.hasErrors("budget")) {
			final Money budget = entity.getBudget();
			final boolean availableCurrency = this.validateAvailableCurrency(budget);
			errors.state(request, availableCurrency, "budget", "inventor.chimpum.form.error.budget-currency-not-available");

			final boolean budgetPositive = budget.getAmount() > 0.;
			errors.state(request, budgetPositive, "budget", "inventor.chimpum.form.error.budget-positive");

		}

	}

	@Override
	public void unbind(final Request<Chimpum> request, final Chimpum entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		final int id;
		final Item item;
		boolean published;

		id = request.getModel().getInteger("id");
		item = this.repository.findOneItemByChimpumId(id);
		published = item.isPublished();

		request.unbind(entity, model, "title", "description", "creationMoment", "startDate", "endDate", "budget", "moreInfo");
		model.setAttribute("published", published);
	}

	@Override
	public Chimpum findOne(final Request<Chimpum> request) {
		assert request != null;

		Chimpum result;
		int id;

		id = request.getModel().getInteger("id");
		result = this.repository.findOneChimpumById(id);

		return result;

	}

	@Override
	public void update(final Request<Chimpum> request, final Chimpum entity) {
		assert request != null;
		assert entity != null;

		this.repository.save(entity);
	}

	//Auxiliary methods

	private boolean validateAvailableCurrency(final Money budget) {

		final String currencies = this.scRepo.findAvailableCurrencies();
		final List<Object> listOfAvailableCurrencies = Arrays.asList((Object[]) currencies.split(";"));

		return listOfAvailableCurrencies.contains(budget.getCurrency());
	}

}
