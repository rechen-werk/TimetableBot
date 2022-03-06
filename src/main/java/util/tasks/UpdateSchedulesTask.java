package util.tasks;

import net.dv8tion.jda.api.entities.User;
import util.ICSParser;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.TimerTask;

import static util.FileWorker.*;

public class UpdateSchedulesTask extends TimerTask {
    private final User admin;
    public UpdateSchedulesTask(User admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        if (admin != null) {
            admin.openPrivateChannel().queue(channel -> channel.sendMessage("Updating Calendar Files.").queue());
        } else {
            System.out.println("Updating Calendar Files.");
        }
        try {
            Scanner sc = new Scanner(new File(userConfig));
            String line;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                String [] temp = line.split("->");
                String id = temp[0];
                String link = temp[1];
                InputStream in = new URL(link).openStream();
                Files.copy(in, Paths.get(timetablesOld + id + ics), StandardCopyOption.REPLACE_EXISTING);
                in.close();
                ICSParser.parseToFile(
                    timetablesOld + id + ics,
                    timetables + id + json
                );
            }
            sc.close();
        } catch (Exception e) {

            if (admin != null) {
                admin.openPrivateChannel().queue(channel -> channel.sendMessage("Exception raised in UpdateTask: " + e.getMessage()).queue());
            } else {
                System.out.println("Exception raised in UpdateTask: " + e.getMessage());
            }
        }
    }
}
