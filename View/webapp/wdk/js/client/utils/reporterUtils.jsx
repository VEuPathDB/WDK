import { getTree, nodeHasProperty } from './OntologyUtils';
import { isQualifying, addSearchSpecificSubtree } from './CategoryUtils';
import CheckboxList from '../components/CheckboxList';

/**
 * Typical attachment type vocabulary for reporter forms
 */
export let attachmentTypes = [
  { value: "text", display: "Text File" },
  { value: "plain", display: "Show in Browser" }
];

export let tabularAttachmentTypes = [
  { value: "text", display: "Text File" },
  { value: "excel", display: "Excel File*" },
  { value: "plain", display: "Show in Browser"}
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
 *   1. user preferences if they exist
 *   2. default columns for the question
 * Then must trim off any non-download-scope attributes
 */
export function getAttributeSelections(userPrefs, question, allReportScopedAttrs) {
  // try initializing based on user prefs
  let userPrefKey = question.name + "_summary";
  let initialAttrs = (userPrefKey in userPrefs ?
      // preference key found; use user preference
      userPrefs[userPrefKey].split(',') :
      // otherwise, use default attribs from question
      question.defaultAttributes);
  // now must trim off any that do not appear in the tree (probably results-page scoped)
  return initialAttrs.filter(attr => allReportScopedAttrs.indexOf(attr) != -1);
}

export function getAttributeTree(categoriesOntology, recordClassName, question) {
  let categoryTree = getTree(categoriesOntology, isQualifying({
    targetType: 'attribute',
    recordClassName,
    scope: 'download'
  }));
  return addSearchSpecificSubtree(question, categoryTree);
}

export function getTableTree(categoriesOntology, recordClassName) {
  let categoryTree = getTree(categoriesOntology, isQualifying({
    targetType: 'table',
    recordClassName,
    scope: 'download'
  }));
  return categoryTree;
}

/**
 * Special implementation of a regular form change handler that adds the
 * recordclass's primary key to any new value passed in
 */
export function getAttributesChangeHandler(inputName, onParentChange, previousState, recordClass) {
  return newAttribsArray => {
    onParentChange(Object.assign({}, previousState, { [inputName]: addPk(newAttribsArray, recordClass) }));
  };
}

/**
 * Inspects the passed attributes array.  If the recordClass's primary key
 * attribute is not already in the array, returns a copied array with the PK as
 * the first element.  If not, simply returns the passed array.
 */
export function addPk(attributesArray, recordClass) {
  return prependAttrib(recordClass.recordIdAttributeName, attributesArray);
}

export function prependAttrib(attribName, attributesArray) {
  let currentIndex = attributesArray.indexOf(attribName);
  if (currentIndex > -1) {
    // attrib already present, copy passed array and remove existing instance
    attributesArray = attributesArray.slice();
    attributesArray.splice(currentIndex, 1);
  }
  // prepend clean array with passed attrib name
  return [ attribName ].concat(attributesArray);
}
