import React, { Component } from 'react';
import { escape, orderBy } from 'lodash';
import { withRouter } from 'react-router';
import { wrappable } from '../utils/componentUtils';
import { Mesa, Utils as MesaUtils } from 'mesa';

import 'mesa/dist/css/mesa.css';

import RecordLink from './RecordLink';
import Checkbox from './Checkbox';
import TextBox from './TextBox';
import TextArea from './TextArea';
import Tooltip from './Tooltip';
import Icon from './IconAlt';
import RealTimeSearchBox from './RealTimeSearchBox';
import NumberRangeSelector from './NumberRangeSelector';
import DateSelector from './DateSelector';
import DateRangeSelector from './DateRangeSelector';
import BannerList from './BannerList';

/**
 * Provides the favorites listing page.  The component relies entirely on its properties.
 */
class FavoritesList extends Component {
  constructor (props) {
    super(props);

    this.renderIdCell = this.renderIdCell.bind(this);
    this.renderGroupCell = this.renderGroupCell.bind(this);
    this.renderTypeCell = this.renderTypeCell.bind(this);
    this.renderNoteCell = this.renderNoteCell.bind(this);

    this.handleEditClick = this.handleEditClick.bind(this);
    this.handleCellChange = this.handleCellChange.bind(this);
    this.handleCellSave = this.handleCellSave.bind(this);
    this.handleCellCancel = this.handleCellCancel.bind(this);
    this.handleRowDelete = this.handleRowDelete.bind(this);
    this.handleSearchTermChange = this.handleSearchTermChange.bind(this);

    this.getRecordClassByName = this.getRecordClassByName.bind(this);

    this.handleUndoDelete = this.handleUndoDelete.bind(this);
    this.handleBannerClose = this.handleBannerClose.bind(this);
    this.onRowSelect = this.onRowSelect.bind(this);
    this.onRowDeselect = this.onRowDeselect.bind(this);
    this.onSortChange = this.onSortChange.bind(this);

    this.getTableActions = this.getTableActions.bind(this);
    this.getTableOptions = this.getTableOptions.bind(this);
    this.getTableColumns = this.getTableColumns.bind(this);

    this.state = {
      banners: [],
      selectedRows: [],
      uiState: {
        sort: {
          column: null,
          direction: 'asc'
        }
      }
    };
  }

  createDeletedBanner (selection) {
    if (!selection || !selection.length) return;
    const { banners } = this.state;
    const bannerId = selection
      .map(s => s.displayName)
      .join('-');

    const output = {
      id: bannerId,
      type: 'success',
      message: null
    };

    const undoDelete = () => {
      console.log('undoing delete for...', selection);
      selection.forEach(_row => this.handleUndoDelete(_row));
      let bannerList = [...this.state.banners];
      let idx = bannerList.findIndex(banner => banner.id === bannerId);
      if (idx >= 0) {
        bannerList.splice(idx, 1);
        this.setState({ banners: bannerList });
      };
    }

    if (selection.length === 1) {
      let deleted = selection[0];
      output.message = (
        <span>
          <b>{deleted.displayName}</b> was removed from your favorites.
          <a onClick={undoDelete}>Undo <Icon fa="undo" /></a>
        </span>
      );
    } else {
      output.message = (
        <span>
          <b>{selection.length} records</b> were removed from your favorites.
          <a onClick={undoDelete}>Undo <Icon fa="undo" /></a>
        </span>
      );
    }

    banners.push(output);
    this.setState({ banners });
  }

  handleBannerClose (index, banner) {
    const { banners } = this.state;
    banners.splice(index, 1);
    this.setState({ banners });
  }

  handleSearchTermChange(value) {
    const { favoritesEvents } = this.props;
    favoritesEvents.searchTerm(value);
  }

  //  RENDERERS ===============================================================

  renderEmptyState () {
    return (
      <div className="no-rows empty-message">
        Your favorites page is currently empty. To add items to your favorites simply click on the favorites icon in a record page.
        If you have favorites, you may have filtered them all out with too restrictive a search criterion.
      </div>
    );
  }

  renderIdCell ({ key, value, row, column }) {
    let { recordClassName, primaryKey, displayName } = row;
    let recordClass = this.getRecordClassByName(recordClassName);
    let style = { whiteSpace: 'normal' };
    return (
      <div style={style}>
        <RecordLink recordClass={recordClass} recordId={primaryKey}>
          {displayName}
        </RecordLink>
      </div>
    );
  }

