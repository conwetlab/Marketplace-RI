package org.fiware.apps.marketplace.it;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2015 CoNWeT Lab, Universidad Politécnica de Madrid
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

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;	

import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Descriptions;
import org.fiware.apps.marketplace.model.ErrorType;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.Offerings;
import org.junit.After;
import org.junit.Test;


public class DescriptionServiceIT extends AbstractIT {
	
	private final static String USER_NAME = "marketplace";
	private final static String PASSWORD = "password1!a";
	private final static String EMAIL = "example@example.com";
	private final static String FIRST_STORE_NAME = "wstore";
	private final static String FIRST_STORE_URL = "http://store.lab.fiware.org";
	private final static String SECOND_STORE_NAME = "wstore-testbed";
	private final static String SECOND_STORE_URL = "http://store.testbed.fiware.org";
	
	private final static String MESSAGE_NAME_IN_USE = "This name is already in use in this Store.";
	private final static String MESSAGE_URL_IN_USE = "This URL is already in use in this Store.";
	private final static String MESSAGE_INVALID_RDF = "Your RDF could not be parsed.";
	private final static String MESSAGE_DESCRIPTION_NOT_FOUND = "Description %s not found";

	@Override
	public void specificSetUp() {
		createUser(USER_NAME, EMAIL, PASSWORD);
		createStore(USER_NAME, PASSWORD, FIRST_STORE_NAME, FIRST_STORE_URL);

		startMockServer();
	}

