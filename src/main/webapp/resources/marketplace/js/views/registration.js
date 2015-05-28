/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns, utils) {

    "use strict";

    ns.registrationForm = app.createForm('registration_form', [
        new app.fields.TextField('displayName', {
            label: "Full name",
            minlength: 3,
            maxlength: 30,
            regexp: new RegExp(utils.patternDisplayName, "i"),
            errorMessages: {
                invalid: "Enter at most one valid word."
            }
        }),
        new app.fields.EmailField('email', {
            label: "E-mail"
        }),
        new app.fields.PasswordField('password', {
            label: "Password",
            minlength: 8,
            maxlength: 30,
            regexp: new RegExp(utils.patternPassword, "i"),
            errorMessages: {
                invalid: "Enter at least one digit, one letter and one special char."
            }
        }),
        new app.fields.PasswordField('passwordConfirm', {
            label: "Confirm your password"
        })
    ])
    .addValidator('passwordConfirm', function (value) {
        if (value !== this.data.password) {
            throw new app.validators.ValidationError("The two passwords do not match.");
        }
    });

})(app.view, app.utils);
