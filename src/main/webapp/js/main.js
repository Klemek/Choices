var ui = {
    addAlert: function (type, text) {
        $('#alerts').append('<div class="alert alert-' + type + ' alert-dismissible fade show"><button type="button" class="close" data-dismiss="alert">&times;</button>' + text + '</div>')
        $('#alerts:last-child').hide().fadeIn();
    },
    addMember: function (member) {
        member.imageUrl = member.imageUrl.split('sz=')[0];
        var html = '<div id="' + member.id + '" class="col-4" title="kick ' + member.name + '"><div class="jumbotron bg-dark member-card text-center text-white">' +
            '<img class="rounded-circle" src="' + member.imageUrl + 'sz=42"/>' +
            '<h4><small>' + member.name + '</small></h4>' +
            '</div></div>';
        $("#members").append(html);
        $("#" + member.userId).click(function () {
            //todo ask for kick
        });
    },
    setMemberBg: function (memberId, bg) {
        var card = $('#' + memberId + ' .member-card');
        card.removeClass(function (index, className) {
            return (className.match(/(^|\s)bg-\S+/g) || []).join(' ');
        });
        card.addClass("bg-" + bg);
    }
};

var room = {
    id: undefined,
    autoRefresh: undefined,
    currentState: undefined,
    currentMembers: undefined,
    answers: undefined,

    set: function (tmproomid, master, pushstate) {
        this.id = tmproomid;

        if (pushstate) {
            window.history.pushState(null, "Room " + this.id, window.location.pathname + "?roomid=" + this.id);
        }

        $(window).bind('beforeunload', function () {
            var dialogText = 'Are you sure you want to quit ?';
            if (master)
                dialogText += ' This will delete the room';
            return dialogText;
        });

        $(window).on('unload', function () {
            $.ajax({
                type: 'DELETE',
                url: '/api/room/' + room.id + '/quit',
                async: false,
                data: {}
            });
        });
    },
    join: function (tmproomid, pushstate) {
        ajax.call('POST', '/room/' + tmproomid + '/join', function () {
            $('#menu-view').hide();
            $('#loading').hide();
            $('#pad-view').show();

            room.set(tmproomid, false, pushstate);
        }, function (data) {

            switch(data.status){
                case 403:
                    ui.addAlert("danger", "Cannot join closed room " + tmproomid);
                    break;
                case 404:
                    ui.addAlert("danger", "Cannot find room " + tmproomid);
                    break;
                default:
                    ui.addAlert("danger", "Cannot join room " + tmproomid);
                    break;
            }

            if (window.location.search && window.location.search.length > 0) {
                window.history.pushState(null, "Choices", "/");
            }

            $('#roomid').val('');
            $('#menu-view').show();
            $('#loading').hide();
        });
    },
    refresh: function (data) {

        $("#room-name").text("Room " + data.id);

        if (data.users.toString() !== this.currentMembers) {
            $("#members").html("");
            data.users.forEach(function (member) {
                ui.addMember(member);
            });
        }

        if (data.state !== this.currentState) {
            switch (data.state) {
                case "REGISTERING":
                    $("#answers").hide();
                    $("#room-text").html('<i class="fas fa-spinner fa-spin"></i> Waiting for members...');
                    $('#btn-next').text("Start questions");
                    break;
                case "ANSWERING":

                    this.answers = Math.shuffle([0,1,2,3]);

                    $("#answers").show();
                    $("#room-text").html('<i class="far fa-question-circle"></i> Question ' + (data.round + 1) + '/' + data.roundCount + ' :<br/>' + data.question);

                    $('#answer-a').attr("class", "btn btn-danger btn-block btn-lg");
                    $('#answer-a').html(data.answers[this.answers[0]]);

                    $('#answer-b').attr("class", "btn btn-success btn-block btn-lg");
                    $('#answer-b').html(data.answers[this.answers[1]]);

                    $('#answer-c').attr("class", "btn btn-info btn-block btn-lg");
                    $('#answer-c').html(data.answers[this.answers[2]]);

                    $('#answer-d').attr("class", "btn btn-warning btn-block btn-lg");
                    $('#answer-d').html(data.answers[this.answers[3]]);

                    $('#btn-next').text("See results");
                    break;
                case "RESULTS":
                    $('#answer-a').attr("class", "btn btn-outline-danger btn-block btn-lg");
                    $('#answer-b').attr("class", "btn btn-outline-success btn-block btn-lg");
                    $('#answer-c').attr("class", "btn btn-outline-info btn-block btn-lg");
                    $('#answer-d').attr("class", "btn btn-outline-warning btn-block btn-lg");
                    switch (this.answers.indexOf(0)) {
                        case 0:
                            $('#answer-a').attr("class", "btn btn-danger btn-block btn-lg");
                            break;
                        case 1:
                            $('#answer-b').attr("class", "btn btn-success btn-block btn-lg");
                            break;
                        case 2:
                            $('#answer-c').attr("class", "btn btn-info btn-block btn-lg");
                            break;
                        case 3:
                            $('#answer-d').attr("class", "btn btn-warning btn-block btn-lg");
                            break;
                    }

                    if (data.round + 1 === data.roundCount) {
                        $('#btn-next').text("Finish");
                    } else {
                        $('#btn-next').text("Next question");
                    }


                    break;
                case "CLOSED":
                    data.users.forEach(function (member) {
                        ui.setMemberBg(member.id, 'secondary');
                    });
                    $("#answers").hide();
                    $("#room-text").html('Finished !');
                    $('#btn-next').hide();
                    break;
            }
        }

        if (data.users.toString() !== this.currentMembers) {
            switch (data.state) {
                case "ANSWERING":
                    data.users.forEach(function (member) {
                        if (member.answer === 0)
                            ui.setMemberBg(member.id, 'secondary');
                        else
                            ui.setMemberBg(member.id, 'dark');
                    });
                    break;
                case "RESULTS":
                    data.users.forEach(function (member) {
                        switch (member.answer) {
                            case 0:
                                ui.setMemberBg(member.id, 'secondary');
                                break;
                            case 1:
                                ui.setMemberBg(member.id, 'danger');
                                break;
                            case 2:
                                ui.setMemberBg(member.id, 'success');
                                break;
                            case 3:
                                ui.setMemberBg(member.id, 'info');
                                break;
                            case 4:
                                ui.setMemberBg(member.id, 'warning');
                                break;
                        }
                    });
                    break;
            }
        }

        this.currentState = data.state;
        this.currentMembers = JSON.stringify(data.users);
    },
    //button events
    ajaxRefresh: function () {
        ajax.call('GET', '/room/' + room.id, function (data) {
            room.refresh(data);
        }, function () {
            $(window).unbind('beforeunload');
            window.location.href = "/";
        });
    },
    answerQuestion: function (val) {
        ajax.call('POST', '/room/' + room.id + '/answer/' + val);
    },
    next: function(){
        ajax.call('POST', '/room/' + room.id + '/next', function (data) {
            room.refresh(data);
        }, function () {
            ui.addAlert("warning", "Cannot edit room, please retry");
        });
    },
    changeAutoRefresh: function(){
        if (room.autoRefresh) {
            clearInterval(room.autoRefresh);
            room.autoRefresh = undefined;
            $('#btn-auto-refresh').html("<i class=\"far fa-square\"></i> Auto-Refresh");
            $('#btn-refresh').show();
        } else {
            room.autoRefresh = setInterval(room.ajaxRefresh, 1000);
            $('#btn-auto-refresh').html("<i class=\"far fa-check-square\"></i> Auto-Refresh");
            $('#btn-refresh').hide();
        }
    }
};


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

            if (jQuery.browser.mobile) {
                $('#div-create-room').hide();
                $('#div-join-room').attr("class", "col-12");
            }

            if (tmproomid) {
                room.join(tmproomid);
            } else {
                $('#loading').hide();
                $('#menu-view').show();
            }
        });
    } else {
        window.location.href = "/login?redirect=" + encodeURI(window.location.href);
    }
}, function () {
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
            room.set(data.id, true, true);
            room.refresh(data);
            $('#btn-auto-refresh').click();
        }, function () {
            $('#loading').hide();
            $('#menu-view').show();
            ui.addAlert("danger", "Cannot create room");
        });
    });
    $("#btn-join").click(function () {
        var tmproomid = $('#roomid').val();
        if (tmproomid && tmproomid.length > 0) {
            room.join(tmproomid, true);
        }
    });

    $('#roomid').keypress(function (e) {
        if (e.which === 13) {
            $("#btn-join").click();
        }
    });

    //room view
    $("#btn-next").click(room.next);
    $("#btn-refresh").click(room.ajaxRefresh);
    $("#btn-auto-refresh").click(room.changeAutoRefresh);

    $("#btn-delete").click(function () {
        if (window.confirm("Are you sure you want to delete the room ?")) {
            $(window).unbind('beforeunload');
            window.location.href = "/";
        }
    });

    //pad view
    $("#btn-a").click(function () {
        room.answerQuestion(1);
    });
    $("#btn-b").click(function () {
        room.answerQuestion(2);
    });
    $("#btn-c").click(function () {
        room.answerQuestion(3);
    });
    $("#btn-d").click(function () {
        room.answerQuestion(4);
    });
});