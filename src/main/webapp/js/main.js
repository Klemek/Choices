//check session at start
ajax.call('GET', '/session', function (data) {
    if (data.userId) {
        $(document).ready(function () {
            data.userImageUrl = data.userImageUrl.split('sz=')[0];

            ui.setCurrentUser(data);

            var url = new URL(window.location);
            var tmproomid = url.searchParams.get("roomid");

            if (tmproomid) {
                room.join(tmproomid);
            } else {
                ui.showView('menu');
            }
        });
    } else {
        window.location.href = "/login?redirect=" + encodeURI(window.location.href);
    }
}, function () {
    window.location.href = "/login?redirect=" + encodeURI(window.location.href);
});

$(document).ready(ui.initUI);