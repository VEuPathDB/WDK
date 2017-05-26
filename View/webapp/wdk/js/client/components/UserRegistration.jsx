import { wrappable } from '../utils/componentUtils';
import UserFormContainer, { UserFormContainerPropTypes } from './UserFormContainer';

let IntroText = () => (
  <div cssStyle={{width:"60%",align:"center"}}>
    IMPORTANT: If you already registered in another site<br/>
    (AmoebaDB, CryptoDB, EuPathDB, FungiDB, GiardiaDB, MicrosporidiaDB,
    PiroplasmaDB, PlasmoDB, SchistoDB, ToxoDB, TrichDB or TriTrypDB)<br/>
    you do NOT need to register again.
  </div>
);

let descriptionBoxStyle = {
  align:"left",
  width:"550px",
  margin:"5px",
  border:"1px solid black",
  padding:"5px",
  lineHeight:"1.5em"
};

let WhyRegister = () => (
  <div cssStyle={descriptionBoxStyle}>
    <p><b>Why register/subscribe?</b> So you can:</p>
    <div id="cirbulletlist">
      <ul>
        <li>Have your strategies back the next time you login</li>
        <li>Use your basket to store temporarily IDs of interest, and either save, or download or access other tools</li>
        <li>Use your favorites to store IDs of permanent interest, for faster access to its record page</li>
        <li>Add a comment on genes and sequences</li>
        <li>Set site preferences, such as items per page displayed in the query result</li>
        <li>Opt to receive infrequent alerts (at most monthly), by selecting (below) from which EuPathDB sites</li>
      </ul>
    </div>
  </div>
);

let PrivacyPolicy = () => (
  <div cssStyle={descriptionBoxStyle}>
    <div cssStyle={{fontSize:"1.2em"}}>
      <b>EuPathDB Websites Privacy Policy</b> 
    </div>
    <table>
      <tableBody>
        <tr>
          <td width="40%">
            <p><b>How we will use your email:</b></p>
            <div id="cirbulletlist">
              <ul>
                <li>Confirm your subscription</li>
                <li>Send you infrequent alerts if you subscribe to receive them</li>
                <li>NOTHING ELSE.  We will not release the email list.</li>
              </ul>
            </div>
          </td>
          <td>
            <p><b>How we will use your name and institution:</b></p>
            <div id="cirbulletlist">
              <ul>
                <li>If you add a comment to a Gene or a Sequence, your name and institution will be displayed with the comment.</li>
                <li>If you make one of your strategies Public, your name and institution will be displayed with it.</li>
                <li>NOTHING ELSE.  We will not release your name or institution.</li>
              </ul>
            </div>
          </td>
        </tr>
      </tableBody>
    </table>
  </div>
);

/**
 * React component for the user profile/account form
 * @type {*|Function}
 */
let UserRegistration = props => (

  <UserFormContainer {...props}
      shouldHideForm={!props.userFormData.isGuest}
      hiddenFormMessage="You must log out before registering a new user."
      titleText="Registration"
      introComponent={IntroText}
      showChangePasswordBox={false}
      submitButtonText="Sign me up!"
      onSubmit={props.userEvents.submitRegistrationForm}/>

);

UserRegistration.propTypes = UserFormContainerPropTypes;

export default wrappable(UserRegistration);
