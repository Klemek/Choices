var room = {
    id: undefined,
    autoRefresh: undefined,
    currentState: undefined,
    currentMembers: undefined,
    answers: [],
    showAnswers: undefined,
    alwaysShowStats: undefined,
    showStats: undefined,
    stats: undefined,
    members: undefined,
    locked: undefined,

    precreate: function () {
        ui.views.loading();
        ajax.call('GET', '/questions/list', function (data) {
            ui.views.showView('create');
            ui.loadQuestionPacks(data);
        }, function () {
            ui.views.showView('menu');
            ui.addAlert('danger', 'Cannot load question packs, please retry');
        });
    },
    set: function (tmproomid, master, pushstate) {
        this.id = tmproomid;

        this.stats = [];
        this.members = {};

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
        ui.views.loading();
        ajax.call('POST', '/room/' + tmproomid + '/join', function () {

            ui.views.showView('pad');

            room.set(tmproomid, false, pushstate);
        }, function (data) {

            if (window.location.search && window.location.search.length > 0) {
                window.history.pushState(null, "Choices", "/");
            }
            ui.views.showView('menu');

            switch (data.status) {
                case 403:
                    ui.addAlert("danger", "Cannot join locked room " + tmproomid);
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
    refresh: function (data, force) {
        if (data.users.toString() !== this.currentMembers) {
            ui.room.clearMembers();
            data.users.forEach(function (member) {
                ui.room.addMember(member);
            });
        }

        var correct = this.answers.indexOf(0) + 1,
            total = 0,
            answered = {1: 0, 2: 0, 3: 0, 4: 0};

        data.users.forEach(function (member) {
            if (member.answer > 0) {
                total++;
                answered[member.answer]++;
            }
        });

        ui.room.setLock(!data.lock);
        ui.room.setStats(!room.showStats);

        if (data.state !== this.currentState || force) {
            switch (data.state) {
                case "REGISTERING":
                    ui.room.updateView(
                        this.id,
                        data.lock,
                        globals.getRoomText(
                            data.state,
                            data.roundCount
                        ),
                        'Start questions'
                    );
                    ui.room.disableStats(true);
                    break;
                case "ANSWERING":
                    ui.room.updateView(
                        this.id,
                        data.lock,
                        globals.getRoomText(
                            data.state,
                            data.roundCount,
                            data.round,
                            data.question.text
                        ),
                        'See results',
                        true,
                        data.question.hint
                    );
                    ui.room.disableStats(room.alwaysShowStats);

                    if (!force)
                        this.answers = Math.shuffle([0, 1, 2, 3]);

                    ['A', 'B', 'C', 'D'].forEach(function (ans) {
                        ui.room.updateAnswer(
                            ans,
                            true,
                            data.question.answers[room.answers[mapping.letterToAnswer[ans] - 1]]
                        );
                    });
                    ui.room.fitAnswers();
                    break;
                case "RESULTS":
                    data.users.forEach(function (member) {
                        if (!room.members[member.id]) {
                            room.members[member.id] = jQuery.extend({}, member);
                            delete room.members[member.id].answer;
                            room.members[member.id].correct = 0;
                        }
                        if (member.answer === correct)
                            room.members[member.id].correct += 1;
                        console.log(member, room.members[member.id]);
                    });
                    this.stats.push([answered[correct], total, data.users.length]);
                    ui.room.updateView(
                        this.id,
                        data.lock,
                        globals.getRoomText(
                            data.state,
                            data.roundCount,
                            data.round,
                            data.question.text
                        ),
                        data.round + 1 === data.roundCount ? 'Finish' : 'Next question',
                        true,
                        data.question.hint
                    );
                    ui.room.disableStats(room.alwaysShowStats);

                    ['A', 'B', 'C', 'D'].forEach(function (ans, i) {
                        ui.room.updateAnswer(
                            ans,
                            i === correct - 1,
                            data.question.answers[room.answers[mapping.letterToAnswer[ans] - 1]],
                            room.showStats ? answered[mapping.letterToAnswer[ans]] : undefined,
                            room.showStats ? data.users.length : undefined
                        );
                    });
                    ui.room.fitAnswers();
                    break;
                case "CLOSED":
                    $(window).unbind('beforeunload');
                    ui.room.updateView(
                        this.id,
                        data.lock,
                        globals.getRoomText(
                            data.state,
                            data.roundCount
                        )
                    );
                    ui.room.showFinalResults(this.stats, this.members);
                    ui.room.disableStats(true);
                    break;
            }
        }

        if (data.state === "ANSWERING") {
            ui.room.disableStats(total < data.users.length);
            if (room.showStats && total === data.users.length) {
                ['A', 'B', 'C', 'D'].forEach(function (ans) {
                    ui.room.updateAnswer(
                        ans,
                        true,
                        data.question.answers[room.answers[mapping.letterToAnswer[ans] - 1]],
                        answered[mapping.letterToAnswer[ans]],
                        total
                    );
                });
            }
        }

        if (data.users.toString() !== this.currentMembers) {
            data.users.forEach(function (member) {
                if (data.state === "RESULTS" && this.showAnswers)
                    ui.room.setMemberBg(member.id, mapping.answerToColor[member.answer]);
                else
                    ui.room.setMemberBg(member.id, member.answer === 0 ? 'secondary' : 'dark');
            });
        }

        this.locked = data.lock;
        this.currentState = data.state;
        this.currentMembers = JSON.stringify(data.users);
    },
//button events
    create: function (packIndex, showAnswers, showStats, lockRoom, autoRefresh) {
        ui.views.loading();
        var data = {
            packId: packIndex
        };
        ajax.call('PUT', '/room/create', data, function (data) {
            ui.views.showView('room');
            room.set(data.id, true, true);
            room.refresh(data);
            if (autoRefresh)
                room.changeAutoRefresh();
            room.showAnswers = showAnswers;
            room.alwaysShowStats = showStats;
            room.showStats = showStats;
        }, function () {
            ui.views.showView('menu');
            ui.addAlert("danger", "Cannot create room");
        });
    },
    next: function () {
        ui.room.closeHint();
        room.showStats = room.alwaysShowStats;
        ajax.call('POST', '/room/' + room.id + '/next', function (data) {
            room.refresh(data);
        }, function () {
            ui.addAlert("warning", "Cannot edit room, please retry");
        });
    },
    ajaxRefresh: function (force) {
        ajax.call('GET', '/room/' + room.id, function (data) {
            room.refresh(data, force);
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
        ui.room.setAutoRefresh(room.autoRefresh);
    },
    delete: function () {
        if (room.currentState === "CLOSED" || window.confirm("Are you sure you want to delete the room ?")) {
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
            ui.views.showView('menu');
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
    },
    changeLock: function () {
        ajax.call('POST', '/room/' + room.id + (room.locked ? '/unlock' : '/lock'), function (data) {
            room.refresh(data, true);
        }, function () {
            ui.addAlert("warning", "Cannot " + (room.locked ? 'unlock' : 'lock') + " room, please retry");
        });
    },
    changeStats: function () {
        room.showStats = !room.showStats;
        room.ajaxRefresh(true);
    }
};
