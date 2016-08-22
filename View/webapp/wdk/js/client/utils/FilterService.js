import _ from 'lodash';
import EventEmitter from 'events';
import {makeTree} from './FilterServiceUtils';

// TODO Integrate with Flux architecture
//
// The likely way to integrate this class will be to define action creators
// that dispatch filterParam change actions, and to make this class a stateless
// service class (possibly with some configuration options passed to the
// constructor). ViewStores would handle handle the dispatched actions.

const CHANGE_EVENT = 'change';

/** Abstract service class */
export default class FilterService {

  constructor(attrs) {
    this._emitter = new EventEmitter();

    // loading status for async operations
    this.isLoading = false;

    // list of filters
    this.filters = [];

    // ignored data
    this.ignoredData = [];

    // metadata properties
    this.fields = makeTree(attrs.fields || [], {});

    // unfiltered data, used for local filtering
    this.data = attrs.data || [];

    // filtered data
    this.filteredData = this.data;

    // visible columns in results
    this.columns = attrs.columns || [];

    // field distributions keyed by field term
    this.distributionMap = {};

    // map of metadata for each field
    this.fieldMetadataMap = attrs.fieldMetadataMap || {};

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
    this.isLoading = true;
    this._emitChange();

    let filter = this.selectedField && this.filters.find(filter => filter.field.term === this.selectedField.term);
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
