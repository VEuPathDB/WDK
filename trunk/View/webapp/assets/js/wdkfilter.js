// WDK filter layout related scripts
$(document).ready(function() {
    var wdkFilter = new WdkFilter();
    wdkFilter.addShowHide();
    wdkFilter.loadFilters();
}

function WdkFilter() {
    
    this.addShowHide = function() {
        $(".filter-layout .layout-handle").click(function() {
            var content = $(this).parents(".filter-layout").children(".layout-content");
            content.slideToggle("normal");
            
            var src = this.src;
            src = src.substr(0, src.lastIndexOf("/") + 1);
            if (children.css("display") == "none") {
				this.src = src + "plus.gif";
			} else {
				this.src = src + "minus.gif";
			}
        });
    };
    
    this.loadFilters = function() {
        $(".filter-layout .instance").each(function() {
            var link = $(this).find(".link");
            if (link.style.display != "block") {
                var countUrl = $(this).find(".count-url").text();
                $(link).load(countUrl);
                $(this).find(".pending").css("display", "none");
                $(link).scc("display", "block");
            }
        });
    };


}