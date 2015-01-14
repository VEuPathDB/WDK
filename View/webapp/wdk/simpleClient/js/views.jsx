
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
    return (
      <div>
        <div style="width:45%; display:inline-block">
          <h2>Choose a Search, then enter paramenters:</h2>
          <QuestionForm data={this.state} ac={this.props.ac}/>
        </div>
        <div style="width:45%; display:inline-block">
          <h3>To run this search programmatically, POST the JSON below to {ServiceUrl}/answer</h3>
          <QuestionJson data={this.state}/>
        </div>
      </div>
      <div>
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
    var firstOption = (selectedQuestion == undefined ?
      ( <option value="---" selected="selected">Select a Search</option> ) :
      ( <option value="---">Select a Search</option> );
    return (
      <select onchange={this.props.onchange}>
        {firstOption}
        {this.props.questions.map(function(question) {
          return (question.name == selectedQuestion ?
            ( <option value={question.name}>{question.name}</option> ) :
            ( <option value={question.name} selected="selected">{question.name}</option> ));
        })}
      </select>
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
  }
  changePageSize: function(event) {
    this.props.ac.setPagination({ pageNum: this.props.data.pagination.pageNum, pageSize: event.target.value });
  }
  submitRequest: function() {
    this.props.ac.loadResults(this.props.data);
  }
  render: function() {
    var selectedQuestion = this.props.data.selectedQuestion;
    if (selectedQuestion === null) {
      return (
        <div>
          <label>Question:</label>
          <QuestionSelect questions={this.props.data.questons} onchange={this.changeQuestion}/>
        </div>
      );
    }
    else {
      return (
        <div>
          <div>
            <label>Question:</label>
            <QuestionSelect selectedQuestion={selectedQuestion} questions={this.props.data.questions} onchange={this.changeQuestion}/>
          </div>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Current Value</th>
              </tr>
            </thead>
            <tbody>
              {this.props.data.params.map(function(param) { return (
                <tr>
                  <td>{param.displayName}</td>
                  <td><input type="text" data-name={param.name} value={param.value} onchange={this.changeParamValue}/></td>
                </tr>
              );})}
            </tbody>
          </table>
          <hr/>
          <div>
            <label>Page To Display:</label>
            <input type="text" value={this.props.data.pagination.pageNum} onchange={this.changePageNum}/>
          </div>
          <div>
            <label>Page Size:</label>
            <input type="text" value={this.props.data.pagination.pageSize} onchange={this.changePageSize}/>
          </div>
          <hr/>
          <input type="button" value="Submit Request" onClick={this.submitRequest}/>
        </div>
      );
    }
  }
});

var QuestionJson = React.createClass({
  render: function() {
    var data = this.props.data;
    var formattedJson = JSON.stringify(Util.getAnswerRequestJson(
      data.selectedQuestion, data.params, data.pagination));
    return ( <pre>{formattedJson}</pre> );
  }
}

var AnswerResults = React.createClass({
  render: function() {
    var formattedJson = JSON.stringify(this.props.data.results, null, 2);
    return ( <pre>{formattedJson}</pre> );
  }
});

