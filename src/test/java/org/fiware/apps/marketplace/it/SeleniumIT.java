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

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class SeleniumIT extends AbstractIT {

	private WebDriver driver;
	
	private static final String REQUIRED_FIELD = "This field is required.";
	private static final String INVALID_ADDRESS = "This field must be a valid email address.";
	private static final String EMAIL_REGISTERED = "This email is already registered.";
	private static final String INVALID_URL = "This field must be a valid URL.";
	
	private static final String ACCOUNT_UPDATE_FORM = "account_update_form";
	private static final String REGISTRATION_FORM = "registration_form";
	private static final String STORE_FORM = "store_form";
	private static final String DESCRIPTION_CREATION_FORM = "description_create_form";
	private static final String DESCRIPTION_UPDATE_FORM = "description_update_form";

	@Override
	public void specificSetUp() {
		driver = new FirefoxDriver();
		startMockServer();
	}

	@After
	public void quitDriver() {
		driver.quit();
		wireMock.stop();
	}

	// LIST OF SHORTCUTS

	private void fillField(WebElement formElement, String fieldName, String fieldValue) {
		String formName = formElement.getAttribute("name");
		WebDriverWait wait = new WebDriverWait(driver, 5);
		WebElement fieldElement = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.cssSelector("form[name='" + formName + "'] [name='" + fieldName + "']")));

		fieldElement.clear();
		fieldElement.sendKeys(fieldValue);
	}

	private void completeField(WebElement formElement, String fieldName, String fieldValue) {
		String formName = formElement.getAttribute("name");
		WebDriverWait wait = new WebDriverWait(driver, 5);
		WebElement fieldElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
				"form[name='" + formName + "'] [name='" + fieldName + "']")));

		fieldElement.sendKeys(fieldValue);
	}

	private boolean existsElement(By by) {
		return driver.findElements(by).size() != 0;
	}

	private void submitForm(WebElement formElement) {
		formElement.findElement(By.cssSelector("button[type='submit']")).click();
	}

	private WebElement submitFormExpectError(WebElement formElement, String fieldName, String fieldError) {
		String formName = formElement.getAttribute("name");
		WebDriverWait wait = new WebDriverWait(driver, 5);

		// Submit
		submitForm(formElement);

		formElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(formName)));
		verifyFieldError(formElement, fieldName, fieldError);

		return formElement;
	}

	private void verifyAlertContent(String textContent) {
		WebDriverWait wait = new WebDriverWait(driver, 5);
		WebElement alert = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.cssSelector(".alert-dismissible")));
		
		assertThat(alert.getText()).isEqualTo(textContent);	
	}

	private void verifyFieldValue(WebElement formElement, String fieldName, String fieldValue) {
		assertThat(formElement.findElement(By.name(fieldName)).getAttribute("value")).isEqualTo(fieldValue);
	}

	private void verifyFieldError(WebElement formElement, String fieldName, String fieldErrorMessage) {
		String formName = formElement.getAttribute("name");
		WebDriverWait wait = new WebDriverWait(driver, 5);
		WebElement fieldError = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.cssSelector("form[name='" + formName + "'] [name='" + fieldName + "'] + p.field-error")));

		assertThat(fieldError.getText()).isEqualTo(fieldErrorMessage);
	}

	private void clickOnSettingPanelItem(String textContent) {
		WebDriverWait wait = new WebDriverWait(driver, 5);

		driver.findElement(By.id("toggle-right-sidebar")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(textContent))).click();
	}

	private void clickOnOperationPanelItem(String textContent) {
		WebDriverWait wait = new WebDriverWait(driver, 5);

		driver.findElement(By.id("toggle-left-sidebar")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(textContent))).click();
	}


	private boolean isElementPresent(By by) {
		try {
			driver.findElement(by);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	// LIST OF OPERATIONS

	private void loginUser(String username, String password) {
		// Find form
		WebElement formElement = driver.findElement(By.name("login_form"));

		// Fill the form fields
		fillField(formElement, "username", username);
		fillField(formElement, "password", password);

		// Submit
		submitForm(formElement);
	}

	private void loginUserExpectError(String username, String password) {
		String errorMessage = "The username and password do not match.";

		loginUser(username, password);
		assertThat(driver.findElement(By.cssSelector(".alert-danger")).getText()).isEqualTo(errorMessage);
	}

	private void loginDefaultUser() {
		String displayName 	= "Default User";
		String email 		= "defaultuser@domain.org";
		String password 	= "fiware1!";

		createUser(displayName, email, password);

		driver.get(endPoint);
		loginUser(email, password);
	}

	private void registerUser(String displayName, String email, String password) {
		// Find form
		WebElement formElement = driver.findElement(By.name(REGISTRATION_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);
		fillField(formElement, "email", email);
		fillField(formElement, "password", password);
		fillField(formElement, "passwordConfirm", password);

		// Submit
		submitForm(formElement);
	}

	private void updateUser(String email, String company) {
		// Find form
		WebElement formElement = driver.findElement(By.name(ACCOUNT_UPDATE_FORM));

		// Fill the form fields
		fillField(formElement, "email", email);
		fillField(formElement, "company", company);

		// Submit
		submitForm(formElement);

		formElement = driver.findElement(By.name(ACCOUNT_UPDATE_FORM));

		// Verify the field values
		verifyFieldValue(formElement, "email", email);
		verifyFieldValue(formElement, "company", company);
	}

	private void updateUserPassword(String oldPassword, String newPassword) {
		// Find form
		WebElement formElement = driver.findElement(By.name("account_password_update_form"));

		// Fill the form fields
		fillField(formElement, "oldPassword", oldPassword);
		fillField(formElement, "password", newPassword);
		fillField(formElement, "passwordConfirm", newPassword);

		// Submit
		submitForm(formElement);
	}

	private void registerStore(String displayName, String url) {
		// Find form
		WebElement formElement = driver.findElement(By.name(STORE_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);
		fillField(formElement, "url", url);

		// Submit
		submitForm(formElement);
	}

	private void registerStore(String displayName, String url, String imagePath) {
		// Find form
		WebElement formElement = driver.findElement(By.name(STORE_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);
		fillField(formElement, "url", url);
		completeField(formElement, "imageBase64", imagePath);

		// Submit
		submitForm(formElement);
	}

	private void updateStoreDisplayName(String displayName) {
		// Find form
		WebElement formElement = driver.findElement(By.name(STORE_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);

		// Submit
		submitForm(formElement);

		formElement = driver.findElement(By.name(STORE_FORM));

		// Verify the field values
		verifyFieldValue(formElement, "displayName", displayName);
		assertThat(driver.findElement(By.cssSelector(".panel-title.store-displayname"))
				.getText()).isEqualTo(displayName);
	}

	private void registerDescription(String displayName, String url) {
		// Find form
		WebElement formElement = driver.findElement(By.name(DESCRIPTION_CREATION_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);
		fillField(formElement, "url", url);

		// Submit
		submitForm(formElement);
	}

	private void updateDescription(String displayName) {
		// Find form
		WebElement formElement = driver.findElement(By.name(DESCRIPTION_UPDATE_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);

		// Submit
		submitForm(formElement);

		formElement = driver.findElement(By.name(DESCRIPTION_UPDATE_FORM));

		// Verify the field values
		verifyFieldValue(formElement, "displayName", displayName);
	}

	// LIST OF TESTS

	@Test
	public void when_UserIsAnonymous_Expect_RedirectLoginView() {
		driver.get(endPoint);
		assertThat(driver.getTitle()).isEqualTo("Sign In - WMarket");
	}

	@Test
	public void should_RedirectLoginView_And_Can_LoginUser_When_RegistrationFormIsSubmitted() {
		String displayName 	= "New User";
		String email 		= "newuser@domain.org";
		String password 	= "fiware1!";

		driver.get(endPoint + "/register");

		registerUser(displayName, email, password);
		verifyAlertContent("You was registered successfully. You can log in right now.");

		loginUser(email, password);
		assertThat(driver.findElement(By.id("toggle-right-sidebar")).getText()).isEqualTo(displayName);
	}

	@Test
	public void should_DisplayErrorMessage_When_RegistrationFormIsSubmitted_And_DisplayNameIsInvalid() {
		driver.get(endPoint + "/register");
		// Find form
		WebElement formElement = driver.findElement(By.name(REGISTRATION_FORM));

		// Submit
		submitFormExpectError(formElement, "displayName", REQUIRED_FIELD);
	}

	@Test
	public void should_DisplayErrorMessage_When_RegistrationFormIsSubmitted_And_EmailIsInvalid() {
		String displayName 	= "Default User";
		String email 		= "defaultuser@domain.org";
		String password 	= "fiware1!";

		createUser(displayName, email, password);
		driver.get(endPoint + "/register");

		// Find form
		WebElement formElement = driver.findElement(By.name(REGISTRATION_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);
		fillField(formElement, "password", password);
		fillField(formElement, "passwordConfirm", password);

		formElement = submitFormExpectError(formElement, "email", REQUIRED_FIELD);

		fillField(formElement, "email", "invalid_email");
		formElement = submitFormExpectError(formElement, "email", INVALID_ADDRESS);

		fillField(formElement, "email", email);
		formElement = submitFormExpectError(formElement, "email", EMAIL_REGISTERED);
	}

	@Test
	public void should_DisplayErrorMessage_When_RegistrationFormIsSubmitted_And_PasswordIsInvalid() {
		String displayName 	= "New User";
		String email 		= "newuser@domain.org";
		String password 	= "fiware1!";

		driver.get(endPoint + "/register");

		// Find form
		WebElement formElement = driver.findElement(By.name(REGISTRATION_FORM));

		// Fill the form fields
		fillField(formElement, "displayName", displayName);
		fillField(formElement, "email", email);

		formElement = submitFormExpectError(formElement, "password", REQUIRED_FIELD);

		fillField(formElement, "password", password);
		formElement = submitFormExpectError(formElement, "passwordConfirm", REQUIRED_FIELD);

		fillField(formElement, "passwordConfirm", "fiware2!");
		formElement = submitFormExpectError(formElement, "passwordConfirm", "The two passwords do not match.");
	}

	@Test
	public void given_UserIsAuthenticated_When_LogoutIsClicked_Then_RedirectLoginView() {
		loginDefaultUser();

		clickOnSettingPanelItem("Log out");
		verifyAlertContent("You've logged out successfully.");
	}

	@Test
	public void should_DisplayNotificationAlert_When_UserEmailOrUserCompanyAreUpdated() {
		String email 	= "newuser@domain.org";
		String company 	= "FIWARE Lab";

		loginDefaultUser();

		clickOnSettingPanelItem("Settings");
		updateUser(email, company);
		verifyAlertContent("Your profile was updated successfully.");
	}

	@Test
	public void should_DisplayErrorMessage_When_AccountUpdateFormIsSubmitted_And_EmailIsInvalid() {
		String displayName 	= "New User";
		String email 		= "newuser@domain.org";
		String password 	= "fiware1!";

		createUser(displayName, email, password);

		loginDefaultUser();
		driver.get(endPoint + "/account");

		// Find form
		WebElement formElement = driver.findElement(By.name(ACCOUNT_UPDATE_FORM));

		fillField(formElement, "email", "invalid_email");
		formElement = submitFormExpectError(formElement, "email", INVALID_ADDRESS);

		fillField(formElement, "email", email);
		formElement = submitFormExpectError(formElement, "email", EMAIL_REGISTERED);
	}

	@Test
	public void should_RedirectLoginView_And_Can_LoginUser_When_UserPasswordIsUpdated() {
		String displayName 	= "Default User";
		String email 		= "defaultuser@domain.org";
		String oldPassword 	= "fiware1!";
		String newPassword 	= "fiware2!";

		loginDefaultUser();

		driver.get(endPoint + "/account/password");
		updateUserPassword(oldPassword, newPassword);

		assertThat(driver.getTitle()).isEqualTo("Sign In - WMarket");
		verifyAlertContent("Your password was changed. Please sign in again.");

		loginUser(email, newPassword);
		assertThat(driver.findElement(By.id("toggle-right-sidebar")).getText()).isEqualTo(displayName);
	}

	@Test
	public void should_RedirectLoginView_And_CanNot_LoginUser_When_UserIsDeleted() {
		String email 	= "defaultuser@domain.org";
		String password = "fiware1!";

		loginDefaultUser();

		clickOnSettingPanelItem("Settings");

		driver.findElement(By.linkText("Delete account")).click();
		assertThat(driver.getTitle()).isEqualTo("Sign In - WMarket");
		verifyAlertContent("Your account was deleted successfully.");

		loginUserExpectError(email, password);
	}

	@Test
	public void given_UserIsAuthenticated_When_StoreCreateFormIsSubmitted_Then_RedirectStoreView() {
		String displayName 	= "FIWARE Store";
		String url 			= "http://store.fiware.es";

		loginDefaultUser();

		clickOnOperationPanelItem("Register a new store");
		registerStore(displayName, url);
		assertThat(driver.getTitle()).isEqualTo(displayName + " - Offerings - WMarket");
	}

	@Test
	public void should_DisplayErrorMessage_When_StoreCreateFormIsSubmitted_And_DisplayNameIsInvalid() {

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");

		// Find form
		WebElement formElement = driver.findElement(By.name(STORE_FORM));

		formElement = submitFormExpectError(formElement, "displayName", REQUIRED_FIELD);
	}

	@Test
	public void should_DisplayErrorMessage_When_StoreCreateFormIsSubmitted_And_URLIsInvalid() {
		String displayName 	= "FIWARE Store";

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");

		WebElement formElement = driver.findElement(By.name(STORE_FORM));
		fillField(formElement, "displayName", displayName);

		formElement = submitFormExpectError(formElement, "url", REQUIRED_FIELD);

		fillField(formElement, "url", "invalid_url");
		formElement = submitFormExpectError(formElement, "url", INVALID_URL);
	}

	@Test
	public void should_DisplayStoreImage_When_StoreIsCreated_And_ImageIsUploaded() {
		File image = new File("src/test/resources/image.png");

		String displayName	= "FIWARE Store";
		String url			= "http://store.fiware.es";
		String imagePath	= image.getAbsolutePath();

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");

		registerStore(displayName, url, imagePath);
		assertThat(existsElement(By.xpath("//img[contains(@src, '/WMarket/media/store/fiware-store.png')]"))).isTrue();
	}

	@Test
	public void given_UserIsOwner_When_StoreDisplayNameIsUpdated_Then_DisplayNotificationAlert() {
		String displayName 	= "FIWARE Store";
		String url 			= "http://store.fiware.es";

		loginDefaultUser();

		clickOnOperationPanelItem("Register a new store");
		registerStore(displayName, url);

		driver.findElement(By.xpath("//a[contains(@href, '/WMarket/stores/fiware-store/about')]")).click();

		displayName = "FIWARE New Store";
		updateStoreDisplayName(displayName);
		verifyAlertContent("The store '" + displayName + "' was updated successfully.");
	}

	@Test
	public void should_DisplayNotificationAlert_And_RedirectHomeView_When_UserIsOwner_And_StoreIsDeleted() {
		String displayName 	= "FIWARE Store";
		String url 			= "http://store.fiware.es";

		loginDefaultUser();

		clickOnOperationPanelItem("Register a new store");
		registerStore(displayName, url);

		driver.findElement(By.xpath("//a[contains(@href, 'javascript:deleteStore()')]")).click();
		verifyAlertContent("The store '" + displayName + "' was deleted successfully.");
	}

	@Test
	public void should_DisplayStoreCreateLink_When_NoStoreAvailable() {
		loginDefaultUser();

		clickOnOperationPanelItem("Upload a new description");
		driver.findElement(By.linkText("register a store")).click();
		assertThat(driver.getTitle()).isEqualTo("New Store - WMarket");
	}

	@Test
	public void should_RedirectStoreView_When_DescriptionCreateFormIsSubmitted() {
		String displayName	= "FIWARE Store";
		String url			= "http://store.fiware.es";

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");
		registerStore(displayName, url);

		driver.get(endPoint + "/descriptions/register");

		registerDescription("New description", defaultUSDLPath);
		assertThat(driver.getTitle()).isEqualTo(displayName + " - Offerings - WMarket");
		verifyAlertContent("The description 'New description' was uploaded successfully.");
	}

	@Test
	public void should_DisplayNotificationAlert_When_DescriptionIsUpdated() {
		String displayName	= "FIWARE Store";
		String url			= "http://store.fiware.es";

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");
		registerStore(displayName, url);

		driver.get(endPoint + "/descriptions/register");

		registerDescription("Test description", defaultUSDLPath);
		clickOnOperationPanelItem("My descriptions");
		driver.findElement(
				By.xpath("//a[contains(@href, '/WMarket/stores/fiware-store/descriptions/test-description')]")).click();

		String newDipslayName = "New description";
		updateDescription(newDipslayName);
		verifyAlertContent("The description '" + newDipslayName + "' was updated successfully.");
	}

	@Test
	public void should_DisplayNotificationAlert_When_DescriptionIsDeleted() {
		String displayName	= "FIWARE Store";
		String url			= "http://store.fiware.es";

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");
		registerStore(displayName, url);

		driver.get(endPoint + "/descriptions/register");

		String descriptionName = "Test description";
		registerDescription(descriptionName, defaultUSDLPath);
		clickOnOperationPanelItem("My descriptions");
		driver.findElement(
				By.xpath("//a[contains(@href, '/WMarket/stores/fiware-store/descriptions/test-description')]")).click();

		driver.findElement(By.xpath("//a[contains(@href, 'javascript:deleteDescription()')]")).click();
		verifyAlertContent("The description '" + descriptionName + "' was deleted successfully.");
	}

	@Test
	public void when_DescriptionIsUploaded_Expect_OfferingHasDetailView() {
		String displayName	= "FIWARE Store";
		String url			= "http://store.fiware.es";

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");
		registerStore(displayName, url);

		driver.get(endPoint + "/descriptions/register");

		registerDescription("New description", defaultUSDLPath);
		driver.findElement(By.cssSelector(".offering-item .panel-title")).click();
		assertThat(driver.getTitle()).isEqualTo("OrionStarterKit" + " - WMarket");
	}

	@Test
	public void given_OfferingIsAvailable_When_BookmarkAdded_Expect_ShownInBookmarkListView() {
		String displayName	= "FIWARE Store";
		String url			= "http://store.fiware.es";

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");
		registerStore(displayName, url);

		driver.get(endPoint + "/descriptions/register");

		registerDescription("New description", defaultUSDLPath);

		driver.findElement(By.cssSelector(".offering-item .panel-title")).click();
		driver.findElement(By.linkText("Add bookmark")).click();
		clickOnOperationPanelItem("My bookmarks");

		assertThat(isElementPresent(By.linkText("OrionStarterKit"))).isTrue();
	}
	
	@Test
	public void given_OfferingIsAvailable_When_BookmarkRemoved_Expect_FadedInBookmarkListView() {
		String displayName = "FIWARE Store";
		String url = "http://store.fiware.es";

		loginDefaultUser();
		driver.get(endPoint + "/stores/register");
		registerStore(displayName, url);

		driver.get(endPoint + "/descriptions/register");

		registerDescription("New description", defaultUSDLPath);

		driver.findElement(By.cssSelector(".offering-item .panel-title")).click();
		driver.findElement(By.linkText("Add bookmark")).click();
		clickOnOperationPanelItem("My bookmarks");

		driver.findElement(By.linkText("OrionStarterKit")).click();
		driver.findElement(By.linkText("Remove bookmark")).click();

		clickOnOperationPanelItem("My bookmarks");
		assertThat(!isElementPresent(By.linkText("OrionStarterKit"))).isTrue();
	}

}
