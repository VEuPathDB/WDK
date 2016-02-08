import React from 'react';
import ReactDOM from 'react-dom';
import AnswerFilter from './AnswerFilter';
import AnswerTable from './AnswerTable';
import RecordList from './RecordList';
import Main from './Main';
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
    return { height: 0 };
  },

  componentDidMount() {
    this.updateHeight();
    $(window).on('resize', this.updateHeight);
  },

  componentWillUnmount() {
    $(window).off('resize', this.updateHeight);
  },

  updateHeight() {
    if (this.refs.records) {
      let node = ReactDOM.findDOMNode(this.refs.records);
      let nodeOffsetTop = getOffsetTop(node);
      let calculatedHeight = window.innerHeight - nodeOffsetTop - 20;
      let minHeight = 335;
      this.setState({
        height: Math.max(calculatedHeight, minHeight)
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
      answerEvents,
      format
    } = this.props;

    let displayNamePlural = recordClass.displayNamePlural;
    let description = recordClass.description;
    let pagination = displayInfo.pagination;
    let firstRec = pagination.offset + 1;
    let lastRec = Math.min(pagination.offset + pagination.numRecords,
                             meta.responseCount, records.length);
    let Records = format === 'list' ? RecordList : AnswerTable;

    return (
      <Main className="wdk-AnswerContainer">
        <h1>{question.displayName}</h1>
        <div>{description}</div>
        <div className="wdk-Answer">
          <AnswerFilter {...this.props}/>
          <p className="wdk-Answer-count">
            Showing {firstRec} - {lastRec} of {meta.totalCount} {displayNamePlural}
          </p>
          <Records
            ref="records"
            height={this.state.height}
            meta={meta}
            records={records}
            recordClass={recordClass}
            displayInfo={displayInfo}
            allAttributes={allAttributes}
            visibleAttributes={visibleAttributes}
            {...answerEvents}
          />
        </div>
      </Main>
    );
  }

});

export default wrappable(Answer);
