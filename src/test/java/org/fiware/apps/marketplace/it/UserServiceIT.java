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


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fiware.apps.marketplace.model.ErrorType;
import org.fiware.apps.marketplace.model.Users;
import org.junit.Test;


public class UserServiceIT extends AbstractIT {
	
	private final static String MESSAGE_INVALID_DISPLAY_NAME = 
			"This field only accepts letters and white spaces.";
	private final static String MESSAGE_EMAIL_ALREADY_REGISTERED = "This email is already registered.";
	private final static String MESSAGE_INVALID_EMAIL = "This field must be a valid email.";
	private final static String MESSAGE_INVALID_PASSWORD = "Password must contain one number, one letter and one "
				+ "unique character such as !#$%&?";
	private final static String MESSAGE_USER_NOT_FOUND = "User %s not found";
	
	
	public void specificSetUp() {
		// No actions are required
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// CREATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
			
	@Test
	public void testUserCreation() {
		
		String userName = "fiware-example";
		String displayName = "FIWARE Example";
		String email = "example@example.com";
		String password = "password!1";
		String company = "UPM";
		
		Response response = createUser(displayName, email, password, company);
		
		assertThat(response.getStatus()).isEqualTo(201);
		assertThat(response.getHeaderString("Location")).isEqualTo(endPoint + "/api/v2/user/" + userName);
		
		checkUser(userName, displayName, company);
	}
	
	@Test
	public void testUserCreationUserRegistered() throws InterruptedException {
		
		String mail = "example@example.com";
		
		createUser("FIWARE Example", mail, "password!1a");
		Response userExistResponse = createUser("Other user name", mail, "anotherPassword!1");
				
		checkAPIError(userExistResponse, 400, "email", MESSAGE_EMAIL_ALREADY_REGISTERED, 
				ErrorType.VALIDATION_ERROR);		
	}
	
	@Test
	public void testUserCreationInvalidDisplayName() {
		Response response = createUser("FIWARE Example 1", "example@example.com", "password!1");
		checkAPIError(response, 400, "displayName", MESSAGE_INVALID_DISPLAY_NAME, 
				ErrorType.VALIDATION_ERROR);	

	}
	
	@Test
	public void testUserCreationDisplayNameMissing() {
		Response response = createUser("", "example@example.com", "password!1");
		checkAPIError(response, 400, "displayName", MESSAGE_FIELD_REQUIRED, ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationDisplayNameTooLong() {
		Response response = createUser("ABCDEFGHIJKMLNOPQRSTUVWXYZABCDEFGHIJKMLNOPQRSTUVWXYZ", 
				"example@example.com", "password!1");
		checkAPIError(response, 400, "displayName", String.format(MESSAGE_TOO_LONG, 30), 
				ErrorType.VALIDATION_ERROR);	
	}
	
	public void testUserCreationDisplayNameTooShort() {
		Response response = createUser("a", "example@example.com", "password!1");
		checkAPIError(response, 400, "displayName", String.format(MESSAGE_TOO_SHORT, 3), 
				ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationInvalidMail() {
		Response response = createUser("FIWARE Example", "example@examplecom", "password!1");
		checkAPIError(response, 400, "email", MESSAGE_INVALID_EMAIL, ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationDisplayEmailMissing() {
		Response response = createUser("FIWARE Example", "", "password!1");
		checkAPIError(response, 400, "email", MESSAGE_FIELD_REQUIRED, ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationInvalidPassword() {
		Response response = createUser("FIWARE Example", "example@example.com", "password!");
		checkAPIError(response, 400, "password", MESSAGE_INVALID_PASSWORD, ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationDisplayPasswordMissing() {
		Response response = createUser("FIWARE Example", "example@example.com", "");
		checkAPIError(response, 400, "password", MESSAGE_FIELD_REQUIRED, ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationPasswordTooLong() {
		Response response = createUser("FIWARE EXAMPLE", "example@example.com", 
				"ABCDEFGHIJKMLNOPQRSTUVWXYZABCDEFGHIJKMLNOPQRSTU!1VWXYZ");
		checkAPIError(response, 400, "password", String.format(MESSAGE_TOO_LONG, 30), ErrorType.VALIDATION_ERROR);	
	}
	
	public void testUserCreationPasswordTooShort() {
		Response response = createUser("FIWARE EXAMPLE", "example@example.com", "passw1!");
		checkAPIError(response, 400, "password", String.format(MESSAGE_TOO_SHORT, 8), ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationCompanyTooLong() {
		Response response = createUser("FIWARE EXAMPLE", "example@example.com", "password!1", 
				"ABCDEFGHIJKMLNOPQRSTUVWXYZABCDEFGHIJKMLNOPQRSTU");
		checkAPIError(response, 400, "company", String.format(MESSAGE_TOO_LONG, 30), ErrorType.VALIDATION_ERROR);	
	}
	
	@Test
	public void testUserCreationCompanyTooShort() {
		Response response = createUser("FIWARE EXAMPLE", "example@example.com", "password!1", "a");
		checkAPIError(response, 400, "company", String.format(MESSAGE_TOO_SHORT, 3), ErrorType.VALIDATION_ERROR);	
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// UPDATE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private Response updateUser(String authUserName, String authPassword, String userName, 
			String displayName, String email, String newPassword, String company) {
		
		SerializableUser user = new SerializableUser();
		user.setDisplayName(displayName);
		user.setEmail(email);
		user.setPassword(newPassword);
		user.setCompany(company);
		
		String encodedAuthorization = getAuthorization(authUserName, authPassword);
		
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/user/" + userName)
				.request(MediaType.APPLICATION_JSON)
				.header("Authorization", encodedAuthorization)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));
		
		return response;

	}
	
	private Response updateUser(String authUserName, String authPassword, String userName, 
			String displayName, String email, String newPassword) {
		return updateUser(authUserName, authPassword, userName, displayName, email, newPassword, null);
	}
	
	@Test
	public void testUpdateDisplayName() {
	
		String userName = "marketplace";
		String displayName = "Marketplace";
		String newDisplayName = "FIWARE Example";
		String password = "password1!a";
		String email = "example@example.com";
		String company = "UPM";
		
		// Create user
		Response createUserResponse = createUser(displayName, email, password, company);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		// Update user
		Response updateUserResponse = updateUser(userName, password, userName, newDisplayName, 
				email, password, company);
		assertThat(updateUserResponse.getStatus()).isEqualTo(200);
		
		checkUser(userName, newDisplayName, company);
	}
	
	private void testUpdateFieldError(String field, String errorMessage, String newDisplayName, 
			String newEmail, String newPassword, String newCompany) {
		String userName = "marketplace";
		String displayName = "Marketplace";
		String password = "password1!a";
		String email = "example@example.com";
		String company = "UPM";
		
		// Create user
		Response createUserResponse = createUser(displayName, email, password, company);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		// Update user
		Response updateUserResponse = updateUser(userName, password, userName, newDisplayName, 
				newEmail, newPassword, newCompany);
		checkAPIError(updateUserResponse, 400, field, errorMessage, ErrorType.VALIDATION_ERROR);	

		// Check that displayName remains the same
		checkUser(userName, displayName, company);

	}
	
	private void testUpdateDisplayNameError(String newDisplayName, String errorMessage) {
		testUpdateFieldError("displayName", errorMessage, newDisplayName, null, null, null);
	}
	
	@Test
	public void testUpdateDisplayNameTooLong() {
		testUpdateDisplayNameError("ABCDEFGHIJKMLNOPQRSTUVWXYZABCDEFGHIJKMLNOPQRSTUVWXYZ", 
				String.format(MESSAGE_TOO_LONG, 30));
	}
	
	@Test
	public void testUpdateUserDisplayNameTooShort() {
		testUpdateDisplayNameError("a", String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testUpdateUserPassword() {
	
		String userName = "marketplace";
		String displayName = "Marketplace";
		String password = "password1!a";
		String newPassword = "passworda!1";
		String email = "example@example.com";
		
		// Create user
		Response createUserResponse = createUser(displayName, email, password);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		// Update user
		Response updateUserResponse = updateUser(userName, password, userName, displayName, email, newPassword);
		assertThat(updateUserResponse.getStatus()).isEqualTo(200);
		
		// Check that the user cannot be updated with the old password
		Response updateUserResponse1 = updateUser(userName, password, userName, displayName, email, newPassword);
		assertThat(updateUserResponse1.getStatus()).isEqualTo(401);
		
		// Check that the user can be updated with the new password
		Response updateUserResponse2 = updateUser(userName, newPassword, userName, displayName, email, newPassword);
		assertThat(updateUserResponse2.getStatus()).isEqualTo(200);

	}
	
	private void testUpdatePasswordError(String newPassword, String errorMessage) {
		testUpdateFieldError("password", errorMessage, null, null, newPassword, null);
	}
	
	@Test
	public void testUpdatePasswordInvalid() {
		testUpdatePasswordError("password!", MESSAGE_INVALID_PASSWORD);
	}
	
	@Test
	public void testUpdatePasswordTooLong() {
		testUpdatePasswordError("ABCDEFGHIJKMLNOPQRSTUVWXYZABCDEFGHIJKMLNOPQRSTU!1VWXYZ", 
				String.format(MESSAGE_TOO_LONG, 30));
	}
	
	@Test
	public void testUpdatePasswordTooShort() {
		testUpdatePasswordError("a", String.format(MESSAGE_TOO_SHORT, 8));
	}
	
	@Test
	public void testUpdateEmailInvalid() {
		testUpdateFieldError("email", MESSAGE_INVALID_EMAIL, null, "t@", null, null);
	}
	
	@Test
	public void testUpdateEmailAlreadyExist() {
		
		String repeatedEmail = "example@example.com";		
		String userName = "fiware-example";
		String password = "anotherPassword!1";
		
		createUser("Fiware Example", "new_email@example.com", password);
		createUser("Other user name", repeatedEmail, "password!1a");				
		Response updateUserResponse = updateUser(userName, password, userName, null, repeatedEmail, null);
		checkAPIError(updateUserResponse, 400, "email", MESSAGE_EMAIL_ALREADY_REGISTERED, 
				ErrorType.VALIDATION_ERROR);	
	}
	
	private void testUpdateCompanyError(String newCompany, String errorMessage) {
		testUpdateFieldError("company", errorMessage, null, null, null, newCompany);
	}
	
	@Test
	public void testUpdateCompanyTooShort() {
		testUpdateCompanyError("a", String.format(MESSAGE_TOO_SHORT, 3));
	}
	
	@Test
	public void testUpdateCompanyTooLong() {
		testUpdateCompanyError("ABCDEFGHIJKMLNOPQRSTUVWXYZABCDEFGHIJKMLNOPQRSTUVWXYZ", 
				String.format(MESSAGE_TOO_LONG, 30));
	}
	
	@Test
	public void testUpdateUserNonExisting() {
		
		String userName = "marketplace";
		String displayName = "Marketplace";
		String password = "password1!a";
		String email = "example@example.com";
		
		Response createUserResponse = createUser(displayName, email, password);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		String userNameUpdated = userName + "a";
		Response updateUserResponse = updateUser(userName, password, userNameUpdated, displayName, email, password);
		checkAPIError(updateUserResponse, 404, null, String.format(MESSAGE_USER_NOT_FOUND, userNameUpdated), 
				ErrorType.NOT_FOUND);
	}
	
	@Test
	public void testUpdateUserWithAnotherUser() {
		
		String userName1 = "marketplace";
		String displayName1 = "Marketplace";
		String password1 = "password1!a";
		String email1 = "example@example.com";
		
		String userName2 = "marketplacebb";
		String displayName2 = "MarketplaceBB";
		String password2 = "password1!b";
		String email2 = "example2@example.com";
		
		// Create both users
		Response createUser1Response = createUser(displayName1, email1, password1);
		assertThat(createUser1Response.getStatus()).isEqualTo(201);
		Response createUser2Response = createUser(displayName2, email2, password2);
		assertThat(createUser2Response.getStatus()).isEqualTo(201);
		
		// Update user with another user should fail
		Response updateUserResponse = updateUser(userName2, password2, userName1, displayName1, email1, password1);
		checkAPIError(updateUserResponse, 403, null, String.format(MESSAGE_NOT_AUTHORIZED, "update user"), 
				ErrorType.FORBIDDEN);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// DELETE ///////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testDeleteUser() {
		String userName = "fiware-example";
		String displayName = "Fiware Example";
		String password = "password1!";
		
		Response createResponse = createUser(displayName, "example@example.com", password);
		assertThat(createResponse.getStatus()).isEqualTo(201);
		
		// Check the response code
		Response deleteResponse = deleteUser(userName, password, userName);
		assertThat(deleteResponse.getStatus()).isEqualTo(204);
		
		// Check that the user does not exist
		Response getResponse = getUser(userName);
		checkAPIError(getResponse, 404, null, String.format(MESSAGE_USER_NOT_FOUND, userName), ErrorType.NOT_FOUND);
	}
	
	@Test
	public void testDeleteUserNonExisting() {
		String userName = "marketplace";
		String displayName = "Marketplace";
		String password = "password1!a";
		String email = "example@example.com";
		
		Response createUserResponse = createUser(displayName, email, password);
		assertThat(createUserResponse.getStatus()).isEqualTo(201);
		
		String userNameUpdated = userName + "a";
		Response deleteUserResponse = deleteUser(userName, password, userNameUpdated);
		checkAPIError(deleteUserResponse, 404, null, String.format(MESSAGE_USER_NOT_FOUND, userNameUpdated), 
				ErrorType.NOT_FOUND);

	}
	
	@Test
	public void testDeleteUserWithAnotherUser() {
		
		String userName1 = "marketplace";
		String displayName1 = "Marketplace";
		String password1 = "password1!a";
		String email1 = "example@example.com";
		
		String userName2 = "marketplacebb";
		String displayName2 = "MarketplaceBB";
		String password2 = "password1!b";
		String email2 = "example2@example.com";
		
		// Create both users
		Response createUserResponse1 = createUser(displayName1, email1, password1);
		assertThat(createUserResponse1.getStatus()).isEqualTo(201);
		Response createUserResponse2 = createUser(displayName2, email2, password2);
		assertThat(createUserResponse2.getStatus()).isEqualTo(201);
		
		// Delete user with another user should fail
		Response deleteUserResponse = deleteUser(userName2, password2, userName1);
		checkAPIError(deleteUserResponse, 403, null, String.format(MESSAGE_NOT_AUTHORIZED, "delete user"), 
				ErrorType.FORBIDDEN);
		
		// Check that the user already exists
		checkUser(userName1, displayName1, null);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// LIST USERS /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void testListAllUsers() {
		
		// Create some users
		String alphabet = "abcdef";
		String displayNamePattern = "User %c";
		
		for (int i = 0; i < alphabet.length(); i++) {
			createUser(String.format(displayNamePattern, alphabet.charAt(i)), 
					"example" + i + "@example.com", "password!1");
		}
		
		// Get all users
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/user/").request(MediaType.APPLICATION_JSON)
				.get();
		
		// Check the response
		assertThat(response.getStatus()).isEqualTo(200);
		Users users = response.readEntity(Users.class);
		assertThat(users.getUsers().size()).isEqualTo(alphabet.length());
		
		// Users are supposed to be returned in order
		for (int i = 0; i < alphabet.length(); i++) {
			assertThat(users.getUsers().get(i).getDisplayName())
					.isEqualTo(String.format(displayNamePattern, alphabet.charAt(i)));
		}
	}
	
	private void testListSomeUsers(int offset, int max) {
		
		// Create some users
		String alphabet = "abcdefghij";
		String displayNamePattern = "User %c";
		
		for (int i = 0; i < alphabet.length(); i++) {
			createUser(String.format(displayNamePattern, alphabet.charAt(i)), 
					"example" + i + "@example.com", "password!1");
		}
		
		// Get some users
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/user/")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.get();
		
		// Check the response
		int usersCreated = alphabet.length();
		int expectedElements = offset + max > usersCreated ? usersCreated - offset : max;
		assertThat(response.getStatus()).isEqualTo(200);
		Users users = response.readEntity(Users.class);
		assertThat(users.getUsers().size()).isEqualTo(expectedElements);
		
		// Users are supposed to be returned in order
		for (int i = offset; i < offset + expectedElements; i++) {
			assertThat(users.getUsers().get(i - offset).getDisplayName())
					.isEqualTo(String.format(displayNamePattern, alphabet.charAt(i)));
		}
	}
	
	@Test
	public void testListSomeUsersInRange() {
		testListSomeUsers(3, 4);
	}
	
	@Test
	public void testListSomeUsersNotInRange() {
		testListSomeUsers(5, 7);
	}
	
	private void testListUsersInvalidParams(int offset, int max) {
		Client client = ClientBuilder.newClient();
		Response response = client.target(endPoint + "/api/v2/user/")
				.queryParam("offset", offset)
				.queryParam("max", max)
				.request(MediaType.APPLICATION_JSON)
				.get();
		
		checkAPIError(response, 400, null, MESSAGE_INVALID_OFFSET_MAX, ErrorType.BAD_REQUEST);

	}
	
	@Test
	public void testListUsersInvalidOffset() {
		testListUsersInvalidParams(-1, 2);
	}
	
	@Test
	public void testListUsersInvalidMax() {
		testListUsersInvalidParams(1, 0);
	}

	
}
