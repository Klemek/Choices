var questions = {
    changes:{},

    load: function(){
        ui.views.loading();
        ajax.call('GET', '/questions/all', function (data) {
            ui.views.showView('questions');
            questions.pack = [];
            data.forEach(function(pack){
                ui.questions.addPack(pack.id, pack.name, pack.questions);
            });
            $(window).bind('beforeunload', function () {
                if(Object.keys(questions.changes).length > 0)
                    return true;
            });
        }, function () {
            ui.views.showView('menu');
            ui.addAlert('danger','Cannot read questions from server');
        });
    },
    new: function(){
        ui.questions.addPack('tmp'+Math.randInt(0,1000000));
    },
    update: function(packId, name, qs){
        console.log(packId, name, qs);
        if(qs.length === 0){
            ui.addAlert('warning','There is no questions in this pack');
            return;
        }

        var data = {
            name:name,
            questions:JSON.stringify(qs)
        };

        if((''+packId).indexOf('tmp') === 0){
            ajax.call('PUT', '/questions/create', data, function (pack) {
                ui.questions.removePack(packId);
                ui.questions.addPack(pack.id, pack.name, pack.questions, true);
                delete questions.changes[packId];
                ui.addAlert('success', 'Pack updated');
            }, function () {
                ui.questions.releasePack(packId);
                ui.addAlert('danger','Cannot create question pack, please retry');
            });
        }else{
            ajax.call('POST', '/questions/'+packId, data, function (pack) {
                ui.questions.updatePack(pack.id, pack.name, pack.questions, true);
                delete questions.changes[packId];
                ui.addAlert('success', 'Pack updated');
            }, function () {
                ui.questions.releasePack(packId);
                ui.addAlert('danger','Cannot update question pack, please retry');
            });
        }
    },
    delete: function(packId, name){
        if((''+packId).indexOf('tmp') !== 0){
            if(window.confirm("Would you like to delete the pack '"+name+"' ?")) {
                ajax.call('DELETE', '/questions/' + packId, function () {
                    ui.questions.removePack(packId);
                    ui.addAlert('success', 'Pack deleted');
                }, function () {
                    ui.questions.releasePack(packId);
                    ui.addAlert('danger', 'Cannot delete question pack, please retry');
                });
            }else{
                ui.questions.releasePack(packId);
            }
        }else{
            if(window.confirm("Would you like to delete this new pack ?")) {
                ui.questions.removePack(packId);
                ui.addAlert('success', 'Pack deleted');
            }else{
                ui.questions.releasePack(packId);
            }
        }
    }
};