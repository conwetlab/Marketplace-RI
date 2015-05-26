/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns, utils) {

    "use strict";

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.ValidationError = function ValidationError(message) {
        this.element = $('<p class="field-error">').text(message);
        this.message = message;
    };

    utils.inherit(ns.ValidationError, Error);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.ValidationError, {

        name: "ValidationError"

    });

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.BaseValidator = function BaseValidator(options) {
        this.addMessage(utils.updateObject(ns.BaseValidator.DEFAULTS, options).message);
    };

    ns.BaseValidator.DEFAULTS = {
        message: ""
    };

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.BaseValidator, {

        addMessage: function addMessage(message) {
            this.message = message;
            return this;
        },

        checkout: function checkout(value, field) {
            return true;
        },

        code: "not_provided",

        trigger: function trigger(value, field) {
            if (!this.checkout(value, field)) {
                throw new ns.ValidationError(this.message);
            }
            return this;
        }

    });

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.RequiredValidator = function RequiredValidator(options) {
        this.superClass(utils.updateObject(ns.RequiredValidator.DEFAULTS, options));
    };

    ns.RequiredValidator.DEFAULTS = {
        message: "This field is required."
    };

    utils.inherit(ns.RequiredValidator, ns.BaseValidator);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.RequiredValidator, {

        checkout: function checkout(value) {
            return value.length > 0;
        },

        code: "required"

    });

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.MinLengthValidator = function MinLengthValidator(minlength, options) {
        this.minlength = minlength;
        this.superClass(utils.updateObject(ns.MinLengthValidator.DEFAULTS, options));
    };

    ns.MinLengthValidator.DEFAULTS = {
        message: "This field must contain at least %(minlength)s chars."
    };

    utils.inherit(ns.MinLengthValidator, ns.BaseValidator);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.MinLengthValidator, {

        addMessage: function addMessage(message) {
            this.message = utils.formatString(message, {
                minlength: this.minlength
            });
            return this;
        },

        checkout: function checkout(value) {
            return !value || value.length >= this.minlength;
        },

        code: "min_length"

    });

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.MaxLengthValidator = function MaxLengthValidator(maxlength, options) {
        this.maxlength = maxlength;
        this.superClass(utils.updateObject(ns.MaxLengthValidator.DEFAULTS, options));
    };

    ns.MaxLengthValidator.DEFAULTS = {
        message: "This field must not exceed %(maxlength)s chars."
    };

    utils.inherit(ns.MaxLengthValidator, ns.BaseValidator);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.MaxLengthValidator, {

        addMessage: function addMessage(message) {
            this.message = utils.formatString(message, {
                maxlength: this.maxlength
            });
            return this;
        },

        checkout: function checkout(value) {
            return !value || value.length <= this.maxlength;
        },

        code: "max_length"

    });

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.RegExpValidator = function RegExpValidator(regexp, options) {
        this.regexp = regexp;
        this.superClass(utils.updateObject(ns.RegExpValidator.DEFAULTS, options));
    };

    ns.RegExpValidator.DEFAULTS = {
        message: "This field must be a valid value."
    };

    utils.inherit(ns.RegExpValidator, ns.BaseValidator);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.RegExpValidator, {

        checkout: function checkout(value) {
            return !value || this.regexp.test(value);
        },

        code: "invalid"

    });

    // **********************************************************************************
    //  CLASS DEFINITION
    // **********************************************************************************

    ns.URISchemeValidator = function URISchemeValidator(schemes, options) {
        this.schemes = schemes;
        this.superClass(utils.updateObject(ns.URISchemeValidator.DEFAULTS, options));
    };

    ns.URISchemeValidator.DEFAULTS = {
        message: "The URI scheme must be (%(schemes)s)."
    };

    utils.inherit(ns.URISchemeValidator, ns.BaseValidator);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.URISchemeValidator, {

        addMessage: function addMessage(message) {
            this.message = utils.formatString(message, {
                schemes: this.schemes.join(" | ")
            });
            return this;
        },

        checkout: function checkout(value) {
            return !value || this.schemes.indexOf(value.split("://")[0]) !== -1;
        },

        code: "invalid_scheme"

    });

    // **********************************************************************************
    //  CLASS DEFINITION
    // **********************************************************************************

    ns.MaxSizeValidator = function MaxSizeValidator(size, options) {
        options = utils.updateObject(ns.MaxSizeValidator.DEFAULTS, options);

        this.unit = fileUnits.indexOf(options.unit) !== -1 ? options.unit : "B";
        this.size = size;
        this.superClass(options);
    };

    ns.MaxSizeValidator.DEFAULTS = {
        unit:    "",
        message: "The file size must be less than %(size)s %(unit)s."
    };

    utils.inherit(ns.MaxSizeValidator, ns.BaseValidator);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.MaxSizeValidator, {

        addMessage: function addMessage(message) {
            this.message = utils.formatString(message, {
                size: this.size,
                unit: this.unit
            });

            return this;
        },

        checkout: function checkout(value, field) {
            if (!value) {
                return true;
            }

            var size = field.file.size;

            for (var i = 0; this.unit !== fileUnits[i] && i < fileUnits.length; i++) {
                size /= 1024;
            }

            return this.size >= size.toFixed(3);
        },

        code: "max_size"

    });

    // **********************************************************************************
    // PRIVATE MEMBERS
    // **********************************************************************************

    var fileUnits = ['B', 'KiB', 'MiB', 'GiB'];

})(app.validators, app.utils);
