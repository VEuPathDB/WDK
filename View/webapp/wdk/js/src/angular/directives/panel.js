/*

   Example usage:

   <div class="panel" href="/some/page.jsp"></div>

*/

angular.module('wdk.directives')
.directive('panel', function($http, $compile) {
  return {
    restrict: 'C',
    link: function(scope, element, attrs) {
      var href = attrs.href;
      if (href) {

        $http.get(href)
        .success(
          function(data, status) {
            element.append($compile(data)(scope));
            //$compile(element.contents())(scope);
          },

          function(data, status) {
            element.html("There was an error");
          }
        );
      }
    }
  }
});
