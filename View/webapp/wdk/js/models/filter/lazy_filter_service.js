wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var FilterService = wdk.models.filter.FilterService;

  // _.flow is a higher-order function that returns a function F composed of the
  // supplied functions. Each provided function is invoked from left to right.
  // The left-most function is called with the argument that F is called with.
  // The next function is called, with the argument returned by the previous
  // function, and so on. The return value of the last function is the return
  // value of F.

  /**
   * Used by lodash sortBy. Returns a value that sortBy will use to
   * compare with other values in an array.
   *
   * FIXME Use natural sort
   *
   * @param {any} value
   */
  function valueSorter(value) {
    return typeof value === 'number' ? Number(value)
         : value === 'Unknown' ? String.fromCharCode(Math.pow(2, 16) - 1)
         : String(value);
  }

  var flattenMetadataValues = _.flow(
    metadata => Object.keys(metadata).map(key => metadata[key]),
    nestedValues => nestedValues.reduce((a, b) => a.concat(b), []),
    values => _.sortBy(values, valueSorter)
  );

  /**
   * Calculate the occurence of each value present in metadata.
   *
   * @param {object} metadata A key-value map of { sample: [ { value } ] }
   * @returns {object} A key-value map of { value: count }
   */
  var countByValues = _.flow(
    flattenMetadataValues,
    values => values.reduce((counts, value) => {
      counts.hasOwnProperty(value) ? counts[value]++ : counts[value] = 1;
      return counts;
    }, {})
  );

  var uniqMetadataValues = _.flow(
    flattenMetadataValues,
    values => _.uniq(values)
  );

  // Helper filtering functions
  // --------------------------

  var gte = _.curry(function gte(min, value) {
    return value >= min;
  });

  var lte = _.curry(function lte(max, value) {
    return value <= max;
  });

  var within = _.curry(function within(min, max, value) {
    return gte(min, value) && lte(max, value);
  });

  var passes = _.curry(function passes(value, fn) {
    return fn(value);
  });

  var passesAll = _.curry(function passesAll(fns, value) {
    var passesWithValue = passes(value);
    return _.every(fns, passesWithValue);
  });


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
              // TODO Use a Symbol for Unknown
              parsedMetadata[d.term] = _.isUndefined(values) ? [ 'Unknown' ]
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
              if (filter.field.type == 'string') {
                return this.getMemberPredicate(filter);
              } else if (filter.field.type == 'number' || filter.field.type == 'date') {
                return this.getRangePredicate(filter);
              }
            }, this);
          // Filter data by applying each predicate above to each data item.
          // If predicates is empty (i.e., no filters), all data is returned.
          var filteredData = _.filter(this.data, passesAll(predicates));

          // We used to only return data when filters were defined
          // return filters.length === 0
          //   ? []
          //   : _.filter(this.get('data'), passesAll(predicates));
          return filteredData;
        }.bind(this));
    },

    // returns a function to apply to data item
    // fn(data) // (true | false)
    getMemberPredicate: function(filter) {
      var field = filter.field;
      var metadata = this.metadata[field.term];

      return function memberPredicate(datum) {
        var filterValues = filter.values;
        var metadataValues = metadata[datum.term];
        var index = filterValues.length;
        var vIndex;

        // Use a for loop for efficiency
        outer:
        while(index--) {
          vIndex = metadataValues.length;
          while(vIndex--) {
            if (filterValues[index] === metadataValues[vIndex]) break outer;
          }
        }

        return (index > -1);
      };
    },

    getRangePredicate: function(filter) {
      var field = filter.field;
      var metadata = this.metadata[field.term];
      var min = filter.values.min;
      var max = filter.values.max;

      if (min !== null && max !== null) {
        return function rangePredicate(datum) {
          var values = metadata[datum.term];
          return values.some(within(min, max));
        };
      }
      if (min !== null) {
        return function rangePredicate(datum) {
          var values = metadata[datum.term];
          return values.some(gte(min));
        };
      }
      if (max !== null) {
        return function rangePredicate(datum) {
          var values = metadata[datum.term];
          return values.some(lte(max));
        };
      }
      throw new Error('Could not determine range predicate.');
    }

  });
});
