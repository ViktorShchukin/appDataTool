package com.anorisno.appDataTool.service.tools;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class NameTool {

    private final static DateTimeFormatter DATE_FORMAT_FOR_PHOTO = DateTimeFormatter.ISO_INSTANT;

    public static String createNameForPhoto() {
        return createNameForPhoto(ZonedDateTime.now());
    }

    public static String createNameForPhoto(ZonedDateTime time) {
        return time.format(DATE_FORMAT_FOR_PHOTO) + ".png";
    }
}
