<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="2.0"
    xmlns:jsp="http://java.sun.com/JSP/Page">
  
  <div id="wdk-dialog-about-openid" style="display:none" title="What is OpenID?">
    <div class="popup-dialog">
      <h2>Why should I use OpenID?</h2>
      <p>
        <a target="_blank" href="http://openid.net">OpenID</a> is a way to authenticate yourself on
        <a target="_blank" href="http://eupathdb.org">EuPathDB</a> sites without having to remember a separate
        password.  It is <em>completely optional</em>, and you will always be able to login with your email and
        password if you prefer.  You can add your OpenID to your account during
        <a href="${pageContext.request.contextPath}/showRegister.do">registration</a>, or on your
        <a href="${pageContext.request.contextPath}/showProfile.do">profile page</a>.  Once you do, use it to
        log in instead of your email and password.  We provide this option simply as a convenience, and to
        support efforts to achieve an open, decentralized authentication standard.  Once again, it is totally
        optional.
      </p>
      <h2>What is OpenID?</h2>
      <p>
        <a target="_blank" href="http://openid.net">OpenID</a> is a decentralized way to identify yourself on the
        web.  Once you have an OpenID, you can log in to any site that supports it, using only one username and
        password- no more remembering passwords for every site you have an account on.  In addition, once you are
        logged in to one site with your OpenID, you need only specify your OpenID to other sites and you won't
        have to type in your password again!  OpenID is not very widespread yet, but it is growing, and its
        technology underlies all those "Login with Google" links you see on various sites.
      </p>
      <h2>How do I get an OpenID?</h2>
      <p>
        You probably already have at least one OpenID, since sites like Google, Yahoo, and AOL provide them along
        with their accounts.  We recommend getting an OpenID from an independent site (e.g. <a target="_blank"
        href="https://www.myopenid.com">MyOpenID</a>).  You can learn how to find or create your OpenID <a
        target="_blank" href="http://openid.net/get-an-openid">here</a>.
      </p>
      <h2>How can I learn more?</h2>
      <p>
        There is an excellent site <a target="_blank" href="http://openidexplained.com">here</a> that can teach
        you the practicality of having an OpenID, and you can read about the development of the technology and
        OpenID standards at the <a target="_blank" href="http://openid.net">OpenID Foundation</a>.  Of course you
        can always read about it on <a target="_blank" href="http://en.wikipedia.org/wiki/Openid">Wikipedia</a>,
        or <a target="_blank" href="https://www.google.com/#q=openid">Google it</a> like you would anything else.
      </p>
    </div>
  </div>

</jsp:root>
