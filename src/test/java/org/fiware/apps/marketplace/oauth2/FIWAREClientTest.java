package org.fiware.apps.marketplace.oauth2;

/*
 * #%L
 * FiwareMarketplace
 * %%
 * Copyright (C) 2014 CoNWeT Lab, Universidad Politécnica de Madrid
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.fiware.apps.marketplace.dao.UserDao;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.model.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pac4j.core.context.WebContext;

public class FIWAREClientTest {

	private final static String SERVER_URL = "https://account.lab.fiware.org";
	@Mock private UserDao userDaoMock;
	@InjectMocks private FIWAREClient client = new FIWAREClient();


	@Before 
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		this.client.setServerURL(SERVER_URL);
	}

	@Test
	public void testRequiresStateParameter() {
		assertThat(client.requiresStateParameter()).isFalse();
	}

	private void testExtractUserProfile(boolean userExist) {
		try {
			// This JSON simulates a response from the IdM
			String userName = "user";
			String displayName = "User Name";
			String email = "user@fiware.org";
			String json = "{\"id\":1,\"actorId\":2487,\"id\":\"" + userName + "\","
					+ "\"displayName\":\"" + displayName + "\",\"email\":\"" + email + "\"}";

			// Mock
			if (userExist) {
				User user = new User();
				user.setId(1);
				user.setUserName(userName + "_old");
				user.setDisplayName(displayName + "_old");
				user.setEmail(email + "_old");
				when(userDaoMock.findByName(userName)).thenReturn(user);
			} else {
				doThrow(new UserNotFoundException("user not found")).when(userDaoMock).findByName(userName);
			}

			// Call the function
			FIWAREProfile profile = client.extractUserProfile(json);

			// Check the profile
			assertThat(profile.getId()).isEqualTo(userName);
			assertThat(profile.getDisplayName()).isEqualTo(displayName);
			assertThat(profile.getEmail()).isEqualTo(email);

			// Capture the user saved in the database
			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			verify(userDaoMock).save(captor.capture());

			User storedUser = captor.getValue();
			assertThat(storedUser.getUserName()).isEqualTo(userName);
			assertThat(storedUser.getEmail()).isEqualTo(email);
			assertThat(storedUser.getPassword()).isEqualTo("");
			assertThat(storedUser.getEmail()).isEqualTo(email);
		} catch (Exception ex) {
			fail("Exception " + ex + " not expected");
		}
	}

	@Test
	public void testExtractUserProfileUserExists() {
		testExtractUserProfile(true);
	}
	
	@Test
	public void testExtractUserProfileUserDoesNotExist() {
		testExtractUserProfile(false);
	}
	
	@Test
	public void testExtractUserProfileNull() {
		// Call the function
		FIWAREProfile profile = client.extractUserProfile(null);
		
		// Assertions
		assertThat(profile).isNull();
		verify(userDaoMock, never()).save(isA(User.class));
	}

	@Test
	public void testGetProfileUrl() {
		assertThat(client.getProfileUrl(null)).isEqualTo(SERVER_URL + "/user");
	}

	private void testHasBeenCancelled(String error, boolean cancelled) {
		WebContext context = mock(WebContext.class);
		when(context.getRequestParameter(anyString())).thenReturn(error);
		assertThat(client.hasBeenCancelled(context)).isEqualTo(cancelled);
	}

	@Test
	public void testCancelled() {
		testHasBeenCancelled("access_denied", true);
	}

	@Test
	public void testNotCancelled() {
		testHasBeenCancelled("internal_error", false);
	}

	@Test
	public void testNewClient() {
		assertThat(client.newClient()).isInstanceOf(FIWAREClient.class);
	}

}
