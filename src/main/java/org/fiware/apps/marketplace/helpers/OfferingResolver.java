package org.fiware.apps.marketplace.helpers;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
 * Copyright (C) 2015 CoNWeT Lab, Universidad Politécnica de Madrid
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holders nor the names of its contributors
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.PriceComponent;
import org.fiware.apps.marketplace.model.PricePlan;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.rdf.RdfHelper;
import org.fiware.apps.marketplace.utils.NameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Class to resolve offerings from store or service instances.
 * 
 * @author D058352
 *
 */
@Service("offeringResolver")
public class OfferingResolver {
	
	@Autowired private RdfHelper rdfHelper;
	
	/**
	 * Gets all the offerings from a USDL
	 * @param model The USDL
	 * @return The offerings list contained in the USDL
	 */
	private List<String> getOfferingUris(Model model) {
		String query = RdfHelper.getQueryPrefixes() + "SELECT ?x WHERE { ?x a usdl:ServiceOffering . } ";
		return rdfHelper.queryUris(model, query, "x");
	}
 
	/**
	 * Gets all the services associated with the offering
	 * @param offeringUri The offering whose services want to be retrieved
	 * @param model The UDSL
	 * @return The list of services associated with the offering
	 */
	private List<String> getServiceUris(Model model, String offeringUri) {
		return rdfHelper.getObjectUris(model, offeringUri, "usdl:includes");
	}

	/**
	 * Gets all the price plans associated with the offering
	 * @param offeringUri The offering whose price plans want to be retrieved
	 * @param model The UDSL
	 * @return The list of price plans associated with the offering
	 */
	private List<String> getPricePlanUris(Model model, String offeringUri) {
		return rdfHelper.getObjectUris(model, offeringUri, "usdl:hasPricePlan");
	}
	
	private List<String> getPriceComponentsUris(Model model, String pricePlanUri) {
		return rdfHelper.getObjectUris(model, pricePlanUri, "price:hasPriceComponent");
	}
	
	/**
	 * Gets the title of an entity
	 * @param model USDL
	 * @param offeringUri The entity URI whose title wants to be retrieved
	 * @return The title of the offering
	 */
	private String getTitle(Model model, String offeringUri) {
		return rdfHelper.getLiteral(model, offeringUri, "dcterms:title");
	}
	
	/**
	 * Gets the description of an entity
	 * @param model USDL
	 * @param offeringUri The entity URI whose description wants to be retrieved
	 * @return The description of the offering
	 */
	private String getDescription(Model model, String offeringUri) {
		return rdfHelper.getLiteral(model, offeringUri, "dcterms:description");
	}
	
	/**
	 * Get the version of an offering 
	 * @param model USDL
	 * @param offeringUri The offering URI whose version wants to be retrieved
	 * @return The version of the offering
	 */
	private String getOfferingVersion(Model model, String offeringUri) {
		return rdfHelper.getLiteral(model, offeringUri, "usdl:versionInfo");
	}
	
	/**
	 * Gets the image of an offering
	 * @param model USDL
	 * @param offeringUri The offering URI whose image URL wants to be retrieved
	 * @return The image URL of the offering
	 */
	private String getOfferingImageUrl(Model model, String offeringUri) {
		return rdfHelper.getObjectUri(model, offeringUri, "foaf:thumbnail");
	}
	
	/**
	 * Gets all offerings contained in the service descriptions in the given list of stores.
	 * @param stores
	 * @return
	 */
	public List<Offering> resolveOfferingsFromStores(List<Store> stores) {
		List<Offering> offerings = new ArrayList<Offering>();
		for (Store store : stores) {
			offerings.addAll(resolveOfferingsFromStore(store));
		}
		return offerings;
	}

	/**
	 * Gets all offerings contained in the service descriptions in the given store.
	 * @param store
	 * @return
	 */
	public List<Offering> resolveOfferingsFromStore(Store store) {
		return resolveOfferingsFromServiceDescriptions(store.getDescriptions());
	}

	/**
	 * Gets all offerings contained in the given service descriptions.
	 * @param offeringDescriptions
	 * @return
	 */
	public List<Offering> resolveOfferingsFromServiceDescriptions(List<Description> offeringDescriptions) {
		List<Offering> offerings = new ArrayList<Offering>();
		for (Description service : offeringDescriptions) {
			offerings.addAll(resolveOfferingsFromServiceDescription(service));
		}
		return offerings;
	}

	/**
	 * Gets all offerings contained in the file in the given URI.
	 * @param uri
	 * @return
	 */
	public List<Offering> resolveOfferingsFromServiceDescription(Description offeringDescription) {
		
		Model model = rdfHelper.getModelFromUri(offeringDescription.getUrl());
		
		// Just in case the model cannot be processed
		if (model == null) {
			return Collections.emptyList();
		}

		List<Offering> offerings = new ArrayList<Offering>();
		List<String> offeringUris = getOfferingUris(model);
		
		for (String offeringUri : offeringUris) {
			
			Offering offering = new Offering();
			offering.setDisplayName(getTitle(model, offeringUri));
			// Maybe the name should depends on the creator and the version...
			offering.setName(NameGenerator.getURLName(offering.getDisplayName()));
			offering.setUri(offeringUri);
			offering.setDescribedIn(offeringDescription);
			offering.setVersion(getOfferingVersion(model, offeringUri));
			offering.setDescription(getDescription(model, offeringUri));
			offering.setImageUrl(getOfferingImageUrl(model, offeringUri));
			
			// Set price plans (offerings have one or more price plans)
			List<String> pricePlansUris = getPricePlanUris(model, offeringUri);
			Set<PricePlan> pricePlans = new HashSet<>();
			
			for (String pricePlanUri: pricePlansUris) {
				
				PricePlan pricePlan = new PricePlan();
				pricePlan.setTitle(getTitle(model, pricePlanUri));
				pricePlan.setComment(getDescription(model, pricePlanUri));
				pricePlan.setOffering(offering);
				
				List<String> priceComponentsUris = getPriceComponentsUris(model, pricePlanUri);
				Set<PriceComponent> priceComponents = new HashSet<>();
				
				for (String priceComponentUri: priceComponentsUris) {
			
					PriceComponent priceComponent = new PriceComponent();
					priceComponent.setPricePlan(pricePlan);
					priceComponent.setTitle(getTitle(model, priceComponentUri));
					priceComponent.setComment(getDescription(model, priceComponentUri));
					priceComponent.setCurrency(rdfHelper.getLiteral(model, priceComponentUri, "gr:hasCurrency"));
					priceComponent.setUnit(rdfHelper.getLiteral(model, priceComponentUri, "gr:hasUnitOfMeasurement"));
					priceComponent.setValue(Float.parseFloat(
							rdfHelper.getLiteral(model, priceComponentUri, "gr:hasCurrencyValue")));
					
					priceComponents.add(priceComponent);
				}
				
				// Update price components with the retrieved price components
				pricePlan.setPriceComponents(priceComponents);
				
				pricePlans.add(pricePlan);
			}
			
			// Update the price plans set with the retrieved price plans
			offering.setPricePlans(pricePlans);
			
			offerings.add(offering);
		}
		return offerings;
	}
}
