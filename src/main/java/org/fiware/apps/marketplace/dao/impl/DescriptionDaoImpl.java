package org.fiware.apps.marketplace.dao.impl;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
 * Copyright (C) 2014-2015 CoNWeT Lab, Universidad Politécnica de Madrid
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
import org.fiware.apps.marketplace.dao.StoreDao;
import org.fiware.apps.marketplace.dao.UserDao;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.utils.MarketplaceHibernateDao;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("offeringsDescriptionDao")
public class DescriptionDaoImpl extends MarketplaceHibernateDao implements DescriptionDao {

	@Autowired private UserDao userDao;
	@Autowired private StoreDao storeDao;
	private final static String TABLE_NAME = Description.class.getName();

	/* @Override
	public void save(Description description) {
		getSession().saveOrUpdate(description);		
	}*/

	@Override
	public void update(Description description) {
		getSession().update(description);		
	}

	/*@Override
	public void delete(Description description) {
		getSession().delete(description);
	}*/

	@Override
	public Description findById(Integer id) throws DescriptionNotFoundException {
		Object res = getSession().get(Description.class, id);

		if (res == null) {
			throw new DescriptionNotFoundException("Description with ID " + id + " not found");
		}

		return (Description) res;
	}

	private Description findByQuery(String queryString, Object[] params) 
			throws DescriptionNotFoundException {

		Query query = getSession()
				.createQuery(queryString);

		for (int i = 0; i < params.length; i++) {
			query.setParameter(i, params[i]);
		}

		List<?> list = query.list();

		if (list.isEmpty()) {
			throw new DescriptionNotFoundException("Description " + params[0] + " not found");
		} else {
			return (Description) list.get(0);
		}
	}

	@Override
	public Description findByNameAndStore(String storeName, String descriptionName) 
			throws DescriptionNotFoundException, StoreNotFoundException {

		// Throws StoreNotFoundException if the Store does not exist
		storeDao.findByName(storeName);

		Object[] params  = {descriptionName , storeName};
		String query = String.format("from %s where name = ? and store.name = ?", TABLE_NAME);
		return this.findByQuery(query, params);				
	}

	@Override
	public boolean isNameAvailableInStore(String storeName, String name) {
		List<?> list = getSession()
				.createQuery(String.format("from %s where name = :name and store.name = :storeName", TABLE_NAME))
				.setParameter("name", name)
				.setParameter("storeName", storeName)
				.list();

		return list.isEmpty();
	}

	@Override
	public boolean isDisplayNameAvailableInStore(String storeName, String displayName) {
		List<?> list = getSession()
				.createQuery(String.format("from %s where displayName = :displayName and store.name = :storeName", 
						TABLE_NAME))
						.setParameter("displayName", displayName)
						.setParameter("storeName", storeName)
						.list();

		return list.isEmpty();
	}

	@Override
	public boolean isURLAvailableInStore(String storeName, String url) {
		List<?> list = getSession()
				.createQuery(String.format("from %s where url = :url and store.name = :storeName", TABLE_NAME))
				.setParameter("url", url)
				.setParameter("storeName", storeName)
				.list();

		return list.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Description> getUserDescriptions(String userName)
			throws UserNotFoundException {

		// Throws UserNotFoundException if the store does not exist
		userDao.findByName(userName);

		return getSession().createQuery(String.format("from %s where creator.userName = :userName", TABLE_NAME))
				.setParameter("userName", userName)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Description> getAllDescriptions() {
		return getSession()
				.createCriteria(Description.class)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Description> getDescriptionsPage(int offset, int max) {
		return getSession()
				.createCriteria(Description.class)
				.setFirstResult(offset)
				.setMaxResults(max)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)		// Avoid duplicates
				.list();
	}

	@Override
	public List<Description> getStoreDescriptions(String storeName) throws StoreNotFoundException {
		return getStoreDescriptionsPage(storeName, 0, Integer.MAX_VALUE);	
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Description> getStoreDescriptionsPage(String storeName, int offset, int max) 
			throws StoreNotFoundException {

		// Throws StoreNotFoundException if the store does not exist
		storeDao.findByName(storeName);

		return getSession().createQuery(String.format("from %s where store.name = :storeName", TABLE_NAME))
				.setParameter("storeName", storeName)
				.setFirstResult(offset)
				.setMaxResults(max)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Description> filterByUserNameAndStoreName(String userName, String storeName)
			throws UserNotFoundException, StoreNotFoundException {

		userDao.findByName(userName);
		storeDao.findByName(storeName);

		return getSession().createQuery(String.format("from %s where creator.userName = :userName and "
				+ "store.name = :storeName", TABLE_NAME))
				.setParameter("userName", userName)
				.setParameter("storeName", storeName)
				.list();
	}

}
