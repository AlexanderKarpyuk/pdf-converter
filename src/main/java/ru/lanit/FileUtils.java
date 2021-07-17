package ru.lanit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
        try {
            html = IOUtils.toString(new InputStreamReader(new FileInputStream(absolutePath), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("Не удалось считать HTML файл");
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

    public static void deleteTempDirectories(Map<String, String> htmlFiles) {
        LOGGER.info("Удаление временных папок");
        for (Map.Entry<String, String> file: htmlFiles.entrySet()) {
            String dirName = file.getKey() + "_files";
            String dirPath = String.format("%s\\%s", getPath(), dirName);
            LOGGER.info("Удаление временной папки '{}'", dirName);

            File dir = new File(dirPath);
            String[] files = Optional.ofNullable(dir.list()).orElse(new String[0]);
            for(String innerFile: files) {
                File currentFile = new File(dir.getPath(), innerFile);
                if (currentFile.delete()) {
                    LOGGER.info("Файл '{} 'в папке '{}' успешно удалён", innerFile, dirName);
                } else {
                    LOGGER.warn("Не удалось удалить файл '{}' в папке '{}'", innerFile, dirName);
                }
            }
            try {
                Files.delete(Paths.get(dirPath));
                LOGGER.warn("Временная папка '{}' успешно удалена", dirName);
            } catch (NoSuchFileException x) {
                LOGGER.warn("Не удалось найти папку '{}'", dirName);
            }  catch (IOException x) {
                LOGGER.error("Произошла неизвестная ошибка при удалении папки '{}'", dirName);
            }
        }
    }
}
