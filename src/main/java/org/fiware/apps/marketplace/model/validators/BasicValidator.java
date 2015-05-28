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

import org.apache.commons.validator.GenericValidator;
import org.fiware.apps.marketplace.exceptions.ValidationException;

public class BasicValidator {

	private static final int DISPLAY_NAME_MIN_LENGTH = 3;
	private static final int DISPLAY_NAME_MAX_LENGTH = 100;
	private static final int COMMENT_MIN_LENGTH = 0;
	private static final int COMMENT_MAX_LENGTH = 200;
	private static final String DISPLAY_NAME_FIELD = "displayName";
	private static final String COMMENT_FIELD = "comment";

	private static BasicValidator INSTANCE = new BasicValidator();
	
	private BasicValidator() {

	}
	
	public static BasicValidator getInstance() {
		return INSTANCE;
	}
	
	public static int getDisplayNameMaxLength() {
		return DISPLAY_NAME_MAX_LENGTH;
	}

	public static int getCommentMaxLength() {
		return COMMENT_MAX_LENGTH;
	}
	
	public static int getDisplayNameMinLength() {
		return DISPLAY_NAME_MIN_LENGTH;
	}

	public static int getCommentMinLength() {
		return COMMENT_MIN_LENGTH;
	}

	// BASIC VALIDATORS
	public void validateRequired(String fieldName, String fieldValue) throws ValidationException {
		if (fieldValue == null || fieldValue.length() == 0) {
			throw new ValidationException(fieldName, "This field is required.");
		}
	}

	public void validatePattern(String fieldName, String fieldValue, String regex, String errorMessage) 
			throws ValidationException {
		
		if (fieldValue == null || !fieldValue.matches(regex)) {
			throw new ValidationException(fieldName, errorMessage);
		}
	}
	
	public void validateDisplayNamePattern(String displayName) throws ValidationException {
		this.validatePattern(DISPLAY_NAME_FIELD, displayName, "^[a-zA-Z0-9. -]+$", 
				"This field only accepts letters, numbers, white spaces, dots and hyphens.");
	}

	public void validateURL(String fieldName, String fieldValue) throws ValidationException {
		if (fieldValue == null || !GenericValidator.isUrl(fieldValue)) {
			throw new ValidationException(fieldName, "This field must be a valid URL.");
		}
	}

	public void validateMinLength(String fieldName, String fieldValue, int minLength) throws ValidationException {
		if (fieldValue == null || fieldValue.length() < minLength) {
			throw new ValidationException(fieldName, 
					String.format("This field must be at least %d chars.", minLength));
		}
	}

	public void validateMaxLength(String fieldName, String fieldValue, int maxLength) throws ValidationException {
		if (fieldValue == null || fieldValue.length() >= maxLength) {
			throw new ValidationException(fieldName, 
					String.format("This field must not exceed %d chars.", maxLength));
		}
	}
	
	public void validateLength(String fieldName, String fieldValue, int minLength, int maxLength) throws ValidationException {
		this.validateMinLength(fieldName, fieldValue, minLength);
		this.validateMaxLength(fieldName, fieldValue, maxLength);
		
	}
	
	public void validateEMail(String fieldName, String email) throws ValidationException {
		if (email == null || !GenericValidator.isEmail(email)) {
			throw new ValidationException(fieldName, "This field must be a valid email.");
		}
	}
	
	// VALIDATORS FOR SPECIFIC FIELDS
	public void validateDisplayNameMinLength(String displayName) throws ValidationException {
		validateMinLength(DISPLAY_NAME_FIELD, displayName, DISPLAY_NAME_MIN_LENGTH);
	}
	
	public void validateCommentMinLength(String description) throws ValidationException {
		validateMaxLength(COMMENT_FIELD, description, COMMENT_MAX_LENGTH);
	}
	
	public void validateDisplayNameMaxLength(String displayName) throws ValidationException {
		validateMaxLength(DISPLAY_NAME_FIELD, displayName, DISPLAY_NAME_MAX_LENGTH);
	}
	
	public void validateCommentMaxLength(String description) throws ValidationException {
		validateMaxLength(COMMENT_FIELD, description, COMMENT_MAX_LENGTH);
	}
	
	public void validateDisplayName(String displayName) throws ValidationException {
		this.validateDisplayNamePattern(displayName);
		this.validateDisplayNameMinLength(displayName);
		this.validateDisplayNameMaxLength(displayName);
	}
	
	public void validateComment(String description) throws ValidationException {
		this.validateCommentMinLength(description);
		this.validateCommentMaxLength(description);
	}
	
}
