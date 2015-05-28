/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns) {

    "use strict";

    ns.linkedUSDL = '<a target="_blank" href="http://linked-usdl.org/"><span class="fa fa-info-circle"></span></a>';

    ns.descriptionUpdateForm = app.createForm('description_update_form', [
        new app.fields.TextField('displayName', {
            label: "Name",
            minlength: 3,
            maxlength: 20,
            regexp: new RegExp("^[A-Z]+[\\w.-]*( [\\w.-]+)*$", "i"),
            errorMessages: {
                invalid: "This field must contain alphanumerics (and -,_,.)."
            }
        }),
        new app.fields.URLField('url', {
            label: "URL to Linked USDL file " + ns.linkedUSDL
        }),
        new app.fields.LongTextField('comment', {
            label: "Comment",
            required: false,
            maxlength: 200,
            controlAttrs: {
                rows: 4
            }
        })
    ]);

})(app.view);
