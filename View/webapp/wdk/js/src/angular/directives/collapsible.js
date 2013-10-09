/*

   Example usage:


   Currently:

   <div class="collapsible">
     <div>Title</div>
     <div>
       <p>Some content here...</p>
     </div>
   </div>


   Should be:
   <div class="collapsible" title="Title">
     <p>Some content here...</p>
   </div>

*/

angular.module('wdk.directives')
.directive('collapsible', function() {
  return {
    restrict: 'C',
    link: function(scope, element, attrs) {
      var $trigger = element.children().first().addClass("collapsible-title");
      var $content = $trigger.next().addClass("collapsible-content");
      var $arrowSpan = $("<span></span>").prependTo($trigger);

      if (!$trigger.attr("title")) {
        $trigger.attr("title", "Click to expand or collapse");
      }

      if ($content.css("display") === "none") {
        $content.hide();
        element.addClass("collapsed");
        $arrowSpan.addClass("ui-icon wdk-icon-plus");
      } else {
        $content.show();
        $arrowSpan.addClass("ui-icon wdk-icon-minus");
      }

      $trigger.on("click", function(e) {
        e.preventDefault();
        element.toggleClass("collapsed", $content.css("display") === "block");
        $content.slideToggle();
        $arrowSpan.toggleClass("wdk-icon-plus", element.hasClass("collapsed"));
        $arrowSpan.toggleClass("wdk-icon-minus", !element.hasClass("collapsed"));
      });
    }
  }
});
