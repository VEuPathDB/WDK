import React, {Component} from 'react';
import { escape, orderBy } from 'lodash';
import { withRouter } from 'react-router';
import { wrappable } from '../utils/componentUtils';
import RecordLink from './RecordLink';
import Checkbox from './Checkbox';
import TextBox from './TextBox';
import TextArea from './TextArea';
import Tooltip from './Tooltip';
import Icon from './IconAlt';
import NumberRangeSelector from './NumberRangeSelector';
import DateSelector from './DateSelector';
import DateRangeSelector from './DateRangeSelector';
import RealTimeSearchBox from './RealTimeSearchBox';
import SelectionToolbar from './SelectionToolbar';
import { AutoSizer, Table, Column, CellMeasurer, CellMeasurerCache, SortDirection, SortIndicator } from 'react-virtualized';
import 'react-virtualized/styles.css';

import BannerList from './BannerList';

/**
 * Provides the favorites listing page.  The component relies entirely on its properties.
 */
class FavoritesList extends Component {
  constructor (props) {
    super(props);

    this.renderHeaderCell = this.renderHeaderCell.bind(this);
    this.renderGroupCell = this.renderGroupCell.bind(this);
    this.renderNoteCell = this.renderNoteCell.bind(this);
    this.renderSelectionCell = this.renderSelectionCell.bind(this);
    this.renderIdCell = this.renderIdCell.bind(this);
    this.renderTypeCell = this.renderTypeCell.bind(this);
    this.renderSelectHeaderCell = this.renderSelectHeaderCell.bind(this);

    this.handleSort = this.handleSort.bind(this);
    this.handleEditClick = this.handleEditClick.bind(this);
    this.handleCellChange = this.handleCellChange.bind(this);
    this.handleCellSave = this.handleCellSave.bind(this);
    this.handleCellCancel = this.handleCellCancel.bind(this);
    this.handleRowDelete = this.handleRowDelete.bind(this);
    this.handleSearchTermChange = this.handleSearchTermChange.bind(this);

    this.getRecordType = this.getRecordType.bind(this);
    this.getRecordClassByName = this.getRecordClassByName.bind(this);

    this.handleUndoDelete = this.handleUndoDelete.bind(this);
    this.handleBannerClose = this.handleBannerClose.bind(this);

    this.toggleRowSelected = this.toggleRowSelected.bind(this);

    this.state = {
      selectedFavorites: [],
      banners: []
    };

    // Dynamic row heights are determined by CellMeasurer components.
    this._cache = new CellMeasurerCache({
      fixedWidth: true,
      minHeight: 20
    });
  }

