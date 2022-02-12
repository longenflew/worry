var orgid = 560185693
var index_load
layui
    .use(
        ['form', 'table', 'laypage', 'layer', 'jquery', 'element'],
        function() {
            var worrytable = layui.table;
            var form = layui.form;
            var laypage = layui.laypage;
            var element = layui.element;
            var html;
            $ = layui.$;
            index_load = layer.load(1, { //icon0-2 加载中,页面显示不同样式
                shade: [0.5, color], //0.4为透明度 ，#000 为颜色
                content: '数据正在生成，非静止画面，给👴等等...',
                success: function(layero) {
                    layero.find('.layui-layer-content').css({
                        'padding-top': '40px', //图标与样式会重合，这样设置可以错开
                        'width': '400px', //文字显示的宽度
                        'font-size': '20px'
                    });
                }
            });

            let urlticket = getQueryString("ticket");
            if (urlticket != null) {
                $("#href_search").attr("href", "./search.html?ticket=" + urlticket);
                $('#href_analysis').attr("href", "./analysis.html?ticket=" + urlticket);
                $("#href_similarworry").attr("href", "./similarworry.html?ticket=" + urlticket);
            } else {
                layer.open({
                    btn: ["知道了"],
                    content: '希望你是点错地方了！赶紧点确定滚到搜索去！！',
                    end: function() {
                        $(location).attr("href", "./search.html")
                    }
                })
            }

            function guessworrycate() {
                let data = [];
                worrys.forEach(function(index) {
                    data.push(index.processId);
                })
                index_load = layer.load(1, { //icon0-2 加载中,页面显示不同样式
                    shade: [0.5, '#009688'], //0.4为透明度 ，#000 为颜色
                    content: '正在分析，给👴等等...',
                    success: function(layero) {
                        layero.find('.layui-layer-content').css({
                            'padding-top': '40px', //图标与样式会重合，这样设置可以错开
                            'width': '400px', //文字显示的宽度
                            'font-size': '20px'
                        });
                    }
                });
                $.ajax({
                    url: "worrysingle/guessworryitem",
                    type: "post",
                    data: JSON.stringify(data),
                    headers: {
                        'Content-Type': 'application/json;charset=utf-8'
                    },
                    success: function(data) {
                        var list = data.data;
                        for (var i = 0; i < list.length; i++) {
                            let id = list[i].processId;
                            for (var j = 0; j < worrys.length; j++) {
                                if (id == worrys[j].processId) {
                                    worrys[j].categorytype = list[i].categorytype
                                    worrys[j].supercategory = list[i].supercategory
                                }
                            }
                        }
                        dataList({ 'data': worrys })
                        layer.close(index_load);
                        layer.alert("分析成功", {
                            title: "success"
                        });
                    },
                    error: function(data) {
                        layer.close(index_load);
                        layer.alert(JSON
                            .stringify(data), {
                                title: "error"
                            });
                    }
                });
            }

            $('#btn_guessworrycate').click(function() {
                guessworrycate();
            });
            $('#btn_worryfavorites').click(function() {
                watchRemarks(33);
            })
            $('#btn_worrycomment').click(function() {
                watchRemarks(31);
            })

            function watchRemarks(remarkType) {
                if (worryIndex < 0)
                    return;

                let w = $('#body_worry').width();
                let h = $('#body_worry').height();
                stream_index = layer.open({
                    type: 1,
                    content: $('#body_worry'),
                    offset: ['50px', '100px'],
                    shade: 0.5,
                    closeBtn: 0,
                    fixed: true,
                    area: ['550px', '800px'],
                    anim: 4,
                });
                if (remarkType == 33) {
                    if (worrys[worryIndex].worryItem.hasOwnProperty("beautyTag")) {
                        if (worrys[worryIndex].worryItem.beautyTag == null) {
                            worrys[worryIndex].worryItem.beautyTag = 0;
                        }
                    } else {
                        worrys[worryIndex].worryItem.beautyTag = 0;
                    }
                    if (worrys[worryIndex].worryItem.beautyTag == 1) {
                        layer_beauty(0);
                    } else {
                        layer_unbeauty(0);
                    }
                } else if (remarkType == 31) {
                    layer.open({
                        type: 1,
                        content: '<form class="layui-form" >' + '<div id="timeline_remarks"></div>' +
                            '<div class="layui-form-item layui-form-text" style="text-align: center;">' +
                            '<textarea placeholder="请输入内容" style="height: 300px;min-width: 500px;margin: auto;max-width: 600px;" id="textarea_remark" class="layui-textarea" style="height: 400px"></textarea>' +
                            '<button type="button" class="layui-btn" lay-submit="" lay-filter="sub_remark">发表评论</button></div></form>',
                        title: "请写下你的观点",
                        offset: ['50px', '700px'],
                        area: ['800px', '600px'],
                        btn: ["关闭"],
                        shade: 0,
                        end: function(index) {
                            layer.close(stream_index);
                        }
                    });
                    $(".layui-btn").each(function() {
                        $(this).css("background-color", color);
                    });
                    postRemark(31)
                }
                rendeRemarks(worrys[worryIndex].processId, remarkType)
            }

            function postRemark(type) {
                form.on('submit(sub_remark)', function(data) {
                    let updata = $('#textarea_remark').val()
                    if (updata.length > 0)
                        $.post("worrysingle/comment", {
                            username: username,
                            remark: updata,
                            remarkType: type,
                            processId: worrys[worryIndex].processId
                        }, function(data) {
                            layer.alert("成功");
                            rendeRemarks(worrys[worryIndex].processId, type)
                        })
                    return false;
                });
            }

            usernameSet = function() {
                if (username === 'sb') {
                    layer.open({
                        type: 1,
                        content: '<form class="layui-form">' +
                            '<div class="layui-form-item layui-form-text">' +
                            '<input type="text"  id="username" placeholder="请输入你的用户名" autocomplete="off" class="layui-input">' +
                            '</div></form>',
                        title: "加载时先留名吧",
                        offset: 'auto',
                        btn: ["确定"],
                        id: 'layer_input_username',
                        success: function(layero) {
                            layer.setTop(layero); //重点2
                        },
                        yes: function(index, layero) {
                            username = $('#username').val();
                            layer.close(index);
                        }
                    });

                }
            }

            function layer_beauty(type) {
                layer.open({
                    type: 1,
                    content: '<form class="layui-form" ><h1>该工单具有收藏价值</h1>' + '<div id="timeline_remarks" style="margin: auto;width: 700px"></div>' +
                        '<div class="layui-form-item layui-form-text" style="text-align: center;">' +
                        '<textarea placeholder="您也可以写下你的意见" id="textarea_remark" class="layui-textarea" style="height: 300px;min-width: 500px;margin: auto;max-width: 600px;"></textarea>' +
                        '<button type="button" class="layui-btn" lay-submit="" lay-filter="sub_remark">发表评论</button></div></form>',
                    title: "工单拙见",
                    area: ['800px', '600px'],
                    offset: ['50px', '700px'],
                    shade: 0,
                    id: 'layer_beauty',
                    anim: 2,
                    btn: ["变成一般工单", "关闭"],
                    end: function() {
                        if (type == 0)
                            layer.close(stream_index);
                        else {
                            type = 0;
                            rendeRemarks(worrys[worryIndex].processId, 33);
                        }
                    },
                    yes: function(index, layero) {
                        let btn_worryf = $('#btn_worryfavorites');
                        layer.confirm('这条工单很一般了？', { icon: 3, title: '提示' }, function(index1) {
                            worrys[worryIndex].worryItem.beautyTag = 0;
                            btn_worryf.attr('class', 'layui-btn layui-btn-sm layui-icon layui-icon-star');
                            btn_worryf.css("background-color", "white");
                            btn_worryf.css("color", color);
                            layer.close(index);
                            layer.close(index1);
                            type = 1;
                            layer_unbeauty(0);
                        });
                    }
                });
                $(".layui-btn").each(function() {
                    $(this).css("background-color", color);
                });
                postRemark(33)
            }

            function layer_unbeauty(type) {
                layer.open({
                    type: 1,
                    content: '<form class="layui-form"><h1>该工单目前很一般</h1>' + '<div id="timeline_remarks"></div>' +
                        '<div class="layui-form-item layui-form-text">' +
                        '</div></form>',
                    title: "工单拙见",
                    area: ['800px', '600px'],
                    offset: ['50px', '700px'],
                    shade: 0,
                    btn: ["认为有价值", "关闭"],
                    id: 'layer_unbeauty',
                    anim: 1,
                    end: function() {
                        if (type == 0)
                            layer.close(stream_index);
                        else {
                            type = 0;
                            rendeRemarks(worrys[worryIndex].processId, 33);
                        }
                    },
                    yes: function(index, layero) {
                        let btn_worryf = $('#btn_worryfavorites');
                        layer.confirm('这条工单就有价值了？', { icon: 3, title: '提示' }, function(index1) {
                            worrys[worryIndex].worryItem.beautyTag = 1;
                            btn_worryf.attr('class', 'layui-btn layui-btn-sm layui-icon layui-icon-star-fill');
                            btn_worryf.removeAttr("style", "");
                            btn_worryf.css("background-color", color);
                            layer.close(index);
                            layer.close(index1);
                            type = 1;
                            layer_beauty(0);
                        });
                    }
                });
            }
            layer.confirm('苟日新，日日新，又日新。工单列表存在新版的界面，去尝试一下吧',{
                btn: ["去","不去"]}
                ,function() {
                    $(location).attr("href", "http://10.242.37.152/worrylist/index.html?ticket="+getQueryString("ticket"));
                },function(){
                    usernameSet();}
            );


            function rendeRemarks(pid, type) {
                $.ajax({
                    url: "worrysingle/watchremark?processId=" +
                        pid + "&type=" + type,
                    type: "GET",
                    async: false,
                    success: function(data) {
                        let remarks = data.data;
                        let htmlstr = "";
                        for (var j = 0; j < remarks.length; j++) {
                            let remark = remarks[j];
                            htmlstr += "<li class='layui-timeline-item'>" +
                                "<i class='layui-icon layui-timeline-axis'></i>" +
                                "<div class='layui-timeline-content layui-text' ><h4 class='layui-timeline-title'>" +
                                '于' + remark.lastTime + remark.username + " 说：</h4>";

                            htmlstr += "<h3 >";
                            htmlstr += pufily(remark.remark) + "</h3></div>"
                            "</div></li>"
                        }
                        $('#timeline_remarks').empty();
                        $('#timeline_remarks').append(htmlstr);
                    }
                })

            }

            worryview = function(i) {
                if (i < 0) {
                    document.getElementById("processTitle").innerHTML = "";
                    document.getElementById("content").innerHTML = "";
                    document.getElementById("stream").innerHTML = "";
                    document.getElementById("supercategory").value = "";
                    document.getElementById("findsimilarh").href = "";
                    document.getElementById("categorytype").value = "";
                    document.getElementById("supercategory").value = "";
                    document.getElementById("worryremark").value = "";
                    document.getElementById("personlist").innerHTML = "";
                    worryIndex = -1;
                    return;
                }
                document.getElementById("processTitle").innerHTML = pufily(worrys[i].processTitle);
                document.getElementById("content").innerHTML = pufily(worrys[i].content);
                var htmlstream = document.getElementById("stream");
                var htmlstr = "";
                var ddstr = "";
                var luckycolor = [];
                var supcatname;
                document.getElementById("findsimilarh").href = "worrysingle/metamon?processId=" + worrys[i].processId.toString() + "&ticket=" + getQueryString("ticket");
                document.getElementById("categorytype").value = worrys[i].worryItem.categorytype;
                if (worrys[i].worryItem.supercategory != null &&
                    (worrys[i].worryItem.supercategory != "" && worrys[i].worryItem.supercategory != 100)) {
                    document.getElementById("supercategory").value = worrys[i].worryItem.supercategory;
                    for (var k = 0; k < supcatype.length; k++) {
                        if (supcatype[k].cat == worrys[i].worryItem.supercategory)
                            supcatname = supcatype[k].catname;
                    }
                } else {
                    document.getElementById("supercategory").value = "";
                }
                if (worrys[i].worryItem.remarks != null) {
                    document.getElementById("worryremark").value = worrys[i].worryItem.remarks;
                } else {
                    document.getElementById("worryremark").value = "";
                }
                let btn_worryf = $('#btn_worryfavorites');
                if (worrys[i].worryItem.beautyTag == 1) {
                    btn_worryf.attr('class', 'layui-btn layui-btn-sm layui-icon layui-icon-star-fill');
                    btn_worryf.removeAttr("style", "");
                } else {
                    btn_worryf.attr('class', 'layui-btn layui-btn-sm layui-icon layui-icon-star');
                    btn_worryf.css("background-color", "white");
                    btn_worryf.css("color", "#009688");
                }


                for (var k = 0; k < worrys[i].lucky.length; k++) {
                    var color;
                    if (luckycolor.length == 0)
                        color = 9688
                    else
                        color = luckycolor[luckycolor.length - 1] + 2000;
                    luckycolor.push(color);
                    ddstr += '<span class="layui-badge-rim"><a href=\"#' + worrys[i].lucky[k] + '\"><strong>' +
                        worrys[i].lucky[k] + '</strong></a></span>';

                }
                document.getElementById("personlist").innerHTML = ddstr;

                for (var j = 0; j < worrys[i].stream.length; j++) {
                    var stream = worrys[i].stream[j];
                    // var department=stream.department[0]+">"+stream.department[1]+">"+stream.department[2]
                    htmlstr += "<li class='layui-timeline-item'>" +
                        "<i class='layui-icon layui-timeline-axis'></i>" +
                        "<div class='layui-timeline-content layui-text' ><h4 class='layui-timeline-title'>" +
                        stream.department + "</h4>";
                    if (stream.keyperson != null)
                        htmlstr += "<div style='background:#ff0000' id='" + stream.usernick + "'>";
                    else
                        htmlstr += "<div style='background:white'>";
                    htmlstr += stream.usernick + " " + stream.handle + "<br>";

                    htmlstr += "<h3 >";
                    htmlstr += pufily(stream.answer) + "</h3></div>"
                    "</div></li>"
                }
                htmlstream.innerHTML = htmlstr;
            }
            $.post('catchsupcat?type=st',
                function(data) {
                    supcatype = data.data;
                    var supselect = $("#supercategory");
                    for (var i = 0; i < supcatype.length; i++) {
                        supselect.append("<option value=" + supcatype[i].cat + ">" + supcatype[i].catname + "</option>");
                    }
                    form.render("select");
                });

            function dataLists(pageNum, numPerPage) {
                $.post('worryarr/hunterprey?ticket=' +
                    getQueryString("ticket"), {
                        page: pageNum, // 页码数
                        limit: numPerPage
                            // 每页条数
                    },
                    function(data) {
                        let datalist = data;
                        worrys = data.data;
                        worryCount = data.count;
                        dataList(datalist);
                        // 数据传到 table组件
                        page(datalist); // 数据传到 分页组件
                        layer.close(index_load);
                        if (init === 0) {
                            getGuessedState(1);
                            init = 1;
                        }
                    })
            }


            dataLists(PAGE, LIMIT);


            function checkGuessedResult(ticket) {
                if (ticket == null) {
                    $('#h1_guess_state').html("似乎出现意外了");
                    $("#guess_result").html('<button id="btn_guessworrycater" style="margin: auto;text-align: center;" class="layui-btn layui-btn layui-btn-danger">要不重试一下？</button>');
                    $('#btn_guessworrycater').click(function() {
                        guessworrylist();
                    });
                } else {
                    $("#guess_result").html('<div class="layui-btn-container"><a href="analysis.html?ticket=' + ticket + '"><button class="layui-btn layui-btn layui-btn-danger">康康结果</button></a>' +
                        '<button id="btn_reguessworrycater" class="layui-btn layui-btn layui-btn-danger">重新分析</button></div>');
                    element.progress('guess_progress', 100 + '%');
                    $(".layui-btn").each(function() {
                        $(this).addClass("layui-btn-danger");
                    });
                    dataLists(PAGE, LIMIT);
                    worryview(-1)
                    color = "#FF5722";
                    $("#btn_reguessworrycater").click(function() {
                        guessworrylist();
                    });
                    return true;
                }
            }

            function guessingView(data) {
                let b = false;
                if (data.success) {
                    if (data.data.hasOwnProperty("guessingSonTicket") && data.data.guessingSonTicket != null) {
                        let html = "该工单列表子列表正在更新：";
                        data.data.guessingSonTicket.forEach(function(data) {
                            html += "<a href='worrylist.html?ticket=" + data + "' target='_blank' >点击查看</a><br>";
                        })
                        $("#h1_guess_state").html(html);
                    } else if (data.data.hasOwnProperty("guessingFatherTicket") && data.data.guessingFatherTicket != null) {
                        let html = "该工单列表父列表正在更新：";
                        data.data.guessingFatherTicket.forEach(function(data) {
                            html += "<a href='worrylist.html?ticket=" + data + "' target='_blank' >点击查看</a><br>";
                        })
                        $("#h1_guess_state").html(html)
                    } else if (data.data.lock) {
                        $("#h1_guess_state").html("由于有子列表正在分析，分析暂停");
                    } else {
                        $("#h1_guess_state").html("正在处理");
                    }
                    if (data.data.finished === true) {
                        checkGuessedResult(data.data.ticket);
                        b = true;
                    } else if (data.data.now > 0) {
                        element.progress('guess_progress', (data.data.now / data.data.sum * 100).toFixed(2) + '%');
                    } else if (data.data.now === 0)
                        element.progress('guess_progress', 0 + '%');
                } else {
                    checkGuessedResult()
                    b = true;
                }
                return b;
            }

            function getGuessedState(type) {
                var errortimes = 0;
                let types = type;
                let b = false;
                $.ajax({
                    url: "worryarr/getworryitemguess_state?ticket=" + urlticket,
                    timeout: 5000,
                    async: false,
                    error: function(xmlHttpRequest, error) {
                        errortimes++;
                        if (errortimes > 3) {
                            checkGuessedResult()
                            if (type === 0)
                                b = true;
                        }
                    },
                    success: function(data, status) {
                        errortimes = 0
                        console.log(status);
                        if (data.success) {
                            if (types == 1 && (data.data.hasOwnProperty("now") || data.data.finished === true)) {
                                let index = layer.open({
                                    type: 1,
                                    offset: "rt",
                                    closeBtn: 0,
                                    area: ['300px', '200px'],
                                    fixed: true,
                                    content: '<div id="guess_result" style="text-align: center;margin-top: 10px;"><h1 id="h1_guess_state" >正在处理：</div>' +
                                        '</h1><br><div class="layui-progress layui-progress-big" lay-showPercent="true" lay-filter="guess_progress" style="width: 250px;margin: auto;" >\n' +
                                        '  <div class="layui-progress-bar layui-bg-red" lay-percent="100%"></div>' +
                                        '</div>',
                                    shade: 0 //不显示遮罩
                                        ,
                                    id: "layer_guess"
                                });
                                element.render();
                                getWorryitemGuessState(data);
                            } else
                                b = guessingView(data);
                        }
                    }
                });
                return b;
            }

            function getWorryitemGuessState(data) {
                var t = false
                if (data != null && data.code == '200') {
                    t = guessingView(data);
                }
                if (!t) {
                    var interval = setInterval(function() {
                        if (getGuessedState(0))
                            clearInterval(interval);
                    }, 6000);
                }
            }

            document.getElementById("out").href = "worryarr/worryoutput" + "?ticket=" + urlticket + "&all=true";

            function guessworrylist() {
                layer.confirm('是否对所有工单进行分析？预计时间' + (worryCount * 0.4 / 60).toFixed(1) + '分钟', {
                    icon: 3,
                    title: '提示'
                }, function(index1) {
                    $.post("worryarr/aoeguessworryitem", {
                        ticket: urlticket,
                    }, function(data) {
                        let index = layer.open({
                            type: 1,
                            offset: "rt",
                            closeBtn: 0,
                            area: ['300px', '200px'],
                            fixed: true,
                            content: '<div id="guess_result" style="text-align: center;margin-top: 10px;"><h1 id="h1_guess_state" >正在处理</div>' +
                                '</h1><br><div class="layui-progress layui-progress-big" lay-showPercent="true" lay-filter="guess_progress" style="width: 250px;margin: auto;" >\n' +
                                '  <div class="layui-progress-bar layui-bg-red" lay-percent="0%"></div>' +
                                '</div>',
                            shade: 0 //不显示遮罩
                                ,
                            id: "layer_guess"
                        });
                        element.render();
                        layer.close(index1)
                        getWorryitemGuessState(data);
                    });

                });
            }

            $("#btn_guessworrylist").click(function() {
                guessworrylist();

            });

            function page(data) {
                laypage
                    .render({
                        elem: 'worrypage', //注意，这里的 page 是 ID，不用加 # 号
                        count: data.count, //数据总数，从服务端得到
                        limit: LIMIT, // 每页条数
                        layout: ['page', 'skip', 'prev',
                            'next', 'count'
                        ],
                        theme: color,
                        groups: 3,
                        width: 80,
                        prev: '保存并上一页',
                        next: '保存并下一页',
                        jump: function(obj, first) {
                            //首次不执行
                            if (!first) {
                                //do something
                                PAGE = obj.curr
                                numpage(obj.curr, obj.limit) // 分页点击传参 
                                if (changed == 1) {
                                    worryIndex = -1;
                                    layer.msg('已上0传当前页面');
                                    worryup();
                                }
                                changed = 0;
                            }
                        }
                    });
            }

            // 从新写了 一个请求
            function numpage(pageNum, numPerPage) {
                $.post('worryarr/hunterprey?ticket=' +
                    getQueryString("ticket"), {
                        page: pageNum,
                        limit: LIMIT
                    },
                    function(data) {
                        let datalist = data;
                        worrys = data.data;
                        dataList(datalist); // 传到table组件
                        layer.close(index_load);
                    })
            }

            // 表格渲染
            function dataList(data) {
                worrytable.render({
                    elem: '#worrylist',
                    cols: [
                        [{
                            field: 'processId',
                            width: 150,
                            title: '工单ID',
                            hide: true
                        }, {
                            field: 'processKey',
                            width: 170,
                            title: '工单编码'
                        }, {
                            field: 'processTitle',
                            width: 300,
                            title: '标题',
                        }]
                    ],
                    heigth: 800,
                    data: data.data, // 数据
                    limit: LIMIT, // 显示的条数

                    //page: true, // 开启分页
                });

            }


            worrytable
                .on(
                    'row(worrylist)',
                    function(obj) {
                        var worry;
                        worrychid = obj.data.processId;
                        worryitem = obj.data.categorytype;
                        var i;
                        for (i = 0; i < worrys.length; i++) {
                            if (worrys[i].processId == worrychid)
                                break;
                        }
                        worryIndex = i;
                        if (worrys[i].content == null) {
                            var lucklist = {};
                            $.ajax({
                                url: "worrysingle/getlucky?processId=" +
                                    worrychid + "&orgId=" + orgid,
                                type: "GET",
                                async: false,
                                success: function(data) {
                                    lucklist = data;
                                }
                            })

                            $.ajax({
                                url: "worrysingle/worryhunter?processId=" +
                                    worrychid,
                                type: "get",
                                headers: {
                                    'Content-Type': 'application/json;charset=utf-8'
                                }, //接口json格式
                                success: function(
                                    data) {
                                    var worry = data.data
                                    worrys[i].processTitle = worry.processTitle;
                                    worrys[i].content = worry.content;
                                    worrys[i].stream = worry.stream;
                                    for (var t = 0; t < worrys[i].stream.length; t++) {
                                        if (lucklist.streamlocation.hasOwnProperty(t))
                                            worrys[i].stream[t].keyperson = true;
                                    }
                                    worrys[i].lucky = lucklist.lucky;
                                    if (!worrys[i].hasOwnProperty("worryItem") || worrys[i].worryItem === null)
                                        worrys[i].worryItem = {};
                                    if (worrys[i].worryItem.categorytype == null || worrys[i].worryItem.categorytype == "") {
                                        worrys[i].worryItem.categorytype = worry.worryItem.categorytype;
                                    }
                                    if (worrys[i].worryItem.supercategory == null || worrys[i].worryItem.supercategory == "") {
                                        worrys[i].worryItem.supercategory = worry.worryItem.supercategory;
                                    }
                                    if (worrys[i].worryItem.remarks == null || worrys[i].worryItem.remarks == "")
                                        worrys[i].worryItem.remarks = worry.worryItem.remarks;
                                    if (worrys[i].worryItem.beautyTag == null || worrys[i].worryItem.beautyTag == "")
                                        worrys[i].worryItem.beautyTag = worry.worryItem.beautyTag;

                                    worryview(i);
                                    form.render('select');
                                    if (worry.log != null && worry.log.length > 0) {
                                        layer.open({
                                            type: 1,
                                            anim: 6,
                                            offset: 'rb',
                                            content: '<div style="padding: 20px 80px;">工单已被标记</div>',
                                            btn: '知道了',
                                            btnAlign: 'c' //按钮居中
                                                ,
                                            shade: 0 //不显示遮罩
                                                ,
                                            time: 3000
                                        });
                                    }
                                    if (worrysptag === 1)
                                        worryspwordview();
                                },
                                error: function(
                                    data) {
                                    layer
                                        .alert(
                                            JSON.stringify(data), {
                                                title: data
                                            });
                                }
                            });
                        } else {
                            worryview(i);
                            if (worrysptag === 1)
                                worryspwordview();
                        }
                        obj.tr
                            .addClass(
                                'layui-table-click')
                            .siblings()
                            .removeClass(
                                'layui-table-click');
                        rowobj = obj.tr;
                        document.getElementById("oop").href = "http://10.236.10.10/uflow/process.do?method=view&processId=" +
                            worrychid;
                        form.render('select');
                    });

            function worryspwordview() {
                if (worryIndex == null)
                    return;
                if (worrys[worryIndex].pspcontent != null && worrys[worryIndex].psptitle != null) {
                    $("#content").html(worrys[worryIndex].pspcontent);
                    $("#processTitle").html(worrys[worryIndex].psptitle);
                    worrys[worryIndex].perspectivity = true;
                    return;
                }
                var bta = " <div class=\"layui-input-block\">\n" +
                    "      <input type=\"checkbox\" checked=\"\" name=\"open\" lay-skin=\"switch\" lay-filter=\"switchTest\" lay-text=''";
                var btb = "'>\n" + "    </div>"
                var updata = { "content": worrys[worryIndex].processTitle };
                $.ajax({
                    url: "worrysingle/worryShit",
                    type: "post",
                    data: JSON.stringify(updata),
                    headers: {
                        'Content-Type': 'application/json;charset=utf-8'
                    },
                    success: function(data) {
                        var list = data.wordList;
                        var html = "";

                        for (var i = 0; i < list.length; i++) {
                            if (list[i].sp)
                            //html+=bta+list[i].content+"|"+list[i].content+btb;
                                html += "<span style=\"color:" + color + ";font-size: 20px\"><strong>[" + list[i].content + "]</strong></span>";
                            else
                                html += "<span>" + list[i].content + "</span>";
                        }
                        worrys[worryIndex].psptitle = html;
                        worrys[worryIndex].perspectivity = true;
                        $("#processTitle").html(worrys[worryIndex].psptitle);
                    },
                    error: function(data) {
                        layer.alert(JSON
                            .stringify(data), {
                                title: "error"
                            });
                    }
                });
                updata = { "content": worrys[worryIndex].content };
                $.ajax({
                    url: "worrysingle/worryShit",
                    type: "post",
                    data: JSON.stringify(updata),
                    headers: {
                        'Content-Type': 'application/json;charset=utf-8'
                    },
                    success: function(data) {
                        var list = data.wordList;
                        var html = "";
                        for (var i = 0; i < list.length; i++) {
                            if (list[i].sp)
                            //html+=bta+list[i].content+"|"+list[i].content+btb;
                                html += "<span style=\"color:" + color + ";font-size: 20px\"><strong>[" + list[i].content + "]</strong></span>";
                            else
                                html += "<span>" + list[i].content + "</span>";
                        }
                        worrys[worryIndex].pspcontent = html;
                        worrys[worryIndex].perspectivity = true;
                        $("#content").html(worrys[worryIndex].pspcontent);
                    },
                    error: function(data) {
                        layer.alert(JSON
                            .stringify(data), {
                                title: "error"
                            });
                    }
                });

            }

            $("#contentsegbt").click(function() {
                worrysptag = worrysptag * (-1);
            });
            $("#spwordup").click(function() {
                $.ajax({
                    url: "worrysingle/worryShit",
                    type: "post",
                    data: JSON.stringify(updata),
                    headers: {
                        'Content-Type': 'application/json;charset=utf-8'
                    },
                    success: function(data) {
                        var list = data.wordList;
                        var html = "";
                        for (var i = 0; i < list.length; i++) {
                            if (list[i].sp)
                                html += "<span style=\"color:#20b2aa;\"><strong>" + list[i].content + "</strong></span>";
                            else
                                html += "<span>" + list[i].content + "</span>";
                        }
                        worrys[worryIndex].pspcontent = html;
                        worrys[worryIndex].perspectivity = true;
                        $("#content").html(worrys[worryIndex].pspcontent);
                    },
                    error: function(data) {
                        layer.alert(JSON
                            .stringify(data), {
                                title: "error"
                            });
                    }
                });
            });

            function worryup() {
                worryset();
                var worryitem = [];
                for (var i = 0; i < worrys.length; i++) {
                    if (!worrys[i].hasOwnProperty("worryItem"))
                        worrys[i].worryItem = {};
                    var temp = {};
                    if (worrys[i].worryItem.categorytype != null && worrys[i].worryItem.categorytype != "")
                        temp.categorytype = worrys[i].worryItem.categorytype;
                    if (worrys[i].worryItem.supercategory != null && worrys[i].worryItem.supercategory != "")
                        temp.supercategory = worrys[i].worryItem.supercategory;
                    if (worrys[i].worryItem.remarks != null && worrys[i].worryItem.remarks != "")
                        temp.remarks = worrys[i].worryItem.remarks;
                    if (worrys[i].worryItem.beautyTag != null && worrys[i].worryItem.beautyTag != "")
                        temp.beautyTag = worrys[i].beautyTag;
                    if (Object.keys(temp).length > 0) {
                        temp.processId = worrys[i].processId;
                        worryitem.push(temp);
                    }
                }
                var updata = {
                    "data": worryitem
                };

                $.ajax({
                    url: "worrysingle/worrycook",
                    type: "post",
                    data: JSON.stringify(updata),
                    headers: {
                        'Content-Type': 'application/json;charset=utf-8'
                    },
                    success: function(data) {
                        layer.msg(data);
                    },
                    error: function(data) {
                        layer.alert(JSON
                            .stringify(data), {
                                title: "error"
                            });
                    }
                });
            }

            form.on('submit(categorysub)', function(data) {
                worryup();
                return false;
            });
            form.on('select(categorytype)', function(data) {
                worryset();
                return false;
            });
            form.on('select(supercategory)', function(data) {
                worryset();
                return false;
            });
        });

function getQueryString(name) {
    let reg = new RegExp("(^|&)" + name +
        "=([^&]*)(&|$)", "i");
    let r = window.location.search.substr(1).match(
        reg);
    if (r != null) {
        return decodeURIComponent(r[2]);
    };
    return null;
}