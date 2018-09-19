/**
 * Init admin ui by registering events and adding html content to body
 */
ui.initAdminUi = function () {

    ui.views.views.push('questions', 'texts');

    $.getScript('js/particles.js').done(function () {
        particles.init('particleCanvas');
    });

    let effects = '';
    Object.keys(mapping.nameToEffect).forEach(function (name) {
        effects += '&lt;' + name + '&gt;';
    });

    const getHTMLExamples = function (questions) {
        return '<table class="table table-bordered table-hover">' +
            '<thead><tr><th>Effect</th><th>Code</th><th>Preview</th></tr></thead>' +
            '<tbody>' +
            '<tr><td>Next line</td><td><code>line1&lt;br&gt;line2</code></td><td>line1<br>line2</td></tr>' +
            (questions ? '' : '<tr><td>Random</td><td><code>text1|text2</code></td><td>(pick one at runtime)</td></tr>') +
            '<tr><td>Bold</td><td><code>&lt;b&gt;text&lt;/b&gt;</code></td><td><b>text</b></td></tr>' +
            '<tr><td>Underlined</td><td><code>&lt;u&gt;text&lt;/u&gt;</code></td><td><u>text</u></td></tr>' +
            '<tr><td>Italic</td><td><code>&lt;i&gt;text&lt;/i&gt;</code></td><td><i>text</i></td></tr>' +
            '<tr><td>Striked</td><td><code>&lt;s&gt;text&lt;/s&gt;</code></td><td><s>text</s></td></tr>' +
            '<tr><td>Colored text<br/>(more <a href="https://www.w3schools.com/bootstrap4/bootstrap_colors.asp" target="_blank">here</a>)</td><td><code>&lt;span class="text-danger"&gt;text&lt;/span&gt;</code></td><td><span class="text-danger">text</span></td></tr>' +
            '<tr><td>Link</td><td><code>&lt;a href="https://[...]" target="_blank"&gt;text&lt;/a&gt;</code></td><td><a href="https://google.com" target="_blank">text</a></td></tr>' +
            '<tr><td>Image</td><td><code>&lt;img src="https://[...].png"&gt;</code></td><td><img src="https://www.google.com/cse/static/images/1x/googlelogo_grey_46x15dp.png"></td></tr>' +
            (questions ?
                    '<tr><td>LaTeX formula<br/>(more <a href="https://www.codecogs.com/eqnedit.php" target="_blank">here</a>)</td><td><code>&dollar;\\frac{1}{2}&dollar;</code></td><td>$\\frac{1}{2}$</td></tr>' :
                    '<tr><td>Screen effect</td><td><code>' + effects + '</code></td><td><span id="btn-try-effect" class="btn btn-primary">Try random</span></td></tr>'
            ) +
            '</tbody>' +
            '</table>';
    };

    const html = '<!-- Questions View -->' +
        '<div id="questions-view" style="display:none" class="container">' +
        '<div class="row"><div class="col-md-10 mx-auto"><div class="jumbotron">' +
        '<h4 title="Back" class="btn-back"><a href="#" class="text-secondary"><i class="fas fa-arrow-left"></i></a></h4>' +
        '<h2 class="text-center"><i class="fas fa-edit"></i>&nbsp;Edit questions</h2>' +
        '<br/>' +
        '<div class="card text-left">' +
        '<div class="card-header">' +
        '<a class="card-link h5" data-toggle="collapse" href="#card-tuto-questions"><i class="fas fa-chevron-circle-right"></i>&nbsp;Tutorial</a>' +
        '</div>' +
        '<div id="card-tuto-questions" class="collapse">' +
        '<div class="card-body">' +
        '<h4>Videos</h4>' +
        'In each video (main video or wrong answer\'s help) you can put the following :' +
        '<ul>' +
        '<li>a youtube link (it will be displayed as an integrated video)</li>' +
        '<li>a link (it will be displayed as clickable link)(add the name of the link before the link between [ ] )</li>' +
        '<li>custom HTML (see below)</li>' +
        '</ul>' +
        '<h4>Questions</h4>' +
        'You can use HTML in questions and answers.<br/>' +
        'Here is what you can do :<br/><br/>' +
        getHTMLExamples(true) +
        '</div></div></div>' +
        '<br/>' +
        '<div id="packs"></div>' +
        '<br/>' +
        '<div class="row"><div class="col-6">' +
        '<button id="btn-create-pack" class="btn btn-success btn-block"><i class="fas fa-plus"></i>&nbsp;New question pack</button>' +
        '</div>' +
        '<div class="col-3">' +
        '<button id="btn-import" class="btn btn-primary btn-block"><i class="fas fa-file-upload"></i>&nbsp;Import</button>' +
        '<input id="input-import" type="file" accept="' + globals.xlsxFileFilter + '" style="display:none" multiple>' +
        '</div>' +
        '<div class="col-3">' +
        '<button id="btn-template" class="btn btn-primary btn-block"><i class="fas fa-file-download"></i>&nbsp;Template</button>' +
        '</div>' +
        '</div></div></div></div></div>' +
        '<!-- Texts View -->' +
        '<div id="texts-view" style="display:none" class="container">' +
        '<div class="row"><div class="col-md-9 mx-auto"><div class="jumbotron">' +
        '<h4 title="Back" class="btn-back"><a href="#" class="text-secondary"><i class="fas fa-arrow-left"></i></a></h4>' +
        '<h2 class="text-center"><i class="fas fa-align-left"></i>&nbsp;Edit texts</h2>' +
        '<br/>' +
        '<div class="card text-left">' +
        '<div class="card-header">' +
        '<a class="card-link h5" data-toggle="collapse" href="#card-tuto-texts"><i class="fas fa-chevron-circle-right"></i>&nbsp;Tutorial</a>' +
        '</div>' +
        '<div id="card-tuto-texts" class="collapse">' +
        '<div class="card-body">' +
        'You can use HTML in most of the texts in this app.<br/>' +
        'Do not remove <code>{n}</code> in texts.<br/>' +
        'Here is what you can do :<br/><br/>' +
        getHTMLExamples(false) +
        '</div></div></div>' +
        '<br/>' +
        '<div id="texts"></div>' +
        '<br/>' +
        '<div class="row"><div class="col-6 offset-1">' +
        '<button id="btn-update-texts" class="btn btn-success btn-block"><i class="fas fa-sync"></i>&nbsp;Update texts</button>' +
        '</div>' +
        '<div class="col-4">' +
        '<button id="btn-undo-texts" class="btn btn-danger btn-block"><i class="fas fa-undo"></i>&nbsp;Revert changes</button>' +
        '</div>' +
        '</div></div></div></div></div>';

    $('body').append(html);

    utils.loadMathJax(function () {
        utils.updateMath();
    });

    const htmlMenuButtons = '<button id="btn-questions" class="btn btn-outline-primary btn-block">Edit questions' +
        '</button>' +
        '<button id="btn-texts" class="btn btn-outline-primary btn-block">Edit texts' +
        '</button>';

    $('#div-create-room').append(htmlMenuButtons);

    //load admin main menu events
    $('#btn-questions').click(function () {
        utils.setPage('Edit questions', 'questions');
        $('#packs').html('');
        questions.load(function () {
            //load questions view event
            $('#btn-create-pack').click(questions.new);
            $('#btn-template').click(questions.downloadTemplate);
            const inputImport = $('#input-import');
            const btnImport = $('#btn-import');
            btnImport.click(function () {
                try {
                    inputImport[0].value = '';
                    if (inputImport[0].value) {
                        inputImport[0].type = 'text';
                        inputImport[0].type = 'file';
                    }
                } catch (e) {
                }
                inputImport.click();
            });
            inputImport.change(function () {
                if (this.files && this.files.length > 0) {
                    const classes = btnImport.find('i').attr('class');
                    btnImport.find('i').attr('class', 'fas fa-spinner fa-spin');
                    btnImport.addClass('disabled');
                    Array.from(this.files).forEach(function (f) {
                        questions.importFile(f, undefined, function () {
                            btnImport.find('i').attr('class', classes);
                            btnImport.removeClass('disabled');
                        });
                    });
                }
            });
        });
    });
    $('#btn-texts').click(function () {
        //load texts view event
        utils.setPage('Edit texts', 'texts');
        ui.texts.initView();
    });
};


