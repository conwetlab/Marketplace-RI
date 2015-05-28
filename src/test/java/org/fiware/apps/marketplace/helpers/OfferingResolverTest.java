package org.fiware.apps.marketplace.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.PriceComponent;
import org.fiware.apps.marketplace.model.PricePlan;
import org.fiware.apps.marketplace.rdf.RdfHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.hp.hpl.jena.rdf.model.Model;

public class OfferingResolverTest {
	
	private final static String DESCRIPTION_URI = "http://127.0.0.1:7777/usdl.rdf";
	
	private Model model;
	
	@Mock private RdfHelper rdfHelperMock;
	@InjectMocks private OfferingResolver offeringResolver;
	
	@Before 
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		this.model = mock(Model.class);
		when(rdfHelperMock.getModelFromUri(DESCRIPTION_URI)).thenReturn(model);

	}
	
	private void checkOfferingBasic(Offering offering, String offeringName, String title,
			String description, String version, String image, String uri,
			Description describedIn) {
		
		assertThat(offering.getName()).isEqualTo(offeringName);
		assertThat(offering.getDisplayName()).isEqualTo(title);
		assertThat(offering.getDescription()).isEqualTo(description);
		assertThat(offering.getImageUrl()).isEqualTo(image);
		assertThat(offering.getVersion()).isEqualTo(version);
		assertThat(offering.getUri()).isEqualTo(uri);
		assertThat(offering.getDescribedIn()).isEqualTo(describedIn);
	}
	
	private void checkPricePlan(PricePlan pricePlan, String title, String description) {
		assertThat(pricePlan.getTitle()).isEqualTo(title);
		assertThat(pricePlan.getComment()).isEqualTo(description);
	}
	
	private void checkPricePlanInSet(Set<PricePlan> pricePlans, String title, String description) {
		
		boolean found = false;
		Iterator<PricePlan> iterator = pricePlans.iterator();
		
		while (!found && iterator.hasNext()) {
			PricePlan pricePlan = iterator.next();
			
			if (pricePlan.getTitle().equals(title) && pricePlan.getComment().equals(description)) {
				found = true;
			}
		}
		
		assertThat(found).isTrue();
	}
	
	private void checkPriceComponent(PriceComponent priceComponent, String currency, String title, String unit,
			float value) {
		assertThat(priceComponent.getCurrency()).isEqualTo(currency);
		assertThat(priceComponent.getTitle()).isEqualTo(title);
		assertThat(priceComponent.getUnit()).isEqualTo(unit);
		assertThat(priceComponent.getValue()).isEqualTo(value);
	}
	
	private void initOffering(String uri, String title, String description, String version, String image) {
		
		
		List<String> offeringsUris = rdfHelperMock.queryUris(model, RdfHelper.getQueryPrefixes() + 
				"SELECT ?x WHERE { ?x a usdl:ServiceOffering . } ", "x");
		
		// Initialize offeringsUris (in case they are not) and add the new URI...
		offeringsUris = offeringsUris == null ? new ArrayList<String>() : offeringsUris;
		offeringsUris.add(uri);
		
		// Update mock
		when(rdfHelperMock.queryUris(model, RdfHelper.getQueryPrefixes() + 
				"SELECT ?x WHERE { ?x a usdl:ServiceOffering . } ", "x")).thenReturn(offeringsUris);
		
		// Mock values for this offering
		when(rdfHelperMock.getLiteral(model, uri, "dcterms:title")).thenReturn(title);
		when(rdfHelperMock.getLiteral(model, uri, "dcterms:description")).thenReturn(description);
		when(rdfHelperMock.getLiteral(model, uri, "usdl:versionInfo")).thenReturn(version);
		when(rdfHelperMock.getObjectUri(model, uri, "foaf:thumbnail")).thenReturn(image);

	}
	
	private void initPricePlan(String offeringUri, String pricePlanUri, String title, String description) {
		
		List<String> offeringPricePlansUris = rdfHelperMock.getObjectUris(model, offeringUri, "usdl:hasPricePlan");
		
		// Initialize pricePlansUris (in case they are not) and add the new URI...
		offeringPricePlansUris = offeringPricePlansUris == null ? new ArrayList<String>() : offeringPricePlansUris;
		offeringPricePlansUris.add(pricePlanUri);		
		
		// Update mock
		when(rdfHelperMock.getObjectUris(model, offeringUri, "usdl:hasPricePlan"))
			.thenReturn(offeringPricePlansUris);
		
		// Mock values for this price plan 
		when(rdfHelperMock.getLiteral(model, pricePlanUri, "dcterms:title")).thenReturn(title);
		when(rdfHelperMock.getLiteral(model, pricePlanUri, "dcterms:description")).thenReturn(description);
	}
	
	private void initPriceComponent(String pricePlanUri, String priceComponentUri, String title,
			String currency, String unit, float value) {
		
		List<String> pricePlanComponentsUris = rdfHelperMock.getObjectUris(model, pricePlanUri, "usdl:hasPricePlan");
		
		// Initialize priceComponentsUris (in case they are not) and add the new URI...
		pricePlanComponentsUris = pricePlanComponentsUris == null ? new ArrayList<String>() : pricePlanComponentsUris;
		pricePlanComponentsUris.add(priceComponentUri);
		
		// Update mock
		when(rdfHelperMock.getObjectUris(model, pricePlanUri, "price:hasPriceComponent"))
			.thenReturn(pricePlanComponentsUris);
		
		// Mock values for this price component
		when(rdfHelperMock.getLiteral(model, priceComponentUri, "dcterms:title"))
				.thenReturn(title);
		when(rdfHelperMock.getLiteral(model, priceComponentUri, "gr:hasCurrency"))
				.thenReturn(currency);
		when(rdfHelperMock.getLiteral(model, priceComponentUri, "gr:hasUnitOfMeasurement"))
				.thenReturn(unit);
		when(rdfHelperMock.getLiteral(model, priceComponentUri, "gr:hasCurrencyValue"))
				.thenReturn(new Float(value).toString());

	}
	
	@Test
	public void testSimpleDescription() throws Exception {
		
		String offeringTitle = "cool offering";
		String offeringUri = "https://store.lab.fiware.org/offerings/offering1";
		String offeringName = offeringTitle.replaceAll(" ", "-");
		String offeringDesc = "a very long long description for the best offering";
		String offeringVersion = "1.2";
		String offeringImg = "https://store.lab.fiware.org/static/img1.png";
		String pricePlanUri = "https://store.lab.fiware.org/offerings/offering1#priceplan1";
		String pricePlanTitle = "price plan";
		String pricePlanDesc = "a description for the price plan";
		String priceComponentUri = "https://store.lab.fiware.org/offerings/offering1#pricecomponent1";
		String priceComponentTitle = "price component";
		String priceComponentCurrency = "EUR (€)";
		String priceComponentUnit = "bytes/month";
		float priceComponentValue = 1.23F;
		
		// Mocking		
		initOffering(offeringUri, offeringTitle, offeringDesc, offeringVersion, offeringImg);
		initPricePlan(offeringUri, pricePlanUri, pricePlanTitle, pricePlanDesc);
		initPriceComponent(pricePlanUri, priceComponentUri, priceComponentTitle, priceComponentCurrency, 
				priceComponentUnit, priceComponentValue);
		
		// Call the function
		Description description = mock(Description.class);
		when(description.getUrl()).thenReturn(DESCRIPTION_URI);
		List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);
		
		// CHeck returned offering
		assertThat(offerings.size()).isEqualTo(1);
		Offering offering = offerings.get(0);
		checkOfferingBasic(offering, offeringName, offeringTitle, offeringDesc, offeringVersion, offeringImg, 
				offeringUri, description);
		
		// Check returned price plan
		Set<PricePlan> pricePlans = offering.getPricePlans();
		assertThat(pricePlans.size()).isEqualTo(1);
		PricePlan pricePlan = (PricePlan) pricePlans.toArray()[0];
		checkPricePlan(pricePlan, pricePlanTitle, pricePlanDesc);
		
		// Check price component
		Set<PriceComponent> priceComponents = pricePlan.getPriceComponents();
		assertThat(priceComponents.size()).isEqualTo(1);
		PriceComponent priceComponent = (PriceComponent) priceComponents.toArray()[0];
		checkPriceComponent(priceComponent, priceComponentCurrency, priceComponentTitle, priceComponentUnit, 
				priceComponentValue);
		
				
	}
	
	@Test
	public void testDescriptionWithTwoOfferings() {
		
		String[] offeringsUris = {"https://store.lab.fiware.org/offerings/offering1",
				"https://store.lab.fiware.org/offerings/offering2"};
		String[] offeringsTitles = {"cool offering", "bad offering"};
		String[] offeringsDescs = {"a very long long description for the best offering", 
				"another long description" };
		String[] offeringsVersions = {"1.2", "2.4"};
		String[] offeringsImgs = {"https://store.lab.fiware.org/static/img1.png",
				"https://store.lab.fiware.org/static/img2.png"};
		
		// Mocking		
		for (int i = 0; i < offeringsUris.length; i++) {
			initOffering(offeringsUris[i], offeringsTitles[i], offeringsDescs[i], 
					offeringsVersions[i], offeringsImgs[i]);
		}
		
		// Call the function
		Description description = mock(Description.class);
		when(description.getUrl()).thenReturn(DESCRIPTION_URI);
		List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);
		
		// Check the results
		assertThat(offerings.size()).isEqualTo(2);
		
		for (int i = 0; i < offerings.size(); i++) {
			checkOfferingBasic(offerings.get(i), offeringsTitles[i].replace(" ", "-"), offeringsTitles[i], 
					offeringsDescs[i], offeringsVersions[i], offeringsImgs[i], offeringsUris[i], description);
		}
	}
	
	@Test
	public void testOfferingWithTwoPricePlans() {
		String offeringTitle = "cool offering";
		String offeringUri = "https://store.lab.fiware.org/offerings/offering1";
		String offeringDesc = "a very long long description for the best offering";
		String offeringVersion = "1.2";
		String offeringImg = "https://store.lab.fiware.org/static/img1.png";
		
		String[] pricePlansUris = {"https://store.lab.fiware.org/offerings/offering1#priceplan1", 
				"https://store.lab.fiware.org/offerings/offering1#priceplan2"};
		String[] pricePlansTitles = {"price plan 1", "price plan 2"};
		String[] pricePlansDescs = {"a description for price plan 1", "a brief desc for price plan 2" };
		
		// Init offerings and price plans
		initOffering(offeringUri, offeringTitle, offeringDesc, offeringVersion, offeringImg);
		for (int i = 0; i < pricePlansUris.length; i++) {
			initPricePlan(offeringUri, pricePlansUris[i], pricePlansTitles[i], pricePlansDescs[i]);
		}
		
		// Call the function
		Description description = mock(Description.class);
		when(description.getUrl()).thenReturn(DESCRIPTION_URI);
		List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);

		// Check offering and its price plans
		assertThat(offerings.size()).isEqualTo(1);
		assertThat(offerings.get(0).getPricePlans()).hasSize(2);
		
		for (int i = 0; i < pricePlansTitles.length; i++) {
			checkPricePlanInSet(offerings.get(0).getPricePlans(), pricePlansTitles[i], pricePlansDescs[i]);
		}
	}
}
