package org.fiware.apps.marketplace.controllers.web;

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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.Description;
import org.fiware.apps.marketplace.model.Store;
import org.fiware.apps.marketplace.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


@Component
@Path("/stores/{storeName}/descriptions/{descriptionName}")
public class DescriptionController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(DescriptionController.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response detailView(
            @Context HttpServletRequest request,
            @PathParam("storeName") String storeName,
            @PathParam("descriptionName") String descriptionName) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;

        try {
            User user = getCurrentUser();
            Store store = getStoreBo().findByName(storeName);
            Description description = getDescriptionBo().findByNameAndStore(storeName, descriptionName);

            model.addAttribute("user", user);
            model.addAttribute("title", description.getDisplayName() + " - " + getContextName());
            model.addAttribute("store", store);
            model.addAttribute("description", description);
            model.addAttribute("currentDescriptionView", "detail");

            addFlashMessage(request, model);

            view = new ModelAndView("description.detail", model);
            builder = Response.ok();
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError();
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED);
        } catch (StoreNotFoundException e) {
            logger.info("Store not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND);
        } catch (DescriptionNotFoundException e) {
            logger.info("Description not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND);
        }

        return builder.entity(view).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response updateView(
            @Context UriInfo uri,
            @Context HttpServletRequest request,
            @PathParam("storeName") String storeName,
            @PathParam("descriptionName") String descriptionName,
            @FormParam("displayName") String displayName,
            @FormParam("url") String url,
            @FormParam("comment") String comment) {

        Description newDescription = new Description();
        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;

        try {
            User user = getCurrentUser();
            Store store = getStoreBo().findByName(storeName);
            Description oldDescription = getDescriptionBo().findByNameAndStore(storeName, descriptionName);

            model.addAttribute("user", user);
            model.addAttribute("title", oldDescription.getDisplayName() + " - " + getContextName());
            model.addAttribute("store", store);
            model.addAttribute("description", oldDescription);

            newDescription.setDisplayName(displayName);
            newDescription.setUrl(url);
            newDescription.setComment(comment);

            getDescriptionBo().update(store.getName(), oldDescription.getName(), newDescription);

            setFlashMessage(request, "The description '" + displayName + "' was updated successfully.");

            URI redirectURI = UriBuilder.fromUri(uri.getBaseUri())
                    .path("stores").path(storeName)
                    .path("descriptions").path(descriptionName)
                    .build();
            builder = Response.seeOther(redirectURI);
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError().entity(view);
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED).entity(view);
        } catch (StoreNotFoundException e) {
            logger.info("Store not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND).entity(view);
        } catch (DescriptionNotFoundException e) {
            logger.info("Description not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND).entity(view);
        } catch (ValidationException e) {
            logger.info("A form field is not valid", e);

			Map<String, String> formInfo = new HashMap<String, String>();

			formInfo.put("storeName", storeName);
			formInfo.put("displayName", displayName);
			formInfo.put("url", url);
			formInfo.put("comment", comment);

			model.addAttribute("form_data", formInfo);
			model.addAttribute("form_error", e);

            view = new ModelAndView("description.detail", model);
            builder = Response.status(Status.BAD_REQUEST).entity(view);
        }

        return builder.build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("offerings")
    public Response offeringListView(
            @Context HttpServletRequest request,
            @PathParam("storeName") String storeName,
            @PathParam("descriptionName") String descriptionName) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;

        try {
            User user = getCurrentUser();
            Store store = getStoreBo().findByName(storeName);
            Description description = getDescriptionBo().findByNameAndStore(storeName, descriptionName);

            model.addAttribute("user", user);
            model.addAttribute("title", description.getDisplayName() + " - " + getContextName());
            model.addAttribute("store", store);
            model.addAttribute("description", description);
            model.addAttribute("currentDescriptionView", "offeringList");

            addFlashMessage(request, model);

            view = new ModelAndView("description.offering.list", model);
            builder = Response.ok();
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError();
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED);
        } catch (StoreNotFoundException e) {
            logger.info("Store not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND);
        } catch (DescriptionNotFoundException e) {
            logger.info("Description not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND);
        }

        return builder.entity(view).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("delete")
    public Response deleteView(
            @Context UriInfo uri,
            @Context HttpServletRequest request,
            @PathParam("storeName") String storeName,
            @PathParam("descriptionName") String descriptionName) {

        ModelAndView view;
        ResponseBuilder builder;

        try {
            Store store = getStoreBo().findByName(storeName);
            Description description = getDescriptionBo().findByNameAndStore(storeName, descriptionName);

            getDescriptionBo().delete(store.getName(), description.getName());
            setFlashMessage(request, "The description '" + description.getDisplayName() + "' was deleted successfully.");

            URI redirectURI = UriBuilder.fromUri(uri.getBaseUri())
                    .path("descriptions").build();
            builder = Response.seeOther(redirectURI);
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED).entity(view);
        } catch (StoreNotFoundException e) {
            logger.info("Store not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND).entity(view);
        } catch (DescriptionNotFoundException e) {
            logger.info("Description not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND).entity(view);
        }

        return builder.build();
    }

}
