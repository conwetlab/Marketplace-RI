/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns, utils) {

    "use strict";

    var ValidationError = app.validators.ValidationError;

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.FormValidator = function FormValidator(name, fields) {
        this.element = $(document.forms[name]);
        this.data    = {};
        this.errors  = {};

        this.fields       = [];
        this.fieldsByName = {};

        fields.forEach(function (field) {
            this.addField(field);
        }, this);
        
        this.element.on('submit', handleSubmitEvent.bind(this));
    };

    utils.members(ns.FormValidator, {

        addErrorMessage: function addErrorMessage(name, errorMessage) {
            this.fieldsByName[name].addError(new ValidationError(errorMessage));

            return this;
        },

        addField: function addField(field) {
            if (this.fields.length) {
                field.element.insertAfter(this.fields[this.fields.length - 1].element);
            } else {
                this.element.prepend(field.element);
            }

            field.attach('success', function (value) {
                addFieldValue.call(this, field.name, value);
            }.bind(this));

            field.attach('failure', function (errorMessage) {
                addFieldError.call(this, field.name, errorMessage);
            }.bind(this));

            this.fields.push(field);
            this.fieldsByName[field.name] = field;
            return this;
        },

        addInitialValue: function addInitialValue(name, initialValue) {
            this.fieldsByName[name].addInitialValue(initialValue.trim());

            return this;
        },

        addValidator: function addValidator(name, simpleValidator) {
            this.fieldsByName[name].addValidator(simpleValidator.bind(this));
            return this;
        },

        cleanAll: function cleanAll() {
            this.fields.forEach(function (field) {
                field.clean();
                cleanField.call(this, field.name);
            }, this);

            return this;
        },

        isValid: function isValid() {
            return this.cleanAll().fields.every(function (field) {
                field.validate();
                return !Object.keys(this.errors).length;
            }, this);
        }

    });

    var addFieldError = function addFieldError(name, errorMessage) {
        this.errors[name] = errorMessage;
        delete this.data[name];
        return this;
    };

    var addFieldValue = function addFieldValue(name, value) {
        this.data[name] = value;
        delete this.errors[name];
        return this;
    };

    var cleanField = function cleanField(name) {
        delete this.data[name];
        delete this.errors[name];
        return this;
    };

    var handleSubmitEvent = function handleSubmitEvent(event) {
        if (!this.isValid(event)) {
            event.preventDefault();
        }
    };

})(app.forms, app.utils);
