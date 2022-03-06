package util;

import discord.TimetableBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FileWorker {
    // File endings.
    public final static String png = ".png";
    public final static String cfg = ".cfg";
    public final static String temp = ".temp";
    public final static String ics = ".ics";
    public final static String json = ".json";

    public final static char sep = File.separatorChar;

    // Folders.
    public final static String root = "src/main/resources" + sep;
    public final static String timetables = root + "timetables" + sep;
    public final static String timetablesOld = root + "timetables_old" + sep;
    public final static String logs = root + "logs" + sep;
    public final static String images = root + "schedule_images" + sep;

    // Configuration file and temp file.
    public final static String userConfig = root + "userconfig" + cfg;
    public final static String userConfigTemp = root + "userconfig" + temp;

    public static List<Appointment> getSchedule(String id, Timestamp when, boolean rangeDay) {
        Timestamp start, end;
        if(rangeDay) {
            start = new Timestamp(when.getYear(), when.getMonth(), when.getDay(),0,0,0);
            end = start.addDays(1).addSeconds(-1);
        } else {
            start = when.getThisMonday();
            end = start.addWeeks(1).addSeconds(-1);
        }

        List<Appointment> appointments = getAppointments(id);
        appointments = appointments
                .stream()
                .filter(it -> it.getEnd().compareTo(start) > 0)
                .filter(it -> it.getStart().compareTo(end) < 0)
                .sorted()
                .collect(Collectors.toList());

        return appointments;
    }

    public static Appointment getNextCourse(String id) {
        Timestamp ts = new Timestamp();

        List<Appointment> appointments = getAppointments(id)
                .stream()
                .dropWhile(it -> it.getStart().compareTo(ts) < 0)
                .limit(1)
                .collect(Collectors.toList());

        if (appointments.size() == 1) {
            return appointments.get(0);
        } else {
            return null;
        }
    }

    public static List<Appointment> getAppointments(String id) {
        List<Appointment> appointments = new ArrayList<>();
        try {
            Scanner s = new Scanner(
                    new File((id.equals("all_appointments") ? root : timetables) + id + json));
            StringBuilder json = new StringBuilder();
            while (s.hasNextLine()) json.append(s.nextLine());
            JSONArray arr = new JSONArray(new JSONTokener(json.toString()));
            for (int i = 0; i < arr.length(); i++)
                appointments.add(Appointment.fromJSONObject((JSONObject) arr.get(i)));
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Collections.sort(appointments);
        return appointments;
    }

    public static void log(SlashCommandInteractionEvent event){
        Timestamp now = new Timestamp();
        String sender = event.getUser().getId();
        String command = event.getName();
        File temp = new File(String.format("%s%d%02d%02d.log", logs, now.getYear(), now.getMonth(), now.getDay()));
        try {
            FileWriter fw = new FileWriter(temp, true);
            fw.append("#").append(sender).append(" @").append(now.compact()).append("-> /").append(command).append("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean unkusss(String senderId) {
        boolean existed = deleteUserConfig(senderId);

        try {
            Files.deleteIfExists(Paths.get(timetables + senderId + json));
            Files.deleteIfExists(Paths.get(timetablesOld + senderId + ics));
            Files.deleteIfExists(Paths.get(images + senderId + png));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(TimetableBot.notificationTimers.get(senderId) != null) {
            TimetableBot.notificationTimers.get(senderId).cancel();
        }
        TimetableBot.notificationTimers.remove(senderId);
        return existed;
    }

    public static boolean kusss(String senderId, String kusssLink){
        if(!kusssLink.startsWith("https://www.kusss.jku.at/kusss/published-calendar.action?token=")) {
            return false;
        }
        try{
            URL url = new URL(kusssLink);
            InputStream in = url.openStream();

            Files.copy(in, Paths.get(FileWorker.timetablesOld + senderId + ics), StandardCopyOption.REPLACE_EXISTING);
            ICSParser.parseToFile(
                    timetablesOld + senderId + ics,
                    timetables + senderId + json
            );
            updateUserConfig(new Userconfig(senderId, kusssLink, -1, false));
            in.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static Userconfig getUserConfig(String userID) {
        Userconfig user = null;
        try {
            Scanner sc = new Scanner(new File(userConfig));
            String line;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if(line.startsWith(userID)) {
                    user = Userconfig.fromLine(line);
                    break;
                }
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }
    public static void updateUserConfig(Userconfig user) {
        try {
            Scanner sc = new Scanner(new File(userConfig));
            FileWriter fw = new FileWriter(userConfigTemp);
            String line;
            boolean written = false;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if(line.startsWith(user.getId())) {
                    fw.write(user.toString());
                    written = true;
                }else {
                    fw.write(line + "\n");
                }
            }
            if(!written){
                fw.write(user.toString());
            }
            fw.close();
            sc.close();
            Files.move(Paths.get(userConfigTemp), Paths.get(userConfig), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean deleteUserConfig(String userID) {
        boolean existed = false;
        try{
            Scanner sc = new Scanner(new File(userConfig));
            FileWriter fw = new FileWriter(userConfigTemp);
            String line;

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if(!line.startsWith(userID)) {
                    fw.write(line + "\n");
                } else {
                    existed = true;
                }
            }
            fw.close();
            sc.close();
            Files.move(Paths.get(userConfigTemp), Paths.get(userConfig), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return existed;
    }
}
