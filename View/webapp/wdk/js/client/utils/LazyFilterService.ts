import $ from 'jquery';
import _ from 'lodash';
import FilterService from './FilterService';
import {
  FilterServiceAttrs,
  Field,
  Metadata,
  Filter,
  RangeFilter,
  MemberFilter,
  Datum,
  Distribution
} from './FilterService';
import {
  countByValues,
  uniqMetadataValues,
  combinePredicates,
  getMemberPredicate,
  getDateRangePredicate,
  getNumberRangePredicate
} from './FilterServiceUtils';


interface LazyFilterServiceAttrs extends FilterServiceAttrs {
  name: string;
  questionName: string;
  metadataUrl: string;
  dependedValue: string;
}

export default class LazyFilterService extends FilterService {

  name: string;
  questionName: string;
  metadataUrl: string;
  dependedValue: string;
  metadataXhrQueue: Map<string, { abort: Function }>;
  private _pendingSelectField: string;

  constructor(attrs: LazyFilterServiceAttrs) {
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

  selectField(field: string) {
    this.cancelXhr(this._pendingSelectField);
    this._pendingSelectField = field;
    super.selectField(field);
  }

  updateColumns(fields: string[]) {
    fields.forEach(field => this.cancelXhr(field));
    super.updateColumns(fields);
  }

  cancelXhr(field: string) {
    if (field) {
      _.result(this.metadataXhrQueue.get(field), 'abort');
    }
  }

  getFieldDistribution(field: string): Promise<Distribution>  {
    var term = field;
    var otherFilters =_.reject(this.filters, function(filter) {
      return filter.field === term;
    });

    // Retrieve metadata and filtered data and return a promise
    return Promise.all([
      this.getFieldMetadata(field),
      this.getFilteredData(otherFilters)
    ]).then(([ fieldMetadata, filteredData ]: [ Metadata, Datum[] ]) => {
      var filteredMetadata = filteredData.reduce(function(acc, fd) {
        acc[fd.term] = fieldMetadata[fd.term];
        return acc;
      }, <Metadata>{});
      var counts = countByValues(fieldMetadata);
      var filteredCounts = countByValues(filteredMetadata);
      var undefinedCount = _.values(fieldMetadata).filter(_.isEmpty).length;
      let distribution: Distribution = uniqMetadataValues(fieldMetadata).map((value) => {
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
    });
  }

  getFieldMetadata(field: string): Promise<Metadata> {
    return new Promise((resolve, reject) => {
      var term = field;

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
        .then((fieldMetadata) => {
          // Cache fieldMetadata and transform to a dict.
          // Each key is the sample term, and each value is an array of values
          // for the given term. If a term does not have values associated with
          // it, an empty array is used.
          fieldMetadata = _.keyBy(fieldMetadata, 'sample');
          this.fieldMetadataMap[term] = this.data.reduce((parsedMetadata, d) =>
            Object.assign(parsedMetadata, {
              [d.term]: _.get(fieldMetadata, [ d.term, 'values' ], [])
            }), {} as Metadata);
          resolve(this.fieldMetadataMap[term]);
        })
        .fail(function(err) {
          if (err.statusText !== 'abort') {
            // TODO Show user an error message
            reject(err);
          }
        })
        .always(() => {
          this.metadataXhrQueue.delete(term);
        });
    });
  }

  getFilteredData(filters: Filter[]): Promise<Datum[]> {
      return Promise.all(
        _.map(filters, filter => this.getFieldMetadata(filter.field))
      ).then(() => {

        // Map filters to a list of predicate functions to call on each data item
        var predicates = filters
          .map(function(filter) {
            var metadata = this.fieldMetadataMap[filter.field];
            var { type } = this.fields[filter.field];
            switch(type) {
              case 'string': return getMemberPredicate(metadata, <MemberFilter>filter);
              case 'date':   return getDateRangePredicate(metadata, <RangeFilter>filter);
              case 'number': return getNumberRangePredicate(metadata, <RangeFilter>filter);
              default: throw new Error("Unknown filter field type: `" + type + "`.");
            }
          }, this);
        // Filter data by applying each predicate above to each data item.
        // If predicates is empty (i.e., no filters), all data is returned.
        return _.filter(this.data, combinePredicates(predicates)) as Datum[];
      });
  }

}
