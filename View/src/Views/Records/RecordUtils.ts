import { Seq } from "Utils/IterableUtils";
import {flattenDeep, partial, pick, values} from 'lodash';
import { filterItems } from 'Utils/SearchUtils';
import { RecordInstance, AttributeValue, TableValue } from 'Utils/WdkModel';

type AttributeValueDict = Record<string, AttributeValue>;
type TableValueDict = Record<string, TableValue>;
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
export function filterRecords(records: RecordInstance[], filterSpec: FilterSpec): RecordInstance[] {
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
  return span.textContent || '';
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
function getSearchableString(filterAttributes: string[], filterTables: string[], record: RecordInstance): string {
  let useAllTablesAndAttributes = filterAttributes.length === 0 && filterTables.length === 0;
  let attributes = useAllTablesAndAttributes ? record.attributes : pick(record.attributes, filterAttributes);
  let tables = useAllTablesAndAttributes ? record.tables : pick(record.tables, filterTables);
  return Seq.from(values(tables))
    .flatMap(rows => rows)
    .flatMap(row => Object.values(row))
    .concat(values(attributes))
    .flatMap(value =>
      value == null ? []
      : typeof value === 'object' ? [value.displayText || value.url]
      : [value] )
    .map(stripHTML)
    .reduce((compositeStr, nextStr) => `${compositeStr}\0${nextStr}`);
}
