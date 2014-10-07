# Notes on migrating from global namespaces to commonjs modules.

Currently WDK uses a global object to attach namespaces. This means that
anything that is exposed to a namespace is also globally accessible. In order to
migrate to commonjs moduldes (or specifically, browserify modules), we need a
plan migrating aways from accessing these things globally.


Additionally, we probably want to partition the build into one core bundle, and
one or more additional bundles. We can think of WDK as providing multiple components:

1. A framework-like structure with general utilities and with registration
hooks (viz data-controller HTML attributes);
2. Modules which utilize the things provided above.


## Removing Globals

When possible, use data-controller (or equivalent) to register a controller
function and use the Node.js require syntax in lieu. For instance, if you find
something like the following:


    <!-- page.html -->
    <div>
      <script>
        wdk.tooltips.setUpTooltips(...);
      </script>
      ...
    </div>

You can convert it to something like this:

    <!-- page.html -->
    <div data-controller="customThing">
      ...
    </div>

    // customThing.js
    var tooltips = require('wdk/tooltips');
    tooltips.setUpTooltips(...);


## Partitioning bundles

Some more research needed here. Look at
https://github.com/substack/browserify-handbook#partition-bundle

Ideally, we can create a single bundle, wdk.core.js, that will create a global
`require` function that allows core WDK modules to be `require`able from other
scripts.

A part of what WDK provides is a way to "register" functions to be called when
HTML with certain attributes are inserted into the DOM. This implementation should
be independent from the use of browserify (or any module authoring format). It
should also avoid the use of a global variable. This criteria makes me lean
towards using events, with the DOM as our event bus.

### Case 1: WDK finds the controller

WDK code might look like

    function findController(controllerName) {
      var controller = require(controllerName);
    }

This would require all controllers to be run through browserify, which will not
scale and is too implentation specific.


### Case 2: Register the controller

Client code might look like

    wdk.registerController('controllerName', function getController(el) {
      // return contructor based on some criteria
    });

This means creating a global object, which we are trying to avoid...


### Case 3: Use events

WDK code might look like this

    $el.trigger($el.data('controller'));

Client code might look like this

    $(document).on('my-controller', function(event) {
      // do stuff...
    });

This has the benefit of decoupling the controller resolution mechanism from any
other implementation. It solely relies upon jQuery and the DOM, which is fine by
me.
