package org.fiware.apps.marketplace.controllers.rest.v2;

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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.fiware.apps.marketplace.bo.OfferingBo;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.OfferingNotFoundException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.model.ErrorType;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.Offerings;
import org.fiware.apps.marketplace.model.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OfferingServiceTest {
	
	@Mock private OfferingBo offeringBoMock;
	@InjectMocks private OfferingService offeringService;
	
	private static final String OFFSET_MAX_INVALID = "offset (%d) and/or max (%d) are not valid";
	
	@Before 
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// GET OFFERING ////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	
	private void testGetOfferingExceptcion(Exception e, int httpCode, 
			ErrorType errorType, String message) {
		
		try {
			doThrow(e).when(offeringBoMock).findOfferingByNameStoreAndDescription(anyString(), 
					anyString(), anyString());
			
			// Call the method
			String offeringName = "offering";
			String descriptionName = "description";
			String storeName = "store";
			
			// Call the method
			Response res = offeringService.getOffering(storeName, descriptionName, 
					offeringName);
			
			// Verify that the BO has been called with the correct arguments
			verify(offeringBoMock).findOfferingByNameStoreAndDescription(storeName, 
					descriptionName, offeringName);
			
			// Check the response
			GenericRestTestUtils.checkAPIError(res, httpCode, errorType, 
					message);
		
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}
	}
	
	private void testGetOffering404(Exception e) {
		testGetOfferingExceptcion(e, 404, ErrorType.NOT_FOUND, e.getMessage());
	}
	
	@Test
	public void testGetOfferingNotAuthorized() {
		User user = mock(User.class);
		when(user.getUserName()).thenReturn("userName");
		Exception e = new NotAuthorizedException("list offerings");
		testGetOfferingExceptcion(e, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	@Test
	public void testGetOfferingNotFound() {
		Exception e = new OfferingNotFoundException("offering not found");
		testGetOffering404(e);
	}
	
	@Test
	public void testGetOfferingDescriptionNotFound() {
		Exception e = new DescriptionNotFoundException("description not found");
		testGetOffering404(e);
	}
	
	@Test
	public void testGetOfferingStoreNotFound() {
		Exception e = new StoreNotFoundException("store not found");
		testGetOffering404(e);
	}
	
	@Test
	public void testGetOfferingUnexpectedException() {
		Exception e = new RuntimeException("Unexpected expection");
		testGetOfferingExceptcion(e, 500, ErrorType.INTERNAL_SERVER_ERROR, e.getMessage());
	}
	
	@Test
	public void testGetOffering() throws Exception {
		String storeName = "store";
		String descriptionName = "description";
		String offeringName = "offering";
		
		Offering offering = new Offering();
		offering.setId(1);
		
		when(offeringBoMock.findOfferingByNameStoreAndDescription(storeName, descriptionName, offeringName))
				.thenReturn(offering);
		
		// Call the function
		Response res = offeringService.getOffering(storeName, descriptionName, offeringName);
		
		// Verify that the BO has been called properly
		verify(offeringBoMock).findOfferingByNameStoreAndDescription(storeName, descriptionName, offeringName);
		
		// Check the response
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(res.getEntity()).isEqualTo(offering);
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// LIST OFFERINGS IN DESCRIPTION ///////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	private void testListAllOfferingsInDescriptionInvalidParams(int offset, int max) {
		try {
			// Call the method
			Response res = offeringService.listOfferingsInDescription("store", "description", offset, max);
	
			// Verify the 
			verify(offeringBoMock, never()).getDescriptionOfferingsPage(anyString(), anyString(), anyInt(), anyInt());
			
			// Assertions
			GenericRestTestUtils.checkAPIError(res, 400, ErrorType.BAD_REQUEST, 
					String.format(OFFSET_MAX_INVALID, offset, max));
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}
	}
	
	@Test
	public void testListAllOfferingsInDescriptionInvalidOffset() {
		testListAllOfferingsInDescriptionInvalidParams(-1, 100);
	}
	
	@Test
	public void testListAllOfferingsInDescriptionInvalidMax() {
		testListAllOfferingsInDescriptionInvalidParams(0, -1);
	}
	
	@Test
	public void testListAllOfferingsInDescriptionInvalidOffsetMax() {
		testListAllOfferingsInDescriptionInvalidParams(-1, -1);
	}
	
	private void testListAllOfferingsInDescriptionException(Exception e, int httpCode, 
			ErrorType errorType, String message) {
		
		try {
			doThrow(e).when(offeringBoMock).getDescriptionOfferingsPage(anyString(), 
					anyString(), anyInt(), anyInt());
			
			// Call the method
			String descriptionName = "description";
			String storeName = "store";
			int offset = 0;
			int max = 100;
			
			// Call the method
			Response res = offeringService.listOfferingsInDescription(storeName, descriptionName, offset, max);
			
			// Verify that the BO has been called with the correct arguments
			verify(offeringBoMock).getDescriptionOfferingsPage(storeName, 
					descriptionName, offset, max);
			
			// Check the response
			GenericRestTestUtils.checkAPIError(res, httpCode, errorType, 
					message);
		
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}
	}
	
	private void testListAllOfferingsInDescription404(Exception e) {
		testListAllOfferingsInDescriptionException(e, 404, ErrorType.NOT_FOUND, e.getMessage());
	}
	
	@Test
	public void testListAllOfferingsInDescriptionNotAuthorized() {
		User user = mock(User.class);
		when(user.getUserName()).thenReturn("userName");
		Exception e = new NotAuthorizedException("list offerings");
		testListAllOfferingsInDescriptionException(e, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	@Test
	public void testListAllOfferingsInDescriptionStoreNotFound() {
		Exception e = new StoreNotFoundException("store not found");
		testListAllOfferingsInDescription404(e);
	}
	
	@Test
	public void testListAllOfferingsInDescriptionDescriptionNotFound() {
		Exception e = new DescriptionNotFoundException("description not found");
		testListAllOfferingsInDescription404(e);
	}
	
	@Test
	public void testListAllOfferingsInDescription() throws Exception {
		
		String storeName = "store";
		String descriptionName = "description";
		int offset = 0;
		int max = 100;
		
		List<Offering> oferrings = new ArrayList<Offering>();
		for (int i = 0; i < 3; i++) {
			Offering offering = new Offering();
			offering.setId(i);
			oferrings.add(offering);
		}
		
		// Mocks
		when(offeringBoMock.getDescriptionOfferingsPage(eq(storeName), eq(descriptionName), anyInt(), anyInt())).
				thenReturn(oferrings);
		
		// Call the method
		Response res = offeringService.listOfferingsInDescription(storeName, descriptionName, offset, max);
		
		// Verify
		verify(offeringBoMock).getDescriptionOfferingsPage(storeName, descriptionName, offset, max);
		
		// Assertions
		assertThat(res.getStatus()).isEqualTo(200);
		assertThat(((Offerings) res.getEntity()).
				getOfferings()).isEqualTo(oferrings);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// BOOKMARK //////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	
	private void testBookmarkOfferingExceptcion(Exception e, int httpCode, 
			ErrorType errorType, String message) {
		
		try {
			doThrow(e).when(offeringBoMock).bookmark(anyString(), anyString(), anyString());
			
			// Call the method
			String offeringName = "offering";
			String descriptionName = "description";
			String storeName = "store";
			
			// Call the method
			Response res = offeringService.bookmark(storeName, descriptionName, offeringName);
			
			// Verify that the BO has been called with the correct arguments
			verify(offeringBoMock).bookmark(storeName, descriptionName, offeringName);
			
			// Check the response
			GenericRestTestUtils.checkAPIError(res, httpCode, errorType, 
					message);
		
		} catch (Exception ex) {
			fail("Exception not expected", ex);
		}
	}
	
	private void testBookmarkOffering404(Exception e) {
		testBookmarkOfferingExceptcion(e, 404, ErrorType.NOT_FOUND, e.getMessage());
	}
	
	@Test
	public void testBookmarkOfferingNotAuthorized() {
		User user = mock(User.class);
		when(user.getUserName()).thenReturn("userName");
		Exception e = new NotAuthorizedException("list offerings");
		testBookmarkOfferingExceptcion(e, 403, ErrorType.FORBIDDEN, e.getMessage());
	}
	
	@Test
	public void testBookmarkOfferingNotFound() {
		Exception e = new OfferingNotFoundException("offering not found");
		testBookmarkOffering404(e);
	}
	
	@Test
	public void testBookmarkOfferingDescriptionNotFound() {
		Exception e = new DescriptionNotFoundException("description not found");
		testBookmarkOffering404(e);
	}
	
	@Test
	public void testBookmarkOfferingStoreNotFound() {
		Exception e = new StoreNotFoundException("store not found");
		testBookmarkOffering404(e);
	}
	
	@Test
	public void testBookmarkOfferingUnexpectedException() {
		Exception e = new RuntimeException("Unexpected expection");
		testGetOfferingExceptcion(e, 500, ErrorType.INTERNAL_SERVER_ERROR, e.getMessage());
	}
	
	@Test
	public void testBookmarkOffering() throws Exception {
		String storeName = "store";
		String descriptionName = "description";
		String offeringName = "offering";
		
		// Call the function
		Response res = offeringService.bookmark(storeName, descriptionName, offeringName);
		
		// Verify that the BO has been called properly
		verify(offeringBoMock).bookmark(storeName, descriptionName, offeringName);
		
		// Check the response
		assertThat(res.getStatus()).isEqualTo(204);
		
	}

	
}
