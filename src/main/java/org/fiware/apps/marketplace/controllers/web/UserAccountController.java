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
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.UserNotFoundException;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


@Component
@Path("account")
public class UserAccountController extends AbstractController {

    private static Logger logger = LoggerFactory.getLogger(UserAccountController.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response detailView(
            @Context HttpServletRequest request) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;

        try {
            model.addAttribute("title", "Account Settings - " + getContextName());
            model.addAttribute("user", getCurrentUser());
            model.addAttribute("currentView", "detail");

            addFlashMessage(request, model);

            view = new ModelAndView("user.detail", model);
            builder = Response.ok();
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError();
        }

        return builder.entity(view).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response updateView(
            @Context UriInfo uri,
            @Context HttpServletRequest request,
            @FormParam("userName") String userName,
            @FormParam("displayName") String displayName,
            @FormParam("email") String email,
            @FormParam("company") String company) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;
        User user = new User();
        User currentUser;
        URI redirectURI;

        try {
            currentUser = getCurrentUser();
            model.addAttribute("user", currentUser);
            model.addAttribute("title", "Account Settings - " + getContextName());

            user.setDisplayName(displayName);
            user.setEmail(email);

            if (!company.isEmpty()) {
                user.setCompany(company);
            }

            getUserBo().update(userName, user);

            redirectURI = UriBuilder.fromUri(uri.getBaseUri()).path("account").build();
            setFlashMessage(request, "Your profile was updated successfully.");

            builder = Response.seeOther(redirectURI);
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError().entity(view);
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED).entity(view);
        } catch (ValidationException e) {
            logger.info("A form field is not valid", e);

            Map<String, String> formInfo = new HashMap<String, String>();

            formInfo.put("userName", userName);
            formInfo.put("displayName", displayName);
            formInfo.put("email", email);
            formInfo.put("company", company);

            model.addAttribute("form_data", formInfo);
            model.addAttribute("form_error", e);

            view = new ModelAndView("user.detail", model);
            builder = Response.status(Status.BAD_REQUEST).entity(view);
        }

        return builder.build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("password")
    public Response credentialsView(
            @Context HttpServletRequest request) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;

        try {
            model.addAttribute("title", "Credentials - " + getContextName());
            model.addAttribute("user", getCurrentUser());
            model.addAttribute("currentView", "credentials");

            addFlashMessage(request, model);

            view = new ModelAndView("user.credentials", model);
            builder = Response.ok();
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError();
        }

        return builder.entity(view).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("password")
    public Response updatePasswordView(
            @Context UriInfo uri,
            @Context HttpServletRequest request,
            @FormParam("oldPassword") String oldPassword,
            @FormParam("password") String password,
            @FormParam("passwordConfirm") String passwordConfirm) {

        HttpSession session;
        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;
        User user = new User();
        User currentUser;
        URI redirectURI;

        try {
            currentUser = getCurrentUser();
            model.addAttribute("user", currentUser);
            model.addAttribute("title", "Credentials - " + getContextName());

            user.setPassword(password);

            // Validate old password
            if (!getUserBo().checkCurrentUserPassword(oldPassword)) {
            	throw new ValidationException("oldPassword", "The password given is not valid.");
            }

            // Exception risen if passwords don't match
            checkPasswordConfirmation(password, passwordConfirm);

            getUserBo().update(currentUser.getUserName(), user);

            session = request.getSession();

            // TODO: Invalidate all the user's session
            synchronized (session) {
                session.invalidate();
            }

            redirectURI = UriBuilder.fromUri(uri.getBaseUri()).path("login")
                    .queryParam("out", 3).build();
            builder = Response.seeOther(redirectURI);
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError().entity(view);
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED).entity(view);
        } catch (ValidationException e) {
            logger.info("A form field is not valid", e);

            model.addAttribute("form_error", e);

            view = new ModelAndView("user.credentials", model);
            builder = Response.status(Status.BAD_REQUEST).entity(view);
        }

        return builder.build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("delete")
    public Response deleteView(
            @Context HttpServletRequest request,
            @Context UriInfo uri) {

        HttpSession session;
        ModelAndView view;
        ResponseBuilder builder;
        URI redirectURI;

        try {
            User user = getCurrentUser();
            getUserBo().delete(user.getUserName());

            session = request.getSession();

            // TODO: Delete all the user's session
            synchronized (session) {
                session.invalidate();
            }

            redirectURI = UriBuilder.fromUri(uri.getBaseUri()).path("login")
                    .queryParam("out", 2).build();
            builder = Response.seeOther(redirectURI);
        } catch (UserNotFoundException e) {
            logger.warn("User not found", e);

            view = buildErrorView(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            builder = Response.serverError().entity(view);
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED).entity(view);
        } 

        return builder.build();
    }

}
