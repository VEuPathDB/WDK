/*global RSVP*/
wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var MemberFilter = wdk.models.filter.MemberFilter;
  var RangeFilter = wdk.models.filter.RangeFilter;

  var Field = wdk.models.filter.Field;
  var Filters = wdk.models.filter.Filters;

  /**
   * Base class for FilterService classes.
   *
   * A subclass should only need to implement a getFilteredData() method.
   * This method's job is to apply added filters to the data. This may be done
   * locally using JavaScript functions, or it may use a remote service to
   * retrieve filtered results.
   *
   */
  var FilterService = Backbone.Model.extend({
    defaults: function() {
      return {
        data: [],
        filteredData: [],
        metadata: []
      };
    },

    initialize: function() {
      var debouncedApplyFilters = _.debounce(this.applyFilters.bind(this), 100);

      // track which filters are being applied
      this.filterChangeSet = [];
      this.filters = new Filters();
      this.listenTo(this.filters, 'add remove', function(m) { this.filterChangeSet.push(m); });
      this.listenTo(this.filters, 'add remove', debouncedApplyFilters);
      debouncedApplyFilters();
    },

    applyFilters: function() {
      this.getFilteredData({ filters: this.filters })
        .then(function(data) {
          this.set('filteredData', data, {
            filterChangeSet: this.filterChangeSet
          });
          this.filterChangeSet = [];
        }.bind(this));
      return this;
    }
  });

  // Helpers
  // -------

  var countByValues = _.compose(_.countBy, _.flatten, _.values);
  var numericSort = function(a, b){ return a > b; };
  var stringSort; // passing this to [].sort() will use the default sort

  // wrap return value in a Promise
  var toPromise = function(fn) {
    return function() {
      var args = arguments;
      return new Promise(function(resolve, reject) {
        try { resolve(fn.apply(this, args)); }
        catch(e) { reject(e); }
      }.bind(this));
    };
  };

  function gte(min, value) {
    return value >= min;
  }

  function lte(max, value) {
    return value <= max;
  }

  function within(min, max, value) {
    return gte(min, value) && lte(max, value);
  }

  var passes = _.curry(function passes(value, fn) {
    return fn(value);
  });

  var passesAll = _.curry(function passesAll(fns, value) {
    var passesWithValue = passes(value);
    return _.every(fns, passesWithValue);
  });

  var LocalFilterService = FilterService.extend({

    initialize: function() {
      this._metadata = {};
      this._metadataXhrQueue = {};
      FilterService.prototype.initialize.apply(this, arguments);
    },

    // Fetches fields and calls callback with fields array
    getFields: function(callback) {
    },

    // Returns a promise that resolves with an object:
    //
    //     { field_term: metadata }
    //
    // This method is used for filtering and displaying results,
    // and it provides a way to lazy-load data.
    getFieldMetadata: function(fields) {
      var promises = _.reduce(fields, function(acc, field) {
        var term = field.get('term');
        acc[term] = this._getMetadata(field);
        return acc;
      }, {}, this);
      return RSVP.hash(promises);
    },

    // returns a promse which resolves to an array of objects:
    // { value, count, filteredCount }
    getFieldDistribution: function(field) {
      var term = field.get('term');
      var type = field.get('type');

      // Retrieve metadata and filtered data and return a promise
      return RSVP.hash({
        metadata: this._getMetadata(field),
        filteredData: this.getFilteredData({
          filters: this.filters,
          omit: [ term ]
        })
      }).then(function(results) {
        var filteredMetadata = results.filteredData
          .reduce(function(acc, fd) {
            acc[fd.term] = results.metadata[fd.term];
            return acc;
          }, {});
        var counts = countByValues(results.metadata);
        var filteredCounts = countByValues(filteredMetadata);

        return _.uniq(_.flatten(_.values(results.metadata)))
          .sort(type == 'number' ? numericSort : stringSort)
          .map(function(value) {
            return {
              value: value,
              count: Number(counts[value]),
              filteredCount: filteredCounts[value]
            };
          });
      }.bind(this));
    },

    // returns a promise that resolves to metadata for a property:
    //     [ { data_term: metadata_value }, ... ]
    _getMetadata: function(field) {
      return new Promise(function(resolve, reject) {
        var term = field.get('term');
        var type = field.get('type');

        // if it's cached, return a promise that resolves immediately
        if (this._metadata[term]) {
          resolve(this._metadata[term]);
          return;
        }

        var metadataUrl = wdk.webappUrl('getMetadata.do');
        var metadataUrlParams = {
          questionFullName: this.questionName,
          name: this.name,
          json: true,
          property: term
        };

        this.showSpinner();
        (this._metadataXhrQueue[term] = $.getJSON(metadataUrl, metadataUrlParams))
          .then(function(metadata) {
            metadata = _.indexBy(metadata, 'sample');
            // cache metadata and transform to a dict
            this._metadata[term] = this.data
              .reduce(function(acc, d) {
                var values = _.result(metadata[d.term], 'values');
                acc[d.term] = _.isUndefined(values)
                  ? [ Field.UNKNOWN_VALUE ]
                  : type == 'number' ? values.map(Number) : values;
                return acc;
              }, {});
            resolve(this._metadata[term]);
          }.bind(this))
          .fail(function(err) {
            // TODO Show user an error message
            reject(err);
          })
          .always(function() {
            delete this._metadataXhrQueue[term];
            if (_.size(this._metadataXhrQueue) === 0) {
              this.hideSpinner();
            }
          }.bind(this));
      }.bind(this));
    },

    /**
     * Applies filters and returns a promise which resolves
     * with a filtered copy of the data.
     *
     * Optionally, provide options to affect the filtering algorithm.
     * Available options are:
     *   * filters: A Backbone Collection of filters to apply.
     *   * omit: A list of fields to omit. This is useful when you want
     *           a distribution of values caused by other filters.
     *
     * @param {Object} options Options for the filtering algorithm
     */
    getFilteredData: toPromise(function(options) {

      // TODO Create composite filter function to apply once

      var filters = options.filters;

      if (options && options.omit) {
        filters = filters.reject(function(filter) {
          return _.contains(options.omit, filter.get('field'));
        });
      }

      // Map filters to a list of predicate functions to call on each data item
      var predicates = filters
        .map(function(filter) {
          if (filter instanceof MemberFilter) {
            return this.getMemberPredicate(filter);
          } else if (filter instanceof RangeFilter) {
            return this.getRangePredicate(filter);
          }
        }, this);

      // Filter data by applying each predicate above to each data item.
      // return filters.length === 0
      //   ? []
      //   : _.filter(this.get('data'), passesAll(predicates));
      return _.filter(this.get('data'), passesAll(predicates));

      // if (filters.length) {
      //   data = filters.reduce(function(data, filter) {
      //     if (filter instanceof MemberFilter) {
      //       return this.applyMemberFilter(filter, data);
      //     } else if (filter instanceof RangeFilter) {
      //       return this.applyRangeFilter(filter, data);
      //     }
      //   }.bind(this), this.get('data'));
      // }
      // return data || [];
    }),

    // returns a function to apply to data item
    // fn(data) // (true | false)
    getMemberPredicate: function(filter) {
      var field = filter.get('field');
      var metadata = this.get('metadata')[field];

      return function memberPredicate(datum) {
        var filterValues = filter.get('values');
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
      var field = filter.get('field');
      var metadata = this.get('metadata')[field];
      var min = filter.get('min');
      var max = filter.get('max');

      if (min !== null && max !== null) {
        return function rangePredicate(datum) {
          return within(min, max, metadata[datum.term]);
        };
      }
      if (min !== null) {
        return function rangePredicate(datum) {
          return gte(min, metadata[datum.term]);
        };
      }
      if (max !== null) {
        return function rangePredicate(datum) {
          return lte(max, metadata[datum.term]);
        };
      }
      throw new Error('Could not determine range predicate.');
    },

    applyMemberFilter: function(filter, data) {
      var field = filter.get('field');
      var filterValues = filter.get('values');
      var metadata = this.get('metadata')[field];

      var ret = data.filter(function(datum) {
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

        return index > -1;
      });
      return ret;
    },

    applyRangeFilter: function(filter, data) {
      var field = filter.get('field');
      var metadata = this.get('metadata')[field];
      var min = filter.get('min');
      var max = filter.get('max');
      var test;

      var gteTest = _.partial(gte, min);
      var lteTest = _.partial(lte, max);
      var withinTest = _.partial(within, min, max);

      if (min !== null && max !== null) {
        test = function(datum) {
          return _.some(metadata[datum.term], withinTest);
        };
      } else if (min !== null) {
        test = function(datum) {
          return _.some(metadata[datum.term], gteTest);
        };
      } else if (max !== null) {
        test = function(datum) {
          return _.some(metadata[datum.term], lteTest);
        };
      }

      return data.filter(test);
    }

  });

  ns.FilterService = FilterService;
  ns.LocalFilterService = LocalFilterService;

});
