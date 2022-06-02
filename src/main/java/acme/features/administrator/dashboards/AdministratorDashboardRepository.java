package acme.features.administrator.dashboards;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.framework.repositories.AbstractRepository;

@Repository
public interface AdministratorDashboardRepository extends AbstractRepository{
	
	
	//Components
	
	@Query("select count(i) from Item i where i.type = 'COMPONENT'")
	Integer totalNumOfComponents();
	
	@Query("select avg(i.retailPrice.amount),technology,i.retailPrice.currency  from Item i where i.type = 'COMPONENT' GROUP BY i.retailPrice.currency, technology")
	List<Object[]> averagePriceOfComponents();
	
	@Query("select max(i.retailPrice.amount),technology,i.retailPrice.currency from Item i where i.type = 'COMPONENT' GROUP BY i.retailPrice.currency, technology")
	List<Object[]> maxPriceOfComponents();
	
	@Query("select min(i.retailPrice.amount),technology,i.retailPrice.currency from Item i where i.type = 'COMPONENT' GROUP BY i.retailPrice.currency, technology")
	List<Object[]> minPriceOfComponents();
	
	@Query("select stddev(i.retailPrice.amount),technology,i.retailPrice.currency from Item i where i.type = 'COMPONENT' GROUP BY i.retailPrice.currency, technology")
	List<Object[]> deviationPriceOfComponents();
	
	
	//Tools
	
	@Query("select count(i) from Item i where i.type = 'TOOL'")
	Integer totalNumOfTools();
	
	@Query("select avg(i.retailPrice.amount),i.retailPrice.currency from Item i where i.type = 'TOOL' GROUP BY i.retailPrice.currency")
	List<Object[]> averagePriceOfTools();
	
	@Query("select max(i.retailPrice.amount),i.retailPrice.currency from Item i where i.type = 'TOOL' GROUP BY i.retailPrice.currency")
	List<Object[]> maxPriceOfTools();
	
	@Query("select min(i.retailPrice.amount),i.retailPrice.currency from Item i where i.type = 'TOOL' GROUP BY i.retailPrice.currency")
	List<Object[]> minPriceOfTools();
	
	@Query("select stddev(i.retailPrice.amount),i.retailPrice.currency from Item i where i.type = 'TOOL' GROUP BY i.retailPrice.currency")
	List<Object[]> deviationPriceOfTools();
	
	
	//Patronages
	
	@Query("select count(p) from Patronage p where p.status = acme.entities.PatronageStatus.PROPOSED ")
	Integer  numberOfProposedPatronages();
	
	@Query("select count(p) from Patronage p where p.status = acme.entities.PatronageStatus.ACCEPTED ")
	Integer  numberOfAcceptedPatronages();
	
	@Query("select count(p) from Patronage p where p.status = acme.entities.PatronageStatus.DENIED ")
	Integer  numberOfDeniedPatronages();
	
	
	
	@Query("select avg(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.PROPOSED GROUP BY p.status")
	Double averageBudgetProposedPatronages();
	
	@Query("select avg(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.ACCEPTED GROUP BY p.status")
	Double averageBudgetAcceptedPatronages();
	
	@Query("select avg(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.DENIED GROUP BY p.status")
	Double averageBudgetDeniedPatronages();
	
	
	
	@Query("select stddev(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.PROPOSED ")
	Double deviationBudgetProposedPatronages();

	@Query("select stddev(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.ACCEPTED ")
	Double deviationBudgetAcceptedPatronages();
	
	@Query("select stddev(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.DENIED ")
	Double deviationBudgetDeniedPatronages();
	
	
	
	@Query("select min(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.PROPOSED ")
	Double minBudgetProposedPatronages();
	
	@Query("select min(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.ACCEPTED ")
	Double minBudgetAcceptedPatronages();
	
	@Query("select min(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.DENIED ")
	Double minBudgetDeniedPatronages();
	

	
	@Query("select max(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.PROPOSED ")
	Double maxBudgetProposedPatronages();
	
	@Query("select max(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.ACCEPTED ")
	Double maxBudgetAcceptedPatronages();
	
	@Query("select max(p.budget.amount) from Patronage p where p.status = acme.entities.PatronageStatus.DENIED ")
	Double maxBudgetDeniedPatronages();
	
	//Chimpums
	//Ratio
	
	@Query("select count(i.chimpum) from Item i where i.chimpum.id != null ")
	Integer getArtefactWithChimpum();
	
	@Query("select count(i) from Item i") // where type==Artefact")
	Integer getTotalArtefacts();
	
	@Query("select avg(i.chimpum.budget.amount),i.chimpum.budget.currency from Item i "/** where i.type = 'COMPONENT'*/+ "GROUP BY i.chimpum.budget.currency")
	List<Object[]> averageBudgetOfChimpums();
	
	@Query("select max(i.chimpum.budget.amount),i.chimpum.budget.currency from Item i "/** where i.type = 'COMPONENT'*/+ "GROUP BY i.chimpum.budget.currency")
	List<Object[]> maxBudgetOfChimpums();
	
	@Query("select min(i.chimpum.budget.amount),i.chimpum.budget.currency from Item i "/** where i.type = 'COMPONENT'*/+ "GROUP BY i.chimpum.budget.currency")
	List<Object[]> minBudgetOfChimpums();
	
	@Query("select stddev(i.chimpum.budget.amount),i.chimpum.budget.currency from Item i "/** where i.type = 'COMPONENT'*/+ "GROUP BY i.chimpum.budget.currency")
	List<Object[]> deviationBudgetOfChimpums();
	
}
