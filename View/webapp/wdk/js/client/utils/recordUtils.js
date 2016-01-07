import partial from 'lodash/function/partial';
import flattenDeep from 'lodash/array/flattenDeep';
import pick from 'lodash/object/pick';
import values from 'lodash/object/values';


/**
 * Filter the results of an answer and return the filtered results.
 *
 * @param {Iterable<Record>} records                     The list of records to filter
 * @param {object}           filterSpec                  The filter specification
 * @param {string}           filterSpec.filterTerm       The query with which to filter records
 * @param {Array<string>}    filterSpec.filterAttributes The attributes to search.
 * @param {Array<string>}    filterSpec.filterTables     The tables to search.
 */
export function filterRecords(records, filterSpec) {
  let { filterTerm, filterAttributes = [], filterTables = [] } = filterSpec;

  if (!filterTerm || !records) return records;

  let terms = parseSearchTerms(filterTerm);
  return terms.reduce(function(records, term) {
    let predicate = partial(isTermInRecord, term, filterAttributes, filterTables);
    return records.filter(predicate);
  }, records);
}

// Split terms on whitespace, unless wrapped in quotes
function parseSearchTerms(terms) {
  let match = terms.match(/\w+|"[^"]*"/g) || [];
  return match.map(function(term) {
    // remove wrapping quotes from phrases
    return term.replace(/(^")|("$)/g, '');
  });
}

function stripHTML(str) {
  let span = document.createElement('span');
  span.innerHTML = str;
  return span.textContent;
}

// Search record for a term.
//
// The approach here is pretty basic and probably ineffecient:
//   - Convert all attribute values to an array of values.
//   - Convert all table values to a flat array of values.
//   - Combine the above arrays into a single array.
//   - Join the array with a control character.
//   - Search the resulting string for the index of 'term'.
//   - return (index !== -1).
//
// There is much room for performance tuning here.
function isTermInRecord(term, filterAttributes, filterTables, record) {
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

  let clob = stripHTML(attributeValues.concat(tableValues).join('\0'));
  return clob.toLowerCase().includes(term.toLowerCase());
}
