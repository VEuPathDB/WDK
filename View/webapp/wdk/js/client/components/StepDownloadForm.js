import React from 'react';
import { wrappable, addOrRemove } from '../utils/componentUtils';
import CheckboxList from './CheckboxList';

let StepDownloadForm = React.createClass({

  getAllAttributes() {
    let attributes = this.props.recordClass.attributes.filter(attr => attr.isInReport);
    this.props.question.dynamicAttributes.filter(attr => attr.isInReport)
      .forEach(reportAttr => { attributes.push(reportAttr); });
    return attributes;
  },

  getAllTables() {
    return this.props.recordClass.tables.filter(table => table.isInReport);
  },

  // always initialize form state based on:
  //   1. current form
  //   1. user preferences if they exist
  //   2. default columns for the question
  getCurrentAttributeSelections(formState, userPrefs, question) {
    if (formState != null && formState.attributes != null) {
      return formState.attributes;
    }
    // try initializing based on user prefs
    let userPrefKey = question.name + "_summary";
    if (userPrefKey in userPrefs) {
      return userPrefs[userPrefKey].split(',');
    }
    // otherwise, use default attribs from question
    return question.defaultAttributes;
  },

  getCurrentTableSelections(formState) {
    return (formState != null && formState.tables != null ?
      formState.tables : []);
  },

  componentWillMount() {
    let initialAttributes = this.getCurrentAttributeSelections(
        this.props.formState, this.props.preferences, this.props.question);
    let initialTables = this.getCurrentTableSelections(this.props.formState);
    setTimeout(() => {
      this.updateFormState(initialAttributes, initialTables);
      // currently no special UI state on this form
      this.props.onFormUiChange({});
    }, 0);
  },

  onAttributeChange(event) {
    if (this.props.formState == null || this.props.formState.attributes == null) {
      // ignore for now; user can click again
      return;
    }
    // call update with modified list
    this.updateAttribs(addOrRemove(
        this.props.formState.attributes, event.target.value));
  },

  onTableChange(event) {
    if (this.props.formState == null || this.props.formState.tables == null) {
      // ignore for now; user can click again
      return;
    }
    // call update with modified list
    this.updateTables(addOrRemove(
        this.props.formState.tables, event.target.value));
  },

  onSelectAllAttribs() { this.updateAttribs(this.getAllAttributes().map(attr => attr.name)); },
  onClearAllAttribs() { this.updateAttribs([]); },
  onSelectAllTables() { this.updateTables(this.getAllTables().map(table => table.name)); },
  onClearAllTables() { this.updateTables([]); },

  updateAttribs(array) { this.updateFormState(array, this.props.formState.tables); },
  updateTables(array) { this.updateFormState(this.props.formState.attributes, array); },

  updateFormState(attribsArray, tablesArray) {
    this.props.onFormChange({
      attributes: attribsArray,
      tables: tablesArray
    });
  },

  render() {
    let {
      step,
      summaryView,
      question,
      recordClass,
      user,
      preferences,
      selectedReporter,
      formState,
      formUiState,
      onFormChange,
      onFormUiChange
    } = this.props;

    let reporterDisplay = (selectedReporter == null ? "WDK Standard JSON" :
      recordClass.formats.find(r => (r.name === selectedReporter)).displayName);
    let selectedAttributes = this.getCurrentAttributeSelections(formState, preferences, question);
    let selectedTables = this.getCurrentTableSelections(formState);
    return (
      <div>
        <h2>Configure download of: {reporterDisplay}</h2>
        <h3>Choose Attributes</h3>
        <div style={{padding: '0 2em'}}>
          <CheckboxList name="attributes"
            onChange={this.onAttributeChange}
            onSelectAll={this.onSelectAllAttribs}
            onClearAll={this.onClearAllAttribs}
            selectedItems={selectedAttributes}
            items={this.getAllAttributes()
              .map(attr => ({ value: attr.name, display: attr.displayName }))}/>
        </div>
        <h3>Choose Tables</h3>
        <div style={{padding: '0 2em'}}>
          <CheckboxList name="tables"
            onChange={this.onTableChange}
            onSelectAll={this.onSelectAllTables}
            onClearAll={this.onClearAllTables}
            selectedItems={selectedTables}
            items={this.getAllTables()
              .map(table => ({ value: table.name, display: table.displayName }))}/>
        </div>
      </div>
    );

  }
});

export default wrappable(StepDownloadForm);
