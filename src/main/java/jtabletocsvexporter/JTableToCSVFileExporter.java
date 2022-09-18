package jtabletocsvexporter;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

public class JTableToCSVFileExporter {
    public static void exportDataToFile(JTable jTable, File exportFile) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile));
        String[] columnNames = new String[jTable.getColumnCount()];
        for (int i = 0; i < jTable.getColumnCount(); i++) {
            columnNames[i] = jTable.getColumnName(i);
        }
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.Builder.create().setRecordSeparator("\n").setEscape('\\').setQuoteMode(QuoteMode.NONE).setHeader(columnNames).build());
        Object[] columnValues = new Object[columnNames.length];
        for (int i = 0; i < jTable.getRowCount(); i++) {
            for (int j = 0; j < jTable.getColumnCount(); j++) {
                columnValues[j] = jTable.getValueAt(i, j);
            }
            csvPrinter.printRecord(columnValues);
        }
        csvPrinter.flush();
    }
}
