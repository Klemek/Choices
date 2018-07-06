//ajax global to handle all ajax calls with authentication (X-Token header)
var ajax = {
    base: '/api',
    call: function (method, URI, data, callback, onerror) {
        var o = {
            method: method,
            url: this.base + URI
        };

        if (typeof data === 'function') {
            onerror = callback;
            callback = data;
        } else if (typeof data === 'object') {
            o.data = data;
        }

        function cbwrap(data) {
            data = data.value ? data.value : data;
            if (callback)
                callback(data);
        }

        function failwrap(data) {
            if (data.status === 401)
                window.location.href = "/login?redirect=" + encodeURI(window.location.href);

            console.error("Error in request " + o.url + " :");
            console.error(data);

            if (onerror)
                onerror(data);
        }

        $.ajax(o).done(cbwrap).fail(failwrap);

    }
};

var roomid = undefined;
var autorefresh = undefined;
var currentstate = undefined;
var currentmembers = undefined;

(function(a){(jQuery.browser=jQuery.browser||{}).mobile=/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))})(navigator.userAgent||navigator.vendor||window.opera);

function addAlert(type,text){
    $('#alerts').append('<div class="alert alert-'+type+' alert-dismissible fade show"><button type="button" class="close" data-dismiss="alert">&times;</button>'+text+'</div>')
    $('#alerts:last-child').hide().fadeIn();
}

function setRoom(tmproomid, master, pushstate){
    roomid = tmproomid;
    if (pushstate) {
        window.history.pushState(null, "Room " + roomid, window.location.pathname + "?roomid=" + roomid);
    }

    $(window).bind('beforeunload', function(){
        var dialogText = 'Are you sure you want to quit ?';
        if(master)
            dialogText += ' This will delete the room';
        return dialogText;
    });

    $(window).on('unload',function(){
        $.ajax({
            type: 'DELETE',
            url: '/api/room/'+roomid+'/quit',
            async:false,
            data: {}
        });
    });
}

function addMember(member) {
    member.imageUrl = member.imageUrl.split('sz=')[0];
    var html = '<div id="' + member.id + '" class="col-4" title="kick ' + member.name + '"><div class="jumbotron bg-dark member-card text-center text-white">' +
        '<img class="rounded-circle" src="' + member.imageUrl + 'sz=42"/>' +
        '<h4><small>' + member.name + '</small></h4>' +
        '</div></div>'
    $("#members").append(html);
    $("#" + member.userId).click(function () {
        //todo ask for kick
    });
}

function setMemberBg(memberid, bg) {
    var card = $('#' + memberid + ' .member-card');
    card.removeClass(function (index, className) {
        return (className.match(/(^|\s)bg-\S+/g) || []).join(' ');
    });
    card.addClass("bg-" + bg);
}

function ajaxRefreshRoom(){
    ajax.call('GET', '/room/'+roomid, function (data) {
        refreshRoom(data);
    }, function(){
        $(window).unbind('beforeunload');
        window.location.href = "/";
    });
}

function refreshRoom(room) {

    $("#room-name").text("Room "+room.id);

    if(room.users.toString() !== currentmembers){
        $("#members").html("");
        room.users.forEach(function(member){
            addMember(member);
        });
    }

    if(room.state !== currentstate || room.users.toString() !== currentmembers){
        switch(room.state){
            case "REGISTERING":
                $("#answers").hide();
                $("#room-text").html('<i class="fas fa-spinner fa-spin"></i> Waiting for members...');
                $('#btn-next').text("Start questions");
                break;
            case "ANSWERING":
                $("#answers").show();
                $("#room-text").html('<i class="far fa-question-circle"></i> Question '+(room.round+1)+'/'+room.roundCount+' : '+room.question);

                $('#answer-a').attr("class","btn btn-danger btn-block btn-lg");
                $('#answer-a').html(room.answers[0]);

                $('#answer-b').attr("class","btn btn-success btn-block btn-lg");
                $('#answer-b').html(room.answers[1]);

                $('#answer-c').attr("class","btn btn-info btn-block btn-lg");
                $('#answer-c').html(room.answers[2]);

                $('#answer-d').attr("class","btn btn-warning btn-block btn-lg");
                $('#answer-d').html(room.answers[3]);

                $('#btn-next').text("See results");
                break;
            case "RESULTS":
                $('#answer-a').attr("class","btn btn-outline-danger btn-block btn-lg");
                $('#answer-b').attr("class","btn btn-outline-success btn-block btn-lg");
                $('#answer-c').attr("class","btn btn-outline-info btn-block btn-lg");
                $('#answer-d').attr("class","btn btn-outline-warning btn-block btn-lg");
                switch(room.correctAnswer){
                    case 1:
                        $('#answer-a').attr("class","btn btn-danger btn-block btn-lg");
                        break;
                    case 2:
                        $('#answer-b').attr("class","btn btn-success btn-block btn-lg");
                        break;
                    case 3:
                        $('#answer-c').attr("class","btn btn-info btn-block btn-lg");
                        break;
                    case 4:
                        $('#answer-d').attr("class","btn btn-warning btn-block btn-lg");
                        break;
                }

                room.users.forEach(function(member){
                    switch(member.answer){
                        case 0:
                            setMemberBg(member.id, 'secondary');
                            break;
                        case 1:
                            setMemberBg(member.id, 'danger');
                            break;
                        case 2:
                            setMemberBg(member.id, 'success');
                            break;
                        case 3:
                            setMemberBg(member.id, 'info');
                            break;
                        case 4:
                            setMemberBg(member.id, 'warning');
                            break;
                    }
                });

                if(room.round+1 === room.roundCount){
                    $('#btn-next').text("Finish");
                }else{
                    $('#btn-next').text("Next question");
                }


                break;
            case "CLOSED":

                room.users.forEach(function(member){
                    setMemberBg(member.id, 'secondary');
                });

                $("#answers").hide();
                $("#room-text").html('Finished !');
                $('#btn-next').hide();
                break;
        }
    }

    if(room.state === "ANSWERING"){
        room.users.forEach(function(member){
            if(member.answer === 0)
                setMemberBg(member.id, 'secondary');
            else
                setMemberBg(member.id, 'dark');
        });
    }

    currentstate = room.state;
    currentmembers = room.users.toString();
}

