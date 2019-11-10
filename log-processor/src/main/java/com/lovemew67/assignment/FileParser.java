package com.lovemew67.assignment;

import com.lovemew67.assignment.Aggregator.StartToAggregateFileContents;
import com.lovemew67.assignment.Aggregator.StopToAggregateFileContents;
import com.lovemew67.assignment.Aggregator.AggregateContentsFromFile;

import org.apache.commons.io.FileUtils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.txt.UniversalEncodingDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import java.nio.charset.Charset;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import akka.event.Logging;
import akka.event.LoggingAdapter;

public class FileParser extends AbstractActor {
    static public Props props(ActorRef aggregator) {
        return Props.create(FileParser.class, () -> new FileParser(aggregator));
    }

    static public class Parse {
        public final String fileName;

        public Parse(String fileName) {
            this.fileName = fileName;
        }
    }

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private UniversalEncodingDetector ued = new UniversalEncodingDetector();
    private Metadata metadata = new Metadata();
    private final ActorRef aggregator;

    public FileParser(ActorRef aggregator) {
        this.aggregator = aggregator;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Parse.class, x -> {
                    try {
                        String currentFileName = x.fileName;
                        log.info("Start to parse file: [{}]", currentFileName);
                        aggregator.tell(new StartToAggregateFileContents(currentFileName), getSelf());

                        // Split the file and send each line to aggregator
                        File currentFile = new File(currentFileName);
                        Charset currentFileCharset = ued.detect(new BufferedInputStream(new FileInputStream(currentFile)), metadata);
                        for (String eachLineInCurrentFile : FileUtils.readLines(currentFile, currentFileCharset)) {
                            aggregator.tell(new AggregateContentsFromFile(currentFileName, eachLineInCurrentFile), getSelf());
                        }

                        log.info("End of parsing file: [{}]", currentFileName);
                        aggregator.tell(new StopToAggregateFileContents(currentFileName), getSelf());
                    }
                    catch (Exception e) {
                        log.error("Exception happened: [{}]", e);
                    }
                }).build();
    }
}