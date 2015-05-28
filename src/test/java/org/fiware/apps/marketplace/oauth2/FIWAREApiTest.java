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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.OAuthEncoder;

public class FIWAREApiTest {
	
	private static final String SERVER_URL = "https://account.lab.fiware.org";
	private FIWAREApi api = new FIWAREApi(SERVER_URL);
	
	@Test
	public void testGetAuthorizationUrl() {
		String clientId = "1";
		String redirectURI = "http://fi-ware.org";
		String scope = "profile,store";
		OAuthConfig config = new OAuthConfig(clientId, null, redirectURI, null, scope, null);		
		String expectedURL = SERVER_URL + "/oauth2/authorize?client_id=" + clientId + 
				"&redirect_uri=" + OAuthEncoder.encode(redirectURI) + "&scope=" + 
				OAuthEncoder.encode(scope) + "&response_type=code";
		
		assertThat(api.getAuthorizationUrl(config)).isEqualTo(expectedURL);
	}
	
	@Test
	public void testGetAccessTokenEndpoint() {
		assertThat(api.getAccessTokenEndpoint()).isEqualTo(SERVER_URL + "/oauth2/token");
	}
	
	@Test
	public void testGetAccessTokenVerb() {
		assertThat(api.getAccessTokenVerb()).isEqualTo(Verb.POST);
	}
	
	@Test
	public void testGetAccessTokenExtractor() {
		assertThat(api.getAccessTokenExtractor()).isInstanceOf(JsonTokenExtractor.class);
	}

}
