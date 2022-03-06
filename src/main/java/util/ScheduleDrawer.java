package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static util.FileWorker.images;
import static util.FileWorker.png;

/**
 * Has just one usable method: drawSchedule which returns a BufferedImage of the schedule ordered
 */
public class ScheduleDrawer {

    private final static Color LINES = new Color(0x738ADB);
    private final static Color TEXT = new Color(0x100D0D);

    private final static float TEXTSIZE_HEADER = 45f, TEXTSIZE_TITLE = 16f, TEXTSIZE_TEXT = 14f;

    private final static int PIXELS_PER_HOUR = 60;
    private final static int DISPLAYED_HOURS = 12;
    private final static int CALENDAR_HEIGHT = PIXELS_PER_HOUR * DISPLAYED_HOURS;
    private final static int CALENDAR_WIDTH = 350;
    private final static int HEADER_HEIGHT = 56;
    private final static int LINE_STRENGTH = 4;
    private static final int SPACE_TIME = 46;
    private static final int RADIUS = 20;

    private final static int HEIGHT = HEADER_HEIGHT + LINE_STRENGTH + CALENDAR_HEIGHT;
    private static int WIDTH;

    private static final int CALENDAR_X = SPACE_TIME + LINE_STRENGTH;
    private static final int CALENDAR_Y = HEADER_HEIGHT + LINE_STRENGTH;

    private static Font headerFont = null, textFont = null, titleFont = null;

    static {
        // Set Font.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        File fontFile = new File(FileWorker.root + File.separator + "font.ttf");

        try {
            ge.registerFont(headerFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(TEXTSIZE_HEADER));
            ge.registerFont(titleFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(TEXTSIZE_TITLE));
            ge.registerFont(textFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(TEXTSIZE_TEXT));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public static File drawSchedule(String id, Timestamp when, boolean rangeDay) {
        List<Appointment> appointments = FileWorker.getSchedule(id, when, rangeDay);
        boolean hasSaturday = !appointments.isEmpty() && appointments.get(appointments.size() - 1).getStart().getDayOfWeekInt() == 5;
        int nDays = rangeDay ? 1 : hasSaturday ? 6 : 5;
        WIDTH = SPACE_TIME + nDays * (CALENDAR_WIDTH + LINE_STRENGTH);

        BufferedImage image = drawCleanSheet(rangeDay ? when.getDayOfWeekInt() : 0, nDays);

        Graphics2D g = (Graphics2D) image.getGraphics();

        // Draw date in top left corner if it is a day-schedule.
        if (rangeDay) {
            g.setColor(LINES);
            drawCenteredString(g, String.format("%02d.%02d", when.getDay(), when.getMonth()),
                    new Rectangle(0, HEADER_HEIGHT / 4, SPACE_TIME, HEADER_HEIGHT / 4),
                    textFont
            );
            drawCenteredString(g, String.format("%04d", when.getYear()),
                    new Rectangle(0, HEADER_HEIGHT / 2, SPACE_TIME, HEADER_HEIGHT / 4),
                    textFont
            );
        }

        // Draw all appointments.
        for (Appointment appointment : appointments) {
            int dayOfWeek = rangeDay ? 0 : appointment.getStart().getDayOfWeekInt();
            int indexInDay = 0;
            int overlaps = 0;
            for (Appointment a : appointments) {
                if (!appointment.equals(a) && appointment.overlaps(a)) {
                    overlaps++;
                    if (appointments.indexOf(a) < appointments.indexOf(appointment)) {
                        indexInDay++;
                    }
                }
                if(a.getStart().compareTo(a.getEnd()) > 0) break;
            }

            int courseStart = appointment.getStart().getHour() * 60 + appointment.getStart().getMinute();
            int courseLength = appointment.getEnd().getHour() * 60 + appointment.getEnd().getMinute() - courseStart;

            // Set start to 8:30 a.m. to be 0.
            courseStart -= (8 * 60 + 30);

            g.setColor(CoolColors.getRandomPastel());

            // Variables to minimize time because I don't want to calculate everything 100 times.
            int x = CALENDAR_X + dayOfWeek * (CALENDAR_WIDTH + LINE_STRENGTH) + indexInDay * (CALENDAR_WIDTH / (overlaps + 1));
            int y = CALENDAR_Y + courseStart;
            int width = CALENDAR_WIDTH / (overlaps + 1);


            g.fillRoundRect(x, y, CALENDAR_WIDTH / (overlaps + 1), courseLength, RADIUS, RADIUS);
            g.setColor(TEXT);
            int rectHeight = courseLength / 3;
            Rectangle rect = new Rectangle(x, y ,width,rectHeight);
            for (int i = 0; i < 3; i++) {
                drawCenteredString(g, getCourseString(i, appointment),rect, i==0 ? titleFont : textFont);
                rect.y += rectHeight;
            }
        }
        File schedule = new File(images + id + png);
        try {
            ImageIO.write(image, "png", schedule);
        } catch (IOException ignore) {}
        return schedule;
    }

    private static BufferedImage drawCleanSheet(int startDay, int nDays) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(LINES);

        // Make background transparent.
        g.setComposite(AlphaComposite.SrcOver.derive(0f));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setComposite(AlphaComposite.SrcOver.derive(1f));

        // Header line.
        g.fillRect(0, HEADER_HEIGHT, WIDTH, LINE_STRENGTH);
        // Hour lines.
        for (int i = 0; i < 12; i++) {
            int y = CALENDAR_Y + PIXELS_PER_HOUR / 2  + (i * PIXELS_PER_HOUR);
            g.fillRect(0, y - 1, WIDTH, LINE_STRENGTH / 2);
            drawCenteredString(g, String.format("%02d:00", i + 9),
                    new Rectangle(5, y + 1, SPACE_TIME / 2, 20), textFont);
        }
        // Days with horizontal line and header.
        for (int i = 0, j = startDay; i < nDays; i++, j = (j + 1) % 7) {
            int x = SPACE_TIME + i * (CALENDAR_WIDTH + LINE_STRENGTH);
            g.fillRect(x, 0, LINE_STRENGTH, HEIGHT);
            drawCenteredString(g, getDayString(j), new Rectangle(x, 0, CALENDAR_WIDTH, HEADER_HEIGHT), headerFont);
        }
        return image;
    }

    // Method stolen from https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java.
    private static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics.
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text.
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen).
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font.
        g.setFont(font);
        // Draw the String.
        g.drawString(text, x, y);
    }

    private static String getDayString(int day) {
        return switch (day) {
            case 0 -> "Monday";
            case 1 -> "Tuesday";
            case 2 -> "Wednesday";
            case 3 -> "Thursday";
            case 4 -> "Friday";
            case 5 -> "Saturday";
            case 6 -> "Sunday";
            default -> "LOL";
        };
    }

    // Helper, just to make writing in the boxes easier and prettier.
    private static String getCourseString(int val, Appointment appointment) {
        return switch (val) {
            case 0 -> appointment.getSubject().getTitle();
            case 1-> appointment.getSubject().getLecturer();
            case 2-> appointment.getLocation();
            default -> "Something went wrong.";
        };
    }
}
