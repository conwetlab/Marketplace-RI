/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns, utils) {

    "use strict";

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.Store = function Store(data) {
        for (var name in data) {
            this[name] = data[name];
        }

        this.element = $('<a class="list-group-item store-item">').attr('href', [
            app.contextPath,
            'stores',
            this.name,
            'offerings']
        .join('/'));

        var thumbnailElement = $('<span class="image-thumbnail image-thumbnail-sm">');

        if (this.imagePath) {
            thumbnailElement.append($('<img class="image image-circle">').attr('src', [
                app.contextPath, this.imagePath
            ].join('/')));
        } else {
            thumbnailElement.append(
                $('<span class="image image-circle image-default-darker">').append(
                        $('<span class="fa fa-building fa-inverse">')));
        }

        var storeHeading = $('<div class="store-heading">').append(
            thumbnailElement);

        var storeBody = $('<div class="store-body">').append(
            $('<span class="store-name">').text(this.displayName),
            $('<span class="store-url">').text(this.url));

        this.element.append(storeHeading, storeBody);
    };

    utils.members(ns.Store, {

        addClass: function addClass(classString) {
            this.element.addClass(classString);
            return this;
        },

        get: function get() {
            return this.element;
        }

    });

    // **********************************************************************************
    // CLASS DEFINITION
    // **********************************************************************************

    ns.StoreList = function StoreList() {
        this.elements = {};
    };

    utils.members(ns.StoreList, {

        add: function add(data) {
            var store = new ns.Store(data);

            this.elements[store.name] = store;

            return store;
        },

        get: function get(name) {
            return this.elements[name];
        }

    });

})(app.components, app.utils);
