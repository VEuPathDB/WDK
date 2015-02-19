/**
 * Common string utils for WDK.
 */

/**
 * Capitalize the first character of `value`
 *
 * @param {string} str
 */
export const capitalize = str => str[0].toUpperCase() + str.slice(1);

/**
 * Replace '_' with ' ' and capitalize the first character of `value`
 *
 * @param {string} str
 */
export const formatAttributeName = str => capitalize(str.replace(/_/g, ' '));

// TODO Look up or inject custom formatters
export const formatAttributeValue = attribute => {
  // FIXME Add type to attribute definition
  // let { value, type } = attribute;
  // switch(type) {
  //   case 'text': return value;
  //   case 'link': return (<a href={value.url}>{value.display}</a>);
  //   default: throw new TypeError(
  //     `Unkonwn type "${attribute.type}" for attribute ${attribute.name}`
  //   );
  // }
  return attribute.value;
}
