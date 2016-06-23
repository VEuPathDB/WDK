import WdkStore from './WdkStore';
import { StaticDataProps } from '../utils/StaticDataUtils';

export default class QuestionStore extends WdkStore {

  getRequiredStaticDataProps() {
    return [ StaticDataProps.QUESTIONS ];
  }

  getInitialState() {
    return { questions: null };
  }

}