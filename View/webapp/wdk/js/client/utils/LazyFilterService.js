import $ from 'jquery';
import _ from 'lodash';
import FilterService from './FilterService';
import {
  countByValues,
  uniqMetadataValues,
  combinePredicates,
  getMemberPredicate,
  getRangePredicate
} from './FilterServiceUtils';


export default class LazyFilterService extends FilterService {

  constructor(attrs) {
    if (!attrs.name) {
      throw new Error('LazyFilterService requires a "name" attribute.');
    }

    if (!attrs.questionName) {
      throw new Error('LazyFilterService requires a "questionName" attribute.');
    }

    if (!attrs.metadataUrl) {
      throw new Error('LazyFilterService requires a "metadataUrl" attribute.');
    }

    super(attrs);

    this.name = attrs.name;
    this.questionName = attrs.questionName;
    this.dependedValue = attrs.dependedValue;
    this.metadataUrl = attrs.metadataUrl;
    this.metadataXhrQueue = new Map();
  }

  selectField(field) {
    this.cancelXhr(this._pendingSelectField);
    this._pendingSelectField = field;
    super.selectField(field);
  }

  updateColumns(fields) {
    fields.forEach(field => this.cancelXhr(field));
    super.updateColumns(fields);
  }

  cancelXhr(field) {
    if (field) {
      return _.result(this.metadataXhrQueue.get(field.term), 'abort');
    }
  }

  getFieldDistribution(field) {
    var term = field.term;
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
      var undefinedCount = _.values(fieldMetadata).filter(_.isEmpty).length;
      let distribution = uniqMetadataValues(fieldMetadata).map(value => {
        return {
          value,
          count: counts[value],
          filteredCount: filteredCounts[value] || 0
        };
      });
      return undefinedCount > 0
        ? distribution.concat({
            value: null,
            count: _.values(fieldMetadata).filter(_.isEmpty).length,
            filteredCount: _.values(filteredMetadata).filter(_.isEmpty).length
          })
        : distribution;
    }.bind(this));
  }

  getFieldMetadata(field) {
    return new Promise(function(resolve, reject) {
      var term = field.term;
      var type = field.type;

      // if it's cached, return a promise that resolves immediately
      if (this.fieldMetadataMap[term]) {
        resolve(this.fieldMetadataMap[term]);
        return;
      }

      // TODO Should this be configurable?
      var metadataUrlParams = {
        questionFullName: this.questionName,
        dependedValue: JSON.stringify(this.dependedValue),
        name: this.name,
        json: true,
        property: term
      };

      var xhr = $.getJSON(this.metadataUrl, metadataUrlParams);
      this.metadataXhrQueue.set(term, xhr);

      xhr
        .then(function(fieldMetadata) {
          // Cache fieldMetadata and transform to a dict. Also parse values
          // into primitives (String, Number, Date, etc.).
          // Each key is the sample term, and each value is an array of values
          // for the given term.
          fieldMetadata = _.indexBy(fieldMetadata, 'sample');
          this.fieldMetadataMap[term] = this.data.reduce(function(parsedMetadata, d) {
            // TODO Add formatting for date type
            var values = _.result(fieldMetadata[d.term], 'values');
            parsedMetadata[d.term] = _.isUndefined(values) ? []
                                   : type === 'number' ? values.map(Number)
                                   : values.map(String);
            return parsedMetadata;
          }, {});
          resolve(this.fieldMetadataMap[term]);
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
  }

  getFilteredData(filters) {
      return Promise.all(_.map(filters, function(filter) {
        return this.getFieldMetadata(filter.field);
      }, this)).then(function() {

        // Map filters to a list of predicate functions to call on each data item
        var predicates = filters
          .map(function(filter) {
            var metadata = this.fieldMetadataMap[filter.field.term];
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

}
