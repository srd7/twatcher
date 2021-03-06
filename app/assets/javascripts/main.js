(function ($) {
  var doms = $(".account-form");
  var userIds = doms.map(function (i, dom){ return parseInt($(dom).attr("data-user-id"), 10); }).toArray();
  userIds.forEach(function (userId) {
    $.ajax(router.twatcher.controllers.SettingController.checkAccount(userId))
      .done(function (data) {
        // update *icon_url, *screen_name
        var dom = $(`.account-form[data-user-id=${data.userId}]`);
        $(dom).find(".account-img").attr("src", data.imageUrl);
        $(dom).find(".account-screen-name").text(`@${data.screenName}`);
        $(dom).find(".auth-check").hide();
        $(dom).find(".auth-ok").show();
      })
      .fail(function (err) {
        var dom = $(`.account-form[data-user-id=${err.responseJSON.userId}]`);
        $(dom).find(".auth-check").hide();
        $(dom).find(".auth-failed").show();
      });
  });
})(jQuery);
