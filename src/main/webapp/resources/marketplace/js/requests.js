/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns, utils) {

    "use strict";

    var endpoints = {
        offerings: {
            collection: "/offerings/",
            store_collection: "/store/%(name)s/offering/",
            entry_bookmark: "/store/%(storeName)s/description/%(descriptionName)s/offering/%(offeringName)s/bookmark"
        },
        stores: {
            collection: "/store/"
        }
    };

    var requests = {};

    var getResource = function getResource(options) {
        var type, name, namespaceArgs, url, attrs;

        namespaceArgs = options.namespace.split(':');

        name = namespaceArgs[0];
        type = namespaceArgs[1];

        url = endpoints[name][type];

        if (options.kwargs) {
            url = utils.formatString(url, options.kwargs);
        }

        if (options.queryString) {
            attrs = Object.keys(options.queryString);
            url += "?";

            for (var i = 0; i < attrs.length; i++) {
                url += attrs[i] + "=" + options.queryString[attrs[i]];
                if ((attrs.length - 1) !== i) {
                    url += "&";
                }
            }
        }

        return {
            'name': name,
            'type': type,
            'url': app.contextPath + "/api/v2" + url
        };
    };

    var makeRequest = function makeRequest(type, options) {
        var resource = getResource(options);

        if (options.container) {
            options.container.empty().append(ns.createSpinner());
        }

        $.ajax({
            async: true,
            type: type,
            url: resource.url,
            dataType: 'json',
            success: function (data) {
                var collection, i;

                if (resource.type.indexOf('collection') != -1) {
                    collection = data[resource.name];
                    options.container.empty();

                    if (!collection.length) {
                        options.container.append(options.alert);
                    } else {
                        options.onSuccess(collection, options.container, data);
                    }
                } else {
                    options.onSuccess(data);
                }

                ns.dispatch(options.namespace);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                options.onFailure(jqXHR.responseText);
            }
        });
    };

    ns.createSpinner = function createSpinner() {
        return $('<div class="loading-state">').append($('<span class="fa fa-spinner fa-pulse">'));
    };

    ns.read = function read(options) {
        makeRequest('GET', options);
    };

    ns.create = function create(options) {
        makeRequest('POST', options);
    };

    ns.attach = function attach(name, type, options) {
        if (!(name in requests)) {
            requests[name] = [];
        }

        requests[name].push({
            type: type,
            options: options
        });
    };

    ns.dispatch = function dispatch(name) {
        if (name in requests) {
            requests[name].forEach(function (request) {
                ns[request.type](request.options);
            });
        }
    };

})(app.requests, app.utils);
