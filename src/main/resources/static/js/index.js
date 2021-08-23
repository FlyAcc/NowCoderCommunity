$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");

    // 发送AJAX请求时，将csrf令牌设置到请求消息头中
    // let token = $("meta[name='_csrf']").attr("content");
    // let header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e, xhr, options) {
    //     xhr.setRequestHeader(header, token);
    // });

    // 获取标题和内容
    let title = $("#recipient-name").val();
    let content = $("#message-text").val();
    /*
    发送异步请求（post）
    形式：$.post(url, data, func, dataType);
    可选参数：
    1）url：链接地址，字符串表示
    2）data：需要发送到服务器的数据，格式为{A: '...', B: '...'}
    3）func：请求成功后，服务器回调的函数；function(data, status, xhr)，
    其中data为服务器回传的数据，status为响应状态，xhr为XMLHttpRequest对象，个人感觉关注data参数即可
    4）dataType：服务器返回数据的格式
     */
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
        function (data) {
            data = $.parseJSON(data);
            // 在提示框显示消息
            $("#hintBody").text(data.msg);

            // 显示提示框
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 刷新
                if (data.code === 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    );
}