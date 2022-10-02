package gui.windows;

import gui.dialogs.settings.SettingsDialog;
import gui.jtablereflection.JReflectionTable;
import workrers.ExtractExceptionWordsFromFilesWorker;
import workrers.ExtractWordsFromFilesWorker;
import jtabletocsvexporter.JTableToCSVFileExporter;
import programsettings.MainSettings;
import wordprocessing.wordinfo.MinimalWordInfo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class MainWindow extends JFrame {
    private final MainWindow me = this;
    private final JPanel contentJPanel;

    private final JProgressBar jProgressBar = new JProgressBar(0, 100);
    private final JTextField searchTextField = new JTextField();
    private final Clipboard systemClipBoard;
    private final ResourceBundle rb = ResourceBundle.getBundle("resources/locales/guitext");
    private final JButton  selectSrtFileButton = new JButton(rb.getString("mainWindow.button.selectSubTitleFiles"));
    private final JButton copyWords = new JButton(rb.getString("mainWindow.button.copyWords"));
    private final JButton selectExceptionWordsFileButton = new JButton(rb.getString("mainWindow.button.selectExceptionWordsFiles"));
    private final JButton extractWordsFromSrtFiles = new JButton(rb.getString("mainWindow.button.extractWords"));
    //создание меню "Файл"
    private final JMenu jFileMenu = new JMenu(rb.getString("mainWindow.menu.File"));
    private final JPopupMenu jWordsTableContextMenu = new JPopupMenu();
    private final JFileChooser jFileChooser = new JFileChooser();
    private final JScrollPane jScrollPane = new JScrollPane();
    private final JMenuItem jExportWordsToFile = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsFromThisFileToAnotherFile"));
    private final JMenuItem jExportWordsAndCountOfRepeatsToCSVTable = new JMenuItem(rb.getString("mainWindow.menu.File.exportWordsAndCountOfRepeatsToCSVTable"));

    //создание контекстного меню для таблицы со словами
    private final JMenuItem jExportSelectedWordsInFile = new JMenuItem(rb.getString("mainWindow.wordTable.menu.ExportSelectedWordsInFile"));
    private final JMenuItem jCopySelectedWords = new JMenuItem(rb.getString("mainWindow.wordTable.menu.CopySelectedWords"));
    private final JMenuItem jSettings = new JMenuItem(rb.getString("mainWindow.menu.File.settings"));
    private JTable displayTable;
    private TableRowSorter<TableModel> wordsSorter;
    private Set<File> exceptionWordsFiles = new HashSet<>();
    private File[] subTitleFiles;


    public MainWindow(){
        jProgressBar.setStringPainted(true);
        this.setTitle(rb.getString("mainWindow.title"));
        systemClipBoard = getSystemClipboard();
        contentJPanel = createRootJPanel();
        fillMenus();
        initControlElementsListeners();
        disableNotAvailableComponents();
        this.getContentPane().add(contentJPanel);
        this.setJMenuBar(createRootJMenuBar());
        this.pack();
        this.setMinimumSize(new Dimension(this.getWidth(), this.getHeight()+ 200));
        this.setPreferredSize(this.getMinimumSize());
    }
    private void fillMenus(){
        jFileMenu.add(jExportWordsToFile);
        jFileMenu.addSeparator();
        jFileMenu.add(jExportWordsAndCountOfRepeatsToCSVTable);
        jFileMenu.addSeparator();
        jFileMenu.add(jSettings);
        jWordsTableContextMenu.add(jExportSelectedWordsInFile);
        jWordsTableContextMenu.addSeparator();
        jWordsTableContextMenu.add(jCopySelectedWords);
    }
    private JMenuBar createRootJMenuBar(){
        JMenuBar jMenuBar = new JMenuBar();
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
                        File[] selectedExceptionFiles = jFileChooser.getSelectedFiles();
                        if(selectedExceptionFiles != null && selectedExceptionFiles.length > 0){
                            exceptionWordsFiles.addAll(List.of(selectedExceptionFiles));
                        }
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
            if(exceptionWordsFiles != null && exceptionWordsFiles.size() > 0)extractExceptionWordsFromFilesWorker.execute();
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

        jCopySelectedWords.addActionListener(e -> {
            StringBuilder copySelectedWordsBuilder = new StringBuilder();
            for (int selectedRowIndex : displayTable.getSelectedRows()) {
                copySelectedWordsBuilder.append(displayTable.getValueAt(selectedRowIndex, 0));
                copySelectedWordsBuilder.append("\n");
            }
            systemClipBoard.setContents(new StringSelection(copySelectedWordsBuilder.toString()), null);
        });
        jExportSelectedWordsInFile.addActionListener(e -> {
            jFileChooser.resetChoosableFileFilters();
            jFileChooser.setFileFilter(txtFileFilter);
            jFileChooser.setMultiSelectionEnabled(false);
            if(jFileChooser.showSaveDialog(me) == JFileChooser.APPROVE_OPTION){
                File fileForSaveWords = jFileChooser.getSelectedFile();
                try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileForSaveWords))) {
                    for (int selectedIndex : displayTable.getSelectedRows()) {
                        bufferedWriter.write(displayTable.getValueAt(selectedIndex, 0).toString());
                        bufferedWriter.newLine();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        jSettings.addActionListener(e ->{
            SettingsDialog settingsDialog = new SettingsDialog(exceptionWordsFiles);
            settingsDialog.setVisible(true);
            MainSettings mainSettings = settingsDialog.getMainSettings();
            if(settingsDialog.isOk()){
                Set<File> exceptionWordsFromSettings = mainSettings.getExceptionWordsFiles();
                if(exceptionWordsFromSettings != null && exceptionWordsFromSettings.size() > 0)
                    exceptionWordsFiles = exceptionWordsFromSettings;
            }
        });
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = searchTextField.getText();

                if (text.trim().length() == 0) {
                    wordsSorter.setRowFilter(null);
                } else {
                    wordsSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = searchTextField.getText();

                if (text.trim().length() == 0) {
                    wordsSorter.setRowFilter(null);
                } else {
                    wordsSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        {
            JPanel progressBarAndSearchFieldPanel = new JPanel();
            progressBarAndSearchFieldPanel.setLayout(new BoxLayout(progressBarAndSearchFieldPanel, BoxLayout.Y_AXIS));
            progressBarAndSearchFieldPanel.add(jProgressBar);
            progressBarAndSearchFieldPanel.add(searchTextField);
            contentPane.add(progressBarAndSearchFieldPanel, BorderLayout.NORTH);
        }

        return contentPane;
    }
    public void disableNotAvailableComponents(){
        extractWordsFromSrtFiles.setEnabled(false);
        copyWords.setEnabled(false);
        jExportWordsAndCountOfRepeatsToCSVTable.setEnabled(false);
        jExportWordsToFile.setEnabled(false);
        jExportSelectedWordsInFile.setEnabled(false);
        jCopySelectedWords.setEnabled(false);
        searchTextField.setEnabled(false);
    }
    private void fillAndDisplayTable(MinimalWordInfo[] minimalWordInfos){
        JTable jTable = new JReflectionTable<>(minimalWordInfos,  MinimalWordInfo.class, MinimalWordInfo.class, rb);
        jTable.setAutoCreateRowSorter(true);
        jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        displayTable = jTable;
        wordsSorter = new TableRowSorter<>(displayTable.getModel());
        displayTable.setRowSorter(wordsSorter);
        displayTable.getSelectionModel().addListSelectionListener(e -> setEnabledTableContextMenuItems(displayTable.getSelectedRowCount() > 0));
        displayTable.setComponentPopupMenu(jWordsTableContextMenu);
        jScrollPane.setViewportView(displayTable);
        contentJPanel.revalidate();
        contentJPanel.invalidate();
    }
    private void setEnabledTableContextMenuItems(boolean enabled){
        jExportSelectedWordsInFile.setEnabled(enabled);
        jCopySelectedWords.setEnabled(enabled);
    }
    private void setEnableControlElements(boolean enable){
        selectSrtFileButton.setEnabled(enable);
        copyWords.setEnabled(enable);
        selectExceptionWordsFileButton.setEnabled(enable);
        extractWordsFromSrtFiles.setEnabled(enable);
        jFileMenu.setEnabled(enable);
        jExportWordsAndCountOfRepeatsToCSVTable.setEnabled(enable);
        jExportWordsToFile.setEnabled(enable);
        searchTextField.setEnabled(enable);
    }
    private static Clipboard getSystemClipboard()
    {
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        return defaultToolkit.getSystemClipboard();
    }
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings","lcd");
        MainWindow mainWindow = new MainWindow();
        mainWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainWindow.setVisible(true);
    }
}
