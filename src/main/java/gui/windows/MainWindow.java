package gui.windows;

import gui.interfaces.ProgressBarInterface;
import gui.jtablereflection.JReflectionTable;
import gui.workrers.ExtractExceptionWordsFromFilesWorker;
import gui.workrers.ExtractWordsFromFilesWorker;
import translategetter.MicrosoftTranslate;
import translategetter.Translate;
import wordprocessing.wordinfo.MinimalWordInfo;
import wordprocessing.wordinfo.WordInfo;
import wordprocessing.wordinfogenerator.WordInfoGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class MainWindow extends JFrame {
    private final HashMap<Translate.PartOfSpeech, String> localizedPartOfSpeechesNames = new HashMap<>();
    private final static String partOfSpeechRbPrefix = "mainWindow.table.cellValues.partOfSpeech";
    private final MainWindow me;

    private final JTabbedPane subTitleFilesTabPane;
    private final JProgressBar jProgressBar;
    private final Clipboard systemClipBoard;
    private final ResourceBundle rb;
    private final WordInfoGenerator wordInfoGenerator;
    private final ProgressBarInterface progressBarInterface;

    private boolean needTranslate = true;
    private JTable[] tables;
    private File[] exceptionWordsFiles;
    private File[] subTitleFiles;
    public MainWindow(){
        progressBarInterface = new ProgressBarInterface() {
            @Override
            public void setMinimum(int minimum) {
                jProgressBar.setMinimum(minimum);
            }

            @Override
            public void setMaximum(int maximum) {
                jProgressBar.setMaximum(maximum);
            }

            @Override
            public void setValue(int newValue) {
                jProgressBar.setValue(newValue);
            }
        };
        jProgressBar = new JProgressBar(0, 100);
        subTitleFilesTabPane = new JTabbedPane();
        me = this;
        rb = ResourceBundle.getBundle("locales/guitext");
        for (Translate.PartOfSpeech partOfSpeech : Translate.PartOfSpeech.values()) {
            localizedPartOfSpeechesNames.put(partOfSpeech, rb.getString(String.format("%s.%s", partOfSpeechRbPrefix, partOfSpeech.name())));
        }
        wordInfoGenerator = new WordInfoGenerator(new MicrosoftTranslate("739f0d0146msh7cc406a9f7c0d10p1f79c6jsn56e503cd2739"), "mainWindow.table.cellValues.partOfSpeech", rb, progressBarInterface);
        systemClipBoard = getSystemClipboard();
        this.getContentPane().add(createContentPane());
        this.setJMenuBar(createRootJMenuBar());
        this.setTitle(rb.getString("mainWindow.title"));
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
            //fixme this is critical, function in current realization will not work
            Predicate<Character> englishWordPredicate = (character -> (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z'));
            ExtractExceptionWordsFromFilesWorker extractExceptionWordsFromFilesWorker = new ExtractExceptionWordsFromFilesWorker(exceptionWordsFiles, englishWordPredicate);
            ExtractWordsFromFilesWorker extractWordsFromFilesWorker = new ExtractWordsFromFilesWorker(subTitleFiles, null, englishWordPredicate);
            PropertyChangeListener propertyChangeListener = evt -> {
                Object propertyValue = evt.getNewValue();
                Object source = evt.getSource();
                switch (evt.getPropertyName()){
                    case "progress" -> jProgressBar.setValue((int)propertyValue);
                    case "state" ->{
                        if (propertyValue == SwingWorker.StateValue.STARTED) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            if(source == extractExceptionWordsFromFilesWorker) jProgressBar.setString("Чтение файлов исключений");
                            else if(source == extractWordsFromFilesWorker)jProgressBar.setString("Чтение файлов субтитров");

                        }
                        else if(propertyValue == SwingWorker.StateValue.DONE){
                            try{
                                if(source == extractExceptionWordsFromFilesWorker) {
                                    extractWordsFromFilesWorker.setExceptionWords(extractExceptionWordsFromFilesWorker.get());
                                }
                                else if(source == extractWordsFromFilesWorker){
                                    setCursor(Cursor.getDefaultCursor());
                                    MinimalWordInfo[] minimalWordInfos = extractWordsFromFilesWorker.get();
                                    if(needTranslate){

                                    }
                                    else fillAndCreateTable(minimalWordInfos);
                                }
                            }
                            catch (ExecutionException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }

                        }
                    }
                }
            };
//            extractExceptionWordsFromFilesWorker.addPropertyChangeListener(eventForExceptionWordsExtractor -> {
//                Object propertyValue = eventForExceptionWordsExtractor.getNewValue();
//                switch (eventForExceptionWordsExtractor.getPropertyName()){
//                    case "progress" -> jProgressBar.setValue((int)propertyValue);
//                    case "state" ->{
//                        if (propertyValue == SwingWorker.StateValue.STARTED) {
//                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//                            jProgressBar.setString("Чтение файлов исключений");
//                        }
//                        else if(propertyValue == SwingWorker.StateValue.DONE){
//                            try {
//                                ExtractWordsFromFilesWorker extractWordsFromFilesWorker = new ExtractWordsFromFilesWorker(subTitleFiles, extractExceptionWordsFromFilesWorker.get(), englishWordPredicate);
//                                extractWordsFromFilesWorker.addPropertyChangeListener(eventForWordsExtractor -> {
//                                    Object propertyValue1 = eventForWordsExtractor.getNewValue();
//                                    switch (eventForWordsExtractor.getPropertyName()){
//                                        case "progress"->{
//                                            jProgressBar.setValue((int) propertyValue1);
//                                        }
//                                        case "state"->{
//                                            if(propertyValue1 == SwingWorker.StateValue.STARTED){
//
//                                            }
//                                            else if(propertyValue1 == SwingWorker.StateValue.DONE){
//
//                                            }
//                                        }
//                                    }
//                                });
//
//                            } catch (InterruptedException | ExecutionException ex) {
//                                throw new RuntimeException(ex);
//                            }
//                        }
//                    }
//                }
//            });
//            try{
//                Set<String> exceptionWords = exceptionWordsFiles != null && exceptionWordsFiles.length > 0 ? new HashSet<>() : null;
//                if(exceptionWordsFiles != null && exceptionWordsFiles.length > 0) {
//                    jProgressBar.setString("Чтение файлов исключений");
//                    for (File exceptionWordsFile : exceptionWordsFiles) {
//                        jProgressBar.setString("Чтение файла исключений: %s".formatted(exceptionWordsFile.getName()));
//                        exceptionWords.addAll(new WordExtractor(exceptionWordsFile, englishWordPredicate, null, progressBarInterface).getWords());
//                    }
//                }
//                if(subTitleFiles != null && subTitleFiles.length > 0){
//                    jProgressBar.setValue(0);
//                    jProgressBar.setString("Чтение файлов с субтитрами");
//                    tables = new JTable[subTitleFiles.length];
//                    for (int i = 0; i < subTitleFiles.length; i++) {
//                        jProgressBar.setString("Чтение файла с субтитрами: %s".formatted(subTitleFiles[i].getName()));
//                        var minimalWordInfos = new WordExtractor(subTitleFiles[i], englishWordPredicate, exceptionWords, progressBarInterface).getWordsAndCountRepeats();
//                        JTable wordsTable = (needTranslate ? fillAndCreateTable(wordInfoGenerator.generateWordInfos(minimalWordInfos)) : fillAndCreateTable(minimalWordInfos));
//                        subTitleFilesTabPane.addTab(subTitleFiles[i].getName(), new JScrollPane(wordsTable));
//                        tables[i] = wordsTable;
//                    }
//                    copyWordsFromThisFile.setEnabled(true);
//                    copyWordsFromAllFiles.setEnabled(true);
//                }
//
//
//            }
//            catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
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
        contentPane.add(jToolBar, BorderLayout.SOUTH);
        contentPane.add(jProgressBar, BorderLayout.NORTH);
        contentPane.add(subTitleFilesTabPane, BorderLayout.CENTER);
        return contentPane;
    }
    private JTable fillAndCreateTable(MinimalWordInfo[] minimalWordInfos){
        JTable jTable = new JReflectionTable<>(minimalWordInfos,  MinimalWordInfo.class, MinimalWordInfo.class, rb);
        jTable.setAutoCreateRowSorter(true);
        return jTable;
    }
    private JTable fillAndCreateTable(WordInfo[] wordInfos){
        JTable jTable = new JReflectionTable<>(wordInfos,  WordInfo.class, MinimalWordInfo.class, rb);
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
