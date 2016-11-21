/*
 * Vote! Javascript functions to provide tab support.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */

// Sets a cookie
function setCookie(name, value) {
    document.cookie = name + "=" + value + "; path=/";
}

// Returns the value of a cookie or the default value
function getCookie(name, defaultValue) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1, c.length);
        }
        if (c.indexOf(nameEQ) === 0) {
            return c.substring(nameEQ.length, c.length);
        }
    }
    return defaultValue;
}

// Update the state of a tab
function updateTabState(e, current) {
    // Adjust navigation bar
    var nav = document.getElementById(e);
    nav = nav.getElementsByTagName("ul")[0];
    var links = nav.getElementsByTagName("a");
    var visibleIndex = -1;
    for (var i = 0; i < links.length; i++) {
        if (links[i].parentElement.tagName !== "LI") {
            continue;
        }
        if (links[i] === current) {
            links[i].className = 'selected';
            links[i].parentElement.className = 'selected';
            visibleIndex = i;
        } else {
            links[i].className = '';
            links[i].parentElement.className = '';
        }
    }

    // Adjust div's
    var tabpane = document.getElementById(e);
    var tabs = tabpane.getElementsByTagName("div");
    var j = 0;
    for (var i = 0; i < tabs.length; i++) {
        if (tabs[i].className.indexOf('tab ') > -1 || tabs[i].className === "tab" ) {
            if (j === visibleIndex) {
                tabs[i].className = 'tab';
            } else {
                tabs[i].className = 'tab hidden';
            }
            j++;
        }
    }
}

// Setup tabs
function initTabs() {
    // Find all div tab boxes
    var allDivs = document.getElementsByTagName("div");
    for (var i = 0; i < allDivs.length; i++) {
        if (allDivs[i].className.indexOf('tabpane') > -1) {
            // Work on the current div box
            var current = allDivs[i];
            var nav = current.getElementsByTagName("ul")[0];
            var selected = parseInt(getCookie(current.id + "_index", "0"));
            var links = nav.getElementsByTagName("a");

            for (var j = 0; j < links.length; j++) {
                if (links[j].parentElement.tagName !== "LI") {
                    continue;
                }
                links[j].key = current.id + "_index";
                links[j].value = j;
                links[j].target = current.id;
                links[j].onclick = function(e) {
                    updateTabState(this.target, this);
                    setCookie(this.key, this.value);
                    return false;
                };
                if (j === selected) {
                    links[j].className = "selected";
                }
            }

            // Adjust div's
            var tabs = current.getElementsByTagName("div");
            var j = 0;
            for (var i = 0; i < tabs.length; i++) {
                if (tabs[i].className.indexOf('tab ') > -1 || tabs[i].className === "tab" ) {
                    if (j === selected) {
                        tabs[i].className = 'tab';
                    } else {
                        tabs[i].className = 'tab hidden';
                    }
                    j++;
                }
            }
        }
    }   
}

var oldOnLoad = window.onload;
window.onload = function() {
    initTabs();
    oldOnLoad();
};