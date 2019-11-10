# log-processor

## Introduction
In this program, a.k.a log processor, helps user to summerize the word occurance of each files in the given list.

### Prepare property file
log processor will read configurations from akka.property file. There are three properties you need to configure.

1. FolderList: List the folders you would like to scan. This list can be concatenated with comma.
1. HistoryPath: log processor support history feature which caches the statics for each file to reduce duplicated efforts.
1. LineSeparator: Which charactor you like to use to separate each line. (In unicode format)
1. TypeOfWordCount: Indicate that you would count the total count or the each count of words in each file. This value must be "TOTAL" or "EACH"

Here is a sample property

```
FolderList=log/1,log/2
HistoryPath=history/
LineSeparator=\u0020
TypeOfWordCount=EACH
```

### Build and Run
This project use Gradle wrapper for dependency management. 
```
gradlew build/clean/test/run
```

### Execution flow
![](http://www.plantuml.com/plantuml/img/XPF1QiCm38RlVWhTTjdo03sCTcDbFGmAPGyW7CKnJMrZAorz-qb9ksGRxI04OVtwzKLobeKnSt_eZ47BCSDdeMmeiESp7D27Qxps7UqiXZ2bzMYBULQmSYwJG_4nHetWwNamMS0sKy9CWFizLYd7H5dqZJW0XXeuU-T4hsAGjCjuK21_HAQHOqY9tmna0Hf04xjQQbVLL36ddSNN-f5w0QyjsVr8iGAnRtqHzlF5iO9D0xRFMRvk59smX0TMG4kbKFshusKOKwZzffa8hWuRE4BIT3NBINVFWF2EfVt0f1f58NaZ2vMtfRfZl1O7Gjj2vmCzpdrBwiHOdfFd5efkOJZNgHSy4d1BaAdq7T_jy2Rws2Yu3-v7XoGdFDXChCg_uphsN9cQRWtfUoktr_8oWaQ8uO_JTS7_tenR9iEzCaiAjVm0Nm00)
