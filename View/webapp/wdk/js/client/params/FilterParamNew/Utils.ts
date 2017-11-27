import { memoize } from 'lodash';
import natsort from 'natural-sort';

import { Filter, MemberFilter } from '../../utils/FilterService';
import { getTree } from '../../utils/FilterServiceUtils';
import { filter, Seq } from '../../utils/IterableUtils';
import { preorderSeq } from '../../utils/TreeUtils';
import { FilterParamNew } from '../../utils/WdkModel';
import { SortSpec } from './State';

const natSortComparator = (natsort as any)();

export function getLeaves(ontology: FilterParamNew['ontology']) {
  return Seq.from(preorderSeq(getTree(ontology)))
    .filter(n => n.children.length === 0);
}

export function findFirstLeaf(ontology: FilterParamNew['ontology']): string {
  return getLeaves(ontology).map(n => n.field.term).first();
}

export const getFilters = memoize(function(paramValue: string) {
  const parsedValue: { filters: Filter[] } = JSON.parse(paramValue);
  return parsedValue.filters;
})


/**
 * Compare distribution values using a natural comparison algorithm.
 * @param {string|null} valueA
 * @param {string|null} valueB
 */
function compareDistributionValues(valueA: any, valueB: any) {
  return natSortComparator(
    valueA == null ? '' : valueA,
    valueB == null ? '' : valueB
  );
}

function makeSelectionComparator(values: any) {
  let set = new Set(values);
  return function compareValuesBySelection(a: any, b: any) {
    return set.has(a.value) && !set.has(b.value) ? -1
      : set.has(b.value) && !set.has(a.value) ? 1
      : 0;
  }
}

/**
 * Sort distribution based on sort spec. `SortSpec` is an object with two
 * properties: `columnKey` (the distribution property to sort by), and
 * `direction` (one of 'asc' or 'desc').
 * @param {Distribution} distribution
 * @param {SortSpec} sort
 */
export function sortDistribution(distribution: any, sort: SortSpec, filter?: MemberFilter) {
  let { columnKey, direction, groupBySelected } = sort;

  let sortedDist = distribution.slice().sort(function compare(a: any, b: any) {
    let order =
      // if a and b are equal, fall back to comparing `value`
      columnKey === 'value' || a[columnKey] === b[columnKey]
        ? compareDistributionValues(a.value, b.value)
        : a[columnKey] > b[columnKey] ? 1 : -1;
    return direction === 'desc' ? -order : order;
  });

  return groupBySelected && filter && filter.value && filter.value.length > 0
    ? sortedDist.sort(makeSelectionComparator(filter.value))
    : sortedDist;
}

export function isMemberField(parameter: FilterParamNew, fieldName: string) {
  const field = parameter.ontology.find(field => field.term === fieldName);
  if (field == null) {
    throw new Error("Could not find a field with the term `" + fieldName + "`.");
  }
  return field.type === 'membership' || field.isRange === false;
}