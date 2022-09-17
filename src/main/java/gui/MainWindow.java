package gui;

import jtablereflection.TableReflectionModel;
import translategetter.MicrosoftTranslate;
import wordextractor.MinimalWordInfo;
import wordextractor.WordExtractor;
import wordinfogenerator.WordInfo;
import wordinfogenerator.WordInfoGenerator;

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
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class MainWindow extends JFrame {

    private final MainWindow me;
    private File[] exceptionWordsFiles;
    private File[] subTitleFiles;
    private JTabbedPane subTitleFilesTabPane;
    private  JTable[] tables;
    private final Clipboard systemClipBoard;
    private final ResourceBundle rb;
    private final WordInfoGenerator wordInfoGenerator;
    private boolean needTranslate = false;
    public MainWindow(){
        rb = ResourceBundle.getBundle("locales/guitext");
        wordInfoGenerator = new WordInfoGenerator(new MicrosoftTranslate("739f0d0146msh7cc406a9f7c0d10p1f79c6jsn56e503cd2739"), "mainWindow.table.cellValues.partOfSpeech", rb);
        me = this;
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
                        var minimalWordInfos = new WordExtractor(subTitleFiles[i], englishWordPredicate, exceptionWords).getWordsAndCountRepeats();
                        JTable wordsTable = new Callable<JTable>(){
                            @Override
                            public JTable call(){
                                if(needTranslate){
                                    //return fillAndCreateTable()
                                    //fixme
                                    return null;
                                }
                                else {
                                    return fillAndCreateTable(minimalWordInfos, MinimalWordInfo.class);
                                }
                            }
                        }.call();

                        subTitleFilesTabPane.addTab(subTitleFiles[i].getName(), new JScrollPane(wordsTable));
                        tables[i] = wordsTable;
                    }
                    copyWordsFromThisFile.setEnabled(true);
                    copyWordsFromAllFiles.setEnabled(true);
                }


            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (Exception ex) {
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
    private <T extends MinimalWordInfo> JTable fillAndCreateTable(T []wordInfos, Class<T> wordInfoType) {
        JTable jTable = new JTable(new TableReflectionModel<>(wordInfos, wordInfoType, rb));
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
