// WDK filter layout related scripts

function WdkFilter() {

    this.initialize = function() {
        addShowHide();
        loadFilters();
    }
    
    this.addShowHide = function() {
        $(".filter-layout .layout-info .handle").click(function() {
            var content = $(this).parents(".filter-layout").children(".layout-detail");
            content.slideToggle("normal");
            
            var src = this.src;
            src = src.substr(0, src.lastIndexOf("/") + 1);
            if (children.css("display")) {
                this.src = src + "plus.gif";
            } else {
                this.src = src + "minus.gif";
            }
        });
    };
    
    this.loadFilters = function() {
        $(".filter-instance").each(function() {
            // add mouse over to the link
            var detail = $(this).find(".instance-detail");
            $(this).hover(function() {
                              var position = $(this).position();
                              var top = position.top + $(this).height();
                              var left = position.left;
                              detail.css("left", left + "px");
                              detail.css("top", top + "px");
                              detail.css("display", "block");
                          },
                          function() {
                              detail.css("display", "none");
                          }
            
            // load the result count of the filter
            var link = $(this).find(".link");
            var content = link.text().trim();
            if (content = "--") {
                var countUrl = $(this).find(".count-url").text();
                $.get(countUrl, 
                      '', 
                      function (data) {
                          $link.text(data);
                      },
                      "text");
            }
        });
    };


}