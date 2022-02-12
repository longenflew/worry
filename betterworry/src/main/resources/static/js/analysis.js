layui.use(
    ["form", "layedit", "laydate", 'table', "element", "layer", "laypage"],
    function () {
        var form = layui.form,
            layer = layui.layer,
            layedit = layui.layedit,
            laydate = layui.laydate;
        var table = layui.table;
        laydate.render({
            elem: "#time",
            type: "date",
            range: true,
        });
        laydate.render({
            elem: "#handletime",
            type: "date",
            range: true,
        });
        form.verify({
            analysis_input: function (value, item) {
            }
        })
        form.on("submit(searchListFilter)", function (data) {
            //var jsondata = data.field;
            //var start = jsondata.time.substring(0, 10);
            //var end = jsondata.time.substring(12, 23);
            //	delete jsondata.time
            //	jsondata.stratTime = start;
            //	jsondata.endTime = end;
        });
        var element = layui.element;
        var laypage = layui.laypage;
        index = layer.load(1, {
            //icon0-2 加载中,页面显示不同样式
            shade: [0.5, color], //0.4为透明度 ，#000 为颜色
            content: "数据正在生成，非静止画面，给👴等等...",
            success: function (layero) {
                layero.find(".layui-layer-content").css({
                    "padding-top": "40px", //图标与样式会重合，这样设置可以错开
                    width: "400px", //文字显示的宽度
                    "font-size": "20px",
                });
            },
        });

        function dataLists(pageNum, numPerPage) {
            var arg = getQueryString("ticket");
            var url = "worryarr/hunterprey";
            if (arg != null) url = url + "?ticket=" + arg;
            $.post(
                url,
                {
                    page: pageNum,
                    limit: numPerPage,
                },
                function (data) {
                    let datalist = data;
                    worrys = data.data;
                    dataList(datalist);
                    page(datalist); // 传到table组件
                    layer.close(index);
                }
            );
        }

        if (getQueryString("ticket") != null)
            dataLists(1, 15);

        function page(data) {
            laypage.render({
                elem: "worrypage", //注意，这里的 page 是 ID，不用加 # 号
                count: data.count, //数据总数，从服务端得到
                limit: data.limit, // 每页条数
                layout: ["page", "skip", "prev", "next"],
                groups: 3,
                width: 80,
                prev: "上一页",
                next: "下一页",
                jump: function (obj, first) {
                    //首次不执行
                    if (!first) {
                        //do something
                        numpage(obj.curr, obj.limit); // 分页点击传参
                    }
                },
            });
        }



        function analysisrender(data, title, id) {

            layer.close(index);
            table.render({
                elem: "#" + id,
                title: title,
                cols: [
                    [
                        {
                            field: "ticket",
                            hide: true
                        },
                        {
                            field: "anatype",
                            width: 150,
                            title: title,
                            sort: true,
                            align: "center"
                        },
                        {
                            field: "sum",
                            width: 120,
                            title: "总数",
                            sort: true,
                            align: "center",
                            templet: function (d) {
                                return '<a target="_blank" class="layui-table-link" href="./worrylist.html?ticket=' + d.ticket + '">' + d.sum + '</a>'
                            }
                        },
                    ],
                ],
                heigth: 800,
                data: data, // 数据
                limit: data.length,
                count: data.length, // 显示的条数
                //page: true, // 开启分页
            });
        }

        function numpage(pageNum, numPerPage) {
            var arg = getQueryString("ticket");
            var url = "worryarr/hunterprey";
            if (arg != null) url = url + "?ticket=" + arg;
            $.post(
                url,
                {
                    page: pageNum,
                    limit: numPerPage,
                },
                function (data) {
                    let datalist = data;
                    worrys = data.data;
                    dataList(datalist); // 传到table组件
                    layer.close(index);
                }
            );
        }

        // 表格渲染
        function dataList(data) {
            table.render({
                elem: "#worrylist",
                cols: [
                    [
                        {
                            field: "processId",
                            width: 150,
                            title: "工单ID",
                            hide: true,
                        },
                        {
                            field: "processKey",
                            width: 170,
                            title: "工单编码",
                        },
                        {
                            field: "processTitle",
                            width: 300,
                            title: "标题",
                        },
                    ],
                ],
                heigth: 800,
                data: data.data, // 数据
                limit: 15, // 显示的条数
                //page: true, // 开启分页
            });
        }

        form.on("submit(anatype_sub)", function (data) {
            let type = [];
            $.each(data.field, function (index, value) {
                type.push(index);
            });
            if (type.length == 0) {
                layer.alert("无中生有可不行，请选择至少一个类型执行！！！");
                return false;
            } else {
                let updata = {processlist: null, type: type};
                var params = "";
                if (getQueryString("ticket") != null)
                    params += "?ticket=" + getQueryString("ticket");
                $.ajax({
                    url: "worryarr/conclusion" + params,
                    type: "post",
                    data: JSON.stringify(updata),
                    contentType: "application/json;charset=utf-8",
                    beforeSend: function () {
                        index = layer.load(1, {
                            //icon0-2 加载中,页面显示不同样式
                            shade: [0.5, "#009688"], //0.4为透明度 ，#000 为颜色
                            content: "数据正在生成，非静止画面，给👴等等...",
                            success: function (layero) {
                                layero.find(".layui-layer-content").css({
                                    "padding-top": "40px", //图标与样式会重合，这样设置可以错开
                                    width: "400px", //文字显示的宽度
                                    "font-size": "20px",
                                });
                            },
                        });
                    },
                    success: function (data) {
                        $('#list_table_analysis').empty();
                        $.each(data.data, function (index, value) {
                            $('#list_table_analysis').append('<div id="table_' + index + '" style="margin-inline: 15px;display: inline-block;vertical-align: top;text-align: center;"><table id="' + index + '" lay-filter="' + index + '"></table></div>');
                            if (KEYNAME.hasOwnProperty(index))
                                analysisrender(value, KEYNAME[index], index);
                            else
                                analysisrender(value, index, index);
                        });
                    },
                    complete: function () {
                        layer.close(index);
                    },
                    error: function (data) {
                        layer.alert(JSON.stringify(data), {
                            title: "error",
                        });
                    },
                });
                return false;
            }

        });

        layer.close(index);
    }
);

