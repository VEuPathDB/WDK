// WDK filter layout related scripts

wdk.util.namespace("window.wdk.filter", function(ns, $) {
  "use strict";

  function WdkFilter() {

    this.initialize = function() {
      this.addShowHide();
      this.displayFilters();
      this.loadFilterCount();
    };
      
    this.addShowHide = function() {
      $(".filter-layout .layout-info .handle").click(function() {
        var content = $(this).parents(".filter-layout").children(".layout-detail");
        content.slideToggle("normal");

        var src = this.src;
        var image = src.substr(src.lastIndexOf("/") + 1);
        src = src.substr(0, src.lastIndexOf("/") + 1);
        if (image == "minus.gif") {
          this.src = src + "plus.gif";
        } else {
          this.src = src + "minus.gif";
        }
      });
    };
      
    this.displayFilters = function() {
      $(".filter-instance").each(function() {
        // attach onclick event using jquery instead of onclick attr
        var link = $(this).find(".link-url");
        link.click(function() {
          wdk.strategy.controller.ChangeFilter(link.attr('strId'),link.attr('stpId'),link.attr('linkUrl'),this);
        });
        // add mouse over to the link
        var detail = $(this).find(".instance-detail");
        //cris: z-index added to show filter popup over column titles in result

        //cris: code added to keep a filter popup from hiding outside the window on the right side
        var popupWidth = 300;
        var winWidth=0;
        if (document.documentElement) {
          winWidth = document.documentElement.offsetWidth;
        } else if (window.innerWidth && window.innerHeight) {
          winWidth = window.innerWidth;
        }
        $(this).hover(function() {
          var position = $(this).position();
          var left = position.left;
          var winMinusLeft = winWidth - left;
          if ( winMinusLeft < (popupWidth + 40) ) {
            left = left - (popupWidth - winMinusLeft + 40);	
            detail.css("left", left + "px"); 
          }
          detail.css("width", popupWidth + "px");
          detail.css("display", "block");
          detail.css("z-index", "10"); 
        },
        function() {
          detail.css("display", "none");
        });
      });
    };

    this.loadFilterCount = function() {
      var wdkFilter = new WdkFilter();
      $(".filter-instance .loading:first").parents(".filter-instance").each(function() {
        // load the result count of the filter
        var link = $(this).find(".link-url");
        var countUrl = link.attr("countref");


        countUrl = countUrl.replace(/\s/, "");
        $.get(countUrl, '', function (data) {
          if (data.match(/^\d+$/g) != null) {
            if (data == '0') {
              var deadz = document.createElement('span');
              $(deadz).addClass("link-url");
              $(deadz).attr('id',link.attr('id'));
              $(deadz).text(data);
              link.replaceWith(deadz);	
            } else {
              link.text(data);
            }
          } else {
            link.text("error");
          }
          wdkFilter.loadFilterCount();
        }, "text");
      });
    };
  }

  ns.WdkFilter = WdkFilter;

});
