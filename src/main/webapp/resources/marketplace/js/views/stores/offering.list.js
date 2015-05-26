/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns) {

    "use strict";

    ns.offeringList = $('.offering-group');

    app.requests.attach('stores:collection', 'read', {
        namespace: "offerings:store_collection",
        kwargs: {
            name: ns.storeName
        },
        container: ns.offeringList,
        alert: app.createAlert('warning', "No offering available.", 'col-sm-10'),
        onSuccess: function (collection, container) {
            collection.forEach(function (offeringInfo) {
                container.append(app.createOffering(offeringInfo).element);
            });
        }
    });

})(app.view);
