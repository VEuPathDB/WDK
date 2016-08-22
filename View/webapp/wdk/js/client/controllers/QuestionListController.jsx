import { wrappable, safeHtml } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';

class QuestionListController extends WdkViewController {

  getStoreName() {
    return 'QuestionListStore';
  }

  isRenderDataLoaded(state) {
    return state.globalData.questions != null;
  }

  getTitle() {
    return "Question List";
  }

  renderView(state) {
    return (
      <div>
        <h2>Available Questions</h2>
        <ol>
          {state.globalData.questions.map(question => (
            <li key={question.name} style={{margin:'10px 0'}}>
              <span style={{fontSize:'1.3em'}}>{question.displayName}</span> ({question.urlSegment})
              { /* <Link to={`/answer/${question.name}`}>answer page</Link> */ }
              <div style={{margin:'0.5em'}}>
                <strong>Summary:</strong><br/>
                <div style={{marginLeft:'2em'}}>{safeHtml(question.summary)}</div>
              </div>
              <div style={{margin:'0.5em'}}>
                <strong>Description:</strong><br/>
                <div style={{marginLeft:'2em'}}>{safeHtml(question.description)}</div>
              </div>
            </li>
          ))}
        </ol>
      </div>
    );
  }
}

export default wrappable(QuestionListController);
