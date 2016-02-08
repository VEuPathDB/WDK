import React from 'react';
import { wrappable } from '../utils/componentUtils';
import * as util from '../utils/reporterUtils';
import ReporterCheckboxList from './ReporterCheckboxList';

let WdkServiceJsonReporterForm = React.createClass({

  componentDidMount() {
    let { formState, preferences, question, onFormChange, onFormUiChange } = this.props;
    let newFormState = this.discoverFormState(formState, preferences, question);
    onFormChange(newFormState);
    // currently no special UI state on this form
    onFormUiChange({});
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
    this.props.onFormChange(Object.assign({}, this.props.formState, { attributes: newAttributes }));
  },

  onTablesChange(newTables) {
    this.props.onFormChange(Object.assign({}, this.props.formState, { tables: newTables }));
  },

  render() {
    let { question, recordClass, preferences, formState, onSubmit } = this.props;
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
        <div style={{width:'30em',textAlign:'center', margin:'0.6em 0'}}>
          <input type="button" value="Submit" onClick={onSubmit}/>
        </div>
      </div>
    );
  }
});

export default wrappable(WdkServiceJsonReporterForm);
