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

import org.fiware.apps.marketplace.dao.DescriptionDao;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.Description;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("offeringsDescriptionValidator")
public class DescriptionValidator {
	
	@Autowired private DescriptionDao descriptionDao;

	private static BasicValidator basicValidator = BasicValidator.getInstance();

	/**
	 * @param description Offerings Description to be checked
	 * @param checkRequiredFields true if the user is being created. In this case, the system will check if
	 * the basic fields (name, URL) are included. The method will also check that there is not another description
	 * with the same name.
	 * @param checkExistingDisplayName If true, the method will check if there is a description with the same
	 * display name and an exception will be thrown in this case
	 * @param checkExistingURL If true, the method will check if there is a description with the same
	 * URL and an exception will be thrown in this case
	 * @throws ValidationException If description is not valid
	 */
	public void validateDescription(Description description, boolean checkRequiredFields, 
			boolean checkExistingDisplayName, boolean checkExistingURL) throws ValidationException {

		// Check basic fields when a description is created
		if (checkRequiredFields) {
			//basicValidator.validateRequired("name", description.getName());
			basicValidator.validateRequired("displayName", description.getDisplayName());
			basicValidator.validateRequired("url", description.getUrl());
			
			// Check that the name is not in use
			if (!descriptionDao.isNameAvailableInStore(description.getStore().getName(), description.getName())) {
				throw new ValidationException("name", "This name is already in use in this Store.");
			}
		}

		if (description.getDisplayName() != null) {
			basicValidator.validateDisplayName(description.getDisplayName());
			
			if (checkExistingDisplayName && !descriptionDao.isDisplayNameAvailableInStore(
					description.getStore().getName(), description.getDisplayName())) {
				throw new ValidationException("displayName", "This name is already in use in this Store.");
			}
		}

		if (description.getUrl() != null) {
			basicValidator.validateURL("url", description.getUrl());
			
			if (checkExistingURL && !descriptionDao.isURLAvailableInStore(description.getStore().getName(), 
					description.getUrl())) {
				throw new ValidationException("url", "This URL is already in use in this Store.");
			}
		}

		if (description.getComment() != null) {
			basicValidator.validateComment(description.getComment());
		}	
	}
	
	/**
	 * Method to check if a new description is valid
	 * @param description The description to be checked
	 * @throws ValidationException If the new description is not valid
	 */
	public void validateNewDescription(Description description) throws ValidationException {
		this.validateDescription(description, true, true, true);
	}
	
	/**
	 * Method to check if an updated description is valid
	 * @param oldDescription The description to be updated
	 * @param updatedDescription The new values that will be set in the existing description
	 * @throws ValidationException It the updated description is not valid
	 */
	public void validateUpdatedDescription(Description oldDescription, Description updatedDescription) throws ValidationException {
		boolean checkExistingDisplayName = !oldDescription.getDisplayName().equals(updatedDescription.getDisplayName());
		boolean checkExistingURL = !oldDescription.getUrl().equals(updatedDescription.getUrl());
				
		this.validateDescription(updatedDescription, false, checkExistingDisplayName, checkExistingURL);
	}

}
