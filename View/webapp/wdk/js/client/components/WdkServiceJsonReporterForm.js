import React from 'react';
import { wrappable } from '../utils/componentUtils';
import * as util from '../utils/reporterUtils';
import ReporterCheckboxList from './ReporterCheckboxList';

let WdkServiceJsonReporterForm = React.createClass({

  componentWillMount() {
    let { formState, preferences, question, onFormChange, onFormUiChange } = this.props;
    let newFormState = this.discoverFormState(formState, preferences, question);
    setTimeout(() => {
      onFormChange(newFormState);
      // currently no special UI state on this form
      onFormUiChange({});
    }, 0);
  },

  discoverFormState(formState, preferences, question) {
    let currentAttributes = (formState == null ? undefined : formState.attributes);
    let currentTables = (formState == null ? undefined : formState.tables);
    return {
      attributes: util.getAttributeSelections(currentAttributes, preferences, question),
      tables: util.getTableSelections(currentTables)
    };
  },

  onAttributesChange(newAttributes) {
    this.props.onFormChange({
      attributes: newAttributes,
      tables: this.props.formState.tables
    });
  },

  onTablesChange(newTables) {
    this.props.onFormChange({
      attributes: this.props.formState.attributes,
      tables: newTables
    });
  },

  render() {
    let { question, recordClass, preferences, formState } = this.props;
    let realFormState = this.discoverFormState(formState, preferences, question);
    return (
      <div>
        <ReporterCheckboxList
          name="attributes" title="Choose Attributes"
          allValues={util.getAllAttributes(recordClass, question, util.isInReport)}
          selectedValueNames={realFormState.attributes}
          onChange={this.onAttributesChange}/>
        <ReporterCheckboxList
          name="tables" title="Choose Tables"
          allValues={util.getAllTables(recordClass, util.isInReport)}
          selectedValueNames={realFormState.tables}
          onChange={this.onTablesChange}/>
      </div>
    );
  }
});

export default wrappable(WdkServiceJsonReporterForm);
