package util;

public enum Message {
    TOKEN_MISSING("You have to provide the token as the first argument!"),
    BOT_START("Bot has been started."),
    ACTIVITY_STATUS("over your Time."),
    SUBSCRIBE_SUCCESS("Successfully subscribed to your calendar. Your link will be handled with care."),
    SUBSCRIBE_HELP("Go to https://www.kusss.jku.at/kusss/ical-multi-form-sz.action, click create and copy your URL."),
    SUBSCRIBE_FAIL("Malformed URL, try to copy the link from https://www.kusss.jku.at/kusss/ical-multi-form-sz.action again."),
    UNSUBSCRIBE_SUCCESS("Successfully unsubscribed from your calendar. Deleted your link and files."),
    UNSUBSCRIBE_NO_DATA("I couldn't find any data about you, I guess we can call it a success then?"),
    NOT_SUBSCRIBED_YET("You are not subscribed to timetables yet. This is possible with \"/kusss\"."),
    UPDATE_SUCCESS("Successfully updated your schedule."),
    UPDATE_FAIL("Link invalid. Have you changed or deleted it in KUSSS?"),
    CANT_ACCESS_ALIEN_NO_SUB("You have not submitted a schedule yet. Thus you can't access others schedules."),
    CANT_ACCESS_ALIEN_YOU_PRIVATE("You can't ask for others schedules when yours is private."),
    ALIEN_NOT_SUBSCRIBED("{} has not subscribed to calendars yet."),
    ALIEN_PRIVATE("{} has a private calendar."),
    SEARCHING("Searching next course of {}."),
    BUILDING("Building schedule of {}."),
    NOTIFICATION_OFF("Successfully updated notification-settings, you will not get any notifications from me any more, until you turn them back on."),
    NOTIFICATION_ON("Successfully updated notification-settings, you will receive your next notification about {} before the course."),
    SET_PRIVATE("Set your visibility to private."),
    SET_PUBLIC("Set your visibility to public."),
    SCHEDULE_TITLE("{}'s Schedule for {}."),
    COURSE_STARTED("Hi! Your next course just started. :)"),
    COURSE_STARTS_IN("Hi! Your next course starts in about {} minutes. :)"),
    NEXT_COURSE_TITLE("Hi! {} next course. :)");

    private final String message;

    Message(String msg) {
        message = msg;
    }

    public final String msg() {
        return message;
    }

    public final String msg(Object... params) {
        String msg = message;
        for (Object param: params) {
            if(param instanceof String) {
                msg = msg.replaceFirst("\\{}", (String) param);
            } else if (param instanceof Integer) {
                msg = msg.replaceFirst("\\{}", Integer.toString((int)param));
            } else if (param instanceof Double) {
                msg = msg.replaceFirst("\\{}", Double.toString((double)param));
            }
        }
        return msg;
    }
}
