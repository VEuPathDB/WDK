import _ from 'lodash';
import React from 'react';
import Router from 'react-router';
import QuestionListStore from '../stores/QuestionListStore';
import QuestionListPageActions from '../actions/QuestionListPageActions';
import createStoreMixin from '../mixins/StoreMixin';

var { Link } = Router;
var storeMixin = createStoreMixin(QuestionListStore);
// var actionsMixin = createActionsMixin(QuestionListPageActions);

var QuestionListPage = React.createClass({

  mixins: [ storeMixin ],

  componentDidMount() {
    var actions = this.props.lookup(QuestionListPageActions);
    actions.loadQuestions();
  },

  render() {
    var { questions, error } = this.state;

    if (error) {
      return (
        <div>There was an error: {error}</div>
      );
    } else {
      return (
        <div>
          <ol>
            {_.map(questions, question => (
              <li key={question}>
                {question + ' - '}
                <Link to="answer" params={{ questionName: question }}>answer page</Link>
              </li>
            ))}
          </ol>
        </div>
      );
    }
  }

});

export default QuestionListPage;
