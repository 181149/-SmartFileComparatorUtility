package com.smartutility.Service;

import com.smartutility.Model.Record;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SmartFileComparatorService {
    /**
     * @param args
     */
    private static final Double PRICEDIFFERENCE = 0.005;

    public static void main(String[] args) throws IOException {
        String expectedFilePath = getCurrentDirectory() + "\\src\\com\\smartutility\\expected.txt";
        String actualFilePath = getCurrentDirectory() + "\\src\\com\\smartutility\\actual.txt";
        compare(expectedFilePath, actualFilePath);
    }

    private static void compare(String expectedFilePath, String actualFilePath) {
        String resultFilePath = getCurrentDirectory() + "\\src\\com\\smartutility\\output.txt";
        File expectedFile = new File(expectedFilePath);
        File actualFile = new File(actualFilePath);
        File resultFile = new File(resultFilePath);
        List<Record> expectedFileRecords = new ArrayList<>();
        List<Record> actualFileRecords = new ArrayList<>();

        // Read Expected File and Actual File and create an arrayList of
        // Records.
        readInputFile(expectedFile, expectedFileRecords);
        readInputFile(actualFile, actualFileRecords);

        compareExpectedAndActualRecords(expectedFileRecords, actualFileRecords, false, resultFile);

    }

    private static void compareExpectedAndActualRecords(List<Record> expectedFileRecords,
                                                        List<Record> actualFileRecords, Boolean isRecordExist, File resultFile) {

        try (BufferedWriter resultFileBufferedStream = new BufferedWriter(new FileWriter(resultFile))) {
            resultFileBufferedStream.write("==== Compare Result ====");
            resultFileBufferedStream.newLine();
            for (Record expectedRecord : expectedFileRecords) {

                for (Record actualRecord : actualFileRecords) {
                    isRecordExist = false;
                    if (expectedRecord.getTicker().equals(actualRecord.getTicker())) {
                        isRecordExist = true;
                        compareQuantity(expectedRecord, actualRecord, resultFileBufferedStream);
                        comparePrice(expectedRecord, actualRecord, resultFileBufferedStream);

                        /*
                         * Remove the matching record from the actualFileRecords
                         * arrayList
                         */

                        actualFileRecords.remove(actualRecord);
                        break;
                    }

                }
                if (!isRecordExist) {
                    resultFileBufferedStream.write("missing line for " + expectedRecord.getTicker());
                    resultFileBufferedStream.newLine();
                }
            }

            if (actualFileRecords.size() > 0) {
                for (Record actualRecord : actualFileRecords) {
                    resultFileBufferedStream.write("unexpected line for " + actualRecord.getTicker());
                    resultFileBufferedStream.newLine();
                }
            }
            resultFileBufferedStream.write("==== END ====");
        } catch (IOException e) {
            System.out.println("Error occurred while reading file " + resultFile.getPath());
            e.printStackTrace();
        }
    }

    // method to read the file and map each line to object
    private static void readInputFile(File file, List<Record> records) {
        String currentLine;
        try (BufferedReader expectedFileBufferedStream = new BufferedReader(new FileReader(file))) {
            // loop through expected file, parse each line.
            while ((currentLine = expectedFileBufferedStream.readLine()) != null) {
                currentLine = currentLine.trim();
                Record rec = recordFromLine(currentLine);
                records.add(rec);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to locate File - " + file.getPath());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error occurred while reading file " + file.getPath());
            e.printStackTrace();
        }
    }

    // method to compare price and check if the difference is greater than 0.005
    private static void comparePrice(Record expectedRecord, Record actualRecord,
                                     BufferedWriter resultFileBufferedStream) throws IOException {
        if (!(expectedRecord.getPrice().equals(actualRecord.getPrice()))) {
            if (Math.abs(Double.valueOf(expectedRecord.getPrice())
                    - Double.valueOf(actualRecord.getPrice())) >= PRICEDIFFERENCE) {
                resultFileBufferedStream.write("mismatch for " + expectedRecord.getTicker() + " (Expected Price: "
                        + expectedRecord.getPrice() + ", Actual Price: " + actualRecord.getPrice()
                        + ")");
                resultFileBufferedStream.newLine();
            }
        }
    }

    // method to compare quantity between expected and actual record
    private static void compareQuantity(Record expectedRecord, Record actualRecord,
                                        BufferedWriter resultFileBufferedStream) throws IOException {
        if (!(expectedRecord.getQuantity().equals(actualRecord.getQuantity()))) {
            resultFileBufferedStream.write("mismatch for " + expectedRecord.getTicker() + " (Expected Quantity: "
                    + expectedRecord.getQuantity() + ", Actual Quantity: " + actualRecord.getQuantity() + ")");
            resultFileBufferedStream.newLine();
        }
    }

    // map the line read from the file to Record object
    private static Record recordFromLine(String currentLine) {
        Record rec = new Record();
        String[] elements;
        elements = currentLine.split(" ");
        rec.setTicker(elements[0].trim());
        rec.setQuantity(elements[1].trim());
        rec.setPrice((elements[2].trim()));
        rec.setComment(elements[3].trim());
        return rec;
    }

    // get current directory for the input and output files
    private static String getCurrentDirectory() {
        String currentDirectory;
        File file = new File(".");
        currentDirectory = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 1);
        return currentDirectory;
    }
}
