import { getChangeHandler, wrappable } from '../utils/componentUtils';
import { isInReport, getAttributeTree, getAllAttributes, getAllTables, getAttributeSelections } from '../utils/reporterUtils';
import ReporterCheckboxList from './ReporterCheckboxList';
import CategoriesCheckboxTree from './CategoriesCheckboxTree';

let WdkServiceJsonReporterForm = props => {
  let { question, recordClass, ontology, formState, formUiState, onFormChange, onFormUiChange, onSubmit } = props;
  let getUpdateHandler = fieldName => getChangeHandler(fieldName, onFormChange, formState);
  let getUiUpdateHandler = fieldName => getChangeHandler(fieldName, onFormUiChange, formUiState);
  return (
    <div>
      <ReporterCheckboxList title="Choose Attributes"
          onChange={getUpdateHandler('attributes')}
          fields={getAllAttributes(recordClass, question, isInReport)}
          selectedFields={formState.attributes}/>

      <CategoriesCheckboxTree

          // title and layout of the tree
          title="Choose Attributes"
          searchBoxPlaceholder="Search Attributes..."
          tree={getAttributeTree(ontology, question, recordClass)}

          // state of the tree
          selectedLeaves={formState.attributes}
          expandedBranches={formUiState.expandedAttributeNodes}
          searchText={formUiState.attributeSearchText}

          // change handlers for each state element controlled by the tree
          onChange={getUpdateHandler('attributes')}
          onUiChange={getUiUpdateHandler('expandedAttributeNodes')}
          onSearchTextChange={getUiUpdateHandler('attributeSearchText')}
      />

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

// TODO: use initial selections to determine initial expansion state
WdkServiceJsonReporterForm.getInitialState = (downloadFormStoreState, userStoreState) => ({
  formState: {
    attributes: getAttributeSelections(
        userStoreState.preferences, downloadFormStoreState.question),
    tables: []
  },
  formUiState: {
    expandedAttributeNodes: [],
    attributeSearchText: "",
    expandedTableNodes: [],
    tableSearchText: ""
  }
});

export default WdkServiceJsonReporterForm;
