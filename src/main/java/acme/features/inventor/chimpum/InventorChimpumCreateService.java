
package acme.features.inventor.chimpum;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.components.CalculateMoneyExchange;
import acme.entities.Chimpum;
import acme.entities.Item;
import acme.entities.ItemType;
import acme.entities.MoneyExchangeCache;
import acme.entities.SystemConfiguration;
import acme.features.administrator.systemConfiguration.AdministratorSystemConfigurationRepository;
import acme.features.spam.SpamDetector;
import acme.forms.MoneyExchange;
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
		
		int id;
		Item item;

		id = request.getModel().getInteger("id");
		item = this.repository.findOneItemByChimpumId(id);
		return !item.isPublished();
	}

	@Override
	public Chimpum instantiate(final Request<Chimpum> request) {
		assert request != null;
		Item item;
		Inventor inventor;
		inventor = this.repository.findInventorById(request.getPrincipal().getActiveRoleId());
		item = new Item();
		final ItemType type = ItemType.valueOf((String) request.getModel().getAttribute("type"));
		item.setType(type);
		item.setInventor(inventor);
		return item;
	}

	public boolean validateAvailableCurrency(final Money budget) {

		final String currencies = this.scRepo.findAvailableCurrencies();
		final List<Object> listOfAvailableCurrencies = Arrays.asList((Object[]) currencies.split(";"));

		return listOfAvailableCurrencies.contains(budget.getCurrency());
	}

	@Override
	public void bind(final Request<Chimpum> request, final Chimpum entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "title", "technology", "code", "description", "retailPrice", "moreInfo");

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
			errors.state(request, spam1, "name", "inventor.chimpum.form.label.spam", "spam");
		}

//		if (!errors.hasErrors("code")) {
//			final Chimpum existing = this.repository.findOneChimpumByCode(entity.getCode());
//			errors.state(request, existing == null, "code", "inventor.item.form.error.duplicated");
//		}

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

		request.unbind(entity, model, "name", "type", "code", "technology", "description", "retailPrice", "convertedPrice", "moreInfo", "published");
	}

	@Override
	public void create(final Request<Chimpum> request, final Chimpum entity) {
		assert request != null;
		assert entity != null;

		final ItemType type = ItemType.valueOf((String) request.getModel().getAttribute("type"));
		entity.setType(type);
		entity.setPublished(false);

		final Money converted;
		Money source;
		String targetCurrency;
		final MoneyExchange exchange;
		final Date date;
		final Calendar today = Calendar.getInstance();

		source = entity.getRetailPrice();
		targetCurrency = this.repository.findBaseCurrency();

		if (!(entity.getRetailPrice().getCurrency().equals(targetCurrency))) {
			exchange = this.getConversion(source, targetCurrency);
			converted = exchange.getTarget();
			date = exchange.getDate();
		} else {
			converted = source;
			date = today.getTime();
		}
		entity.setConvertedPrice(converted);
		entity.setExchangeDate(date);
		this.repository.save(entity);
	}

	public MoneyExchange getConversion(final Money source, final String targetCurrency) {
		MoneyExchangeCache cache;
		MoneyExchange exchange;
		final Calendar date;

		date = Calendar.getInstance();

		final Optional<MoneyExchangeCache> opt = this.repository.findCacheBySourceAndTarget(source.getCurrency(), targetCurrency);
		if (opt.isPresent()) {
			cache = opt.get();
			if (Boolean.TRUE.equals(CalculateMoneyExchange.checkCache(cache)))
				exchange = CalculateMoneyExchange.calculateMoneyExchangeFromCache(source, targetCurrency, cache);
			else {
				exchange = CalculateMoneyExchange.computeMoneyExchange(source, targetCurrency);

				date.setTime(exchange.getDate());
				cache.setDate(date);
				cache.setRate(exchange.getRate());

				this.repository.save(cache);
			}
			return exchange;
		} else {
			exchange = CalculateMoneyExchange.computeMoneyExchange(source, targetCurrency);

			date.setTime(exchange.getDate());
			cache = new MoneyExchangeCache();
			cache.setDate(date);
			cache.setRate(exchange.getRate());
			cache.setSource(source.getCurrency());
			cache.setTarget(targetCurrency);

			this.repository.save(cache);

			return exchange;
		}
	}
}
