/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns) {

    "use strict";

    ns.Offering = function Offering(data) {
        for (var name in data) {
            this[name] = data[name];
        }

        this.element = $('<div class="panel panel-default-lighter offering-item">');
        this.store   = app.getStore(this.describedIn.store);

        var offeringHeading = $('<div class="panel-heading text-center">').append(
            $('<span class="image-thumbnail">').append(
                $('<img class="image image-rounded image-bordered">')
                    .addClass('offering-image')
                    .attr('src', this.imageUrl)),
            $('<a class="panel-title">').text(this.displayName).attr('href', [
                app.contextPath,
                'offerings',
                this.store.name,
                this.describedIn.name,
                this.name
            ].join('/')));

        var offeringBody = $('<div class="panel-body">').append(
            $('<a class="offering-store">').text(this.store.displayName).attr('href', [
                app.contextPath, 'stores', this.store.name, 'offerings'
            ].join('/')),
            $('<div class="offering-description">').text(this.description));

        this.element.append(offeringHeading, offeringBody);
    };

})(app.components);
