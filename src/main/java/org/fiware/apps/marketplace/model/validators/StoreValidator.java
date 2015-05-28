package org.fiware.apps.marketplace.model.validators;

/*
 * #%L
 * FiwareMarketplace
 * %%
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

import org.fiware.apps.marketplace.dao.StoreDao;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("storeValidator")
public class StoreValidator {

    @Autowired private StoreDao storeDao;
    @Value("${media.maxSize}") private int maxImageSize;

    private static BasicValidator basicValidator = BasicValidator.getInstance();
	
	/**
	 * @param store Store to be checked
	 * @param checkRequiredFields true if the user is being created. In this case, the system will check if
	 * the basic fields (name, URL) are included. Additionally, the method will check that there is not another
	 * store with the same name.
	 * @param checkExistingDisplayName If true, the method will check if there is a store with the same display
	 * name and an exception will be thrown in this case
	 * @param checkExistingURL If true, the method will check if there is a store with the same URL
	 * and an exception will be thrown in this case
	 * @throws ValidationException If store is not valid
	 */
	private void validateStore(Store store, boolean checkRequiredFields, boolean checkExistingDisplayName, 
			boolean checkExistingURL) throws ValidationException {

		// Check basic fields when a store is created
		if (checkRequiredFields) {
			
			//basicValidator.validateRequired("name", store.getName());
			basicValidator.validateRequired("displayName", store.getDisplayName());
			basicValidator.validateRequired("url", store.getUrl());
			
			// Name does not changes, so it should only be checked on creation
			if (!storeDao.isNameAvailable(store.getName())) {
	            throw new ValidationException("name", "This name is already in use.");
			}
		}

		// If the store is being created, this value cannot be null since we have
		// checked it before
		if (store.getDisplayName() != null) {
			basicValidator.validateDisplayName(store.getDisplayName());
			
			if (checkExistingDisplayName && !storeDao.isDisplayNameAvailable(store.getDisplayName())) {
	            throw new ValidationException("displayName", "This name is already in use.");
			}
		}

		// If the store is being created, this value cannot be null since we have
		// checked it before
		if (store.getUrl() != null) {
			basicValidator.validateURL("url", store.getUrl());
			
			// Check that the URL is available
			if (checkExistingURL && !storeDao.isURLAvailable(store.getUrl())) {
	            throw new ValidationException("url", "This URL is already in use.");
			}
		}

		if (store.getComment() != null) {
			basicValidator.validateComment(store.getComment());
		}
		
		// Check image length
		int imageLength = store.getImageBase64() != null ? store.getImageBase64().length() : 0;
		// Conversion factor between an array of bytes and its representation in Base 64
		int finalSizeBytes = imageLength * 3 / 4;
		
		if (finalSizeBytes > maxImageSize) {
			int sizeInMB = finalSizeBytes / 1024 / 1024;
			throw new ValidationException("imageBase64", 
					"The image is too large. The maximum size accepted is: " + sizeInMB + " MB.");
		}
		
		
	}
	
	/**
	 * Method to check if a new store is valid
	 * @param store The store to be checked
	 * @throws ValidationException If the new store is not valid
	 */
	public void validateNewStore(Store store) throws ValidationException {
		this.validateStore(store, true, true, true);
	}
	
	/**
	 * Method to check if an updated store is valid
	 * @param oldStore The store to be updated
	 * @param updatedStore The new values that will be set in the existing store
	 * @throws ValidationException It the updated store is not valid
	 */
	public void validateUpdatedStore(Store oldStore, Store updatedStore) throws ValidationException {
		boolean checkExistingDisplayName = !oldStore.getDisplayName().equals(updatedStore.getDisplayName());
		boolean checkExistingURL = !oldStore.getUrl().equals(updatedStore.getUrl());
		
		this.validateStore(updatedStore, false, checkExistingDisplayName, checkExistingURL);
	}
	
	
}
