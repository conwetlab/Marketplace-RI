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
import static org.mockito.Mockito.*;

import java.util.Date;

import org.fiware.apps.marketplace.dao.StoreDao;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.model.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StoreValidatorTest {

	@Mock private StoreDao storeDaoMock;
	@InjectMocks private StoreValidator storeValidator = new StoreValidator();

	private static final String MISSING_FIELDS_MSG = "This field is required.";
	private static final String TOO_SHORT_PATTERN = "This field must be at least %d chars.";
	private static final String TOO_LONG_PATTERN = "This field must not exceed %d chars.";
	private static final String INVALID_URL = "This field must be a valid URL.";

	private static Store generateValidStore() {
		Store store = new Store();
		store.setCreator(new User());
		store.setComment("This is a basic comment");
		store.setLasteditor(store.getCreator());
		store.setName("name");
		store.setDisplayName("store-. ");
		store.setUrl("https://store.lab.fiware.org");
		store.setRegistrationDate(new Date());

		return store;
	}

	private void assertInvalidNewStore(Store store, String field, String expectedMsg) {
		try {
			storeValidator.validateNewStore(store);
			failBecauseExceptionWasNotThrown(ValidationException.class);
		} catch (ValidationException ex) {
			assertThat(ex).hasMessage(expectedMsg);
			assertThat(ex.getFieldName()).isEqualTo(field);
		}
	}
	
	private void assertInvalidUpdatedStore(Store oldStore, Store updatedStore, String field, String expectedMsg) {
		try {
			storeValidator.validateUpdatedStore(oldStore, updatedStore);
			failBecauseExceptionWasNotThrown(ValidationException.class);
		} catch (ValidationException ex) {
			assertThat(ex).hasMessage(expectedMsg);
			assertThat(ex.getFieldName()).isEqualTo(field);
		}
	}
	
	@Before 
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(storeDaoMock.isNameAvailable(anyString())).thenReturn(true);
		when(storeDaoMock.isDisplayNameAvailable(anyString())).thenReturn(true);
		when(storeDaoMock.isURLAvailable(anyString())).thenReturn(true);
		
	}

	@Test
	public void testValidBasicStore() throws ValidationException {
		Store store = new Store();
		store.setName("wstore");
		store.setDisplayName("WStore");
		store.setUrl("https://store.lab.fi-ware.org");

		storeValidator.validateNewStore(store);
	}

	@Test
	public void testValidComplexStore() throws ValidationException {
		Store store = generateValidStore();
		storeValidator.validateNewStore(store);
	}

	@Test
	public void testMissingDisplayNameOnCreation() {
		Store store = generateValidStore();
		store.setDisplayName(null);

		assertInvalidNewStore(store, "displayName", MISSING_FIELDS_MSG);
	}
	
	@Test
	public void testMissingDisplayNameOnUpdate() throws ValidationException {
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		updatedStore.setName(null);

		storeValidator.validateUpdatedStore(oldStore, updatedStore);
	}

	@Test
	public void testMissingUrlOnCreation() {
		Store store = generateValidStore();
		store.setUrl(null);

		assertInvalidNewStore(store, "url", MISSING_FIELDS_MSG);
	}
	
	@Test
	public void testMissingUrlOnUpdate() throws ValidationException {
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		updatedStore.setUrl(null);

		storeValidator.validateUpdatedStore(oldStore, updatedStore);
	}

	@Test
	public void testMissingCommentOnCreation() throws ValidationException {
		Store store = generateValidStore();
		store.setComment(null);

		storeValidator.validateNewStore(store);
	}
	
	@Test
	public void testMissingCommentOnUpdate() throws ValidationException {
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		updatedStore.setComment(null);

		storeValidator.validateUpdatedStore(oldStore, updatedStore);
	}

	@Test
	public void testDisplayNameTooShort() {
		Store store = generateValidStore();
		store.setDisplayName("a");

		assertInvalidNewStore(store, "displayName", String.format(TOO_SHORT_PATTERN, 3));
	}

	@Test
	public void testDisplayNameTooLong() {
		Store store = generateValidStore();
		store.setDisplayName("123456789012345678901234567890123456789012345678901234567890"
				+ "12345678901234567890123456789012345678901");

		assertInvalidNewStore(store, "displayName", String.format(TOO_LONG_PATTERN, 100));
	}
	
	@Test
	public void testDisplayNameInUseOnCreation() {
		String name = "storeName";
		Store store = generateValidStore();
		store.setDisplayName(name);
		when(storeDaoMock.isDisplayNameAvailable(name)).thenReturn(false);
		
		assertInvalidNewStore(store, "displayName", "This name is already in use.");
	}
	
	@Test
	public void testDisplayNameNotInUseOnUpdate() throws ValidationException {
		String oldDisplayName = "oldDisplayName";
		String newDisplayName = "newDisplayName";
		
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		
		oldStore.setDisplayName(oldDisplayName);
		updatedStore.setDisplayName(newDisplayName);
		
		storeValidator.validateUpdatedStore(oldStore, updatedStore);
	}
	
	@Test
	public void testDisplayNameInUseByOtherStoreOnUpdate() throws ValidationException {
		String oldDisplayName = "oldDisplayName";
		String newDisplayName = "newDisplayName";
		
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		
		oldStore.setDisplayName(oldDisplayName);
		updatedStore.setDisplayName(newDisplayName);
		
		when(storeDaoMock.isDisplayNameAvailable(newDisplayName)).thenReturn(false);
		
		this.assertInvalidUpdatedStore(oldStore, updatedStore, "displayName", "This name is already in use.");
	}
	
	@Test
	public void testDisplayNameInUseByTheSameStoreOnUpdate() throws ValidationException {
		String displayName = "newDisplayName";
		
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		
		oldStore.setDisplayName(displayName);
		updatedStore.setDisplayName(displayName);
		
		when(storeDaoMock.isDisplayNameAvailable(displayName)).thenReturn(false);
		
		storeValidator.validateUpdatedStore(oldStore, updatedStore);
	}
	
	@Test
	public void testInvalidURL1() {
		Store store = generateValidStore();
		store.setUrl("http://");

		assertInvalidNewStore(store, "url", INVALID_URL);
	}

	@Test
	public void testInvalidURL2() {
		Store store = generateValidStore();
		store.setUrl("store.lab.fi-ware.org");

		assertInvalidNewStore(store, "url", INVALID_URL);
	}

	@Test
	public void testInvalidURL3() {
		Store store = generateValidStore();
		store.setUrl("https://store.lab.fi-ware.org:222222");

		assertInvalidNewStore(store, "url", INVALID_URL);
	}

	@Test
	public void testInvalidURL4() {
		Store store = generateValidStore();
		store.setUrl("store");

		assertInvalidNewStore(store, "url", INVALID_URL);
	}
	
	@Test
	public void testURLInUseOnCreation() {
		String url = "http://store.lab.fiware.org";
		Store store = generateValidStore();
		store.setUrl(url);
		when(storeDaoMock.isURLAvailable(url)).thenReturn(false);
		
		assertInvalidNewStore(store, "url", "This URL is already in use.");

	}
	
	@Test
	public void testURLNotInUseOnUpdate() throws ValidationException {
		String oldUrl = "http://store-old.lab.fi-ware.org";
		String updateUrl = "http://store.lab.fiware.org";
		
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		
		oldStore.setUrl(oldUrl);
		updatedStore.setUrl(updateUrl);
				
		storeValidator.validateUpdatedStore(oldStore, updatedStore);
	}
	
	@Test
	public void testURLInUseByOtherStoreOnUpdate() {
		String oldUrl = "http://store-old.lab.fi-ware.org";
		String updateUrl = "http://store.lab.fiware.org";
		
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		
		oldStore.setUrl(oldUrl);
		updatedStore.setUrl(updateUrl);
		
		when(storeDaoMock.isURLAvailable(updateUrl)).thenReturn(false);
		
		assertInvalidUpdatedStore(oldStore, updatedStore, "url", "This URL is already in use.");
	}
	
	@Test
	public void testURLInUseByTheSameStoreOnUpdate() throws ValidationException {
		String url = "http://store.lab.fiware.org";
		
		Store oldStore = generateValidStore();
		Store updatedStore = generateValidStore();
		
		oldStore.setUrl(url);
		updatedStore.setUrl(url);
		
		when(storeDaoMock.isURLAvailable(url)).thenReturn(false);
		
		storeValidator.validateUpdatedStore(oldStore, updatedStore);
	}

	@Test
	public void testEmptyComment() throws ValidationException {
		Store store = generateValidStore();
		store.setComment("");

		// Empty descriptions are allowed
		storeValidator.validateNewStore(store);
	}

	@Test
	public void testCommentTooLong() throws ValidationException {
		Store store = generateValidStore();

		//240 characters (80 * 3)
		store.setComment("12345678901234567890123456789012345678901234567890123456789012345678901234567890" + 
				"12345678901234567890123456789012345678901234567890123456789012345678901234567890" + 
				"12345678901234567890123456789012345678901234567890123456789012345678901234567890");	

		// Too long descriptions are not allowed
		assertInvalidNewStore(store, "comment", String.format(TOO_LONG_PATTERN, 200));	
	}

}
