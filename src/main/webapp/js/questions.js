/* exported questions */
const questions = {
    changes: {},
    /**
     * Load all packs
     * @param {function} callback
     */
    load: function (callback) {
        ui.views.loading();
        utils.loadMathJax();
        $.getScript(globals.xlsxCDN);
        ajax.call('GET', '/questions/all',
            /**
             * @param {{id:string,name:string,video:string,message:string,enabled:boolean,questions:{text:string,answers:string[],links:string[]}[]}[]} data
             */
            function (data) {
                ui.views.showView('questions');
                questions.pack = [];
                data.forEach(function (pack) {
                    ui.questions.addPack(pack.id, pack.name, pack.video, pack.message, pack.questions, pack.enabled);
                });
                $(window).bind('beforeunload', function () {
                    if (Object.keys(questions.changes).length > 0)
                        return true;
                });
                if (callback)
                    callback();
            }, function () {
                ui.views.showView('menu');
                ui.alert('danger', 'Cannot read questions from server');
            });
    },
    /**
     * Add a new pack
     */
    new: function () {
        ui.questions.addPack('tmp' + utils.randInt(0, 1000000));
    },
    /**
     * Update a pack
     * @param {string} packId
     * @param {string} name
     * @param {string} video
     * @param {string} message
     * @param {{text:string,answers:string[],links:string[]}[]} qs
     * @param {boolean} enabled
     */
    update: function (packId, name, video, message, qs, enabled) {
        if (qs.length < globals.minQuestions) {
            ui.alert('warning', 'There is not enough questions in this pack (minimum 10)');
            return;
        }

        const data = {
            name: name,
            video: video,
            message: message,
            questions: JSON.stringify(qs),
            enabled: enabled
        };

        if (('' + packId).indexOf('tmp') === 0) {
            ajax.call('PUT', '/questions/create', data,
                /**
                 * @param {{id:string,name:string,video:string,message:string,enabled:boolean,questions:{text:string,answers:string[],links:string[]}[]}} pack
                 */
                function (pack) {
                    ui.questions.removePack(packId);
                    ui.questions.addPack(pack.id, pack.name, pack.video, pack.message, pack.questions, pack.enabled, true);
                    delete questions.changes[packId];
                    ui.alert('success', 'Pack updated');
                }, function () {
                    ui.questions.releasePack(packId);
                    ui.alert('danger', 'Cannot create question pack, please retry');
                });
        } else {
            ajax.call('POST', '/questions/' + packId, data,
                /**
                 * @param {{id:string,name:string,video:string,message:string,enabled:boolean,questions:{text:string,answers:string[],links:string[]}[]}} pack
                 */
                function (pack) {
                    ui.questions.updatePack(pack.id, pack.name, pack.video, pack.message, pack.questions, pack.enabled, true);
                    delete questions.changes[packId];
                    ui.alert('success', 'Pack updated');
                }, function () {
                    ui.questions.releasePack(packId);
                    ui.alert('danger', 'Cannot update question pack, please retry');
                });
        }
    },
    /**
     * Delete a pack
     * @param {string} packId
     * @param {string} name
     */
    delete: function (packId, name) {
        if (('' + packId).indexOf('tmp') !== 0) {
            if (window.confirm('Would you like to delete the pack {0} ?'.format(name))) {
                ajax.call('DELETE', '/questions/' + packId, function () {
                    ui.questions.removePack(packId);
                    ui.alert('success', 'Pack deleted');
                }, function () {
                    ui.questions.releasePack(packId);
                    ui.alert('danger', 'Cannot delete question pack, please retry');
                });
            } else {
                ui.questions.releasePack(packId);
            }
        } else {
            if (window.confirm('Would you like to delete this new pack ?')) {
                ui.questions.removePack(packId);
                ui.alert('success', 'Pack deleted');
            } else {
                ui.questions.releasePack(packId);
            }
        }
    },
    /**
     * Generate then download template
     */
    downloadTemplate: function () {
        if (typeof XLSX === 'undefined')
            return;
        const helpData = [
            ['Welcome to the template spreadsheet.'],
            ['You can delete this \'help\' sheet and keep only the \'data\' one for common usage.'],
            [],
            ['STEPS'],
            ['First you will need to enter some information for the context such as the name, video or message. (Video and message can be blank)'],
            ['Then you will need to enter all questions after the 5th line.'],
            [],
            ['VIDEOS'],
            ['In each video cell (main video or wrong answer\'s help) you can put the following :'],
            ['* a youtube link (it will be displayed as an integrated video)'],
            ['* a link (it will be displayed as clickable link)(add the name of the link before the link between [ ] )'],
            ['* custom html'],
            ['* several videos / links separated by the character \';\''],
            [],
            ['QUESTIONS'],
            ['When entering questions, you can use extra columns, they will not be accounted in the import.'],
            ['Either question\'s text or answers can contains LaTeX formulas between two $.'],
            ['* For example $\\frac{1}{2}$ will show a nice 1/2 fracton.'],
            ['* See https://www.codecogs.com/eqnedit.php to conceive formulas'],
            ['You can leave an answer\'s help video blank'],
        ];
        let templateData = [['Name', 'Your pack name'],
            ['Video', 'Your pack main video'],
            ['Message', 'Message to be shown when mastered'],
            [],
            ['Question', 'Answer 1', 'Answer 2', 'Answer 3', 'Answer 4', 'Video 2', 'Video 3', 'Video 4']];
        for (let i = 1; i <= 10; i++) {
            templateData.push(['Question ' + i + '\'s text', 'Correct answer', 'Wrong answer', 'Wrong answer', 'Wrong answer',
                'Answer 2\'s help', 'Answer 3\'s help', 'Answer 3\'s help']);
        }
        /*jshint -W106 */
        const workbook = XLSX.utils.book_new();
        const helpSheet = XLSX.utils.aoa_to_sheet(helpData);
        XLSX.utils.book_append_sheet(workbook, helpSheet, 'help');
        const dataSheet = XLSX.utils.aoa_to_sheet(templateData);
        XLSX.utils.book_append_sheet(workbook, dataSheet, 'data');
        XLSX.writeFile(workbook, 'template.xlsx');
        /*jshint +W106 */
    },
    /**
     * Export a pack
     * @param {string} name
     * @param {string} video
     * @param {string} message
     * @param {{text:string,answers:string[],links:string[]}[]} qs
     */
    exportPack: function (name, video, message, qs) {
        if (typeof XLSX === 'undefined')
            return;
        let packData = [['Name', name],
            ['Video', video],
            ['Message', message],
            [],
            ['Question', 'Answer 1', 'Answer 2', 'Answer 3', 'Answer 4', 'Video 2', 'Video 3', 'Video 4']];
        qs.forEach(function (question) {
            const tmp = [question.text];
            question.answers.forEach(function (e) {
                tmp.push(e);
            });
            question.links.forEach(function (e, i) {
                if (i > 0)
                    tmp.push(e);
            });
            packData.push(tmp);
        });
        let fileName = name.replace(/[^a-z0-9]/gi, '_').toLowerCase();
        /*jshint -W106 */
        const workbook = XLSX.utils.book_new();
        const dataSheet = XLSX.utils.aoa_to_sheet(packData);
        XLSX.utils.book_append_sheet(workbook, dataSheet, 'data');
        XLSX.writeFile(workbook, fileName + '.xlsx');
        /*jshint +W106 */
    },
    /**
     * Import user file
     * @param file
     * @param {String|undefined} packId
     * @param {Function} callback
     */
    importFile: function (file, packId, callback) {
        const reader = new FileReader();
        let extension = file.name.split('.').pop();//should be csv or xls / xlsx
        switch (extension) {
            case 'csv':
                if (file.type !== 'text/csv') {
                    ui.alert('warning', 'File <b>' + file.name + '</b> : Invalid file format, mime type is invalid');
                    callback(false);
                    return;
                }
                reader.onload = function (e) {
                    const res = questions.loadCSV(file.name, e.target.result, packId);
                    callback(res);
                };
                reader.readAsText(file);
                return;
            case 'xls':
            case 'xlsx':
                if (file.type !== 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' &&
                    file.type !== 'application/vnd.ms-excel') {
                    ui.alert('warning', 'File <b>' + file.name + '</b> : Invalid file format, mime type is invalid');
                    callback(false);
                    return;
                }
                if (typeof XLSX === 'undefined') {
                    callback(false);
                    return;
                }
                const rABS = true; // true: readAsBinaryString ; false: readAsArrayBuffer
                reader.onload = function (e) {
                    let data = e.target.result;
                    if (!rABS) data = new Uint8Array(data);
                    const workbook = XLSX.read(data, {type: rABS ? 'binary' : 'array'});
                    let found = false;
                    let result = false;
                    workbook.SheetNames.forEach(function (name) {
                        if (name === 'data') {
                            /*jshint -W106 */
                            let content = XLSX.utils.sheet_to_csv(workbook.Sheets[name], {FS: ';'});
                            /*jshint +W106 */
                            result = questions.loadCSV(file.name, content, packId);
                            found = true;
                        }
                    });
                    if (!found)
                        ui.alert('danger', 'File <b>' + file.name + '</b> : No sheet \'data\' found');
                    callback(found && result);
                };
                if (rABS)
                    reader.readAsBinaryString(file);
                else
                    reader.readAsArrayBuffer(file);
                return;
            default:
                ui.alert('warning', 'File <b>' + file.name + '</b> : Invalid file format .' + extension);
                callback(false);
                return;
        }
    },
    /**
     * Process CSV file
     * @param {String} fname
     * @param {String} data
     * @param {String} packId
     */
    loadCSV: function (fname, data, packId) {
        let name = '';
        let qs;
        let video = '';
        let message = '';
        let nInvalid = 0;
        const header = ['Question', 'Answer 1', 'Answer 2', 'Answer 3', 'Answer 4', 'Video 2', 'Video 3', 'Video 4'];
        data.split('\n').forEach(function (line) {
            if (line.length > 0) {
                const cells = utils.splitCSVLine(line);
                if (!qs) {
                    switch (cells[0].toLowerCase()) {
                        case 'name':
                            name = cells[1];
                            if (!name) {
                                ui.alert('danger', 'File <b>' + fname + '</b> : Name is blank');
                                return false;
                            }
                            break;
                        case 'video':
                            video = cells[1];
                            console.log(line, cells);
                            break;
                        case 'message':
                            message = cells[1];
                            break;
                        case 'question':
                            let isHeader = true;
                            cells.forEach(function (c, i) {
                                if (c !== header[i])
                                    isHeader = false;
                            });
                            if (isHeader) {
                                if (!name) {
                                    ui.alert('danger', 'File <b>' + fname + '</b> : No name definition found');
                                    return false;
                                }
                                qs = [];
                            }
                            break;
                    }
                } else {
                    if (cells.length > 5) {
                        const question = {
                            text: cells[0],
                            answers: ['', '', '', ''],
                            links: ['', '', '', '']
                        };
                        let valid = question.text && question.text.length > 0;
                        cells.forEach(function (v, i) {
                            if (valid) {
                                if (i > 0 && i <= 4) {
                                    if (v && v.length > 0) {
                                        question.answers[i - 1] = v;
                                    } else {
                                        valid = false;
                                    }
                                } else if (i > 4 && i <= 8 && v && v.length > 0) {
                                    question.links[i - 4] = v;
                                }
                            }
                        });
                        if (valid)
                            qs.push(question);
                        else
                            nInvalid++;
                    }
                }
            }
        });
        if (!name || name.length === 0) {
            ui.alert('danger', 'File <b>' + fname + '</b> : No name definition found');
            return false;
        } else if (qs === undefined) {
            ui.alert('danger', 'File <b>' + fname + '</b> : The question header was not found');
            return false;
        } else {
            if (nInvalid > 0)
                ui.alert('warning', 'File <b>' + fname + '</b> : ' + nInvalid + ' questions were invalid');
            ui.alert('success', 'Loaded file <b>' + fname + '</b>');
            questions.update(packId ? packId : 'tmpImport', name, video, message, qs);
            return true;
        }
    }
};

//# sourceURL=js/questions.js