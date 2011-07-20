/*  Javascript file to control the actions of certain buttons in the menubar
Cary  Feb. 15, 2010
Basket Button
*/

$(document).ready(function(){
	var basketButton = new BasketButton();
	basketButton.init();
});

function BasketButton() {
	this.init = function(){
		$("div#menubar div#menu a#mybasket").click(function(){
			if(location.href.indexOf("showApplication") == -1){
				setCurrentTabCookie("basket", false);
				return true;
			}else{
				showPanel("basket");
				return false;
			}
		});
	};
}