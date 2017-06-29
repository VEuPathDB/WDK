import React, {Component} from 'react';
import { escape, orderBy } from 'lodash';
import { withRouter } from 'react-router';
import { wrappable } from '../utils/componentUtils';
import RecordLink from './RecordLink';
import TextBox from './TextBox';
import TextArea from './TextArea';
import Tooltip from './Tooltip';
import RealTimeSearchBox from './RealTimeSearchBox';
import { Table, Column, CellMeasurer, CellMeasurerCache, SortDirection, SortIndicator } from 'react-virtualized';
import 'react-virtualized/styles.css';

/**
 * Provides the favorites listing page.  The component relies entirely on its properties.
 */
class FavoritesList extends Component {

    constructor (props) {
        super(props);

        this._headerRenderer = this._headerRenderer.bind(this);
        this._groupCellRenderer = this._groupCellRenderer.bind(this);
        this._noteCellRenderer = this._noteCellRenderer.bind(this);
        this._idCellRenderer = this._idCellRenderer.bind(this);
        this._typeCellRenderer = this._typeCellRenderer.bind(this);
        this._deleteCellRenderer = this._deleteCellRenderer.bind(this);
        this._onEditClick = this._onEditClick.bind(this);
        this._onCellChange = this._onCellChange.bind(this);
        this._onCellSave = this._onCellSave.bind(this);
        this._onCellCancel = this._onCellCancel.bind(this);
        this._onRowDelete = this._onRowDelete.bind(this);
        this._onSearchTermChange = this._onSearchTermChange.bind(this);
        this._onSort = this._onSort.bind(this);
        this._getType = this._getType.bind(this);
        this._getRecordClass = this._getRecordClass.bind(this);
        this._onUndoDelete = this._onUndoDelete.bind(this);

        // Dynamic row heights are determined by CellMeasurer components.
        this._cache = new CellMeasurerCache({
          fixedWidth: true,
          minHeight: 20
        })
    }

