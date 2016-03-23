import { getTree, nodeHasProperty } from './OntologyUtils';
import CheckboxList from '../components/CheckboxList';

/**
 * Typical attachment type vocabulary for reporter forms
 */
export let attachmentTypes = [
  { value: "text", display: "Text File" },
  { value: "plain", display: "Show in Browser" }
];

/**
 * Predicate to tell whether a given object should be shown in a reporter form
 */
export function isInReport(obj) {
  return obj.isInReport;
}

/**
 * Retrieves attribute metadata objects from the passed record class that pass
 * the predicate and appends any reporter dynamic attribute metadata (that pass
 * the predicate) from the question.
 */
export function getAllAttributes(recordClass, question, predicate) {
  let attributes = recordClass.attributes.filter(predicate);
  question.dynamicAttributes.filter(predicate)
    .forEach(reportAttr => { attributes.push(reportAttr); });
  return attributes;
}

/**
 * Retrieves table metadata objects from the passed record class that pass the
 * predicate.
 */
export function getAllTables(recordClass, predicate) {
  return recordClass.tables.filter(predicate);
}

/**
 * Initializes form attribute state based on:
 *   1. current attribute selections
 *   2. user preferences if they exist
 *   3. default columns for the question
 */
export function getAttributeSelections(userPrefs, question) {
  // try initializing based on user prefs
  let userPrefKey = question.name + "_summary";
  if (userPrefKey in userPrefs) {
    return userPrefs[userPrefKey].split(',');
  }
  // otherwise, use default attribs from question
  return question.defaultAttributes;
}

export function getAttributeTree(categoriesOntology, question, recordClass) {
  let qualified = node => (
      nodeHasProperty('targetType', 'attribute', node) &&
      nodeHasProperty('recordClassName', recordClass.name, node) &&
      nodeHasProperty('scope', 'download', node));
  let categoryTree = getTree(categoriesOntology, qualified);
  addSearchSpecificSubtree(question, categoryTree);
  return categoryTree;
}