  renderGroupCell ({ key, value, row, rowIndex, columnIndex, column }) {
    const { editCoordinates, editValue } = this.props;
    const normalStyle = { display: 'flex', whiteSpace: 'normal' };
    const editStyle = { marginLeft: 'auto', paddingRight: '1em', cursor: 'pointer' };
    const isBeingEdited = (editCoordinates && editCoordinates.row === rowIndex && editCoordinates.column === columnIndex);

    return isBeingEdited ? (
      <div className="editor-cell">
        <TextBox
          value={editValue}
          onKeyPress={(e) => this.handleEnterKey(e, column.key)}
          onChange={(newValue) => this.handleCellChange(newValue)}
          maxLength='50'
          size='20'
        />
        <Icon
          fa="check-circle action-icon save-icon"
          onClick={() => this.handleCellSave(column.key)}
        />
        <Icon
          fa="times action-icon cancel-icon"
          onClick={() => this.handleCellCancel()}
        />
      </div>
    ) : (
      <div style={normalStyle}>
        <div>
          {value ? escape(value) : <span className="faded">No project set.</span>}
        </div>
        <div style={editStyle}>
          <a
            onClick={() => this.handleEditClick(rowIndex, columnIndex, key, row, value)}
            className="edit-link"
            title="Edit This Favorite's Project Grouping"
          >
            <Icon fa="pencil" />
          </a>
        </div>
      </div>
    );
  }

  renderTypeCell ({ key, value, row, column }) {
    let type = this.getRecordClassByName(value);
    type = type ? type.displayName : 'Unknown';
    return (
      <div>
        {type}
      </div>
    );
  }

  renderNoteCell ({ key, value, row, rowIndex, column, columnIndex }) {
    const { editCoordinates, editValue } = this.props;
    const editContainerStyle = { display: 'flex', whiteSpace: 'normal' };
    const editStyle = { marginLeft: 'auto', paddingRight: '1em', cursor: 'pointer' };
    const isBeingEdited = (editCoordinates && editCoordinates.row === rowIndex && editCoordinates.column === columnIndex);

    return isBeingEdited ? (
      <div className="editor-cell">
        <TextArea
          value={editValue}
          onChange={(newValue) => this.handleCellChange(newValue)}
          maxLength="200"
          cols="50"
          rows="4"
        />
        <Icon
          fa="check-circle action-icon save-icon"
          onClick={() => this.handleCellSave(key)}
        />
        <Icon
          fa="times action-icon cancel-icon"
          onClick={() => this.handleCellCancel()}
        />
      </div>
    ) : (
      <div style={editContainerStyle}>
        <div>
          {value ? escape(value) : <span className="faded">This favorite has no notes.</span>}
        </div>
        <div style={editStyle}>
          <a
            onClick={() => this.handleEditClick(rowIndex, columnIndex, key, row, value)}
            className="edit-link"
            title="Edit This Favorite's Project Grouping"
          >
            <Icon fa="pencil" />
          </a>
        </div>
      </div>
    )
  }

  // Table event handlers =====================================================

  onRowSelect ({ displayName }) {
    let { selectedRows } = this.state;
    if (selectedRows.includes(displayName)) return;
    selectedRows.push(displayName);
    this.setState({ selectedRows });
  }

  onRowDeselect ({ displayName }) {
    let { selectedRows } = this.state;
    let index = selectedRows.indexOf(displayName);
    if (index < 0) return;
    selectedRows.splice(index, 1);
    this.setState({ selectedRows })
  }

  onSortChange ({ key }, direction) {
    const sort = { columnKey: key, direction };
    const uiState = { sort };
    this.setState({ uiState });
  }

  // Table config generators =================================================

  getTableActions () {
    return [
      {
        selectionRequired: true,
        element (selection) {
          return (
            <button className="btn btn-error">
              <Icon fa="trash" /> Remove {selection.length ? selection.length + ' favorite' + (selection.length === 1 ? '' : 's') : ''}
            </button>
          );
        },
        handler: (rowData) => {
          this.handleRowDelete(rowData);
        },
        callback: (selection) => {
          this.createDeletedBanner(selection);
          let selectedFavorites = [];
          this.setState({ selectedFavorites });
        }
      }
    ];
  }

  getTableOptions () {
    const { searchBoxPlaceholder } = this.props;
    const { selectedRows } = this.state;

    return {
      title: 'Favorites',
      editableColumns: false,
      searchPlaceholder: searchBoxPlaceholder,
      isRowSelected ({ displayName }) {
        return selectedRows.includes(displayName);
      }
    };
  }

  getTableColumns () {
    const { renderIdCell, renderTypeCell, renderNoteCell, renderGroupCell } = this;
    return [
      {
        key: 'displayName',
        name: 'ID',
        renderCell: renderIdCell,
        width: '130px',
        sortable: true
      },
      {
        key: 'recordClassName',
        name: 'Type',
        renderCell: renderTypeCell,
        width: '200px',
        sortable: true
      },
      {
        key: 'description',
        name: 'Notes',
        renderCell: renderNoteCell,
        width: '450px'
      },
      {
        key: 'group',
        name: 'Project',
        renderCell: renderGroupCell,
        width: '250px'
      }
    ];
  }

