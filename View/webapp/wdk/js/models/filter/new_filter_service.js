/* global _, wdk */
import EventEmitter from 'events';
import {makeTree} from './utils';

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

  var CHANGE_EVENT = 'change';

  /** Abstract service class */
  class FilterService {

    constructor(attrs) {
      this._emitter = new EventEmitter();

      // Partition options.filters into two arrays:
      //   * valid
      //   * invalid
      var filterPartitions = _.reduce(attrs.filters, function(partitions, filter) {
        if (_.any(attrs.fields, function(field) {
          return filter.field.term === field.term;
        })) {
          partitions.valid.push(filter);
        }
        else {
          partitions.invalid.push(filter);
        }
        return partitions;
      }, { valid: [], invalid: [] });

      // loading status for async operations
      this.isLoading = false;

      // metadata properties
      this.fields = makeTree(attrs.fields || [], {});

      // list of filters
      this.filters = filterPartitions.valid || [];

      // list of invalid filters
      this.invalidFilters = filterPartitions.invalid;

      // unfiltered data, used for local filtering
      this.data = attrs.data || [];

      // filtered data
      this.filteredData = attrs.filteredData || [];

      // ignored data
      this.ignoredData = attrs.ignoredData || [];

      // visible columns in results
      this.columns = attrs.columns || [];

      // field distributions keyed by field term
      this.distributionMap = {};

      // map of metadata for each field
      this.fieldMetadataMap = attrs.fieldMetadataMap || {};

      // selected field
      if (attrs.selectedField) {
        this.selectField(attrs.selectedField);
      }

      // apply filters
      this.getFilteredData(this.filters)
        .then(function(filteredData) {
          this.filteredData = filteredData;
          this._emitChange();
        }.bind(this));

      // bind all methods
      _.bindAll(this);
    }

    addListener(listener) {
      this._emitter.on(CHANGE_EVENT, listener);
      return {
        remove: () => {
          this._emitter.removeListener(CHANGE_EVENT, listener);
        }
      };
    }

    getState() {
      return _.pick(
        this,
        'fields',
        'filters',
        'data',
        'filteredData',
        'ignoredData',
        'columns',
        'selectedField',
        'isLoading',
        'invalidFilters',
        'distributionMap',
        'fieldMetadataMap'
      );
    }

    selectField(field) {
      this.isLoading = true;
      this.selectedField = field;
      this._emitChange();

      this.getFieldDistribution(field)
        .then(function(distribution) {
          this.distributionMap[field.term] = distribution;
          // this.selectedField = field;
          this.isLoading = false;
          this._emitChange();
        }.bind(this));
    }

    updateFilters(filters) {
      this.filters = filters;
      this.applyFilters();
    }

    // Filter is optional. If supplied, calculate it's selection.
    // If @selectedField is undefined, skip updating @distributionMap.
    applyFilters() {
      this.isLoading = true;
      this._emitChange();

      let filter = this.filters.find(filter => filter.field.term === this.selectedField.term);
      var promises = [
        this.getFilteredData(this.filters),
        this.selectedField && this.getFieldDistribution(this.selectedField),
        filter && this.getFilteredData([filter])
      ];

      Promise.all(promises).then(function([ filteredData, distribution, filterSelection ]) {
        if (distribution) {
          this.distributionMap[this.selectedField.term] = distribution;
        }
        if (filter) {
          filter.selection = filterSelection;
        }
        this.filteredData = filteredData;
        this.isLoading = false;
        this._emitChange();
      }.bind(this));
    }

    updateColumns(fields) {
      this.isLoading = true;
      this._emitChange();

      Promise.all(fields.map(field => this.getFieldMetadata(field)))
        .then(() => {
          this.columns = fields;
          this.isLoading = false;
          this._emitChange();
        });
    }

    updateIgnoredData(data) {
      this.ignoredData = data;
      this._emitChange();
    }

    // Methods to override
    // ------------------

    // Returns a Promise-like that resolves with the field's distribution.
    //
    //     [ { value, count, filteredCount } ]
    //
    getFieldDistribution(field) {
      throw new Error('getFieldDistribution() should be implemented ' + field);
    }

    // Returns a Promise-like that resolves with the filtered data.
    //
    //     [ { term, display } ]
    //
    getFilteredData(filters) {
      throw new Error('getFilteredData() should be implemented ' + filters);
    }

    // Returns a Promise-like that resolves with the field's metadata:
    //
    //     [ { data_term: field_value } ]
    //
    getFieldMetadata(field) {
      throw new Error('getFieldMetadata() should be implemented ' + field);
    }

    _emitChange() {
      this._emitter.emit(CHANGE_EVENT);
    }

  }

  ns.FilterService = FilterService;
});
