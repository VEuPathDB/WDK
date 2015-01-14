
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
  // render is simply a wrapper around EntryList, passing latest state
  render: function() {
    var topDivStyle = { width: "45%", display: "inline-block" };
    return (
      <div>
        <div>
          <div style={topDivStyle}>
            <h2>Choose a Search, then enter paramenters:</h2>
            <QuestionForm data={this.state} ac={this.props.ac}/>
          </div>
          <div style={topDivStyle}>
            <h3>To run this search programmatically, POST the JSON below to {ServiceUrl}/answer</h3>
            <QuestionJson data={this.state}/>
          </div>
        </div>
        <AnswerResults data={this.state}/>
      </div>
    );
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
            return ( <option key={question.name} value={question.name}>{question.name}</option> );
          })}
        </select>
      </div>
    );
  }
});

var QuestionForm = React.createClass({
  changeQuestion: function(event) {
    this.props.ac.setQuestion(event.target.value);
  },
  changeParamValue: function(event) {
    var paramName = jQuery(event.target).data("name");
    this.props.ac.setParamValue(paramName, event.target.value);
  },
  changePageNum: function(event) {
    this.props.ac.setPagination({ pageNum: event.target.value, pageSize: this.props.data.pagination.pageSize });
  },
  changePageSize: function(event) {
    this.props.ac.setPagination({ pageNum: this.props.data.pagination.pageNum, pageSize: event.target.value });
  },
  submitRequest: function() {
    this.props.ac.loadResults(this.props.data);
  },
  render: function() {
    var store = this.props.data;
    if (store.selectedQuestion == Store.NO_QUESTION_SELECTED) {
      return (
        <div>
          <QuestionSelect questions={store.questions} selectedQuestion={store.selectedQuestion} onChange={this.changeQuestion}/>
        </div>
      );
    }
    return (
      <div>
        <QuestionSelect questions={store.questions} selectedQuestion={store.selectedQuestion} onChange={this.changeQuestion}/>
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
                  <td><input type="text" data-name={param.name} value={param.value} onChange={this.changeParamValue}/></td>
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

var AnswerResults = React.createClass({
  render: function() {
    var formattedJson = JSON.stringify(this.props.data.results, null, 2);
    return ( <div><pre>{formattedJson}</pre></div> );
  }
});

