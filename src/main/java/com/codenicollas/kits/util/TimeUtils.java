package com.codenicollas.kits.util;

import java.util.concurrent.TimeUnit;

import com.codenicollas.kits.model.KitCooldown;

public class TimeUtils {

    public static KitCooldown convertToCooldown(long milliseconds) {
        int days = (int) TimeUnit.MILLISECONDS.toDays(milliseconds);
        int hours = (int) TimeUnit.MILLISECONDS.toHours(milliseconds) % 24;
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        return new KitCooldown(days, hours, minutes);
    }

    public static String format(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(millis);

        StringBuilder formattedTime = new StringBuilder();

        if (days > 0) {
            formattedTime.append(days).append("d ");
        }
        if (hours > 0) {
            formattedTime.append(hours).append("h ");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append("m ");
        }
        if (seconds > 0 || formattedTime.length() == 0) {
            formattedTime.append(seconds).append("s");
        }

        return formattedTime.toString().trim();
    }
}