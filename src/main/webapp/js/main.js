globals.appPath = utils.getRootPath();
//let browser decide of caching
$.ajaxSetup({
    cache: true
});

//check session at start
ajax.call('GET', '/',
    /**
     * @param {{userId: String, userName: String, userEmail: String, userImageUrl: String, admin: boolean, langHash: int, appPath: string}} data - result from request
     */
    function (data) {
        globals.appPath = data.appPath.endsWith('/') ? data.appPath.substr(0, data.appPath.length - 1) : data.appPath;
        if (data.userId) {
            $(document).ready(function () {
                data.userImageUrl = data.userImageUrl.split('sz=')[0];
                if (data.admin && !jQuery.browser.mobile) {
                    $.getMultiScripts(['js/admin-ui.js','js/questions.js']).done(function(){
                        ui.setCurrentUser(data);
                    });
                }else{
                    ui.setCurrentUser(data);
                }

                const match = /roomid=([^&]+)/.exec(window.location.search);

                //to avoid this to be ready before UI
                const interval = setInterval(function () {
                    if (ui.isReady()) {
                        clearInterval(interval);
                        if (match && match[1]) {
                            padRoom.join(match[1]);
                        } else {
                            ui.views.showView('menu');
                            switch (window.location.search) {
                                case '?create':
                                    ui.goToView('create');
                                    break;
                                case '?questions':
                                    if (data.admin)
                                        ui.goToView('questions');
                                    else
                                        ui.alert('warning', lang.getString('warnForbidden'));
                                    break;
                                case '?texts':
                                    if (data.admin)
                                        ui.goToView('texts');
                                    else
                                        ui.alert('warning', lang.getString('warnForbidden'));
                                    break;
                            }


                        }
                    }
                }, 100);

            });

            lang.init(data);

        } else {
            window.location.href = globals.appPath + '/login?redirect=' + encodeURI(window.location.href);
        }
    }, function () {
        window.location.href = globals.appPath + '/login?redirect=' + encodeURI(window.location.href);
    });

$(document).ready(ui.setDocumentReady);

//# sourceURL=js/main.js