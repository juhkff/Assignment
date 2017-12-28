var inputNum=document.getElementById("inputNum");
var left_in=document.getElementById("left_in");
var right_in=document.getElementById("right_in");
var left_delete=document.getElementById("left_delete");
var right_delete=document.getElementById("right_delete");
var sorter=document.getElementById("sort");
var Content=document.getElementById("Content");

function insert(btnname) {
    if(!inputNum.value||inputNum.value===""||inputNum.value>100||inputNum.value<10){
        alert("请输入10至100的数字！")

        //>?>=?
    }else if(Content.childElementCount>60){
        alert("输入的数值已经有60个了！")
    }else{
        var div=document.createElement("div");
        div.innerHTML=inputNum.value;
        if(btnname.id==="left_in"){
            Content.insertBefore(div,Content.firstElementChild);
        }else{
            Content.appendChild(div);
        }
        div.style.height=inputNum.value+"px";
    }
}

function Delete(btnname) {
    if(Content.childElementCount<=0){
        alert("没东西可删了！")
    }else{
        if(btnname.id==="left_delete"){
            var first=Content.firstElementChild;
            Content.removeChild(Content.firstElementChild);
            alert(first.innerHTML);
        }else if(btnname.id==="right_delete"){
            var last=Content.lastElementChild;
            Content.removeChild(Content.lastElementChild);
            alert(last.innerHTML);
        }
    }
}

function sort() {
    var NumList=Content.childNodes;
    for(var i=0;i<NumList.length-1;i++){
        var index=i;
        var thisnum=NumList[i].style.height;
        for(var j=i+1;j<NumList.length;j++){
            if(thisnum>NumList[j].style.height){
                index=j;
                thisnum=NumList[j].style.height;
            }
        }
        // var temp=NumList[i];

        /*同内存地址啊啊啊！！！*/
        /*可以用强制类型转换解决！*/

        var temp=parseInt(NumList[i].style.height);
        var temp2=parseInt(NumList[index].style.height);
        // temp.style.height=NumList[i].style.height;
        // temp.innerHTML=NumList[i].innerHTML;
        // NumList[i]=NumList[index];
        // NumList[index]=temp;

        // var result=NumList.splice(i,1,NumList[index]);
        // var result=NumList.splice(index,1,temp);
        // NumList[i].style.height=NumList[index].style.height;
        // NumList[i].innerHTML=NumList[index].innerHTML;
        NumList[i].style.height=temp2+"px";
        // NumList[i].innerHTML=temp2;
        // NumList[index].style.height=temp.style.height;
        NumList[index].style.height=temp+"px";
        // NumList[index].innerHTML=temp;
    }
    // Content.childNodes=NumList;
}

/*function sort() {
    var result=[];
    var list = Content.childNodes;
    var NumList = [];
    for (var i = 0; list.length; i++) {
        NumList[i] = list[i];
    }
    for (var i = 0; i < NumList.length; i++) {
        var index = i;
        var thisnum = NumList[i].innerHTML;
        for (var j = i + 1; j < NumList.length; j++) {
            if (thisnum > NumList[j].innerHTML) {
                index = j;
                thisnum = NumList[j].innerHTML;
            }
        }
        result[i]=NumList.splice(index,1,NumList[i]);
    }
    Content.innerHTML="";
    for(var i=0;i<result.length;i++){
        Content.appendChild(result[i]);
    }
}*/
left_in.onclick=function () {
    insert(this);
};

right_in.onclick=function () {
    insert(this);
};

left_delete.onclick=function () {
    Delete(this);
};

right_delete.onclick=function () {
    Delete(this);
};

sorter.onclick=function () {
    sort();
};
//监听器是啥！
Content.addEventListener('click',function (e) {
   Content.removeChild(e.target);
});