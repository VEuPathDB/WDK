import { pick, property } from 'lodash';
import React from 'react';
import ReactDOM from 'react-dom';
import { withRouter } from 'react-router';
import DataTable from './DataTable';
import Dialog from './Dialog';
import { wrappable } from '../utils/componentUtils';

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

let { PropTypes } = React;

/**
 * Function that doesn't do anything. This is the default for many
 * optional handlers. We can do an equality check as a form of feature
 * detection. E.g., if onSort === noop, then we won't enable sorting.
 */
function noop() {}

let AttributeSelectorItem = React.createClass({

  propTypes: {
    attribute: PropTypes.object.isRequired,
    isChecked: PropTypes.bool,
    onChange: PropTypes.func.isRequired
  },

  render() {
    let { attribute } = this.props;
    let name = attribute.name;
    let displayName = attribute.displayName;
    return (
      <li key={name}>
        <input type="checkbox"
          id={'column-select-' + name}
          name="pendingAttribute"
          value={name}
          onChange={this.props.onChange}
          disabled={!attribute.isRemovable}
          checked={this.props.isChecked}/>
        <label htmlFor={'column-select-' + name}> {displayName} </label>
      </li>
    );
  }

});

let AttributeSelector = React.createClass({

  propTypes: {
    allAttributes: PropTypes.array.isRequired,
    selectedAttributes: PropTypes.array,
    onSubmit: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired
  },

  render() {
    // only want to display displayable attributes
    let displayableAttributes = this.props.allAttributes.filter(attrib => attrib.isDisplayable);
    return (
      <form onSubmit={this.props.onSubmit}>
        <div className="wdk-AnswerTable-AttributeSelectorButtonWrapper">
          <button>Update Columns</button>
        </div>
        <ul className="wdk-AnswerTable-AttributeSelector">
          {displayableAttributes.map(this._renderItem)}
        </ul>
        <div className="wdk-AnswerTable-AttributeSelectorButtonWrapper">
          <button>Update Columns</button>
        </div>
      </form>
    );
  },

  _renderItem(attribute) {
    let isAttribute = attr => attr.name === attribute.name;
    return (
      <AttributeSelectorItem
        key={attribute.name}
        isChecked={this.props.selectedAttributes.some(isAttribute)}
        attribute={attribute}
        onChange={this.props.onChange}
      />
    );
  }

});

