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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.fiware.apps.marketplace.bo.OfferingBo;
import org.fiware.apps.marketplace.exceptions.DescriptionNotFoundException;
import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.OfferingNotFoundException;
import org.fiware.apps.marketplace.exceptions.StoreNotFoundException;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.model.Offering;
import org.fiware.apps.marketplace.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


@Component
@Path("offerings")
public class OfferingController extends AbstractController {

    @Autowired private OfferingBo offeringBo;

    private static Logger logger = LoggerFactory.getLogger(OfferingController.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response listView(
            @Context HttpServletRequest request) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;
        User user;

        try {
            user = getCurrentUser();

            model.addAttribute("user", user);
            model.addAttribute("title", "Catalogue - " + getContextName());

            addFlashMessage(request, model);

            view = new ModelAndView("offering.list", model);
            builder = Response.ok();
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError();
        }

        return builder.entity(view).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("{storeName}/{descriptionName}/{offeringName}")
    public Response detailView(
            @PathParam("storeName") String storeName,
            @PathParam("descriptionName") String descriptionName,
            @PathParam("offeringName") String offeringName) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        Offering offering;
        ResponseBuilder builder;

        try {
            model.addAttribute("user", getCurrentUser());

            offering = offeringBo.findOfferingByNameStoreAndDescription(
                    storeName, descriptionName, offeringName);

            model.addAttribute("offering", offering);
            model.addAttribute("title", offering.getDisplayName() + " - " + getContextName());

            if (offeringBo.getAllBookmarkedOfferings().contains(offering)) {
                model.addAttribute("bookmark", true);
            }

            view = new ModelAndView("offering.detail", model);
            builder = Response.ok();
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError();
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED);
        } catch (OfferingNotFoundException e) {
            logger.info("Offering not found", e);

            view = buildErrorView(Status.NOT_FOUND, e.getMessage());
            builder = Response.status(Status.NOT_FOUND);
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

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("bookmarks")
    public Response bookmarkListView(
            @Context HttpServletRequest request) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;
        User user;

        try {
            user = getCurrentUser();

            model.addAttribute("user", user);
            model.addAttribute("title", "My bookmarks - " + getContextName());

            addFlashMessage(request, model);

            view = new ModelAndView("offering.bookmark.list", model);
            builder = Response.ok();
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError();
        }

        return builder.entity(view).build();
    }

}
