var ui = {
    views: ['menu', 'room', 'pad', 'create'],
    initUI: function () {
        ui.registerEvents();

        if (jQuery.browser.mobile) {
            //hide room creation
            $('#div-create-room').hide();
            $('#div-join-room').attr("class", "col-12");
        } else {
            //populate room creation window
            ui.createCheckBox('#form-create-checkboxes', 'cbAnswers', false, 'Show user\'s answers');
            ui.createCheckBox('#form-create-checkboxes', 'cbStats', true, 'Show statistics');
            ui.createCheckBox('#form-create-checkboxes', 'cbLock', false, 'Lock room at start');
            ui.createCheckBox('#form-create-checkboxes', 'cbRefresh', true, 'Automatically refresh content');
        }
    },
    addAlert: function (type, text) {
        $('#alerts').append('' +
            '<div class="alert alert-' + type + ' alert-dismissible fade show">' +
            '<button type="button" class="close" data-dismiss="alert">&times;</button>' + text + '' +
            '</div>');
        $('#alerts:last-child').hide().fadeIn();
    },
    clearMembers: function () {
        $("#members").html("");
    },
    addMember: function (member) {
        member.imageUrl = member.imageUrl.split('sz=')[0];
        var html = '' +
            '<div id="' + member.id + '" class="col-4" title="kick ' + member.name + '">' +
            '<div class="jumbotron bg-dark member-card text-center text-white">' +
            '<img class="rounded-circle" src="' + member.imageUrl + 'sz=42"/>' +
            '<h4><small>' + member.name + '</small></h4>' +
            '</div></div>';
        $("#members").append(html);
        $("#" + member.id).click(function () {
            room.kick(member.id, member.name);
        });
    },
    setMemberBg: function (memberId, bg) {
        var card = $('#' + memberId + ' .member-card');
        card.removeClass(function (index, className) {
            return (className.match(/(^|\s)bg-\S+/g) || []).join(' ');
        });
        card.addClass("bg-" + bg);
    },
    setCurrentUser: function (data) {
        $('#user')
            .show()
            .attr("title", data.userName);
        $('.user-name').text(data.userName);
        $('.user-mail').text(data.userEmail);
        $('.user-image-nav').attr("src", data.userImageUrl + "sz=42");
        $('.user-image-full').attr("src", data.userImageUrl + "sz=70");
    },
    showView: function (name) {
        this.hideAll();
        $('#' + name + '-view').show();
    },
    loading: function () {
        this.hideAll();
        $('#loading').show();
    },
    hideAll: function () {
        $('#alerts').html('');
        this.views.forEach(function (name) {
            $('#' + name + '-view').hide();
        });
        $('#loading').hide();
    },
    registerEvents: function () {
        $("#btn-logout").click(function(){
            window.location.href = "/logout";
        });

        //main menu
        $("#btn-new").click(function () {
            ui.showView('create');
        });
        $("#btn-join").click(function () {
            var input = $('#roomid');
            var tmproomid = input.val();
            input.val('');
            if (tmproomid && tmproomid.length > 0) {
                room.join(tmproomid, true);
            }
        });

        $('#roomid').keypress(function (e) {
            if (e.which === 13) {
                $("#btn-join").click();
            }
        });

        //create view
        $("#btn-create").click(function () {
            room.create(
                $("#question-pack").prop('selectedIndex'),
                ui.checkBoxValue('cbAnswers'),
                ui.checkBoxValue('cbStats'),
                ui.checkBoxValue('cbLock'),
                ui.checkBoxValue('cbRefresh')
            );
        });
        $("#btn-cancel").click(function () {
            ui.showView('menu');
        });

        //room view
        $("#btn-next").click(room.next);
        $("#btn-refresh").click(room.ajaxRefresh);
        $("#btn-auto-refresh").click(room.changeAutoRefresh);
        $("#btn-delete").click(room.delete);

        $("#hintLink").click(function(){
            var icon = $(this).find('svg');
            if (icon.hasClass("fa-chevron-circle-right")) {
                icon
                    .removeClass("fa-chevron-circle-right")
                    .addClass("fa-chevron-circle-down");
            } else {
                icon
                    .removeClass("fa-chevron-circle-down")
                    .addClass("fa-chevron-circle-right");
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
    },
    updateRoomView: function (id, text, btnNext, showAnswers, hint) {
        $("#room-name").text("Room " + id);
        $("#room-text").html(text);

        if(hint && hint.length > 0){
            $('#hintDiv').show();
            $('#hint').html(hint);
        }else{
            $('#hintDiv').hide();
        }

        if (btnNext)
            $('#btn-next').text(btnNext);
        else
            $('#btn-next').hide();

        if (showAnswers)
            $("#answers").show();
        else
            $("#answers").hide();


    },
    updateAnswer: function (ans, plain, text, answered, total) {
        var html = ans + " : " + text;
        if (answered !== undefined) {
            var percent = total <= 0 ? 0 : (100 * (answered / total)).toFixed(0);
            html = percent + '% ' + html;
        }
        $('#answer-' + ans)
            .attr("class", "btn btn-" + (plain ? '' : 'outline-') + mapping.letterToColor(ans) + " btn-block btn-lg")
            .html(html);
    },
    setAutoRefresh: function (autoRefresh) {
        if (autoRefresh) {
            $('#btn-auto-refresh').html("<i class=\"far fa-check-square\"></i> Auto-Refresh");
            $('#btn-refresh').hide();
        } else {
            $('#btn-auto-refresh').html("<i class=\"far fa-square\"></i> Auto-Refresh");
            $('#btn-refresh').show();
        }
    },
    getRoomText: function (state, roundCount, round, question) {
        switch (state) {
            case "REGISTERING":
                return '<i class="fas fa-spinner fa-spin"></i>&nbsp;Waiting for members...';
            case "ANSWERING":
                return '<small><i class="fas fa-question-circle"></i>&nbsp;Question ' + (round + 1) + '/' + roundCount + ' :</small><br/>' + question;
            case "RESULTS":
                return '<small><i class="fas fa-info-circle"></i>&nbsp;Question ' + (round + 1) + '/' + roundCount + ' :</small><br/>' + question;
            case "CLOSED":
                return '<i class="fas fa-check-circle"></i>&nbsp;Finished !';
        }
    },
    createCheckBox: function (parent, id, checked, text) {
        $(parent).append('' +
            '<h4 id="' + id + '" class="custom-checkbox">' +
            '<i class="far fa-' + (checked ? 'check-' : '') + 'square"></i>&nbsp;' + text + '' +
            '</h4>');
        $('#' + id).click(function () {
            var icon = $(this).find('svg');
            if (icon.hasClass("fa-check-square")) {
                icon
                    .removeClass("fa-check-square")
                    .addClass("fa-square");
            } else {
                icon
                    .removeClass("fa-square")
                    .addClass("fa-check-square");
            }
        });
    },
    checkBoxValue: function (id) {
        return $('#' + id).find('svg').hasClass("fa-check-square");
    },
    closeHint: function(){
        var link = $("#hintLink");
        if(link.find('svg').hasClass("fa-chevron-circle-down")){
            link.click();
        }
    }
};
