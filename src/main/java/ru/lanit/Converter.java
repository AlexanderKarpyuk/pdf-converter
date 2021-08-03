package ru.lanit;

import com.aspose.pdf.*;
import com.aspose.pdf.Document;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanit.models.Person;
import ru.lanit.models.TableInfo;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);
    private final String FONT_FAMILY = "Times New Roman";

    public void convert() {
        Converter converter = new Converter();
        Map<String, String> htmlFiles = converter.generateHTMLFromPDF();
        try {
            Map<String, Person> persons = converter.getPersonFromHtml(htmlFiles);
            converter.generateDocx(persons);
        } finally {
            FileUtils.deleteHtmlFiles(htmlFiles);
            FileUtils.deleteTempDirectories(htmlFiles);
        }
    }

    /**
     * Конвертирует pdf в html формат
     * @return - ключ - имя файла, value - абсолютный путь до файла
     */
    private Map<String, String> generateHTMLFromPDF() {
        Map<String, String> htmlFiles = new HashMap<>();
        LOGGER.info("Поиск файлов PDF в текущей папке и конвертация их в HTML");
        for (Map.Entry<String, String> filePath: FileUtils.getPdfFiles().entrySet()) {
            Document pdfDocument = new Document(filePath.getValue());
            HtmlSaveOptions htmlOptions = new HtmlSaveOptions(SaveFormat.Html);
            String newAbsolutePath = String.format("%s\\%s.html", FileUtils.getPath(), filePath.getKey());
            pdfDocument.save(newAbsolutePath, htmlOptions);
            htmlFiles.put(filePath.getKey(), newAbsolutePath);
            LOGGER.info("Файл '{}' конвертирован в HTML", filePath.getKey());
        }
        return htmlFiles;
    }

    /**
     * Конвертация HTML фалов в объекты Person
     * @param htmlFiles - Map полученный из метода generateHTMLFromPDF()
     * @return - ключ - имя файла, value - объект Person
     */
    private Map<String, Person> getPersonFromHtml(Map<String, String> htmlFiles) {
        Map<String, Person> persons = new HashMap<>();
        LOGGER.info("Генерация сущностей Person из HTML файлов");
        for (Map.Entry<String, String> file: htmlFiles.entrySet()) {
            LOGGER.info("Создание сущности для файла: " + file.getKey());
            Person person = new Person();
            String html = FileUtils.getHtml(file.getValue());
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            Elements lines = doc.select("span");
            lines.removeIf(s -> s.text().contains("Evaluation Only. Created with Aspose.PDF. Copyright 2002-2019 Aspose Pty Ltd"));
            lines.removeIf(s -> s.text().isEmpty());

            person.setName(lines.get(0).text());
            person.setJobInfo(getJobInfo(lines));
            person.setKeySkills(getKeySkills(lines));
            person.setEducationInfo(getEducationInfo(lines));
            person.setLanguages(getLanguages(lines));
            person.setQualifications(getSubText(lines, "Повышение квалификации и курсы"));
            person.setTests(getSubText(lines, "Тесты"));

            persons.put(file.getKey(), person);
            LOGGER.info(String.format("Для файла '%s' создана сущность Person", file.getKey()));
        }

        return persons;
    }

    /**
     * Генерирует DOCX документы из объектов Person
     * @param persons - Map полученный из метода getPersonFromHtml()
     */
    private void generateDocx(Map<String, Person> persons) {
        LOGGER.info("Генерация DOCX файлов");
        for (Map.Entry<String, Person> person: persons.entrySet()) {
            XWPFDocument document = new XWPFDocument();
            String newLine = "\n";
            LOGGER.info("Генерация документа DOCX из файла '{}'", person.getKey());
            try {
                //Заголовок
                XWPFParagraph paragraphTitle = document.createParagraph();
                paragraphTitle.setSpacingBetween(1.5);
                XWPFRun runTitle = paragraphTitle.createRun();
                runTitle.setText("Резюме");
                runTitle.setFontFamily(FONT_FAMILY);
                runTitle.setFontSize(14);
                paragraphTitle.setAlignment(ParagraphAlignment.CENTER);

                //Инфо
                addLine(document, "Должность в проекте:", true);
                addLine(document, "Имя: ", true);
                addCurrentLine(document, person.getValue().getName(), false);
                addLine(document, "Основные показатели квалификации:", true);
                addLine(document, StringUtils.join(person.getValue().getKeySkills(), ", "), false);
                addLine(document, "Сведения о трудовой деятельности:", true);

                //Таблица пред. работа
                String[] titlesJob = new String[]{"Период", "Место работы / должность", "Характер работ, проекты, в которых участвовал"};
                addTable(document, titlesJob, person.getValue().getJobInfo());

                //Таблица образование
                addLine(document, newLine, true);
                addLine(document, "Образование:", true);
                String[] titlesEducation = new String[]{"Период", "Название учебного заведения", "Присвоенная степень/звание/сертификат"};
                addTable(document, titlesEducation, person.getValue().getEducationInfo());

                //Доп. инфо
                addLine(document, "Знание языков (отлично, хорошо, удовлетворительно, плохо):", true);
                addLine(document, String.format("Русский: %s", person.getValue().getLanguages().getOrDefault("Русский", "")), false);
                addLine(document, String.format("Английский: %s", person.getValue().getLanguages().getOrDefault("Английский", "")), false);

                addLine(document, "Повышение квалификации и курсы:", true);
                for (String qualification: person.getValue().getQualifications()) {
                    addLine(document, qualification, false);
                }

                addLine(document, "Тесты:", true);
                for (String test: person.getValue().getTests()) {
                    addLine(document, test, false);
                }

                FileOutputStream out;

                out = new FileOutputStream(new File(String.format("%s\\%s.docx", FileUtils.getPath(), person.getKey())));
                document.write(out);
                out.close();
                document.close();

                LOGGER.info("DOCX документ для файла '{}' создан", person.getKey());
            } catch (Exception ex) {
                LOGGER.error("Для файла '{}' не удалось создать DOCX документ", person.getKey());
            }
        }
    }

    private List<TableInfo> getJobInfo(Elements lines) {
        List<Integer> jobIndex = new ArrayList<>();
        List<Integer> positionIndex = new ArrayList<>();
        List<Integer> periodIndex = new ArrayList<>();
        List<Integer> lastJobIndex = new ArrayList<>();
        List<String> title = new ArrayList<>();
        List<String> position = new ArrayList<>();
        List<String> period = new ArrayList<>();
        List<String> description = new ArrayList<>();
        List<TableInfo> result = new ArrayList<>();
        String blueFont = getClassValue(lines, "ПРОФЕССИОНАЛЬНЫЙ ОПЫТ", 1);
        String boldFont = getClassValue(lines, "ПРОФЕССИОНАЛЬНЫЙ ОПЫТ", 2);

        if (boldFont == null || blueFont == null) return result;

        for (int i = 0; i < lines.size(); i++) {
            if(lines.get(i).hasClass(blueFont)) {
                jobIndex.add(i);
                positionIndex.add(i + 1);
                periodIndex.add(i + 2);
            }
        }

        for (Integer i: periodIndex) {
            for(int j = i + 1; j < lines.size(); j++) {
                if(lines.get(j).hasClass(blueFont) || lines.get(j).hasClass(boldFont)) {
                    lastJobIndex.add(j - 1);
                    break;
                }
            }
        }

        for (Integer i: jobIndex) {
            title.add(lines.get(i).text());
            position.add(lines.get(i + 1).text());
            period.add(lines.get(i + 2).text().replaceAll("\\(.*\\)", ""));
        }

        for (int i = 0; i < periodIndex.size(); i++) {
            TableInfo tableInfo = new TableInfo();
            StringBuilder sb = new StringBuilder();
            for (int j = periodIndex.get(i) + 1; j <= lastJobIndex.get(i); j++) {
                sb.append(lines.get(j).text()).append("\n");
            }
            tableInfo.setName(title.get(i));
            tableInfo.setPosition(position.get(i));
            tableInfo.setPeriod(period.get(i));
            tableInfo.setDescription(sb.toString());
            result.add(tableInfo);
        }

        return result;
    }

    private List<TableInfo> getEducationInfo(Elements lines) {
        List<TableInfo> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        OptionalInt start = IntStream.range(0, lines.size())
                .filter(i -> lines.get(i).text().contains("ОБРАЗОВАНИЕ"))
                .findFirst();
        OptionalInt end = IntStream.range(0, lines.size())
                .filter(i -> lines.get(i).text().contains("Повышение квалификации и курсы"))
                .findFirst();

        if (start.isPresent() && end.isPresent()) {
            TableInfo tableInfo = new TableInfo();
            for(int i = start.getAsInt() + 2; i < end.getAsInt(); i++) {
                if (lines.get(i).text().matches("^\\d+$")) {
                    tableInfo.setPeriod(lines.get(i).text());
                } else {
                    sb.append(lines.get(i).text()).append("\n");
                    tableInfo.setName(sb.toString());
                }
            }
            result.add(tableInfo);
        }

        return result;
    }

    private Map<String, String> getLanguages(Elements lines) {
        Map<String, String> result = new HashMap<>();
        String boldFont = getClassValue(lines, "Знание языков", 0);
        Integer end = null;
        OptionalInt start = IntStream.range(0, lines.size())
                .filter(i -> lines.get(i).text().contains("Знание языков"))
                .findFirst();

        if(start.isPresent()) {
            for (int i = start.getAsInt() + 1; i < lines.size(); i++) {
                if (lines.get(i).hasClass(boldFont)) {
                    end = i;
                    break;
                }
            }
        }

        if (start.isPresent() && end != null) {
            for(int i = start.getAsInt() + 1; i < end; i++) {
                if(lines.get(i).text().contains("—")) {
                    String[] lang = lines.get(i).text().split("—");
                    result.put(lang[0].trim(), String.join("", Arrays.asList(lang).subList(1, lang.length)));
                }
            }
        }

        return result;
    }

    private List<String> getSubText(Elements lines, String text) {
        List<String> result = new ArrayList<>();
        String boldFont = getClassValue(lines, text, 0);
        Integer end = null;
        OptionalInt start = IntStream.range(0, lines.size())
                .filter(i -> lines.get(i).text().contains(text))
                .findFirst();

        if(start.isPresent()) {
            for (int i = start.getAsInt() + 1; i < lines.size(); i++) {
                if (lines.get(i).hasClass(boldFont)) {
                    end = i;
                    break;
                }
            }
        }

        if (start.isPresent() && end != null) {
            for(int i = start.getAsInt() + 1; i < end; i++) {
                result.add(lines.get(i).text());
            }
        }

        return result;
    }

    private List<String> getKeySkills(Elements lines) {
        String keySkillClass = getClassValue(lines, "Ключевые навыки", 1);

        return lines.stream().filter(e -> e.hasClass(keySkillClass)).map(Element::text).collect(Collectors.toList());
    }

    private String getClassValue(Elements lines, String text, int countAfter) {
        String value = null;
        for (int i = 0; i < lines.size(); i++) {
            if(lines.get(i).text().equals(text)) {
                Optional<String> title = lines.get(i + countAfter).classNames().stream().findFirst();
                if(title.isPresent()) {
                    value = title.get();
                }
            }
        }
        return value;
    }

    private void addLine(XWPFDocument document, String text, boolean isBold) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBetween(1.5);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(11);
        run.setBold(isBold);
        run.setFontFamily(FONT_FAMILY);
    }

    private void addCurrentLine(XWPFDocument document, String text, boolean isBold) {
        XWPFParagraph paragraph = document.getLastParagraph();
        paragraph.setSpacingBetween(1.5);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(11);
        run.setBold(isBold);
        run.setFontFamily(FONT_FAMILY);
    }

    private void addTable(XWPFDocument document, String[] titles, List<TableInfo> tableInfo) {
        XWPFTable table = document.createTable();

        CTTblLayoutType type = table.getCTTbl().getTblPr().addNewTblLayout();
        type.setType(STTblLayoutType.FIXED);

        XWPFTableRow row = table.getRow(0);
        XWPFRun run = row.getCell(0).getParagraphArray(0).createRun();
        run.setBold(true);
        run.setFontFamily(FONT_FAMILY);
        run.setFontSize(11);
        run.setText(titles[0]);

        XWPFRun run2 = row.addNewTableCell().addParagraph().createRun();
        run2.setBold(true);
        run2.setFontFamily(FONT_FAMILY);
        run2.setFontSize(11);
        run2.setText(titles[1]);

        XWPFRun run3 = row.addNewTableCell().addParagraph().createRun();
        run3.setBold(true);
        run3.setFontFamily(FONT_FAMILY);
        run3.setFontSize(11);
        run3.setText(titles[2]);

        for (TableInfo info : tableInfo) {
            XWPFTableRow subRow = table.createRow();
            XWPFRun run4 = subRow.getCell(0).addParagraph().createRun();
            run4.setFontFamily(FONT_FAMILY);
            run4.setFontSize(9);
            if (info.getPeriod() != null) {
                run4.setText(info.getPeriod());
            }

            XWPFRun run5 = subRow.getCell(1).addParagraph().createRun();
            run5.setFontFamily(FONT_FAMILY);
            run5.setFontSize(9);
            if (info.getName() != null && info.getPosition() != null) {
                run5.setText(String.format("%s/%s", info.getName(),
                        info.getPosition()));
            } else if (info.getName() != null && info.getPosition() == null) {
                run5.setText(info.getName());
            }

            XWPFRun run6 = subRow.getCell(2).addParagraph().createRun();
            run6.setFontFamily(FONT_FAMILY);
            run6.setFontSize(9);
        }

        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow rowTable = table.getRow(i);
            int numCells = rowTable.getTableCells().size();
            for (int j = 0; j < numCells; j++) {
                XWPFTableCell cell = rowTable.getCell(j);
                CTTblWidth cellWidth = cell.getCTTc().addNewTcPr().addNewTcW();
                CTTcPr pr = cell.getCTTc().addNewTcPr();
                pr.addNewNoWrap();
                cellWidth.setW(BigInteger.valueOf(2880));
            }
        }
    }
}