let AnswerTable = React.createClass({

  propTypes: {
    meta: PropTypes.object.isRequired,
    displayInfo: PropTypes.object.isRequired,
    records: PropTypes.array.isRequired,
    recordClass: PropTypes.object.isRequired,
    allAttributes: PropTypes.array.isRequired,
    visibleAttributes: PropTypes.array.isRequired,
    height: PropTypes.number.isRequired,
    router: PropTypes.object.isRequired,
    onSort: PropTypes.func,
    onMoveColumn: PropTypes.func,
    onChangeColumns: PropTypes.func,
    onNewPage: PropTypes.func,
    onRecordClick: PropTypes.func
  },

  getDefaultProps() {
    return {
      onSort: noop,
      onMoveColumn: noop,
      onChangeColumns: noop,
      onNewPage: noop,
      onRecordClick: noop
    };
  },

  /**
   * If this is changed, be sure to update handleAttributeSelectorClose()
   */
  getInitialState() {
    return Object.assign({}, this._getInitialAttributeSelectorState(), {
      columns: setVisibilityFlag(this.props.recordClass.attributes, this.props.visibleAttributes),
      data: getDataFromRecords(this.props.records, this.props.recordClass, this.props.router),
      sorting: getDataTableSorting(this.props.displayInfo.sorting)
    });
  },

  componentWillReceiveProps(nextProps) {
    this.setState({
      pendingVisibleAttributes: nextProps.visibleAttributes
    });
    if (this.props.records !== nextProps.records) {
      this.setState({ data: getDataFromRecords(nextProps.records, nextProps.recordClass, nextProps.router) });
    }
    if (this.props.visibleAttributes !== nextProps.visibleAttributes) {
      this.setState({ columns: setVisibilityFlag(nextProps.recordClass.attributes, nextProps.visibleAttributes) });
    }
    if (this.props.displayInfo.sorting !== nextProps.displayInfo.sorting) {
      this.setState({ sorting: getDataTableSorting(nextProps.displayInfo.sorting) });
    }
  },

  handleSort(datatableSorting) {
    this.props.onSort(datatableSorting.map(entry => ({
      attributeName: entry.name,
      direction: entry.direction
    })));
  },

  // TODO remove
  handleChangeColumns(attributes) {
    this.props.onChangeColumns(attributes);
  },

  handleHideColumn(name) {
    let attributes = this.props.visibleAttributes
      .filter(attr => attr.name !== name);
    this.props.onChangeColumns(attributes);
  },

  handleOpenAttributeSelectorClick() {
    this.setState({
      attributeSelectorOpen: !this.state.attributeSelectorOpen
    });
  },

  handleAttributeSelectorClose() {
    this.setState(this._getInitialAttributeSelectorState());
  },

  handleAttributeSelectorSubmit(e) {
    e.preventDefault();
    e.stopPropagation();
    this.props.onChangeColumns(this.state.pendingVisibleAttributes);
    this.setState({
      attributeSelectorOpen: false
    });
  },

  handlePrimaryKeyClick(record, event) {
    this.props.onRecordClick(record);
    event.preventDefault();
  },

  /**
   * Filter unchecked checkboxes and map to attributes
   */
  togglePendingAttribute() {
    let form = ReactDOM.findDOMNode(this.refs.attributeSelector);
    let attributes = this.props.allAttributes;
    let visibleAttributes = this.props.visibleAttributes;

    let checkedAttrs = [].slice.call(form.pendingAttribute)
      .filter(a => a.checked)
      .map(a => attributes.find(attr => attr.name === a.value));

    // Remove visible attributes that are not checked.
    // Then, concat checked attributes that are not currently visible.
    let pendingVisibleAttributes = visibleAttributes
      .filter(attr => checkedAttrs.find(p => p.name === attr.name))
      .concat(checkedAttrs.filter(attr => !visibleAttributes.find(a => a.name === attr.name)));

    this.setState({ pendingVisibleAttributes });
  },

  _getInitialAttributeSelectorState() {
    return {
      pendingVisibleAttributes: this.props.visibleAttributes,
      attributeSelectorOpen: false
    };
  },

  render() {
    // creates variables: meta, records, and visibleAttributes
    let { pendingVisibleAttributes } = this.state;
    let { allAttributes } = this.props;

    return (
      <div className="wdk-AnswerTable">

        <p className="wdk-AnswerTable-AttributeSelectorOpenButton">
          <button onClick={this.handleOpenAttributeSelectorClick}>Add / Remove Columns</button>
        </p>

        <Dialog
          modal={true}
          open={this.state.attributeSelectorOpen}
          onClose={this.handleAttributeSelectorClose}
          title="Select Columns">
          <AttributeSelector
            ref="attributeSelector"
            allAttributes={allAttributes}
            selectedAttributes={pendingVisibleAttributes}
            onSubmit={this.handleAttributeSelectorSubmit}
            onChange={this.togglePendingAttribute}
          />
        </Dialog>

        <DataTable
          columns={this.state.columns}
          data={this.state.data}
          searchable={false}
          width="100%"
          height={this.props.height}
          sorting={this.state.sorting}
          onSortingChange={this.handleSort}
        />
      </div>
    );
  }

});

export default wrappable(withRouter(AnswerTable));

/** Convert records array to DataTable format */
function getDataFromRecords(records, recordClass, router) {
  let attributeNames = recordClass.attributes
    .filter(attr => attr.isDisplayable)
    .map(attr => attr.name);

  return records.map(record => {
    let trimmedAttrs = pick(record.attributes, attributeNames);
    let recordUrl = router.createHref(`/record/${recordClass.urlSegment}/${record.id.map(property('value')).join('/')}`);
    trimmedAttrs.primary_key =
      `<a href="${recordUrl}">${trimmedAttrs.primary_key}</a>`;
    return trimmedAttrs;
  });
}

/** Convert sorting to DataTable format */
function getDataTableSorting(wdkSorting) {
  return wdkSorting.map(entry => ({
    name: entry.attributeName,
    direction: entry.direction
  }));
}

/** Convert attributes to DataTable format */
function setVisibilityFlag(attributes, visibleAttributes) {
  let visibleSet = new Set(visibleAttributes);
  return attributes
    .filter(attr => attr.isDisplayable)
    .map(attr => Object.assign({}, attr, {
      isDisplayable: visibleSet.has(attr)
    }));
}
