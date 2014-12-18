package org.fiware.apps.marketplace.model.validators;

import static org.assertj.core.api.Assertions.*;

import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.User;
import org.junit.Test;

public class UserValidatorTest {
	
	private UserValidator userValidator = new UserValidator();
	
	private static final String MISSING_FILEDS_MSG = "name, email and/or password cannot be null";
	private static final String INVALID_LENGTH_PATTERN = "%s is not valid. (min length: %d, max length: %d)";
	private static final String INVALID_EMAIL = "email is not valid";
	
	private static User generateValidUser() {
		User user = new User();
		user.setUserName("userName");
		user.setPassword("12345678");
		user.setEmail("example@example.com");
		user.setCompany("EXAMPLE");
		user.setDisplayName("Example Name");
		
		return user;
	}
	
	private void assertInvalidUser(User user, String expectedMsg, boolean creating) {
		try {
			userValidator.validateUser(user, creating);
			failBecauseExceptionWasNotThrown(ValidationException.class);
		} catch (ValidationException ex) {
			assertThat(ex).hasMessage(expectedMsg);
		}
	}
	
	@Test
	public void testValidBasicUser() throws ValidationException {
		User user = new User();
		user.setUserName("userName");
		user.setPassword("12345678");
		user.setEmail("example@example.com");
		
		assertThat(userValidator.validateUser(user, true)).isTrue();
	}
	
	@Test
	public void testValidComplexUser() throws ValidationException {
		User user = generateValidUser();
		assertThat(userValidator.validateUser(user, true)).isTrue();
	}
	
	@Test
	public void testMissingUserNameOnCreation() {
		// Generate a user without username
		User user = generateValidUser();
		user.setUserName(null);
		
		assertInvalidUser(user, MISSING_FILEDS_MSG, true);
	}
	
	@Test
	public void testMissingUserNameOnUpdate() throws ValidationException {
		// Generate a user without username
		User user = generateValidUser();
		user.setUserName(null);
		
		assertThat(userValidator.validateUser(user, false)).isTrue();
	}
	
	@Test
	public void testMissingPasswordOnCreation() {
		// Generate a user without password
		User user = generateValidUser();
		user.setPassword(null);
		
		assertInvalidUser(user, MISSING_FILEDS_MSG, true);
	}
	
	@Test
	public void testMissingPasswordOnUpdate() throws ValidationException {
		// Generate a user without password
		User user = generateValidUser();
		user.setPassword(null);
		
		assertThat(userValidator.validateUser(user, false)).isTrue();
	}
	
	@Test
	public void testMissingMailOnCreation() {
		// Generate a user without mail
		User user = generateValidUser();
		user.setEmail(null);
		
		assertInvalidUser(user, MISSING_FILEDS_MSG, true);
	}
	
	@Test
	public void testMissingMailOnUpdate() throws ValidationException {
		// Generate a user without mail
		User user = generateValidUser();
		user.setEmail(null);
		
		assertThat(userValidator.validateUser(user, false)).isTrue();
	}
	
	@Test
	public void testMissingDisplayName() throws ValidationException {
		User user = generateValidUser();
		user.setDisplayName(null);
		
		// Display name can be set to null
		assertThat(userValidator.validateUser(user, false)).isTrue();
	}
	
	@Test
	public void testMissingCompany() throws ValidationException {
		User user = generateValidUser();
		user.setCompany(null);
		
		// Company name can be set to null
		assertThat(userValidator.validateUser(user, false)).isTrue();
	}
	
	@Test
	public void testUserNameTooShort() {
		User user = generateValidUser();
		user.setUserName("a");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "userName", 5, 15), false);
	}
	
	@Test
	public void testUserNameTooLong() {
		User user = generateValidUser();
		user.setUserName("1234567890123456");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "userName", 5, 15), false);
	}
	
	@Test
	public void testPasswordTooShort() {
		User user = generateValidUser();
		user.setPassword("a");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "password", 8, 30), false);
	}
	
	@Test
	public void testPasswordTooLong() {
		User user = generateValidUser();
		user.setPassword("1234567890123456789012345678901");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "password", 8, 30), false);
	}
	
	@Test
	public void testDisplayNameTooShort() {
		User user = generateValidUser();
		user.setDisplayName("a");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "displayName", 5, 30), false);
	}
	
	@Test
	public void testDisplayNameTooLong() {
		User user = generateValidUser();
		user.setDisplayName("1234567890123456789012345678901");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "displayName", 5, 30), false);
	}
	
	@Test
	public void testInvalidMail1() {
		User user = generateValidUser();
		user.setEmail("test");
		
		assertInvalidUser(user, INVALID_EMAIL, false);
	}
	
	@Test
	public void testInvalidMail2() {
		User user = generateValidUser();
		user.setEmail("test@test");
		
		assertInvalidUser(user, INVALID_EMAIL, false);
	}
	
	@Test
	public void testInvalidMail3() {
		User user = generateValidUser();
		user.setEmail("@test.com");
		
		assertInvalidUser(user, INVALID_EMAIL, false);
	}
	
	@Test
	public void testCompanyTooShort() {
		User user = generateValidUser();
		user.setCompany("a");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "company", 3, 30), false);
	}
	
	@Test
	public void testCompanyTooLong() {
		User user = generateValidUser();
		user.setCompany("1234567890123456789012345678901");
		
		assertInvalidUser(user, String.format(INVALID_LENGTH_PATTERN, "company", 3, 30), false);
	}
}
