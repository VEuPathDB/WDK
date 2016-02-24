/**
 * General use utilities for WDK-related entities
 */

/**
 * Get an attribute definition for a given recordClass.
 *
 * @param {Object} recordClass
 * @param {string} name Name of attribute as defined in the WDK Model XML
 */
export function getAttribute(recordClass, name) {
    return recordClass.attributesMap.get(name);
}

/**
 * Get a table definition for a given recordClass.
 *
 * @param {Object} recordClass
 * @param {string} name Name of table as defined in the WDK Model XML
 */
export function getTable(recordClass, name) {
    return recordClass.tablesMap.get(name);
}
