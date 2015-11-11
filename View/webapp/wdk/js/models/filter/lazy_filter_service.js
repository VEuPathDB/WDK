import {
  countByValues,
  uniqMetadataValues,
  combinePredicates,
  getMemberPredicate,
  getRangePredicate
} from './utils';


wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var FilterService = wdk.models.filter.FilterService;
  var Field = wdk.models.filter.Field;

  ns.LazyFilterService = FilterService.extend({

    constructor: function(attrs, options) {
      if (!attrs.name) {
        throw new Error('LazyFilterService requires a "name" attribute.');
      }

      if (!attrs.questionName) {
        throw new Error('LazyFilterService requires a "questionName" attribute.');
      }

      this.name = attrs.name;
      this.questionName = attrs.questionName;
      this.dependedValue = attrs.dependedValue;
      this.metadata = {};
      this.metadataXhrQueue = new Map();
      this.listenTo(options.intents, this.xhrActions);
      FilterService.prototype.constructor.apply(this, arguments);
    },

    xhrActions: {
      selectField: function(field) {
        this.cancelXhr(this._pendingSelectField);
        this._pendingSelectField = field;
      },

      removeColumn: function(field) {
        this.cancelXhr(field);
      },
    },

    cancelXhr: function(field) {
      if (field) {
        return _.result(this.metadataXhrQueue.get(field.term), 'abort');
      }
    },

    getFieldDistribution: function(field) {
      var term = field.term;
      var type = field.type;
      var otherFilters =_.reject(this.filters, function(filter) {
        return filter.field.term === term;
      });


      // Retrieve metadata and filtered data and return a promise
      return Promise.all([
        this.getFieldMetadata(field),
        this.getFilteredData(otherFilters)
      ]).then(function([ fieldMetadata, filteredData ]) {
        var filteredMetadata = filteredData
          .reduce(function(acc, fd) {
            acc[fd.term] = fieldMetadata[fd.term];
            return acc;
          }, {});
        var counts = countByValues(fieldMetadata);
        var filteredCounts = countByValues(filteredMetadata);
        return uniqMetadataValues(fieldMetadata).map(value => {
          return {
            value,
            count: counts[value],
            filteredCount: filteredCounts[value] || 0
          };
        });

      }.bind(this));
    },

    getFieldMetadata: function(field) {
      return new Promise(function(resolve, reject) {
        var term = field.term;
        var type = field.type;

        // if it's cached, return a promise that resolves immediately
        if (this.metadata[term]) {
          resolve(this.metadata[term]);
          return;
        }

        // TODO Should this be configurable?
        var metadataUrl = wdk.webappUrl('getMetadata.do');
        var metadataUrlParams = {
          questionFullName: this.questionName,
          dependedValue: JSON.stringify(this.dependedValue),
          name: this.name,
          json: true,
          property: term
        };

        var xhr = $.getJSON(metadataUrl, metadataUrlParams);
        this.metadataXhrQueue.set(term, xhr);

        xhr
          .then(function(fieldMetadata) {
            // Cache fieldMetadata and transform to a dict. Also parse values
            // into primitives (String, Number, Date, etc.).
            // Each key is the sample term, and each value is an array of values
            // for the given term.
            fieldMetadata = _.indexBy(fieldMetadata, 'sample');
            this.metadata[term] = this.data.reduce(function(parsedMetadata, d) {
              // TODO Add formatting for date type
              var values = _.result(fieldMetadata[d.term], 'values');
              parsedMetadata[d.term] = _.isUndefined(values) ? [ Field.UNKNOWN_VALUE ]
                                     : type === 'number' ? values.map(Number)
                                     : values.map(String);
              return parsedMetadata;
            }, {});
            resolve(this.metadata[term]);
          }.bind(this))
          .fail(function(err) {
            if (err.statusText !== 'abort') {
              // TODO Show user an error message
              reject(err);
            }
          })
          .always(function() {
            this.metadataXhrQueue.delete(term);
          }.bind(this));
      }.bind(this));
    },

    getFilteredData: function(filters) {
        return Promise.all(_.map(filters, function(filter) {
          return this.getFieldMetadata(filter.field);
        }, this)).then(function() {

          // Map filters to a list of predicate functions to call on each data item
          var predicates = filters
            .map(function(filter) {
              var metadata = this.metadata[filter.field.term];
              if (filter.field.type == 'string') {
                return getMemberPredicate(metadata, filter);
              } else if (filter.field.type == 'number' || filter.field.type == 'date') {
                return getRangePredicate(metadata, filter);
              }
            }, this);
          // Filter data by applying each predicate above to each data item.
          // If predicates is empty (i.e., no filters), all data is returned.
          var filteredData = _.filter(this.data, combinePredicates(predicates));
          return filteredData;
        }.bind(this));
    }

  });

});
