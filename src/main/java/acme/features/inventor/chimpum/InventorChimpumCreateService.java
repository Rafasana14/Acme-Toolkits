
package acme.features.inventor.chimpum;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Chimpum;
import acme.entities.Item;
import acme.entities.ItemType;
import acme.entities.SystemConfiguration;
import acme.features.administrator.systemConfiguration.AdministratorSystemConfigurationRepository;
import acme.features.spam.SpamDetector;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.datatypes.Money;
import acme.framework.services.AbstractCreateService;
import acme.roles.Inventor;

@Service
public class InventorChimpumCreateService implements AbstractCreateService<Inventor, Chimpum> {

	@Autowired
	protected InventorChimpumRepository						repository;

	@Autowired
	protected AdministratorSystemConfigurationRepository	scRepo;


	@Override
	public boolean authorise(final Request<Chimpum> request) {
		assert request != null;

		final ItemType type = ItemType.COMPONENT; // Cambiar aquiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
		int id;
		Item item;

		id = request.getModel().getInteger("id");
		item = this.repository.findOneItemByChimpumId(id);
		return !item.isPublished() && item.getType().equals(type);
	}

	@Override
	public Chimpum instantiate(final Request<Chimpum> request) {
		assert request != null;
		final Chimpum chimpum;

		chimpum = new Chimpum();

		return chimpum;
	}

	@Override
	public void bind(final Request<Chimpum> request, final Chimpum entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "title", "code", "description", "startDate", "endDate", "budget", "moreInfo");

	}

	@Override
	public void validate(final Request<Chimpum> request, final Chimpum entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		final Calendar moment = Calendar.getInstance();
		final String finalCode;

		final Date now = new Date(System.currentTimeMillis() - 1);

		moment.setTime(now);

		final SystemConfiguration sc = this.scRepo.findSystemConfigurationById();
		final String[] parts = sc.getStrongSpam().split(";");
		final String[] parts2 = sc.getWeakSpam().split(";");
		final List<String> strongSpam = new LinkedList<>();
		final List<String> weakSpam = new LinkedList<>();
		Collections.addAll(strongSpam, parts);
		Collections.addAll(weakSpam, parts2);

		finalCode = this.generateCode(entity.getCode(), moment);
		entity.setCode(finalCode);
		entity.setCreationMoment(moment.getTime());

		if (entity.getDescription() != null && !entity.getDescription().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getDescription(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getDescription(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "description", "inventor.chimpum.form.label.spam", "spam");
		}

		if (entity.getTitle() != null && !entity.getTitle().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getTitle(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getTitle(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "name", "inventor.chimpum.form.label.spam", "spam");
		}

		//		if (!errors.hasErrors("code")) {
		//			final Chimpum existing = this.repository.findOneChimpumByCode(entity.getCode());
		//			errors.state(request, existing == null, "code", "inventor.item.form.error.duplicated");
		//		}

		if (!errors.hasErrors("startDate")) {
			errors.state(request, entity.getStartDate().after(entity.getCreationMoment()), "startDate", "inventor.chimpum.form.error.past-start-date");
		}
		if (!errors.hasErrors("startDate")) {
			final Date oneMonthAfterCreationDate = DateUtils.addMonths(entity.getCreationMoment(), 1);
			errors.state(request, entity.getStartDate().equals(oneMonthAfterCreationDate) || entity.getStartDate().after(oneMonthAfterCreationDate), "startDate", "inventor.chimpum.form.error.too-close");
		}

		if (!errors.hasErrors("endDate")) {
			errors.state(request, entity.getEndDate().after(entity.getCreationMoment()), "endDate", "inventor.chimpum.form.error.past-end-date");
		}
		if (!errors.hasErrors("endDate")) {
			errors.state(request, entity.getEndDate().after(entity.getStartDate()), "endDate", "inventor.chimpum.form.error.end-date-previous-to-start-date");
		}
		if (!errors.hasErrors("endDate")) {
			final Date oneWeekAfterStartDate = DateUtils.addWeeks(entity.getStartDate(), 1);
			errors.state(request, entity.getEndDate().equals(oneWeekAfterStartDate) || entity.getEndDate().after(oneWeekAfterStartDate), "endDate", "inventor.chimpum.form.error.insufficient-duration");
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

		request.unbind(entity, model, "title", "code", "description", "startDate", "endDate", "budget", "moreInfo");
	}

	@Override
	public void create(final Request<Chimpum> request, final Chimpum entity) {
		assert request != null;
		assert entity != null;

		int masterId;
		Item item;

		masterId = request.getModel().getInteger("masterId");
		item = this.repository.findOneItemById(masterId);

		this.repository.save(entity);
		item.setChimpum(entity);
		this.repository.save(item);
	}

	//Auxiliary methods

	private boolean validateAvailableCurrency(final Money budget) {

		final String currencies = this.scRepo.findAvailableCurrencies();
		final List<Object> listOfAvailableCurrencies = Arrays.asList((Object[]) currencies.split(";"));

		return listOfAvailableCurrencies.contains(budget.getCurrency());
	}

	private String generateCode(final String code, final Calendar moment) {
		final String yy = String.valueOf(moment.get(Calendar.YEAR));
		final String mm = String.valueOf(moment.get(Calendar.MONTH));
		final String dd = String.valueOf(moment.get(Calendar.DAY_OF_MONTH));

		return code + "-" + yy + "/" + mm + "/" + dd;
	}

}
