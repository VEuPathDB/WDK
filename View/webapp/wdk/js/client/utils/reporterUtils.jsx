import CheckboxList from '../components/CheckboxList';

/**
 * Typical attachment type vocabulary for reporter forms
 */
export let attachmentTypes = [
  { value: "text", display: "File" },
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
export function getAttributeSelections(currentAttributes, userPrefs, question) {
  if (currentAttributes != null && currentAttributes != undefined) {
    return currentAttributes;
  }
  // try initializing based on user prefs
  let userPrefKey = question.name + "_summary";
  if (userPrefKey in userPrefs) {
    return userPrefs[userPrefKey].split(',');
  }
  // otherwise, use default attribs from question
  return question.defaultAttributes;
}

/**
 * Initializes form table state to be an empty array if not already set
 */
export function getTableSelections(currentTables) {
  return (currentTables != null && currentTables != undefined ? currentTables : []);
}

export function getReporterCheckboxList(title, onChange, fields, selectedFields) {
  let mappedFields = fields.map(val => ({ value: val.name, display: val.displayName }));
  return (
    <div>
      <h3>{title}</h3>
      <div style={{padding: '0 2em'}}>
        <CheckboxList onChange={onChange} items={mappedFields} value={selectedFields}/>
      </div>
    </div>
  );
}