ui.questions = {
    /**
     * Add a pack to view
     * @param {string} id
     * @param {string} [name]
     * @param {string} [video]
     * @param {string} [message]
     * @param {{text:string,answers:string[],links:string[]}[]} [qs]
     * @param {boolean} [enabled]
     * @param {boolean} [open]
     */
    addPack: function (id, name, video, message, qs, enabled, open) {
        const html = '' +
            '<div id="' + id + 'c" class="card text-left"></div>';
        $('#packs').append(html);
        ui.questions.updatePack(id, name, video, message, qs, enabled, open);
    },
    /**
     * Update a pack view
     * @param {string} id
     * @param {string} name
     * @param {string} video
     * @param {string} message
     * @param {{text:string,answers:string[],links:string[]}[]} qs
     * @param {boolean} enabled
     * @param {boolean} open
     */
    updatePack: function (id, name, video, message, qs, enabled, open) {

        const vids = video ? video.split(';') : undefined;

        const html = '' +
            '<div class="card-header">' +
            '<a class="card-link" data-toggle="collapse" href="#' + id + '"><i class="fas fa-chevron-circle-right"></i>&nbsp;' + (name ? name : 'New question pack') + '</a>' +
            '<span title="Delete" class="btn-delete text-danger"><i class="fas fa-times"></i></span>' +
            '<span title="' + (enabled ? 'Visible' : 'Hidden') + '" class="visibility-badge"><i class="fas fa-eye' + (enabled ? '' : '-slash') + '"></i></span>' +
            '</div>' +
            '<div id="' + id + '" class="collapse" data-parent="#packs">' +
            '<div class="card-body">' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-edit" title="Pack name"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + id + 'n" placeholder="Pack name" value="' + (name ? name : '') + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-eye" title="Visible"></i></label>' +
            '<div class="col-11"><select class="form-control custom-select-md" id="' + id + 'e">' +
            '<option value="enabled" ' + (enabled ? 'selected' : '') + '>Visible</option>' +
            '<option value="disabled" ' + (enabled ? '' : 'selected') + '>Hidden</option>' +
            '</select></div>' +
            '</div>' +
            '<div id="' + id + 'vs">' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-video" title="Pack video link"></i></label>' +
            '<div class="col-10"><input type="text" class="form-control ' + id + 'v" placeholder="Pack video link" value="' + (vids ? vids[0] : '') + '"></div>' +
            '<label class="col-1 col-form-label"><i id="' + id + 'vp" title="add video" class="fas fa-plus cursor-pointer"></i></label>' +
            '</div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-comment-alt" title="Pack finish message (displayed when mastered)"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + id + 'm" placeholder="Pack finish message (displayed when mastered)" value="' + (message ? message : '') + '"></div>' +
            '</div>' +
            '<div class="pack-questions"></div><br/>' +
            '<input id="' + id + 'i" type="file" accept="' + globals.xlsxFileFilter + '" style="display:none">' +
            '<div class="row">' +
            '<div class="col-6"><button class="pack-btn btn btn-primary btn-block"><i class="fas fa-sync"></i>&nbsp;Save</button></div>' +
            '<div class="col-6"><button class="pack-btn btn btn-add btn-success btn-block"><i class="fas fa-plus"></i>&nbsp;New question</button></div>' +
            '<div class="col-6"><button class="pack-btn btn btn-primary btn-block"><i class="fas fa-file-upload"></i>&nbsp;Import changes</button></div>' +
            '<div class="col-6"><button class="pack-btn btn btn-primary btn-block"><i class="fas fa-file-download"></i>&nbsp;Download</button></div>' +
            '</div></div></div>';

        const card = $('#' + id + 'c');

        card.html(html);

        if (vids)
            vids.forEach(function (v, i) {
                if (i > 0) {
                    $('#' + id + 'vs').append('' +
                        '<div class="form-group row">' +
                        '<label class="col-1 col-form-label"><i class="fas fa-video" title="Pack video link (blank to delete)"></i></label>' +
                        '<div class="col-10"><input type="text" class="form-control ' + id + 'v" placeholder="Pack video link (blank to delete)" value="' + v + '"></div>' +
                        '</div>'
                    );
                }
            });

        $('#' + id + 'vp').click(function () {
            $('#' + id + 'vs').append('' +
                '<div class="form-group row">' +
                '<label class="col-1 col-form-label"><i class="fas fa-video" title="Pack video link (blank to delete)"></i></label>' +
                '<div class="col-10"><input type="text" class="form-control ' + id + 'v" placeholder="Pack video link (blank to delete)"></div>' +
                '</div>'
            );
        });

        if (qs)
            qs.forEach(function (q, i) {
                ui.questions.addQuestion(id, i + 1, q.text, q.answers, q.links);
            });

        $('#' + id + 'n').on('input', function () {
            questions.changes[id] = true;
        });

        $(card.find('.btn-delete')[0]).click(function () {
            $(card.find('.btn-delete')[0]).addClass('disabled');
            $(card.find('.btn-primary')[0]).addClass('disabled');
            $(card.find('.btn-primary')[1]).addClass('disabled');
            $(card.find('.btn-primary')[2]).addClass('disabled');
            $(card.find('.btn-delete')[0]).find('i').attr('class', 'fas fa-spinner fa-spin');
            questions.delete(id, name);
        });

        const readValues = function () {

            let videos = [];
            $('.' + id + 'v').each(function () {
                const v = $(this).val();
                if (v && v.length > 0)
                    videos.push($(this).val());
            });

            const pack = {
                name: $('#' + id + 'n').val(),
                message: $('#' + id + 'm').val(),
                video: videos.join(';'),
                qs: [],
                enabled: $('#' + id + 'e').val() === 'enabled'
            };

            let total = 0;

            $('#' + id).find('.pack-question').each(function () {
                total++;

                const quid = $(this).attr('id');

                const question = {
                    text: $('#' + quid + 't').val(),
                    answers: [],
                    links: []
                };

                for (let i = 1; i <= 4; i++) {
                    const ans = $('#' + quid + 'a' + i).val();
                    if (ans)
                        question.answers.push(ans);
                    else
                        break;
                    if (i > 1) {
                        const link = $('#' + quid + 'l' + i).val();
                        question.links.push(link ? link : '');
                    } else {
                        question.links.push('');
                    }
                }

                if (question.text && question.answers.length === 4)
                    pack.qs.push(question);
            });

            if (!pack.name || pack.name.length === 0)
                ui.alert('warning', 'This pack has no name');
            else if (total !== pack.qs.length)
                ui.alert('warning', 'Some questions are incomplete, fix it before saving');
            else
                return pack;
        };

        $(card.find('.btn-primary')[0]).click(function () {
            const pack = readValues();
            if (pack) {
                $(card.find('.btn-delete')[0]).addClass('disabled');
                $(card.find('.btn-primary')[0]).addClass('disabled');
                $(card.find('.btn-primary')[1]).addClass('disabled');
                $(card.find('.btn-primary')[2]).addClass('disabled');
                $(card.find('.btn-primary')[0]).find('i').attr('class', 'fas fa-spinner fa-spin');
                questions.update(id, pack.name, pack.video, pack.message, pack.qs, pack.enabled);
            }
        });

        const inputImport = $('#' + id + 'i');
        const btnImport = $(card.find('.btn-primary')[1]);
        btnImport.click(function () {
            try {
                inputImport[0].value = '';
                if (inputImport[0].value) {
                    inputImport[0].type = 'text';
                    inputImport[0].type = 'file';
                }
            } catch (e) {
            }
            inputImport.click();
        });
        inputImport.change(function () {
            if (this.files && this.files[0]) {
                const classes = btnImport.find('i').attr('class');
                btnImport.find('i').attr('class', 'fas fa-spinner fa-spin');
                btnImport.addClass('disabled');
                questions.importFile(this.files[0], id, function () {
                    btnImport.find('i').attr('class', classes);
                    btnImport.removeClass('disabled');
                });
            }
        });

        $(card.find('.btn-primary')[2]).click(function () {
            const pack = readValues();
            if (pack)
                questions.exportPack(pack.name, pack.video, pack.message, pack.qs);
        });

        card.find('.btn-add').click(function () {
            questions.changes[id] = true;
            const i = card.find('.fa-question-circle').length + 1;
            ui.questions.addQuestion(id, i);
        });

        ui.registerCards();

        if (open) {
            $(card.find('.card-link')[0]).click();
        }
    },
    /**
     * Release buttons of pack
     * @param {string} id
     */
    releasePack: function (id) {
        const card = $('#' + id + 'c');
        $(card.find('.btn-delete')[0]).removeClass('disabled');
        $(card.find('.btn-delete')[0]).find('i').attr('class', 'fas fa-plus');
        $(card.find('.btn-primary')[0]).removeClass('disabled');
        $(card.find('.btn-primary')[0]).find('i').attr('class', 'fas fa-sync');
    },
    /**
     * Remove a pack
     * @param {string} id
     */
    removePack: function (id) {
        $('#' + id + 'c').remove();
    },
    /**
     * Add a question to the pack view
     * @param {string} packId
     * @param {int} n
     * @param {string} [text]
     * @param {string[]} [answers]
     * @param {string[]} [links]
     */
    addQuestion: function (packId, n, text, answers, links) {
        text = text ? text : '';
        answers = answers ? answers : ['', '', '', ''];
        links = links ? links : ['', '', '', ''];

        const html = '' +
            '<div id="' + packId + 'q' + n + 'c" class="card text-left">' +
            '<div class="card-header">' +
            '<a class="card-link" data-toggle="collapse" href="#' + packId + 'q' + n + '"><i class="fas fa-chevron-circle-right"></i>&nbsp;Question ' + n + '</a>' +
            '<span title="Delete" class="btn-delete text-danger"><i class="fas fa-times"></i></span>' +
            '</div>' +
            '<div id="' + packId + 'q' + n + '" class="collapse pack-question" data-parent="#' + packId + '">' +
            '<div class="card-body">' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-question-circle" title="Question text"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 't" placeholder="Question text" value="' + text + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-success fa-check" title="Answer 1 (correct)"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a1" placeholder="Answer 1 (correct)" value="' + answers[0] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-danger fa-times" title="Answer 2 (wrong)"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a2" placeholder="Answer 2 (wrong)" value="' + answers[1] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-danger fa-times" title="Answer 3 (wrong)"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a3" placeholder="Answer 3 (wrong)" value="' + answers[2] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas text-danger fa-times" title="Answer 4 (wrong)"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'a4"  placeholder="Answer 4 (wrong)" value="' + answers[3] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-video" title="Answer 2\'s help link"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'l2" placeholder="Answer 2\'s help link" value="' + links[1] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-video" title="Answer 3\'s help link"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'l3" placeholder="Answer 3\'s help link" value="' + links[2] + '"></div>' +
            '</div>' +
            '<div class="form-group row">' +
            '<label class="col-1 col-form-label"><i class="fas fa-video" title="Answer 4\'s help link"></i></label>' +
            '<div class="col-11"><input type="text" class="form-control" id="' + packId + 'q' + n + 'l4" placeholder="Answer 4\'s help link" value="' + links[3] + '"></div>' +
            '</div>' +
            '<div class="card text-left">' +
            '<div class="card-header">' +
            '<a class="card-link" data-toggle="collapse" href="#' + packId + 'q' + n + 'p"><i class="fas fa-eye"></i>&nbsp;Preview</a>' +
            '</div>' +
            '<div id="' + packId + 'q' + n + 'p" class="collapse" data-parent="#' + packId + 'q' + n + '">' +
            '<div class="card-body">' +
            '<h3 class="text-center"></h3>' +
            '<div class="row">' +
            '<div class="col-6 answer"><div class="btn btn-success btn-block btn-lg h-100"></div></div>' +
            '<div class="col-6 answer"><div class="btn btn-danger btn-block btn-lg h-100"></div></div>' +
            '<div class="col-6 answer"><div class="btn btn-info btn-block btn-lg h-100"></div></div>' +
            '<div class="col-6 answer"><div class="btn btn-warning btn-block btn-lg h-100"></div></div>' +
            '</div>' +
            '</div></div>' +
            '</div></div></div>';
        $('#' + packId).find('.pack-questions').append(html);

        const cardheader = $('#' + packId + 'q' + n + 'c');

        $(cardheader.find('.btn-delete')[0]).click(function () {
            questions.changes[packId] = true;
            ui.questions.removeQuestion(packId, n);
        });

        const preview = $('#' + packId + 'q' + n + 'p');
        const previewLink = $($('#' + packId + 'q' + n).find('.card-link')[0]);

        const changeEvent = function () {
            questions.changes[packId] = true;
            if (preview.hasClass('show')) {
                previewLink.click();
            }
        };

        $('#' + packId + 'q' + n + 't').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'h').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'a1').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'a2').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'a3').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'a4').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'l2').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'l3').on('input', changeEvent);
        $('#' + packId + 'q' + n + 'l4').on('input', changeEvent);

        previewLink.click(function () {
            if (!preview.hasClass('show')) {
                const text = $('#' + packId + 'q' + n + 't').val();
                const answers = [
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
                utils.updateMath();
                setTimeout(function () {
                    let height = 0;
                    preview.find('.answer').each(function () {
                        if ($(this).height() > height) {
                            height = $(this).height();
                        }
                    });
                    if (height) {
                        preview.find('.answer').each(function () {
                            if ($(this).height() < height) {
                                $(this).height(height);
                            }
                        });
                    }
                });
            }
        });

        ui.registerCards();

        if (text.length === 0) {
            $(cardheader.find('.card-link')[0]).click();
        }
    },
    /**
     * Remove a question from pack view
     * @param {string} packId
     * @param {int} n
     */
    removeQuestion: function (packId, n) {
        $('#' + packId + 'q' + n + 'c').remove();
    }
};

