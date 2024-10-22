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

    public PhotoService() {
    }

    public void savePhoto(InputStream stream) throws IOException {
        File photo = new File(NameTool.createNameForPhoto());
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(photo))) {
            stream.transferTo(outputStream);
        }
    }
}
