import React from 'react';
import { getChangeHandler } from '../utils/componentUtils';
import { getAttributeTree, getTableTree, getAttributeSelections, getAttributesChangeHandler, addPk } from '../utils/reporterUtils';
import {getAllLeafIds, CategoryTreeNode} from '../utils/CategoryUtils';
import CategoriesCheckboxTree from './CategoriesCheckboxTree';
import ReporterSortMessage from './ReporterSortMessage';
import {State} from "../stores/DownloadFormStore";
import {RecordClass, Question} from "../utils/WdkModel";
import {Ontology} from "../utils/OntologyUtils";

type Props<T, U> = {
  scope: string;
  question: Question;
  recordClass: RecordClass;
  summaryView: string;
  ontology: Ontology<CategoryTreeNode>;
  formState: any;
  formUiState: any;
  updateFormState: (state: T) => T;
  updateFormUiState: (uiState: U) => U;
  onSubmit: () => void;
}

function WdkServiceJsonReporterForm<T, U>(props: Props<T, U>) {
  let { scope, question, recordClass, ontology, formState, formUiState, updateFormState, updateFormUiState, onSubmit } = props;
  let getUpdateHandler = (fieldName: string) => getChangeHandler(fieldName, updateFormState, formState);
  let getUiUpdateHandler = (fieldName: string) => getChangeHandler(fieldName, updateFormUiState, formUiState);
  return (
    <div>
      <ReporterSortMessage scope={scope}/>
      <CategoriesCheckboxTree
        title="Choose Columns:"
        leafType="columns"
        searchBoxPlaceholder="Search Columns..."
        tree={getAttributeTree(ontology, recordClass.name, question)}

        selectedLeaves={formState.attributes}
        expandedBranches={formUiState.expandedAttributeNodes}
        searchTerm={formUiState.attributeSearchText}

        onChange={getAttributesChangeHandler('attributes', updateFormState, formState, recordClass)}
        onUiChange={getUiUpdateHandler('expandedAttributeNodes')}
        onSearchTermChange={getUiUpdateHandler('attributeSearchText')}
      />

      <CategoriesCheckboxTree
        title="Choose Tables:"
        leafType="columns"
        searchBoxPlaceholder="Search Tables..."
        tree={getTableTree(ontology, recordClass.name)}

        selectedLeaves={formState.tables}
        expandedBranches={formUiState.expandedTableNodes}
        searchTerm={formUiState.tableSearchText}

        onChange={getUpdateHandler('tables')}
        onUiChange={getUiUpdateHandler('expandedTableNodes')}
        onSearchTermChange={getUiUpdateHandler('tableSearchText')}
      />

      <div style={{width:'30em',textAlign:'center', margin:'0.6em 0'}}>
        <input type="button" value="Submit" onClick={onSubmit}/>
      </div>
    </div>
  );
}

namespace WdkServiceJsonReporterForm {
  export function getInitialState(downloadFormStoreState: State) {
    let { scope, question, recordClass, globalData: { ontology, preferences } } = downloadFormStoreState;
    // select all attribs and tables for record page, else column user prefs and no tables
    let attribs = (scope === 'results' ?
      addPk(getAttributeSelections(preferences, question), recordClass) :
      addPk(getAllLeafIds(getAttributeTree(ontology, recordClass.name, question)), recordClass));
    let tables = (scope === 'results' ? [] :
      getAllLeafIds(getTableTree(ontology, recordClass.name)));
    return {
      formState: {
        attributes: attribs,
        tables:tables
      },
      formUiState: {
        expandedAttributeNodes: null,
        attributeSearchText: "",
        expandedTableNodes: null,
        tableSearchText: ""
      }
    };
  }
}

export default WdkServiceJsonReporterForm;
