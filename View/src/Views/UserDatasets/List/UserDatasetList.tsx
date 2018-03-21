import React from 'react';
import { escape } from 'lodash';
import { History } from 'history';
import {
  Mesa,
  MesaState,
  Utils as MesaUtils,
  AnchoredTooltip as Tooltip
} from 'mesa';

import { User } from 'Utils/WdkUser';
import { wrappable } from 'Utils/ComponentUtils';
import { bytesToHuman } from 'Utils/Converters';
import { UserDataset, UserDatasetMeta } from 'Utils/WdkModel';

import Link from 'Components/Link';
import Icon from 'Components/Icon/IconAlt';
import Modal from 'Components/Overlays/Modal';
import Loading from 'Components/Loading/Loading';
import TextBox from 'Components/InputControls/TextBox';
import DataTable from 'Components/DataTable/DataTable';
import SearchBox from 'Components/SearchBox/RealTimeSearchBox';
import { MesaColumn, MesaDataCellProps } from 'Core/CommonTypes';

interface Props {
  user: User;
  history: History;
  userDatasets: UserDataset[];
  updateUserDatasetDetail: (userDataset: UserDataset, meta: UserDatasetMeta) => any
};

interface MesaSortObject {
  columnKey: string;
  direction: string;
};

interface State {
  selectedRows: number[];
  uiState: {
    sort: MesaSortObject;
  };
  searchTerm: string;
  editingCache: any;
}

class UserDatasetList extends React.Component <Props, State> {
  state: State;
  props: Props;

  constructor (props: Props) {
    super(props);
    this.state = {
      selectedRows: [],
      uiState: {
        sort: {
          columnKey: '',
          direction: 'asc'
        }
      },
      editingCache: {},
      searchTerm: ''
    };

    this.onSort = this.onSort.bind(this);
    this.getColumns = this.getColumns.bind(this);
    this.onRowSelect = this.onRowSelect.bind(this);
    this.onRowDeselect = this.onRowDeselect.bind(this);
    this.isRowSelected = this.isRowSelected.bind(this);
    this.getEventHandlers = this.getEventHandlers.bind(this);
    this.onMultipleRowSelect = this.onMultipleRowSelect.bind(this);
    this.onMultipleRowDeselect = this.onMultipleRowDeselect.bind(this);

    this.editNameField = this.editNameField.bind(this);
    this.onNameFieldChange = this.onNameFieldChange.bind(this);
    this.saveEditNameField = this.saveEditNameField.bind(this);
    this.cancelEditNameField = this.cancelEditNameField.bind(this);
    this.renderEditableNameField = this.renderEditableNameField.bind(this);
    this.editSummaryField = this.editSummaryField.bind(this);
    this.onSummaryFieldChange = this.onSummaryFieldChange.bind(this);
    this.saveEditSummaryField = this.saveEditSummaryField.bind(this);
    this.cancelEditSummaryField = this.cancelEditSummaryField.bind(this);
    this.renderEditableSummaryField = this.renderEditableSummaryField.bind(this);

    this.isMyDataset = this.isMyDataset.bind(this);
    this.renderOwnerCell = this.renderOwnerCell.bind(this);
    this.filterAndSortRows = this.filterAndSortRows.bind(this);
    this.onSearchTermChange = this.onSearchTermChange.bind(this);
  }

  isRowSelected (row: UserDataset): boolean {
    const id: number = row.id;
    const { selectedRows } = this.state;
    return selectedRows.includes(id);
  }

  isMyDataset (dataset: UserDataset): boolean {
    const { user } = this.props;
    return user.id === dataset.ownerUserId;
  }

  onSearchTermChange (searchTerm: string) {
    this.setState({ searchTerm });
  }

  editNameField (row: UserDataset): void {
    const { editingCache } = this.state;
    const editingKey = `name:${row.id}`;
    if (editingKey in editingCache) return;
    this.setState({ editingCache: { ...editingCache, [editingKey]: row.meta.name }});
  }

  cancelEditNameField (row: UserDataset): void {
    const editingCache = { ...this.state.editingCache };
    delete editingCache[`name:${row.id}`];
    this.setState({ editingCache });
  }