function joinRoom(tmproomid, pushstate) {
    ajax.call('POST', '/room/' + tmproomid + '/join', function () {
        $('#menu-view').hide();
        $('#loading').hide();
        $('#pad-view').show();

        setRoom(tmproomid, false, pushstate);
    }, function(){
        addAlert("danger","Cannot find room "+tmproomid);

        if(window.location.search && window.location.search.length > 0){
            window.history.pushState(null, "Choices", "/");
        }

        $('#roomid').val('');
        $('#menu-view').show();
        $('#loading').hide();
    });
}

function answerQuestion(val){
    ajax.call('POST', '/room/'+roomid+'/answer/'+val);
}

//check session at start
ajax.call('GET', '/session', function (data) {
    if (data.userId) {
        $(document).ready(function () {
            data.userImageUrl = data.userImageUrl.split('sz=')[0];

            $('#user').show();
            $('#user').attr("title", data.userName);
            $('.user-name').text(data.userName);
            $('.user-mail').text(data.userEmail);
            $('.user-image-nav').attr("src", data.userImageUrl + "sz=42");
            $('.user-image-full').attr("src", data.userImageUrl + "sz=70");

            var url = new URL(window.location);
            var tmproomid = url.searchParams.get("roomid");

            if(jQuery.browser.mobile){
                $('#div-create-room').hide();
                $('#div-join-room').attr("class","col-12");
            }

            if (tmproomid) {
                joinRoom(tmproomid);
            } else {
                $('#loading').hide();
                $('#menu-view').show();
            }
        });
    } else {
        window.location.href = "/login?redirect=" + encodeURI(window.location.href);
    }
}, function(){
    window.location.href = "/login?redirect=" + encodeURI(window.location.href);
});

$(document).ready(function () {
    //main menu
    $("#btn-create").click(function () {
        $('#loading').show();
        $('#menu-view').hide();

        ajax.call('PUT', '/room/create', function (data) {
            $('#loading').hide();
            $('#room-view').show();
            setRoom(data.id, true, true);
            refreshRoom(data);
            $('#btn-auto-refresh').click();
        }, function(){
            $('#loading').hide();
            $('#menu-view').show();
            addAlert("danger","Cannot create room");
        });
    });
    $("#btn-join").click(function () {
        var tmproomid = $('#roomid').val();
        if(tmproomid && tmproomid.length > 0){
            joinRoom(tmproomid, true);
        }
    });

    $('#roomid').keypress(function (e) {
        if (e.which === 13) {
            $("#btn-join").click();
        }
    });

    //room view
    $("#btn-next").click(function () {
        ajax.call('POST', '/room/'+roomid+'/next', function (data) {
            refreshRoom(data);
        }, function(){
            addAlert("warning","Cannot edit room, please retry");
        });
    });
    $("#btn-refresh").click(ajaxRefreshRoom);
    $("#btn-auto-refresh").click(function () {
        if(autorefresh){
            clearInterval(autorefresh);
            autorefresh = undefined;
            $('#btn-auto-refresh').html("<i class=\"far fa-square\"></i> Auto-Refresh");
            $('#btn-refresh').show();
        }else{
            autorefresh = setInterval(ajaxRefreshRoom,1000);
            $('#btn-auto-refresh').html("<i class=\"far fa-check-square\"></i> Auto-Refresh");
            $('#btn-refresh').hide();
        }
    });

    $("#btn-delete").click(function(){
        if(confirm("Are you sure you want to delete the room ?")){
            $(window).unbind('beforeunload');
            window.location.href = "/";
        }
    });


    //pad view
    $("#btn-a").click(function () {
        answerQuestion(1);
    });
    $("#btn-b").click(function () {
        answerQuestion(2);
    });
    $("#btn-c").click(function () {
        answerQuestion(3);
    });
    $("#btn-d").click(function () {
        answerQuestion(4);
    });
});