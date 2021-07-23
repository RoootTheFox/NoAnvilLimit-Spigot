package net.ddns.foxsquad.noanvillimitspigot;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorThing {
    public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    public static final Pattern UNHEX_PATTERN = Pattern.compile("&x((&[0-9A-F]){6})");

    public static String translate (String in) {
        in = ChatColor.translateAlternateColorCodes('&', in);
        Matcher matcher = HEX_PATTERN.matcher(in);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return matcher.appendTail(buffer).toString();
    }

    public static String untranslateHexCodes(String in) {
        Matcher matcher = UNHEX_PATTERN.matcher(in);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, "&#"+matcher.group(1).replace("&", "").toLowerCase());
        }

        return matcher.appendTail(buffer).toString();
    }
}
