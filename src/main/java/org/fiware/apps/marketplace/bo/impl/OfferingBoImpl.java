package org.fiware.apps.marketplace.bo.impl;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2012 SAP
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

import org.fiware.apps.marketplace.bo.DescriptionBo;
import org.fiware.apps.marketplace.bo.OfferingBo;
import org.fiware.apps.marketplace.bo.StoreBo;
import org.fiware.apps.marketplace.bo.UserBo;
import org.fiware.apps.marketplace.dao.OfferingDao;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.OfferingNotFoundException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.model.User;
import org.fiware.apps.marketplace.security.auth.OfferingAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("offeringBo")
public class OfferingBoImpl implements OfferingBo {
	
	@Autowired private OfferingAuth offeringAuth;
	@Autowired private OfferingDao offeringDao;
	@Autowired private UserBo userBo;
	@Autowired private StoreBo storeBo;
	@Autowired private DescriptionBo descriptionBo;

	@Override
	@Transactional(readOnly = false)
	public void save(Offering offering) throws NotAuthorizedException {
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canCreate(offering)) {
			throw new NotAuthorizedException("create offering");
		}
		
		offeringDao.save(offering);
	}

	@Override
	@Transactional(readOnly = false)
	public void update(Offering offering) throws NotAuthorizedException {
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canUpdate(offering)) {
			throw new NotAuthorizedException("update offering");
		}		offeringDao.update(offering);
	}

	@Override
	@Transactional(readOnly = false)
	public void delete(Offering offering) throws NotAuthorizedException {
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canDelete(offering)) {
			throw new NotAuthorizedException("delete offering");
		}
		offeringDao.delete(offering);
	}

	@Override
	@Transactional
	public Offering findByUri(String uri) throws NotAuthorizedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public Offering findOfferingByNameStoreAndDescription(String storeName, 
			String descriptionName, String offeringName)
			throws NotAuthorizedException, OfferingNotFoundException, 
			StoreNotFoundException, DescriptionNotFoundException {
		
		Offering offering = offeringDao.findDescriptionByNameStoreAndDescription(storeName, 
				descriptionName, offeringName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canGet(offering)) {
			throw new NotAuthorizedException("find offering");
		}
		
		return offering;
	}

	@Override
	@Transactional
	public List<Offering> getAllOfferings() throws NotAuthorizedException {
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canList()) {
			throw new NotAuthorizedException("list offerings");
		}
		
		return offeringDao.getAllOfferings();
	}

	@Override
	@Transactional
	public List<Offering> getOfferingsPage(int offset, int max) 
			throws NotAuthorizedException {
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canList()) {
			throw new NotAuthorizedException("list offerings");
		}
		
		return offeringDao.getOfferingsPage(offset, max);
	}

	@Override
	@Transactional
	public List<Offering> getAllStoreOfferings(String storeName) 
			throws StoreNotFoundException, NotAuthorizedException {
		
		Store store = storeBo.findByName(storeName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canList(store)) {
			throw new NotAuthorizedException("list offerings in store " + store.getName());
		}
		
		return offeringDao.getAllStoreOfferings(storeName);
	}

	@Override
	@Transactional
	public List<Offering> getStoreOfferingsPage(String storeName, int offset,
			int max) throws StoreNotFoundException, NotAuthorizedException {
		
		Store store = storeBo.findByName(storeName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canList(store)) {
			throw new NotAuthorizedException("list offerings in store " + store.getName());
		}
		
		return offeringDao.getStoreOfferingsPage(storeName, offset, max);
	}

	@Override
	@Transactional
	public List<Offering> getAllDescriptionOfferings(String storeName, String descriptionName) 
			throws StoreNotFoundException, DescriptionNotFoundException, NotAuthorizedException {
		
		Description description = descriptionBo.findByNameAndStore(storeName, descriptionName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canList(description)) {
			throw new NotAuthorizedException("list offerings in description " + description.getName());
		}
		
		return offeringDao.getAllDescriptionOfferings(storeName, descriptionName);
	}

	@Override
	@Transactional
	public List<Offering> getDescriptionOfferingsPage(String storeName, 
			String descriptionName, int offset, int max) throws 
			StoreNotFoundException, DescriptionNotFoundException, 
			NotAuthorizedException {
		
		Description description = descriptionBo.findByNameAndStore(storeName, descriptionName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canList(description)) {
			throw new NotAuthorizedException("list offerings in description " + description.getName());
		}
		
		return offeringDao.getDescriptionOfferingsPage(storeName, descriptionName, offset, max);
	}

	@Override
	@Transactional
	public void bookmark(String storeName, String descriptionName, String offeringName) 
			throws NotAuthorizedException,StoreNotFoundException, DescriptionNotFoundException, 
			OfferingNotFoundException {
		
		Offering offering = offeringDao.findDescriptionByNameStoreAndDescription(storeName, 
				descriptionName, offeringName);
		
		// Check if the user is allowed to bookmark the offering. An exception will be
		// risen if the user is not allowed to do it.
		if (!offeringAuth.canBookmark(offering)) {
			throw new NotAuthorizedException("bookmark offering");
		}
		
		try {
			User user = userBo.getCurrentUser();
			List<Offering> bookmarks = user.getBookmarks();
			
			// If the offering is already bookmarked, this operation will remove it
			// from the bookmarked offerings.
			if (bookmarks.contains(offering)) {
				bookmarks.remove(offering);
			} else {
				bookmarks.add(offering);
			}
			
			// The user is automatically updated since the method is marked as "Transactional"
						
		} catch (UserNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Transactional
	public List<Offering> getAllBookmarkedOfferings() throws NotAuthorizedException {
		return getBookmarkedOfferingsPage(0, Integer.MAX_VALUE);
	}

	@Override
	@Transactional
	public List<Offering> getBookmarkedOfferingsPage(int offset, int max)
			throws NotAuthorizedException {

		// Check rights and raise exception if user is not allowed to perform this action
		if (!offeringAuth.canListBookmarked()) {
			throw new NotAuthorizedException("list bookmarked offerings");
		}
		
		try {
			List<Offering> offerings = userBo.getCurrentUser().getBookmarks();
			int finalElement = offset + max;
			int checkedMax = finalElement <= offerings.size() ? finalElement : offerings.size();
			return offerings.subList(offset, checkedMax);
		} catch (UserNotFoundException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
}
