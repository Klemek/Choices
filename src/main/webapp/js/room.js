/* exported room */
const room = {
    id: undefined,
    // room options
    autoRefresh: undefined,
    setSize: undefined,
    autoLock: undefined,
    //gathered data
    /**
     * @type {{total:int,right:int,wrong:int,unanswered:int,score:int}[]}
     */
    stats: undefined, //questions answers
    incorrects: undefined,
    errorLinks: {},
    members: undefined, //room all time members + answers data
    // current room state
    locked: undefined, //room current lock status
    currentState: undefined, //current room state
    currentMembers: undefined, //string to compare and detect changes
    round: undefined, //current round
    pack: undefined, //all questions
    currentQuestion: undefined, //self explaining
    answers: [], //order of answers
    groups: {},
    score: undefined,
    voting: undefined,
    currentTeacher: undefined,
    firstSubgroups: undefined,
    //results
    startTime: undefined,
    videos: undefined,
    teachers: undefined,

    /**
     * Set the current room data
     * @param {string} tmproomid - the id to set
     * @param {boolean} pushstate - if the page title / url need to be updated
     */
    set: function (tmproomid, pushstate) {
        this.id = tmproomid;

        if (pushstate)
            utils.setPage('Room ' + this.id, 'roomid=' + this.id);

        //ask before leaving
        $(window).bind('beforeunload', function () {
            return lang.getString('askQuit');
        });

        //sync ajax request to quit/delete room on unload
        $(window).on('unload', function () {
            $.ajax({
                type: 'DELETE',
                url: '/api/room/' + room.id + '/delete',
                async: false,
                data: {}
            });
        });
    },
    /**
     * Load question list to be shown in the "create" view
     * @param {function} callback - function to call when loaded
     */
    precreate: function (callback) {
        ui.views.loading();
        ajax.call('GET', '/questions/list',
            /**
             * @param {{id:int,name:string,questionCount:int}[]} data
             */
            function (data) {
                ui.views.showView('create');
                ui.loadQuestionPacks(data);
                if (callback)
                    callback();
            }, function () {
                ui.views.showView('menu');
                ui.alert('danger', lang.getString('errorPackLoad'));
            });
    },
    /**
     * Compute data to refresh the room
     * @param {{users:{id:string,name:string,imageUrl:string,answer:int}[]}} [data]
     * @param {boolean} [force]
     */
    refreshMembers: function (data, force) {
        const tmpMembers = room.currentMembers ? JSON.parse(room.currentMembers) : [];

        if (!data || !data.users) {
            data = {
                users: tmpMembers
            };
        }

        Object.keys(camera.members).forEach(function (k) {
            let add = true;
            data.users.forEach(function (u, i) {
                if (u.id === k) {
                    add = false;
                    data.users[i].answer = camera.members[k];
                }
            });
            if (add) {
                let newMember = true;
                tmpMembers.forEach(function (member) {
                    if (member.id === k)
                        newMember = false;
                });
                if (!room.locked || !newMember) {
                    data.users.push({
                        id: '' + k,
                        name: lang.getString('guestName').format(k),
                        imageUrl: '',
                        answer: camera.members[k],
                        generated: true
                    });
                }
            }
        });

        const newMembers = JSON.stringify(data.users);

        if (newMembers !== this.currentMembers) {
            ui.room.clearMembers();
            data.users.forEach(function (member) {
                ui.room.addMember(member);
            });
        }
        let unanswered;
        if (force || newMembers !== this.currentMembers) {
            switch (this.currentState) {
                case 'REGISTERING':
                    ui.room.lockNextButton(false);
                    break;
                case 'VOTE':
                    let m = Object.values(this.members);
                    m.sort(function (a, b) {
                        return b.correct - a.correct;
                    });
                    for (let i = m.length - 1; i >= 0; i--)
                        if (m[i].correct === 0 || m[i].wasSelected)
                            m = utils.removeIndex(m, i);
                    m = m.slice(0, 3);
                    if (m.length > 1)
                        m.push({name: lang.getString('textSkipTeaching')});
                    const answered = {1: 0, 2: 0, 3: 0, 4: 0};
                    unanswered = 0;
                    data.users.forEach(function (member, i) {
                        if (room.voting || !room.members[member.id].selected) {
                            if (member.answer > 0) {
                                if (room.voting && m[member.answer - 1] && m[member.answer - 1].id === member.id) {
                                    data.users[i].answer = 0;
                                    unanswered++;
                                } else {
                                    answered[member.answer]++;
                                }
                            } else {
                                unanswered++;
                            }
                        }
                    });
                    if (unanswered === 0 || (this.voting && m.length <= 1)) {
                        if (this.voting) {
                            const a = utils.getOrderedDict(answered);
                            const selected = m.length === 1 ? 1 : a[0][0];
                            ['A', 'B', 'C', 'D'].forEach(function (ans, i) {
                                ui.room.updateAnswer(
                                    ans,
                                    i === selected - 1,
                                    m[mapping.letterToAnswer[ans] - 1] !== undefined ?
                                        m[mapping.letterToAnswer[ans] - 1].name : ''
                                );
                            });
                            if (m.length === 0) {
                                ui.room.hideAnswers();
                                ui.room.changeRoomText(lang.getString('textNoTeacher'));
                                ui.room.setTimer(globals.messageTimer, function () {
                                    room.next();
                                });
                            } else if (!m[selected - 1].id) {
                                ui.room.setTimer(globals.messageTimer, function () {
                                    room.next();
                                });
                            } else {
                                this.selectTeacher(m[selected - 1].id);
                            }
                        } else {
                            if (answered[1] >= data.users.length / 2) {
                                ui.room.lockNextButton(false);
                                ui.room.showMessage(lang.getString('textNext'));
                                ui.room.setTimer(globals.messageTimer, function () {
                                    room.next();
                                });
                            } else {
                                Object.keys(this.members).forEach(function (memberId) {
                                    if (room.members[memberId].selected) {
                                        room.members[memberId].wasSelected = true;
                                        room.members[memberId].selected = undefined;
                                    }
                                });
                                camera.resetAnswers();
                                const ar = room.autoRefresh !== undefined;
                                if (ar)
                                    room.changeAutoRefresh();
                                ajax.call('POST', '/room/' + room.id, {reset: true},
                                    /**
                                     * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[]}} data
                                     */
                                    function (data) {
                                        room.locked = data.lock;
                                        ui.room.setLock(!data.lock);
                                        room.voting = true;
                                        room.refreshState('VOTE');
                                        room.refreshMembers(data, true);
                                        if (ar)
                                            room.changeAutoRefresh();
                                    }, function () {
                                        ui.alert('warning', lang.getString('errorRoomEdit'));
                                    });
                            }

                        }
                    }
                    break;
                case 'RESULTS':
                    if (!this.stats[this.round]) {
                        //update stats
                        const correct = this.answers.indexOf(0) + 1;
                        const answered = {1: 0, 2: 0, 3: 0, 4: 0};
                        let total = 0;
                        data.users.forEach(function (member) {
                            if (member.answer > 0) {
                                total++;
                                answered[member.answer]++;
                            }
                        });
                        this.stats[this.round] = {
                            total: total,
                            right: answered[correct],
                            wrong: total - answered[correct],
                            unanswered: data.users.length - total,
                            score: this.score
                        };

                        //compute biggest error and get link
                        const a = utils.getOrderedDict(answered);
                        let link;
                        for (let i = 0; i < 4; i++) {
                            link = this.currentQuestion.links[this.answers[a[i][0] - 1]];
                            if (a[i][0] !== correct && a[i][1] > 0 && link && link.length > 0) {
                                const sublinks = link.split(';');
                                for (let i = 0; i < sublinks.length; i++) {
                                    if (!this.errorLinks[sublinks[i]])
                                        this.errorLinks[sublinks[i]] = 0;
                                    this.errorLinks[sublinks[i]]++;
                                }
                                break;
                            }
                        }

                        data.users.forEach(function (member) {
                            if (!room.members[member.id]) {
                                room.members[member.id] = jQuery.extend({
                                    correct: 0,
                                    avoid: [],
                                    groupTeaching: 0,
                                    teachingFailed: 0
                                }, member);
                            }
                            if (member.answer === correct)
                                room.members[member.id].correct += 1;
                            room.members[member.id].lastCorrect = member.answer === correct;
                            room.members[member.id].corrected = member.answer === correct;
                        });

                        ['A', 'B', 'C', 'D'].forEach(function (ans, i) {
                            ui.room.updateAnswer(
                                ans,
                                i === correct - 1,
                                room.currentQuestion.answers[room.answers[mapping.letterToAnswer[ans] - 1]],
                                answered[mapping.letterToAnswer[ans]],
                                data.users.length
                            );
                        });
                        this.createGroups();
                        if (this.incorrects === 0) { //perfect
                            this.score++;
                        } else {
                            this.score -= globals.questionPenalty;
                            if (this.score < 0)
                                this.score = 0;
                        }
                        this.refreshProgress();
                    } else if (Object.keys(this.groups).length > 0 && this.incorrects > 0) {
                        data.users.forEach(function (member) {
                            if (room.members[member.id]) {
                                if (!room.members[member.id].corrected)
                                    room.members[member.id].answer = member.answer;
                                else
                                    room.members[member.id].answer = 0;
                            }
                        });
                        let unanswered = 0;
                        Object.values(this.groups).forEach(function (grp) {
                            grp.forEach(function (member) {
                                if (room.members[member.id].answer < 1 || room.members[member.id].answer > 2) {
                                    unanswered++;
                                }
                            });
                        });
                        if (unanswered === 0) {
                            let rejected = false;
                            Object.keys(this.groups).forEach(function (id) {
                                rejected = false;
                                room.groups[id].forEach(function (member) {
                                    if (room.members[member.id].answer === 1) {
                                        room.members[member.id].corrected = true;
                                    } else if (room.members[member.id].answer === 2) {
                                        room.members[member.id].avoid.push(id);
                                        rejected = true;
                                    }
                                });
                                if (rejected)
                                    room.members[id].teachingFailed++;
                            });
                            camera.resetAnswers();
                            const ar = room.autoRefresh !== undefined;
                            if (ar)
                                room.changeAutoRefresh();
                            ajax.call('POST', '/room/' + room.id, {reset: true},
                                /**
                                 * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[]}} data
                                 */
                                function (data) {
                                    room.locked = data.lock;
                                    ui.room.setLock(!data.lock);
                                    room.refreshMembers(data);
                                    if (ar)
                                        room.changeAutoRefresh();
                                    room.createGroups();
                                }, function () {
                                    ui.alert('warning', lang.getString('errorRoomEdit'));
                                });

                        }
                    }
                    break;
            }
            unanswered = 0;
            data.users.forEach(function (member, i) {
                if (member.answer === 0)
                    unanswered++;
                let color = member.answer === 0 ? 'secondary' : 'dark';
                switch (room.currentState) {
                    case 'REGISTERING':
                        color = mapping.answerToColor[member.answer];
                        break;
                    case 'VOTE':
                        if (!room.voting && room.members[member.id].selected) {
                            color = 'secondary';
                        } else if (!room.voting) {
                            color = mapping.answerToColor[member.answer];
                        }
                        break;
                    case 'RESULTS':
                        if (room.members[member.id] && !room.members[member.id].corrected) {
                            color = mapping.answerToColor[member.answer];
                        }
                        break;
                }
                ui.room.setMemberBg(member.id, color, !tmpMembers[i] || tmpMembers[i].answer !== member.answer);
                if (member.generated)
                    ui.room.updateCameraMember(member);
            });

            if (unanswered === 0 && data.users.length > 0) {
                switch (room.currentState) {
                    case 'VIDEO':
                    case 'ANSWERING':
                    case 'HELPVIDEO':
                        ui.room.setTimer(globals.lastAnswerTimer, function () {
                            room.next();
                        });
                        break;
                }
            }
        }


        this.currentMembers = JSON.stringify(data.users);
    },
    /**
     * Compute data to refresh the room
     * @param {string} newState
     */
    refreshState: function (newState) {
        ui.room.updateView(
            this.id,
            this.locked,
            newState,
            this.currentQuestion,
            this.round,
            this.pack.name
        );
        ui.room.hideMessage();
        ui.room.hideVideo();
        ui.room.hideStats();
        ui.room.lockNextButton(false);
        this.refreshProgress();
        switch (newState) {
            case 'REGISTERING':
                ui.room.lockNextButton(true);
                ui.room.hideRoomProgress();
                break;
            case 'VIDEO':
                ui.room.showVideo(this.pack.video);
                ui.room.hideRoomProgress();
                break;
            case 'ANSWERING':
                camera.lockChange(false);
                ['A', 'B', 'C', 'D'].forEach(function (ans) {
                    ui.room.updateAnswer(
                        ans,
                        true,
                        room.currentQuestion.answers[room.answers[mapping.letterToAnswer[ans] - 1]]
                    );
                });
                break;
            case 'RESULTS':
                this.firstSubgroups = true;
                Object.keys(this.members).forEach(function (memberId) {
                    room.members[memberId].avoid = [];
                });
                break;
            case 'VOTE':
                if (this.voting) {
                    ui.room.showStats(this.stats);
                    ui.room.showMessage(lang.getString('textMidPoint'));

                    let m = Object.values(this.members);
                    m.sort(function (a, b) {
                        return b.correct - a.correct;
                    });
                    for (let i = m.length - 1; i >= 0; i--) {
                        if (m[i].correct === 0 || m[i].wasSelected)
                            m = utils.removeIndex(m, i);
                    }
                    m = m.slice(0, 3);
                    if (m.length > 1)
                        m.push({name: lang.getString('textSkipTeaching')});
                    ['A', 'B', 'C', 'D'].forEach(function (ans) {
                        ui.room.updateAnswer(
                            ans,
                            m[mapping.letterToAnswer[ans] - 1] !== undefined,
                            m[mapping.letterToAnswer[ans] - 1] !== undefined ?
                                m[mapping.letterToAnswer[ans] - 1].name : ''
                        );
                    });
                    if (m.length === 0) {
                        ui.room.lockNextButton(false);
                        ui.room.hideAnswers();
                        ui.room.changeRoomText(lang.getString('textNoTeacher'));
                        ui.room.setTimer(globals.messageTimer, function () {
                            room.next();
                        });
                    } else if (m.length === 1) {
                        this.selectTeacher(m[0].id);
                    } else {
                        ui.room.lockNextButton(true);
                    }
                } else {
                    let questionIndex = 0;
                    let minimal = 1;
                    this.stats.forEach(function (s, i) {
                        if (s.right / s.total < minimal && s.total >= 1) {
                            minimal = s.right / s.total;
                            questionIndex = i;
                        }
                    });

                    const question = this.pack.questions[questionIndex % this.pack.questions.length];

                    ['A', 'B', 'C', 'D'].forEach(function (ans) {
                        ui.room.updateAnswer(
                            ans,
                            ans === 'A',
                            question.answers[mapping.letterToAnswer[ans] - 1]
                        );
                    });

                    ui.room.updateView(
                        this.id,
                        this.locked,
                        newState,
                        this.currentQuestion,
                        this.round,
                        this.pack.name
                    );

                    ui.room.changeRoomText(lang.getString('stateVote2').format(this.members[this.currentTeacher].name) +
                        '<br/>' + question.text);

                }

                break;
            case 'HELPVIDEO':
                const a = utils.getOrderedDict(this.errorLinks);
                const video = a[0] ? a[0][0] : '';
                this.videos.push(video);
                ui.room.showVideo(video, true);
                this.errorLinks = {};
                break;
            case 'CLOSED':
                $(window).unbind('beforeunload');
                ui.room.showStats(this.stats);
                ui.room.showMessage(this.pack.message);
                ui.room.lockNextButton(true);
                this.sendResults();
                break;
        }

        ui.room.finishView();

        this.currentState = newState;
    },
    /**
     * Select a teacher to teach the class at midpoint
     * @param {string} memberId
     */
    selectTeacher: function (memberId) {
        this.teachers[this.teachers.length - 1].push(memberId);
        this.members[memberId].selected = true;
        this.currentTeacher = memberId;
        ui.room.lockNextButton(false);
        ui.room.changeRoomText(lang.getString('stateVote2').format(this.members[memberId].name));
        camera.resetAnswers();
        const ar = room.autoRefresh !== undefined;
        if (ar)
            room.changeAutoRefresh();
        ajax.call('POST', '/room/' + room.id, {reset: true},
            /**
             * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[]}} data
             */
            function (data) {
                room.locked = data.lock;
                ui.room.setLock(!data.lock);
                room.voting = false;
                room.refreshMembers(data);
                room.refreshState('VOTE');
                if (ar)
                    room.changeAutoRefresh();
                ui.room.showMessage(lang.getString('textVote').format(room.setSize - room.score));
            }, function () {
                ui.alert('warning', lang.getString('errorRoomEdit'));
            });
    },
    /**
     * Update the current room progress
     */
    refreshProgress: function () {
        ui.room.setRoomProgress(100 * this.score / this.setSize, this.setSize - this.score);
    },
    /**
     * Create work groups from question results
     */
    createGroups: function () {
        let correctGroup = [], incorrectGroup = [];
        const tmpGroups = {};
        Object.values(room.members).forEach(function (member) {
            if (member.lastCorrect) {
                correctGroup.push(member);
                tmpGroups[member.id] = [];
            }
            else if (!member.corrected)
                incorrectGroup.push(member);
        });
        this.incorrects = incorrectGroup.length;
        if (incorrectGroup.length > 0 && correctGroup.length > 0) {
            //low score first
            correctGroup.sort(function (a, b) {
                return (a.correct > b.correct) ? 1 : ((b.correct > a.correct) ? -1 : 0);
            });
            incorrectGroup.sort(function (a, b) {
                return (a.correct > b.correct) ? 1 : ((b.correct > a.correct) ? -1 : 0);
            });
            let step, tmpGrp, remainCorrect, change;
            const computeMaster = function (master) {
                step = Math.ceil((incorrectGroup.length) / (remainCorrect));
                tmpGrp = incorrectGroup.filter(function (m) {
                    return m.avoid.indexOf(master.id) < 0;
                }).slice(0, step);
                tmpGroups[master.id] = tmpGroups[master.id].concat(tmpGrp);
                incorrectGroup = incorrectGroup.filter(function (m) {
                    return !tmpGrp.includes(m);
                });
                change = tmpGrp.length > 0;
                remainCorrect--;
            };
            do {
                change = false;
                remainCorrect = correctGroup.length;
                correctGroup.forEach(computeMaster);
            } while (change);
        }

        this.groups = {};
        Object.keys(tmpGroups).forEach(function (key) {
            if (tmpGroups[key].length > 0) {
                room.groups[key] = tmpGroups[key];
                room.members[key].groupTeaching++;
            }
        });

        if (Object.keys(this.groups).length === 0) {
            if (this.firstSubgroups) {
                if (this.incorrects > 0) {
                    ui.room.showMessage(lang.getString('textAllWrong'));
                } else {
                    ui.room.showMessage(lang.getString('textNoWrong'));
                }
            } else if (this.incorrects > 0) {
                ui.room.showMessage(lang.getString('textNoGroups'));
            }
            ui.room.setTimer(globals.messageTimer, function () {
                room.next();
            });
        } else {
            let texts = [];
            let master;
            let names;
            const uniqueTeacher = Object.keys(this.groups).length === 1 && correctGroup.length === 1;
            Object.keys(this.groups).forEach(function (id) {
                master = room.members[id];
                if (master && room.groups[id].length > 0) {
                    names = [];
                    room.groups[id].forEach(function (m) {
                        names.push(m.name);
                    });
                    if (uniqueTeacher)
                        texts.push(lang.getString('textTeachAll')
                            .format(master.name));
                    else
                        texts.push(lang.getString('textTeachers')
                            .format(master.name, lang.join(names)));
                }
            });
            ui.room.showMessage('<small>' + texts.join('<br/>') + '<br/>' + lang.getString('textVote').format(room.setSize - room.score) + '</small>');
        }
        this.firstSubgroups = false;
    },
    //button events
    /**
     * Create a new room with parameters
     * @param {int} packIndex - the id of the pack to use
     * @param {int} setSize
     * @param {boolean} lockRoom
     * @param {boolean} autoRefresh
     * @param {function} callback
     */
    create: function (packIndex, setSize, lockRoom, autoRefresh, callback) {
        ui.views.loading();
        const data = {
            packId: packIndex
        };
        this.autoLock = lockRoom;
        ajax.call('PUT', '/room/create', data,
            /**
             * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[],pack:{id:int,name:string,video:string,message:string,questions:{text:string,answers:string[],links:string[]}[]}}} data
             */
            function (data) {
                room.set(data.id, true);
                room.pack = data.pack;
                utils.shuffle(room.pack.questions);
                room.round = 0;
                room.score = 0;
                room.setSize = setSize;
                room.stats = [];
                room.members = {};
                room.locked = data.lock;
                room.startTime = new Date().getTime();
                room.videos = [];
                room.teachers = [];
                if (autoRefresh)
                    room.changeAutoRefresh();
                ui.views.showView('room');
                room.refreshMembers(data, true);
                room.refreshState('REGISTERING');
                if (callback)
                    callback();
            }, function () {
                ui.views.showView('menu');
                ui.alert('danger', lang.getString('errorRoomCreate'));
            });
    },
    /**
     * Got to the next room state
     */
    next: function () {
        ui.room.closeExtra();
        let reqData = {
            lockAnswers: false,
            reset: true,
        };
        switch (this.currentState) {
            case 'REGISTERING':
                reqData.lock = this.autoLock;
                room.refreshState('VIDEO');
                break;
            case 'VIDEO':
                this.currentQuestion = this.pack.questions[0];
                this.answers = utils.shuffle([0, 1, 2, 3]);
                room.refreshState('ANSWERING');
                break;
            case 'ANSWERING':
                room.refreshState('RESULTS');
                room.refreshMembers(undefined, true);
                break;
            case 'RESULTS':
                this.round++;
                this.currentQuestion = this.pack.questions[this.round % this.pack.questions.length];
                this.answers = utils.shuffle([0, 1, 2, 3]);
                if (this.score < this.setSize) {
                    if (this.round % this.setSize === 0) {
                        room.refreshState('HELPVIDEO');
                        this.round--;
                    } else if (this.round % this.setSize === Math.floor(this.setSize / 2) &&
                        Object.keys(this.members).length > 1) {
                        this.voting = true;
                        this.teachers.push([]);
                        Object.keys(this.members).forEach(function (memberId) {
                            room.members[memberId].selected = undefined;
                            room.members[memberId].wasSelected = undefined;
                        });
                        room.refreshState('VOTE');
                        this.round--;
                    } else {
                        room.refreshState('ANSWERING');
                    }
                } else {
                    room.refreshState('CLOSED');
                    room.refreshMembers(undefined, true);
                }
                break;
            case 'HELPVIDEO':
            case 'VOTE':
                this.round++;
                this.currentQuestion = this.pack.questions[this.round % this.pack.questions.length];
                this.answers = utils.shuffle([0, 1, 2, 3]);
                room.refreshState('ANSWERING');
                break;
            case 'CLOSED':
                break;
        }
        const ar = room.autoRefresh !== undefined;
        if (ar)
            room.changeAutoRefresh();
        camera.resetAnswers();
        ajax.call('POST', '/room/' + room.id, reqData,
            /**
             * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[]}} data
             */
            function (data) {
                room.locked = data.lock;
                ui.room.setLock(!data.lock);
                room.refreshMembers(data, true);
                if (ar)
                    room.changeAutoRefresh();
            }, function () {
                ui.alert('warning', lang.getString('errorRoomEdit'));
            });
    },
    /**
     * Get the data to refresh the room
     * @param {boolean} [force]
     */
    ajaxRefresh: function (force) {
        ajax.call('GET', '/room/' + room.id,
            /**
             * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[]}} data
             */
            function (data) {
                room.locked = data.lock;
                ui.room.setLock(!data.lock);
                room.refreshMembers(data, force);
            }, function () {
                $(window).unbind('beforeunload');
                window.location.href = globals.appPath + '/';
            });
    },
    /**
     * Change the auto refresh state
     */
    changeAutoRefresh: function () {
        if (room.autoRefresh) {
            clearInterval(room.autoRefresh);
            room.autoRefresh = undefined;
        } else {
            room.autoRefresh = setInterval(room.ajaxRefresh, 1000);
        }
        ui.room.setAutoRefresh(room.autoRefresh);
    },
    /**
     * Delete the current room
     */
    delete: function () {
        if (room.currentState === 'CLOSED' || window.confirm(lang.getString('askRoomDelete'))) {
            $(window).unbind('beforeunload');
            window.location.href = globals.appPath + '/';
        }
    },
    /**
     * Kick a member from the room
     * @param {string} memberId - the member's id
     * @param {string} memberName - the member's name
     */
    kick: function (memberId, memberName) {
        if (window.confirm(lang.getString('askKick').format(memberName))) {
            if (camera.members[memberId]) {
                camera.deleteMember(memberId);
                delete this.members[memberId];
                room.refreshMembers();
                ui.alert('success', lang.getString('infoKick').format(memberName));
            } else {
                const ar = room.autoRefresh !== undefined;
                if (ar)
                    room.changeAutoRefresh();
                ajax.call('DELETE', '/room/' + room.id + '/kick/' + memberId,
                    /**
                     * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[]}} data
                     */
                    function (data) {
                        room.locked = data.lock;
                        ui.room.setLock(!data.lock);
                        delete room.members[memberId];
                        room.refreshMembers(data);
                        if (ar)
                            room.changeAutoRefresh();
                        ui.alert('success', lang.getString('infoKick').format(memberName));
                    }, function () {
                        ui.alert('warning', lang.getString('warnKick').format(memberName));
                    });
            }
        }
    },
    /**
     * Change the lock state of the room
     */
    changeLock: function () {
        const reqData = {
            lock: !room.locked
        };
        const ar = room.autoRefresh !== undefined;
        if (ar)
            room.changeAutoRefresh();
        ajax.call('POST', '/room/' + room.id, reqData,
            /**
             * @param {{id:string,lock:boolean,lockAnswers:boolean,users:{id:string,name:string,imageUrl:string,answer:int}[]}} data
             */
            function (data) {
                ui.alert('success', (room.locked ? lang.getString('infoRoomUnlock') : lang.getString('infoRoomLock')));
                room.locked = data.lock;
                ui.room.setLock(!data.lock);
                room.refreshMembers(data);
                if (ar)
                    room.changeAutoRefresh();
            }, function () {
                ui.alert('warning', (room.locked ? lang.getString('warnRoomUnlock') : lang.getString('warnRoomLock')));
            });
    },
    /**
     * Send the final results to the server
     */
    sendResults: function () {

        const users = [];
        Object.values(this.members).forEach(function (member) {
            users.push({
                id: member.id,
                name: member.name,
                score: member.correct,
                groupTeaching: member.groupTeaching,
                teachingFailed: member.teachingFailed
            });
        });
        const reqData = {
            datetime: this.startTime,
            duration: (new Date().getTime()) - this.startTime,
            target: this.setSize,
            packId: this.pack.id,
            users: JSON.stringify(users),
            questions: JSON.stringify(this.stats),
            videos: JSON.stringify(this.videos),
            teachers: JSON.stringify(this.teachers)
        };
        ajax.call('POST', '/room/' + this.id + '/results', reqData);
    }
};

//# sourceURL=js/room.js