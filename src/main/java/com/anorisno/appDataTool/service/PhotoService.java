package com.anorisno.appDataTool.service;

import com.anorisno.appDataTool.service.tools.NameTool;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.time.ZonedDateTime;

@Service
public class PhotoService {

    private final static String DEFAULT_PATH_FOR_PHOTO = "./pythonScripts/photoFromApp/";

    public PhotoService() {
    }

    public void savePhoto(InputStream stream) throws IOException {
        savePhoto(stream, DEFAULT_PATH_FOR_PHOTO);
    }

    public void savePhoto(InputStream stream, String path) throws IOException {
        File photo = new File(path + NameTool.createNameForPhoto());
        photo.getParentFile().mkdirs();
        photo.createNewFile();
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(photo))) {
            stream.transferTo(outputStream);
        }
    }
}
