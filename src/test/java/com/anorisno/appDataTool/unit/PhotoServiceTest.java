package com.anorisno.appDataTool.unit;

import com.anorisno.appDataTool.service.PhotoService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class PhotoServiceTest {

    private final static String RES_PATH = "src/test/resources/";
    private final static String PHOTO = "pngwing.com.png";
    private final static String OUT = "out/";

    private final PhotoService photoService = new PhotoService();

    @Test
    void testSavePhoto() {
        try (InputStream inputStream = new FileInputStream(RES_PATH + PHOTO)) {
            photoService.savePhoto(inputStream, RES_PATH +OUT);
        } catch (IOException e) {
            fail("it should not throw exceptions", e);
        }

    }

    @AfterAll
    static void clearOutDir() throws IOException {
        FileUtils.deleteDirectory(new File(RES_PATH + OUT));
    }
}
