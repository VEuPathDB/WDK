// adapted from https://github.com/semmypurewal/jermaine/blob/master/src/util/namespace.js
//
// Bootstrap the wdk namespace and make the namespace utility available at
// wdk.util.namespace. Assumes that jQuery is available on the window
// object and passes it as the second argument to the namespace closure.
//
// USAGE EXAMPLE
//     The following creates the namespace 'wdk.tooltip'
//
//        wdk.util.namespace("wdk.tooltip", function(ns, $) {
//          // ns resolves to "window.wdk.tooltip"
//          // $ resolves to jQuery
//          //
//          // attach some functionality to the ns object
//        });

(function (ns, root) {
   "use strict";

    var namespace = function (ns, func) {
        var nsRegExp = /^([a-zA-Z_]+)(\.[a-zA-Z_]*)*$/,
            nsArray,
            currentNS,
            i;

        //check to make sure ns is a properly formatted namespace string
        if (ns.match(nsRegExp) === null || ns === "window") {
            throw new Error("namespace: " + ns + " is a malformed namespace string");
        }

        //parse namespace string
        nsArray = ns.split(".");

        //set the root namespace to root (if it's not explictly stated)
        if (nsArray[0] === "window") {
            currentNS = root;
        } else {
            currentNS = (root[nsArray[0]] === undefined)?root[nsArray[0]] = {}:root[nsArray[0]];
        }

        //confirm func is actually a function
        if (typeof(func) !== "function") {
            throw new Error("namespace: second argument must be a function that accepts a namespace parameter");
        }

        //build namespace
        for (i = 1; i < nsArray.length; ++i) {
            if (currentNS[nsArray[i]] === undefined) {
                currentNS[nsArray[i]] = {};
            }
            currentNS = currentNS[nsArray[i]];
        }

        //if the function was defined, run it on the current namespace
        if (func) {
            func(currentNS, root.jQuery);
        }

        //return namespace
        return currentNS;
    };

    return namespace(ns, function (exports) {
        exports.namespace = namespace;
    });
}("wdk", typeof window !== 'undefined' ? window : global));

// for legacy
wdk.namespace('wdk.util', function(ns) {
  ns.namespace = wdk.namespace;
});