  createDeletedBanner (selection) {
    if (!selection || !selection.length) return;
    const { banners } = this.state;
    const bannerId = selection.map(s => s.displayName).join('-');

    const output = {
      id: bannerId,
      type: 'success',
      message: null
    };

    const undoDelete = () => {
      console.log('undoing delete for...', selection);
      selection.forEach(_row => this.handleUndoDelete(_row));
      let bannerList = [].concat(this.state.banners);
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

  getToolbarActions () {
    return [
      {
        element: <button className="btn btn-error"><Icon fa="trash" /> Remove</button>,
        handler: (rowData) => {
          this.handleRowDelete(rowData);
        },
        callback: (selection) => {
          console.log('running delete callback. selection is', selection);
          this.createDeletedBanner(selection);
          let selectedFavorites = [];
          this.setState({ selectedFavorites });
        }
      }
    ];
  }

  handleBannerClose (index, banner) {
    const { banners } = this.state;
    banners.splice(index, 1);
    this.setState({ banners });
  }

  renderEmptyState () {
    return (
      <div className="no-rows empty-message">
        Your favorites page is currently empty. To add items to your favorites simply click on the favorites icon in a record page.
        If you have favorites, you may have filtered them all out with too restrictive a search criterion.
      </div>
    );
  }

  render () {
    // A race condition is possible in which the global data may not be in place when the page is rendered.  This
    // avoids that issue.
    if (this.props.recordClasses == null) return null;

    const { sortBy, sortDirection } = this.props;

    // The list shown to the user is always a 'filtered' list, which may in fact be identical to the unfiltered list.
    // The purpose is to allow the user to sort and edit the filtered version of the list readily.
    const list = this.props.filteredList;
    const sortedList = orderBy(list, [sortBy], [sortDirection === SortDirection.ASC ? 'asc' : 'desc']);

    // Identifies the table row data by index
    const rowGetter = ({index}) => sortedList[index];

    // Distinguishes row styles between the table header row and table body rows
    const rowStyle = ({index}) => index === -1 ? "wdk-VirtualizedTableHeaderRow" : "wdk-VirtualizedTableBodyRow";

    let { selectedFavorites, banners } = this.state;

    return (
      <div className="wdk-Favorites">
        <h1 className="page-title">Favorites</h1>
        {this.props.user.isGuest ? <div className="empty-message"> You must login first to use favorites</div> :
        <div>
          <BannerList onClose={this.handleBannerClose} banners={banners} />
          <SelectionToolbar selection={selectedFavorites} actions={this.getToolbarActions()}>
            <RealTimeSearchBox
              className="favorites-search-field"
              autoFocus={false}
              searchTerm={this.props.searchText}
              onSearchTermChange={this.handleSearchTermChange}
              placeholderText={this.props.searchBoxPlaceholder}
              helpText={this.props.searchBoxHelp}
            />
          </SelectionToolbar>

          <Table
            width={960}
            height={600}
            headerHeight={20}
            rowHeight={this._cache.rowHeight}
            rowCount={list.length}
            rowGetter={rowGetter}
            noRowsRenderer={this.renderEmptyState}
            sort={this.handleSort}
            sortBy={sortBy}
            sortDirection={sortDirection}
            className="wdk-VirtualizedTable"
            rowClassName={rowStyle}
            headerClassName="wdk-VirtualizedTableHeaderCell"
          >
            <Column
              headerRenderer={this.renderSelectHeaderCell}
              label=""
              dataKey='selection'
              width={30}
              className="wdk-VirtualizedTableCell"
              cellRenderer={this.renderSelectionCell}
            />
            <Column
              headerRenderer={this.renderHeaderCell}
              label="ID"
              dataKey='displayName'
              width={130}
              className="wdk-VirtualizedTableCell"
              cellRenderer={this.renderIdCell}
            />
            <Column
              headerRenderer={this.renderHeaderCell}
              width={200}
              label='Type'
              dataKey='recordClassName'
              className="wdk-VirtualizedTableCell"
              cellRenderer={this.renderTypeCell}
            />
            <Column
              headerRenderer={this.renderHeaderCell}
              width={450}
              label='Notes'
              dataKey='description'
              className="wdk-VirtualizedTableCell"
              cellRenderer={this.renderNoteCell}
            />
            <Column
              headerRenderer={this.renderHeaderCell}
              width={250}
              label='Project'
              dataKey='group'
              className="wdk-VirtualizedTableCell"
              cellRenderer={this.renderGroupCell}
            />
          </Table>
        </div>
      }
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
    this._cache.clearAll();
    this.props.favoritesEvents.editCell({
      coordinates: {
        row: rowIndex,
        column:columnIndex
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
    this._cache.clearAll();
    let favorite = Object.assign({}, this.props.existingFavorite, {[dataKey] : this.props.editValue});
    this.props.favoritesEvents.saveCellData(favorite);
  }

  /**
   * Calls appropriate handler when the in-line edit changes are discarded.  Again, because this event collapses the
   * in-line edit form, which can alter row height, the CellMeasure cache is cleared.
   * @private
   */
  handleCellCancel() {
    this._cache.clearAll();
    this.props.favoritesEvents.cancelCellEdit();
  }

  /**
   * A workaround that watches an cell input (specifically the group editor) and, when "enter" is pressed, submits
   * the relevant cell for saving.
   * @param e - Keypress event
   * @param dataKey - cell data key to pass along for saving
  **/
  _handleEnterKey (e, dataKey) {
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
    this._cache.clearAll();
    this.props.favoritesEvents.deleteRow(rowData);
  }

  handleUndoDelete (row) {
    this._cache.clearAll();
    this.props.favoritesEvents.addRow(row);
  }

  /**
   * Calls appropriate handler when the search term is edited.  The search term is forwarded to the handler.  Again,
   * not sure whether the cache needs to be cleared in this instance.
   * @param value
   * @private
   */
  handleSearchTermChange(value) {
    this._cache.clearAll();
    this.props.favoritesEvents.searchTerm(value);
  }

  /**
   * Calls appropriate handler when a column is sorted.  The column to be sorted and the sort direction are forwarded
   * to the handler.  Since the rows are juggled, the cache may need to be cleared.
   * @param sortBy
   * @param sortDirection
   * @private
   */
  handleSort ({ sortBy, sortDirection }) {
    this._cache.clearAll();
    this.props.favoritesEvents.sortColumn(sortBy, sortDirection);
  }

  /**
   * Renders the table cell containing the display version of the favorite entity's id.  A cell
   * measurer is applied because some of the id displays are lengthy
   * @param cellData - the id's diplay
   * @param columnIndex
   * @param dataKey
   * @param parent
   * @param rowData - all the favorite information
   * @param rowIndex
   * @returns {XML}
   * @private
   */
  renderIdCell ({ cellData, columnIndex, dataKey, parent, rowData, rowIndex }) {
    let recordClass = this.getRecordClassByName(rowData.recordClassName);
    return (
      <CellMeasurer
        cache={this._cache}
        columnIndex={columnIndex}
        key={dataKey}
        parent={parent}
        rowIndex={rowIndex}
      >
        <div style={{ whiteSpace:'normal' }}>
          <RecordLink recordClass={recordClass} recordId={rowData.primaryKey}>{rowData.displayName}</RecordLink>
        </div>
      </CellMeasurer>
    );
  }

  toggleRowSelected (rowData) {
    let { selectedFavorites } = this.state;
    const selectionIndex = selectedFavorites.findIndex(favorite => rowData.displayName === favorite.displayName);
    if (selectionIndex >= 0) {
      selectedFavorites.splice(selectionIndex, 1);
    } else {
      selectedFavorites.push(rowData);
    };
    this.setState({ selectedFavorites });
  }

  renderSelectHeaderCell () {
    const { selectedFavorites } = this.state;
    const favoriteList = this.props.filteredList;
    const allAreSelected = favoriteList.length && favoriteList.every(fav => selectedFavorites.includes(fav));
    const replacement = allAreSelected ? [] : [].concat(favoriteList);
    const handler = () => this.setState({ selectedFavorites: replacement });
    return <Checkbox value={allAreSelected} onChange={handler} />;
  }

  /**
   * Renders the checkbox selection cell for bulk selecting/affecting rows
  */
  renderSelectionCell ({ rowData }) {
    const { selectedFavorites } = this.state;
    const isSelected = !!selectedFavorites.find(favorite => rowData.displayName === favorite.displayName);
    const handler = () => this.toggleRowSelected(rowData);

    return <Checkbox value={isSelected} onChange={handler} />
  }

  /**
   * Renders the table cell containing the type of the favorite entity (e.g., Gene, Compound, ORF).
   * @param cellData
   * @returns {*}
   * @private
   */
  renderTypeCell({ "cellData" : cellData }) {
    return (
      this.getRecordType(cellData)
    )
  }

  /**
   * Renders the table cell containing the group of the favorite entity (which is presented to the user as an
   * opportunity to group favorites by the user's project).  When the user clicks the edit button, an in-line
   * edit form is offered.
   * @param cellData
   * @param columnIndex
   * @param dataKey
   * @param rowData
   * @param rowIndex
   * @returns {XML}
   * @private
   */
  renderGroupCell ({ cellData, columnIndex, dataKey, rowData, rowIndex }) {
    const editStyle = { marginLeft: 'auto', paddingRight: '1em', cursor: 'pointer' };
    const coords = this.props.editCoordinates;
    const value = this.props.editValue;
    if (coords && coords.row === rowIndex && coords.column === columnIndex) {
      return (
        <div className="editor-cell">
          <TextBox value={value} onKeyPress={(e) => this._handleEnterKey(e, dataKey)} onChange={(value) => this.handleCellChange(value)} maxLength='50' size='20' />
          <Icon fa="check-circle action-icon save-icon" onClick={() => this.handleCellSave(dataKey)} />
          <Icon fa="times action-icon cancel-icon" onClick={() => this.handleCellCancel()} />
        </div>
      );
    } else {
      return (
        <div style={{ display: 'flex', whiteSpace: 'normal' }}>
          <div>
            {cellData ?
              <span>{escape(cellData)}</span> :
              <span className="faded">No project set.</span>
            }
          </div>
          <div style={editStyle}>
            <a onClick={() => this.handleEditClick(rowIndex, columnIndex, dataKey, rowData, cellData)} className="edit-link" title="Edit This Favorite's Project Grouping">
              <Icon fa="pencil" />
            </a>
          </div>
        </div>
      );
    }
  }

  /**
   * Renders the table cell containg the notes of the favorite entity.  When the user clicks the edit button, an in-line
   * edit form is offered.
   * @param cellData
   * @param columnIndex
   * @param dataKey
   * @param parent
   * @param rowData
   * @param rowIndex
   * @returns {XML}
   * @private
   */
  renderNoteCell ({ cellData, columnIndex, dataKey, parent, rowData, rowIndex }) {
    const editStyle = {marginLeft: 'auto', paddingRight: '1em', cursor: 'pointer'};
    const coords = this.props.editCoordinates;
    const value = this.props.editValue;
    if (coords && coords.row === rowIndex && coords.column === columnIndex) {
     return (
       <CellMeasurer
         cache={this._cache}
         columnIndex={columnIndex}
         key={dataKey}
         parent={parent}
         rowIndex={rowIndex}
       >
       <div className="editor-cell">
           <TextArea value={value} onChange={(value) => this.handleCellChange(value)} maxLength="200" cols="50" rows="4" />
           <Icon fa="check-circle action-icon save-icon" onClick={() => this.handleCellSave(dataKey)} />
           <Icon fa="times action-icon cancel-icon" onClick={() => this.handleCellCancel()} />
         </div>
       </CellMeasurer>
     );
    } else {
      return (
        <CellMeasurer
          cache={this._cache}
          columnIndex={columnIndex}
          key={dataKey}
          parent={parent}
          rowIndex={rowIndex}
        >
          <div style={{display: 'flex', whiteSpace:'normal'}}>
            <div>
              {cellData ? <span>{escape(cellData)}</span> : <span className="faded">This favorite has no notes.</span>}
            </div>
            <div style={editStyle}>
              <a
                onClick={() => this.handleEditClick(rowIndex, columnIndex, dataKey, rowData, cellData)}
                className="edit-link"
                title="Edit This Favorite's Project Grouping"
              >
                <Icon fa="pencil" />
              </a>
            </div>
          </div>
        </CellMeasurer>
      );
    }
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

  renderHeaderCell ({ dataKey, label, sortBy, sortDirection }) {
    return (
      <Tooltip content={this.getDataKeyTooltip(dataKey)}>
        <div>
          {label}
          {sortBy === dataKey && <SortIndicator sortDirection={sortDirection}/>}
        </div>
      </Tooltip>
    );
  }

  getRecordClassByName (recordClassName) {
    return this.props.recordClasses.find((recordClass) => recordClass.name === recordClassName);
  }

  getRecordType (recordClassName) {
    let item = this.getRecordClassByName(recordClassName);
    return item ? item.displayName : 'Unknown';
  }

}

export default wrappable(withRouter(FavoritesList));
