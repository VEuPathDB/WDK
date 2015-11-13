import _ from 'lodash';


/**
 * Returns a lodash-wrapped array of metadata values.
 * See https://lodash.com/docs/#_ for details of lodash-wrapped objectd.
 *
 * There are two main benefits to the lodash wrapper:
 *   1. Operations can be expressed fluently as method calls.
 *   2. lodash will merge operations to use as few overall iterations as
 *      possible. It will also reduce the number of intermediate objects
 *      created on each iteration, thus reducing GC pressure.
 */
function flattenMetadataValues(metadata) {
  return _(metadata).values().flatten();
}

/**
 * Calculate the occurence of each value present in metadata.
 *
 * @param {object} metadata A key-value map of { sample: [ { value } ] }
 * @returns {object} A key-value map of { value: count }
 */
export function countByValues(metadata) {
  return flattenMetadataValues(metadata).countBy().value();
}

/**
 * Create a array of uniq metadata values
 */
export function uniqMetadataValues(metadata) {
  return flattenMetadataValues(metadata).sortBy().uniq(true).value();
}

export function getMemberPredicate(metadata, filter) {
  var field = filter.field;
  var filterValues = filter.values;

  return function memberPredicate(datum) {
    var metadataValues = metadata[datum.term];
    var index = filterValues.length;
    var vIndex;

    // Use a for loop for efficiency
    outer: while(index--) {
      vIndex = metadataValues.length;
      while(vIndex--) {
        if (filterValues[index] === metadataValues[vIndex]) break outer;
      }
    }

    return (index > -1);
  };
}

export function getRangePredicate(metadata, filter) {
  var field = filter.field;
  var min = filter.values.min;
  var max = filter.values.max;
  var test = min !== null && max !== null ? makeWithin(min, max)
           : min !== null ? makeGte(min)
           : max !== null ? makeLte(max)
           : undefined;

  if (test === undefined) throw new Error('Count not determine range predicate.');

  return function rangePredicate(datum) {
    return metadata[datum.term].some(test);
  }
}

// Helper filtering functions
// --------------------------

export function makeGte(min) {
  return function gte(value) {
    return value >= min;
  };
}

export function makeLte(max) {
  return function lte(value) {
    return value <= max;
  };
}

export function makeWithin(min, max) {
  let gte = makeGte(min);
  let lte = makeLte(max);
  return function within(value) {
    return gte(value) && lte(value);
  };
}

export function passesWith(value) {
  return function passes(predicate) {
    return predicate(value);
  };
}

export function combinePredicates(predicates) {
  return function predicate(value) {
    return predicates.every(passesWith(value));
  };
}
