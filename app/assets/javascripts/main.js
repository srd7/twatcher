(($) => {
  const doms = $(".account-form");
  const userIds = doms.map((i, dom) => parseInt($(dom).attr("data-user-id"), 10)).toArray();
  userIds.forEach((userId) => {
    $.ajax(router.twatcher.controllers.SettingController.checkAccount(userId))
      .done((data) => {
        // update *icon_url, *screen_name
        const dom = $(`.account-form[data-user-id=${data.userId}]`);
        $(dom).find(".account-img").attr("src", data.imageUrl);
        $(dom).find(".account-screen-name").text(`@${data.screenName}`);
        $(dom).find(".auth-check").hide();
        $(dom).find(".auth-ok").show();
      })
      .fail((err) => {
        const dom = $(`.account-form[data-user-id=${err.responseJSON.userId}]`);
        $(dom).find(".auth-check").hide();
        $(dom).find(".auth-failed").show();
      });
  });
})(jQuery);
