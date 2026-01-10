package com.binewsian.util;

import org.springframework.stereotype.Component;

@Component("userAvatar")
public class UserAvatar {

    public String getUserInitials(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "U";
        }

        username = username.trim();
        String[] words = username.split("\\s+");

        if (words.length == 1) {
            return String.valueOf(words[0].charAt(0)).toUpperCase();
        }

        StringBuilder initials = new StringBuilder();
        initials.append(words[0].charAt(0));
        if (words.length > 1) {
            initials.append(words[1].charAt(0));
        }

        return initials.toString().toUpperCase();
    }

    public String getAvatarColor(String username) {
        String[] colors = {
                "#5b21b6", // Dark Purple
                "#be185d", // Dark Pink
                "#1e40af", // Dark Blue
                "#047857", // Dark Green
                "#be123c", // Dark Rose
                "#dc2626", // Dark Red
                "#d97706", // Dark Orange
                "#0891b2", // Dark Cyan
                "#059669", // Dark Emerald
                "#4338ca", // Dark Indigo
                "#7c3aed", // Dark Violet
                "#0284c7", // Dark Sky Blue
                "#15803d", // Dark Forest Green
                "#b91c1c", // Dark Crimson
                "#1e3a8a"  // Dark Navy
        };

        if (username == null || username.trim().isEmpty()) {
            return colors[0];
        }

        int index = Math.abs(username.hashCode()) % colors.length;
        return colors[index];
    }
}
