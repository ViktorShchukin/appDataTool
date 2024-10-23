package com.anorisno.appDataTool.unit;

import com.anorisno.appDataTool.service.tools.NameTool;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class NameToolTest {

    @Test
    void testCreateName() {
        String expected = "2011-12-03T10:15:30Z";
        ZonedDateTime time = ZonedDateTime.parse(expected);
        String name = NameTool.createNameForPhoto(time);
        String actual = name.substring(0, 20);
        assertEquals(expected, actual);
    }
}
