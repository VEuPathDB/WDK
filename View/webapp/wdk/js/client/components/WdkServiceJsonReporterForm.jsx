import { getChangeHandler, wrappable } from '../utils/componentUtils';
import { isInReport, getAllAttributes, getAllTables, getAttributeSelections } from '../utils/reporterUtils';
import ReporterCheckboxList from './ReporterCheckboxList';

let WdkServiceJsonReporterForm = props => {
  let { question, recordClass, formState, onFormChange, onSubmit } = props;
  let getUpdateHandler = fieldName => getChangeHandler(fieldName, onFormChange, formState);
  return (
    <div>
      <ReporterCheckboxList title="Choose Attributes"
          onChange={getUpdateHandler('attributes')}
          fields={getAllAttributes(recordClass, question, isInReport)}
          selectedFields={formState.attributes}/>

      <ReporterCheckboxList title="Choose Tables"
          onChange={getUpdateHandler('tables')}
          fields={getAllTables(recordClass, isInReport)}
          selectedFields={formState.tables}/>

      <div style={{width:'30em',textAlign:'center', margin:'0.6em 0'}}>
        <input type="button" value="Submit" onClick={onSubmit}/>
      </div>
    </div>
  );
};

WdkServiceJsonReporterForm.getInitialState = (downloadFormStoreState, userStoreState) => ({
  formState: {
    attributes: getAttributeSelections(
        userStoreState.preferences, downloadFormStoreState.question),
    tables: []
  },
  formUiState: null
});

export default WdkServiceJsonReporterForm;
