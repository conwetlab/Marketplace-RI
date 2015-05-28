package org.fiware.apps.marketplace.bo.impl;

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

import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;

import org.fiware.apps.marketplace.bo.DescriptionBo;
import org.fiware.apps.marketplace.bo.StoreBo;
import org.fiware.apps.marketplace.bo.UserBo;
import org.fiware.apps.marketplace.dao.DescriptionDao;
import org.fiware.apps.marketplace.dao.OfferingDao;
import org.fiware.apps.marketplace.dao.StoreDao;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.helpers.OfferingResolver;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.model.User;
import org.fiware.apps.marketplace.model.validators.DescriptionValidator;
import org.fiware.apps.marketplace.rdf.RdfIndexer;
import org.fiware.apps.marketplace.security.auth.DescriptionAuth;
import org.fiware.apps.marketplace.utils.NameGenerator;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.shared.JenaException;

@org.springframework.stereotype.Service("descriptionBo")
public class DescriptionBoImpl implements DescriptionBo {
	
	@Autowired private DescriptionAuth descriptionAuth;
	@Autowired private DescriptionValidator descriptionValidator;
	@Autowired private DescriptionDao descriptionDao;
	@Autowired private RdfIndexer rdfIndexer;
	@Autowired private OfferingResolver offeringResolver;
	@Autowired private UserBo userBo;
	@Autowired private StoreBo storeBo;
	@Autowired private OfferingDao offeringDao;
	@Autowired private StoreDao storeDao;
	@Autowired private SessionFactory sessionFactory;
	
	private static final String JENA_ERROR = "Your RDF could not be parsed.";
		
