package gui.windows;

import gui.jtablereflection.JReflectionTable;
import gui.workrers.ExtractExceptionWordsFromFilesWorker;
import gui.workrers.ExtractWordsFromFilesWorker;
import gui.workrers.GetWordInfoFromMinimalWordInfoWorker;
import translategetter.MicrosoftTranslate;
import translategetter.Translate;
import translategetter.TranslateProvider;
import wordprocessing.wordinfo.MinimalWordInfo;
import wordprocessing.wordinfo.WordInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class MainWindow extends JFrame {
    private final HashMap<Translate.PartOfSpeech, String> localizedPartOfSpeechesNames = new HashMap<>();
    private final static String partOfSpeechRbPrefix = "mainWindow.table.cellValues.partOfSpeech";
    private final MainWindow me;
    private final JPanel contentJPanel;

    //private final JTabbedPane subTitleFilesTabPane;
    private final JProgressBar jProgressBar;
    private final Clipboard systemClipBoard;
    private final ResourceBundle rb;
    private final JButton  selectSrtFileButton;
    private final JButton copyWords;
    private final JButton selectExceptionWordsFileButton;
    private final JButton extractWordsFromSrtFiles;
    private final JMenu jFileMenu;
    private final JFileChooser jFileChooser = new JFileChooser();
    private boolean needTranslate = false;
    private final JScrollPane jScrollPane;
    private JTable displayTable;
    private File[] exceptionWordsFiles;
    private File[] subTitleFiles;
    private final TranslateProvider translateProvider;

    public MainWindow(){
        jScrollPane = new JScrollPane();
        me = this;
        rb = ResourceBundle.getBundle("locales/guitext");
        jProgressBar = new JProgressBar(0, 100);
        jProgressBar.setStringPainted(true);

        selectSrtFileButton = new JButton(rb.getString("mainWindow.button.selectSubTitleFiles"));
        copyWords = new JButton(rb.getString("mainWindow.button.copyWords"));
        selectExceptionWordsFileButton = new JButton(rb.getString("mainWindow.button.selectExceptionWordsFiles"));
        extractWordsFromSrtFiles = new JButton(rb.getString("mainWindow.button.extractWords"));
        jFileMenu = new JMenu(rb.getString("mainWindow.menu.File"));
        this.setTitle(rb.getString("mainWindow.title"));
        for (Translate.PartOfSpeech partOfSpeech : Translate.PartOfSpeech.values()) {
            localizedPartOfSpeechesNames.put(partOfSpeech, rb.getString(String.format("%s.%s", partOfSpeechRbPrefix, partOfSpeech.name())));
        }
        translateProvider = new MicrosoftTranslate("739f0d0146msh7cc406a9f7c0d10p1f79c6jsn56e503cd2739");
        systemClipBoard = getSystemClipboard();
        contentJPanel = createRootJPanel();
        initButtonListeners();
        this.getContentPane().add(contentJPanel);
        this.setJMenuBar(createRootJMenuBar());
        this.pack();
    }
    private JMenuBar createRootJMenuBar(){
        JMenuBar jMenuBar = new JMenuBar();
        {
            JMenuItem jExportWordsFromThisFileToAnotherFile = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsFromThisFileToAnotherFile"));
            JMenuItem jExportWordsFromAllFilesToOneFile = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsFromAllFilesToAnotherFile"));
            JCheckBoxMenuItem jEnableTranslateWhenImportWords = new JCheckBoxMenuItem(rb.getString("mainWindow.menu.File.enableTranslateWhenImportFile"));
            jEnableTranslateWhenImportWords.addItemListener(e -> needTranslate = e.getStateChange() == ItemEvent.SELECTED);
            jFileMenu.add(jExportWordsFromThisFileToAnotherFile);
            jFileMenu.addSeparator();
            jFileMenu.add(jExportWordsFromAllFilesToOneFile);
            jFileMenu.addSeparator();
            jFileMenu.add(jEnableTranslateWhenImportWords);
        }
        jMenuBar.add(jFileMenu);
        return jMenuBar;
    }
    private void initButtonListeners(){
        FileNameExtensionFilter subRipAndPlainTextFileFilter = new FileNameExtensionFilter("SubRip and Plain text", "srt", "txt");
        FileNameExtensionFilter exceptionWordsFileFilter = new FileNameExtensionFilter("Plaint text", "txt");
        selectSrtFileButton.addActionListener(
                (l)->{
                    jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jFileChooser.setMultiSelectionEnabled(true);
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
                    jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jFileChooser.setMultiSelectionEnabled(true);
                    jFileChooser.setDialogTitle("Выберите файл исключаемых слов");
                    jFileChooser.setFileFilter(exceptionWordsFileFilter);
                    int result = jFileChooser.showOpenDialog(me);
                    if(result == JFileChooser.APPROVE_OPTION){
                        exceptionWordsFiles = jFileChooser.getSelectedFiles();
                    }
                }
        );
        extractWordsFromSrtFiles.addActionListener(e -> {
            setEnableControlElements(false);
            //fixme this is critical, function in current realization will not work
            Predicate<Character> englishWordPredicate = (character -> (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z'));
            ExtractExceptionWordsFromFilesWorker extractExceptionWordsFromFilesWorker = new ExtractExceptionWordsFromFilesWorker(exceptionWordsFiles, englishWordPredicate);
            ExtractWordsFromFilesWorker extractWordsFromFilesWorker = new ExtractWordsFromFilesWorker(subTitleFiles, null, englishWordPredicate);
            GetWordInfoFromMinimalWordInfoWorker getWordInfoFromMinimalWordInfoWorker = new GetWordInfoFromMinimalWordInfoWorker(null, translateProvider, localizedPartOfSpeechesNames);
            PropertyChangeListener propertyChangeListener = evt -> {
                Object propertyValue = evt.getNewValue();
                Object source = evt.getSource();
                switch (evt.getPropertyName()){
                    case "progress" -> jProgressBar.setValue((int)propertyValue);
                    case "state" ->{
                        if (propertyValue == SwingWorker.StateValue.STARTED) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            if(source == extractExceptionWordsFromFilesWorker) {
                                jProgressBar.setString(rb.getString("mainWindow.progressBar.readingExceptionFiles"));
                                jProgressBar.setValue(0);
                            }
                            else if(source == extractWordsFromFilesWorker) {
                                jProgressBar.setString(rb.getString("mainWindow.progressBar.readingSrtFiles"));
                                jProgressBar.setValue(0);
                            }
                            else if(source == getWordInfoFromMinimalWordInfoWorker)jProgressBar.setString(rb.getString("mainWindow.progressBar.gettingTranslates"));
                        }
                        else if(propertyValue == SwingWorker.StateValue.DONE){
                            try{
                                if(source == extractExceptionWordsFromFilesWorker) {
                                    extractWordsFromFilesWorker.setExceptionWords(extractExceptionWordsFromFilesWorker.get());
                                    extractWordsFromFilesWorker.execute();
                                }
                                else if(source == extractWordsFromFilesWorker){
                                    setCursor(Cursor.getDefaultCursor());
                                    MinimalWordInfo[] minimalWordInfos = extractWordsFromFilesWorker.get();
                                    if(needTranslate){
                                        getWordInfoFromMinimalWordInfoWorker.setMinimalWordInfos(minimalWordInfos);
                                        getWordInfoFromMinimalWordInfoWorker.execute();
                                    }
                                    else {
                                        fillAndDisplayTable(minimalWordInfos);
                                        jProgressBar.setString(rb.getString("mainWindow.progressBar.completed"));
                                        setEnableControlElements(true);
                                    }
                                }
                                else if(source == getWordInfoFromMinimalWordInfoWorker){
                                    fillAndDisplayTable(getWordInfoFromMinimalWordInfoWorker.get());
                                    jProgressBar.setString(rb.getString("mainWindow.progressBar.completed"));
                                    setEnableControlElements(true);
                                }
                            }
                            catch (ExecutionException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }

                        }
                    }
                }
            };
            extractExceptionWordsFromFilesWorker.addPropertyChangeListener(propertyChangeListener);
            extractWordsFromFilesWorker.addPropertyChangeListener(propertyChangeListener);
            getWordInfoFromMinimalWordInfoWorker.addPropertyChangeListener(propertyChangeListener);
            if(exceptionWordsFiles != null)extractExceptionWordsFromFilesWorker.execute();
            else extractWordsFromFilesWorker.execute();
        });
        copyWords.addActionListener(e -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < displayTable.getRowCount(); i++){
                stringBuilder.append(displayTable.getValueAt(i, 0));
                stringBuilder.append("\n");
            }
            systemClipBoard.setContents(new StringSelection(stringBuilder.toString()), null);
        });
    }
    private JPanel createRootJPanel(){
        JPanel contentPane = new JPanel();
        extractWordsFromSrtFiles.setEnabled(false);
        copyWords.setEnabled(false);
        contentPane.setLayout(new BorderLayout());
        JToolBar jToolBar = new JToolBar();
        jToolBar.setFloatable(false);
        jToolBar.add(selectSrtFileButton);
        jToolBar.add(selectExceptionWordsFileButton);
        jToolBar.add(extractWordsFromSrtFiles);
        jToolBar.add(copyWords);
        contentPane.add(jScrollPane, BorderLayout.CENTER);
        contentPane.add(jToolBar, BorderLayout.SOUTH);
        contentPane.add(jProgressBar, BorderLayout.NORTH);
        return contentPane;
    }
    private void fillAndDisplayTable(MinimalWordInfo[] minimalWordInfos){
        JTable jTable = new JReflectionTable<>(minimalWordInfos,  MinimalWordInfo.class, MinimalWordInfo.class, rb);
        jTable.setAutoCreateRowSorter(true);
        replaceDisplayJTable(jTable);
    }
    private void fillAndDisplayTable(WordInfo[] wordInfos){
        JTable jTable = new JReflectionTable<>(wordInfos,  WordInfo.class, MinimalWordInfo.class, rb);
        jTable.setAutoCreateRowSorter(true);
        replaceDisplayJTable(jTable);
    }
    private void replaceDisplayJTable(JTable newJTable){
        displayTable = newJTable;
        jScrollPane.setViewportView(displayTable);
        contentJPanel.revalidate();
        contentJPanel.invalidate();
    }
    private void setEnableControlElements(boolean enable){
        selectSrtFileButton.setEnabled(enable);
        copyWords.setEnabled(enable);
        selectExceptionWordsFileButton.setEnabled(enable);
        extractWordsFromSrtFiles.setEnabled(enable);
        jFileMenu.setEnabled(enable);
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
