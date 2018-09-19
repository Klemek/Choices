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
        const roomid = $('#roomid');
        roomid.attr('placeholder', lang.getString('roomIdPlaceholder'));
        roomid.click(function () {
            $(this).focus();
        });
        roomid.click();

        $('#div-create-room').hide();
        $('#div-join-room').attr('class', 'col-12');
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
        $('#btn-join').click(function () {
            const input = $('#roomid');
            const tmproomid = input.val();
            input.val('');
            if (tmproomid && tmproomid.length > 0) {
                padRoom.join(tmproomid.toLowerCase(), true);
            }
        });
        $('#roomid').keypress(function (e) {
            if (e.which === 13) {
                $('#btn-join').click();
            }
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

//# sourceURL=js/mobile-ui.js