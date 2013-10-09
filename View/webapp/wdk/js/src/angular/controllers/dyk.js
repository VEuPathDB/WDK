var app = angular.module('wdk.controllers');

app.factory('dykFactory', ['$http', function($http) {
  var tips = [];

  $http.get('showXmlDataContent.do?name=XmlQuestions.StrategiesHelp')
  .then(function(response) {
    var html = jQuery(response.data);
    html.find('[id^="tip_"]').each(function() {
      tips.push($(this).find('b').text());
    });
  });

  return {
    tips: tips
  };
}]);

app.controller('DykCtrl', ['$scope', 'dykFactory', function($scope, dykFactory) {

  var cookieVal = jQuery.cookie('DYK'),
      cookie = {
        session: {
          value: 'hide',
          expires: null
        },
        permanent: {
          value: 'permanent_hide',
          expires: 300
        }
      };

  $scope.show = false;

  $scope.tips = dykFactory.tips;

  $scope.current = Math.floor(Math.random() * $scope.tips.length);

  if (cookieVal !== cookie.session.value && cookieVal !== cookie.permanent.value) {
    $scope.show = true;
  }
  if (cookie === cookie.permanent.value) {
    setDYKCookie(cookie.permanent);
  }

  $scope.close = function() {
    $scope.show = false;
    setDYKCookie($scope.permanent ? cookie.permanent : cookie.session);
  }

  $scope.open = function() {
    $scope.show = true;
  }

  $scope.next = function() {
    $scope.current = ($scope.current + 1) % $scope.tips.length;
  }

  $scope.prev = function() {
    $scope.current = ($scope.current - 1) % $scope.tips.length;
  }

  function setDYKCookie(cookie) {
    jQuery.cookie('DYK', cookie.value, {
      domain: location.host.split('.').slice(-2).join('.'),
      path: '/',
      expires: cookie.expires
    });
  }
}]);

app.directive('dyk', function() {
  return {
    restrict: 'C',
    link: function(scope, element, attrs) {
      var box = element.find('div:first');
      box.resizable({
        minWidth: 405,
        minHeight: 87,
        alsoResize: box.next()
      });
      box.next().resizable();
      element.draggable({
        handle: '.dragHandle',
        containment: 'window'
      });
    }
  }
});
