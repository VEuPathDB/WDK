import _ from 'lodash';
import React from 'react';
import Record from './Record';

/**
 * RecordList displays a list of Record components.
 */

const $ = window.jQuery;

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
const noop = () => {};

const PropTypes = React.PropTypes;

const sortClassMap = {
  ASC:  'ui-icon ui-icon-arrowthick-1-n',
  DESC: 'ui-icon ui-icon-arrowthick-1-s'
};

const RecordList = React.createClass({

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

  handleSort(e) {
    const attribute = _.find(this.props.meta.attributes, { name: e.target.value });
    this.props.onSort(attribute);
  },

  // TODO remove
  handleChangeColumns(attributes) {
    this.props.onChangeColumns(attributes);
  },

  handleHideColumn(attribute, e) {
    e.stopPropagation(); // prevent click event from bubbling to sort handler
    const attributes = this.props.displayInfo.visibleAttributes;
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
    const form = this.refs.attributeSelector.getDOMNode();
    const { attributes } = this.props.meta;
    const pendingVisibleAttributes = [].slice.call(form.pendingAttribute)
      .filter(a => a.checked)
      .map(a => attributes.filter(attr => attr.name === a.value)[0]);
    this.setState({ pendingVisibleAttributes });
  },

  render() {
    /** creates variables: meta, records, sorting, and visibleAttributes */
    const { meta, records, displayInfo: { pagination, sorting, visibleAttributes } } = this.props;
    const visibleNames = this.state.pendingVisibleAttributes.map(a => a.name);
    const firstRec = pagination.offset + 1;
    const lastRec = Math.min(pagination.offset + pagination.numRecords, meta.count);
    const sortSpec = sorting[0];

    return (
      <div className="wdk-RecordList">
        {records.map(record => {
          const attrs = _.indexBy(record.attributes, 'name');
          return (
            <div className="wdk-RecordList-Record" style={{margin: '3em 0'}}>
              <Record record={record} attributes={meta.attributes}/>
            </div>
          );
        })}
      </div>
    );
  }

});

export default RecordList;