    render() {

        // A race condition is possible in which the global data may not be in place when the page is rendered.  This
        // avoids that issue.
        if (this.props.recordClasses == null) return null;

        const {
            sortBy,
            sortDirection
        } = this.props;

        // The list shown to the user is always a 'filtered' list, which may in fact be identical to the unfiltered list.
        // The purpose is to allow the user to sort and edit the filtered version of the list readily.
        const list = this.props.filteredList;
        const sortedList = orderBy(list, [sortBy], [sortDirection === SortDirection.ASC ? 'asc' : 'desc']);

        // Identifies the table row data by index
        const rowGetter = ({index}) => sortedList[index];

        // Distinguishes row styles between the table header row and table body rows
        const rowStyle = ({index}) => index === -1 ? "wdk-VirtualizedTableHeaderRow" : "wdk-VirtualizedTableBodyRow";

        // The html rendered when the table is empty.
        const noRowsRenderer = () =>
            <div>
                Your favorites page is currently empty. To add items to your favorites simply click on the favorites icon in a record page.
                If you have favorites, you may have filtered them all out with too restrictive a search criterion.
            </div>;


        return (
            <div>
                <h1>Favorites</h1>
                {this.props.user.isGuest ? <div> You must login first to use favorites</div> :
                <div>
                  {this.props.deletedFavorite && this.props.deletedFavorite.display ?
                  <div>
                    Favorite {this.props.deletedFavorite.display} was deleted.  Want to <a onClick = {() => this._onUndoDelete()}>undo</a>?
                  </div>
                    : ''
                  }
                <div style={{width:'300px', paddingBottom: '1em'}}>
                  <RealTimeSearchBox
                      autoFocus={false}
                      searchTerm={this.props.searchText}
                      onSearchTermChange={this._onSearchTermChange}
                      placeholderText={this.props.searchBoxPlaceholder}
                      helpText={this.props.searchBoxHelp} />
                </div>
                <Table
                    width={1025}
                    height={400}
                    headerHeight={20}
                    rowHeight={this._cache.rowHeight}
                    rowCount={list.length}
                    rowGetter={rowGetter}
                    noRowsRenderer={noRowsRenderer}
                    sort={this._onSort}
                    sortBy={sortBy}
                    sortDirection={sortDirection}
                    className="wdk-VirtualizedTable"
                    rowClassName={rowStyle}
                    headerClassName="wdk-VirtualizedTableHeaderCell"
                  >
                      <Column
                          headerRenderer={this._headerRenderer}
                          label="ID"
                          dataKey='display'
                          width={100}
                          className="wdk-VirtualizedTableCell"
                          cellRenderer={this._idCellRenderer}
                      />
                      <Column
                          headerRenderer={this._headerRenderer}
                          width={200}
                          label='Type'
                          dataKey='recordClassName'
                          className="wdk-VirtualizedTableCell"
                          cellRenderer={this._typeCellRenderer}
                      />
                      <Column
                          headerRenderer={this._headerRenderer}
                          width={450}
                          label='Notes'
                          dataKey='note'
                          className="wdk-VirtualizedTableCell"
                          cellRenderer={this._noteCellRenderer}
                      />
                      <Column
                          headerRenderer={this._headerRenderer}
                          width={250}
                          label='Project'
                          dataKey='group'
                          className="wdk-VirtualizedTableCell"
                          cellRenderer={this._groupCellRenderer}
                      />
                      <Column
                          headerRenderer={this._headerRenderer}
                          width={25}
                          label=''
                          dataKey='delete'
                          className="wdk-VirtualizedTableCell"
                          cellRenderer={this._deleteCellRenderer}
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
    _onEditClick(rowIndex, columnIndex, dataKey, rowData, cellData) {
        this._cache.clearAll();
        this.props.favoritesEvents.editCell({coordinates: {row:rowIndex, column:columnIndex}, key: dataKey, value: cellData, rowData: rowData});
    }

    /**
     * Calls appropriate handler when changes are made to content during an in-line edit.
     * @param value - edited value
     * @private
     */
    _onCellChange(value) {
        this.props.favoritesEvents.changeCell(value);
    }

    /**
     * Calls appropriate handler when an in-line edit save button is clicked.  A new favorite is sent back to the handler
     * with the original favorite information updated with the edited value.  Again, because this event collapses the
     * in-line edit form, which can alter row height, the CellMeasurer cache is cleared.
     * @param dataKey - the property of the favorite that was edited (group or note here)
     * @private
     */
    _onCellSave(dataKey) {
        this._cache.clearAll();
        let favorite = Object.assign({}, this.props.existingFavorite, {[dataKey] : this.props.editValue});
        this.props.favoritesEvents.saveCellData(favorite);
    }

    /**
     * Calls appropriate handler when the in-line edit changes are discarded.  Again, because this event collapses the
     * in-line edit form, which can alter row height, the CellMeasure cache is cleared.
     * @private
     */
    _onCellCancel() {
        this._cache.clearAll();
        this.props.favoritesEvents.cancelCellEdit();
    }

    /**
     * Calls appropriate handler when the delete button for a favorite is clicked.  The rowData carries all the
     * favorite information.  Not sure whether the cache needs to be cleared in this instance.
     * @param rowData
     * @private
     */
    _onRowDelete(rowData) {
        this._cache.clearAll();
        this.props.favoritesEvents.deleteRow(rowData);
    }

    _onUndoDelete() {
        this._cache.clearAll();
        this.props.favoritesEvents.addRow(this.props.deletedFavorite);
    }

    /**
     * Calls appropriate handler when the search term is edited.  The search term is forwarded to the handler.  Again,
     * not sure whether the cache needs to be cleared in this instance.
     * @param value
     * @private
     */
    _onSearchTermChange(value) {
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
    _onSort({sortBy, sortDirection}) {
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
    _idCellRenderer({ cellData, columnIndex, dataKey, parent, rowData, rowIndex }) {
        let recordClass = this._getRecordClass(rowData.recordClassName);
        return (
            <CellMeasurer
              cache={this._cache}
              columnIndex={columnIndex}
              key={dataKey}
              parent={parent}
              rowIndex={rowIndex}
            >
                <div style={{whiteSpace:'normal'}}>
                    <RecordLink recordClass={recordClass} recordId={rowData.id}>{cellData}</RecordLink>
                </div>
            </CellMeasurer>
        )
    }

    /**
     * Renders the table cell containing the type of the favorite entity (e.g., Gene, Compound, ORF).
     * @param cellData
     * @returns {*}
     * @private
     */
    _typeCellRenderer({ "cellData" : cellData }) {
        return (
            this._getType(cellData)
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
    _groupCellRenderer({
                         cellData,
                         columnIndex,
                         dataKey,
                         rowData,
                         rowIndex
                       }) {
        const editStyle = {marginLeft: 'auto', paddingRight: '1em', cursor: 'pointer'};
        const coords = this.props.editCoordinates;
        const value = this.props.editValue;
        if(coords && coords.row === rowIndex && coords.column === columnIndex) {
            return (
                <div>
                    <TextBox value={value} onChange={(value) => this._onCellChange(value)} maxLength='50' size='20' />
                    <input type="submit" value="Save" onClick={() => this._onCellSave(dataKey)} />
                    <input type="submit" value="Cancel" onClick={() => this._onCellCancel()} />
                </div>
            )
        }
        else {
            return (
                <div style={{'display': 'flex', 'whiteSpace':'normal'}}>
                    <div>
                       {escape(cellData)}
                    </div>
                    <div style={editStyle}>
                        <a onClick={() => this._onEditClick(rowIndex, columnIndex, dataKey, rowData, cellData)}>
                            edit
                        </a>
                    </div>
                </div>
            )
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
    _noteCellRenderer({
                  cellData,
                  columnIndex,
                  dataKey,
                  parent,
                  rowData,
                  rowIndex
                }) {
        const editStyle = {marginLeft: 'auto', paddingRight: '1em', cursor: 'pointer'};
        const coords = this.props.editCoordinates;
        const value = this.props.editValue;
        if(coords && coords.row === rowIndex && coords.column === columnIndex) {
            return (
                <CellMeasurer
                  cache={this._cache}
                  columnIndex={columnIndex}
                  key={dataKey}
                  parent={parent}
                  rowIndex={rowIndex}
                >
                    <div>
                        <TextArea value={value} onChange={(value) => this._onCellChange(value)} maxLength="200" cols="50" rows="4" />
                        <input type="submit" value="Save" onClick={() => this._onCellSave(dataKey)} />
                        <input type="submit" value="Cancel" onClick={() => this._onCellCancel()} />
                     </div>
                </CellMeasurer>
            )
        }
        else {
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
                            {escape(cellData)}
                        </div>
                        <div style={editStyle}>
                            <a onClick={() => this._onEditClick(rowIndex, columnIndex, dataKey, rowData, cellData)}>
                                edit
                            </a>
                        </div>
                    </div>
                </CellMeasurer>
            )
        }
    }

  /**
   * Renders the table cell containing the delete button.
   * @param rowData
   * @returns {XML}
   * @private
   */
    _deleteCellRenderer({ rowData }) {
        return (
            <div onClick={() => this._onRowDelete(rowData)}>X</div>
        )
    }

  /**
   * Renders the table cell containing the header for each column
   * @param dataKey
   * @param label
   * @param sortBy
   * @param sortDirection
   * @returns {XML}
   * @private
   */
    _headerRenderer({
                        dataKey,
                        label,
                        sortBy,
                        sortDirection
                    }) {
        return (
            <Tooltip content={dataKey==='display' ? 'This links back to your favorite'
              : dataKey === 'recordClassName' ? "This is the type of your favorite"
                : dataKey === 'note'? 'Use this column to add notes (click edit to change this field).'
                  : dataKey === 'group' ? 'Organize your favorites by project names'
                    : ''
              }
            >
            <div>

                {label}
                {sortBy === dataKey &&
                <SortIndicator sortDirection={sortDirection}/>
                }
            </div>
            </Tooltip>
        )
    }

    /**
     * Returns a recordClass object given the name of the record class.
     * @param recordClassName
     * @returns {any}
     * @private
     */
    _getRecordClass(recordClassName) {
        return this.props.recordClasses.find((recordClass) => recordClass.name === recordClassName);
    }

    /**
     * Returns the type display name given a record class name.  If the record class name fails to resolve to a
     * record class, the string 'Unknown' is returned.
     * @param recordClassName
     * @returns {string}
     * @private
     */
    _getType(recordClassName) {
      let item = this._getRecordClass(recordClassName);
      return item ? item.displayName : 'Unknown';
    }

}

export default wrappable(withRouter(FavoritesList));