	@Override
	@Transactional(readOnly=false, rollbackFor=Exception.class)
	public void save(String storeName, Description description) 
			throws NotAuthorizedException, 
			ValidationException, StoreNotFoundException {
		
		Store store = null;
		User user = null;
		
		try {
			
			user = userBo.getCurrentUser();
			store = storeDao.findByName(storeName);
			
			// Check rights and raise exception if user is not allowed to perform this action
			if (!descriptionAuth.canCreate(description)) {
				throw new NotAuthorizedException("create description");
			}
			
			// Set basic fields
			description.setRegistrationDate(new Date());
			description.setStore(store);
			description.setCreator(user);
			description.setLasteditor(user);
			
			// Set the name based on the display name
			description.setName(NameGenerator.getURLName(description.getDisplayName()));
			
			// Exception is risen if the description is not valid
			descriptionValidator.validateNewDescription(description);
			
			// Get all the offerings described in the USDL
			List<Offering> offerings = offeringResolver.resolveOfferingsFromServiceDescription(description);
			description.addOfferings(offerings);
			
			// Save the description
			store.addDescription(description);
			// Use StoreDAO to create. It's easier and safer. Useful to avoid weird Hibernate exceptions
			storeDao.update(store);
			
			// ID is required to index the service. Otherwise, the indexer will throw an exception
			// The ID is not automatically set when the description is saved by adding it to the Store
			// so we need to retrieve the object from the database
			try {
				Description createdDescription = descriptionDao.findByNameAndStore(storeName, description.getName());
				description.setId(createdDescription.getId());
			} catch (DescriptionNotFoundException ex) {
				// This should not happen. We have just created the description
			}
			
			// Index
			rdfIndexer.indexOrUpdateService(description);
		} catch (MalformedURLException | JenaException ex) {
			
			// These two exceptions are only thrown if the description cannot be parsed by the RdfIndexer
			// When the indexer cannot index the service, the description cannot be attached to the Store
			// Because of this, the we have set the rollbackFor parameter
			
			String errorMessage;
			if (ex instanceof JenaException) {
				errorMessage = JENA_ERROR;
			} else {
				errorMessage = ex.getMessage();
			}
			
			throw new ValidationException("url", errorMessage);
		} catch (UserNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	@Transactional(readOnly=false, rollbackFor=Exception.class)
	public void update(String storeName, String descriptionName, Description updatedDescription) 
			throws NotAuthorizedException, 
			ValidationException, StoreNotFoundException, DescriptionNotFoundException {
				
		try {
			Description descriptionToBeUpdated = descriptionDao.findByNameAndStore(storeName, descriptionName);
			
			// Check rights and raise exception if user is not allowed to perform this action
			if (!descriptionAuth.canUpdate(descriptionToBeUpdated)) {
				throw new NotAuthorizedException("update description");
			}
			
			//Set store in description
			updatedDescription.setStore(descriptionToBeUpdated.getStore());
			
			// Exception is risen if the description is not valid
			descriptionValidator.validateUpdatedDescription(descriptionToBeUpdated, updatedDescription);
			
			// Update fields
			if (updatedDescription.getDisplayName() != null) {
				descriptionToBeUpdated.setDisplayName(updatedDescription.getDisplayName());
			}
			
			if (updatedDescription.getComment() != null) {
				descriptionToBeUpdated.setComment(updatedDescription.getComment());
			}
	
			if (updatedDescription.getUrl() != null) {
				// If the URL has changed, the new offerings have to be retrieved and loaded. Otherwise,
				// this step is not required
				
				descriptionToBeUpdated.setUrl(updatedDescription.getUrl());
				
				// Remove previous offerings
				List<Offering> descriptionOfferings = descriptionToBeUpdated.getOfferings();
				descriptionOfferings.clear();
				
				// At this point, transaction need to be flushed in order to avoid constraints violations
				// that occur when the embedded offerings has not been updated
				sessionFactory.getCurrentSession().flush();
				
				// Get all the offerings described in the USDL
				List<Offering> newOfferings = offeringResolver
						.resolveOfferingsFromServiceDescription(descriptionToBeUpdated);
				
				descriptionOfferings.addAll(newOfferings);
								
				// When the description URL changes, the index must be updated. 
				rdfIndexer.indexOrUpdateService(descriptionToBeUpdated);
			}
	
			descriptionToBeUpdated.setLasteditor(userBo.getCurrentUser());
			descriptionDao.update(descriptionToBeUpdated);
		} catch (MalformedURLException | JenaException ex) {
						
			String errorMessage;
			if (ex instanceof JenaException) {
				errorMessage = JENA_ERROR;
			} else {
				errorMessage = ex.getMessage();
			}
			
			throw new ValidationException("url", errorMessage);			
		} catch (UserNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	@Transactional(readOnly=false)
	public void delete(String storeName, String descriptionName) 
			throws NotAuthorizedException, StoreNotFoundException, DescriptionNotFoundException {
		
		Store store = storeDao.findByName(storeName);
		Description description = descriptionDao.findByNameAndStore(storeName, descriptionName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!descriptionAuth.canDelete(description)) {
			throw new NotAuthorizedException("delete description");
		}
		
		// Delete the description from the data base
		// We must relay on StoreDao to remove descriptions. It's easier and safer
		// And weird exceptions are avoided
		store.removeDescription(description);
		storeDao.update(store);
		
		// Delete indexes
		rdfIndexer.deleteService(description);
	}

	@Override
	@Transactional
	public Description findById(Integer id) 
			throws DescriptionNotFoundException, NotAuthorizedException{
		
		Description description = descriptionDao.findById(id);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!descriptionAuth.canGet(description)) {
			throw new NotAuthorizedException("find description");
		}
		
		return description;
	}

	@Override
	@Transactional
	public Description findByNameAndStore(String storeName, String descriptionName) 
			throws StoreNotFoundException, DescriptionNotFoundException, 
			NotAuthorizedException {
		
		Description description = descriptionDao.findByNameAndStore(storeName, descriptionName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!descriptionAuth.canGet(description)) {
			throw new NotAuthorizedException("find description");
		}
		
		return description;
	}
	
	@Override
	@Transactional
	public List<Description> getCurrentUserDescriptions() {
		try {
			return descriptionDao.getUserDescriptions(userBo.getCurrentUser().getUserName());
		} catch (UserNotFoundException e) {
			// This exception should never happen
			throw new RuntimeException(e);
		}
	}
	
	@Override
	@Transactional
	public List<Description> getAllDescriptions() 
			throws NotAuthorizedException {
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!descriptionAuth.canList()) {
			throw new NotAuthorizedException("list descriptions");
		}

		return descriptionDao.getAllDescriptions();
	}
	
	@Override
	@Transactional
	public List<Description> getDescriptionsPage(int offset, int max)
			throws NotAuthorizedException {
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!descriptionAuth.canList()) {
			throw new NotAuthorizedException("list descriptions");
		}
		
		return descriptionDao.getDescriptionsPage(offset, max);
	}

	@Override
	@Transactional
	public List<Description> getStoreDescriptions(String storeName) 
			throws StoreNotFoundException, NotAuthorizedException {
		
		// Will throw exception in case the Store does not exist
		Store store = storeDao.findByName(storeName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!descriptionAuth.canList(store)) {
			throw new NotAuthorizedException("list descriptions in store " + store.getName());
		}
		
		return store.getDescriptions();
	}

	@Override
	@Transactional
	public List<Description> getStoreDescriptionsPage(String storeName,
			int offset, int max) throws StoreNotFoundException, 
			NotAuthorizedException {
		
		// Will throw exception in case the Store does not exist
		Store store = storeDao.findByName(storeName);
		
		// Check rights and raise exception if user is not allowed to perform this action
		if (!descriptionAuth.canList(store)) {
			throw new NotAuthorizedException("list descriptions in store " + store.getName());
		}
		
		return descriptionDao.getStoreDescriptionsPage(storeName, offset, max);
	}

    @Override
    @Transactional
    public List<Description> filterByUserNameAndStoreName(String userName, String storeName)
            throws UserNotFoundException, StoreNotFoundException {

        return descriptionDao.filterByUserNameAndStoreName(userName, storeName);
    }

}
