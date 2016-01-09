import React from 'react';
import ReactDOM from 'react-dom';
import Record from './Record';
import { wrappable } from '../utils/componentUtils';

/**
 * RecordList displays a list of Record components.
 */

let $ = window.jQuery;

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
let noop = () => {};

let PropTypes = React.PropTypes;

let sortClassMap = {
  ASC: 'ui-icon ui-icon-arrowthick-1-n',
  DESC: 'ui-icon ui-icon-arrowthick-1-s'
};

let RecordList = React.createClass({

  propTypes: {
    meta: PropTypes.object.isRequired,
    displayInfo: PropTypes.object.isRequired,
    records: PropTypes.array.isRequired,
    onSort: PropTypes.func,
    onMoveColumn: PropTypes.func,
    onChangeColumns: PropTypes.func,
    onNewPage: PropTypes.func,
    height: PropTypes.number.isRequired
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
      pendingVisibleAttributes: this.props.visibleAttributes,
      attributeSelectorOpen: false
    };
  },

  componentWillReceiveProps(nextProps) {
    this.setState({
      pendingVisibleAttributes: nextProps.visibleAttributes
    });
  },

  componentDidMount() {
    this._scrollToTarget();
  },

  render() {
    let { records } = this.props;
    return (
      <div
        ref="recordList"
        className="wdk-RecordList"
        style={{ height: this.props.height }}
      >
        {records.map(this._renderRecord)}
      </div>
    );
  },

  _renderRecord(record, index) {
    let { meta, position } = this.props;
    let refName = index === position ? 'target' : null;

    return (
      <div ref={refName} className="wdk-RecordList-Record" >
        <Record record={record} attributes={meta.attributes}/>
      </div>
    );
  },

  _scrollToTarget() {
    let recordListNode = ReactDOM.findDOMNode(this.refs.recordList);
    let targetNode = ReactDOM.findDOMNode(this.refs.target);
    recordListNode.scrollTop = targetNode.offsetTop - 30;
  }

});

export default wrappable(RecordList);
