var aqiData = [
    ["北京", 90],
    ["上海", 50],
    ["福州", 10],
    ["广州", 50],
    ["成都", 90],
    ["西安", 100]
];

(function Start() {

    /*
    在注释下方编写代码
    遍历读取aqiData中各个城市的数据
    将空气质量指数大于60的城市显示到aqi-list的列表中
    */

    var element=document.getElementById("aqi-list");
    var result=[];
    var width=0;
    for(var i=0;i<aqiData.length;i++){
        if(aqiData[i][1]>60){
            result[width]=aqiData[i][0]+"，"+aqiData[i][1];
            width++;
        }
    }


    for(var j=0;j<result.length;j++){
        var list=document.createElement("li");
        var text=document.createTextNode(result[j]);
        list.appendChild(text);
        element.appendChild(list);
    }
})();