package util;

public class Userconfig {
    private final String id;
    private final String kusssLink;
    private int notifyMinutes;
    private boolean visibility;

    public Userconfig(String id, String kusssLink, int notifyMinutes, boolean visibility) {
        this.id = id;
        this.kusssLink = kusssLink;
        this.notifyMinutes = notifyMinutes;
        this.visibility = visibility;
    }

    public String getId() {
        return id;
    }

    public String getKusssLink() {
        return kusssLink;
    }

    public int getNotifyMinutes() {
        return notifyMinutes;
    }

    public boolean isPrivate() {
        return !visibility;
    }


    public Userconfig setNotifyMinutes(int notifyMinutes) {
        this.notifyMinutes = notifyMinutes;
        return this;
    }

    public Userconfig setPrivate(boolean private_) {
        visibility = !private_;
        return this;
    }

    public static Userconfig fromLine(String line) {
        String[] config = line.split("->");
        return new Userconfig(config[0], config[1], Integer.parseInt(config[2]), Boolean.parseBoolean(config[3]));
    }

    @Override
    public String toString() {
        return String.format("%s->%s->%d->"+visibility, id, kusssLink, notifyMinutes);
    }
}
