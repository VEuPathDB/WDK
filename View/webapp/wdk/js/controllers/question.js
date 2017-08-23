// =============================================================================
// The js related to the display on question page

wdk.namespace("wdk.question", function(ns) {
  "use strict";

  let initializers = new Map;

  /**
   * Register a callback to be invoked with a question form is loaded.
   *
   * @param {string} questionName Fully qualified question name, as defined
   *   in model xml.
   * @param {Function} initializer Callback function that is invoke when the
   *   question form is loaded. Called with jQuery wrapped reference to the
   *   form element.
   */
  function registerInitializer(quesitonName, initializer) {
    if (typeof initializer !== 'function') {
      throw new Error('registerInitializer() expected a function, but got ' +
        (typeof initializer) + ' instead.');
    }
    if (initializers.has(quesitonName)) {
      console.warn('An initializer has already been registered for %o. ' +
        'Overriding with %o.', quesitonName, initializer);
    }
    initializers.set(quesitonName, initializer);
  }

  /**
   * Main question form initializer.
   */
  function init($el) {
    var questionFullName = $el.data('questionFullName');
    var $form = $el.closest('form');

    // turn off autocompletion
    $form.prop('autocomplete', 'off');

    // turn off html5 validation
    $form.attr('novalidate', '');

    // set up parameter handlers
    if ($el.data('showParams') === true) {
      wdk.parameterHandlers.init($el);
    }

    // initialize delegate views
    if (initializers.has(questionFullName)) {
      initializers.get(questionFullName)($form);
    }

    wdk.onloadQuestion();
  }

  Object.assign(ns, {
    init,
    registerInitializer
  });

});
