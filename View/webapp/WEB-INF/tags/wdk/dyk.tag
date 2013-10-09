<div class="dyk" ng-controller="DykCtrl" ng-show="show">
  <div id="dyk-box">
    <div class="dragHandle">
      <div style="margin:0;padding:0;position:absolute;top:0">
        <span class="h3left">Did You Know...</span>
      </div>
      <span id="dyk-count">{{ current + 1 }} of {{ tips.length }}</span>
    </div>

    <div id="dyk-text">
      <ul>
        <li ng-repeat="tip in tips" ng-show="$index == current">
          <div style="margin: 10px 15px 15px;">
            <p><strong>{{ tip }}</strong>
            <a href="#strat_help_{{ $index }}" ng-click="close()" onclick="wdk.addStepPopup.showPanel('help')">Learn more...</a></p></div>
        </li>
    </div>

    <ul class="tip-box">
        <li><input style="font-size:85%" type="button" ng-click="prev()" value="<< Previous" /></li>
        <li><input style="font-size:85%" type="button" ng-click="next()" value="Next >>" /></li>
    </ul>
      
    <div id="closing-items">
        <input style="vertical-align:bottom" ng-model="permanent" type="checkbox" name="stayClosed" />
        <span style="font-size:90%">Never show me this again</span>
        <input style="font-size:85%" ng-click="close()" type="button" value="Close" />
    </div>
    <div id="content" style="display:none">
    </div>
  </div>

  <div id="dyk-shadow"></div>
</div>
