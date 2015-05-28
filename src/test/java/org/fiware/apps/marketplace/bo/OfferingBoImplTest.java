package org.fiware.apps.marketplace.bo;

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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.fiware.apps.marketplace.bo.impl.OfferingBoImpl;
import org.fiware.apps.marketplace.dao.OfferingDao;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.OfferingNotFoundException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.User;
import org.fiware.apps.marketplace.security.auth.OfferingAuth;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OfferingBoImplTest {

	// TODO: Add tests for other methods

	@Mock private OfferingDao offeringDaoMock;
	@Mock private OfferingAuth offeringAuthMock;
	@Mock private UserBo userBoMock;
	@InjectMocks private OfferingBoImpl offeringBo;

	@Before 
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.offeringBo = spy(this.offeringBo);
	}

	@Test(expected=NotAuthorizedException.class)
	public void testBookmarkNotAuthorized() throws Exception {

		String storeName = "store";
		String descriptionName = "description";
		String offeringName = "offering";

		Offering offering = mock(Offering.class);

		// Configure mocks
		when(offeringDaoMock.findDescriptionByNameStoreAndDescription(storeName, descriptionName, 
				offeringName)).thenReturn(offering);
		when(offeringAuthMock.canBookmark(any(Offering.class))).thenReturn(false);

		// Call the function
		offeringBo.bookmark(storeName, descriptionName, offeringName);
	}

	private void testNotFound(Exception e) throws Exception {

		String storeName = "store";
		String descriptionName = "description";
		String offeringName = "offering";

		// Configure mock
		doThrow(e).when(offeringDaoMock).findDescriptionByNameStoreAndDescription(storeName, descriptionName, 
				offeringName);

		// Call the function
		offeringBo.bookmark(storeName, descriptionName, offeringName);
	}

	@Test(expected=StoreNotFoundException.class)
	public void testStoreNotFoundException() throws Exception {
		testNotFound(new StoreNotFoundException(""));
	}

	@Test(expected=DescriptionNotFoundException.class)
	public void testDescriptionNotFoundException() throws Exception {
		testNotFound(new DescriptionNotFoundException(""));
	}

	@Test(expected=OfferingNotFoundException.class)
	public void testOfferingNotFoundException() throws Exception {
		testNotFound(new OfferingNotFoundException(""));
	}

	private void testChangeBookmarkState(boolean offeringBookmarked) throws Exception {

		String storeName = "store";
		String descriptionName = "description";
		String offeringName = "offering";

		Offering offering = mock(Offering.class);
		User user = mock(User.class);
		@SuppressWarnings("unchecked")
		List<Offering> bookmarked = mock(List.class);

		// Configure mocks
		when(offeringDaoMock.findDescriptionByNameStoreAndDescription(storeName, descriptionName, 
				offeringName)).thenReturn(offering);
		when(offeringAuthMock.canBookmark(any(Offering.class))).thenReturn(true);
		when(userBoMock.getCurrentUser()).thenReturn(user);
		when(user.getBookmarks()).thenReturn(bookmarked);
		when(bookmarked.contains(offering)).thenReturn(offeringBookmarked);

		// Call the function
		offeringBo.bookmark(storeName, descriptionName, offeringName);

		if (!offeringBookmarked) {
			// Check that the offering has been included in the list of bookmarked offerings
			verify(bookmarked).add(offering);
		} else {
			// Check that the offering has been removed from the list of bookmarked offerings
			verify(bookmarked).remove(offering);
		}
	}
	
	@Test
	public void testBookmark() throws Exception {
		testChangeBookmarkState(false);
	}
	
	@Test
	public void testUnbookmark() throws Exception {
		testChangeBookmarkState(true);
	}
}
