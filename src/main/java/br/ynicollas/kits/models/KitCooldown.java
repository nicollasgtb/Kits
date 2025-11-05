package br.ynicollas.kits.models;

public class KitCooldown {
    private final int days;
    private final int hours;
    private final int minutes;

    public KitCooldown(int days, int hours, int minutes) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    public long getMilliseconds() {
        return (days * 86_400_000L) + (hours * 3_600_000L) + (minutes * 60_000L);
    }
}