	@After
	public void stopMockServer() {
		wireMock.stop();
	}

	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// AUXILIAR //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	private Response getDescription(String userName, String password, String storeName, String descriptionName) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + descriptionName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	private void checkDescription(String userName, String password, String storeName, 
			String descriptionName, String displayName, String url, String comment) {
		
		Description description = getDescription(userName, password, storeName, descriptionName)
				.readEntity(Description.class);
		
		assertThat(description.getName()).isEqualTo(descriptionName);
		assertThat(description.getDisplayName()).isEqualTo(displayName);
		assertThat(description.getUrl()).isEqualTo(url);
		assertThat(description.getComment()).isEqualTo(comment);
		
		// Check offerings
		Offering[] expectedOfferings;
		if (url == secondaryUSDLPath) {
			expectedOfferings = new Offering[] {FIRST_OFFERING, SECOND_OFFERING};
		} else {	// defaultUSDLPath
			expectedOfferings = new Offering[] {FIRST_OFFERING};
		}
		
		List<Offering> descriptionOfferings = description.getOfferings();
		assertThat(descriptionOfferings.size()).isEqualTo(expectedOfferings.length);
		
		for (Offering expectedOffering: expectedOfferings) {
			assertThat(expectedOffering).isIn(descriptionOfferings);
		}
	}
	
	private Response createOrUpdateDescription(String userName, String password, String storeName, 
			String descriptionName, String displayName, String url, String comment) {
		
		Description description = new Description();
		description.setDisplayName(displayName);
		description.setUrl(url);
		description.setComment(comment);
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + descriptionName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password))
				.post(Entity.entity(description, MediaType.APPLICATION_JSON));
		
		return response;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// CREATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	private Response createDescription(String userName, String password, String storeName, String displayName,
			String url, String comment) {
		return createOrUpdateDescription(userName, password, storeName, "", displayName, url, comment);
	}
	
	private void testCreation(String url) {
		String displayName = "Description 1";
		String descriptionName = "description-1";
		String descriptionComment = "Example Comment";
		
		Response response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, url, 
				descriptionComment);	
		assertThat(response.getStatus()).isEqualTo(201);
		assertThat(response.getHeaderString("Location")).isEqualTo(endPoint + "/api/v2/store/" + FIRST_STORE_NAME +
				"/description/" + descriptionName);
		
		// Check that the description actually exists
		checkDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, descriptionName, displayName, url, 
				descriptionComment);

	}
	
	@Test
	public void testCreationDefaultUSDL() {
		testCreation(defaultUSDLPath);
	}
	
	@Test
	public void testCreationSecondaryUSDL() {
		testCreation(secondaryUSDLPath);
	}
	
	private void testCreationInvalidField(String displayName, String url, String comment, String invalidField,
			String message) {

		Response response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, url, comment);
		checkAPIError(response, 400, invalidField, message, ErrorType.VALIDATION_ERROR);
	}
	
	@Test
	public void testCreationDisplayNameInvalid() {
		testCreationInvalidField("Description!", defaultUSDLPath, "", "displayName", 
				MESSAGE_INVALID_DISPLAY_NAME);
	}
	
	@Test
	public void testCreationDisplayNameTooShort() {
		testCreationInvalidField("a", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testCreationDisplayNameTooLong() {
		testCreationInvalidField("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"
				+ "abcdefghijklmnopqrstuvw", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_LONG, 100));
	}
	
	@Test
	public void testCreationURLInvalid() {
		testCreationInvalidField("Description", "https:/127.0.0.1:" + wireMock.port(), "", "url", 
				MESSAGE_INVALID_URL);
	}
	
	@Test
	public void testCreationRDFInvalid() {
		testCreationInvalidField("Description", serverUrl, "", "url", MESSAGE_INVALID_RDF);

	}
	
	@Test
	public void testCreationCommentTooLong() {
		testCreationInvalidField("Description", defaultUSDLPath, "12345678901234567890123456789012345678901234567890"
				+ "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"
				+ "7890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
				"comment", String.format(MESSAGE_TOO_LONG, 200));
	}
	
	private void testCreationFieldAlreayExists(String displayName1, String displayName2, String url1, String url2,
			String field, String expectedMessage) {

		createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName1, url1, "");
		Response response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName2, url2, "");
		
		checkAPIError(response, 400, field, expectedMessage, ErrorType.VALIDATION_ERROR);

	}
	
	@Test
	public void testCreationDisplayNameAlreadyExists() {		
		String displayName = "Description 1";
		
		// name is based on display name and name is checked before display name...
		testCreationFieldAlreayExists(displayName, displayName, defaultUSDLPath, defaultUSDLPath + "a", "name", 
				MESSAGE_NAME_IN_USE);
	}
	
	@Test
	public void testCreationURLAlreadyExists() {
		testCreationFieldAlreayExists("offering-1", "offering-2", defaultUSDLPath, defaultUSDLPath, "url", 
				MESSAGE_URL_IN_USE);
	}
	
	@Test
	public void testCreationNameAndUrlAlreayExistsInAnotherStore() {

		// Create another Store
		String descriptionName = "description-1"; 
		
		createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		
		Response createResponse1 = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionName, defaultUSDLPath, "");
		Response createResponse2 = createDescription(USER_NAME, PASSWORD, SECOND_STORE_NAME, 
				descriptionName, defaultUSDLPath, "");
		
		// Both offerings can be created
		assertThat(createResponse1.getStatus()).isEqualTo(201);
		assertThat(createResponse2.getStatus()).isEqualTo(201);
		
	}
	
	@Test
	public void testDeleteUserWithDescription() {
		String name = "description-1";
		
		Response createStoreResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, "");
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		checkDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, name, defaultUSDLPath, "");
		
		// Delete user
		Response deleteUserResponse = deleteUser(USER_NAME, PASSWORD, USER_NAME);
		assertThat(deleteUserResponse.getStatus()).isEqualTo(204);
		
		// Create another user to be able to check the store
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		Response createUserResponse = createUser(newUserName, email, PASSWORD);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		// Check that the Store does not exist anymore
		Response getStoreResponse = getDescription(newUserName, PASSWORD, FIRST_STORE_NAME, name);
		checkAPIError(getStoreResponse, 404, null, String.format(MESSAGE_STORE_NOT_FOUND, FIRST_STORE_NAME), 
				ErrorType.NOT_FOUND);
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// UPDATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response updateDescription(String userName, String password, String storeName, 
			String descriptionName, String displayName, String url, String comment) {
		return createOrUpdateDescription(userName, password, storeName, descriptionName, displayName, url, comment);
	}
	
	private void testUpdate(String newDisplayName, String newUrl, String newComment) {
		// Create Description
		String name = "description-1";
		String displayName = "Description-1";
		String comment = "commnet1";
		
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, comment);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Update the description		
		Response updateDescriptionResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				newDisplayName, newUrl, newComment);
		assertThat(updateDescriptionResponse.getStatus()).isEqualTo(200);
		
		// Check that the description has been updated
		String expectedDisplayName = newDisplayName == null ? displayName : newDisplayName;
		String expectedUrl = newUrl == null ? defaultUSDLPath : newUrl;
		String expectedComment = newComment == null ? comment : newComment;
		
		checkDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, expectedDisplayName, 
				expectedUrl, expectedComment);

	}
	
	@Test
	public void testUpdateNameAndDescription() {
		testUpdate("Description 2", null, "comment-2");
	}
	
	@Test
	public void tesUpdateUrlSameUrl() {
		testUpdate(null, defaultUSDLPath, null);
	}
	
	@Test
	public void testUpdateUrlDifferentUrl() {
		testUpdate(null, secondaryUSDLPath, null);
	}
	
	private void testUpdateInvalidField(String newDisplayName, String newUrl, String newComment, 
			String invalidField, String message) {
		
		String name = "offering";
		String displayName = "Offering";
		String comment = "";
		
		Response createResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, comment);
		assertThat(createResponse.getStatus()).isEqualTo(201);

		Response updateResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, newDisplayName, 
				newUrl, newComment);
		checkAPIError(updateResponse, 400, invalidField, message, ErrorType.VALIDATION_ERROR);
	}
	
	@Test
	public void testUpdateDisplayNameInvalid() {
		testUpdateInvalidField("Description!", defaultUSDLPath, "", "displayName", 
				MESSAGE_INVALID_DISPLAY_NAME);
	}
	
	@Test
	public void testUpdateDisplayNameTooShort() {
		testUpdateInvalidField("a", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testUpdateDisplayNameTooLong() {
		testUpdateInvalidField("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"
				+ "abcdefghijklmnopqrstuvw", defaultUSDLPath, "", "displayName", 
				String.format(MESSAGE_TOO_LONG, 100));
	}
	
	@Test
	public void testUpdateURLInvalid() {
		testUpdateInvalidField("Description", "https:/store.lab.fiware.org/offering1.rdf", "", "url", 
				MESSAGE_INVALID_URL);
	}
	
	@Test
	public void testUpdateRDFInvalid() {
		testUpdateInvalidField("Description", serverUrl, "", "url", MESSAGE_INVALID_RDF);
	}
	
	@Test
	public void testUpdateCommentTooLong() {
		testUpdateInvalidField("Offering", serverUrl, "12345678901234567890123456789012345678901234567890"
				+ "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"
				+ "7890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 
				"comment", String.format(MESSAGE_TOO_LONG, 200));
	}
	
	/**
	 * This methods creates two descriptions, based on the different parameters and tries to updates the second
	 * description based on updatedDisplayName and updatedURL. However, it's expected that one of these parameters
	 * has been used to create the first description, so an error should arise. Error details should be contained in
	 * field an expectedMessage.
	 * @param nameDescription1 The name of the first description
	 * @param urlDescription1 The URL of the first description
	 * @param nameDescription2 The name of the second description (the one to be updated). This name is used to modify 
	 * the description so this is not a displayName but the name (the one without spaces, ...) 
	 * @param urlDescription2 The URL of the second description (the one to be updated)
	 * @param updatedDisplayName The new display name to be set in the second description
	 * @param updatedURL The new URL to be set in the second description
	 * @param field The field that is repeated
	 * @param expectedMessage Expected error message
	 */
	private void testUpdateFieldAlreayExists(
			String nameDescription1,String urlDescription1, 
			String nameDescription2, String urlDescription2,
			String updatedDisplayName, String updatedURL,
			String field, String expectedMessage) {
		
		Response createStore1Response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, nameDescription1, 
				urlDescription1, "");
		Response createStore2Response = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, nameDescription2, 
				urlDescription2, "");
		assertThat(createStore1Response.getStatus()).isEqualTo(201);
		assertThat(createStore2Response.getStatus()).isEqualTo(201);
		
		Response updateResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, nameDescription2, 
				updatedDisplayName, updatedURL, null);
		checkAPIError(updateResponse, 400, field, expectedMessage, ErrorType.VALIDATION_ERROR);

	}
	
	@Test
	public void testUpdateDisplayNameAlreadyExists() {		
		String displayName = "description";
		
		testUpdateFieldAlreayExists(
				displayName, defaultUSDLPath, 
				"descritpion-2", secondaryUSDLPath, 
				displayName, null, 
				"displayName", MESSAGE_NAME_IN_USE);	
	}
	
	@Test
	public void testUpdateURLAlreadyExists() {
		testUpdateFieldAlreayExists(
				"description", defaultUSDLPath, 
				"description-2", secondaryUSDLPath, 
				"description-2", defaultUSDLPath, 
				"url", MESSAGE_URL_IN_USE);	
	}
	
	@Test
	public void testUpdateNonExisting() {
		
		String displayName = "offering-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Update non-existing description
		String descriptionToBeUpdated = displayName + "a";  	//This ID is supposed not to exist
		Response updateDescriptionResponse = updateDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, 
				descriptionToBeUpdated, "new display", null, null);
		checkAPIError(updateDescriptionResponse, 404, null, 
				String.format(MESSAGE_DESCRIPTION_NOT_FOUND, descriptionToBeUpdated), ErrorType.NOT_FOUND);	
	}
	
	@Test
	public void testUpdateWithAnotherUser() {
		
		String displayName = "offering-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, displayName, 
				defaultUSDLPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);

		// Create another user
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		createUser(newUserName, email, PASSWORD);
		
		// Update description with the new user
		Response updateDescriptionResponse = updateDescription(newUserName, PASSWORD, FIRST_STORE_NAME, displayName, 
				"new display name", null, null);
		checkAPIError(updateDescriptionResponse, 403, null, 
				String.format(MESSAGE_NOT_AUTHORIZED, "update description"), ErrorType.FORBIDDEN);	

	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// DELETE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	private Response deleteDescription(String authUserName, String authPassword, String storeName, 
			String descriptionName) {
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + descriptionName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(authUserName, authPassword))
				.delete();
		
		return response;

	}
	
	@Test
	public void testDelete() {
		
		String name = "description-1";
		
		// Create the description
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, null);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Delete the description
		Response deleteDescriptionResponse = deleteDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name);
		assertThat(deleteDescriptionResponse.getStatus()).isEqualTo(204);
	}
	
	@Test
	public void testDeleteNonExisting() {
		
		String name = "description-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, null);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Delete non-existing description
		String descriptionToBeDeleted = name + "a";  	//This ID is supposed not to exist
		Response deleteStoreResponse = deleteDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, descriptionToBeDeleted);
		checkAPIError(deleteStoreResponse, 404, null, 
				String.format(MESSAGE_DESCRIPTION_NOT_FOUND, descriptionToBeDeleted), ErrorType.NOT_FOUND);	
	}
	
	@Test
	public void testDeleteWithAnotherUser() {
		
		String name = "description-1";
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, name, 
				defaultUSDLPath, null);
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);

		// Create another user
		String newUserName = USER_NAME + "a";
		String email = "new_email__@example.com";
		createUser(newUserName, email, PASSWORD);
		
		//Delete user
		Response deleteStoreResponse = deleteDescription(newUserName, PASSWORD, FIRST_STORE_NAME, name);
		checkAPIError(deleteStoreResponse, 403, null, String.format(MESSAGE_NOT_AUTHORIZED, "delete description"), 
				ErrorType.FORBIDDEN);	

	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// LIST DESCRIPTIONS //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testListAllDescriptionsInStore() {
		
		int DESCRIPTIONS_CREATED = 6;
		
		// Create some descriptions
		String displayNamePattern = "Store %d";
		String urlPattern = serverUrl + "/default%d.rdf";
		
		for (int i = 0; i < DESCRIPTIONS_CREATED; i++) {
			createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, String.format(displayNamePattern, i), 
					String.format(urlPattern, i), "");
		}
		
		// Get all descriptions
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/description")
				.request(MediaType.APPLICATION_JSON).header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check the response
		assertThat(response.getStatus()).isEqualTo(200);
		Descriptions descriptions = response.readEntity(Descriptions.class);
		assertThat(descriptions.getDescriptions().size()).isEqualTo(DESCRIPTIONS_CREATED);
		
		// Users are supposed to be returned in order
		for (int i = 0; i < DESCRIPTIONS_CREATED; i++) {
			Description description = descriptions.getDescriptions().get(i);
			assertThat(description.getDisplayName()).isEqualTo(String.format(displayNamePattern, i));
			assertThat(description.getUrl()).isEqualTo(String.format(urlPattern, i));
		}
	}
	
	private void testListSomeDescriptionsInStore(int offset, int max) {
		
		int DESCRIPTIONS_CREATED = 10;
		
		// Create some descriptions
		String displayNamePattern = "Store %d";
		String urlPattern = serverUrl + "/default%d.rdf";
		
		for (int i = 0; i < DESCRIPTIONS_CREATED; i++) {
			createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, String.format(displayNamePattern, i), 
					String.format(urlPattern, i), "");
		}
		
		// Get required descriptions
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/description")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check the response
		int expectedElements = offset + max > DESCRIPTIONS_CREATED ? DESCRIPTIONS_CREATED - offset : max;
		assertThat(response.getStatus()).isEqualTo(200);
		Descriptions descriptions = response.readEntity(Descriptions.class);
		assertThat(descriptions.getDescriptions().size()).isEqualTo(expectedElements);
		
		// Users are supposed to be returned in order
		for (int i = offset; i < offset + expectedElements; i++) {
			Description description = descriptions.getDescriptions().get(i - offset);
			assertThat(description.getDisplayName()).isEqualTo(String.format(displayNamePattern, i));
			assertThat(description.getUrl()).isEqualTo(String.format(urlPattern, i));
		}
	}
	
	@Test
	public void testListSomeDescriptionsMaxInRange() {
		testListSomeDescriptionsInStore(3, 4);
	}
	
	@Test
	public void testListSomeDescriptionsMaxNotInRange() {
		testListSomeDescriptionsInStore(5, 7);
	}
	
	private void testListDescriptionsInStoreInvalidParams(int offset, int max) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/description")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, MESSAGE_INVALID_OFFSET_MAX, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListDescriptionsInStoreInvalidOffset() {
		testListDescriptionsInStoreInvalidParams(-1, 2);
	}
	
	@Test
	public void testListDescriptionsInStoreInvalidMax() {
		testListDescriptionsInStoreInvalidParams(1, 0);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// ALL DESCRIPTIONS  //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	
	private void testListDescriptions(int offset, int max) {
		
		// Create an additional Store
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);
		
		// Create descriptions (2)
		Description[] originalDescriptions = new Description[2];
		
		Description description0 = new Description();
		description0.setName("default");
		description0.setUrl(defaultUSDLPath);

		Description description1 = new Description();
		description1.setName("secondary");
		description1.setUrl(secondaryUSDLPath);
		
		originalDescriptions[0] = description0;
		originalDescriptions[1] = description1;
		
		// Insert descriptions (2) into the Stores (2). Total: 4 descriptions
		String[] stores = new String[]{FIRST_STORE_NAME, SECOND_STORE_NAME};
		
		for (String store: stores) {
			
			for (Description description: originalDescriptions) {
				Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, store, 
						description.getName(), description.getUrl(), "");
				assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);

			}
		}
				
		// Get all descriptions
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/descriptions")
				.queryParam("offset", offset)
				.matrixParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		// Check that the right number of descriptions has been returned...
		Descriptions retrievedDescriptions = response.readEntity(Descriptions.class);
		int descriptionsCreated = stores.length * originalDescriptions.length;	// 2 descriptions
		int expectedElements = offset + max > descriptionsCreated ? descriptionsCreated - offset : max;
		assertThat(retrievedDescriptions.getDescriptions().size()).isEqualTo(expectedElements);
		
		// Check descriptions
		for (int i = 0; i < retrievedDescriptions.getDescriptions().size(); i++) {
			
			Description description = retrievedDescriptions.getDescriptions().get(i);
			int indexInGeneralArray = offset + i;
			
			// Check that store name is correct
			// Store 0: [DESC0, DESC1], Store 1: [DESC2, DESC3], ...
			int storeIndex = indexInGeneralArray / originalDescriptions.length;
			assertThat(description.getStore().getName()).isEqualTo(stores[storeIndex]);
			
			// Check that the description is correct
			// Even -> Description 1 (description1Name, defaultUSDLPath)
			// Odd -> Description 2 (description2Name, secondaryUSDLPath)
			int descriptionIndex = indexInGeneralArray % originalDescriptions.length;
			assertThat(description.getName()).isEqualTo(originalDescriptions[descriptionIndex].getName());
			assertThat(description.getUrl()).isEqualTo(originalDescriptions[descriptionIndex].getUrl());			
		}
		
	}
	
	@Test
	public void testListAllDescriptions() {
		testListDescriptions(0,	100);
	}
	
	@Test
	public void testListSomeDescriptions() {
		testListDescriptions(1, 3);
	}
	
	private void testListDescriptionsInvalidParams(int offset, int max) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/descriptions")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, MESSAGE_INVALID_OFFSET_MAX, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListDescriptionsInvalidOffset() {
		testListDescriptionsInvalidParams(-1, 2);
	}
	
	@Test
	public void testListDescriptionsInvalidMax() {
		testListDescriptionsInvalidParams(1, 0);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// STORE OFFERINGS ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response getStoreOfferings(String userName, String password, String storeName) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + storeName + "/offering/")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}
	
	private Response getStoreOfferings(String userName, String password, String storeName, int offset, int max) {
		Client client = ClientBuilder.newClient();
		return client.target(endPoint + "/api/v2/store/" + storeName + "/offering/")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(userName, password)).get();
	}

	
	@Test
	public void testStoreOfferings() {
		
		// Create an additional Store
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);

		// Push each description in a different store
		Response createDesc1Res = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, "displayName", 
				defaultUSDLPath, "");
		Response createDesc2Res = createDescription(USER_NAME, PASSWORD, SECOND_STORE_NAME, "secondary", 
				secondaryUSDLPath, "");
		
		assertThat(createDesc1Res.getStatus()).isEqualTo(201);
		assertThat(createDesc2Res.getStatus()).isEqualTo(201);
		
		// Get Store1 offerings
		Response store1OfferingResponse = getStoreOfferings(USER_NAME, PASSWORD, FIRST_STORE_NAME);
		assertThat(store1OfferingResponse.getStatus()).isEqualTo(200);
		
		Offerings offerings = store1OfferingResponse.readEntity(Offerings.class);
		assertThat(offerings.getOfferings().size()).isEqualTo(1);
		assertThat(FIRST_OFFERING).isIn(offerings.getOfferings());
		
		// Get Store2 offerings
		Response store2OfferingResponse = getStoreOfferings(USER_NAME, PASSWORD, SECOND_STORE_NAME);
		assertThat(store2OfferingResponse.getStatus()).isEqualTo(200);
		
		offerings = store2OfferingResponse.readEntity(Offerings.class);
		assertThat(offerings.getOfferings().size()).isEqualTo(2);
				
		assertThat(FIRST_OFFERING).isIn(offerings.getOfferings());
		assertThat(SECOND_OFFERING).isIn(offerings.getOfferings());
	}
	
	private void testGetSomeStoreOfferings(int offset, int max) {
		
		int OFFERINGS_IN_DESCRIPTION = 2;
		
		// We are using the description that contains two offerings
		// and checking if offset and max works in an appropriate way
		Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, FIRST_STORE_NAME, "displayName", 
				secondaryUSDLPath, "");
		assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
		
		// Check that the number of returned offerings is correct
		Response storeOfferingResponse = getStoreOfferings(USER_NAME, PASSWORD, FIRST_STORE_NAME, offset, max);
		Offerings offerings = storeOfferingResponse.readEntity(Offerings.class);
		int expectedElements = offset + max > OFFERINGS_IN_DESCRIPTION ? OFFERINGS_IN_DESCRIPTION - offset : max;
		assertThat(offerings.getOfferings().size()).isEqualTo(expectedElements);
	}
	
	@Test
	public void testGetFirstStoreOffering() {
		testGetSomeStoreOfferings(0, 1);
	}
	
	@Test
	public void tesGetSecondStoreOffering() {
		testGetSomeStoreOfferings(1, 1);
	}
	
	@Test
	public void tesGetAllStoreOffering() {
		testGetSomeStoreOfferings(0, 2);
	}
	
	private void testListOfferingsInStoreInvalidParams(int offset, int max) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + FIRST_STORE_NAME + "/offering")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, MESSAGE_INVALID_OFFSET_MAX, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListOfferingsInStoreInvalidOffset() {
		testListOfferingsInStoreInvalidParams(-1, 2);
	}
	
	@Test
	public void testListOfferingsInStoreInvalidMax() {
		testListOfferingsInStoreInvalidParams(1, 0);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// ALL OFFERINGS ////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private void intializeStoresWithOfferings(String firstDescriptionName, String secondDescriptionName) {
		// Create an additional Store
		Response createStoreResponse = createStore(USER_NAME, PASSWORD, SECOND_STORE_NAME, SECOND_STORE_URL);
		assertThat(createStoreResponse.getStatus()).isEqualTo(201);

		// Push both descriptions in both stores
		String[] stores = new String[]{FIRST_STORE_NAME, SECOND_STORE_NAME};
		
		for (String store: stores) {
			
			Response createDescriptionResponse = createDescription(USER_NAME, PASSWORD, store, 
					firstDescriptionName, defaultUSDLPath, "");
			assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
			
			createDescriptionResponse = createDescription(USER_NAME, PASSWORD, store, 
					secondDescriptionName, secondaryUSDLPath, "");
			assertThat(createDescriptionResponse.getStatus()).isEqualTo(201);
			
		}

	}
	
	@Test
	public void testGetAllOfferings() {
		
		final int TOTAL_OFFERINGS = 6;			// 6 offerings: 3 in each store.
		
		intializeStoresWithOfferings("default", "secondary");
		
		// Get all the offerings
		Client client = ClientBuilder.newClient();
		Response allOfferingsResponse = client.target(endPoint + "/api/v2/offerings")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		assertThat(allOfferingsResponse.getStatus()).isEqualTo(200);
		
		Offerings offerings = allOfferingsResponse.readEntity(Offerings.class);
		assertThat(offerings.getOfferings().size()).isEqualTo(TOTAL_OFFERINGS);

	}
	
	private void testGetSomeOfferings(int offset, int max) {
		
		final int TOTAL_OFFERINGS = 6;
		
		intializeStoresWithOfferings("default", "secondary");
		
		// Get all the offerings
		Client client = ClientBuilder.newClient();
		Response allOfferingsResponse = client.target(endPoint + "/api/v2/offerings")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		assertThat(allOfferingsResponse.getStatus()).isEqualTo(200);
		
		Offerings offerings = allOfferingsResponse.readEntity(Offerings.class);
		int expectedElements = offset + max > TOTAL_OFFERINGS ? TOTAL_OFFERINGS - offset : max;
		assertThat(offerings.getOfferings().size()).isEqualTo(expectedElements);

	}
	
	@Test
	public void testGetFirstTwoElements() {
		testGetSomeOfferings(0, 2);
	}
	
	@Test
	public void testGetMoreElementsThanExisting() {
		testGetSomeOfferings(3, 9);
	}
	
	private void testListOfferingsInvalidParams(int offset, int max) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/offerings")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		checkAPIError(response, 400, null, MESSAGE_INVALID_OFFSET_MAX, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListOfferingsInvalidOffset() {
		testListOfferingsInvalidParams(-1, 2);
	}
	
	@Test
	public void testListOfferingsInvalidMax() {
		testListOfferingsInvalidParams(1, 0);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// BOOKMARKS //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response getBookmarkedOfferings() {
		// Get all the offerings
		Client client = ClientBuilder.newClient();
		Response allOfferingsResponse = client.target(endPoint + "/api/v2/offerings")
				.queryParam("bookmarked", true)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.get();
		
		return allOfferingsResponse;
	}
	
	/**
	 * This method bookmarks or unbookmarks an offering depending on its previous state
	 * @param storeName The name of the store where the offering to be bookmarked is contained
	 * @param descriptionName The name of the descritpion where the offering to be bookmarked is described 
	 * @param offeringName The name of the offering to be bookmarked
	 * @return The response from the server
	 */
	private Response bookmarkOrUnbookmarkOffering(String storeName, String descriptionName, String offeringName) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/store/" + storeName + "/description/" + 
					descriptionName + "/offering/" + offeringName + "/bookmark")
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", getAuthorization(USER_NAME, PASSWORD))
				.post(null);

		return response;
	}
	
	@Test
	public void testBookmarkAndUnbookmarkOffering() {
		
		String firstDescriptionName = "default";
		String secondDescriptionName = "secondary";
		intializeStoresWithOfferings(firstDescriptionName, secondDescriptionName);
		Offering bookmarkedOffering = SECOND_OFFERING;
		
		// Bookmark one offering
		Response bookmarkResponse = bookmarkOrUnbookmarkOffering(FIRST_STORE_NAME, secondDescriptionName, 
				bookmarkedOffering.getName());
		assertThat(bookmarkResponse.getStatus()).isEqualTo(204);
		
		// Check that bookmarked offerings contains the bookmarked offering
		Response bookmarkedOfferingsResponse = getBookmarkedOfferings();
		assertThat(bookmarkedOfferingsResponse.getStatus()).isEqualTo(200);
		List<Offering> bookmarkedOfferings = bookmarkedOfferingsResponse.readEntity(Offerings.class).getOfferings();
		assertThat(bookmarkedOfferings.size()).isEqualTo(1);
		assertThat(bookmarkedOfferings.get(0)).isEqualTo(bookmarkedOffering);
		assertThat(bookmarkedOfferings.get(0).getDescribedIn().getName()).isEqualTo(secondDescriptionName);
		assertThat(bookmarkedOfferings.get(0).getDescribedIn().getStore().getName()).isEqualTo(FIRST_STORE_NAME);
		
		// Unbookmark the offering
		Response unbookmarkResponse = bookmarkOrUnbookmarkOffering(FIRST_STORE_NAME, secondDescriptionName, 
				bookmarkedOffering.getName());
		assertThat(unbookmarkResponse.getStatus()).isEqualTo(204);

		// Check that bookmarked offerings is empty
		bookmarkedOfferingsResponse = getBookmarkedOfferings();
		assertThat(bookmarkedOfferingsResponse.getStatus()).isEqualTo(200);
		bookmarkedOfferings = bookmarkedOfferingsResponse.readEntity(Offerings.class).getOfferings();
		assertThat(bookmarkedOfferings).isEmpty();
	}
	
	@Test
	public void testBookmarkTwoOfferings() {
		
		String firstDescriptionName = "default";
		String secondDescriptionName = "secondary";
		Offering bookmarkedOffering = SECOND_OFFERING;
		intializeStoresWithOfferings(firstDescriptionName, secondDescriptionName);

		// Bookmark the offerings from different stores (same offering in different stores)
		String[] stores = {FIRST_STORE_NAME, SECOND_STORE_NAME};
		for (String storeName: stores) {
			Response bookmarkResponse = bookmarkOrUnbookmarkOffering(storeName, secondDescriptionName, 
					bookmarkedOffering.getName());
			assertThat(bookmarkResponse.getStatus()).isEqualTo(204);
		}
		
		// Check that bookmarked offerings contains both offerings
		Response bookmarkedOfferingsResponse = getBookmarkedOfferings();
		assertThat(bookmarkedOfferingsResponse.getStatus()).isEqualTo(200);
		List<Offering> bookmarkedOfferings = bookmarkedOfferingsResponse.readEntity(Offerings.class).getOfferings();
		assertThat(bookmarkedOfferings.size()).isEqualTo(stores.length);
		
		for (int i = 0; i < bookmarkedOfferings.size(); i++) {
			assertThat(bookmarkedOfferings.get(i)).isEqualTo(bookmarkedOffering);
			assertThat(bookmarkedOfferings.get(i).getDescribedIn().getStore().getName()).isEqualTo(stores[i]);
		}
	}
}
