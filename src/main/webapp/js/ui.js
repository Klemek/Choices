var ui = {
    views: ['menu', 'room', 'pad', 'create', 'questions'],
    initUI: function () {
        ui.registerEvents();

        if (jQuery.browser.mobile) {
            //hide room creation
            $('#div-create-room').hide();
            $('#div-join-room').attr("class", "col-12");
        } else {
            //todo check admin rights
            $('#btn-questions').show();

            //populate room creation window
            ui.createCheckBox('#form-create-checkboxes', 'cbAnswers', false, 'Show user\'s answers');
            ui.createCheckBox('#form-create-checkboxes', 'cbStats', true, 'Show statistics');
            ui.createCheckBox('#form-create-checkboxes', 'cbLock', false, 'Lock room at start');
            ui.createCheckBox('#form-create-checkboxes', 'cbRefresh', true, 'Automatically refresh content');
        }

        ui.registerCards();
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
        $("#btn-logout").click(function () {
            window.location.href = "/logout";
        });

        //main menu
        $("#btn-new").click(function () {
            ui.showView('create');
        });
        $("#btn-questions").click(function () {
            ui.showView('questions');
            ui.addAlert('danger', 'Template section, nothing is real');
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
        $('#btn-lock').click(room.changeLock);
        $("#btn-delete").click(room.delete);

        //todo questions view
        $('#btn-create-pack').click(function () {
            ui.addQuestionPack('p' + Math.randInt(0, 100000), 'New question pack');
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
    updateRoomView: function (id, lock, text, btnNext, showAnswers, hint) {
        $("#room-name").html("Room " + id + (lock ? '&nbsp;<i class="fas fa-lock" title="room locked"></i>' : ''));
        $("#room-text").html(text);

        if (hint && hint.length > 0) {
            $('#hintDiv').show();
            $('#hint').html(hint);
        } else {
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
            $('#btn-auto-refresh').html("<i class=\"far fa-check-square\"></i>&nbsp;Auto-Refresh");
            $('#btn-refresh').addClass('disabled');
        } else {
            $('#btn-auto-refresh').html("<i class=\"far fa-square\"></i>&nbsp;Auto-Refresh");
            $('#btn-refresh').removeClass('disabled');
        }
    },
    setLock: function (lock) {
        $('#btn-lock').html('<i class="fas fa-lock' + (lock ? '' : '-open') + '"></i>&nbsp;' + (lock ? 'Lock' : 'Unlock'));
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
    closeHint: function () {
        var link = $("#hintLink");
        if (link.find('svg').hasClass("fa-chevron-circle-down")) {
            link.click();
        }
    },
    registerCards: function () {
        $(".card-link").each(function () {
            //todo check useful $(this).unbind('onclick');
            $(this).click(function () {
                var icon = $(this).find('svg');
                if (icon.hasClass("fa-chevron-circle-right")) {
                    icon
                        .removeClass("fa-chevron-circle-right")
                        .addClass("fa-chevron-circle-down");
                } else if (icon.hasClass("fa-chevron-circle-down")) {
                    icon
                        .removeClass("fa-chevron-circle-down")
                        .addClass("fa-chevron-circle-right");
                }
            });
        });
    },
    addQuestionPack: function (id, name, questions) {
        var html = '' +
            '<div id="' + id + 'c" class="card text-left"></div>';
        $('#packs').append(html);
        ui.updateQuestionPack(id, name, questions);
    },
    updateQuestionPack: function (id, name, questions) {
        var html = '' +
            '<div class="card-header">' +
            '<a class="card-link" data-toggle="collapse" href="#' + id + '"><i class="fas fa-chevron-circle-right"></i>&nbsp;' + name + '</a>' +
            '<span title="delete" class="btn-delete text-danger"><i class="fas fa-times"></i></span>' +
            '</div>' +
            '<div id="' + id + '" class="collapse" data-parent="#packs">' +
            '<div class="card-body">' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-edit" title="Pack name"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + id + 'n" placeholder="Pack name" value="' + name + '"></div>' +
            '</div>' +
            '<div class="pack-questions"></div><br/>' +
            '<div class="row">' +
            '<div class="col-6"><button class="btn btn-primary btn-block"><i class="fas fa-sync"></i>&nbsp;Update</button></div>' +
            '<div class="col-6"><button class="btn btn-add btn-success btn-block"><i class="fas fa-plus"></i>&nbsp;New question</button>' +
            '</div></div></div></div>';
        $('#' + id + 'c').html(html);

        if (questions)
            questions.forEach(function (q, i) {
                ui.addQuestion(id, i + 1, q.question, q.hint, q.answers);
            });

        $($('#' + id + 'c').find('.btn-delete')[0]).click(function () {
            ui.removeQuestionPack(id);
        });

        $('#' + id + 'c').find('.btn-add').click(function () {
            var i = $('#' + id + 'c').find('.fa-question-circle').length + 1;
            ui.addQuestion(id, i);
        });

        ui.registerCards();
    },
    removeQuestionPack: function (id) {
        $('#' + id + 'c').remove();
    },
    addQuestion: function (packId, n, question, hint, answers) {
        question = question ? question : '';
        hint = hint ? hint : '';
        answers = answers ? answers : ['', '', '', ''];

        var html = '' +
            '<div id="' + packId + 'q' + n + 'c" class="card text-left">' +
            '<div class="card-header">' +
            '<a class="card-link" data-toggle="collapse" href="#' + packId + 'q' + n + '"><i class="fas fa-chevron-circle-right"></i>&nbsp;Question ' + n + '</a>' +
            '<span title="delete" class="btn-delete text-danger"><i class="fas fa-times"></i></span>' +
            '</div>' +
            '<div id="' + packId + 'q' + n + '" class="collapse" data-parent="#' + packId + '">' +
            '<div class="card-body">' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-question-circle" title="question"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 't" placeholder="Question text" value="' + question + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-info fa-question" title="hint"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'h" placeholder="Question hint" value="' + hint + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-success fa-check" title="correct answer"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a1" placeholder="Answer 1 (correct)" value="' + answers[0] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-danger fa-times" title="wrong answer"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a2" placeholder="Answer 2" value="' + answers[1] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-danger fa-times" title="wrong answer"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a3" placeholder="Answer 3" value="' + answers[2] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-danger fa-times" title="wrong answer"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a4"  placeholder="Answer 4" value="' + answers[3] + '"></div>' +
            '</div>' +
            '<div class="card text-left">' +
            '<div class="card-header">' +
            '<a class="card-link" data-toggle="collapse" href="#' + packId + 'q' + n + 'p"><i class="fas fa-eye"></i>&nbsp;Preview</a>' +
            '</div>' +
            '<div id="' + packId + 'q' + n + 'p" class="collapse" data-parent="#' + packId + 'q' + n + '">' +
            '<div class="card-body">' +
            '<h3 class="text-center"></h3>' +
            '<div class="row">' +
            '<div class="col-6 answer"><div class="btn btn-danger btn-block btn-lg"></div></div>' +
            '<div class="col-6 answer"><div class="btn btn-success btn-block btn-lg"></div></div>' +
            '<div class="col-6 answer"><div class="btn btn-info btn-block btn-lg"></div></div>' +
            '<div class="col-6 answer"><div class="btn btn-warning btn-block btn-lg"></div></div>' +
            '</div>' +
            '<div class="card text-left">' +
            '<div class="card-header"><a class="card-link" data-toggle="collapse"><i class="fas fa-chevron-circle-down"></i>&nbsp;Hint</a></div>' +
            '<div class="collapse show"><div class="card-body"></div></div>' +
            '</div>' +
            '</div></div>' +
            '</div></div></div>';
        $('#' + packId).find('.pack-questions').append(html);

        $($('#' + packId + 'q' + n + 'c').find('.btn-delete')[0]).click(function () {
            ui.removeQuestion(packId, n);
        });

        var preview =  $('#' + packId + 'q' + n+'p');
        var previewLink = $($('#' + packId + 'q' + n).find('.card-link')[0]);

        var changeEvent = function(){
            if(preview.hasClass("show")){
                previewLink.click();
            }
        };

        $('#' + packId + 'q' + n+'t').on('input',changeEvent);
        $('#' + packId + 'q' + n+'h').on('input',changeEvent);
        $('#' + packId + 'q' + n+'a1').on('input',changeEvent);
        $('#' + packId + 'q' + n+'a2').on('input',changeEvent);
        $('#' + packId + 'q' + n+'a3').on('input',changeEvent);
        $('#' + packId + 'q' + n+'a4').on('input',changeEvent);

        previewLink.click(function () {
            if(!preview.hasClass("show")){
                var question = $('#' + packId + 'q' + n+'t').val();
                var hint = $('#' + packId + 'q' + n+'h').val();
                var answers = [
                    $('#' + packId + 'q' + n+'a1').val(),
                    $('#' + packId + 'q' + n+'a2').val(),
                    $('#' + packId + 'q' + n+'a3').val(),
                    $('#' + packId + 'q' + n+'a4').val()
                ];

                preview.find('h3').html(question);
                preview.find('.btn-danger').html('A : '+answers[0]);
                preview.find('.btn-success').html('B : '+answers[1]);
                preview.find('.btn-info').html('C : '+answers[2]);
                preview.find('.btn-warning').html('D : '+answers[3]);
                $(preview.find('.card-body')[1]).html(hint);
            }
        });

        ui.registerCards();
    },
    removeQuestion: function (packId, n) {
        $('#' + packId + 'q' + n + 'c').remove();
    }
};
