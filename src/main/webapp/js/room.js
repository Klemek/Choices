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

        if (master) {
            $(window).bind('beforeunload', function () {
                return 'Are you sure you want to quit ? This will delete the room';
            });
        }

        $(window).on('unload', function () {
            $.ajax({
                type: 'DELETE',
                url: '/api/room/' + room.id + (master ? '/delete' : '/quit'),
                async: false,
                data: {}
            });
        });
    },
    join: function (tmproomid, pushstate) {
        ajax.call('POST', '/room/' + tmproomid + '/join', function () {

            ui.showView('pad');

            room.set(tmproomid, false, pushstate);
        }, function (data) {

            if (window.location.search && window.location.search.length > 0) {
                window.history.pushState(null, "Choices", "/");
            }
            ui.showView('menu');

            switch (data.status) {
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
        });
    },
    refresh: function (data) {
        if (data.users.toString() !== this.currentMembers) {
            ui.clearMembers();
            data.users.forEach(function (member) {
                ui.addMember(member);
            });
        }

        if (data.state !== this.currentState) {
            switch (data.state) {
                case "REGISTERING":
                    ui.updateRoomView(this.id,
                        '<i class="fas fa-spinner fa-spin"></i> Waiting for members...',
                        'Start questions'
                    );
                    break;
                case "ANSWERING":
                    ui.updateRoomView(this.id,
                        '<small><i class="fas fa-question-circle"></i> Question ' + (data.round + 1) + '/' + data.roundCount + ' :</small><br/>' + data.question,
                        'See results',
                        true
                    );

                    this.answers = Math.shuffle([0, 1, 2, 3]);
                    ['A', 'B', 'C', 'D'].forEach(function (ans) {
                        ui.updateAnswer(
                            ans,
                            true,
                            data.answers[room.answers[mapping.letterToAnswer[ans] - 1]]
                        );
                    });
                    break;
                case "RESULTS":
                    ui.updateRoomView(this.id,
                        '<small><i class="fas fa-info-circle"></i> Question ' + (data.round + 1) + '/' + data.roundCount + ' :</small><br/>' + data.question,
                        data.round + 1 === data.roundCount ? 'Finish' : 'Next question',
                        true
                    );

                    ['A', 'B', 'C', 'D'].forEach(function (ans, i) {
                        ui.updateAnswer(
                            ans,
                            i === room.answers.indexOf(0)
                        );
                    });
                    break;
                case "CLOSED":
                    ui.updateRoomView(this.id,
                        'Finished !'
                    );
                    break;
            }
        }

        if (data.users.toString() !== this.currentMembers) {
            data.users.forEach(function (member) {
                if (data.state === "RESULTS")
                    ui.setMemberBg(member.id, mapping.answerToColor[member.answer]);
                else
                    ui.setMemberBg(member.id, member.answer === 0 ? 'secondary' : 'dark');
            });
        }

        this.currentState = data.state;
        this.currentMembers = JSON.stringify(data.users);
    },
    //button events
    create: function () {
        ui.loading();
        ajax.call('PUT', '/room/create', function (data) {
            ui.showView('room');
            room.set(data.id, true, true);
            room.refresh(data);
            room.changeAutoRefresh();
        }, function () {
            ui.showView('menu');
            ui.addAlert("danger", "Cannot create room");
        });
    },
    next: function () {
        ajax.call('POST', '/room/' + room.id + '/next', function (data) {
            room.refresh(data);
        }, function () {
            ui.addAlert("warning", "Cannot edit room, please retry");
        });
    },
    ajaxRefresh: function () {
        ajax.call('GET', '/room/' + room.id, function (data) {
            room.refresh(data);
        }, function () {
            $(window).unbind('beforeunload');
            window.location.href = "/";
        });
    },
    changeAutoRefresh: function () {
        if (room.autoRefresh) {
            clearInterval(room.autoRefresh);
            room.autoRefresh = undefined;
        } else {
            room.autoRefresh = setInterval(room.ajaxRefresh, 1000);
        }
        ui.setAutoRefresh(room.autoRefresh);
    },
    delete: function () {
        if (window.confirm("Are you sure you want to delete the room ?")) {
            $(window).unbind('beforeunload');
            window.location.href = "/";
        }
    },
    answerQuestion: function (val) {
        ajax.call('POST', '/room/' + room.id + '/answer/' + val, function () {
        }, function () {
            if (window.location.search && window.location.search.length > 0) {
                window.history.pushState(null, "Choices", "/");
            }
            ui.showView('menu');
            ui.addAlert("danger", "Disconnected from room " + room.id);
        });
    },
    kick: function (memberId, memberName) {
        if (window.confirm("Are you sure you want to kick " + memberName + " ?")) {
            ajax.call('DELETE', '/room/' + room.id + '/kick/' + memberId, function (data) {
                room.refresh(data);
            }, function () {
                ui.addAlert("warning", "Cannot kick " + memberName + ", please retry");
            });
        }
    }
};
