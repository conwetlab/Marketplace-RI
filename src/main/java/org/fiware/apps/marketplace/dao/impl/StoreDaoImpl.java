package org.fiware.apps.marketplace.dao.impl;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
 * Copyright (C) 2014 CoNWeT Lab, Universidad Politécnica de Madrid
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

import org.fiware.apps.marketplace.dao.StoreDao;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.utils.MarketplaceHibernateDao;
import org.springframework.stereotype.Repository;

@Repository("storeDao")
public class StoreDaoImpl extends MarketplaceHibernateDao implements StoreDao {

	@Override
	public void save(Store store) {
		getSession().saveOrUpdate(store);	
	}
	
	@Override
	public void update(Store store) {
		getSession().update(store);
	}

	@Override
	public void delete(Store store) {
		getSession().delete(store);
	}

	@Override
	public Store findByName(String name) throws StoreNotFoundException {	
		List<?> list = getSession()
				.createQuery("from Store where name=:name")
				.setParameter("name", name)
				.list();	
		
		if (list.isEmpty()){
			throw new StoreNotFoundException("Store " + name + " not found");
		} else {
			return (Store) list.get(0);
		}
			
	}
	
    @Override
    public boolean isNameAvailable(String name) {
		List<?> list = getSession()
				.createQuery("from Store where name=:name")
				.setParameter("name", name)
				.list();
		
		return list.isEmpty();
    }
    
    @Override
    public boolean isDisplayNameAvailable(String displayName) {
		List<?> list = getSession()
				.createQuery("from Store where displayName=:displayName")
				.setParameter("displayName", displayName)
				.list();
		
		return list.isEmpty();
    }
    
	@Override
	public boolean isURLAvailable(String url) {
		List<?> list = getSession()
				.createQuery("from Store where url=:url")
				.setParameter("url", url)
				.list();
		
		return list.isEmpty();
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Store> getStoresPage(int offset, int max) {
		return getSession()
				.createCriteria(Store.class)
				.setFirstResult(offset)
				.setMaxResults(max)
				.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List <Store> getAllStores() {
		return getSession().createCriteria(Store.class).list();
	}
}
