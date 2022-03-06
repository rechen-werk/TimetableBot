package util.tasks;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import util.Appointment;
import util.CoolColors;
import util.FileWorker;

import java.util.TimerTask;

import static util.Message.*;

public class NotificationTask extends TimerTask {
    User user;
    Appointment appointment;

    NotificationTask(User user, Appointment appointment) {
        this.user = user;
        this.appointment = appointment;
    }

    @Override
    public void run() {
        int minutes = FileWorker.getUserConfig(user.getId()).getNotifyMinutes();
        if(minutes != -1) {
            user.openPrivateChannel().queue(
                privateChannel -> {
                    String message = String.format("%s %s\nwith %s\n%s-%s\n@%s",
                        appointment.getSubject().getType(),
                        appointment.getSubject().getTitle(),
                        appointment.getSubject().getLecturer(),
                        appointment.getStart().toString(),
                        appointment.getEnd().toHourString(),
                        appointment.getLocation()
                    );
                    privateChannel.sendMessageEmbeds(
                        new EmbedBuilder()
                        .setTitle(minutes == 0 ? COURSE_STARTED.msg() : COURSE_STARTS_IN.msg(minutes))
                        .setDescription(message)
                        .setColor(CoolColors.NEON_PINK)
                        .build()
                    ).queue();
                }
            );
        }
    }
}
