package discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import util.*;
import util.tasks.NotificationManagerTask;

import java.io.*;

import static util.Message.*;
import static util.FileWorker.*;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        String senderId = event.getUser().getId();
        Userconfig senderConfig = getUserConfig(senderId);

        boolean emph = senderConfig == null || senderConfig.isPrivate();

        log(event);

        switch (command) {
            case "kusss" -> {
                OptionMapping urlOption = event.getOption("url");

                assert urlOption != null;
                String kusssLink = urlOption.getAsString();
                // If the user types "get" as the url they get instructions on how to get the link.
                if(kusssLink.equals("get")) {
                    event.reply(SUBSCRIBE_HELP.msg()).setEphemeral(true).queue();
                    return;
                }

                boolean subscribed = kusss(senderId, kusssLink);
                if(subscribed) {
                    event.reply(SUBSCRIBE_SUCCESS.msg()).setEphemeral(true).queue();
                } else {
                    event.reply(SUBSCRIBE_FAIL.msg()).setEphemeral(true).queue();
                }
            }
            case "unkusss" -> {
                boolean unsubscribed = unkusss(senderId);
                if(unsubscribed) {
                    event.reply(UNSUBSCRIBE_SUCCESS.msg()).setEphemeral(true).queue();
                } else {
                    event.reply(UNSUBSCRIBE_NO_DATA.msg()).setEphemeral(true).queue();
                }
            }
            case "update" -> {
                if(senderConfig == null) {
                    event.reply(NOT_SUBSCRIBED_YET.msg()).setEphemeral(true).queue();
                    return;
                }

                boolean success = kusss(senderId, senderConfig.getKusssLink());
                if (success) {
                    event.reply(UPDATE_SUCCESS.msg()).setEphemeral(true).queue();
                } else {
                    event.reply(UPDATE_FAIL.msg()).setEphemeral(true).queue();
                }
            }
            case "schedule" -> {
                // Getting required data.
                OptionMapping rangeOption = event.getOption("range");
                OptionMapping whenOption = event.getOption("when");
                OptionMapping dateOption = event.getOption("date");
                OptionMapping userOption = event.getOption("user");

                User wanted;
                if (userOption == null) {
                    wanted = event.getUser();
                } else {
                    wanted = userOption.getAsUser();
                }
                String wantedId = wanted.getId();
                String wantedMention = wanted.getAsMention();
                String wantedName = wanted.getName();

                boolean goOn = guard(event, senderId, wantedId, senderConfig, getUserConfig(wantedId), wantedMention);
                if(!goOn) return;

                event.reply(BUILDING.msg(wantedMention)).setEphemeral(emph)
                    .flatMap(v -> {
                        boolean rangeDay;
                        rangeDay = rangeOption == null || rangeOption.getAsString().equals("day");

                        Timestamp timestamp = new Timestamp();

                        if (whenOption == null) {
                            if (dateOption != null) {
                                try {
                                    timestamp = Timestamp.fromString(dateOption.getAsString());
                                } catch (IllegalArgumentException e) {
                                    return event.getHook().editOriginal(e.getMessage());
                                }
                            }
                        } else {
                            // Option "when" overwrites "range", that's fine.
                            switch (whenOption.getAsString()) {
                                case "tomorrow" -> timestamp = timestamp.addDays(1);
                                case "yesterday" -> timestamp = timestamp.addDays(-1);
                                case "this week" -> rangeDay = false;
                                case "next week" -> {
                                    timestamp = timestamp.addWeeks(1);
                                    rangeDay = false;
                                }
                                case "last week" -> {
                                    timestamp = timestamp.addWeeks(-1);
                                    rangeDay = false;
                                }
                            }
                        }

                        File schedule = ScheduleDrawer.drawSchedule(wantedId, timestamp, rangeDay);

                        return event.getHook().editOriginal(new MessageBuilder().setEmbeds(
                            new EmbedBuilder()
                                .setImage("attachment://" + wantedId + png)
                                .setTitle(SCHEDULE_TITLE.msg(wantedName, (
                                        rangeDay ? timestamp.toDateString() :
                                        "the week of " + timestamp.getThisMonday().toDateString()))
                                )
                                .setColor(CoolColors.NEON_PINK)
                                .build()
                            ).build()
                        ).addFile(schedule);
                        }
                    )
                    .queue();
            }
            case "nextcourse" -> {
                OptionMapping userOption = event.getOption("user");

                User wanted;
                if (userOption == null) {
                    wanted = event.getUser();
                } else {
                    wanted = userOption.getAsUser();
                }
                String wantedId = wanted.getId();
                String wantedMention = wanted.getAsMention();
                String wantedName = wanted.getName();

                boolean goOn = guard(event, senderId, wantedId, senderConfig, getUserConfig(wantedId), wantedMention);
                if(!goOn) return;
                // Here wanted and sender are public or the same person
                // so the following assert is unnecessary but it removes the Warning in the EmbedBuilder.
                assert senderConfig != null;

                event.reply(SEARCHING.msg(wantedMention)).setEphemeral(emph)
                    .flatMap(v -> {
                        Appointment appointment = getNextCourse(wantedId);
                        String title;
                        String message;

                        if(appointment != null) {
                            title = NEXT_COURSE_TITLE.msg(wantedId.equals(senderId) ? "Your" : wantedName+"'s");
                            message = String.format("%s %s\nwith %s\n%s-%s\n@%s",
                                    appointment.getSubject().getType(),
                                    appointment.getSubject().getTitle(),
                                    appointment.getSubject().getLecturer(),
                                    appointment.getStart().toString(),
                                    appointment.getEnd().toHourString(),
                                    appointment.getLocation());
                        } else {
                            title = "No course found.";
                            message = "";
                        }

                        return event.getHook().editOriginal(new MessageBuilder().setEmbeds(
                            new EmbedBuilder()
                                .setTitle(title)
                                .setDescription(message)
                                .setColor(CoolColors.NEON_PINK)
                                .build()
                            ).build()
                        );
                    }
                )
                .queue();
            }
            case "notify" -> {
                OptionMapping optionOption = event.getOption("option");
                OptionMapping minutesOption = event.getOption("minutes");

                // Guard.
                if(senderConfig == null) {
                    event.reply(NOT_SUBSCRIBED_YET.msg()).setEphemeral(true).queue();
                    return;
                }

                boolean notify = optionOption == null || optionOption.getAsBoolean();
                int minutes;
                if(notify) {
                    if(minutesOption == null) {
                        if(senderConfig.getNotifyMinutes() == -1) {
                            minutes = 10;
                        } else {
                            minutes = senderConfig.getNotifyMinutes();
                        }
                    } else {
                        minutes = (int) minutesOption.getAsLong();
                    }
                } else {
                    minutes = -1;
                }

                updateUserConfig(senderConfig.setNotifyMinutes(minutes));

                if(notify) {
                    NotificationManagerTask.updateNotifications(senderId, minutes);
                    event.reply(NOTIFICATION_ON.msg(minutes)).setEphemeral(true).queue();
                } else {
                    TimetableBot.notificationTimers.get(senderId).cancel();
                    TimetableBot.notificationTimers.remove(senderId);
                    event.reply(NOTIFICATION_OFF.msg()).setEphemeral(true).queue();
                }
            }
            case "setprivate" -> {
                // Guard.
                if (senderConfig == null) {
                    event.reply(NOT_SUBSCRIBED_YET.msg()).setEphemeral(true).queue();
                    return;
                }

                if (!senderConfig.isPrivate()) {
                    updateUserConfig(senderConfig.setPrivate(true));
                }
                event.reply(SET_PRIVATE.msg()).setEphemeral(true).queue();
            }
            case "setpublic" -> {
                // Guard.
                if (senderConfig == null) {
                    event.reply(NOT_SUBSCRIBED_YET.msg()).setEphemeral(true).queue();
                    return;
                }

                if (senderConfig.isPrivate()) {
                    updateUserConfig(senderConfig.setPrivate(false));
                }
                event.reply(SET_PUBLIC.msg()).setEphemeral(true).queue();
            }
        }
    }

    private boolean guard(SlashCommandInteractionEvent event, String senderId, String wantedId, Userconfig senderConfig, Userconfig wantedConfig, String wantedMention) {
        // Guard 1 user not subscribed.
        if(senderConfig == null) {
            if(senderId.equals(wantedId)) {
                event.reply(NOT_SUBSCRIBED_YET.msg()).setEphemeral(true).queue();
            } else {
                event.reply(CANT_ACCESS_ALIEN_NO_SUB.msg()).setEphemeral(true).queue();
            }
             return false;
        }

        // Guard 2 user private, access alien.
        if(!senderId.equals(wantedId) && senderConfig.isPrivate()) {
            event.reply(CANT_ACCESS_ALIEN_YOU_PRIVATE.msg()).setEphemeral(true).queue();
            return false;
        }

        // Guard 3 alien is not subscribed.
        // Just triggered when you have submitted because of Guard 1.
        if(wantedConfig == null) {
            event.reply(ALIEN_NOT_SUBSCRIBED.msg(wantedMention)).setEphemeral(true).queue();
            return false;
        }

        // Guard 4 alien is private.
        if(!senderId.equals(wantedId) && wantedConfig.isPrivate()) {
            event.reply(ALIEN_PRIVATE.msg(wantedMention)).setEphemeral(true).queue();
            return false;
        }
        return true;
    }
}