ui.texts = {
    /**
     * Init text edition view
     */
    initView: function () {
        ui.views.loading();
        const btn = $('#btn-update-texts');
        btn.click(function () {
            const classes = btn.find('i').attr('class');
            btn.addClass('disabled');
            btn.find('i').attr('class', 'fas fa-spinner fa-spin');
            const values = ui.texts.getValues();
            if (values !== null) {
                lang.update(values, function () {
                    btn.removeClass('disabled');
                    btn.find('i').attr('class', classes);
                    ui.texts.populate();
                });
            } else {
                ui.alert('danger', 'You have invalid HTML, please fix it before saving.');
                btn.removeClass('disabled');
                btn.find('i').attr('class', classes);
            }
        });

        $('#btn-try-effect').click(function () {
            particles.show(globals.particlesTime, utils.getRandom(Object.values(mapping.nameToEffect)));
        });

        this.populate();
        ui.views.showView('texts');
    },
    /**
     * Add all texts values to view
     */
    populate: function () {
        const keys = Object.keys(lang.strings);
        keys.sort();
        const texts = $('#texts');
        texts.html('');
        Object.keys(mapping.categoryToLangPrefix).forEach(function (key) {
            texts.append('<h4 class="text-center"><u>' + key + '</u></h4><div id="text-' + key.toLowerCase().replaceAll(' ', '-') + '"></div><br/>');
        });
        keys.forEach(function (key) {
            const cat = mapping.langToCategory(key).toLowerCase().replaceAll(' ', '-');
            $('#text-' + cat).append('' +
                '<div class="form-group row">' +
                '<label class="col-3 text-right col-form-label"><b>' + key + '</b></label>' +
                '<div class="col-9"><input type="text" id="input-' + key + '" class="form-control input-lang-text" value="' + lang.strings[key] + '"></div>' +
                '</div>');
        });
        $('.input-lang-text').on('change', function () {
            if (ui.texts.checkValue($(this).val()))
                $(this).removeClass('is-invalid').addClass('is-valid');
            else
                $(this).removeClass('is-valid').addClass('is-invalid');
        });
    },
    /**
     * Gather all values from view
     * @returns {Object|null}
     */
    getValues: function () {
        const values = {};
        let valid = true;
        $('.input-lang-text').each(function () {
            const key = $(this).attr('id').replace('input-', '');
            const val = $(this).val();

            if (!ui.texts.checkValue(val)) {
                valid = false;
                $(this).removeClass('is-valid').addClass('is-invalid');
            } else {
                values[key] = (val && val.length > 0) ? val : ' ';
            }
        });
        return valid ? values : null;
    },
    /**
     * Check if a value is correct
     * @param {string} val - value to check
     * @return {boolean}
     */
    checkValue: function (val) {
        let valid = true;
        val.split('|').forEach(function (v) {
            Object.keys(mapping.nameToEffect).forEach(function (effect) {
                v = v.replace('<' + effect + '>', '');
            });
            if (!utils.checkHTML(v) && !v.includes('; '))
                valid = false;
        });
        return valid;
    }
};

//# sourceURL=js/admin-ui.js