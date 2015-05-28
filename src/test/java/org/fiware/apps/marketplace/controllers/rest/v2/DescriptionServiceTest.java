package org.fiware.apps.marketplace.controllers.rest.v2;

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
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fiware.apps.marketplace.bo.DescriptionBo;
import org.fiware.apps.marketplace.controllers.rest.v2.DescriptionService;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.ErrorType;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Descriptions;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DescriptionServiceTest {

	@Mock private DescriptionBo descriptionBoMock;

	@InjectMocks private DescriptionService descriptionRegistrationService;

	// Default values
	private Description description;
	private UriInfo uri;

	// Other useful constants
	private static final String STORE_NAME = "wstore";
	private static final String OFFSET_MAX_INVALID = "offset and/or max are not valid";
	private static final String DESCRIPTION_ALREADY_EXISTS = 
			"There is already a Description in this Store with that name";
	private static final String VALIDATION_ERROR = "Validation Exception";
	private static final String COMMENT = "This is a basic description";
	private static final String DESCRIPTION_DISPLAY_NAME = "Offerings Description";
	private static final String DESCRIPTION_NAME = "offerings-description";
	private static final String URL = "https://repo.lab.fi-ware.org/description.rdf";
	private static final String PATH = "/api/store/" + STORE_NAME + "/offerings_description";
	
	private static final ConstraintViolationException VIOLATION_EXCEPTION = 
			new ConstraintViolationException("", new SQLException(), "");

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// BASIC METHODS ////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Before 
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Before
	public void generateValidStore() {
		description = new Description();
		description.setComment(COMMENT);
		description.setDisplayName(DESCRIPTION_DISPLAY_NAME);
		description.setUrl(URL);
	}
	
	@Before
	public void setUpUri() {
		uri = mock(UriInfo.class);
		when(uri.getPath()).thenReturn(PATH);
	}

	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// CREATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void testCreateDescriptionNotAllowed() throws Exception {
		// Mocks
		Exception e = new NotAuthorizedException("create description");
		doThrow(e).when(descriptionBoMock).save(STORE_NAME, description);

		// Call the method
		Response res = descriptionRegistrationService.createDescription(uri, STORE_NAME, description);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());

		// Verify mocks
		verify(descriptionBoMock).save(STORE_NAME, description);
	}
	
	@Test
	public void testCreateDescriptionNoErrors() throws Exception {
		// Mocks
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				invocation.getArgumentAt(1, Description.class).setName(DESCRIPTION_NAME);
				return null;
			}
		}).when(descriptionBoMock).save(STORE_NAME, description);

		//Call the method
		Response res = descriptionRegistrationService.createDescription(uri, STORE_NAME, description);

		// Verify mocks
		verify(descriptionBoMock).save(STORE_NAME, description);

		// Check the response
		assertThat(res.getStatus()).isEqualTo(201);
		assertThat(res.getHeaders().get("Location").get(0).toString()).isEqualTo(PATH + "/" + DESCRIPTION_NAME);

		// Check that all the parameters of the Description are correct
		// (some of them must have been changed by the method)
		// assertThat(description.getRegistrationDate()).isNotNull();
		assertThat(description.getName()).isEqualTo(DESCRIPTION_NAME);
		assertThat(description.getDisplayName()).isEqualTo(DESCRIPTION_DISPLAY_NAME);
		assertThat(description.getComment()).isEqualTo(COMMENT);
		assertThat(description.getUrl()).isEqualTo(URL);
		// assertThat(description.getCreator()).isEqualTo(user);
		// assertThat(description.getLasteditor()).isEqualTo(user);
	}
	
	private void testCreateDescriptionGenericError(int statusCode, ErrorType errorType, 
			String field, String errorMsg) {
		
		// Call the method
		Response res = descriptionRegistrationService.createDescription(uri, STORE_NAME, description);

		// Verify mocks
		try {
			verify(descriptionBoMock).save(STORE_NAME, description);
			GenericRestTestUtils.checkAPIError(res, statusCode, errorType, errorMsg, field);
		} catch (Exception e) {
			// Impossible...
			fail("exception " + e + " not expected");
		}
	}

	@Test
	public void testCreateDescriptionValidationException() throws Exception {
		// Mocks
		String field = "aField";
		doThrow(new ValidationException(field, VALIDATION_ERROR)).when(descriptionBoMock).save(STORE_NAME, description);

		testCreateDescriptionGenericError(400, ErrorType.VALIDATION_ERROR, field, VALIDATION_ERROR);
	}
	
	@Test
	public void testCreateDescriptionUserNotFoundException() throws Exception {
		// Mocks
		String exceptionMsg = "User Not Found exception";
		doThrow(new RuntimeException(new UserNotFoundException("User Not Found exception")))
				.when(descriptionBoMock).save(STORE_NAME, description);

		testCreateDescriptionGenericError(500, ErrorType.INTERNAL_SERVER_ERROR, 
				null, exceptionMsg);

	}
	
	@Test
	public void testCreateDescriptionStoreNotFoundException() throws Exception {
		// Mocks
		String exceptionMsg = "Store Not Found!";
		doThrow(new StoreNotFoundException(exceptionMsg)).when(descriptionBoMock).save(STORE_NAME, description);

		testCreateDescriptionGenericError(404, ErrorType.NOT_FOUND, null, exceptionMsg);
	}
	
	private void testCreateDescriptionHibernateException(Exception exception, String message) 
			throws Exception {
		
		// Mock
		doThrow(exception).when(descriptionBoMock).save(STORE_NAME, description);

		testCreateDescriptionGenericError(400, ErrorType.BAD_REQUEST, null, message);
	}

	@Test
	public void testCreateDescriptionAlreadyExists() throws Exception {
		testCreateDescriptionHibernateException(VIOLATION_EXCEPTION, 
				DESCRIPTION_ALREADY_EXISTS);
	}
	
	@Test
	public void testCreateDescriptionOtherDataException() throws Exception {
		HibernateException exception = new HibernateException(new Exception("Too much content"));
		testCreateDescriptionHibernateException(exception, exception.getCause().getMessage());
	}

	@Test
	public void testCreteDescriptionNotKnowException() throws Exception {
		// Mock
		String exceptionMsg = "SERVER ERROR";
		doThrow(new RuntimeException("", new Exception(exceptionMsg))).
				when(descriptionBoMock).save(STORE_NAME, description);

		testCreateDescriptionGenericError(500, ErrorType.INTERNAL_SERVER_ERROR, null, exceptionMsg);
	}


	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// UPDATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testUpdateDescriptionNotAllowed() throws Exception {
		
		// Mocks
		Exception e = new NotAuthorizedException("update description");
		doThrow(e).when(descriptionBoMock).update(STORE_NAME, DESCRIPTION_NAME, description);

		// Call the method
		Response res = descriptionRegistrationService.
				updateDescription(STORE_NAME, DESCRIPTION_NAME, description);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());

		// Verify mocks
		verify(descriptionBoMock).update(STORE_NAME, DESCRIPTION_NAME, description);	
	}
	
	private void testUpdateDescriptionGenericError(Exception exception, int status, ErrorType errorType, 
			String field, String message) {

		try {
			doThrow(exception).when(descriptionBoMock).update(STORE_NAME, DESCRIPTION_NAME, description);
			
			// Call the method
			Response res = descriptionRegistrationService.
					updateDescription(STORE_NAME, DESCRIPTION_NAME, description);

			// Assertions
			GenericRestTestUtils.checkAPIError(res, status, errorType, message, field);

			// Verify mocks
			verify(descriptionBoMock).update(STORE_NAME, DESCRIPTION_NAME, description);	
		} catch (Exception ex) {
			fail("Exception " + ex + " not expected.");
		}
	}


	@Test
	public void testUpdateDescriptionValidationException() throws Exception {
		// Mocks
		String field = "aField";
		Exception e = new ValidationException(field, VALIDATION_ERROR);

		// Test
		testUpdateDescriptionGenericError(e, 400, ErrorType.VALIDATION_ERROR, field, VALIDATION_ERROR);
	}

	@Test
	public void testUpdateDescriptionStoreNotFound() throws Exception {
		//Mocks
		String exceptionMsg = "Store not Found!";
		Exception e = new StoreNotFoundException(exceptionMsg);

		testUpdateDescriptionGenericError(e, 404, ErrorType.NOT_FOUND, null, exceptionMsg);
	}
	
	@Test
	public void testUpdateDescriptionNotFound() throws Exception {
		//Mocks
		String exceptionMsg = "Description not Found!";
		Exception e = new DescriptionNotFoundException(exceptionMsg);

		testUpdateDescriptionGenericError(e, 404, ErrorType.NOT_FOUND, null, exceptionMsg);
	}
	
	@Test
	public void testUpdateDescriptionUserException() throws Exception {
		// Mocks
		String exceptionMsg = "User not found";
		Exception e = new RuntimeException(new UserNotFoundException(exceptionMsg));

		// Test
		testUpdateDescriptionGenericError(e, 500, ErrorType.INTERNAL_SERVER_ERROR, null, exceptionMsg);
	}
	
	@Test
	public void testUpdateDescriptionAlreadyExists() {
		testUpdateDescriptionGenericError(VIOLATION_EXCEPTION, 400, ErrorType.BAD_REQUEST, null, 
				DESCRIPTION_ALREADY_EXISTS);
	}
	
	@Test
	public void testUpdateDescriptionOtherDataException() {
		HibernateException exception = new HibernateException(new Exception("Too much content"));
		testUpdateDescriptionGenericError(exception, 400, ErrorType.BAD_REQUEST, null, 
				exception.getCause().getMessage());
	}

	@Test
	public void testUpdateDescriptionNotKnownException() throws Exception {
		String exceptionMsg = "SERVER ERROR";
		Exception e = new RuntimeException("", new Exception(exceptionMsg));

		testUpdateDescriptionGenericError(e, 500, ErrorType.INTERNAL_SERVER_ERROR, null, exceptionMsg);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// DELETE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private void testDeleteDescriptionGenericError(int status, ErrorType errorType, 
			String message) {

		try {
			// Call the method
			Response res = descriptionRegistrationService.deleteDescription(
					STORE_NAME, DESCRIPTION_NAME);

			// Assertions
			GenericRestTestUtils.checkAPIError(res, status, errorType, message);

			// Verify mocks
			verify(descriptionBoMock).delete(STORE_NAME, DESCRIPTION_NAME);	
		} catch (Exception ex) {
			fail("Exception " + ex + " not expected.");
		}
	}
	
	@Test
	public void testDeleteDescriptionNotAllowed() throws Exception {
		
		// Mocks
		Exception e = new NotAuthorizedException("delete description");
		doThrow(e).when(descriptionBoMock).delete(STORE_NAME, DESCRIPTION_NAME);
		
		testDeleteDescriptionGenericError(403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	@Test 
	public void testDeleteDescriptionStoreNotFound() throws Exception {
		
		// Mocks
		String exceptionMsg = "Store not found";
		doThrow(new StoreNotFoundException(exceptionMsg)).when(descriptionBoMock).
				delete(STORE_NAME, DESCRIPTION_NAME);
		
		testDeleteDescriptionGenericError(404, ErrorType.NOT_FOUND, exceptionMsg);
	}
	
	@Test 
	public void testDeleteDescriptionNotFound() throws Exception {
		// Mocks
		String exceptionMsg = "Description not found";
		doThrow(new DescriptionNotFoundException(exceptionMsg)).when(descriptionBoMock).
				delete(STORE_NAME, DESCRIPTION_NAME);
		
		testDeleteDescriptionGenericError(404, ErrorType.NOT_FOUND, exceptionMsg);
	}
	
	@Test
	public void testDeleteDescriptionNotKnownException() throws Exception {
		String exceptionMsg = "SERVER ERROR";
		doThrow(new RuntimeException("", new Exception(exceptionMsg))).
				when(descriptionBoMock).delete(STORE_NAME, DESCRIPTION_NAME);

		testDeleteDescriptionGenericError(500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	
	@Test
	public void testDeleteDescriptionNoErrors() throws Exception {
		// Call the method
		Response res = descriptionRegistrationService.deleteDescription(
				STORE_NAME, DESCRIPTION_NAME);

		// Assertions
		assertThat(res.getStatus()).isEqualTo(204);

		// Verify mocks
		verify(descriptionBoMock).delete(STORE_NAME, DESCRIPTION_NAME);	
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////// GET ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testGetDescriptionNotAllowed() throws Exception {
		
		// Mocks
		Exception e = new NotAuthorizedException("get description");
		doThrow(e).when(descriptionBoMock).findByNameAndStore(STORE_NAME, DESCRIPTION_NAME);

		// Call the method
		Response res = descriptionRegistrationService.getDescription(STORE_NAME, DESCRIPTION_NAME);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	@Test
	public void testGetDescriptionNoErrors() throws Exception {
		
		// Mocks
		when(descriptionBoMock.findByNameAndStore(STORE_NAME, DESCRIPTION_NAME)).thenReturn(description);

		// Call the method
		Response res = descriptionRegistrationService.getDescription(STORE_NAME, DESCRIPTION_NAME);

		// Assertions
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat((Description) res.getEntity()).isEqualTo(description);
	}
	
	private void testGetDescriptionGenericError(int status, ErrorType errorType, String message) {

		try {
			// Call the method
			Response res = descriptionRegistrationService.getDescription(
					STORE_NAME, DESCRIPTION_NAME);

			// Assertions
			GenericRestTestUtils.checkAPIError(res, status, errorType, message);
		} catch (Exception ex) {
			fail("Exception " + ex + " not expected.");
		}
	}
	
	@Test
	public void testGetDescriptionStoreNotFound() throws Exception {
		// Mocks
		String exceptionMsg = "Store not found";
		doThrow(new StoreNotFoundException(
				exceptionMsg)).when(descriptionBoMock).findByNameAndStore(
						STORE_NAME, DESCRIPTION_NAME);
		
		testGetDescriptionGenericError(404, ErrorType.NOT_FOUND, exceptionMsg);
	}
	
	@Test
	public void testGetDescriptionNotKnownException() throws Exception {
		
		String exceptionMsg = "SERVER ERROR";
		doThrow(new RuntimeException("", 
				new Exception(exceptionMsg))).when(descriptionBoMock).
						findByNameAndStore(STORE_NAME, DESCRIPTION_NAME);
		
		testGetDescriptionGenericError(500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
	

	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////// LIST ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void testListDescriptionsNotAllowed() throws Exception {
		// Mocks
		Exception e = new NotAuthorizedException("get description");
		doThrow(e).when(descriptionBoMock).getStoreDescriptionsPage(eq(STORE_NAME), anyInt(), anyInt());

		// Call the method
		Response res = descriptionRegistrationService.listDescriptionsInStore(STORE_NAME, 0, 100);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	
	private void testListDescriptionsInvalidParams(int offset, int max) {
		// Call the method
		Response res = descriptionRegistrationService.listDescriptionsInStore(STORE_NAME, offset, max);

		// Assertions
		GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, OFFSET_MAX_INVALID);
	}
	
	@Test
	public void testListDescriptionsInvalidOffset() {
		testListDescriptionsInvalidParams(-1, 100);
	}
	
	@Test
	public void testListDescriptionsInvalidMax() {
		testListDescriptionsInvalidParams(0, 0);
	}
	
	@Test
	public void testListDescriptionsInvalidOffsetMax() {
		testListDescriptionsInvalidParams(-1, -1);
	}
	
	@Test
	public void testListDescriptionsNoErrors() throws Exception {
		List<Description> descriptions = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			Description description = new Description();
			description.setId(i);
			descriptions.add(description);
		}
		
		// Mocks
		when(descriptionBoMock.getStoreDescriptionsPage(eq(STORE_NAME), anyInt(), anyInt())).thenReturn(descriptions);
		
		// Call the method
		int offset = 5;
		int max = 8;
		Response res = descriptionRegistrationService.listDescriptionsInStore(STORE_NAME, offset, max);
		
		// Chceks
		verify(descriptionBoMock).getStoreDescriptionsPage(STORE_NAME, offset, max);
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(((Descriptions) res.getEntity()).
				getDescriptions()).isEqualTo(descriptions);

	}
	
	@Test
	public void testListDescriptionsException() throws Exception {
		// Mocks
		String exceptionMsg = "exception";
		doThrow(new RuntimeException("", new Exception(exceptionMsg))).when(descriptionBoMock)
				.getStoreDescriptionsPage(eq(STORE_NAME), anyInt(), anyInt());

		// Call the method
		int offset = 0;
		int max = 100;
		Response res = descriptionRegistrationService.listDescriptionsInStore(STORE_NAME, offset, max);
		
		// Verify
		verify(descriptionBoMock).getStoreDescriptionsPage(STORE_NAME, offset, max);
		
		// Check exception
		GenericRestTestUtils.checkAPIError(res, 500, ErrorType.INTERNAL_SERVER_ERROR, exceptionMsg);
	}
}
