import React from 'react';

/* global Spinner */
wdk.namespace('wdk.controllers', function(ns) {
  'use strict';


  // Imports
  // -------

  var { AttributeFilter } = wdk.components.attributeFilter;

  // models
  var LazyFilterService = wdk.models.filter.LazyFilterService;
  // var Fields = wdk.models.filter.Fields;
  // var Field = wdk.models.filter.Field;


  // FilterParam
  // -----------

  // options:
  //   - data: Array of data objects to filter
  //   - fields: Array of metadata terms
  //   - filters: Array of filter objects
  //   - ignored: Array of data ids to ignore
  //   - title: String name used in UI. Defaults to "Items".
  //   - trimMetadataTerms: Boolean, when true remove parents w/ one child
  //   - defaultColumns: Array of field names to show in results view
  //
  ns.FilterParam = wdk.views.core.View.extend({

    className: 'filter-param',

    initialize: function(options) {
      this.data = options.data;
      this.metadata = options.metadata || {};
      this.ignored = options.ignored || [];
      this.title = options.title || 'Items';
      this.questionName = options.questionName;
      this.dependedValue = options.dependedValue;
      this.name = options.name;
      this.defaultColumns = options.defaultColumns;
      this.spinner = new Spinner({
        lines: 11, // The number of lines to draw
        length: 3, // The length of each line
        width: 2, // The line thickness
        radius: 4, // The radius of the inner circle
        corners: 1, // Corner roundness (0..1)
        rotate: 0, // The rotation offset
        direction: 1, // 1: clockwise, -1: counterclockwise
        color: '#000', // #rgb or #rrggbb or array of colors
        speed: 1, // Rounds per second
        trail: 60, // Afterglow percentage
        shadow: false, // Whether to render a shadow
        hwaccel: false, // Whether to use hardware acceleration
        className: 'spinner', // The CSS class to assign to the spinner
        zIndex: 2e9, // The z-index (defaults to 2000000000)
        top: '34px', // Top position relative to parent
        left: '-222px' // Left position relative to parent
      });

      var actions = this.actions = _.extend({

        // Which field has focus
        selectField: function(field) {
          this.trigger('selectField', field);
        },

        // The store can decide if field-field is one-to-one
        addFilter: function(field, values) {
          this.trigger('addFilter', field, values);
        },

        removeFilter: function(filter) {
          this.trigger('removeFilter', filter);
        },

        addColumn: function(column) {
          this.trigger('addColumn', column);
        },

        removeColumn: function(column) {
          this.trigger('removeColumn', column);
        },

        addIgnored: function(datum) {
          this.trigger('addIgnored', datum);
        },

        removeIgnored: function(datum) {
          this.trigger('removeIgnored', datum);
        },

        updateColumns: function(fields) {
          this.trigger('updateColumns', fields);
        }

      }, Backbone.Events);

      _.bindAll(actions);

      // Set default values
      // ---------------------------

      var selectedField = options.filter && options.filters.length
        ? options.filters[0].field
        : null;
        // : _.find(options.fields, { leaf: 'true' });


      var filterService = this.filterService = LazyFilterService.create({
        filters: options.filters,
        fields: options.fields,
        ignored: options.ignored,
        data: options.data,
        columns: options.defaultColumns,
        name: options.name,
        questionName: options.questionName,
        dependedValue: options.dependedValue,
        selectedField: selectedField
      }, {
        intents: actions
      });

      filterService.on('change', function() {
        this.trigger('change:value', this, filterService.getState());
      }, this);


      // Render filter param
      // -------------------------------------

      React.render(
        <AttributeFilter
          actions={actions}
          store={filterService}
          trimMetadataTerms={Boolean(options.trimMetadataTerms)}
          displayName={options.title}
        />,
        this.el
      );
    }

  });

});
