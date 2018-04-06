import React from 'react';
import { escape } from 'lodash';
import { History } from 'history';
import {
  Mesa,
  MesaState,
  Utils as MesaUtils,
  AnchoredTooltip as Tooltip
} from 'mesa';


import './UserDatasetList.scss';
import { User } from 'Utils/WdkUser';
import moment from 'Utils/MomentUtils';
import { wrappable } from 'Utils/ComponentUtils';
import { bytesToHuman } from 'Utils/Converters';
import { UserDataset, UserDatasetMeta } from 'Utils/WdkModel';

import Link from 'Components/Link';
import Icon from 'Components/Icon/IconAlt';
import Modal from 'Components/Overlays/Modal';
import HelpIcon from 'Components/Icon/HelpIcon';
import Loading from 'Components/Loading/Loading';
import SearchBox from 'Components/SearchBox/RealTimeSearchBox';
import SaveableTextEditor from 'Components/InputControls/SaveableTextEditor';
import { textCell, normalizePercentage } from 'Views/UserDatasets/UserDatasetUtils';
import UserDatasetEmptyState from 'Views/UserDatasets/EmptyState';
import UserDatasetTutorial from 'Views/UserDatasets/UserDatasetTutorial';
import { MesaColumn, MesaDataCellProps, MesaSortObject } from 'Core/CommonTypes';
import SharingModal from 'Views/UserDatasets/Sharing/UserDatasetSharingModal';

interface Props {
  user: User;
  history: History;
  location: any;
  projectId: string;
  projectName: string;
  userDatasets: UserDataset[];
  updateUserDatasetDetail: (userDataset: UserDataset, meta: UserDatasetMeta) => any
};

