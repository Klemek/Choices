/* exported ui */
const ui = {
    documentReady: false,
    langReady: false,
    userReady: false,
    /**
     * If the ui is ready to init
     * @returns {boolean}
     */
    isReady: function () {
        return ui.documentReady && ui.langReady && ui.userReady;
    },
    /**
     * State that the document is loaded
     */
    setDocumentReady: function () {
        console.log('Document is ready');
        ui.documentReady = true;
        if (ui.isReady())
            ui.initUI();
    },
    /**
     * State that the lang is loaded
     */
    setLangReady: function () {
        console.log('Lang is ready');
        ui.langReady = true;
        if (ui.isReady())
            ui.initUI();
    },
    /**
     * State that the user is loaded
     */
    setUserReady: function () {
        console.log('User is ready');
        ui.userReady = true;
        if (ui.isReady())
            ui.initUI();
    },
    /**
     * Init ui
     */
    initUI: function () {
        if (!ui.isReady())
            return;
        ui.registerEvents();

        document.title = lang.getString('titleMain');
        $('#lang-text-title').html(lang.getString('titleMain'));
        $('.lang-text').each(function () {
            const key = $(this).text().trim();
            $(this).html(lang.getString(key));
        });
        $('#roomid').attr('placeholder', lang.getString('inputRoomId'));

        if (jQuery.browser.mobile) {
            //hide room creation
            $('#div-create-room').hide();
            $('#div-join-room').attr('class', 'col-12');
        }

        ui.registerCards();
    },
    /**
     * Show an alert on screen
     * @param {string} type
     * @param {string} text
     */
    alert: function (type, text) {
        const id = utils.randInt(0, 1e8);
        $('#alerts').append('' +
            '<div id="alert-' + id + '" style="display:none" class="alert alert-' + type + ' alert-dismissible w-100 fade show">' +
            '<button type="button" class="close" data-dismiss="alert">&times;</button>' + text + '' +
            '</div>');
        const alert = $('#alert-' + id);
        alert.stop(true).fadeTo(400, 1).delay(5000).fadeOut(400, function () {
            alert.remove();
        });
    },
    /**
     * Update the current user's info in the screen
     * @param {{userId: String, userName: String, userEmail: String, userImageUrl: String, admin: boolean}} data - result from request
     */
    setCurrentUser: function (data) {
        $('#user')
            .show()
            .attr('title', data.userName);
        $('.user-name').text(data.userName);
        $('.user-mail').text(data.userEmail);
        $('.user-image-nav').attr('src', data.userImageUrl + 'sz=42');
        $('.user-image-full').attr('src', data.userImageUrl + 'sz=70');

        if (data.admin)
            ui.initAdminUi();

        ui.setUserReady();
    },
    /**
     * Register all events
     */
    registerEvents: function () {
        $('#btn-logout').click(function () {
            cookies.clear();
            window.location.href = globals.appPath + '/logout';
        });

        //load main menu view events
        $('#btn-new').click(function () {
            room.precreate(function () {
                utils.setPage(lang.getString('titleRoomCreate'), 'create');

                const setSize = $('#set-size');
                setSize.text(globals.setSizeDefault);
                $('#btn-set-minus').click(function () {
                    const currSize = parseInt(setSize.text());
                    if (currSize > globals.setSizeMin)
                        setSize.text(currSize - globals.setSizeStep);
                });
                $('#btn-set-plus').click(function () {
                    const currSize = parseInt(setSize.text());
                    setSize.text(currSize + globals.setSizeStep);
                });


                //load create view events
                $('#btn-create').click(function () {
                    room.create(
                        $('#question-pack').val(),
                        parseInt(setSize.text()),
                        $('#cbLock').is(':checked'),
                        $('#cbRefresh').is(':checked'),
                        function () {
                            //load room view events
                            $('#btn-next').click(function () {
                                ui.room.killTimer();
                                room.next();
                            });
                            $('#btn-refresh').click(room.ajaxRefresh);
                            $('#btn-auto-refresh').click(room.changeAutoRefresh);
                            $('#btn-lock').click(room.changeLock);
                            $('#btn-delete').click(room.delete);

                            utils.loadMathJax();
                            $.getMultiScripts(globals.roomScripts);
                            if (typeof particles === 'undefined')
                                $.getScript('js/particles.js').done(function () {
                                    particles.init('particleCanvas');
                                });

                            if ($('#cbCamera').is(':checked')) {
                                $.getMultiScripts(globals.cameraScripts)
                                    .done(function () {
                                        camera.init($('#room-camera'));
                                        $('#transparencyControlDiv').show();
                                        $('#transparencyControl').on('input', function () {
                                            const value = $(this).val();
                                            const label = $('#transparencyControlLabel');
                                            ui.room.changeOpacity(value);
                                            label.html('&nbsp;Opacity: ' + (value * 100).toFixed(0) + '%');
                                            label.stop(true).fadeTo(100, 1).delay(1000).fadeOut(400, function () {
                                                label.hide();
                                            });
                                        });
                                    });
                            }
                        }
                    );
                });
                $('#btn-cancel').click(function () {
                    ui.views.showView('menu');
                    utils.setPage(lang.getString('titleMain'));
                });
            });
        });
        $('#btn-join').click(function () {
            const input = $('#roomid');
            const tmproomid = input.val();
            input.val('');
            if (tmproomid && tmproomid.length > 0) {
                padRoom.join(tmproomid.toLowerCase(), true);
            }
        });
        //when validate on room id
        $('#roomid').keypress(function (e) {
            if (e.which === 13) {
                $('#btn-join').click();
            }
        });
        //back buttons events
        $('.btn-back').click(function () {
            ui.views.showView('menu');
            utils.setPage(lang.getString('titleMain'));
        });
    },
    /**
     * Register cards to interact when opened
     */
    registerCards: function () {
        $('.card-link').each(function () {
            $(this).unbind('click');
            $(this).click(function () {
                const icon = $(this).find('i');
                if (icon.hasClass('fa-chevron-circle-right')) {
                    icon
                        .removeClass('fa-chevron-circle-right')
                        .addClass('fa-chevron-circle-down');
                } else if (icon.hasClass('fa-chevron-circle-down')) {
                    icon
                        .removeClass('fa-chevron-circle-down')
                        .addClass('fa-chevron-circle-right');
                }
            });
        });
    },
    /**
     * Load question packs list to "create" view
     * @param {{id:int,name:string,questionCount:int}[]} packs
     */
    loadQuestionPacks: function (packs) {
        $('#question-pack').html('');
        packs.forEach(function (pack) {
            $('#question-pack').append('<option value="' + pack.id + '" title="' + pack.questionCount + '">' + pack.name + ' (' + pack.questionCount + ' questions)</option>');
        });
    },
    /**
     * Click a button from the menu
     * @param {string} name
     */
    goToView: function (name) {
        switch (name) {
            case 'create':
                $('#btn-new').click();
                return;
            case 'questions':
                $('#btn-questions').click();
                return;
            case 'texts':
                $('#btn-texts').click();
                return;
        }
    },
    views: {
        views: ['menu', 'room', 'pad', 'create'],
        /**
         * Show a specific view
         * @param {string} name
         */
        showView: function (name) {
            this.hideAll();
            $('#' + name + '-view').show();
            if (name === 'pad') {
                $('#user').hide();
            } else {
                $('#user').show();
            }
        },
        /**
         * Show loading view
         */
        loading: function () {
            this.hideAll();
            $('#loading').show();
        },
        /**
         * Hide all views
         */
        hideAll: function () {
            $('#alerts').html('');
            this.views.forEach(function (name) {
                $('#' + name + '-view').hide();
            });
            $('#loading').hide();
        }
    },
    room: {
        timer: undefined,
        timeLeft: undefined,
        /**
         * Update the room view
         * @param {string} id
         * @param {boolean} lock
         * @param {string} state
         * @param {{text:string,answers:string[],links:string[]}} [question]
         * @param {int} [round]
         * @param {string} [packName]
         */
        updateView: function (id, lock, state, question, round, packName) {
            $('#room-name').html('Room <u>' + id + '</u>' + (lock ? '&nbsp;<i class="fas fa-lock" title="room locked"></i>' : ''));
            ui.room.setLock(!lock);

            const roomText = $('#room-text');
            const btnNext = $('#btn-next');
            const answers = $('#answers');
            switch (state) {
                case 'REGISTERING':
                    roomText.html('<i class="fas fa-spinner fa-spin"></i>&nbsp;' + lang.getString('stateRegistering'));
                    btnNext.text(lang.getString('btnNextRegistering'));
                    answers.hide();
                    break;
                case 'VIDEO':
                    roomText.html('<i class="fas fa-video"></i>&nbsp;' + lang.getString('stateVideo'));
                    btnNext.text(lang.getString('btnNextVideo'));
                    answers.hide();
                    break;
                case 'ANSWERING':
                    roomText.html('<small><i class="fas fa-question-circle"></i>&nbsp;<small>[' + packName + ']</small>&nbsp;Question ' + (round + 1) + ' :</small><br/>' + question.text);
                    btnNext.text(lang.getString('btnNextAnswers'));
                    answers.show();
                    break;
                case 'RESULTS':
                    roomText.html('<small><i class="fas fa-info-circle"></i>&nbsp;<small>[' + packName + ']</small>&nbsp;Question ' + (round + 1) + ' :</small><br/>' + question.text);
                    btnNext.text(lang.getString('btnNextResults'));
                    answers.show();
                    break;
                case 'VOTE':
                    roomText.html('<i class="fas fa-chalkboard-teacher"></i>&nbsp;' + lang.getString('stateVote'));
                    btnNext.text(lang.getString('btnNextResults'));
                    answers.show();
                    break;
                case 'HELPVIDEO':
                    roomText.html('<i class="fas fa-video"></i>&nbsp;' + lang.getString('stateHelpVideo'));
                    btnNext.text(lang.getString('btnNextResults'));
                    answers.hide();
                    break;
                case 'CLOSED':
                    roomText.html('<i class="fas fa-check-circle"></i>&nbsp;' + lang.getString('stateClosed'));
                    btnNext.text(lang.getString('btnNextFinished'));
                    answers.hide();
                    break;
            }

            Object.keys(mapping.nameToEffect).forEach(function (effect) {
                if (roomText.html().includes('<' + effect + '>')) {
                    particles.show(globals.particlesTime, mapping.nameToEffect[effect]);
                }
            });
        },
        /**
         * Hide answers on screen
         */
        hideAnswers: function () {
            $('#answers').hide();
        },
        /**
         * Edit room text but keep current fontawesome icon at start
         * @param {string} text
         */
        changeRoomText: function (text) {
            const roomText = $('#room-text');
            const classes = $(roomText.find('i')).attr('class');
            roomText.html('<i class="' + classes + '"></i>&nbsp;' + text);
            Object.keys(mapping.nameToEffect).forEach(function (effect) {
                if (text.includes('<' + effect + '>')) {
                    particles.show(globals.particlesTime, mapping.nameToEffect[effect]);
                }
            });
        },
        /**
         * Change availability of lock button
         * @param {boolean} lock
         */
        lockNextButton: function (lock) {
            const btnNext = $('#btn-next');
            if (lock)
                btnNext.addClass('disabled');
            else
                btnNext.removeClass('disabled');
        },
        /**
         * Update an answer
         * @param {string} ans - letter
         * @param {boolean} plain
         * @param {string} text
         * @param {int | undefined} [answered]
         * @param {int | undefined} [total]
         */
        updateAnswer: function (ans, plain, text, answered, total) {
            let html = ans + ' : ' + text;
            if (answered !== undefined) {
                const percent = total <= 0 ? 0 : (100 * (answered / total)).toFixed(0);
                html = percent + '% ' + html;
            }
            $('#answer-' + ans)
                .attr('class', 'btn btn-' + (plain ? '' : 'outline-') + mapping.letterToColor(ans) + ' btn-block btn-lg h-100')
                .html(html);
        },
        /**
         * Update math + align answers
         */
        finishView: function () {
            utils.updateMath();
            const ans = $('.answer');
            ans.each(function () {
                $(this).attr('style', '');
            });
            setTimeout(function () {
                let height = 0;
                ans.each(function () {
                    if ($(this).height() > height) {
                        height = $(this).height();
                    }
                });
                if (height) {
                    ans.each(function () {
                        $(this).height(height);
                    });
                }
            });
        },
        /**
         * Update autorefresh button
         * @param {boolean} autoRefresh
         */
        setAutoRefresh: function (autoRefresh) {
            if (autoRefresh) {
                $('#btn-auto-refresh').html('<i class="fas fa-check-square"></i>&nbsp;' + lang.getString('btnAutoRefresh'));
                $('#btn-refresh').addClass('disabled');
            } else {
                $('#btn-auto-refresh').html('<i class="fas fa-square"></i>&nbsp;' + lang.getString('btnAutoRefresh'));
                $('#btn-refresh').removeClass('disabled');
            }
        },
        /**
         * Update lock button and icon next to room name
         * @param {boolean} lock
         */
        setLock: function (lock) {
            $('#room-name').html('Room <u>' + room.id + '</u>' + (!lock ? '&nbsp;<i class="fas fa-lock" title="room locked"></i>' : ''));
            $('#btn-lock').html('<i class="fas fa-lock' + (lock ? '' : '-open') + '"></i>&nbsp;' + (lock ? lang.getString('btnLock') : lang.getString('btnUnlock')));
        },
        /**
         * Clear all members from view
         */
        clearMembers: function () {
            $('#members').html('');
        },
        /**
         * Add a new member
         * @param {{id:string,name:string,imageUrl:string,answer:int,generated:boolean}} member
         */
        addMember: function (member) {

            const imageUrl = member.imageUrl.split('sz=')[0];

            const html = '' +
                '<div id="' + member.id + '" class="col-4 col-md-3 col-lg-2 member-card-holder" title="kick ' + member.name + '">' +
                '<div class="jumbotron h-100 bg-dark member-card text-center text-white">' +
                '<div class="text-danger btn-kick"><i class="fas fa-times"></i></div>' +
                (member.generated ?
                    '<div class="user-image-nav rounded-circle"></div>' :
                    '<img class="user-image-nav rounded-circle" src="' + imageUrl + 'sz=42"/>') +
                '<h4><small>' + member.name + '</small></h4>' +
                '</div></div>';
            $('#members').append(html);
            $('#' + member.id).click(function () {
                room.kick(member.id, member.name);
            });
        },
        /**
         * Change a member background
         * @param {string} memberId
         * @param {string} bg - color
         * @param {boolean} [flash]
         */
        setMemberBg: function (memberId, bg, flash) {
            const card = $('#' + memberId + ' .member-card');
            if (!card.is(':animated')) {
                card.removeClass(function (index, className) {
                    return (className.match(/(^|\s)bg-\S+/g) || []).join(' ');
                });
                card.css('background-color', '');
                if (flash) {
                    const body = $('body');
                    card.css('background-color', body.css('--light'));
                    card.animate({
                        backgroundColor: body.css('--' + bg)
                    }, 300);
                } else {
                    card.addClass('bg-' + bg);
                }
            }
        },
        /**
         * Update a camera member by setting its QR code preview
         * @param {{id:string,answer:int}} member
         */
        updateCameraMember: function (member) {
            let html;
            if (member.answer === 0) {
                html = '';
            } else {
                const matrix = cards[member.id - 1].matrix;
                html = '' +
                    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 5 5">' +
                    '<g transform="scale(-1,1) translate(-5 ,0) rotate(' + (member.answer * 90) + ', 2.5, 2.5)">';
                for (let x = 0; x < 5; x++)
                    for (let y = 0; y < 5; y++)
                        html += '<rect x="' + x + '" y="' + y + '" width="1" height="1" style="fill:' + (matrix[x][y] ? 'black' : 'white') + '" />';
                html += '</g></svg>';
            }

            $('#' + member.id).find('.user-image-nav').html(html);
        },
        /**
         * Collapse the "extra" card
         */
        closeExtra: function () {
            const link = $('#extraLink');
            if (link.find('i').hasClass('fa-chevron-circle-down')) {
                link.click();
            }
        },
        /**
         * Update room progress
         * @param {number} val - in %
         * @param {int} left - number of questions left
         */
        setRoomProgress: function (val, left) {
            $('#room-progress-container').show();
            $('#room-progress').css('width', val + '%');
            $('#room-progress-text').text(lang.getString('labelProgress').format(val.toFixed(0), left));
        },
        /**
         * Hide the room progress bar
         */
        hideRoomProgress: function () {
            $('#room-progress-container').hide();
        },
        /**
         * Set the timer and animation
         * @param {int} time - in ms
         * @param {function} callback - to be called at the end
         */
        setTimer: function (time, callback) {
            if (this.timer)
                clearInterval(this.timer);
            const container = $('#timer-progress-container');
            container.stop(true);
            container.show();
            $('#timer-progress').css('width', '100%');
            this.timeLeft = time;
            this.timer = setInterval(function () {
                ui.room.timeLeft -= 100;
                if (ui.room.timeLeft < 0) {
                    ui.room.killTimer();
                    callback();
                } else {
                    $('#timer-progress').css('width', (100 * ui.room.timeLeft / time) + '%');
                }
            }, 100);
            container.find('i').click(ui.room.killTimer);
        },
        /**
         * Kill and hide the timer
         */
        killTimer: function () {
            const container = $('#timer-progress-container');
            clearInterval(ui.room.timer);
            ui.room.timer = undefined;
            $('#timer-progress').css('width', '0%');
            container.fadeOut(400, function () {
                container.hide();
                $('#timer-progress').css('width', '100%');
            });
        },
        /**
         * Set up links/videos on screen
         * @param {string} mainlink
         * @param {boolean} [autoplay]
         */
        showVideo: function (mainlink, autoplay) {
            const sublinks = mainlink ? mainlink.split(';') : [''];
            let html = '';
            for (let i = 0; i < sublinks.length; i++) {
                let link = sublinks[i].trim();
                let subhtml = link;
                const youtube = utils.getYoutubeEmbeddedLink(link, autoplay);
                if (link.length === 0) {
                    subhtml = '<h4>' + lang.getString('textNoVideo') + '</h4>';
                } else if (youtube) {
                    subhtml = '<iframe width="560" height="315" src="' + youtube + '" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>';
                } else {
                    let name = link.length > 40 ? (link.substr(0, 37) + '...') : link;
                    const pos = link.indexOf(']');
                    if (link.startsWith('[') && pos > -1) {
                        name = link.substr(1, pos - 1);
                        link = link.substr(pos + 1);
                    }
                    if (utils.matchUrl(link)) {
                        subhtml = '<h4><a href="' + link + '" target="_blank">' + name + '</a></h4>';
                    }
                }
                html += '<div id="video' + i + '" style="display:none">' + subhtml + '</div>';
            }
            $('#videos').html(html);
            $('#video').show();
            $('#video0').show();

            const btnLast = $('#btn-last-video');
            const btnNext = $('#btn-next-video');

            if (sublinks.length > 1) {
                btnLast.hide();
                btnNext.show();

                btnLast.click(function () {
                    let i;
                    let vid;
                    for (i = 0; i < sublinks.length; i++) {
                        vid = $('#video' + i);
                        if (vid.is(':visible'))
                            break;
                    }
                    if (i - 1 >= 0) {
                        vid.hide();
                        btnNext.show();
                        if (i - 2 === -1)
                            btnLast.hide();
                        $('#video' + (i - 1)).show();
                    }
                });
                btnNext.click(function () {
                    let i;
                    let vid;
                    for (i = 0; i < sublinks.length; i++) {
                        vid = $('#video' + i);
                        if (vid.is(':visible'))
                            break;
                    }
                    if (i + 1 < sublinks.length) {
                        vid.hide();
                        btnLast.show();
                        if (i + 2 === sublinks.length)
                            btnNext.hide();
                        $('#video' + (i + 1)).show();
                    }
                });
            } else {
                btnLast.hide();
                btnNext.hide();
            }

        },
        /**
         * Delete all links and videos on screen
         */
        hideVideo: function () {
            $('#videos').html('');
            $('#video').hide();
        },
        /**
         * Show statistics chart
         * @param {{total:int,right:int,wrong:int,unanswered:int,score:int}[]} stats
         */
        showStats: function (stats) {
            let scoreData = [[0, 0]];
            let errorData = [[0, 0]];

            stats.forEach(function (e, i) {
                scoreData.push([i + 1, e.score / room.setSize]);
                errorData.push([i + 1, (e.wrong + e.unanswered) / (e.total + e.unanswered)]);
            });

            $('#stats-div').show();
            $.plot($('#chart'), [scoreData, errorData], globals.flotOptions);
        },
        /**
         * Hide statistics chart
         */
        hideStats() {
            $('#stats-div').hide();
        },
        /**
         * Show a message above room buttons
         * @param {string} message
         */
        showMessage(message) {
            Object.keys(mapping.nameToEffect).forEach(function (effect) {
                if (message.includes('<' + effect + '>')) {
                    particles.show(globals.particlesTime, mapping.nameToEffect[effect]);
                    message = message.replaceAll('<' + effect + '>', '');
                }
            });
            $('#room-message').html(message).show();
        },
        /**
         * Hide room message
         */
        hideMessage() {
            $('#room-message').hide();
        },
        /**
         * Change opacity of room UI
         * @param {float} o
         */
        changeOpacity: function (o) {
            const rgb = (36 * o + 200).toFixed(0);
            const val = 'rgba(' + rgb + ', ' + rgb + ', ' + rgb + ', ' + o + ')';
            $('.jumbotron').css('backgroundColor', val);
        }
    },
    pad: {
        /**
         * Init pad view
         * @param {string} name - room name
         */
        init: function (name) {
            //pad view
            ['A', 'B', 'C', 'D'].forEach(function (letter) {
                const clickStart = function () {
                    $(this).find('div')
                        .removeClass('bg-' + mapping.letterToColor(letter))
                        .addClass('bg-dark')
                        .addClass('text-white');
                };
                const clickStop = function () {
                    $(this).find('div')
                        .addClass('bg-' + mapping.letterToColor(letter))
                        .removeClass('bg-dark')
                        .removeClass('text-white');
                };

                $('#btn-' + letter).click(function () {
                    padRoom.answerQuestion(mapping.letterToAnswer[letter]);
                }).mousedown(clickStart)
                    .mouseup(clickStop)
                    .on('touchstart', clickStart)
                    .on('touchend', clickStop);
            });

            const laclickStart = function () {
                const la = padRoom.lastAnswer;
                if (la)
                    $('#btn-' + mapping.answerToLetter[la]).find('div')
                        .removeClass('bg-' + mapping.answerToColor[la])
                        .addClass('bg-dark')
                        .addClass('text-white');
            };

            const laClickStop = function () {
                const la = padRoom.lastAnswer;
                if (la)
                    $('#btn-' + mapping.answerToLetter[la]).find('div')
                        .addClass('bg-' + mapping.answerToColor[la])
                        .removeClass('bg-dark')
                        .removeClass('text-white');
            };

            $('#btn-last-answer').mousedown(laclickStart)
                .mouseup(laClickStop)
                .on('touchstart', laclickStart)
                .on('touchend', laClickStop);

            $('#btn-quit').click(padRoom.quit);

            $('#pad-room-name').html('<u>' + name + '</u>');
        }
    }
};
