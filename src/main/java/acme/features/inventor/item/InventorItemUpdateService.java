
package acme.features.inventor.item;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.components.CalculateMoneyExchange;
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
import acme.framework.services.AbstractUpdateService;
import acme.roles.Inventor;

@Service

public class InventorItemUpdateService implements AbstractUpdateService<Inventor, Item> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected InventorItemRepository						repository;

	@Autowired
	protected AdministratorSystemConfigurationRepository	scRepo;

	// AbstractUpdateService<Inventor,Item> interface -----------------


	public boolean validateAvailableCurrencyRetailPrice(final Money retailPrice) {

		final String currencies = this.repository.findAvailableCurrencies();
		final List<Object> listOfAvailableCurrencies = Arrays.asList((Object[]) currencies.split(";"));
		return listOfAvailableCurrencies.contains(retailPrice.getCurrency());
	}

	@Override
	public void validate(final Request<Item> request, final Item entity, final Errors errors) {
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
			errors.state(request, spam1, "description", "inventor.item.form.label.spam", "spam");
		}

		if (entity.getName() != null && !entity.getName().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getName(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getName(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "name", "inventor.item.form.label.spam", "spam");
		}

		if (entity.getTechnology() != null && !entity.getTechnology().equals("")) {
			final boolean spam1 = SpamDetector.validateNoSpam(entity.getTechnology(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getTechnology(), strongSpam, sc.getStrongThreshold());
			errors.state(request, spam1, "technology", "inventor.item.form.label.spam", "spam");
		}
		
		if (!entity.getMoreInfo().equals("") && entity.getMoreInfo() != null) {
			final boolean spam2 = SpamDetector.validateNoSpam(entity.getMoreInfo(), weakSpam, sc.getWeakThreshold()) && SpamDetector.validateNoSpam(entity.getMoreInfo(), strongSpam, sc.getStrongThreshold());

			errors.state(request, spam2, "moreInfo", "inventor.item.form.label.spam", "spam");
		}

		if (!errors.hasErrors("code")) {

			final Item existing = this.repository.findOneItemByCode(entity.getCode());
			errors.state(request, existing.getId() == entity.getId(), "code", "inventor.item.form.error.duplicated");

		}

		if (!errors.hasErrors("retailPrice")) {

			final Money retailPrice = entity.getRetailPrice();
			final boolean availableCurrency = this.validateAvailableCurrencyRetailPrice(retailPrice);
			errors.state(request, availableCurrency, "retailPrice", "inventor.item.form.error.retail-price-currency-not-available");

			if (entity.getType().equals(ItemType.COMPONENT)) {
				final boolean retailPriceComponentPositive = retailPrice.getAmount() > 0.;
				errors.state(request, retailPriceComponentPositive, "retailPrice", "inventor.item.form.error.retail-price-component-positive");

			} else if (entity.getType().equals(ItemType.TOOL)) {
				final boolean retailPriceToolZeroOrPositive = retailPrice.getAmount() >= 0.;
				errors.state(request, retailPriceToolZeroOrPositive, "retailPrice", "inventor.item.form.error.retail-price-tool-zero-or-positive");

			}

		}

	}

	@Override
	public boolean authorise(final Request<Item> request) {
		assert request != null;
		boolean result;

		Item item;
		int id;
		
		id = request.getModel().getInteger("id");
		item = this.repository.findOneItemById(id);
		final Inventor inventor = item.getInventor();
		
		result = item.isPublished();
		
		return !result && request.isPrincipal(inventor);
	}

	@Override
	public void bind(final Request<Item> request, final Item entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		entity.setPublished(false);
		this.repository.save(entity);

		request.bind(entity, errors, "name", "code", "technology", "description", "retailPrice", "moreInfo");
	}

	@Override
	public void unbind(final Request<Item> request, final Item entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "name", "type", "code", "technology", "description", "retailPrice", "convertedPrice", "moreInfo", "published");
	}

	@Override
	public Item findOne(final Request<Item> request) {
		assert request != null;

		Item result;
		int id;

		id = request.getModel().getInteger("id");
		result = this.repository.findOneItemById(id);

		return result;

	}

	@Override
	public void update(final Request<Item> request, final Item entity) {
		assert request != null;
		assert entity != null;
		final Money converted;
		Money source;
		String targetCurrency;
		final MoneyExchange exchange;
		Calendar today;
		final Calendar exchangeDate = Calendar.getInstance();

		source = entity.getRetailPrice();
		targetCurrency = this.repository.findBaseCurrency();
		today = Calendar.getInstance();
		exchangeDate.setTime(entity.getExchangeDate());

		if (!(entity.getRetailPrice().getCurrency().equals(targetCurrency))) {
			exchange = this.getConversion(source, targetCurrency);
			converted = exchange.getTarget();
			entity.setExchangeDate(exchange.getDate());
		} else {
			converted = source;
			entity.setExchangeDate(today.getTime());
		}

		entity.setConvertedPrice(converted);
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
