import isEqual from 'lodash/lang/isEqual';
import React from 'react';
import RecordTable from './RecordTable';
import RecordList from './RecordList';
import Tooltip from './Tooltip';
import TabbableContainer from './TabbableContainer';

let $ = window.jQuery;

// Calculate the offset of `node` relative to the top of the document.
function getOffsetTop(node, sum = 0) {
  let { offsetTop, offsetParent } = node;
  return offsetTop === 0 ? sum : getOffsetTop(offsetParent, sum + offsetTop);
}

function renderFilterField(field, isChecked, handleChange) {
  return (
    <div key={field.name}>
      <label>
        <input type="checkbox" value={field.name} checked={isChecked} onChange={handleChange}/>
        {' ' + field.displayName}
      </label>
    </div>
  );
}

let AnswerFilterSelector = React.createClass({

  componentDidMount() {
    if (this.props.open) {
      $(document).on('click', this.handleDocumentClick);
    }
  },

  componentWillUnmount() {
    $(document).off('click', this.handleDocumentClick);
  },

  componentDidUpdate() {
    $(document).off('click', this.handleDocumentClick);
    if (this.props.open) {
      $(document).on('click', this.handleDocumentClick);
    }
  },

  handleKeyPress(e) {
    if (e.key === 'Escape') {
      this.props.onClose();
    }
  },

  handleDocumentClick(e) {
    // close if the click target is not contained by this node
    let node = React.findDOMNode(this);
    if ($(e.target).closest(node).length === 0) {
      this.props.onClose();
    }
  },

  render() {
    if (!this.props.open) return null;

    let {
      recordClass,
      filterAttributes,
      filterTables,
      selectAll,
      clearAll,
      onClose,
      toggleAttribute,
      toggleTable
    } = this.props;

    return (
      <TabbableContainer
        onKeyDown={this.handleKeyPress}
        className="wdk-Answer-filterFieldSelector">

        <p>
          <a href="#" onClick={selectAll}>select all</a>
          {' | '}
          <a href="#" onClick={clearAll}>clear all</a>
        </p>

        {recordClass.attributes.map(attr => {
          let isChecked = filterAttributes.includes(attr.name);
          return renderFilterField(attr, isChecked, toggleAttribute);
        })}

        {recordClass.tables.map(table => {
          let isChecked = filterTables.includes(table.name);
          return renderFilterField(table, isChecked, toggleTable);
        })}

        <div className="wdk-Answer-filterFieldSelectorCloseIconWrapper">
          <button
            className="fa fa-close wdk-Answer-filterFieldSelectorCloseIcon"
            onClick={onClose}
          />
        </div>

      </TabbableContainer>
    );
  }

});

let AnswerFilter = React.createClass({

  getInitialState() {
    let { filterAttributes, filterTables } = this.props;
    return {
      showFilterFieldSelector: false,
      filterAttributes,
      filterTables
    };
  },

  componentDidUpdate(prevProps, prevState) {
    let { filterAttributes, filterTables } = this.state;
    if (!isEqual(filterAttributes, prevState.filterAttributes) ||
        !isEqual(filterTables, prevState.filterTables)) {
      this.handleFilter();
    }
  },

  toggleFilterFieldSelector() {
    this.setState({ showFilterFieldSelector: !this.state.showFilterFieldSelector });
  },

  handleFilter() {
    let value = this.refs.filterInput.getDOMNode().value;
    let { filterAttributes, filterTables } = this.state;
    this.props.answerEvents.onFilter(value, filterAttributes, filterTables);
  },

  toggleAttribute(e) {
    let attr = e.target.value;
    let op = e.target.checked ? addToArray : removeFromArray;
    this.setState({
      filterAttributes: op(this.state.filterAttributes, attr)
    });
  },

  toggleTable(e) {
    let table = e.target.value;
    let op = e.target.checked ? addToArray : removeFromArray;
    this.setState({
      filterTables: op(this.state.filterTables, table)
    });
  },

  selectAll(e) {
    let { attributes, tables } = this.props.recordClass;
    this.setState({
      filterAttributes: attributes.map(a => a.name),
      filterTables: tables.map(t => t.name)
    });
    e.preventDefault();
  },

  clearAll(e) {
    this.setState({ filterAttributes: [], filterTables: [] });
    e.preventDefault();
  },

  render() {
    let { filterAttributes, filterTables, showFilterFieldSelector } = this.state;
    let { recordClass, filterTerm } = this.props;
    let { displayNamePlural } = recordClass;

    let tooltipContent = (
      <div>
        <p>
          Enter words or phrases that you wish to query. Words should be
          separated by spaces, and phrases should be enclosed in double-quotes.
        </p>
        <p>
          {displayNamePlural} displayed will contain these words or phrases
          in any field. All words and phrases are partially matched.
        </p>
        <p>
          For example, the word <i>typical</i> will match both the
          word <i><u>typical</u>ly</i> and the word <i>a<u>typical</u></i>.
        </p>
      </div>
    );

    return (
      <div className="wdk-Answer-filter">
        <input
          ref="filterInput"
          className="wdk-Answer-filterInput"
          value={filterTerm}
          placeholder={`Search ${displayNamePlural}`}
          onChange={this.handleFilter}
        />
        <Tooltip content="Show search fields">
          <button className="fa fa-caret-down wdk-Answer-filterSelectFieldsIcon"
            onClick={this.toggleFilterFieldSelector}/>
        </Tooltip>
        <Tooltip content={tooltipContent}>
          <i className="fa fa-question-circle fa-lg wdk-Answer-filterInfoIcon"/>
        </Tooltip>

        <AnswerFilterSelector
          recordClass={recordClass}
          open={showFilterFieldSelector}
          onClose={this.toggleFilterFieldSelector}
          filterAttributes={filterAttributes}
          filterTables={filterTables}
          selectAll={this.selectAll}
          clearAll={this.clearAll}
          toggleAttribute={this.toggleAttribute}
          toggleTable={this.toggleTable}
        />

      </div>
    );
  }

});

let Answer = React.createClass({

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
      let node = this.refs.records.getDOMNode();
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
      answer,
      question,
      recordClass,
      displayInfo,
      filteredRecords,
      answerEvents,
      format
    } = this.props;

    let displayNamePlural = recordClass.displayNamePlural;
    let description = recordClass.description;
    let meta = answer.meta;
    let pagination = displayInfo.pagination;
    let firstRec = pagination.offset + 1;
    let lastRec = Math.min(pagination.offset + pagination.numRecords,
                             meta.count, filteredRecords.length);
    let Records = format === 'list' ? RecordList : RecordTable;

    return (
      <div>
        <h1>{question.displayName}</h1>
          <div>{description}</div>
          <div className="wdk-Answer">
            <AnswerFilter {...this.props}/>
            <p className="wdk-Answer-count">
              Showing {firstRec} - {lastRec} of {meta.count} {displayNamePlural}
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

// concatenate each item in items with arr
function addToArray(arr, item) {
  return arr.concat(item);
}

function removeFromArray(arr, item) {
  return arr.filter(function(a) {
    return a !== item;
  });
}

export default Answer;
