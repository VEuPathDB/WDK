import { max, min, padStart } from 'lodash';

import { isRange } from './Utils/FilterServiceUtils';

/**
 * Determine if a filter should be created, or if the values represent the default state.
 *
 * @param {Field} field Field term id
 * @param {any} value Filter value
 * @param {boolean} includeUnknown
 * @param {ValueCount[]} valueCounts
 */
export function shouldAddFilter(field, value, includeUnknown, valueCounts, selectByDefault) {
  if (selectByDefault == false) {
    return isRange(field) && (value == null || (value.min == null && value.max == null)) ? false
         : value == null ? true
         : value.length == 0 ? false
         : true;
  }

  // user doesn't want unknowns
  if (!includeUnknown) return true;

  // user wants everything except unknowns
  if (value == null) return !includeUnknown;

  if (isRange(field)) {
    const values = valueCounts
      .filter(entry => entry.value != null)
      .map(entry => field.type === 'number' ? Number(entry.value) : entry.value);
    const summaryMin = min(values);
    const summaryMax = max(values);
    return (
      (value.min == null && value.max == null) ||
      (value.min != null && value.min > summaryMin) ||
      (value.max != null && value.max < summaryMax)
    );
  }

  return value.length !== valueCounts.filter(item => item.value != null).length;
}

const dateStringRe = /^(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?$/;

/**
 * Returns an strftime style format string.
 * @param {string} dateString
 */
export function getFormatFromDateString(dateString) {
  var matches = dateString.match(dateStringRe);
  if (matches == null) {
    throw new Error(`Expected a date string using the ISO 8601 format, but got "${dateString}".`);
  }
  var [ , , m, d ] = matches;
  return  d !== undefined ? '%Y-%m-%d'
    : m !== undefined ? '%Y-%m'
    : '%Y';
}

/**
 * Returns a formatted date.
 *
 * @param {string} format strftime style format string
 * @param {Date} date
 */
export function formatDate(format, date) {
  if (!(date instanceof Date)) {
    date = new Date(date);
  }
  return format
  .replace(/%Y/, String(date.getFullYear()))
  .replace(/%m/, padStart(String(date.getMonth() + 1), 2, '0'))
  .replace(/%d/, padStart(String(date.getDate()), 2, '0'));
}
