package gui;

import wordextractor.WordExtractor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class MainWindow extends JFrame {
    private final MainWindow me;
    private File[] exceptionWordsFiles;
    private File[] subTitleFiles;
    private JTabbedPane subTitleFilesTabPane;
    private  JTable[] tables;
    private final Clipboard systemClipBoard;
    private final ResourceBundle rb;
    public MainWindow(){
        rb = ResourceBundle.getBundle("locales/guitext");
        me = this;
        systemClipBoard = getSystemClipboard();
        this.getContentPane().add(createContentPane());
        this.setJMenuBar(createRootJMenuBar());
        this.pack();
    }
    private JMenuBar createRootJMenuBar(){
        JMenuBar jMenuBar = new JMenuBar();
        JMenu jFileMenu = new JMenu(rb.getString("mainWindow.menu.File"));
        {
            JMenuItem jExportWordsFromThisFileToAnotherFile = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsFromThisFileToAnotherFile"));
            JMenuItem jExportWordsFromAllFilesToOneFile = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsFromAllFilesToAnotherFile"));
            jFileMenu.add(jExportWordsFromThisFileToAnotherFile);
            jFileMenu.addSeparator();
            jFileMenu.add(jExportWordsFromAllFilesToOneFile);
        }
        jMenuBar.add(jFileMenu);
        return jMenuBar;
    }
    private Component createContentPane(){

        subTitleFilesTabPane = new JTabbedPane();
        FileNameExtensionFilter subRipAndPlainTextFileFilter = new FileNameExtensionFilter("SubRip and Plain text", "srt", "txt");
        FileNameExtensionFilter exceptionWordsFileFilter = new FileNameExtensionFilter("Plaint text", "txt");
        JPanel contentPane = new JPanel();
        JButton selectSrtFileButton = new JButton("Выберите файл субтитров");
        JButton copyWordsFromThisFile = new JButton("Скопировать слова\n из этого файла");
        JButton copyWordsFromAllFiles = new JButton("Скопировать слова\n из всех файлов");
        JButton selectExceptionWordsFileButton = new JButton("Выберите файл с\n исключаемыми словами");
        JButton extractWordsFromSrtFiles = new JButton("Извлечь слова");
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(true);
        selectSrtFileButton.addActionListener(
                (l)->{
                    jFileChooser.setDialogTitle("Выберите файл субтитров");
                    jFileChooser.setFileFilter(subRipAndPlainTextFileFilter);
                    int result = jFileChooser.showOpenDialog(me);
                    if(result == JFileChooser.APPROVE_OPTION){
                        subTitleFiles = jFileChooser.getSelectedFiles();
                        extractWordsFromSrtFiles.setEnabled(true);
                    }
                }
        );
        selectExceptionWordsFileButton.addActionListener(
                (l)->{
                    jFileChooser.setDialogTitle("Выберите файл исключаемых слов");
                    jFileChooser.setFileFilter(exceptionWordsFileFilter);
                    int result = jFileChooser.showOpenDialog(me);
                    if(result == JFileChooser.APPROVE_OPTION){
                        exceptionWordsFiles = jFileChooser.getSelectedFiles();
                    }
                }
        );
        extractWordsFromSrtFiles.addActionListener(e -> {
            Predicate<Character> englishWordPredicate = (character -> (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z'));

            try{
                Set<String> exceptionWords = exceptionWordsFiles != null && exceptionWordsFiles.length > 0 ? new HashSet<>() : null;
                if(exceptionWordsFiles != null && exceptionWordsFiles.length > 0) {
                    for (File exceptionWordsFile : exceptionWordsFiles) {
                        exceptionWords.addAll(new WordExtractor(exceptionWordsFile, englishWordPredicate, null).getWords());
                    }
                }
                if(subTitleFiles != null && subTitleFiles.length > 0){
                    tables = new JTable[subTitleFiles.length];
                    for (int i = 0; i < subTitleFiles.length; i++) {
                        var extractedWordsAndCountRepeats = new WordExtractor(subTitleFiles[i], englishWordPredicate, exceptionWords).getWordsAndCountRepeats();
                        JTable jTable = fillTable(extractedWordsAndCountRepeats);
                        subTitleFilesTabPane.addTab(subTitleFiles[i].getName(), new JScrollPane(jTable));
                        tables[i] = jTable;
                    }
                    copyWordsFromThisFile.setEnabled(true);
                    copyWordsFromAllFiles.setEnabled(true);
                }


            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        });
        copyWordsFromThisFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedTabIndex = subTitleFilesTabPane.getSelectedIndex();
                JTable table = tables[selectedTabIndex];
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < table.getRowCount(); i++){
                    stringBuilder.append(table.getValueAt(i, 0));
                    stringBuilder.append("\n");
                }
                systemClipBoard.setContents(new StringSelection(stringBuilder.toString()), null);
            }
        });

        extractWordsFromSrtFiles.setEnabled(false);
        copyWordsFromThisFile.setEnabled(false);
        copyWordsFromAllFiles.setEnabled(false);
        contentPane.setLayout(new BorderLayout());
        JToolBar jToolBar = new JToolBar();
        jToolBar.setFloatable(false);

        jToolBar.add(selectSrtFileButton);
        jToolBar.add(selectExceptionWordsFileButton);
        jToolBar.add(extractWordsFromSrtFiles);
        jToolBar.add(copyWordsFromThisFile);
        jToolBar.setMinimumSize(new Dimension(selectSrtFileButton.getWidth() + selectExceptionWordsFileButton.getWidth() + extractWordsFromSrtFiles.getWidth() + copyWordsFromAllFiles.getWidth() + 20, selectExceptionWordsFileButton.getHeight()));
        contentPane.add(jToolBar, BorderLayout.PAGE_END);
        contentPane.add(subTitleFilesTabPane, BorderLayout.CENTER);
        return contentPane;
    }
    private JTable fillTable(Map<String, Integer> extractedWords) {
        Object[][] tableData = extractedWords.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).map(value -> {
            Object[] kv = new Object[2];
            kv[0] = value.getKey();
            kv[1] = value.getValue();
            return kv;
        }).toArray(Object[][]::new);
        String[] tableColumns = {"Слово", "Количество повторений"};
        JTable jTable = new JTable(tableData, tableColumns);
        jTable.setAutoCreateRowSorter(true);
        return jTable;
    }
    private static Clipboard getSystemClipboard()
    {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        return defaultToolkit.getSystemClipboard();
    }
    public static void main(String[] args) {
        MainWindow mainWindow = new MainWindow();
        mainWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainWindow.setVisible(true);
    }
}