  render () {
    let { banners, uiState } = this.state;
    let { recordClasses, list, filteredList, searchText, searchBoxPlaceholder, searchBoxHelp, user } = this.props;
    let { renderIdCell, renderTypeCell, renderNoteCell, renderGroupCell, onRowSelect, onRowDeselect, onSortChange } = this;

    let { sort } = uiState;
    filteredList = (sort.columnKey ? MesaUtils.textSort(filteredList, sort.columnKey, sort.direction === 'asc') : filteredList);

    const columns = this.getTableColumns();
    const options = this.getTableOptions();
    const actions = this.getTableActions();
    const eventHandlers = {
      onRowSelect,
      onRowDeselect,
      onSort: onSortChange
    };


    const emptinessCulprit = (list.length && !filteredList.length ? 'search' : null);
    uiState = Object.assign({}, uiState, { emptinessCulprit });

    console.log('using uiState', uiState);

    const mesaProps = { rows: filteredList, columns, options, actions, uiState, eventHandlers };

    if (!recordClasses) return null;
    if (user.isGuest) return (<div className="empty-message">You must login first to use favorites</div>);

    return (
      <div className="wdk-Favorites">
        {!banners.length ? null : <BannerList onClose={this.handleBannerClose} banners={banners} />}

        <h1>Favorites</h1>

        <RealTimeSearchBox
          className="favorites-search-field"
          autoFocus={false}
          searchTerm={searchText}
          onSearchTermChange={this.handleSearchTermChange}
          placeholderText={searchBoxPlaceholder}
          helpText={searchBoxHelp}
        />

        <Mesa {...mesaProps} />
      </div>
    );
  }

  /**
   * Calls appropriate handler when any edit link is pressed.  Because the switch between the cell contents and the
   * in-line edit form can alter row height, the CellMeasurer cache is cleared.
   * @param rowIndex
   * @param columnIndex
   * @param dataKey
   * @param rowData
   * @param cellData
   * @private
   */
  handleEditClick (rowIndex, columnIndex, dataKey, rowData, cellData) {
    this.props.favoritesEvents.editCell({
      coordinates: {
        row: rowIndex,
        column: columnIndex
      },
      key: dataKey,
      value: cellData,
      rowData: rowData
    });
  }

  /**
   * Calls appropriate handler when changes are made to content during an in-line edit.
   * @param value - edited value
   * @private
   */
  handleCellChange (value) {
    this.props.favoritesEvents.changeCell(value);
  }

  /**
   * Calls appropriate handler when an in-line edit save button is clicked.  A new favorite is sent back to the handler
   * with the original favorite information updated with the edited value.  Again, because this event collapses the
   * in-line edit form, which can alter row height, the CellMeasurer cache is cleared.
   * @param dataKey - the property of the favorite that was edited (group or note here)
   * @private
   */
  handleCellSave (dataKey) {
    let favorite = Object.assign({}, this.props.existingFavorite, {[dataKey] : this.props.editValue});
    this.props.favoritesEvents.saveCellData(favorite);
  }

  /**
   * Calls appropriate handler when the in-line edit changes are discarded.  Again, because this event collapses the
   * in-line edit form, which can alter row height, the CellMeasure cache is cleared.
   * @private
   */
  handleCellCancel() {
    this.props.favoritesEvents.cancelCellEdit();
  }

  /**
   * A workaround that watches an cell input (specifically the group editor) and, when "enter" is pressed, submits
   * the relevant cell for saving.
   * @param e - Keypress event
   * @param dataKey - cell data key to pass along for saving
  **/
  handleEnterKey (e, dataKey) {
    if (e.key !== 'Enter' || !dataKey) return;
    this.handleCellSave(dataKey);
  }

  /**
   * Calls appropriate handler when the delete button for a favorite is clicked.  The rowData carries all the
   * favorite information.  Not sure whether the cache needs to be cleared in this instance.
   * @param rowData
   * @private
   */
  handleRowDelete (rowData) {
    this.props.favoritesEvents.deleteRow(rowData);
    let { displayName } = rowData;
    let { selectedRows } = this.state;
    if (selectedRows.includes(displayName)) this.onRowDeselect(rowData);
  }

  handleUndoDelete (row) {
    this.props.favoritesEvents.addRow(row);
  }

  getDataKeyTooltip (dataKey) {
    switch (dataKey) {
      case 'display':
        return 'This links back to your favorite';
      case 'recordClassName':
        return 'This is the type of your favorite';
      case 'note':
        return 'Use this column to add notes (click edit to change this field).';
      case 'group':
        return 'Organize your favorites by project names';
      default:
        return '';
    };
  }

  getRecordClassByName (recordClassName) {
    let { recordClasses } = this.props;
    return recordClasses.find((recordClass) => recordClass.name === recordClassName);
  }
}

export default wrappable(withRouter(FavoritesList));
