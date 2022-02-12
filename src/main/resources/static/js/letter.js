$(function () {
    //对发送私信按钮绑定单击事件，单击时执行send_letter方法
    $("#sendBtn").click(send_letter);
    $(".close").click(delete_msg);
});

function send_letter() {
    //隐藏私信输入框
    $("#sendModal").modal("hide");

    var toName = $("#recipient-name").val();
    var content = $("#message-text").val();

    //以post方式发送异步请求
    $.post(
        CONTEXT_PATH + "/letter/send",
        {"toName": toName, "content": content},
        function (data) {
            //接收服务器传来的数据，判断是否发送成功
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#hintBody").text("发送成功！");
            } else {
                $("#hintBody").text(data.msg);
            }

            //2秒后隐藏提示框，刷新当前页面
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                location.reload();
            }, 2000);
        }
    )
}

function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}