package discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import util.tasks.NotificationManagerTask;
import util.tasks.UpdateSchedulesTask;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static util.Message.*;

public class TimetableBot {


    public final static Map<String, Timer> notificationTimers = new HashMap<>();
    public static JDA jda;

    public static void main(String[] args) throws LoginException {

        if (args.length < 1) {
            System.out.println(TOKEN_MISSING.msg());
            System.exit(1);
        }
        String TOKEN = args[0];

        jda = JDABuilder.createDefault(TOKEN)
            .setActivity(Activity.watching(ACTIVITY_STATUS.msg()))
            .addEventListeners(new CommandListener())
            .build();

        // Just add the following line if you add another command, they don't need to be added every time the bot starts.
        // addCommands(jda);

        User admin = null;

        if (args.length >= 2) {
            final String ADMIN_ID = args[1];
            admin = jda.retrieveUserById(ADMIN_ID).complete();
        }

        // Timer for the notifications and updates of the files.
        Timer timer = new Timer();

        Calendar sundayNoon = Calendar.getInstance();
        sundayNoon.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        sundayNoon.set(Calendar.HOUR_OF_DAY, 12);
        sundayNoon.set(Calendar.MINUTE, 0);
        sundayNoon.set(Calendar.SECOND, 0);
        sundayNoon.set(Calendar.MILLISECOND, 0);

        Calendar saturdayMidnight = Calendar.getInstance();
        sundayNoon.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        sundayNoon.set(Calendar.HOUR_OF_DAY, 23);
        sundayNoon.set(Calendar.MINUTE, 59);
        sundayNoon.set(Calendar.SECOND, 59);
        sundayNoon.set(Calendar.MILLISECOND, 0);


        // Task to update the calendar files weekly on sunday noon.
        timer.schedule(new UpdateSchedulesTask(admin), sundayNoon.getTime(), TimeUnit.DAYS.toMillis(7));

        // Weekly task to add the tasks for the upcoming week.
        timer.schedule(new NotificationManagerTask(admin), saturdayMidnight.getTime(), TimeUnit.DAYS.toMillis(7));

        // Notify about start of bot if admin id is given.
        if(admin != null) {
            admin.openPrivateChannel().queue(channel -> channel.sendMessage(BOT_START.msg()).queue());
        }
    }

    @SuppressWarnings("unused")
    private static void addCommands(JDA jda) {
        jda.upsertCommand("unkusss", "Delete my schedule and unsubscribe from the calendar.").queue();
        jda.upsertCommand("update", "Force a update of your schedule.").queue();
        jda.upsertCommand("nextcourse", "Your (or @User's) next course.")
            .addOption(OptionType.USER, "user", "The user from whom you want the next course.")
            .queue();
        jda.upsertCommand("notify", "Opt into notifications about upcoming courses. By default you are not signed up to them.")
            .addOptions(
                new OptionData(OptionType.INTEGER, "minutes", "How many minutes in advance should I inform you about your course? Default is 10 minutes.")
                    .setMaxValue(60*24)
                    .setMinValue(0),
                new OptionData(OptionType.BOOLEAN, "option", "With false you can turn off notifications again.")
            ).queue();
        jda.upsertCommand("setprivate", "Set your schedule to private mode. This also means you can't access other's schedules.").queue();
        jda.upsertCommand("setpublic", "Set your schedule to public mode. This allows you to see others schedules if theirs is also public.").queue();
        jda.upsertCommand("kusss", "Create a calendar subscription in KUSSS and copy the link here. Use \"get\" as url to get your url.")
            .addOption(
                OptionType.STRING,
                "url",
                "The URL you get when creating a subscription. Automatic updates happen every Sunday at noon.",
                true
            ).queue();
        jda.upsertCommand("schedule", "Get the schedule you specify.")
            .addOptions(
                new OptionData(OptionType.STRING, "when", "Common schedule queries. (default=TODAY)")
                    .addChoices(
                        new Command.Choice("today", "today"),
                        new Command.Choice("tomorrow", "tomorrow"),
                        new Command.Choice("yesterday", "yesterday"),
                        new Command.Choice("this week", "this week"),
                        new Command.Choice("next week", "next week"),
                        new Command.Choice("last week", "last week")
                    ),
                new OptionData(OptionType.STRING, "date", "The date in DD.MM.YYYY"),
                new OptionData(OptionType.STRING, "range", "DAY or week")
                    .addChoices(
                        new Command.Choice("day", "day"),
                        new Command.Choice("week", "week")
                    ),
                new OptionData(OptionType.USER, "user", "Get users schedule instead of yours.")
            ).queue();
    }
}
