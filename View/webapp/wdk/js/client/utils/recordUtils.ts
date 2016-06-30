import {flattenDeep, partial, pick, values} from 'lodash';
import { filterItems } from './SearchUtils';
import { Record, AttributeValue, TableValue } from './WdkModel';

type Dictionary<T> = {
  [key: string]: T
};
type AttributeValueDict = Dictionary<AttributeValue>;
type TableValueDict = Dictionary<TableValue>;
type FilterSpec = {
  /** Search string */
  filterTerm: string;
  /** Record attributes to search in */
  filterAttributes: string[];
  /** Record tables to search in */
  filterTables: string[];
};

/**
 * Filter the results of an answer and return the filtered results.
 */
export function filterRecords(records: Record[], filterSpec: FilterSpec): Record[] {
  let { filterTerm, filterAttributes = [], filterTables = [] } = filterSpec;
  let getSearchableStringPartial = partial(getSearchableString, filterAttributes, filterTables);
  return filterItems(records, getSearchableStringPartial, filterTerm);
}

/**
 * Strip HTML characters from a string.
 */
function stripHTML(str: string): string {
  let span = document.createElement('span');
  span.innerHTML = str;
  return span.textContent;
}

/**
 * Combine appropriate fields from the record into a searchable string.
 *
 * The approach here is pretty basic and probably ineffecient:
 *   - Convert all attribute values to an array of values.
 *   - Convert all table values to a flat array of values.
 *   - Combine the above arrays into a single array.
 *   - Join the array with a control character.
 *
 * There is much room for performance tuning here.
 */
function getSearchableString(filterAttributes: string[], filterTables: string[], record: Record): string {
  let useAllTablesAndAttributes = filterAttributes.length === 0 && filterTables.length === 0;
  let attributes = useAllTablesAndAttributes ? record.attributes : pick<AttributeValueDict, AttributeValueDict>(record.attributes, filterAttributes);
  let tables = useAllTablesAndAttributes ? record.tables : pick<TableValueDict, TableValueDict>(record.tables, filterTables);
  let attributeValues = Object.keys(attributes).map(name => attributes[name]);
  let tableValues = flattenDeep<AttributeValue>(values(tables)
      .map(function(table: TableValue) {
        return table.map(row => values<AttributeValue>(row));
      }));

  return stripHTML(attributeValues.concat(tableValues).join('\0'));
}
