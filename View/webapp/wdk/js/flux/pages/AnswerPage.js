import React from 'react';
import Router from 'react-router';
import Answer from '../components/Answer';

var AnswerPage = React.createClass({

  mixins: [ Router.State ],

  getInitialState() {
    // stubbing Answer resource
    return {
      answer: {
        meta: {
          count: 10,
          "class": 'SomeClassName',
          attributes: [{
            name: 'first_name',
            displayName: 'First Name',
            sortable: true,
            removable: false,
            category: 'general',
            type: 'text',
            className: 'first-name'
          },
          {
            name: 'last_name',
            displayName: 'Last Name',
            sortable: true,
            removable: true,
            category: 'general',
            type: 'text',
            className: 'last-name green-border'
          }],
          tables: []
        },
        records: [{
          id: 1,
          attributes: {
            first_name: 'Dave',
            last_name: 'Falke'
          }
        }, {
          id: 2,
          attributes: {
            first_name: 'Jerric',
            last_name: 'Gao'
          }
        }, {
          id: 3,
          attributes: {
            first_name: 'Ryan',
            last_name: 'Doherty'
          }
        }, {
          id: 4,
          attributes: {
            first_name: 'Cristina',
            last_name: 'Aurrecoechea'
          }
        }, {
          id: 5,
          attributes: {
            first_name: 'Steve',
            last_name: 'Fischer'
          }
        }, {
          id: 6,
          attributes: {
            first_name: 'Mark',
            last_name: 'Heiges'
          }
        }, {
          id: 7,
          attributes: {
            first_name: 'Matt',
            last_name: 'Guidry'
          }
        }, {
          id: 8,
          attributes: {
            first_name: 'Haiming',
            last_name: 'Wang'
          }
        }, {
          id: 9,
          attributes: {
            first_name: 'Jessie',
            last_name: 'Kissinger'
          }
        }, {
          id: 10,
          attributes: {
            first_name: 'Brian',
            last_name: 'Brunk'
          }
        }]
      }
    };
  },

  render() {
    var { questionName } = this.getParams();
    return (
      <div>
        <Answer questionName={questionName} answer={this.state.answer}/>
      </div>
    );
  }

});

export default AnswerPage;
