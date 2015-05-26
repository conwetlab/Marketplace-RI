package org.fiware.apps.marketplace.oauth2;

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

import java.util.Date;

import org.fiware.apps.marketplace.dao.UserDao;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.model.User;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.oauth.client.BaseOAuth20Client;
import org.pac4j.oauth.client.exception.OAuthCredentialsException;
import org.pac4j.oauth.credentials.OAuthCredentials;
import org.pac4j.oauth.profile.JsonHelper;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Token;
import org.scribe.model.SignatureType;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;

public class FIWAREClient extends BaseOAuth20Client<FIWAREProfile>{

	// To store users information
	@Autowired private UserDao userDao;

	private String scopeValue = "";
	private String serverURL;

	/**
	 * Method to get the FIWARE IdM that is being in used
	 * @return The FIWARE IdM that is being used to authenticate the users
	 */
	public String getServerURL() {
		return this.serverURL;
	}

	/**
	 * Method to set the FIWARE IdM that will be use to authenticate the users
	 * @param serverURL The FIWARE IdM that will be use to authenticate the users
	 */
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	@Override
	protected void internalInit() {
		super.internalInit();

		this.scopeValue = "";
		this.service = new ProxyOAuthFIWARE(new FIWAREApi(this.serverURL), 
				new OAuthConfig(this.key, this.secret,
						this.callbackUrl,
						SignatureType.Header,
						this.scopeValue, null),
						this.connectTimeout, this.readTimeout, this.proxyHost,
						this.proxyPort, false, true);
	}

	@Override
	protected boolean requiresStateParameter() {
		return false;
	}

	@Override
	protected FIWAREProfile extractUserProfile(String body) {

		FIWAREProfile profile = new FIWAREProfile();

		if (body != null) {
			
			final JsonNode json = JsonHelper.getFirstNode(body);
			profile.setId(JsonHelper.get(json, "id"));
			for (final String attribute : new FIWAREAttributesDefinition().getPrincipalAttributes()) {
				profile.addAttribute(attribute, JsonHelper.get(json, attribute));
			}

			// FIXME: By default, we are adding the default Role...
			profile.addRole("ROLE_USER");

			// User information should be stored in the local users table
			User user;
			String username = (String) profile.getUsername();
			String email = (String) profile.getEmail();
			String displayName = (String) profile.getDisplayName();

			try {
				// Modify the existing user
				user = userDao.findByName(username);
			} catch (UserNotFoundException e) {
				// Create a new user
				user = new User();
				user.setRegistrationDate(new Date());
			}

			// Set field values
			user.setUserName(username);
			user.setEmail(email);
			user.setPassword("");	// Password cannot be NULL
			user.setDisplayName(displayName);
			user.setOauth2(true);

			// Save the new user
			userDao.save(user);

			return profile;
		} else {
			return null;
		}
	}

	@Override
	protected String getProfileUrl(Token arg0) {
		return String.format("%s/user", this.serverURL);
	}

	@Override
	protected boolean hasBeenCancelled(WebContext context) {
		final String error = context.getRequestParameter(OAuthCredentialsException.ERROR);

		// user has denied permissions
		if ("access_denied".equals(error)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected BaseClient<OAuthCredentials, FIWAREProfile> newClient() {
		FIWAREClient newClient = new FIWAREClient();
		return newClient;
	}
}