  saveEditNameField (row: UserDataset): void {
    const { editingCache } = this.state;
    const { updateUserDatasetDetail } = this.props;
    const name = editingCache[`name:${row.id}`];
    const meta: UserDatasetMeta = { ...row.meta, name };
    updateUserDatasetDetail(row, meta);
    this.cancelEditNameField(row);
  }

  onNameFieldChange (row: UserDataset, name: string): void {
    const { editingCache } = this.state;
    this.setState({
      editingCache: {
        ...editingCache,
        [`name:${row.id}`]: name
      }
    });
  }

  renderEditableNameField (cellProps: MesaDataCellProps) {
    const { user } = this.props;
    const { editingCache } = this.state;
    const row: UserDataset = cellProps.row;
    const id: number = row.id;

    const editingKey = `name:${id}`;
    const editingName: any = editingKey in editingCache
      ? editingCache[editingKey]
      : null;

    const name: string = row.meta.name;
    const edit = () => this.editNameField(row);
    const save = () => this.saveEditNameField(row);
    const cancel = () => this.cancelEditNameField(row);
    const onChange = (event: React.ChangeEvent<HTMLInputElement>): void => { this.onNameFieldChange(row, event.target.value); };
    return (
      <div className="CellEditor CellEditor-Name">
        {editingName
          ? (
            <div className="CellEdit-Row">
              <input type="text" value={editingName} className="CellEdit-Input" onChange={onChange} />
              <div className="CellEdit-Actions">
                <Icon fa="check-circle CellEdit-Icon" title="Save Name Changes" onClick={save} />
                <Icon fa="times CellEdit-Icon" title="Cancel Name Changes" onClick={cancel} />
              </div>
            </div>
          ) : (
            <span>
              <Link to={`/workspace/datasets/${id}`}>
                {name} <span className="faded">({id})</span>
              </Link>
              {!this.isMyDataset(row) ? null : (
                <Icon fa="pencil CellEdit-Icon" title="Edit Dataset Name" onClick={edit} />
              )}
            </span>
          )
        }
      </div>
    );
  }

  editSummaryField (row: UserDataset): void {
    const { editingCache } = this.state;
    const editingKey = `summary:${row.id}`;
    if (editingKey in editingCache) return;
    this.setState({ editingCache: { ...editingCache, [editingKey]: row.meta.summary }});
  }

  cancelEditSummaryField (row: UserDataset): void {
    const editingCache = { ...this.state.editingCache };
    const editingKey = `summary:${row.id}`;
    delete editingCache[editingKey];
    this.setState({ editingCache });
  }

  saveEditSummaryField (row: UserDataset): void {
    const { editingCache } = this.state;
    const { updateUserDatasetDetail } = this.props;
    const editingKey = `summary:${row.id}`;
    const summary = editingCache[editingKey];
    const meta: UserDatasetMeta = { ...row.meta, summary };
    updateUserDatasetDetail(row, meta);
    this.cancelEditSummaryField(row);
  }

  onSummaryFieldChange (row: UserDataset, summary: string): void {
    const { editingCache } = this.state;
    const editingKey = `summary:${row.id}`;
    this.setState({ editingCache: { ...editingCache, [editingKey]: summary }});
  }

  renderEditableSummaryField (cellProps: MesaDataCellProps) {
    const { user } = this.props;
    const { editingCache } = this.state;
    const row: UserDataset = cellProps.row;
    const editingKey = `summary:${row.id}`;
    const id: number = row.id;
    const editingSummary: any = editingKey in editingCache
      ? editingCache[editingKey]
      : null;
    const summary: string = row.meta.summary;
    const edit = () => this.editSummaryField(row);
    const save = () => this.saveEditSummaryField(row);
    const cancel = () => this.cancelEditSummaryField(row);
    const onChange = (event: React.ChangeEvent<HTMLTextAreaElement>): void => { this.onSummaryFieldChange(row, event.target.value); }
    return (
      <div className="CellEditor CellEditor-Summary">
        {typeof editingSummary === 'string'
          ? (
            <div className="CellEdit-Row">
              <textarea
                rows={3}
                onChange={onChange}
                value={editingSummary}
                style={{ width: '100%' }}
                className="CellEdit-Input"
              />
              <div className="CellEdit-Actions">
                <Icon fa="check-circle CellEdit-Icon" title="Save Summary Changes" onClick={save} />
                <Icon fa="times CellEdit-Icon" title="Cancel Summary Changes" onClick={cancel} />
              </div>
            </div>
          ) : (
            <span>
              {escape(summary)}
              {!this.isMyDataset(row) ? null : (
                <Icon fa="pencil CellEdit-Icon" title="Edit Dataset Summary" onClick={edit} />
              )}
            </span>
          )
        }

      </div>
    );
  }

