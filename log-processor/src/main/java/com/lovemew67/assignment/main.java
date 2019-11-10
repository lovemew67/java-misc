package com.lovemew67.assignment;

import com.lovemew67.assignment.FileScanner.Scan;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Objects;
import java.util.Properties;

public class main {

    public static String lineSeparator;
    public static String historyPath;
    public static String folderList;
    public static String typeOfWordCount;

    public static boolean isCountForEachWord = false;

    public static final String COUNT_FOR_EACH_WORD = "EACH";
    public static final String COUNT_FOR_TOTAL_WORD = "TOTAL";

    public static void main(String[] args) {
        final String propertyFile = "akka.properties";
        final ActorSystem system = ActorSystem.create("log-processor");

        try {
            //#process-property
            Properties prop = new Properties();
            prop.load(new FileInputStream(new File(propertyFile)));
            if (Objects.isNull(prop.getProperty("FolderList"))) {
                throw new Exception("Property [FolderList] was not provided.");
            }
            folderList = prop.getProperty("FolderList");
            if (Objects.isNull(prop.getProperty("HistoryPath"))) {
                throw new Exception("Property [HistoryPath] was not provided.");
            }
            if (prop.getProperty("HistoryPath").contains("/")) {
                historyPath = prop.getProperty("HistoryPath");
            }
            else {
                historyPath = prop.getProperty("HistoryPath") + "/";
            }
            if (Objects.isNull(prop.getProperty("LineSeparator"))) {
                throw new Exception("Property [LineSeparator] was not provided.");
            }
            lineSeparator = prop.getProperty("LineSeparator");
            if (Objects.isNull(prop.getProperty("TypeOfWordCount"))) {
                throw new Exception("Property [TypeOfWordCount] was not provided.");
            }
            typeOfWordCount = prop.getProperty("TypeOfWordCount");
            if (!typeOfWordCount.equals(COUNT_FOR_TOTAL_WORD) && !typeOfWordCount.equals(COUNT_FOR_EACH_WORD)) {
                throw new Exception("Unrecognized value for property [TypeOfWordCount]. Please give [TOTAL] or [EACH].");
            }
            isCountForEachWord = typeOfWordCount.equals(COUNT_FOR_EACH_WORD);
            typeOfWordCount = typeOfWordCount + "_";
            //#process-property

            //#make-history
            Path path = Paths.get(historyPath);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            //#make-history

            //#create-actors
            final ActorRef aggregator = system.actorOf(Aggregator.props());
            final ActorRef fileParser = system.actorOf(FileParser.props(aggregator));
            final ActorRef fileScanner = system.actorOf(FileScanner.props(fileParser));
            //#create-actors

            //#main-send-messages
            for ( String eachFolder : folderList.split(",")) {
                fileScanner.tell(new Scan(eachFolder), ActorRef.noSender());
            }
            //#main-send-message

            System.out.println(">>>Press Enter to exit<<<");
            System.in.read();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            system.terminate();
        }
    }
}