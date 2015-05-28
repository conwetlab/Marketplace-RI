package org.fiware.apps.marketplace.dao.impl;

/*
 * #%L
 * FiwareMarketplace
 * %%
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

import java.util.List;

import org.fiware.apps.marketplace.dao.DescriptionDao;
import org.fiware.apps.marketplace.dao.OfferingDao;
import org.fiware.apps.marketplace.dao.StoreDao;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.OfferingNotFoundException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.utils.MarketplaceHibernateDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("offeringDao")
public class OfferingDaoImpl extends MarketplaceHibernateDao implements OfferingDao  {
	
	@Autowired private StoreDao storeDao;
	@Autowired private DescriptionDao descriptionDao;
	
	private static final String TABLE_NAME = Offering.class.getName();

	@Override
	public void save(Offering offering) {
		getSession().save(offering);
	}

	@Override
	public void update(Offering offering) {
		getSession().update(offering);
	}

	@Override
	public void delete(Offering offering) {
		getSession().delete(offering);
	}

	@Override
	public Offering findDescriptionByNameStoreAndDescription(String storeName, 
			String descriptionName, String offeringName) throws OfferingNotFoundException, 
			StoreNotFoundException, DescriptionNotFoundException{
		
		// Throw exceptions if the Store or the Description does not exist
		storeDao.findByName(storeName);
		descriptionDao.findByNameAndStore(storeName, descriptionName);
		
		// Get the Offering
		List<?> offerings = getSession().createQuery("from " + TABLE_NAME + " WHERE "
				+ "describedIn.name = :descriptionName AND describedIn.store.name = :storeName "
				+ "AND name = :offeringName")
				.setParameter("storeName", storeName)
				.setParameter("descriptionName", descriptionName)
				.setParameter("offeringName", offeringName)
				.list();
		
		if (offerings.isEmpty()) {
			throw new OfferingNotFoundException(String.format("Offering %s not found in "
					+ "description %s (Store: %s)", offeringName, descriptionName, storeName));
		} else {
			return (Offering) offerings.get(0);
		}
	}

	@Override
	public List<Offering> getAllOfferings() {
		return getOfferingsPage(0, Integer.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Offering> getOfferingsPage(int offset, int max) {
		// Avoid Hibernate Null Pointer Exception
		return getSession().createQuery("FROM " + TABLE_NAME)
				.setFirstResult(offset)
				.setMaxResults(max)
				.list();
	}

	@Override
	public List<Offering> getAllStoreOfferings(String storeName) 
			throws StoreNotFoundException {
		return getStoreOfferingsPage(storeName, 0, Integer.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Offering> getStoreOfferingsPage(String storeName, int offset,	
			int max) throws StoreNotFoundException {
		
		// Throw exceptions if the Store or the Description does not exist
		storeDao.findByName(storeName);
		
		// Get the offerings
		return getSession().createQuery("FROM " + TABLE_NAME + " WHERE describedIn.store.name = :storeName")
				.setParameter("storeName", storeName)
				.setFirstResult(offset)
				.setMaxResults(max)
				.list();
	}

	@Override
	public List<Offering> getAllDescriptionOfferings(String storeName, String descriptionName) 
			throws StoreNotFoundException, DescriptionNotFoundException {
		return getDescriptionOfferingsPage(storeName, descriptionName, 0, Integer.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Offering> getDescriptionOfferingsPage(String storeName, String descriptionName,
			int offset, int max) throws StoreNotFoundException, DescriptionNotFoundException {
		
		// Throw exceptions if the Store or the Description does not exist
		storeDao.findByName(storeName);
		descriptionDao.findByNameAndStore(storeName, descriptionName);
		
		// Get the offerings
		return getSession().createQuery("FROM " + TABLE_NAME + " "
				+ "WHERE describedIn.name = :descriptionName AND describedIn.store.name = :storeName")
				.setParameter("descriptionName", descriptionName)
				.setParameter("storeName", storeName)
				.setFirstResult(offset)
				.setMaxResults(max)
				.list();
	}

}
