import partial from 'lodash/function/partial';
import flattenDeep from 'lodash/array/flattenDeep';
import pick from 'lodash/object/pick';
import values from 'lodash/object/values';
import { filterItems } from './SearchUtils';

/**
 * Filter the results of an answer and return the filtered results.
 *
 * @param {Array<Record>} records                     The list of records to filter
 * @param {object}           filterSpec                  The filter specification
 * @param {string}           filterSpec.filterTerm       The query string with which to filter records
 * @param {Array<string>}    filterSpec.filterAttributes The attributes to search.
 * @param {Array<string>}    filterSpec.filterTables     The tables to search.
 */
export function filterRecords(records, filterSpec) {
  let { filterTerm, filterAttributes = [], filterTables = [] } = filterSpec;
  let getSearchableStringPartial = partial(getSearchableString, filterAttributes, filterTables);
  return filterItems(records, getSearchableStringPartial, filterTerm);
}

function stripHTML(str) {
  let span = document.createElement('span');
  span.innerHTML = str;
  return span.textContent;
}

// Combine appropriate fields from the record into a searchable string.
//
// The approach here is pretty basic and probably ineffecient:
//   - Convert all attribute values to an array of values.
//   - Convert all table values to a flat array of values.
//   - Combine the above arrays into a single array.
//   - Join the array with a control character.
//
// There is much room for performance tuning here.
function getSearchableString(filterAttributes, filterTables, record) {
  let attributes, tables;

  if (filterAttributes.length == 0 && filterTables.length == 0) {
    attributes = record.attributes;
    tables = record.tables;
  }
  else {
    attributes = pick(record.attributes,  filterAttributes);
    tables = pick(record.tables, filterTables);
  }

  let attributeValues = Object.keys(attributes).map(name => attributes[name]);
  let tableValues = flattenDeep(values(tables)
      .map(function(table) {
        return table.map(values);
      }));

  return stripHTML(attributeValues.concat(tableValues).join('\0'));
}
