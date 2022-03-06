package util.tasks;

import discord.TimetableBot;
import net.dv8tion.jda.api.entities.User;
import util.FileWorker;
import util.Timestamp;
import java.io.File;
import java.util.*;


public class NotificationManagerTask extends TimerTask {
    // For the scheduling, if true I am taking one more day, so notifications wont be doubled but every course will be covered.
    private final User admin;
    public NotificationManagerTask(User admin) {
        this.admin = admin;
    }
    @Override
    public void run() {
        try {
            Scanner sc = new Scanner(new File(FileWorker.userConfig));
            String line;
            while (sc.hasNextLine()) {
                // Get line and parse the current users settings.
                line = sc.nextLine();
                String[] temp = line.split("->");
                String id = temp[0];
                int minutesEarlier = Integer.parseInt(temp[2]);
                if(minutesEarlier != -1) {
                    updateNotifications(id, minutesEarlier);
                }
            }
            sc.close();
        } catch (Exception e) {
            admin.openPrivateChannel().queue(channel ->
                channel
                    .sendMessage("Exception raised in TaskCreationTask: " + e.getMessage())
                    .queue()
            );
        }
    }

    public static void updateNotifications(String id, int minutesEarlier)  {
        Timestamp start = new Timestamp();
        Timestamp end = start.addDays(7);

        // Get user to notify.
        User whom = TimetableBot.jda.retrieveUserById(id).complete();

        if(TimetableBot.notificationTimers.get(id) != null) {
            TimetableBot.notificationTimers.get(id).cancel();
        }
        TimetableBot.notificationTimers.put(id, new Timer());

        FileWorker
            .getAppointments(id)
            .stream()
            .filter(it -> it.getStart().compareTo(start) >= 0)
            .filter(it -> it.getStart().compareTo(end) < 0)
            .forEach(
                it ->
                    TimetableBot.notificationTimers.get(id).schedule(
                        new NotificationTask(whom, it),
                        it.getStart().addMinutes(-minutesEarlier).toCalendar().getTime()
                    )
            );
    }
}
