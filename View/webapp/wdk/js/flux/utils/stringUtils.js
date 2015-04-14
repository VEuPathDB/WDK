/**
 * Common string utils for WDK.
 */

/**
 * Capitalize the first character of `value`
 *
 * @param {string} str
 */
export function capitalize(str) {
  return str[0].toUpperCase() + str.slice(1);
}

/**
 * Replace '_' with ' ' and capitalize the first character of `value`
 *
 * @param {string} str
 */
export function formatAttributeName(str) {
  return capitalize(str.replace(/_/g, ' '));
}

// TODO Look up or inject custom formatters
// FIXME Return React-renderable.
export function formatAttributeValue(value, type) {
  if (type === 'link') {
    return `<a href="${value.url}">${value.displayText}</a>`;
  }
  return value;
}