interface State {
  selectedRows: number[];
  uiState: { sort: MesaSortObject; };
  searchTerm: string;
  sharingModalOpen: boolean;
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
      sharingModalOpen: false,
      searchTerm: ''
    };

    this.onRowSelect = this.onRowSelect.bind(this);
    this.onRowDeselect = this.onRowDeselect.bind(this);
    this.isRowSelected = this.isRowSelected.bind(this);
    this.onMultipleRowSelect = this.onMultipleRowSelect.bind(this);
    this.onMultipleRowDeselect = this.onMultipleRowDeselect.bind(this);

    this.onSort = this.onSort.bind(this);
    this.getColumns = this.getColumns.bind(this);
    this.isMyDataset = this.isMyDataset.bind(this);
    this.getEventHandlers = this.getEventHandlers.bind(this);
    this.filterAndSortRows = this.filterAndSortRows.bind(this);
    this.onSearchTermChange = this.onSearchTermChange.bind(this);
    this.onMetaAttributeSaveFactory = this.onMetaAttributeSaveFactory.bind(this);

    this.renderOwnerCell = this.renderOwnerCell.bind(this);
    this.renderStatusCell = this.renderStatusCell.bind(this);

    this.openSharingModal = this.openSharingModal.bind(this);
    this.closeSharingModal = this.closeSharingModal.bind(this);
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

  onMetaAttributeSaveFactory (dataset: UserDataset, attrKey: string) {
    const { meta } = dataset;
    const { updateUserDatasetDetail } = this.props;
    return (value: string) => updateUserDatasetDetail(dataset, { ...meta, [attrKey]: value });
  }

  renderStatusCell (cellProps: MesaDataCellProps) {
    const dataset: UserDataset = cellProps.row;
    const { projectId, projectName } = this.props;
    const { isInstalled, projects } = dataset;
    const isInstallable = projects.includes(projectId);
    const appNames = projects.join(', ');
    const content = !isInstallable
      ? <span>This dataset is not compatible with {projectName}.</span>
      : isInstalled
        ? (
          <span>
            The files in this dataset have been installed to <b>{projectName}</b>.<br />
            Visit this dataset's page to see how to use it in <b>{projectName}</b>.
          </span>
        ) : (
          <span>
            This dataset could not be installed to  {projectName} due to a server error.
            <br />
            Please remove this dataset and try again.
          </span>
        )
    const children = <Icon fa={!isInstallable ? 'minus-circle' : isInstalled ? 'check-circle' : 'times-circle'}/>;
    const tooltipProps = { content, children };
    return (
      <Link to={`/workspace/datasets/${dataset.id}`}>
        <Tooltip {...tooltipProps}/>
      </Link>
    );
  }

  renderOwnerCell (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const { user } = this.props;
    const { owner } = row;
    return this.isMyDataset(row)
      ? <span className="faded">Me</span>
      : <span>{owner}</span>
  }

  getColumns (): MesaColumn[] {
    const { userDatasets, user } = this.props;
    function isOwner (ownerId: number): boolean {
      return user.id === ownerId;
    };
    return [
      {
        key: 'id',
        sortable: true,
        name: 'Name / ID',
        helpText: '',
        renderCell: (cellProps: MesaDataCellProps) => {
          const dataset: UserDataset = cellProps.row;
          const saveName = this.onMetaAttributeSaveFactory(dataset, 'name');
          return (
            <SaveableTextEditor
              value={dataset.meta.name}
              multiLine={true}
              rows={2}
              onSave={saveName}
              readOnly={!isOwner(dataset.ownerUserId)}
              displayValue={(value: string) => (
                <React.Fragment>
                  <Link to={`/workspace/datasets/${dataset.id}`}>
                    {value}
                  </Link>
                  <br/>
                  <span className="faded">
                    ({dataset.id})
                  </span>
                </React.Fragment>
              )}
            />
          );
        }
      },
      {
        key: 'summary',
        name: 'Summary',
        style: { maxWidth: '300px' },
        renderCell: (cellProps: MesaDataCellProps) => {
          const dataset: UserDataset = cellProps.row;
          const saveSummary = this.onMetaAttributeSaveFactory(dataset, 'summary');
          return (
            <div style={{ display: 'block', maxWidth: '100%' }}>
              <SaveableTextEditor
                rows={Math.max(2, Math.floor(dataset.meta.summary.length / 22))}
                multiLine={true}
                onSave={saveSummary}
                value={dataset.meta.summary}
                readOnly={!isOwner(dataset.ownerUserId)}
              />
            </div>
          );
        }
      },
      {
        key: 'type',
        name: 'Type',
        sortable: true,
        renderCell: textCell('type', (datasetType: any) => {
          const display: string = datasetType.display;
          const version: string = datasetType.version;
          return (
            <span>{display} <span className="faded">({version})</span></span>
          );
        })
      },
      {
        key: 'projects',
        name: 'Projects',
        renderCell (cellProps: MesaDataCellProps) {
          const userDataset: UserDataset = cellProps.row;
          const { projects } = userDataset;
          return (
            <ul>
              {projects.map((projectName: string, index: number) => (
                <li key={index}>{projectName}</li>
              ))}
            </ul>
          )
        }
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
        renderCell: textCell('created', (created: number) => moment(created).fromNow())
      },
      {
        key: 'datafiles',
        name: 'File Count',
        renderCell: textCell('datafiles', (files: any[]) => files.length)
      },
      {
        key: 'size',
        name: 'Size',
        sortable: true,
        renderCell: textCell('size', (size: number) => bytesToHuman(size))
      },
      {
        key: 'percentQuotaUsed',
        name: 'Quota Usage',
        sortable: true,
        renderCell: textCell('percentQuotaUsed', (percent: number) => percent
          ? `${normalizePercentage(percent)}%`
          : null
        )
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
          this.openSharingModal();
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
    const { userDatasets, projectName, location } = this.props;
    const rootUrl: string = window.location.href.substring(0, window.location.href.indexOf('/app/'));
    const emptyMessage = !userDatasets.length
      ? (
        <React.Fragment>
          <p>You don't have any user datasets.</p>
          <br/>
          <small>
            <Link to="/search/dataset/AllDatasets/result">
              See <b>{projectName}</b>'s Public Datasets.
            </Link>
          </small>
        </React.Fragment>
      ) : (
        <React.Fragment>
          <p>Your search returned no results.</p>
          <br/>
          <small>
            <a onClick={() => this.setState({ searchTerm: '' })} href="#">
              Clear Search Query <Icon fa="chevron-right"/>
            </a>
          </small>
        </React.Fragment>
      );
    return {
      isRowSelected,
      showToolbar: true,
      renderEmptyState () {
        return (
          <React.Fragment>
            <UserDatasetEmptyState message={emptyMessage}/>
            <UserDatasetTutorial projectName={projectName} rootUrl={rootUrl}/>
          </React.Fragment>
        )
      }
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
    const mappedValue = this.getColumnSortValueMapper(columnKey);
    const sorted = [ ...rows ].sort(MesaUtils.sortFactory(mappedValue));
    return direction === 'asc'
      ? sorted
      : sorted.reverse();
  }

  closeSharingModal () {
    const sharingModalOpen = false;
    this.setState({ sharingModalOpen });
  }

  openSharingModal () {
    const sharingModalOpen = true;
    this.setState({ sharingModalOpen });
  }

  render () {
    const { isRowSelected } = this;
    const { userDatasets, history, user, projectName } = this.props;
    const { uiState, selectedRows, searchTerm, sharingModalOpen } = this.state;

    console.info('Datasets:', userDatasets);

    const rows = userDatasets;
    const actions = this.getTableActions();
    const options = this.getTableOptions();
    const columns = this.getColumns();
    const eventHandlers = this.getEventHandlers();
    const filteredRows = this.filterAndSortRows(userDatasets);

    const tableState = {
      rows,
      columns,
      options,
      actions,
      filteredRows,
      selectedRows,
      eventHandlers,
      uiState: {
        ...uiState,
        emptinessCulprit: userDatasets.length
          ? 'search'
          : null
        },
    };

    return (
      <div className="UserDatasetList">
        <Mesa state={MesaState.create(tableState)}>
          <h1 className="UserDatasetList-Title">
            My Datasets
            <HelpIcon>
              <div>
                As a part of your new user Workspace, you can now upload your own datasets to use in <b>{projectName}</b>.
                <ul style={{ marginTop: '10px' }}>
                  <li>This data can be used in all the same ways as our public datasets.</li>
                  <li>Easily manage how you leverage your data: push compatible data straight to <a>GBrowse</a>, with other tooling coming soon.</li>
                  <li>Share your dataset with others and receive shared data from your own colleagues.</li>
                </ul>
              </div>
            </HelpIcon>
          </h1>
          {sharingModalOpen
            ? <SharingModal
                user={user}
                datasets={rows.filter(isRowSelected)}
                deselectDataset={this.onRowDeselect}
                onClose={this.closeSharingModal}
              />
            : null
          }
          <SearchBox
            placeholderText="Search Datasets"
            searchTerm={searchTerm}
            onSearchTermChange={this.onSearchTermChange}
          />
        </Mesa>
      </div>
    )
  }
};

export default wrappable(UserDatasetList);
