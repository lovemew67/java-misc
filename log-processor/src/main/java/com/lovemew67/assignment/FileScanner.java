package com.lovemew67.assignment;

import static com.lovemew67.assignment.Utility.*;

import com.lovemew67.assignment.FileParser.Parse;

import org.apache.commons.io.FileUtils;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import akka.event.Logging;
import akka.event.LoggingAdapter;

public class FileScanner extends AbstractActor {
    static public Props props(ActorRef fileParser) {
        return Props.create(FileScanner.class, () -> new FileScanner(fileParser));
    }

    static public class Scan {
        public final String folderPath;

        public Scan(String folderPath) {
            this.folderPath = folderPath;
        }
    }

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef fileParser;

    public FileScanner(ActorRef fileParser) {
        this.fileParser = fileParser;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Scan.class, x -> {
                    try {
                        String currentFolder = x.folderPath;
                        Path path = Paths.get(currentFolder);
                        log.info("Set to: [{}]", currentFolder);

                        // Check whther given path exists and is a folder
                        if (Files.exists(path) && Files.isDirectory(path)) {
                            log.info("Start to scan folder: [{}]", currentFolder);

                            // Traverse recursively for each file in the folder
                            for (File eachFile : FileUtils.listFiles(new File(currentFolder), null, true)) {
                                String historyFileName = main.historyPath + main.typeOfWordCount + getFileSha256Checksum(eachFile);
                                path = Paths.get(historyFileName);

                                // Check if current file have ever processed or not. Print out the word-count map if it had processed.
                                if (Files.exists(path)) {
                                    log.info("File: [{}] already processed.", eachFile.getCanonicalPath());
                                    if (main.isCountForEachWord) {
                                        printOutHashMapBeautifully(readHashMapFromFile(historyFileName), eachFile.getCanonicalPath(), log);
                                    }
                                    else {
                                        int totalWordCount = (int) readIntegerFromFile(historyFileName);
                                        log.info("Total word count of file: [{}] is [{}]", eachFile.getCanonicalPath(), totalWordCount);
                                    }
                                }
                                else {
                                    String mimeType = getFileMimeType(eachFile);

                                    // Only process files in MIME text type
                                    if (mimeType.contains("text/")) {
                                        log.info("Trigger parse file: [{}]", eachFile.getCanonicalPath());
                                        fileParser.tell(new Parse(eachFile.getCanonicalPath()), getSelf());
                                    }
                                    else {
                                        log.error("File: [{}] with mimeType: [{}] does not support", eachFile.getCanonicalPath(), mimeType);
                                    }
                                }
                            }
                        }
                        else {
                            log.error("Either given path does not exist or is not a folder");
                        }
                    }
                    catch (Exception e) {
                        log.error("Exception happened: [{}]", e);
                    }
                }).build();
    }
}