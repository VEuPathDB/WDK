import _ from 'lodash';

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

function flattenMetadataValues(metadata) {
  return _(metadata)
  .values()
  .flatten();
}

/**
 * Calculate the occurence of each value present in metadata.
 *
 * @param {object} metadata A key-value map of { sample: [ { value } ] }
 * @returns {object} A key-value map of { value: count }
 */
export function countByValues(metadata) {
  return flattenMetadataValues(metadata)
  .countBy()
  .value();
}

export function uniqMetadataValues(metadata) {
  return flattenMetadataValues(metadata)
  .sortBy(valueSorter)
  .uniq(true)
  .value();
}

export function getMemberPredicate(metadata, filter) {
  var field = filter.field;

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
}

export function getRangePredicate(metadata, filter) {
  var field = filter.field;
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

// Helper filtering functions
// --------------------------

export var gte = _.curry(function gte(min, value) {
  return value >= min;
});

export var lte = _.curry(function lte(max, value) {
  return value <= max;
});

export var within = _.curry(function within(min, max, value) {
  return gte(min, value) && lte(max, value);
});

export var passes = _.curry(function passes(value, fn) {
  return fn(value);
});

export var passesAll = _.curry(function passesAll(fns, value) {
  var passesWithValue = passes(value);
  return _.every(fns, passesWithValue);
});

