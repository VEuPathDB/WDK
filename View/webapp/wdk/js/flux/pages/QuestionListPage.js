import React from 'react';
import Router from 'react-router';
import QuestionListStore from '../stores/QuestionListStore';
import { loadQuestions } from '../actions/QuestionListPageActions';
import createStoreMixin from '../mixins/StoreMixin';
var { Link } = Router;

export default React.createClass({

  displayName: 'QuestionListPage',

  mixins: [ createStoreMixin(QuestionListStore) ],

  componentDidMount() {
    loadQuestions();
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
            {questions.map(question => (
              <li>
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
