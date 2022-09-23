package gui.windows;

import gui.jtablereflection.JReflectionTable;
import gui.workrers.ExtractExceptionWordsFromFilesWorker;
import gui.workrers.ExtractWordsFromFilesWorker;
import jtabletocsvexporter.JTableToCSVFileExporter;
import wordprocessing.wordinfo.MinimalWordInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class MainWindow extends JFrame {
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
    private final JScrollPane jScrollPane;
    private final JMenuItem jExportWordsToFile;
    private final JMenuItem jExportWordsAndCountOfRepeatsToCSVTable;
    private JTable displayTable;
    private File[] exceptionWordsFiles;
    private File[] subTitleFiles;

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
        jExportWordsToFile = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsFromThisFileToAnotherFile"));
        jExportWordsAndCountOfRepeatsToCSVTable = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsAndCountOfRepeatsToCSVTable"));
        this.setTitle(rb.getString("mainWindow.title"));
        systemClipBoard = getSystemClipboard();
        contentJPanel = createRootJPanel();
        initControlElementsListeners();
        disableNotAvailableComponents();
        this.getContentPane().add(contentJPanel);
        this.setJMenuBar(createRootJMenuBar());
        this.pack();
    }
    private JMenuBar createRootJMenuBar(){
        JMenuBar jMenuBar = new JMenuBar();
        {

            jFileMenu.add(jExportWordsToFile);
            jFileMenu.addSeparator();
            jFileMenu.add(jExportWordsAndCountOfRepeatsToCSVTable);
            jFileMenu.addSeparator();
        }
        jMenuBar.add(jFileMenu);
        return jMenuBar;
    }
    private void initControlElementsListeners(){
        FileNameExtensionFilter subRipAndPlainTextFileFilter = new FileNameExtensionFilter("SubRip and Plain text", "srt", "txt");
        FileNameExtensionFilter txtFileFilter = new FileNameExtensionFilter("Plaint text", "txt");
        File emptyFileForClearJFileChooserSelection = new File("");
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
                    jFileChooser.resetChoosableFileFilters();
                    jFileChooser.setFileFilter(txtFileFilter);
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
                                    fillAndDisplayTable(minimalWordInfos);
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
        jExportWordsToFile.addActionListener(e -> {
            jFileChooser.resetChoosableFileFilters();
            jFileChooser.setFileFilter(txtFileFilter);
            jFileChooser.setMultiSelectionEnabled(false);
            if(jFileChooser.showSaveDialog(me) == JFileChooser.APPROVE_OPTION){
                File fileForSaveWords = jFileChooser.getSelectedFile();
                try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileForSaveWords))) {
                    for (int i = 0; i < displayTable.getRowCount(); i++) {
                        bufferedWriter.write(displayTable.getValueAt(i, 0).toString());
                        bufferedWriter.newLine();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }

        });
        FileNameExtensionFilter csvFileFilter = new FileNameExtensionFilter("Text CSV", ".csv");

        jExportWordsAndCountOfRepeatsToCSVTable.addActionListener(e -> {
            jFileChooser.setFileFilter(csvFileFilter);
            jFileChooser.setMultiSelectionEnabled(false);
            jFileChooser.setSelectedFile(emptyFileForClearJFileChooserSelection);
            if(jFileChooser.showSaveDialog(me) == JFileChooser.APPROVE_OPTION){
                File fileForSavingTable = jFileChooser.getSelectedFile();
                try {
                    JTableToCSVFileExporter.exportDataToFile(displayTable, fileForSavingTable);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
    private JPanel createRootJPanel(){
        JPanel contentPane = new JPanel();
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
    public void disableNotAvailableComponents(){
        extractWordsFromSrtFiles.setEnabled(false);
        copyWords.setEnabled(false);
        jExportWordsAndCountOfRepeatsToCSVTable.setEnabled(false);
        jExportWordsToFile.setEnabled(false);
    }
    private void fillAndDisplayTable(MinimalWordInfo[] minimalWordInfos){
        JTable jTable = new JReflectionTable<>(minimalWordInfos,  MinimalWordInfo.class, MinimalWordInfo.class, rb);
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
        jExportWordsAndCountOfRepeatsToCSVTable.setEnabled(enable);
        jExportWordsToFile.setEnabled(enable);
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
