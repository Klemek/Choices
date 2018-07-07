var room = {
    id: undefined,
    autoRefresh: undefined,
    currentState: undefined,
    currentMembers: undefined,
    answers: undefined,
    showAnswers: undefined,
    showStats: undefined,
    stats: undefined,

    set: function (tmproomid, master, pushstate) {
        this.id = tmproomid;

        this.stats = [];

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
                    ui.updateRoomView(
                        this.id,
                        ui.getRoomText(
                            data.state,
                            data.roundCount
                        ),
                        'Start questions'
                    );
                    break;
                case "ANSWERING":
                    ui.updateRoomView(
                        this.id,
                        ui.getRoomText(
                            data.state,
                            data.roundCount,
                            data.round,
                            data.question
                        ),
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
                    var correct = room.answers.indexOf(0) + 1,
                        total = 0,
                        answered = {1: 0, 2: 0, 3: 0, 4: 0};

                    data.users.forEach(function (member) {
                        if (member.answer > 0) {
                            total++;
                            answered[member.answer]++;
                        }
                    });

                    this.stats.push([answered[correct], total]);

                    ui.updateRoomView(
                        this.id,
                        ui.getRoomText(
                            data.state,
                            data.roundCount,
                            data.round,
                            data.question
                        ),
                        data.round + 1 === data.roundCount ? 'Finish' : 'Next question',
                        true
                    );

                    ['A', 'B', 'C', 'D'].forEach(function (ans, i) {
                        ui.updateAnswer(
                            ans,
                            i === correct - 1,
                            data.answers[room.answers[mapping.letterToAnswer[ans] - 1]],
                            room.showStats ? answered[mapping.letterToAnswer[ans]] : undefined,
                            room.showStats ? total : undefined
                        );
                    });
                    break;
                case "CLOSED":
                    ui.updateRoomView(
                        this.id,
                        ui.getRoomText(
                            data.state,
                            data.roundCount
                        )
                    );
                    break;
            }
        }

        if (data.users.toString() !== this.currentMembers) {
            data.users.forEach(function (member) {
                if (data.state === "RESULTS" && this.showAnswers)
                    ui.setMemberBg(member.id, mapping.answerToColor[member.answer]);
                else
                    ui.setMemberBg(member.id, member.answer === 0 ? 'secondary' : 'dark');
            });
        }

        this.currentState = data.state;
        this.currentMembers = JSON.stringify(data.users);
    },
    //button events
    create: function (packIndex, showAnswers, showStats, lockRoom) {
        ui.loading();
        ajax.call('PUT', '/room/create?pack=' + packIndex + (lockRoom ? '&lock' : ''), function (data) {
            ui.showView('room');
            room.set(data.id, true, true);
            room.refresh(data);
            room.changeAutoRefresh();
            room.showAnswers = showAnswers;
            room.showStats = showStats;
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
