import _ from 'lodash';
import React from 'react';
import FixedDataTable from 'fixed-data-table';
import Dialog from './Dialog';

/**
 * Generic table with UI features:
 *
 *   - Sort columns
 *   - Move columns
 *   - Show/Hide columns
 *   - Paging
 *
 *
 * NB: A View-Controller will need to pass handlers to this component:
 *
 *   - onSort(columnName: string, direction: string(asc|desc))
 *   - onMoveColumn(columnName: string, newPosition: number)
 *   - onShowColumns(columnNames: Array<string>)
 *   - onHideColumns(columnNames: Array<string>)
 *   - onNewPage(offset: number, numRecords: number)
 */

var $ = window.jQuery;
var { Table, Column } = FixedDataTable;
var { PropTypes } = React.PropTypes;

var sortClassMap = {
  ASC:  'ui-icon ui-icon-arrowthick-1-n',
  DESC: 'ui-icon ui-icon-arrowthick-1-s'
};

var RecordTable = React.createClass({

  propTypes: {
    meta: PropTypes.object.isRequired,
    displayInfo: PropTypes.object.isRequired,
    records: PropTypes.array.isRequired,
    onSort: PropTypes.func,
    onMoveColumn: PropTypes.func,
    onChangeColumns: PropTypes.func,
    onNewPage: PropTypes.func
  },

  getDefaultProps() {
    return {
      onSort: noop,
      onMoveColumn: noop,
      onChangeColumns: noop,
      onNewPage: noop
    };
  },

  /**
   * If this is changed, be sure to update handleAttributeSelectorClose()
   */
  getInitialState() {
    return {
      pendingVisibleAttributes: this.props.displayInfo.visibleAttributes,
      attributeSelectorOpen: false
    };
  },

  componentWillReceiveProps(nextProps) {
    this.setState({
      pendingVisibleAttributes: nextProps.displayInfo.visibleAttributes
    });
  },

  handleSort(attribute) {
    this.props.onSort(attribute);
  },

  // TODO remove
  handleChangeColumns(attributes) {
    this.props.onChangeColumns(attributes);
  },

  handleHideColumn(attribute, e) {
    e.stopPropagation(); // prevent click event from bubbling to sort handler
    var attributes = this.props.displayInfo.visibleAttributes;
    this.props.onChangeColumns(attributes.filter(attr => attr !== attribute));
  },

  handleNewPage() {
  },

  handleOpenAttributeSelectorClick() {
    this.setState({
      attributeSelectorOpen: !this.state.attributeSelectorOpen
    });
  },

  handleAttributeSelectorClose() {
    this.setState(this.getInitialState());
  },

  handleAttributeSelectorSubmit(e) {
    e.preventDefault();
    e.stopPropagation();
    this.props.onChangeColumns(this.state.pendingVisibleAttributes);
    this.setState({
      attributeSelectorOpen: false
    });
  },

  /** filter unchecked checkboxes and map to attributes */
  togglePendingAttribute() {
    var form = this.refs.attributeSelector.getDOMNode();
    var { attributes } = this.props.meta;
    var pendingVisibleAttributes = [].slice.call(form.pendingAttribute)
      .filter(a => a.checked)
      .map(a => attributes.filter(attr => attr.name === a.value)[0]);
    this.setState({ pendingVisibleAttributes });
  },

  componentDidMount() {
    var { onMoveColumn } = this.props;

    if (onMoveColumn !== noop) {
      // Only set up column reordering if a callback is provided.
      //
      // We are using jQueryUI's .sortable() method to implement
      // visual drag-n-drop of table headings for reordering columns.
      // However, we prevent jQueryUI from actually altering the DOM.
      // Instead, we:
      //   1. Get the new position of the header item (jQueryUI has actually
      //      updated the DOM at this point).
      //   2. Cancel the sort event (.sortable("cancel")).
      //   3. Call the Action to update the column order, allowing React to
      //      update the DOM when it rerenders the component.
      //
      // A future iteration may be to use HTML5's draggable, thus removing the
      // jQueryUI dependency.
      var $headerRow = $(this.refs.headerRow.getDOMNode());
      $headerRow.sortable({
        items: '> th',
        helper: 'clone',
        opacity: 0.7,
        placeholder: 'ui-state-highlight',
        stop(e, ui) {
          var { item } = ui;
          var columnName = item.data('column');
          var newPosition = item.index();
          // We want to let React update the position, so we'll cancel.
          $headerRow.sortable('cancel');
          onMoveColumn(columnName, newPosition);
        }
      });
    }
  },

  render() {
    /** creates variables: meta, records, sorting, and visibleAttributes */
    var { meta, records, displayInfo: { pagination, sorting, visibleAttributes } } = this.props;
    var visibleNames = this.state.pendingVisibleAttributes.map(a => a.name);
    var firstRec = pagination.offset + 1;
    var lastRec = Math.min(pagination.offset + pagination.numRecords, meta.count);

    return (
      <div>
        <div className="wdk-RecordTable-AttributeSelectorWrapper">
          <button onClick={this.handleOpenAttributeSelectorClick}>Add Columns</button>
          <Dialog
            modal={true}
            open={this.state.attributeSelectorOpen}
            onClose={this.handleAttributeSelectorClose}
            title="Choose columns to shoe or hide">
            <form onSubmit={this.handleAttributeSelectorSubmit} ref="attributeSelector">
              <ul className="wdk-RecordTable-AttributeSelector">
                {meta.attributes.map(attribute => {
                  var { name, displayName } = attribute;
                  return (
                    <li key={name}>
                      <input type="checkbox"
                        id={'column-select-' + name}
                        name="pendingAttribute"
                        value={name}
                        onChange={this.togglePendingAttribute}
                        checked={visibleNames.indexOf(name) > -1}/>
                      <label htmlFor={'column-select-' + name}> {displayName} </label>
                    </li>
                  );
                })}
              </ul>
              <button>Update</button>
            </form>
          </Dialog>
        </div>

        <p> Showing {firstRec} - {lastRec} of {meta.count} {meta['class']} records </p>

        <div className="wdk-RecordTable-Wrapper">
          <Table
            width={600}
            maxHeight={600}
            rowsCount={records.length}
            rowHeight={50}
            rowGetter={this.getRow}
            headerHeight={50}>

            {visibleAttributes.map((attribute, idx) => {
              var cellClassName = attribute.name + ' ' + attribute.className;
              return (
                <Column
                  label={attribute.displayName}
                  dataKey={attribute.name}
                  width={100}
                  cellClassName={cellClassName}/>
              );
            })}
          </Table>
        </div>
      </div>
    );
  }

});

/* Helper functions */

/** TODO Look up or inject custom formatters */
function formatAttribute(attribute, value) {
  switch(attribute.type) {
    case 'text': return value;
    case 'link': return (<a href={value.url}>{value.display}</a>);

    /** FIXME Throw on unknown types when we have that info from service */
    default: return value;
    /*
    default: throw new TypeError(`Unkonwn type "${attribute.type}"` +
                                 ` for attribute ${attribute.name}`);
    */
  }
}

/**
 * Function that doesn't do anything. This is the default for many
 * optional handlers. We can do an equality check as a form of feature
 * detection. E.g., if onSort === noop, then we won't enable sorting.
 */
function noop(){}

export default RecordTable;
