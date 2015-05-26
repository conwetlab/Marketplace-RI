/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns, utils) {

    "use strict";

    var ValidationError    = app.validators.ValidationError,
        RequiredValidator  = app.validators.RequiredValidator,
        MinLengthValidator = app.validators.MinLengthValidator,
        MaxLengthValidator = app.validators.MaxLengthValidator,
        RegExpValidator    = app.validators.RegExpValidator,
        URISchemeValidator = app.validators.URISchemeValidator,
        MaxSizeValidator   = app.validators.MaxSizeValidator;

    ns.states = {
        CLEANED: 0,
        SUCCESS: 1,
        PENDING: 2,
        FAILURE: 3
    };

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.BaseField = function BaseField(name, options) {
        options = utils.updateObject(ns.BaseField.DEFAULTS, options);

        this.state = ns.states.CLEANED;
        this.name  = name;
        this.error = null;

        this.label   = $('<label class="field-label">').append(options.label);
        this.control = $(options.control).addClass('field-control').attr('name', this.name);

        this.readonly = options.readonly;

        if (this.readonly) {
            this.control.addClass('static');
        }

        for (var attrName in options.controlAttrs) {
            this.addAttr(attrName, options.controlAttrs[attrName]);
        }

        this.element = $('<div class="form-field">').append(this.label, this.control);

        if (options.required) {
            options.validators.unshift(new RequiredValidator());
        } else {
            this.label.append($('<small>').text("optional"));
        }

        this.validators = options.validators.map(function (validator) {
            if (typeof validator !== 'function' && validator.code in options.errorMessages) {
                validator.addMessage(options.errorMessages[validator.code]);
            }
            return validator;
        });

        this.events = {
            failure: [],
            success: [],
            update:  []
        };

        Object.defineProperty(this, 'errorMessage', {
            get: function get() {
                return this.error !== null ? this.error.message : null;
            }
        });

        Object.defineProperty(this, 'value', {
            get: function get() {
                return this.control.val().trim();
            }
        });
    };

    ns.BaseField.DEFAULTS = {
        required:      true,
        readonly:      false,
        label:         "",
        helpText:      "",
        control:       "",
        controlAttrs:  {},
        validators:    [],
        errorMessages: {}
    };

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.BaseField, {

        _insertError: function _insertError(error) {
            this.control.after(error.element);
            this.error = error;

            return dispatchEvent.call(this, 'failure', error.message);
        },

        _insertValue: function _insertValue(value) {
            this.control.val(value);

            return dispatchEvent.call(this, 'success', value);
        },

        addAttr: function addAttr(name, value) {
            this.control.attr(name, value);
            return this;
        },

        addError: function addError(error) {
            return this.clean()._insertError(error);
        },

        addInitialValue: function addInitialValue(value) {
            return this.clean()._insertValue(value);
        },

        addValidator: function addValidator(validator) {
            this.validators.push(validator);
            return this;
        },

        attach: function attach(name, handler) {
            this.events[name].push(handler);
            return this;
        },

        clean: function clean() {
            if (this.state === ns.states.FAILURE) {
                removeError.call(this);
            }

            this.state = ns.states.CLEANED;
            return this;
        },

        dispatch: function dispatch(name) {
            var handlerArgs = Array.prototype.slice.call(arguments, 1);

            this.events[name].forEach(function (handler) {
                handler.apply(this, handlerArgs);
            }, this);

            return this;
        },

        validate: function validate() {
            if (this.readonly) {
                return this;
            }

            var value = this.clean().value;
            return findError.call(this, value) ? this : dispatchEvent.call(this, 'success', value);
        }

    });

    // **********************************************************************************
    // PRIVATE MEMBERS
    // **********************************************************************************

    var dispatchEvent = function dispatchEvent(name, context) {
        switch (name) {
        case 'success':
            this.state = ns.states.SUCCESS;
            break;
        case 'failure':
            this.state = ns.states.FAILURE;
            break;
        }
        return this.dispatch(name, context, this);
    };

    var findError = function findError(value) {
        return this.validators.some(function (validator) {
            return triggerValidator.call(this, validator, value);
        }, this);
    };

    var removeError = function removeError() {
        this.error.element.remove();
        this.error = null;
        return this;
    };

    var triggerValidator = function triggerValidator(validator, value) {
        try {
            if (typeof validator === 'function') {
                validator(value, this);
            } else {
                validator.trigger(value, this);
            }
        } catch (e) {
            this._insertError(e);
        }

        return this.state === ns.states.FAILURE;
    };

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.TextField = function TextField(name, options) {
        options = utils.updateObject(ns.TextField.DEFAULTS, options);

        if (options.regexp instanceof RegExp) {
            options.validators.unshift(new RegExpValidator(options.regexp));
        }

        if (options.maxlength > options.minlength) {
            options.validators.unshift(new MaxLengthValidator(options.maxlength));
        }

        if (options.minlength > 0) {
            options.validators.unshift(new MinLengthValidator(options.minlength));
        }

        this.superClass(name, options);
        this.control.on('blur input', cleanSpacesBeforeValidate.bind(this));
    };

    ns.TextField.DEFAULTS = {
        minlength:  0,
        maxlength:  0,
        regexp:     null,
        control:    '<input type="text">',
        validators: []
    };

    utils.inherit(ns.TextField, ns.BaseField);

    // **********************************************************************************
    // PRIVATE MEMBERS
    // **********************************************************************************

    var cleanSpacesBeforeValidate = function cleanSpacesBeforeValidate(event) {
        switch (event.type) {
        case 'blur':
            this.control.val(this.value);
            break;
        }

        this.validate();
    };

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.PasswordField = function PasswordField(name, options) {
        this.superClass(name, utils.updateObject(ns.PasswordField.DEFAULTS, options));
    };

    ns.PasswordField.DEFAULTS = {
        control: '<input type="password">'
    };

    utils.inherit(ns.PasswordField, ns.TextField);

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.EmailField = function EmailField(name, options) {
        this.superClass(name, utils.updateObject(ns.EmailField.DEFAULTS, options));
    };

    ns.EmailField.DEFAULTS = {
        regexp: new RegExp(utils.patternEmail, "i"),
        errorMessages: {
            invalid: "This field must be a valid email address."
        }
    };

    utils.inherit(ns.EmailField, ns.TextField);

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.URLField = function URLField(name, options) {
        options = utils.updateObject(ns.URLField.DEFAULTS, options);

        if (options.schemes.length) {
            options.validators.unshift(new URISchemeValidator(options.schemes));
        }

        this.superClass(name, options);
    };

    ns.URLField.DEFAULTS = {
        regexp: new RegExp(utils.patternURL, "i"),
        schemes: ['http', 'https'],
        validators: [],
        errorMessages: {
            invalid: "This field must be a valid URL."
        }
    };

    utils.inherit(ns.URLField, ns.TextField);

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.LongTextField = function LongTextField(name, options) {
        this.superClass(name, utils.updateObject(ns.LongTextField.DEFAULTS, options));
    };

    ns.LongTextField.DEFAULTS = {
        control: '<textarea>'
    };

    utils.inherit(ns.LongTextField, ns.TextField);

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.ChoiceField = function ChoiceField(name, options) {
        options = utils.updateObject(ns.ChoiceField.DEFAULTS, options);
        this.superClass(name, options);

        Object.defineProperty(this, 'choices', {
            get: function get() {
                return this.control.prop('options');
            }
        });

        Object.defineProperty(this, 'selected', {
            get: function get() {
                return this.choices[this.control.prop('selectedIndex')];
            }
        });

        for (var choiceName in options.choices) {
            this.addChoice(choiceName, options.choices[choiceName]);
        }
    };

    ns.ChoiceField.DEFAULTS = {
        control: "<select>",
        choices: {}
    };

    utils.inherit(ns.ChoiceField, ns.BaseField);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.ChoiceField, {

        addChoice: function addChoice(name, value) {
            this.control.append($('<option>').val(name).text(value));

            return this;
        }

    });

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.FileField = function FileField(name, options) {
        options = utils.updateObject(ns.FileField.DEFAULTS, options);

        if (options.maxvalue.size) {
            options.validators.unshift(new MaxSizeValidator(options.maxvalue.size, {
                unit: options.maxvalue.unit
            }));
        }

        this.superClass(name + "Base64", options);

        this.textField = $('<input type="text" class="field-control">')
            .attr('name', name + "Name").attr('readonly', true);

        var btnContainer = $('<span class="btn btn-default btn-file">').append(
            $('<span class="btn-icon fa fa-folder-open">'),
            $('<span class="btn-text">').text(options.btnText));

        this.controlGroup = $('<div class="field-control-group">').append(
            this.textField,
            $('<span class="field-control-btn">').append(btnContainer));

        this.control.removeAttr('class').replaceWith(this.controlGroup);
        btnContainer.append(this.control);

        Object.defineProperty(this, 'file', {
            get: function get() {
                return this.value ? this.control[0].files[0] : null;
            }
        });

        this.control.on('change', handleChangeEvent.bind(this));
    };

    ns.FileField.DEFAULTS = {
        btnText: "Browser",
        control: '<input type="file">',
        maxvalue: {
            size: 0,
            unit: "MiB"
        },
        validators: []
    };

    utils.inherit(ns.FileField, ns.BaseField);

    // **********************************************************************************
    // PUBLIC MEMBERS
    // **********************************************************************************

    utils.members(ns.FileField, {

        _insertError: function _insertError(error) {
            this.controlGroup.after(error.element);
            this.error = error;

            this.control.val("");

            return dispatchEvent.call(this, 'failure', error.message);
        },

        addInitialValue: function addInitialValue(value) {
            return this;
        },

        validate: function validate() {
            ns.BaseField.prototype.validate.call(this);
            this.textField.val(this.file !== null ? this.file.name : "");
            return this;
        }

    });

    // **********************************************************************************
    // PRIVATE MEMBERS
    // **********************************************************************************

    var handleChangeEvent = function handleChangeEvent(event) {
        this.validate();
    };

})(app.fields, app.utils);
