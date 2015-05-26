/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns) {

    ns.panelViews = $('#left-sidebar');
    ns.panelPrefs = $('#right-sidebar');
    ns.storeList = $('.store-group');

    ns.togglePrefs = $('#toggle-right-sidebar');
    ns.togglePrefs.on('click', function (event) {
        event.preventDefault();

        if (this.classList.contains('active')) {
            this.classList.remove('active');
            ns.panelPrefs.removeClass('active');
        } else {
            this.classList.add('active');
            ns.panelPrefs.addClass('active');
        }

        event.stopPropagation();
    });

    ns.toggleViews = $('#toggle-left-sidebar');
    ns.toggleViews.on('click', function (event) {
        event.preventDefault();

        if (this.classList.contains('active')) {
            this.classList.remove('active');
            ns.panelViews.removeClass('active');
        } else {
            this.classList.add('active');
            ns.panelViews.addClass('active');
        }

        event.stopPropagation();
    });

    ns.logout = function logout() {
        document.logout_form.submit();
    };

    app.requests.attach('core', 'read', {
        namespace: "stores:collection",
        container: ns.storeList,
        alert: app.createAlert('warning', "No store available."),
        onSuccess: function (collection, container) {
            collection.forEach(function (data) {
                container.append(app.createStore(data).get());
            });
        }
    });

})(app.view);
