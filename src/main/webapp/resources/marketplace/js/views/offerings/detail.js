/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns) {

    "use strict";

    ns.btnToggleBookmark = $('.btn-toggle-bookmark');

    ns.toggleBookmark = function toggleBookmark() {
        app.requests.create({
            namespace: "offerings:entry_bookmark",
            kwargs: {
                descriptionName: ns.descriptionName,
                storeName: ns.storeName,
                offeringName: ns.offeringName
            },
            onSuccess: function () {
                if (ns.btnToggleBookmark.hasClass('tab-danger')) {
                    ns.btnToggleBookmark
                        .removeClass('tab-danger')
                        .find('.hidden-sm')
                        .text('Add bookmark');
                } else {
                    ns.btnToggleBookmark
                        .addClass('tab-danger')
                        .find('.hidden-sm')
                        .text('Remove bookmark');
                }
            }
        });
    };

})(app.view);
