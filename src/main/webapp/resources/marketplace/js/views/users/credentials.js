/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns, utils) {

    "use strict";

    ns.passwordUpdateForm = app.createForm('account_password_update_form', [
        new app.fields.PasswordField('oldPassword', {
            label: "Old password"
        }),
        new app.fields.PasswordField('password', {
            label: "New password",
            minlength: 8,
            maxlength: 30,
            regexp: new RegExp(utils.patternPassword, "i"),
            errorMessages: {
                invalid: "Enter at least one digit, one letter and one special char."
            }
        }),
        new app.fields.PasswordField('passwordConfirm', {
              label: "Confirm new password"
        })
    ])
    .addValidator('passwordConfirm', function (value) {
        if (value !== this.data.password) {
            throw new app.validators.ValidationError("The two passwords do not match.");
        }
    });

})(app.view, app.utils);
