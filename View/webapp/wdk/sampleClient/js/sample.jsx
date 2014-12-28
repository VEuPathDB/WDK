
var EntryList = React.createClass({
  render: function () {
    return (
      <div>
        State:<br/>
        <pre>{JSON.stringify(this.props.data, undefined, 2)}</pre>
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
      data: { offset: 0 },
      dataType: "json",
      success: function(data) {
        Sample.model = data;
        React.render(<EntryList data={Sample.model}/>, document.body);
      },
      error: function(jqXHR, textStatus, errorThrown ) {
        alert("Unable to load data: " + textStatus + ", " + errorThrown);
      }
    });
  }

};

Sample.loadPage();
