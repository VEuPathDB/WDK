import React from 'react';
import ReactDOM from 'react-dom';
import AnswerFilter from './AnswerFilter';
import AnswerTable from './AnswerTable';
import { wrappable } from '../utils/componentUtils';

let $ = window.jQuery;

// Calculate the offset of `node` relative to the top of the document.
let getOffsetTop = (node, sum = 0) => {
  if (node == null) return sum;
  let { offsetTop, offsetParent } = node;
  return offsetTop === 0 ? sum : getOffsetTop(offsetParent, sum + offsetTop);
}

let Answer = React.createClass({

  getInitialState() {
    return { height: 0, width: 0 };
  },

  componentDidMount() {
    this.updateHeight();
    this.updateWidth();
    $(window).on('resize', this.updateHeight);
    $(window).on('resize', this.updateWidth);
  },

  componentWillUnmount() {
    $(window).off('resize', this.updateHeight);
    $(window).off('resize', this.updateWidth);
  },

  updateHeight() {
    if (this.refs.records) {
      let minHeight = 335;
      let node = ReactDOM.findDOMNode(this.refs.records);
      let nodeOffsetTop = getOffsetTop(node);
      let calculatedHeight = window.innerHeight - nodeOffsetTop - 20;
      this.setState({
        height: Math.max(calculatedHeight, minHeight)
      });
    }
  },

  updateWidth() {
    if (this.refs.records) {
      let node = ReactDOM.findDOMNode(this.refs.records);
      this.setState({
        width: node.clientWidth
      });
    }
  },

  render() {
    // use "destructuring" syntax to assign this.props.params.questionName to questionName
    let {
      meta,
      records,
      question,
      recordClass,
      displayInfo,
      allAttributes,
      visibleAttributes,
      onSort,
      onMoveColumn,
      onChangeColumns,
      onFilter,
      format
    } = this.props;

    let displayNamePlural = recordClass.displayNamePlural;
    let description = recordClass.description;
    let pagination = displayInfo.pagination;
    let firstRec = pagination.offset + 1;
    let lastRec = Math.min(pagination.offset + pagination.numRecords,
                             meta.responseCount, records.length);
    let countPhrase = records.length ? `${firstRec} - ${lastRec} of ${meta.totalCount}` : 0;

    return (
      <div className="wdk-AnswerContainer">
        <h1 className="wdk-AnswerHeader">{displayInfo.customName || question.displayName}</h1>
        <div className="wdk-AnswerDescription">{description}</div>
        <div className="wdk-Answer">
          <AnswerFilter {...this.props}/>
          <p className="wdk-Answer-count">
            Showing {countPhrase} {displayNamePlural}
          </p>
          <AnswerTable
            ref="records"
            height={this.state.height}
            width={this.state.width}
            meta={meta}
            records={records}
            recordClass={recordClass}
            displayInfo={displayInfo}
            allAttributes={allAttributes}
            visibleAttributes={visibleAttributes}
            onSort={onSort}
            onMoveColumn={onMoveColumn}
            onChangeColumns={onChangeColumns}
            onFilter={onFilter}
          />
        </div>
      </div>
    );
  }

});

export default wrappable(Answer);