  renderStatusCell (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const { isInstalled } = row;
    const appNames = row.projects.join(', ');
    const content = isInstalled
      ? `Files in this dataset have been installed to ${appNames}.`
      : `The files in this dataset have not been installed to ${appNames}.`;
    const children = <Icon fa={isInstalled ? 'check-circle' : 'minus-circle'}/>;
    const tooltipProps = { content, children };
    return (
      <Link to={`/workspace/datasets/${row.id}`}>
        <Tooltip {...tooltipProps}/>
      </Link>
    );
  }

  renderTypeCell (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const { display, version } = row.type;
    return <span>{display} <span className="faded">({version})</span></span>
  }

  renderOwnerCell (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const { user } = this.props;
    const { owner } = row;
    return this.isMyDataset(row)
      ? <span className="faded">You</span>
      : <span>{owner}</span>
  }

  renderFileCountCell (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const fileCount = row.datafiles.length;
    return fileCount;
  }

  renderCreatedCell (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const { created } = row;
    const formattedDate = (new Date(created)).toLocaleDateString();
    return <span>{formattedDate}</span>
  }

  renderSizeCell (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const size: number = row.size;
    const percent: number = row.percentQuotaUsed;
    return (
      <span>
        {bytesToHuman(size)}
        {percent !== null ? <span style={{ opacity: 0.5 }}> ({percent}%)</span> : ''}
      </span>
    );
  }

  getColumns (): MesaColumn[] {
    const { userDatasets, user } = this.props;
    return [
      {
        key: 'id',
        sortable: true,
        name: 'Name / ID',
        renderCell: this.renderEditableNameField
      },
      {
        key: 'summary',
        name: 'Summary',
        renderCell: this.renderEditableSummaryField
      },
      {
        key: 'type',
        name: 'Type',
        sortable: true,
        renderCell: this.renderTypeCell
      },
      {
        key: 'status',
        className: 'StatusColumn',
        name: 'Status',
        style: { textAlign: 'center' },
        renderCell: this.renderStatusCell
      },
      {
        key: 'owner',
        name: 'Owner',
        sortable: true,
        renderCell: this.renderOwnerCell
      },
      {
        key: 'created',
        name: 'Created',
        sortable: true,
        renderCell: this.renderCreatedCell
      },
      {
        key: 'datafiles',
        name: 'File Count',
        renderCell: this.renderFileCountCell
      },
      {
        key: 'size',
        name: 'Size / Quota Usage',
        sortable: true,
        style: { textAlign: 'right' },
        renderCell: this.renderSizeCell
      }
    ]
  }

  onRowSelect (row: UserDataset): void {
    const id: number = row.id;
    const { selectedRows } = this.state;
    if (selectedRows.includes(id)) return;
    const newSelection: number[] = [ ...selectedRows, id ];
    this.setState({ selectedRows: newSelection });
  }

  onRowDeselect (row: UserDataset): void {
    const id: number = row.id;
    const { selectedRows } = this.state;
    if (!selectedRows.includes(id)) return;
    const newSelection: number[] = selectedRows.filter(selectedId => selectedId !== id);
    this.setState({ selectedRows: newSelection });
  }

  onMultipleRowSelect (rows: UserDataset[]): void {
    if (!rows.length) return;
    const { selectedRows } = this.state;
    const unselectedRows = rows
      .filter((dataset: UserDataset) => !selectedRows.includes(dataset.id))
      .map((dataset: UserDataset) => dataset.id);
    if (!unselectedRows.length) return;
    const newSelection: number[] = [ ...selectedRows, ...unselectedRows ];
    this.setState({ selectedRows: newSelection });
  }

