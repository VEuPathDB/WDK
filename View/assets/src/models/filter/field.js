wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var Field = ns.Field = Backbone.Model.extend({
    idAttribute: 'term',

    defaults: {
      filteredValues: [],
      display: false
    },

    select: function() {
      this.trigger('select', this);
    }
  });

  // Use String as constructor so that the following is true:
  //
  //     'Uknown' !== Field.UNKNOWN_VALUE
  //
  // This will make it possible to differentiate between this
  // and a valid value of 'Unknown'.
  Field.UNKNOWN_VALUE = new String('Unknown'); // jshint ignore:line

  ns.Fields = Backbone.Collection.extend({
    model: Field
  });

});
