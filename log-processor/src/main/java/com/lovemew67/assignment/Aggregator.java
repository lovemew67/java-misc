package com.lovemew67.assignment;

import static com.lovemew67.assignment.Utility.*;

import akka.actor.AbstractActor;
import akka.actor.Props;

import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

public class Aggregator extends AbstractActor {
    static public Props props() {
        return Props.create(Aggregator.class, () -> new Aggregator());
    }

    static public class StartToAggregateFileContents {
        public final String fileName;

        public StartToAggregateFileContents(String fileName) {
            this.fileName = fileName;
        }
    }
    static public class StopToAggregateFileContents {
        public final String fileName;

        public StopToAggregateFileContents(String fileName) {
            this.fileName = fileName;
        }
    }
    static public class AggregateContentsFromFile {
        public final String fileName;
        public final String lineContent;

        public AggregateContentsFromFile(String fileName, String lineContent) {
            this.fileName = fileName;
            this.lineContent = lineContent;
        }
    }

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private Map<String, HashMap<String, Integer>> eachWordCountMap = new HashMap<String, HashMap<String, Integer>>();
    private Map<String, Integer> totalWordCountMap = new HashMap<String, Integer>();

    public Aggregator() {

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartToAggregateFileContents.class, stafc -> {
                    eachWordCountMap.put(stafc.fileName, new HashMap<String, Integer>());
                    totalWordCountMap.put(stafc.fileName, 0);
                })
                .match(AggregateContentsFromFile.class, acff -> {
                    if (main.isCountForEachWord) {
                        Map wordCountMap = eachWordCountMap.get(acff.fileName);

                        // Split given line content with line separator and store the statistics in a map
                        for (String eachToken : acff.lineContent.split(main.lineSeparator)) {
                            if (wordCountMap.containsKey(eachToken)) {
                                wordCountMap.put(eachToken, (int)wordCountMap.get(eachToken)+1 );
                            }
                            else {
                                wordCountMap.put(eachToken, 1);
                            }
                        }
                    }
                    else {
                        int numberOfWordInCurrentLine = acff.lineContent.split(main.lineSeparator).length;
                        totalWordCountMap.put(acff.fileName, totalWordCountMap.get(acff.fileName) + numberOfWordInCurrentLine);
                    }
                })
                .match(StopToAggregateFileContents.class, stafc -> {
                    if (main.isCountForEachWord) {
                        printOutHashMapBeautifully(eachWordCountMap.get(stafc.fileName), stafc.fileName, log);

                        // Write current word-count map to history folder
                        String historyFileName = main.historyPath + main.typeOfWordCount + getFileSha256Checksum(new File(stafc.fileName));
                        writeHashMapToFile(eachWordCountMap.get(stafc.fileName), historyFileName);
                    }
                    else {
                        log.info("Total word count of file: [{}] is [{}]", stafc.fileName, totalWordCountMap.get(stafc.fileName));

                        String historyFileName = main.historyPath + main.typeOfWordCount + getFileSha256Checksum(new File(stafc.fileName));
                        writeIntegerToFile(totalWordCountMap.get(stafc.fileName), historyFileName);
                    }
                })
                .build();
    }

}