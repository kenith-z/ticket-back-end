$("#ticket").click(function(){
        var enName = $("#jx").val();
        var cnName = $("#gjj").val();
        var price = $("#vip").val();
        var uscontent = {"itemId": itemId,"enName": enName,"cnName":cnName,"price":price};
        $.ajax({
            type: "POST",
            url: "/office/modify",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(uscontent),
            dataType : "json", // 指定返回类型
            success: function(data){
                str="";
                var str="<td>"+count+"</td><td id='itemId'>"+itemId+"</td><td>"+data[0].cnName+"</td><td>"+data[0].enName+"</td><td>"+data[0].price+"</td><td><a href=\"javascript:Modify('"+count+"','"+itemId+"')\">修改</a> <a href=\"javascript:delItem('"+itemId+"','"+cnName+"')\">删除</a></td>";
                $("#row_"+itemId).html(str);
            }
        });

    });
}