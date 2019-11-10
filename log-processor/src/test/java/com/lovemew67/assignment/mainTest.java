package com.lovemew67.assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.lovemew67.assignment.FileScanner.Scan;
import com.lovemew67.assignment.FileParser.Parse;
import com.lovemew67.assignment.Aggregator.StartToAggregateFileContents;
import com.lovemew67.assignment.Aggregator.StopToAggregateFileContents;
import com.lovemew67.assignment.Aggregator.AggregateContentsFromFile;

import org.apache.commons.io.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import akka.testkit.javadsl.TestKit;

public class mainTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        // Generate testing property
        main.historyPath = "history/";
        main.lineSeparator = "\u0020";
        main.typeOfWordCount = "EACH_";
        main.isCountForEachWord = true;

        // Check testing data
        File testLogFolder = new File("log/test");
        assertTrue(testLogFolder.exists());
        File testLogData = new File("log/test/data.txt");
        assertTrue(testLogData.exists());

        // Check test-history folder
        try {
            FileUtils.deleteDirectory(new File("history"));
            Files.createDirectory(Paths.get("history"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() throws Exception {
        //History file should be generated
        assertTrue(retry(() -> {
            File file = new File("history/EACH_1b9886f69db2700f903cf4e04d9dc8c5cdd12252e942adae510a8ba896a4c5d6");
            return (file.exists());
        },20, 1));

        //Binary file should be skipped
        assertTrue(1 == FileUtils.listFiles(new File("history"), null, true).size());

        // Remove testing history
        try {
            FileUtils.deleteDirectory(new File("history"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void verifyMessageOfEachActors() {
        final TestKit testProbe = new TestKit(system);
        final ActorRef fileScanner = system.actorOf(FileScanner.props(testProbe.getRef()));
        final ActorRef fileParser = system.actorOf(FileParser.props(testProbe.getRef()));

        fileScanner.tell(new Scan("log/test"), ActorRef.noSender());
        Parse parse = testProbe.expectMsgClass(Parse.class);
        assertTrue(parse.fileName.contains("log/test/data.txt"));

        fileParser.tell(new Parse("log/test/data.txt"), ActorRef.noSender());
        StartToAggregateFileContents starttafc = testProbe.expectMsgClass(StartToAggregateFileContents.class);
        assertTrue(starttafc.fileName.contains("log/test/data.txt"));

        AggregateContentsFromFile acff = testProbe.expectMsgClass(AggregateContentsFromFile.class);
        assertEquals("line1,word1 word2", acff.lineContent);

        StopToAggregateFileContents stoptafc = testProbe.expectMsgClass(StopToAggregateFileContents.class);
        assertTrue(stoptafc.fileName.contains("log/test/data.txt"));
    }

    @Test
    public void verifyOnlyTextFilesWereProcessed() throws Exception {
        final ActorRef aggregator = system.actorOf(Aggregator.props());
        final ActorRef fileParser = system.actorOf(FileParser.props(aggregator));
        final ActorRef fileScanner = system.actorOf(FileScanner.props(fileParser));

        fileScanner.tell(new Scan("log/test"), ActorRef.noSender());
    }

    public static boolean retry(RetryOperation<Boolean> action, int maxAttempts, int sleepTime) throws Exception {
        for(int attempt = 1; attempt <= maxAttempts; ++attempt) {
            Boolean returnValue = (Boolean)action.supply();
            if (returnValue) {
                return true;
            }
            Thread.sleep((long)(sleepTime * 1000));
        }
        return false;
    }

    @FunctionalInterface
    public interface RetryOperation<T> {
        T supply() throws Exception;
    }
}