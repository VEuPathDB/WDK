import './BaseAttributeAnalysis.scss';

import { escapeRegExp } from 'lodash';
import React from 'react';

import Mesa from 'Components/Mesa';
import RealTimeSearchBox from 'Components/SearchBox/RealTimeSearchBox';
import Tabs from 'Components/Tabs/Tabs';
import { DispatchAction } from 'Core/CommonTypes';
import { Seq } from 'Utils/IterableUtils';

import { TableSearched, TableSorted, TabSelected } from './BaseAttributeAnalysisActions';
import { State } from './BaseAttributeAnalysisState';

type VisualizationConfig = {
  display: string;
  content: React.ReactNode;
}

type TableConfig<T extends string> = {
  columns: { key: T; display: string; }[];
  data: { [P in T]: string | number }[]
}

type Props<T extends string> = {
  state: State<T>;
  dispatch: DispatchAction;
  visualizationConfig: VisualizationConfig;
  tableConfig: TableConfig<T>;
}

type Column = { key: 'value' | 'count'; display: string; }

export class AttributeAnalysis<T extends string> extends React.PureComponent<Props<T>> {

  onSort = (column: Column, direction: 'asc' | 'desc') =>
    this.props.dispatch(TableSorted.create({ key: column.key, direction }))

  onSearch = (search: string) =>
    this.props.dispatch(TableSearched.create(search));

  onTabSelected = (tab: string) =>
    this.props.dispatch(TabSelected.create(tab))

  render() {
    const { state, visualizationConfig, tableConfig } = this.props;
    const { sort, search } = state.table;


    const { data } = this.props.tableConfig;

    const searchRe = new RegExp(escapeRegExp(search), 'i');

    const filteredData = Seq.from(tableConfig.data)
      .filter(row => search ? tableConfig.columns.some(column => searchRe.test(String(row[column.key] || ''.toLowerCase()))) : true)
      .orderBy(row => row[sort.key], sort.direction === 'desc')
      .toArray();

    return (
      <Tabs
        className="TabularAttributeAnalysis"
        activeTab={state.tabs.activeTab}
        onTabSelected={this.onTabSelected}
        tabs={[
          {
            key: 'visualization',
            display: visualizationConfig.display,
            content: visualizationConfig.content
          },
          {
            key: 'table',
            display: 'Data',
            content: (
              <React.Fragment>
                <RealTimeSearchBox
                  className="TabularAttributeAnalysisSearchBox"
                  placeholderText="Search table"
                  searchTerm={state.table.search}
                  onSearchTermChange={this.onSearch}
                />
                <Mesa
                  state={{
                    options: {
                      useStickyHeader: true,
                      tableBodyMaxHeight: '60vh'
                    },
                    actions: [],
                    eventHandlers: {
                      onSort: this.onSort
                    },
                    uiState: {
                      sort: {
                        columnKey: state.table.sort.key,
                        direction: state.table.sort.direction
                      }
                    },
                    rows: data,
                    filteredRows: filteredData,
                    columns: tableConfig.columns.map(({ key, display: name }) => ({
                      key,
                      name,
                      sortable: true,
                    }))
                  }}
                />
              </React.Fragment>
            )
          }
        ]}
      />
    );
  }
}
