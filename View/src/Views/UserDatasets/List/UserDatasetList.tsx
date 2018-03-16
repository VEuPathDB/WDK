import React from 'react';
import { escape } from 'lodash';
import { History } from 'history';
import { Mesa, MesaState } from 'mesa';

import { User } from 'Utils/WdkUser';
import { wrappable } from 'Utils/ComponentUtils';
import { bytesToHuman } from 'Utils/Converters';
import { UserDataset, UserDatasetMeta } from 'Utils/WdkModel';

import Link from 'Components/Link';
import Icon from 'Components/Icon/IconAlt';
import Loading from 'Components/Loading/Loading';
import TextBox from 'Components/InputControls/TextBox';
import DataTable from 'Components/DataTable/DataTable';
import { MesaColumn, MesaDataCellProps } from 'Core/CommonTypes';

interface Props {
  user: User;
  userDatasets: UserDataset[];p
  history: History;
};

interface State {
  selectedRows: number[];
  uiState?: {
    sort?: {
      columnKey?: string;
      direction?: string;
    }
  };
  editingCache: any;
}

class UserDatasetList extends React.Component <Props, State> {
  state: State;
  props: Props;

  constructor (props: Props) {
    super(props);
    this.state = {
      selectedRows: [],
      uiState: { sort: {} },
      editingCache: {}
    };

    this.onSort = this.onSort.bind(this);
    this.getColumns = this.getColumns.bind(this);
    this.onRowSelect = this.onRowSelect.bind(this);
    this.onRowDeselect = this.onRowDeselect.bind(this);
    this.isRowSelected = this.isRowSelected.bind(this);
    this.getEventHandlers = this.getEventHandlers.bind(this);
    this.onMultipleRowSelect = this.onMultipleRowSelect.bind(this);
    this.onMultipleRowDeselect = this.onMultipleRowDeselect.bind(this);
  }

  isRowSelected (row: UserDataset): boolean {
    const id: number = row.id;
    const { selectedRows } = this.state;
    return selectedRows.includes(id);
  }

  renderEditableNameField (cellProps: MesaDataCellProps) {
    const row: UserDataset = cellProps.row;
    const id: number = row.id;
    const name: string = row.meta.name;
    const editing: boolean = this.state.
    return (
      <div>
        <Link to={`/workspace/datasets/${id}`}>
          {name}  <span className="faded">({id})</span>
        </Link>
        
      </div>
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
        renderCell: (cellProps: MesaDataCellProps) => {
          const row: UserDataset = cellProps.row;
          const meta: UserDatasetMeta = row.meta;
          const summary: string = meta.summary;
          return escape(summary);
        }
      },
      {
        key: 'status',
        className: 'StatusColumn',
        name: 'Status',
        style: {
          textAlign: 'center'
        },
        renderCell: (cellProps: MesaDataCellProps) => {
          const row: UserDataset = cellProps.row;
          const { isInstalled } = row;
          return isInstalled
            ? <Icon fa="check-circle"/>
            : <Icon fa="times-cirlce"/>;
        }
      },
      {
        key: 'type',
        name: 'Type',
        renderCell: (cellProps: MesaDataCellProps) => {
          const row: UserDataset = cellProps.row;
          const { display, version } = row.type;
          return <span>{display} <span className="faded">({version})</span></span>
        }
      },
      {
        key: 'owner',
        name: 'Owner',
        renderCell: (cellProps: MesaDataCellProps) => {
          const row: UserDataset = cellProps.row;
          const { owner, ownerUserId } = row;
          return ownerUserId === user.id
            ? <span className="faded">You</span>
            : <span>{owner}</span>
        }
      },
      {
        key: 'created',
        name: 'Created',
        renderCell: (cellProps: MesaDataCellProps) => {
          const row: UserDataset = cellProps.row;
          const { created } = row;
          const formattedDate = (new Date(created)).toLocaleDateString();
          return (
            <span>{formattedDate}</span>
          )
        }
      },
      {
        key: 'datafiles',
        name: 'File Count',
        renderCell: (cellProps: MesaDataCellProps) => {
          const row: UserDataset = cellProps.row;
          const fileCount = row.datafiles.length;
          return fileCount;
        }
      },
      {
        key: 'size',
        name: 'Size / Quota Usage',
        sortable: true,
        style: { textAlign: 'right' },
        renderCell: (cellProps: MesaDataCellProps) => {
          const row: UserDataset = cellProps.row;
          const size: number = row.size;
          const percent: number = row.percentQuotaUsed;
          return (
            <span>{bytesToHuman(size)}{percent !== null ? ` (${percent}%)` : ''}</span>
          );
        }
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
    const newSelection: number[] = selectedRows.filter(selectedId => selectedId === id);
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
    const newSelection = rows.reduce((selection: number[], dataset: UserDataset) => {
      const id: number = dataset.id;
      return selection.includes(id)
        ? selection
        : [ ...selection, id ];
    }, selectedRows);
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

  render () {
    const { userDatasets, history, user } = this.props;
    const { uiState, selectedRows } = this.state;
    const eventHandlers = this.getEventHandlers();

    const tableState = {
      uiState,
      selectedRows,
      eventHandlers,

      options: {},
      rows: userDatasets,
      columns: this.getColumns(),
    };

    console.info('UDs', userDatasets);
    return <Mesa state={MesaState.create(tableState)}/>
  }
};

export default wrappable(UserDatasetList);
