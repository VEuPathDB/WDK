// WDK filter layout related scripts
//$(document).ready(function() {
//    var wdkFilter = new WdkFilter();
//    wdkFilter.initialize();
//});

function WdkFilter() {

    this.initialize = function() {
        this.addShowHide();
        this.displayFilters();
        this.loadFilterCount();
    }
    
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
                 ChangeFilter(link.attr('strId'),link.attr('stpId'),link.attr('linkUrl'),this);
            });
            // add mouse over to the link
            var detail = $(this).find(".instance-detail");
            $(this).hover(function() {
                              var position = $(this).position();
                              var top = position.top + $(this).height() + 3;
                              var left = position.left - 3;
                              detail.css("left", left + "px");
                              detail.css("top", top + "px");
                              detail.css("display", "block");
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
            $.get(countUrl, 
                  '', 
                  function (data) {
                      if (data.match(/^\d+$/g) != null) {
			if (data == '0') {
				deadz = document.createElement('span');
				$(deadz).addClass("link-url");
				$(deadz).attr('id',link.attr('id'));
				$(deadz).text(data);
				link.replaceWith(deadz);	
			}else{ link.text(data);}
		      }else 
			link.text("error");
                      wdkFilter.loadFilterCount();
                  },
                  "text");
        });
    };


}
