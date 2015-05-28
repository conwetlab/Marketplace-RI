/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns) {

    "use strict";

    var createAlert = function createAlert(type, icon, message, extraClass) {
        return $('<div>').addClass('alert alert-' + type).addClass(extraClass).append(icon, " ", message);
    };

    ns.alerts = {

        info: function info(message, extraClass) {
            return createAlert('info', $('<span>').addClass("fa fa-info-circle"), message, extraClass);
        },

        warning: function warning(message, extraClass) {
            return createAlert('warning', $('<span>').addClass("fa fa-exclamation-circle"), message, extraClass);
        }

    };

})(app.components);
