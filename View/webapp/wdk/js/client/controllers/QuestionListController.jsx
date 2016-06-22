import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import Link from '../components/Link';

class QuestionListController extends WdkViewController {

  getStoreName() {
    return 'QuestionListStore';
  }

  isRenderDataLoaded(state) {
    return state.questions != null;
  }

  getTitle() {
    return "Question List";
  }

  renderView(state) {
    return (
      <div>
        <h2>Available Questions</h2>
        <ol>
          {state.questions.map(question => (
            <li key={question.name}>
              {question.displayName + ' - '}
              <Link to={`/answer/${question.name}`}>answer page</Link>
            </li>
          ))}
        </ol>
      </div>
    );
  }
}

export default wrappable(QuestionListController);
