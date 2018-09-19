/* exported padRoom */
const padRoom = {
    id: undefined,
    lastAnswer: undefined,
    /**
     * Set the current room data
     * @param {string} tmproomid - the id to set
     * @param {boolean} pushstate - if the page title / url need to be updated
     */
    set: function (tmproomid, pushstate) {
        this.id = tmproomid;

        if (pushstate)
            utils.setPage('Room ' + this.id, 'roomid=' + this.id);

        //sync ajax request to quit/delete room on unload
        $(window).on('unload', function () {
            $.ajax({
                type: 'DELETE',
                url: '/api/room/' + padRoom.id + '/quit',
                async: false,
                data: {}
            });
        });
    },
    /**
     * Join a room as a simple user
     * @param {string} tmproomid - the room id
     * @param {boolean} [pushstate] - if the title/url need to be updated
     */
    join: function (tmproomid, pushstate) {
        ui.views.loading();
        ajax.call('POST', '/room/' + tmproomid + '/join', function () {
            padRoom.set(tmproomid, pushstate);
            ui.pad.init(tmproomid);
            ui.views.showView('pad');
        }, function (data) {
            if (window.location.search && window.location.search.length > 0) {
                utils.setPage(lang.getString('titleMain'));
            }
            ui.views.showView('menu');
            switch (data.status) {
                case 403:
                    ui.alert('danger', lang.getString('errorRoomLocked').format(tmproomid));
                    break;
                case 404:
                    ui.alert('danger', lang.getString('errorRoomNotFound').format(tmproomid));
                    break;
                default:
                    ui.alert('danger', lang.getString('errorRoomJoin').format(tmproomid, data.status));
                    break;
            }
        });
    },
    /**
     * Answer the question
     * @param {int} val - the answer to give
     */
    answerQuestion: function (val) {
        ajax.call('POST', '/room/' + padRoom.id + '/answer/' + val,
            function () {
                padRoom.lastAnswer = val;
            },
            function () {
                if (window.location.search && window.location.search.length > 0) {
                    utils.setPage(lang.getString('titleMain'));
                }
                ui.views.showView('menu');
                ui.alert('danger', lang.getString('errorRoomQuit').format(padRoom.id));
            });
    },
    /**
     * Quit the current room
     */
    quit: function () {
        if (window.confirm(lang.getString('askRoomQuit')))
            window.location.href = globals.appPath + '/';
    }

};

//# sourceURL=js/padRoom.js