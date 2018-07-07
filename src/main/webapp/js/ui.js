var ui = {
    views:['menu','room','pad'],
    addAlert: function (type, text) {
        $('#alerts').append('<div class="alert alert-' + type + ' alert-dismissible fade show"><button type="button" class="close" data-dismiss="alert">&times;</button>' + text + '</div>')
        $('#alerts:last-child').hide().fadeIn();
    },
    clearMembers: function(){
        $("#members").html("");
    },
    addMember: function (member) {
        member.imageUrl = member.imageUrl.split('sz=')[0];
        var html = '<div id="' + member.id + '" class="col-4" title="kick ' + member.name + '"><div class="jumbotron bg-dark member-card text-center text-white">' +
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
    setCurrentUser: function(data){
        $('#user').show();
        $('#user').attr("title", data.userName);
        $('.user-name').text(data.userName);
        $('.user-mail').text(data.userEmail);
        $('.user-image-nav').attr("src", data.userImageUrl + "sz=42");
        $('.user-image-full').attr("src", data.userImageUrl + "sz=70");
    },
    showView: function(name){
        this.hideAll();
        $('#'+name+'-view').show();
    },
    loading: function(){
        this.hideAll();
        $('#loading').show();
    },
    hideAll: function(){
        $('#alerts').html('');
        this.views.forEach(function(name){
            $('#'+name+'-view').hide();
        });
        $('#loading').hide();
    },
    changeMobile: function(){
        if (jQuery.browser.mobile) {
            $('#div-create-room').hide();
            $('#div-join-room').attr("class", "col-12");
        }
    },
    registerEvents: function(){
        //main menu
        $("#btn-create").click(room.create);
        $("#btn-join").click(function () {
            var tmproomid = $('#roomid').val();
            $('#roomid').val('');
            if (tmproomid && tmproomid.length > 0) {
                room.join(tmproomid, true);
            }
        });

        $('#roomid').keypress(function (e) {
            if (e.which === 13) {
                $("#btn-join").click();
            }
        });

        //room view
        $("#btn-next").click(room.next);
        $("#btn-refresh").click(room.ajaxRefresh);
        $("#btn-auto-refresh").click(room.changeAutoRefresh);
        $("#btn-delete").click(room.delete);

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
    updateRoomView: function(id, text, btnNext, showAnswers){
        $("#room-name").text("Room " + id);
        $("#room-text").html(text);

        if(btnNext)
            $('#btn-next').text(btnNext);
        else
            $('#btn-next').hide();

        if(showAnswers)
            $("#answers").show();
        else
            $("#answers").hide();
    },
    updateAnswer: function(ans, plain, text){
        var divAns = $('#answer-' + ans);
        divAns.attr("class", "btn btn-" +(plain?'':'outline-') + mapping.letterToColor(ans) + " btn-block btn-lg");
        if(text)
            divAns.html(ans + " : " + text);
    },
    setAutoRefresh: function(autoRefresh){
        if (autoRefresh) {
            $('#btn-auto-refresh').html("<i class=\"far fa-check-square\"></i> Auto-Refresh");
            $('#btn-refresh').hide();
        } else {
            $('#btn-auto-refresh').html("<i class=\"far fa-square\"></i> Auto-Refresh");
            $('#btn-refresh').show();
        }
    }
};
