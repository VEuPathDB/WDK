
var EntryList = React.createClass({
  showAddForm: function(event) {
    alert("Will add new");
  },
  modifyRecord: function(event) {
    alert("Will modify record " + $(event.target).data("id"));
  },
  deleteRecord: function(event) {
    alert("Will delete record " + $(event.target).data("id"));
  },
  render: function() {
    var component = this;
    var data = component.props.data;
    var getRecord = function(key) {
      return (
        <tr key={key}>
          <td>{key}</td>
          <td style={{border: "1px solid black"}}><pre>{JSON.stringify(data[key], undefined, 2)}</pre></td>
          <td>
            <input type="button" value="Modify" data-id={key} onClick={component.modifyRecord}/>
            <input type="button" value="Delete" data-id={key} onClick={component.deleteRecord}/>
          </td>
        </tr>
      );
    };
    return (
      <div>
        State:<br/>
        <table>
          <tr><th>ID</th><th>Record</th></tr>
          {Object.keys(data).map(getRecord)}
        </table>
        <hr/>
        <input type="button" value="Add Record" onClick={this.showAddForm}/>
      </div>
    );
  }
});

var Sample = {

  serviceUrl: "http://rdoherty.plasmodb.org/plasmo.rdoherty/service/",

  model: {},

  loadPage: function() {
    $.ajax({
      type: "GET",
      url: Sample.serviceUrl + "sample",
      data: { expandRecords: true },
      dataType: "json",
      success: function(data) {
        Sample.model = data;
        React.render(<EntryList data={Sample.model}/>, document.body);
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Error: Unable to load data");
      }
    });
  }

};

Sample.loadPage();