  onMultipleRowDeselect (rows: UserDataset[]): void {
    if (!rows.length) return;
    const { selectedRows } = this.state;
    const deselectedIds: number[] = rows.map((row: UserDataset) => row.id);
    const newSelection = selectedRows.filter(id => !deselectedIds.includes(id));
    this.setState({ selectedRows: newSelection });
  }

  onSort (column: MesaColumn, direction: string): void {
    const key: string = column.key;
    const { state } = this;
    const { setSortColumnKey, setSortDirection } = MesaState;
    const updatedState = setSortDirection(setSortColumnKey(state, key), direction);
    this.setState(updatedState);
  }

  getEventHandlers () {
    return {
      onSort: this.onSort,
      onRowSelect: this.onRowSelect,
      onRowDeselect: this.onRowDeselect,
      onMultipleRowSelect: this.onMultipleRowSelect,
      onMultipleRowDeselect: this.onMultipleRowDeselect
    };
  }

  getTableActions () {
    return [
      {
        callback: (rows: UserDataset[]) => {
          alert('affecting ' + rows.map(({ id }) => id).join(', '));
        },
        element: (
          <button className="btn btn-info">
            Share Datasets <Icon fa="share-alt right-side"/>
          </button>
        ),
        selectionRequired: true
      },
      {
        callback: (rows: UserDataset[]) => {
          alert('affecting ' + rows.map(({ id }) => id).join(', '));
        },
        element: (
          <button className="btn btn-error">
            Remove <Icon fa="trash-o right-side"/>
          </button>
        ),
        selectionRequired: true
      },
    ];
  }

  getTableOptions () {
    const { isRowSelected } = this;
    return {
      title: 'My Datasets',
      showToolbar: true,
      isRowSelected
    };
  }

  filterAndSortRows (rows: UserDataset[]): UserDataset[] {
    const { searchTerm, uiState } = this.state;
    const sort: MesaSortObject = uiState.sort;
    if (searchTerm && searchTerm.length) rows = this.filterRowsBySearchTerm([ ...rows ], searchTerm);
    if (sort.columnKey.length) rows = this.sortRowsByColumnKey([ ...rows ], sort);
    return [...rows];
  }

  filterRowsBySearchTerm (rows: UserDataset[], searchTerm?: string): UserDataset[] {
    if (!searchTerm || !searchTerm.length) return rows;
    return rows.filter((dataset: UserDataset) => {
      const searchableRow: string = JSON.stringify(dataset).toLowerCase();
      return searchableRow.indexOf(searchTerm.toLowerCase()) !== -1;
    });
  }

  getColumnSortValueMapper (columnKey: string|null) {
    if (columnKey === null) return (data: any) => data;
    switch (columnKey) {
      case 'type':
        return (data: any, index: number): string => data.type.display;
      default:
        return (data: any, index: number) => {
          return typeof data[columnKey] !== 'undefined'
            ? data[columnKey]
            : null
        }
    };
  }

  sortRowsByColumnKey (rows: UserDataset[], sort: MesaSortObject): UserDataset[] {
    const direction: string = sort.direction;
    const columnKey: string = sort.columnKey;
    const mapValue = this.getColumnSortValueMapper(columnKey);
    const sorted = [ ...rows ].sort(MesaUtils.sortFactory(mapValue));
    return direction === 'asc'
      ? sorted
      : sorted.reverse();
  }

  render () {
    const { userDatasets, history, user } = this.props;
    const { uiState, selectedRows, searchTerm } = this.state;

    console.info('Datasets:', userDatasets);

    const rows = userDatasets;
    const actions = this.getTableActions();
    const options = this.getTableOptions();
    const eventHandlers = this.getEventHandlers();
    const filteredRows = this.filterAndSortRows(userDatasets);

    const tableState = {
      rows,
      filteredRows,
      options,
      actions,
      selectedRows,
      eventHandlers,
      columns: this.getColumns(),
      uiState: {
        ...uiState,
        emptinessCulprit: userDatasets.length
          ? 'search'
          : null
        },
    };

    return (
      <Mesa state={MesaState.create(tableState)}>
        <h1 className="UserDatasetList-Title">My Datasets</h1>
        <SearchBox
          placeholderText="Search Datasets"
          searchTerm={searchTerm}
          onSearchTermChange={this.onSearchTermChange}
        />
      </Mesa>
    )
  }
};

export default wrappable(UserDatasetList);
