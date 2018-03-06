var list;
var i = 0;
var wai_button = document.getElementById("wai");
var search_button = document.getElementById("search_start");
var thelist = document.getElementsByTagName("div");
var Content = document.getElementById("Content");
var add_content = document.getElementById("add_content");
var add_start = document.getElementById("add_start");
var delete_start=document.getElementById("delete");
for (var N = 0; N < thelist.length; N += 2) {
    thelist[N].style.backgroundColor = "white";
}

/*function getlist(name){
    list[i]=name;
    i++;
    if(name.childElementCount===0){

    }
    else if(name.childElementCount!==0){
        for(var j=0;j<name.childElementCount;j++){

        }
    }
}*/

function from_wai() {
    var num = 0;
    var testInter = setInterval(function () {
        if (num < thelist.length) {
            thelist[num].style.backgroundColor = "red";
            // thelist[num+1].style.backgroundColor = "red";
            if (num >= 1) {
                thelist[num - 2].style.backgroundColor = "white";
                // thelist[num - 1].style.backgroundColor = "white";
            }
            // if(thelist[num].className==="horizontal_content"){
            //     thelist[num].style.backgroundColor = "white";
            // }
            // if(thelist[num+1].className==="horizontal_content"){
            //     thelist[num+1].style.backgroundColor = "white";
            // }
            num += 2;
        } else {
            thelist[num - 2].style.backgroundColor = "white";
            thelist[num - 1].style.backgroundColor = "white";
            clearInterval(testInter)
        }
    }, 500);
    // for(var testNum=0;testNum<thelist.length;testNum++){
    //     thelist[testNum].style.backgroundColor="red";
    // }
}

// var ifexist = false;
// var ifalert=false;
function search() {
    // var allnum = 0;
    var content = document.getElementById("search_content").value;
    for (var k = 0; k < thelist.length; k += 2) {
        thelist[k].style.backgroundColor = "white";
        thelist[k].style.color = "black";
    }
    var num = 0;
    var testInter = setInterval(function () {
        if (num < thelist.length) {
            thelist[num].style.backgroundColor = "red";
            if (thelist[num].className === "Content_horizontal_sub") {
                if (thelist[num].innerText.indexOf(content) !== -1) {
                    thelist[num].style.color = "#7FFF00";
                    // ifexist = true;
                    // allnum++;
                }
            }
            else {
                var longstring = thelist[num].innerText;
                var shortstring = thelist[num + 1].innerText;
                var shortindex = longstring.indexOf(shortstring);
                var thisstring = longstring.substring(0, shortindex);
                if (thisstring.indexOf(content) !== -1) {
                    thelist[num].style.color = "#7FFF00";
                    // ifexist = true;
                    // allnum++;
                }
            }

            if (num >= 1) {
                thelist[num - 2].style.backgroundColor = "white";
            }
            num += 2;
        } else {
            // ifalert=true;
            thelist[num - 2].style.backgroundColor = "white";

            clearInterval(testInter);

        }

    }, 500);

}
// if(ifalert===true){
//     if (ifexist===true) {
//         alert("搜索完毕，有匹配结果，匹配成功的项：" + num + "项");
//     } else {
//         alert("搜索完毕，无匹配成功的项...")
//     }
//     ifalert=false;
// }

wai_button.onclick = from_wai;
search_button.onclick = search;
// see_button.onclick=getlist(see_button);

var thediv;
Content.addEventListener('click', function (e) {
    var alldiv=document.getElementsByTagName("div");
    for (var i = 0; i < alldiv.length; i++) {
        alldiv[i].style.backgroundColor = "white";
        alldiv[i].style.color = "black";
    };
    thediv=null;
    thediv = e.target;
    if (thediv.style.backgroundColor === "white") {
        thediv.style.backgroundColor = "blue";
        thediv.style.color = "white";
        var subdiv = thediv.children;
        for (var i = 0; i < subdiv.length; i++) {
            subdiv[i].style.color = "black";
        }
    } /*else {
        thediv.style.backgroundColor = "white";
        thediv.style.color = "black";
    }*/
    var subdiv = thediv.children;
    for (var i = 0; i < subdiv.length; i++) {
        subdiv[i].style.color = "black";
    }
    // var add_HTML=add_start.onclick=
});

function add() {
    var add_Text = add_content.value;
    var newdiv_1 = document.createElement("div");
    var newdiv_2 = document.createElement("div");
    // var newdiv_3=document.createElement("div");
    if (thediv.id === "Content") {
        newdiv_1.className = "vertical_content";
        newdiv_2.className = "Content_horizontal";
        // newdiv_3.className="horizontal_content";
        newdiv_2.innerText = add_Text;
    }
    else {
        newdiv_1.className = "horizontal_content";
        newdiv_2.className = "Content_horizontal";
        newdiv_2.innerText = add_Text;
    }
    thediv.appendChild(newdiv_1);
    thediv.appendChild(newdiv_2);
    // newdiv.className="horizontal_content";
    var alldiv = document.getElementsByTagName("div");
    for (var i = 0; i < alldiv.length; i++) {
        alldiv[i].style.backgroundColor = "white";
        alldiv[i].style.color = "black";
    }
    thediv=null;
}

add_start.onclick = add;


function deletediv(){
    // var Content=document.getElementById("Content");
    // Content.removeChild(thediv);
    thediv.parentNode.removeChild(thediv);
}

delete_start.onclick=deletediv;
