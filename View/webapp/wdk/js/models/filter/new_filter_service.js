/* global _, Backbone, RSVP */

// TODO How does this class relate to Flux?
//
// All of the implementation here should actually be in the "Store" (for the
// immediate term, this would be the filterParam controller). The methods
// implemented in LazyFilterService should probably be util functions.
//
// ---------
// OLD NOTES
// ---------
//
// In Flux, this class would be broken into two pieces:
//
//   1. Store
//   2. ActionCreators
//
// The Store would be usable by multiple ActionCreators. It would simply
// provide the data model to the View layer via getters.
//
// The ActionCreators would contain much of the logic here. It isn't clear to
// me how ActionCreators will access the data for filtering. I suppose the
// ActionCreators will contain its own reference to the data (e.g., for local
// filtering; for remote filtering, things are more clear). Or, to put it in
// more favorable terms, the ActionCreator can cache the AJAX request for the
// data.
//
// Alternatively, we can have a LocalFilterStore and a RemoteFilterStore. The
// ActionCreators don't do much more than dispatch messages, and the Stores are
// responsible for determining how to filter data. (See
// https://groups.google.com/forum/#!topic/reactjs/jBPHH4Q-8Sc). In this way,
// we can view filter requests as GET requests (get the data that matches this
// criteria--it's essentially a query).
//
// One problem with this approach is if another Store wants to handle an action
// based on the contents of this Store after said action, it won't be easy. I
// don't think this will come up in this case, since the View container will
// pass an onChange callback to the AttributeFilter View component (maybe this
// is wrong...?).
wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var BaseClass = wdk.core.BaseClass;

  var CHANGE_EVENT = 'change';

  ns.FilterService = BaseClass.extend(Backbone.Events, {

    // user actions to react to
    actionListeners: {
      selectField: function(field) {
        this.setSelectedField(field);
      },

      addFilter: function(field, values) {
        this.addFilter(field, values);
      },

      removeFilter: function(filter) {
        this.removeFilter(filter);
      },

      addIgnored: function(datum) {
        this.addIgnored(datum);
      },

      removeIgnored: function(datum) {
        this.removeIgnored(datum);
      },

      updateColumns: function(fields) {
        this.updateColumns(fields);
      }
    },

    constructor: function(attrs, options) {

      // loading status for async operations
      this.isLoading = false;

      // metadata properties
      this.fields = attrs.fields || [];

      // list of filters
      this.filters = attrs.filters || [];

      // unfiltered data, used for local filtering
      this.data = attrs.data || [];

      // filtered data
      this.filteredData = attrs.filteredData || [];

      // visible columns in results
      this.columns = attrs.columns || [];

      // ignored data
      this.ignored = attrs.ignored || [];

      // selected field
      this.selectedField =  null;

      // field distributions keyed by field term
      this.distributionMap = {};

      // listen to actions
      this.listenTo(options.intents, this.actionListeners);

      // apply filters
      this.getFilteredData(this.filters)
        .then(function(filteredData) {
          this.filteredData = filteredData;
          this.emitChange();
        }.bind(this));
    },

    emitChange: function() {
      this.trigger(CHANGE_EVENT);
    },

    getState: function() {
      return _.pick(this, 'isLoading', 'fields', 'filters', 'data', 'filteredData', 'columns', 'ignored', 'selectedField', 'distributionMap');
    },

    setSelectedField: function(field) {
      this.isLoading = true;
      this.emitChange();

      this.getFieldDistribution(field)
        .then(function(distribution) {
          this.distributionMap[field.term] = distribution;
          this.selectedField = field;
          this.isLoading = false;
          this.emitChange();
        }.bind(this));
    },

    // Only one filter per field
    addFilter: function(field, values) {
      var filter = _.find(this.filters, function(filter) {
        return filter.field.term === field.term;
      });

      if (!filter) {
        filter = { field: field, values: [] };
        this.filters.push(filter);
      }

      if (field.type === 'string') {
        this.addStringFilter(filter, values);
      }
      else if (field.type === 'number') {
        this.addNumberFilter(filter, values);
      }
    },

    addStringFilter: function(filter, values) {
      var field = filter.field;
      var allValues = _.pluck(this.distributionMap[field.term], 'value');
      if (_.difference(allValues, values).length === 0) {
        // remove filter if all values are selected
        this.removeFilter(filter);
        return;
      }

      filter.values = values;
      filter.display = filter.values.length
        ? filter.field.display + ' is ' + filter.values.join(', ')
        : 'No ' + filter.field.display + ' selected';
      this.updateFilters(filter);
    },

    addNumberFilter: function(filter, values) {
      if (values.min === null && values.max === null) {
        this.removeFilter(filter);
        return;
      }

      filter.values = values;
      filter.display = filter.field.display + ' between ' + filter.values.min + ' and ' + filter.values.max;
      this.updateFilters(filter);
    },

    removeFilter: function(filter) {
      _.pull(this.filters, filter);
      this.updateFilters();
    },

    // Filter is optional. If supplied, calculate it's selection.
    // If @selectedField is undefined, skip updating @distributionMap.
    updateFilters: function(filter) {
      this.isLoading = true;
      this.emitChange();

      var promises = {
        filteredData: this.getFilteredData(this.filters),
        distribution: this.selectedField &&
          this.getFieldDistribution(this.selectedField),
        filterSelection: filter && this.getFilteredData([filter])
      };

      RSVP.hash(promises).then(function(results) {
        if (results.distribution) {
          this.distributionMap[this.selectedField.term] = results.distribution;
        }
        if (filter) {
          filter.selection = results.filterSelection;
        }
        this.filteredData = results.filteredData;
        this.isLoading = false;
        this.emitChange();
      }.bind(this));
    },

    updateColumns: function(fields) {
      this.isLoading = true;
      this.emitChange();

      Promise.all(fields.map(field => this.getFieldMetadata(field)))
        .then(() => {
          this.columns = fields;
          this.isLoading = false;
          this.emitChange();
        });
    },

    addIgnored: function(datum) {
      // this.ignored = _.uniq(this.ignored.concat(datum.term));
      datum.isIgnored = true;
      cloneDatum(this.data, datum);
      cloneDatum(this.filteredData, datum);
      this.emitChange();
    },

    removeIgnored: function(datum) {
      // this.ignored = this.ignored.filter(t => t != datum.term);
      datum.isIgnored = false;
      cloneDatum(this.data, datum);
      cloneDatum(this.filteredData, datum);
      this.emitChange();
    },


    // Methods to override
    // ------------------

    // Returns a Promise-like that resolves with the field's distribution.
    //
    //     [ { value, count, filteredCount } ]
    //
    getFieldDistribution: function(field) {
      throw new Error('getFieldDistribution() should be implemented ' + field);
    },

    // Returns a Promise-like that resolves with the filtered data.
    //
    //     [ { term, display } ]
    //
    getFilteredData: function(filters) {
      throw new Error('getFilteredData() should be implemented ' + filters);
    },

    // Returns a Promise-like that resolves with the field's metadata:
    //
    //     [ { data_term: field_value } ]
    //
    getFieldMetadata: function(field) {
      throw new Error('getFieldMetadata() should be implemented ' + field);
    }

  });
});

function cloneDatum(data, datum) {
  for (var index = 0; index < data.length; index++) {
    if (data[index] == datum) {
      data[index] = _.cloneDeep(datum);
      break;
    }
  }
}
