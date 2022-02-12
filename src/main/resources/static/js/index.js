$(function () {
    //为发布按钮绑定单击事件，调用publish方法
    $("#publishBtn").click(publish);
});

function publish() {
    //隐藏弹出框
    $("#publishModal").modal("hide");

    //发送AJAX请求之前, 将CSRF令牌设置到请求的消息头中.
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e, xhr, options) {
    //     xhr.setRequestHeader(header, token);
    // });

    //获取前端输入的帖子标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    //发送异步请求（POST）
    $.post(
        //请求地址
        CONTEXT_PATH + "/discuss/add",
        //向服务器传送的数据
        {"title": title, "content": content},
        //回调函数：处理服务器响应给浏览器的数据
        function (data) {
            data = $.parseJSON(data);
            //在提示框内显示提示信息
            $("#hintBody").text(data.msg);
            //显示提示框
            $("#hintModal").modal("show");
            //过2秒隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
                //如果发布成功，刷新当前页面
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    )

}
