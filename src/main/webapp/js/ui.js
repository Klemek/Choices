var ui = {
    initUI: function () {
        ui.registerEvents();

        if (jQuery.browser.mobile) {
            //hide room creation
            $('#div-create-room').hide();
            $('#div-join-room').attr("class", "col-12");
        } else {

            //populate room creation window
            ui.checkbox.create('#form-create-checkboxes', 'cbAnswers', false, 'Show user\'s answers');
            ui.checkbox.create('#form-create-checkboxes', 'cbStats', true, 'Always show statistics');
            ui.checkbox.create('#form-create-checkboxes', 'cbLock', false, 'Lock room at start');
            ui.checkbox.create('#form-create-checkboxes', 'cbRefresh', true, 'Automatically refresh content');
        }

        ui.registerCards();
    },
    addAlert: function (type, text) {
        $('#alerts').append('' +
            '<div class="alert alert-' + type + ' alert-dismissible w-100 fade show">' +
            '<button type="button" class="close" data-dismiss="alert">&times;</button>' + text + '' +
            '</div>');
        var alert = $('#alerts:last-child');
        alert.hide().fadeIn();
        setTimeout(function(){
            alert.fadeOut(400, function(){
                alert.remove();
            });
        },5000);
    },
    setCurrentUser: function (data) {
        $('#user')
            .show()
            .attr("title", data.userName);
        $('.user-name').text(data.userName);
        $('.user-mail').text(data.userEmail);
        $('.user-image-nav').attr("src", data.userImageUrl + "sz=42");
        $('.user-image-full').attr("src", data.userImageUrl + "sz=70");

        if (data.admin) {
            $('#btn-questions').show();
        }
    },
    registerEvents: function () {
        $("#btn-logout").click(function () {
            window.location.href = "/logout";
        });

        //main menu
        $("#btn-new").click(room.precreate);
        $("#btn-questions").click(questions.load);
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
                $("#question-pack").val(),
                ui.checkbox.value('cbAnswers'),
                ui.checkbox.value('cbStats'),
                ui.checkbox.value('cbLock'),
                ui.checkbox.value('cbRefresh')
            );
        });
        $("#btn-cancel").click(function () {
            ui.views.showView('menu');
        });

        //room view
        $("#btn-next").click(room.next);
        $("#btn-refresh").click(room.ajaxRefresh);
        $("#btn-auto-refresh").click(room.changeAutoRefresh);
        $('#btn-lock').click(room.changeLock);
        $('#btn-stats').click(room.changeStats);
        $("#btn-delete").click(room.delete);

        //questions view
        $('#btn-create-pack').click(questions.new);

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
    registerCards: function () {
        $(".card-link").each(function () {
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
    loadQuestionPacks: function(packs){
        packs.forEach(function(pack){
           $('#question-pack').append('<option value="'+pack.id+'">'+pack.name+'</option>');
        });
    },
    views: {
        views: ['menu', 'room', 'pad', 'create', 'questions'],
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
        }
    },
    checkbox: {
        create: function (parent, id, checked, text) {
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
        value: function (id) {
            return $('#' + id).find('svg').hasClass("fa-check-square");
        }
    },
    room: {
        updateView: function (id, lock, text, btnNext, showAnswers, hint) {
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
                $('#btn-next').addClass('disabled').text('Finished');

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
                .attr("class", "btn btn-" + (plain ? '' : 'outline-') + mapping.letterToColor(ans) + " btn-block btn-lg h-100")
                .html(html);
        },
        finishView: function(){
            window.updateMath();
            setTimeout(function(){
                var height = 0;
                $('.answer').each(function(){
                    if($(this).height()>height){
                        height = $(this).height();
                    }
                });
                if(height){
                    $('.answer').each(function(){
                        $(this).height(height);
                    });
                }
            });
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
        setStats: function(stats){
            $('#btn-stats').html('<i class="fas fa-eye'+(stats?'':'-slash')+'"></i>&nbsp;Statistics');
        },
        disableStats: function(disabled){
            if(disabled)
                $('#btn-stats').addClass('disabled');
            else
                $('#btn-stats').removeClass('disabled');
        },
        clearMembers: function () {
            $("#members").html("");
        },
        addMember: function (member) {
            member.imageUrl = member.imageUrl.split('sz=')[0];
            var html = '' +
                '<div id="' + member.id + '" class="col-4" title="kick ' + member.name + '">' +
                '<div class="jumbotron h-100 bg-dark member-card text-center text-white">' +
                '<div class="text-danger btn-kick"><i class="fas fa-times"></i></div>' +
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
        closeHint: function () {
            var link = $("#hintLink");
            if (link.find('svg').hasClass("fa-chevron-circle-down")) {
                link.click();
            }
        },
        showFinalResults: function(stats, members){
            $('#hintLink').html('<i class="fas fa-chevron-circle-right"></i>&nbsp;Results');

            $('#hint').append('' +
                '<h3>Question results :</h3>' +
                '<ul id="questionStats"></ul>');

            stats.forEach(function(e, i){
                var html = '<li><b>Question '+(i+1)+' : </b>';
                var pa = e[2] <= 0 ? 0 : (100 * (e[0] / e[2])).toFixed(0);
                html += 'Correct : '+pa+' % ';
                if(e[1] !== e[2] && e[2] > 0){
                    var pu = (100 * (1 - (e[1] / e[2]))).toFixed(0);
                    html += '/ Unanswered : '+pu+' %';
                }
                html += '</li>';

                $('#questionStats').append(html);
            });

            if(Object.keys(members).length > 0){
                $('#hint').append('' +
                    '<h3>Individual results :</h3>' +
                    '<ul id="memberStats"></ul>');
                var m = Object.keys(members).map(function(v) { return members[v]; });
                m.sort(function(a,b){
                   return b.correct - a.correct;
                });
                m.forEach(function(e){
                    var pa = (100 * (e.correct / stats.length)).toFixed(0);
                    $('#memberStats').append('<li><b>'+e.name+' : </b> Correct : '+e.correct+' ('+pa+' %)</li>');
                });
            }

            $('#hintDiv').show();

        }
    },
    questions: {
        addPack: function (id, name, qs) {
            var html = '' +
                '<div id="' + id + 'c" class="card text-left"></div>';
            $('#packs').append(html);
            ui.questions.updatePack(id, name, qs);
        },
        updatePack: function (id, name, qs, open) {
            var html = '' +
                '<div class="card-header">' +
                '<a class="card-link" data-toggle="collapse" href="#' + id + '"><i class="fas fa-chevron-circle-right"></i>&nbsp;' + (name ? name : 'New question pack') + '</a>' +
                '<span title="delete" class="btn-delete text-danger"><i class="fas fa-times"></i></span>' +
                '</div>' +
                '<div id="' + id + '" class="collapse" data-parent="#packs">' +
                '<div class="card-body">' +
                '<div class="form-group row">' +
                '<label class="col-1 col-form-label"><i class="fas fa-edit" title="Pack name"></i></label>' +
                '<div class="col-11"><input type="text" class="form-control" id="' + id + 'n" placeholder="Pack name" value="' + (name ? name : '') + '"></div>' +
                '</div>' +
                '<div class="pack-questions"></div><br/>' +
                '<div class="row">' +
                '<div class="col-6"><button class="btn btn-primary btn-block"><i class="fas fa-sync"></i>&nbsp;Save</button></div>' +
                '<div class="col-6"><button class="btn btn-add btn-success btn-block"><i class="fas fa-plus"></i>&nbsp;New question</button>' +
                '</div></div></div></div>';

            var card = $('#' + id + 'c');

            card.html(html);

            if (qs)
                qs.forEach(function (q, i) {
                    ui.questions.addQuestion(id, i + 1, q.text, q.hint, q.answers);
                });

            $('#' + id + 'n').on('input', function(){
                questions.changes[id] = true;
            });

            $(card.find('.btn-delete')[0]).click(function () {
                $(card.find('.btn-delete')[0]).addClass('disabled');
                $(card.find('.btn-primary')[0]).addClass('disabled');
                $(card.find('.btn-delete')[0]).find('svg').attr('class','fas fa-spinner fa-spin');
                questions.delete(id, name);
            });

            $(card.find('.btn-primary')[0]).click(function () {

                var name = $('#' + id + 'n').val();
                var qs = [];

                var total = 0;

                $('#' + id).find('.pack-question').each(function () {
                    total++;

                    var quid = $(this).attr("id");

                    var question = {
                        text: $('#' + quid + 't').val(),
                        hint: $('#' + quid + 'h').val(),
                        answers: []
                    };

                    for (var i = 1; i < 5; i++) {
                        if ($('#' + quid + 'a' + i).val())
                            question.answers.push($('#' + quid + 'a' + i).val());
                        else
                            break;
                    }

                    console.log(question);

                    if (question.text && question.answers.length === 4)
                        qs.push(question);
                });

                if (total !== qs.length)
                    ui.addAlert('warning', 'Some questions are incomplete, fix it before saving');
                else{
                    $(card.find('.btn-delete')[0]).addClass('disabled');
                    $(card.find('.btn-primary')[0]).addClass('disabled');
                    $(card.find('.btn-primary')[0]).find('svg').attr('class','fas fa-spinner fa-spin');
                    questions.update(id, name, qs);
                }
            });

            card.find('.btn-add').click(function () {
                questions.changes[id] = true;
                var i = card.find('.fa-question-circle').length + 1;
                ui.questions.addQuestion(id, i);
            });

            ui.registerCards();

            if(open){
                $(card.find('.card-link')[0]).click();
            }
        },
        releasePack: function(id){
            var card = $('#' + id + 'c');
            $(card.find('.btn-delete')[0]).removeClass('disabled');
            $(card.find('.btn-delete')[0]).find('svg').attr('class','fas fa-plus');
            $(card.find('.btn-primary')[0]).removeClass('disabled');
            $(card.find('.btn-primary')[0]).find('svg').attr('class','fas fa-sync');
        },
        removePack: function (id) {
            $('#' + id + 'c').remove();
        },
        addQuestion: function (packId, n, text, hint, answers) {
            text = text ? text : '';
            hint = hint ? hint : '';
            answers = answers ? answers : ['', '', '', ''];

            var html = '' +
                '<div id="' + packId + 'q' + n + 'c" class="card text-left">' +
                '<div class="card-header">' +
                '<a class="card-link" data-toggle="collapse" href="#' + packId + 'q' + n + '"><i class="fas fa-chevron-circle-right"></i>&nbsp;Question ' + n + '</a>' +
                '<span title="delete" class="btn-delete text-danger"><i class="fas fa-times"></i></span>' +
                '</div>' +
                '<div id="' + packId + 'q' + n + '" class="collapse pack-question" data-parent="#' + packId + '">' +
                '<div class="card-body">' +
                '<div class="form-group row">' +
                '<label class="col-1 col-form-label"><i class="fas fa-question-circle" title="question"></i></label>' +
                '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 't" placeholder="Question text" value="' + text + '"></div>' +
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
                '<div class="col-6 answer"><div class="btn btn-danger btn-block btn-lg h-100"></div></div>' +
                '<div class="col-6 answer"><div class="btn btn-success btn-block btn-lg h-100"></div></div>' +
                '<div class="col-6 answer"><div class="btn btn-info btn-block btn-lg h-100"></div></div>' +
                '<div class="col-6 answer"><div class="btn btn-warning btn-block btn-lg h-100"></div></div>' +
                '</div>' +
                '<div class="card text-left">' +
                '<div class="card-header"><a class="card-link" data-toggle="collapse"><i class="fas fa-chevron-circle-down"></i>&nbsp;Hint</a></div>' +
                '<div class="collapse show"><div class="card-body"></div></div>' +
                '</div>' +
                '</div></div>' +
                '</div></div></div>';
            $('#' + packId).find('.pack-questions').append(html);

            $($('#' + packId + 'q' + n + 'c').find('.btn-delete')[0]).click(function () {
                questions.changes[packId] = true;
                ui.questions.removeQuestion(packId, n);
            });

            var preview = $('#' + packId + 'q' + n + 'p');
            var previewLink = $($('#' + packId + 'q' + n).find('.card-link')[0]);

            var changeEvent = function () {
                questions.changes[packId] = true;
                if (preview.hasClass("show")) {
                    previewLink.click();
                }
            };

            $('#' + packId + 'q' + n + 't').on('input', changeEvent);
            $('#' + packId + 'q' + n + 'h').on('input', changeEvent);
            $('#' + packId + 'q' + n + 'a1').on('input', changeEvent);
            $('#' + packId + 'q' + n + 'a2').on('input', changeEvent);
            $('#' + packId + 'q' + n + 'a3').on('input', changeEvent);
            $('#' + packId + 'q' + n + 'a4').on('input', changeEvent);

            previewLink.click(function () {
                if (!preview.hasClass("show")) {
                    var text = $('#' + packId + 'q' + n + 't').val();
                    var hint = $('#' + packId + 'q' + n + 'h').val();
                    var answers = [
                        $('#' + packId + 'q' + n + 'a1').val(),
                        $('#' + packId + 'q' + n + 'a2').val(),
                        $('#' + packId + 'q' + n + 'a3').val(),
                        $('#' + packId + 'q' + n + 'a4').val()
                    ];

                    preview.find('h3').html(text);
                    preview.find('.btn-danger').html('A : ' + answers[0]);
                    preview.find('.btn-success').html('B : ' + answers[1]);
                    preview.find('.btn-info').html('C : ' + answers[2]);
                    preview.find('.btn-warning').html('D : ' + answers[3]);
                    $(preview.find('.card-body')[1]).html(hint);
                    window.updateMath();
                    setTimeout(function(){
                        var height = 0;
                        preview.find('.answer').each(function(){
                            if($(this).height()>height){
                                height = $(this).height();
                            }
                        });
                        if(height){
                            preview.find('.answer').each(function(){
                                $(this).height(height);
                            });
                        }
                    });
                }
            });

            ui.registerCards();
        },
        removeQuestion: function (packId, n) {
            $('#' + packId + 'q' + n + 'c').remove();
        }
    }
};
