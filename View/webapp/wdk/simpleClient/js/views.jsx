
//**************************************************
// View-Controller (wraps primary page component)
//**************************************************

var ViewController = React.createClass({
  // get state from store for initial render
  getInitialState: function() {
    return this.props.store.get();
  },
  // register with store to receive update notifications
  componentDidMount: function() {
    this.props.store.register(this.update);
  },
  // handle update notifications from the store
  update: function(store) {
    this.replaceState(store.get());
  },
  // render wraps the top-level component, passing latest state
  render: function() {
    return ( <SearchPage data={this.state} ac={this.props.ac}/> );
  }
});

//**************************************************
// Primary Rendering Components
//**************************************************

var QuestionSelect = React.createClass({
  render: function() {
    return (
      <div>
        <label>Question:</label>
        <select value={this.props.selectedQuestion} onChange={this.props.onChange}>
          <option key={Store.NO_QUESTION_SELECTED} value={Store.NO_QUESTION_SELECTED}>Select a Search</option> );
          {this.props.questions.map(function(question) {
            return ( <option key={question} value={question}>{question}</option> );
          })}
        </select>
      </div>
    );
  }
});

var SearchPage = React.createClass({
  changeQuestion: function(event) {
    this.props.ac.setQuestion(event.target.value);
  },
  render: function() {
    var midDivStyle = {
      "width": "45%",
      "display": "inline-block",
      "border": "2px solid blue",
      "border-radius": "10px",
      "padding": "5px",
      "margin": "10px",
      "vertical-align": "top",
      "height": "250px",
      "overflow": "scroll"
    };
    var store = this.props.data;
    var loadingStyle = ( !store.isLoading ? {"display":"none"} :
      {"height":"60px","float":"right","margin-right":"60px"} );
    return (
      <div>
        <h3>Choose a Search<img style={loadingStyle} src="images/loading.gif"/></h3>
        <QuestionSelect questions={store.questions} selectedQuestion={store.selectedQuestion} onChange={this.changeQuestion}/>
        <div>
          <div style={midDivStyle}>
            <QuestionForm data={store} ac={this.props.ac}/>
          </div>
          <div style={midDivStyle}>
            <strong>To run this search programmatically...</strong><br/>
            <span style={{"font-size":"0.8em"}}>
              POST the JSON below to:<br/>
              {ServiceUrl}/answer
            </span>
            <hr/>
            <QuestionJson data={store}/>
          </div>
        </div>
        <AnswerResults results={store.results} pagination={store.pagination}/>
      </div>
    );
  }
});

var QuestionForm = React.createClass({
  changeParamValue: function(event) {
    var paramName = jQuery(event.target).data("name");
    this.props.ac.setParamValue(paramName, event.target.value);
  },
  tryToSetPaging: function(newPageNum, newPageSize) {
    // check to ensure integers
    if (!Util.isPositiveInteger(newPageNum) || !Util.isPositiveInteger(newPageSize)) {
      alert("You can only type positive integers in this field");
    }
    else {
      this.props.ac.setPagination({ pageNum: newPageNum, pageSize: newPageSize });
    }
  },
  changePageNum: function(event) {
    this.tryToSetPaging(event.target.value, this.props.data.pagination.pageSize);
  },
  changePageSize: function(event) {
    this.tryToSetPaging(this.props.data.pagination.pageNum, event.target.value);
  },
  submitRequest: function() {
    this.props.ac.loadResults(this.props.data);
  },
  render: function() {
    var store = this.props.data;
    var changeParamFunction = this.changeParamValue;
    if (store.selectedQuestion == Store.NO_QUESTION_SELECTED) {
      // don't display anything if no question selected
      return ( <div/> );
    }
    return (
      <div>
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Current Value</th>
            </tr>
          </thead>
          <tbody>
            {store.paramOrdering.map(function(paramName) {
              var param = store.paramValues[paramName];
              return (
                <tr key={param.name}>
                  <td>{param.displayName}</td>
                  <td><input type="text" data-name={param.name} value={param.value} onChange={changeParamFunction}/></td>
                </tr>
              );
            })}
          </tbody>
        </table>
        <hr/>
        <div>
          <label>Page To Display:</label>
          <input type="text" value={store.pagination.pageNum} onChange={this.changePageNum}/>
        </div>
        <div>
          <label>Page Size:</label>
          <input type="text" value={store.pagination.pageSize} onChange={this.changePageSize}/>
        </div>
        <hr/>
        <input type="button" value="Submit Request" onClick={this.submitRequest}/>
      </div>
    );
  }
});

var QuestionJson = React.createClass({
  render: function() {
    var data = this.props.data;
    var formattedJson = JSON.stringify(Util.getAnswerRequestJson(
      data.selectedQuestion, data.paramValues, data.pagination), null, 2);
    return ( <div><pre>{formattedJson}</pre></div> );
  }
});

var HtmlDiv = React.createClass({
  render: function() {
    return ( <div style={this.props.style} dangerouslySetInnerHTML={{__html: this.props.contents}}/> );
  }
});

var AnswerResults = React.createClass({
  render: function() {
    if (this.props.results == null) {
      return ( <div></div> );
    }
    var records = this.props.results.records;
    var meta = this.props.results.meta;
    var pagination = this.props.pagination;
    var headerStyle = { "border":"1px solid blue", "background-color":"peachpuff" };
    var cellStyle = { "border":"1px solid blue", "font-size":"0.8em", "vertical-align":"top" };
    var cellDivStyle = { "overflow":"scroll", "overflow-x":"hidden", "overflow-y":"auto", "height":"50px" };
    return (
      <div>
        <div style={{"margin":"20px 0"}}>
          <strong>
            Query returned {meta.count} total records of type {meta.class}.<br/>
            Showing {pagination.pageSize} records on page {pagination.pageNum}.
          </strong>
        </div>
        <table style={{"border-collapse":"collapse"}}>
          <tr>
            {meta.attributes.map(function(attrib) {
              return ( <th style={headerStyle}>{attrib.displayName}</th> ); })}
          </tr>
          {records.map(function(record) { return (
            <tr>
              {meta.attributes.map(function(attrib) { return (
                <td style={cellStyle}>
                  <HtmlDiv style={cellDivStyle} contents={record.attributes[attrib.name]}/>
                </td>
              );})}
            </tr>
          );})}
        </table>
      </div>
    );
  }
});

