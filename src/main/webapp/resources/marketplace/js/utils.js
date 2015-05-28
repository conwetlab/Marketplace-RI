/**
 * Copyright (c) 2015, CoNWeT Lab., Universidad Politécnica de Madrid
 * Code licensed under BSD 3-Clause (https://github.com/conwetlab/WMarket/blob/master/LICENSE)
 */

(function (ns) {

    "use strict";

    var emailRE = {
        user:   "^[-!#$%&'*+/=?^_`{}|~A-Z0-9]+" +
                "(\\.[-!#$%&'*+/=?^_`{}|~0-9A-Z]+)*",
        domain: "((?:[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?\\.)+)" +
                "(?:[A-Z0-9-]{2,63}(?!-))$"
    };

    var ipv4RE = "(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)" +
                 "(?:\\.(?:25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";

    var hostRE = {
        name:   "[A-Z0-9](?:[A-Z0-9-]*[A-Z0-9])?",
        domain: "(?:\\.[A-Z0-9]+(?:[A-Z0-9-]*[A-Z0-9]+)*)*",
        tld:    "\\.[A-Z]{2,}\\.?"
    };

    var urlRE = {
        scheme: "^(?:[A-Z0-9\\.\\-]*)://",
        host:   "(?:" + ipv4RE + "|" +
                "(" + hostRE.name + hostRE.domain + hostRE.tld + "|localhost))",
        port:   "(?::\\d{2,5})?",
        path:   "(?:[/?#][^\\s]*)?$"
    };

    var cloneProperty = function cloneProperty(sourceValue) {

        if (isNull(sourceValue)) {
            return null;
        }

        if (isSimple(sourceValue)) {
            return this.cloneObject(sourceValue);
        }

        if (Array.isArray(sourceValue)) {
            return sourceValue.slice();
        }

        return sourceValue;
    };

    var isNull = function isNull(source) {
        return typeof source === 'undefined' || source === null;
    };

    var isSimple = function isSimple(source) {
        return typeof source === 'object' && source !== null && source.constructor === Object;
    };

    var isSubClass = function isSubClass(parentClass, childClass) {
        var c1, c2, found = false;

        if (typeof parentClass !== 'function' || typeof childClass !== 'function') {
            return found;
        }

        c1 = parentClass.prototype;
        c2 = childClass.prototype;

        while (!(found = (c1 === c2))) {
            c2 = Object.getPrototypeOf(c2);

            if (c2 === null || (c2 === Object.prototype && c1 !== c2)) {
                break;
            }
        }

        return found;
    };

    var updateProperty = function updateProperty(targetValue, sourceValue) {

        if (isNull(sourceValue)) {
            if (isNull(targetValue)) {
                targetValue = null;
            }
        } else if (typeof sourceValue === 'function') {
            if (isNull(targetValue) || isSubClass(targetValue, sourceValue)) {
                targetValue = sourceValue;
            }
        } else if (isSimple(sourceValue)) {
            if (isNull(targetValue) || isSimple(targetValue)) {
                targetValue = this.updateObject(targetValue, sourceValue);
            }
        } else if (Array.isArray(sourceValue)) {
            if (isNull(targetValue)) {
                targetValue = [];
            }
            if (Array.isArray(targetValue)) {
                targetValue = targetValue.concat(sourceValue);
            }
        } else {
            if (isNull(targetValue) || targetValue.constructor === sourceValue.constructor) {
                targetValue = sourceValue;
            }
        }

        return targetValue;
    };

    // **********************************************************************************
    // NAMESPACE DEFINITION
    // **********************************************************************************

    ns.formatString = function formatString(target, namedArgs) {
        for (var name in namedArgs) {
            target = target.replace("%(" + name + ")s", namedArgs[name]);
        }
        return target;
    };

    ns.cloneObject = function cloneObject(source) {
        var target = {};

        if (isNull(source)) {
            return target;
        }

        if (!isSimple(source)) {
            throw new TypeError("[error description]");
        }

        for (var name in source) {
            target[name] = cloneProperty.call(ns, source[name]);
        }

        return target;
    };

    ns.updateObject = function updateObject(target, source) {
        target = ns.cloneObject(target);
        source = ns.cloneObject(source);

        for (var name in source) {
            target[name] = updateProperty.call(ns, target[name], source[name]);
        }

        return target;
    };

    ns.patternEmail = emailRE.user + "@" + emailRE.domain;

    ns.patternURL = urlRE.scheme + urlRE.host + urlRE.port + urlRE.path;

    ns.patternDisplayName = "^[A-Z]+( [A-Z]+)*$";

    ns.patternPassword = "^.*(?=.*[A-Z])(?=.*\\d)(?=.*[!#$%&?]).*$";

    ns.members = function members(childConstructor, memberGroup) {
        for (var name in memberGroup) {
            childConstructor.prototype[name] = memberGroup[name];
        }
    };

    ns.inherit = function inherit(childConstructor, superConstructor) {
        var counter = 0;

        childConstructor.prototype = Object.create(superConstructor.prototype);

        ns.members(childConstructor, {

            constructor: childConstructor,

            superConstructor: superConstructor,

            superClass: function superClass() {
                var currentClass = superConstructor;

                for (var i = 0; i < counter; i++) {
                    currentClass = currentClass.prototype.superConstructor;
                }

                counter++;

                try {
                    currentClass.apply(this, Array.prototype.slice.call(arguments));
                } catch (e) {
                    counter = 0;
                    throw e;
                }

                counter--;
            }

        });
    };

})(app.utils);
