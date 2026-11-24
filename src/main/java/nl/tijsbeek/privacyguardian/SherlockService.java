package nl.tijsbeek.privacyguardian;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

@Service
public class SherlockService {

    private final boolean testMode;

    public SherlockService(@Value("${sherlock.test-mode:false}") boolean testMode) {
        this.testMode = testMode;
    }

    public List<String> getSiteLinks(String userName) {
        try {

            Path outputPath = runSherlock(userName);

            Path csvPath = checkForCsvOutput(outputPath);
            return readIntoMainUrls(csvPath);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path runSherlock(String userName) {
        if (testMode) {
            return Path.of("C:\\Users\\tijs\\Documents\\Test");
        }

        Process sherlockProcess = null;
        try {
            Path tempPath = Files.createTempDirectory("privacyguardian");

            sherlockProcess = new ProcessBuilder(
                    "sherlock", userName, "--csv", "--folderoutput", tempPath.toString()
            ).redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();

            sherlockProcess.waitFor();

            return tempPath;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (sherlockProcess != null) {
                sherlockProcess.destroy();
            }
        }
    }

    private List<String> readIntoMainUrls(Path csvPath) throws IOException {
        if (csvPath == null) {
            return List.of();
        }

        CSVFormat format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        try (BufferedReader reader = Files.newBufferedReader(csvPath);
             CSVParser parser = format.parse(reader)) {

            return parser.stream()
                    .map(row -> row.get("url_main"))
                    .toList();
        }
    }

    private Path checkForCsvOutput(Path tempPath) throws IOException {
        Path csvPath;

        try (Stream<Path> files = Files.list(tempPath)) {
            csvPath = files
                    .filter(Files::isRegularFile)
                    .filter(this::isCsv)
                    .findFirst()
                    .orElse(null);
        }

        return csvPath;
    }

    private boolean isCsv(@NonNull Path filePath) {
        return filePath.toString().endsWith(".csv");
    }
}
