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

      var actions = _.extend({

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

      var selectedField = options.filters.length
        ? options.filters[0].field
        : _.find(options.fields, { leaf: 'true' });


      var filterService = LazyFilterService.create({
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


      // Append view to DOM and trigger 'ready'
      // --------------------------------------

      React.render(<AttributeFilter actions={actions} store={filterService}
        trimMetadataTerms={Boolean(options.trimMetadataTerms)}
        displayName={options.title} />, this.el);


      // this.listenTo(filterService, 'change', _.debounce(function() {
      //   var value = {
      //     values: _.pluck(filterService.get('filteredData'), 'term'),
      //     ignored: this.ignored,
      //     filters: this.filterService.get('filters')
      //   };
      //   this.trigger('change:value', this, value);
      // }, 200));
    }

    // showSpinner: function() {
    //   this.$('.filter-param-tabs').append(this.spinner.spin().el);
    // },

    // hideSpinner: function() {
    //   this.spinner.stop();
    // },

    // isIgnored: function(datum) {
    //   var term = _.isObject(datum) ? datum.term : datum;
    //   return _.contains(this.ignored, term);
    // },

    // toggleIgnored: function(datum, isIgnored) {
    //   var term = _.isObject(datum) ? datum.term : datum;
    //   var index = _.indexOf(this.ignored, term);
    //   var doUpdate = false;

    //   if (isIgnored && index === -1) {
    //     this.ignored.push(term);
    //     doUpdate = true;
    //   } else if (!isIgnored && index > -1) {
    //     this.ignored.splice(index, 1);
    //     doUpdate = true;
    //   }
    //   if (doUpdate) this.updateValue();
    //   return this;
    // },

    // // This is the user's selection of data with all
    // // filters applied, and all ignored values removed.
    // getSelectedData: function() {
    //   var ignored = this.ignored;
    //   return this.filterService.get('filteredData')
    //     .reduce(function(acc, data) {
    //       if (!_.contains(ignored, data.term)) acc.push(data);
    //       return acc;
    //     }, []);
    // },

    // // Trigger events with selected values.
    // // value - serialization of param
    // // selectedData - just the values selected
    // updateValue: function() {
    //   var data = this.getSelectedData();
    //   var value = {
    //     values: _.pluck(data, 'term'),
    //     ignored: this.ignored,
    //     filters: this.filterService.filters
    //   };
    //   this.trigger('change:value', this, value);
    //   this.trigger('change:selectedData', this, data);
    //   return this;
    // },

    // _setSelectedFieldDistribution: function() {
    //   var field = this.selectedField;
    //   return field && this.getFieldDistribution(field)
    //     .then(function(distribution) {
    //       field.set('distribution', distribution);
    //     });
    // },

    // // returns a promise that resolves to metadata for a property:
    // //     [ { data_term: metadata_value }, ... ]
    // getMetadata: function(field) {
    //   return new RSVP.Promise(function(resolve, reject) {
    //     var term = field.get('term');
    //     var type = field.get('type');

    //     // if it's cached, return a promise that resolves immediately
    //     if (this.metadata[term]) {
    //       resolve(this.metadata[term]);
    //       return;
    //     }

    //     var metadataUrl = wdk.webappUrl('getMetadata.do');
    //     var metadataUrlParams = {
    //       questionFullName: this.questionName,
    //       name: this.name,
    //       json: true,
    //       property: term
    //     };

    //     this.showSpinner();
    //     (this.metadataXhrQueue[term] = $.getJSON(metadataUrl, metadataUrlParams))
    //       .then(function(metadata) {
    //         metadata = _.indexBy(metadata, 'sample');
    //         // cache metadata and transform to a dict
    //         this.metadata[term] = this.data
    //           .reduce(function(acc, d) {
    //             var values = _.result(metadata[d.term], 'values');
    //             acc[d.term] = _.isUndefined(values)
    //               ? [ Field.UNKNOWN_VALUE ]
    //               : type == 'number' ? values.map(Number) : values;
    //             return acc;
    //           }, {});
    //         resolve(this.metadata[term]);
    //       }.bind(this))
    //       .fail(function(err) {
    //         // TODO Show user an error message
    //         reject(err);
    //       })
    //       .always(function() {
    //         delete this.metadataXhrQueue[term];
    //         if (_.size(this.metadataXhrQueue) === 0) {
    //           this.hideSpinner();
    //         }
    //       }.bind(this));
    //   }.bind(this));
    // },

    // // returns a promse which resolves to an array of objects:
    // // { value, count, filteredCount }
    // getFieldDistribution: function(field) {
    //   var term = field.get('term');
    //   var type = field.get('type');

    //   // Retrieve metadata and filtered data and return a promise
    //   return RSVP.hash({
    //     metadata: this.getMetadata(field),
    //     filteredData: this.filterService.getFilteredData({
    //       filters: this.filterService.filters,
    //       omit: [ term ]
    //     })
    //   }).then(function(results) {
    //     var filteredMetadata = results.filteredData
    //       .reduce(function(acc, fd) {
    //         acc[fd.term] = results.metadata[fd.term];
    //         return acc;
    //       }, {});
    //     var counts = countByValues(results.metadata);
    //     var filteredCounts = countByValues(filteredMetadata);

    //     return _.uniq(_.flatten(_.values(results.metadata)))
    //       .sort(type == 'number' ? numericSort : stringSort)
    //       .map(function(value) {
    //         return {
    //           value: value,
    //           count: Number(counts[value]),
    //           filteredCount: filteredCounts[value]
    //         };
    //       });
    //   }.bind(this));
    // },

    // // if field, abort current metadata requests
    // // and trigger request for new field
    // selectField: function(field) {
    //   if (field && field !== this.selectedField) {
    //     this.abortMetadataRequest(this.selectedField);
    //     this.selectedField = field;
    //     this._setSelectedFieldDistribution().then(function() {
    //       this.trigger('select:field', field);
    //     }.bind(this));
    //   }
    //   return this;
    // },

    // abortMetadataRequest: function(field) {
    //   if (field) {
    //     return _.result(this.metadataXhrQueue[field.get('term')], 'abort');
    //   }
    // },

    // getFieldFilter: function(field) {
    //   return this.filterService.filters.findWhere({
    //     field: field.get('term')
    //   });
    // }

  });

});
