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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.fiware.apps.marketplace.dao.DescriptionDao;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.model.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DescriptionValidatorTest {
	
	@Mock private DescriptionDao descriptionDaoMock;
	@InjectMocks private DescriptionValidator descriptionValidator = new DescriptionValidator();
	
	private static final String MISSING_FIELDS_MSG = "This field is required.";
	private static final String TOO_SHORT_PATTERN = "This field must be at least %d chars.";
	private static final String TOO_LONG_PATTERN = "This field must not exceed %d chars.";
	private static final String INVALID_URL = "This field must be a valid URL.";
	
	private static Description generateValidDescription() {
		// Additional classes
		User creator = new User();
		creator.setId(1);
		
		Store store = new Store();
		store.setName("storeName");
		
		Description description = new Description();
		// Name is supposed to be auto-generated based on display name...
		description.setName("description-1");
		description.setDisplayName("Description 1.-");
		description.setUrl("https://repo.lab.fi-ware.org/offerings/offering1.rdf");
		description.setComment("This is an example comment");
		description.setRegistrationDate(new Date());
		description.setStore(store);
		description.setCreator(creator);
		description.setLasteditor(creator);
		
		return description;
	}
	
	
	private void assertInvalidNewDescription(Description description, String field, String expectedMsg) {
		try {
			descriptionValidator.validateNewDescription(description);
			failBecauseExceptionWasNotThrown(ValidationException.class);
		} catch (ValidationException ex) {
			assertThat(ex).hasMessage(expectedMsg);
			assertThat(ex.getFieldName()).isEqualTo(field);
		}
	}
	
	private void assertInvalidUpdatedDescription(Description oldDescription, Description updatedDescription, 
			String field, String expectedMsg) {
		try {
			descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);
			failBecauseExceptionWasNotThrown(ValidationException.class);
		} catch (ValidationException ex) {
			assertThat(ex).hasMessage(expectedMsg);
			assertThat(ex.getFieldName()).isEqualTo(field);
		}
	}
	
	@Before 
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(descriptionDaoMock.isNameAvailableInStore(anyString(), anyString())).thenReturn(true);
		when(descriptionDaoMock.isDisplayNameAvailableInStore(anyString(), anyString())).thenReturn(true);
		when(descriptionDaoMock.isURLAvailableInStore(anyString(), anyString())).thenReturn(true);
		
	}
	
	@Test
	public void testValidBasicDescription() throws ValidationException {
		Store store = mock(Store.class);
		when(store.getName()).thenReturn("store display name");
		Description description = new Description();
		description.setDisplayName("description 1");
		description.setName("description-1");
		description.setStore(store);
		description.setUrl("https://repo.lab.fi-ware.org/offerings/offering1.rdf");
		
		descriptionValidator.validateNewDescription(description);
	}
	
	@Test
	public void testValidComplexDescription() throws ValidationException {
		Description description = generateValidDescription();
		descriptionValidator.validateNewDescription(description);
	}
	
	@Test
	public void testMissingDisplayNameOnCreation() {
		Description description = generateValidDescription();
		description.setDisplayName(null);
		
		assertInvalidNewDescription(description, "displayName", MISSING_FIELDS_MSG);
	}
	
	@Test
	public void testMissingDisplayNameOnUpdate() throws ValidationException {
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		updatedDescription.setName(null);
		
		descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);
	}
	
	@Test
	public void testMissingUrlOnCreation() {
		Description description = generateValidDescription();
		description.setUrl(null);
		
		assertInvalidNewDescription(description, "url", MISSING_FIELDS_MSG);
	}
	
	@Test
	public void testMissingUrlOnUpdate() throws ValidationException {
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		updatedDescription.setUrl(null);
		
		descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);	
	}
	
	@Test
	public void testMissingCommentOnCreation() throws ValidationException {
		Description description = generateValidDescription();
		description.setComment(null);

		descriptionValidator.validateNewDescription(description);
	}
	
	@Test
	public void testMissingCommentOnUpdate() throws ValidationException {
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		updatedDescription.setComment(null);

		descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);
	}
	
	@Test
	public void testDisplayNameTooShort() {
		Description description = generateValidDescription();
		description.setDisplayName("a");
		
		assertInvalidNewDescription(description, "displayName", String.format(TOO_SHORT_PATTERN, 3));
	}
	
	@Test
	public void testDispalyNameTooLong() {
		Description description = generateValidDescription();
		description.setDisplayName("123456789012345678901234567890123456789012345678901234567890"
				+ "12345678901234567890123456789012345678901");
		
		assertInvalidNewDescription(description, "displayName", String.format(TOO_LONG_PATTERN, 100));
	}
	
	@Test
	public void testDisplayNameInUseOnCreation() {
		String name = "descriptionName";
		Description description = generateValidDescription();
		description.setDisplayName(name);
		
		when(descriptionDaoMock.isDisplayNameAvailableInStore(description.getStore().getName(), name))
				.thenReturn(false);
		
		assertInvalidNewDescription(description, "displayName", "This name is already in use in this Store.");
	}
	
	@Test
	public void testDisplayNameNotInUseOnUpdate() throws ValidationException {
		String oldDisplayName = "oldDisplayName";
		String newDisplayName = "newDisplayName";
		
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		
		oldDescription.setDisplayName(oldDisplayName);
		updatedDescription.setDisplayName(newDisplayName);
		
		descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);
	}
	
	@Test
	public void testDisplayNameInUseByOtherUserOnUpdate() throws ValidationException {
		String oldDisplayName = "oldDisplayName";
		String newDisplayName = "newDisplayName";
		
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		
		oldDescription.setDisplayName(oldDisplayName);
		updatedDescription.setDisplayName(newDisplayName);
		updatedDescription.setStore(oldDescription.getStore());
		
		when(descriptionDaoMock.isDisplayNameAvailableInStore(updatedDescription.getStore().getName(), newDisplayName))
				.thenReturn(false);
		
		this.assertInvalidUpdatedDescription(oldDescription, updatedDescription, "displayName", 
				"This name is already in use in this Store.");
	}
	
	@Test
	public void testDisplayNameInUseByTheSameUserOnUpdate() throws ValidationException {
		String displayName = "newDisplayName";
		
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		
		oldDescription.setDisplayName(displayName);
		updatedDescription.setDisplayName(displayName);
		
		when(descriptionDaoMock.isDisplayNameAvailableInStore(updatedDescription.getStore().getName(), displayName))
				.thenReturn(false);
		
		descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);
	}

	
	@Test
	public void testInvalidURL1() {
		Description description = generateValidDescription();
		description.setUrl("http://");

		assertInvalidNewDescription(description, "url", INVALID_URL);
	}

	@Test
	public void testInvalidURL2() {
		Description description = generateValidDescription();
		description.setUrl("repo.lab.fi-ware.org/offerings/offering1.rdf");

		assertInvalidNewDescription(description, "url", INVALID_URL);
	}

	@Test
	public void testInvalidURL3() {
		Description description = generateValidDescription();
		description.setUrl("https://repo.lab.fi-ware.org:222222/offerings/offering1.rdf");

		assertInvalidNewDescription(description, "url", INVALID_URL);
	}

	@Test
	public void testInvalidURL4() {
		Description description = generateValidDescription();
		description.setUrl("offering");

		assertInvalidNewDescription(description, "url", INVALID_URL);
	}
	
	@Test
	public void testURLInUseOnCreation() {
		String url = "http://store.lab.fiware.org";
		Description description = generateValidDescription();
		description.setUrl(url);
		
		when(descriptionDaoMock.isURLAvailableInStore(description.getStore().getName(), url))
				.thenReturn(false);
		
		assertInvalidNewDescription(description, "url", 
				"This URL is already in use in this Store.");

	}
	
	@Test
	public void testURLNotInUseOnUpdate() throws ValidationException {
		String oldUrl = "http://store-old.lab.fi-ware.org";
		String updateUrl = "http://store.lab.fiware.org";
		
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		
		oldDescription.setUrl(oldUrl);
		updatedDescription.setUrl(updateUrl);
				
		descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);
	}
	
	@Test
	public void testURLInUseByOtherStoreOnUpdate() {
		String oldUrl = "http://store-old.lab.fi-ware.org";
		String updateUrl = "http://store.lab.fiware.org";
		
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		
		oldDescription.setUrl(oldUrl);
		updatedDescription.setUrl(updateUrl);
		
		when(descriptionDaoMock.isURLAvailableInStore(updatedDescription.getStore().getName(), updateUrl))
				.thenReturn(false);
		
		assertInvalidUpdatedDescription(oldDescription, updatedDescription, "url", 
				"This URL is already in use in this Store.");
	}
	
	@Test
	public void testURLInUseByTheSameStoreOnUpdate() throws ValidationException {
		String url = "http://store.lab.fiware.org";
		
		Description oldDescription = generateValidDescription();
		Description updatedDescription = generateValidDescription();
		
		oldDescription.setUrl(url);
		updatedDescription.setUrl(url);
		
		when(descriptionDaoMock.isURLAvailableInStore(updatedDescription.getStore().getName(), url))
				.thenReturn(false);
		
		descriptionValidator.validateUpdatedDescription(oldDescription, updatedDescription);
	}
	
	@Test
	public void testEmptyComment() throws ValidationException {
		Description description = generateValidDescription();
		description.setComment("");

		// Empty descriptions are allowed
		descriptionValidator.validateNewDescription(description);
	}

	@Test
	public void testCommentTooLong() throws ValidationException {
		Description description = generateValidDescription();

		//240 characters (80 * 3)
		description.setComment(
				"12345678901234567890123456789012345678901234567890123456789012345678901234567890" + 
				"12345678901234567890123456789012345678901234567890123456789012345678901234567890" + 
				"12345678901234567890123456789012345678901234567890123456789012345678901234567890");	

		// Too longs descriptions are not allowed
		assertInvalidNewDescription(description, "comment", String.format(TOO_LONG_PATTERN, 200));
	}
}
