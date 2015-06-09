import throttle from 'lodash/function/throttle';
import React from 'react';
import RecordTable from './RecordTable';
import RecordList from './RecordList';
import Tooltip from './Tooltip';

// Assign global jQuery to local variable
const $ = window.jQuery;


// Calculate the offset of `node` relative to the top of the document.
const getOffsetTop = (node, sum = 0) => node.offsetTop === 0
  ? sum
  : getOffsetTop(node.offsetParent, sum + node.offsetTop);


const Answer = React.createClass({

  getInitialState() {
    return {
      height: 0
    };
  },

  componentDidMount() {
    this._updateHeight();
    $(window).on('resize', this._updateHeight);
  },

  componentWillUnmount() {
    $(window).off('resize', this._updateHeight);
  },

  _updateHeight() {
    if (this.refs.records) {
      const node = this.refs.records.getDOMNode();
      const nodeOffsetTop = getOffsetTop(node);
      const calculatedHeight = window.innerHeight - nodeOffsetTop - 20;
      const minHeight = 335;
      this.setState({
        height: Math.max(calculatedHeight, minHeight)
      });
    }
  },

  handleFilter() {
    //e.preventDefault();
    const value = this.refs.filterInput.getDOMNode().value;
    this.props.answerEvents.onFilter.call(this, value);
  },
  render() {

    // use "destructuring" syntax to assign this.props.params.questionName to questionName
    const {
      answer,
      question,
      recordClass,
      displayInfo,
      filterTerm,
      filteredRecords,
      answerEvents,
      format
    } = this.props;

    const displayName = recordClass.displayName;
    const description = recordClass.description;
    const meta = answer.meta;
    const pagination = displayInfo.pagination;
    const firstRec = pagination.offset + 1;
    const lastRec = Math.min(pagination.offset + pagination.numRecords,
                             meta.count, filteredRecords.length);
    const Records = format === 'list' ? RecordList : RecordTable;
    const tooltipContent = (
      <div>
        <p>
          Enter words or phrases that you wish to query. Words should be
          separated by spaces, and phrases should be enclosed in double-quotes (").
        </p>
        <p>
          {displayName} records displayed will contain these words or phrases
          in any field. All words and phrases are partially matched.
        </p>
        <p>
          For example, the word <i>typical</i> will match both the
          word <i><u>typical</u>ly</i> and the word <i>a<u>typical</u></i>.
        </p>
      </div>
    );

    return (
      <div>
        <h1>{question.displayName}</h1>
          <div>{description}</div>
          <div className="wdk-Answer">
            <div className="wdk-Answer-filter">
              <Tooltip content={tooltipContent}>
                <input
                  ref="filterInput"
                  className="wdk-Answer-filterInput"
                  defaultValue={filterTerm}
                  placeholder={`Search ${displayName} records`}
                  onKeyUp={throttle(this.handleFilter, 150, { leading: false })}
                />
                <i className="fa fa-search fa-lg wdk-Answer-filterIcon"/>
              </Tooltip>
            </div>
            <p className="wdk-Answer-count">
              Showing {firstRec} - {lastRec} of {meta.count} {displayName} records
            </p>
            <Records
              ref="records"
              height={this.state.height}
              meta={meta}
              records={filteredRecords}
              displayInfo={displayInfo}
              {...answerEvents}
              getCellRenderer={this.props.getCellRenderer}
            />
          </div>
      </div>
    );
  }

});

export default Answer;
