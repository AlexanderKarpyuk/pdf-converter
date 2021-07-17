package ru.lanit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    public static Map<String, String> getPdfFiles() {
        File directoryPath = new File(getPath());
        Map<String, String> result= new HashMap<>();
        for (File file : Objects.requireNonNull(directoryPath.listFiles())) {
            if (file.getAbsolutePath().endsWith(".pdf")) {
                result.put(file.getName().substring(0, file.getName().length() - 4), file.getAbsolutePath());
            }
        }
        return result;
    }

    public static String getHtml(String absolutePath) {
        String html = null;
        int count = 0;

        while (count < 5) {
            try {
                html = IOUtils.toString(new InputStreamReader(new FileInputStream(absolutePath),
                        StandardCharsets.UTF_8));
                count = 5;
            } catch (IOException e) {
                try {
                    count += 1;
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return html;
    }

    public static String getPath() {
        String path = null;
        try {
            path = new File(Converter.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getPath();
            path = path.substring(0, path.lastIndexOf("\\"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return path;
    }

    public static void deleteHtmlFiles(Map<String, String> htmlFiles) {
        LOGGER.info("Удаление временных HTML файлов");
        for (Map.Entry<String, String> file: htmlFiles.entrySet()) {
            LOGGER.info("Удаление файла '{}'", file.getKey());
            try {
                Files.delete(Paths.get(file.getValue()));
                LOGGER.warn("Файл '{}' успешно удалён", file.getKey());
            } catch (NoSuchFileException x) {
                LOGGER.warn("В папке '{}' не найден файл '{}.html'", getPath(), file.getKey());
            }  catch (IOException x) {
                LOGGER.error("Произошла неизвестная ошибка при удалении файла '{}'", file.getKey());
            }
        }
    }
}
