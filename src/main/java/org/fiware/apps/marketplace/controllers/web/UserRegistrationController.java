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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.fiware.apps.marketplace.exceptions.NotAuthorizedException;
import org.fiware.apps.marketplace.exceptions.ValidationException;
import org.fiware.apps.marketplace.model.User;
import org.fiware.apps.marketplace.model.validators.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


@Component
@Path("register")
public class UserRegistrationController extends AbstractController {

    @Autowired private UserValidator userValidator;
    private static Logger logger = LoggerFactory.getLogger(UserRegistrationController.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response registerFormView() {

        ModelAndView view;
        ModelMap model = new ModelMap();

        model.addAttribute("title", "Sign Up - " + getContextName());
        model.addAttribute("current_view", "register_user");
        view = new ModelAndView("core.register", model);

        return Response.ok().entity(view).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response registerFormView(
            @Context UriInfo uri,
            @Context HttpServletRequest request,
            @FormParam("displayName") String displayName,
            @FormParam("email") String email,
            @FormParam("password") String password,
            @FormParam("passwordConfirm") String passwordConfirm) {

        ModelAndView view;
        ModelMap model = new ModelMap();
        ResponseBuilder builder;
        User user = new User();
        URI redirectURI;

        try {
            model.addAttribute("title", "Sign Up - " + getContextName());
            model.addAttribute("current_view", "register_user");
            
            // Exception is throw if passwords do not match
            checkPasswordConfirmation(password, passwordConfirm);
            
            user.setDisplayName(displayName);
            user.setEmail(email);
            user.setPassword(password);
            getUserBo().save(user);

            redirectURI = UriBuilder.fromUri(uri.getBaseUri()).path("login").build();
            setFlashMessage(request, "You was registered successfully. You can log in right now.");

            builder = Response.seeOther(redirectURI);
        } catch (NotAuthorizedException e) {
            logger.info("User unauthorized", e);

            view = buildErrorView(Status.UNAUTHORIZED, e.getMessage());
            builder = Response.status(Status.UNAUTHORIZED).entity(view);
        } catch (ValidationException e) {
            logger.info("A form field is not valid", e);

            Map<String, String> formInfo = new HashMap<String, String>();

            formInfo.put("displayName", displayName);
            formInfo.put("email", email);
            formInfo.put("password", password);
            formInfo.put("passwordConfirm", passwordConfirm);

            model.addAttribute("form_data", formInfo);
            model.addAttribute("form_error", e);

            view = new ModelAndView("core.register", model);
            builder = Response.status(Status.BAD_REQUEST).entity(view);
        }

        return builder.build();
    }

}
