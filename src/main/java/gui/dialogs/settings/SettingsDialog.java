package gui.dialogs.settings;

import programsettings.MainSettings;
import programsettings.exceptions.exceptionwordsfileprocessing.CanNotReadExceptionWordFileException;
import programsettings.exceptions.exceptionwordsfileprocessing.ExceptionWordsFileIsEmptyException;
import programsettings.exceptions.exceptionwordsfileprocessing.ExceptionWordsFileNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

public class SettingsDialog extends JDialog {
    private MainSettings mainSettings;
    private final DefaultListModel<File> exceptionFilesListModel = new DefaultListModel<>();
    private final JList<File> exceptionFilesJList = new JList<>(exceptionFilesListModel);
    private final JButton addFiles = new JButton("Добавить файлы");
    private final JButton deleteFiles = new JButton("Удалить файлы");
    private final JButton okButton = new JButton("Ок");
    private final JButton cancelButton = new JButton("Отмена");
    private final JFileChooser jFileChooser = new JFileChooser();
    private boolean ok = false;
    {
        okButton.setActionCommand("OK");
        cancelButton.setActionCommand("CANCEL");
    }
    private final SettingsDialog me = this;
    public SettingsDialog() {
        configureJFileChooser();
        initControlListeners();
        this.getContentPane().add(createRootPanel());
        this.pack();
        this.setMinimumSize(new Dimension(this.getWidth(), this.getHeight() + 150));
        this.setModal(true);
    }
    public SettingsDialog(File[] exceptionFiles){
        this();
        if(exceptionFiles != null && exceptionFiles.length > 0){
            Arrays.stream(exceptionFiles).forEach(file -> {
                if(!exceptionFilesListModel.contains(file))exceptionFilesListModel.addElement(file);
            });
        }
    }
    public MainSettings getMainSettings() {
        return mainSettings;
    }
    private void configureJFileChooser(){
        jFileChooser.setDialogTitle("Выберите файлы с исключаемыми словами");
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setFileFilter(new FileNameExtensionFilter("plain text", "txt"));
        jFileChooser.setMultiSelectionEnabled(true);
    }
    private JPanel createRootPanel(){

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout());
        {
            JPanel okCancelButtonPanel = new JPanel();
            okCancelButtonPanel.add(okButton);
            okCancelButtonPanel.add(addFiles);
            okCancelButtonPanel.add(deleteFiles);
            okCancelButtonPanel.add(cancelButton);
            rootPanel.add(okCancelButtonPanel, BorderLayout.SOUTH);
        }
        rootPanel.add(new JScrollPane(exceptionFilesJList), BorderLayout.CENTER);
        return rootPanel;
    }
    private void askToDeleteInvalidFile(File fileForDelete){
        if(JOptionPane.showConfirmDialog(me, "Желаете убрать данный файл из списка?", "Запрос на удаление файла из списка", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            exceptionFilesListModel.removeElement(fileForDelete);
        }
    }
    private void initControlListeners(){
        ActionListener okCancelActionListener = e -> {
            switch (e.getActionCommand()){
                case "OK"->{
                    try {
                        if(exceptionFilesListModel.getSize() > 0){
                            mainSettings = new MainSettings(IntStream.range(0, exceptionFilesListModel.getSize())
                                    .mapToObj(exceptionFilesListModel::getElementAt)
                                    .toArray(File[]::new));
                            ok = true;
                            dispose();
                        }
                    }
                    catch (ExceptionWordsFileNotFoundException ex){
                        JOptionPane.showMessageDialog(me, "Файл %s не существует!".formatted(ex.getProvidedFile()), "Ошибка при обращении к файлу!", JOptionPane.ERROR_MESSAGE);
                        askToDeleteInvalidFile(ex.getProvidedFile());
                    }
                    catch (ExceptionWordsFileIsEmptyException ex){
                        JOptionPane.showMessageDialog(me, "Файл %s пуст!".formatted(ex.getProvidedFile()), "Ошибка при обращении к файлу!", JOptionPane.ERROR_MESSAGE);
                        askToDeleteInvalidFile(ex.getProvidedFile());
                    }
                    catch (CanNotReadExceptionWordFileException ex){
                        JOptionPane.showMessageDialog(me, "Файл %s не доступен для чтения!".formatted(ex.getProvidedFile()), "Ошибка при обращении к файлу!", JOptionPane.ERROR_MESSAGE);
                        askToDeleteInvalidFile(ex.getProvidedFile());
                    }
                }
                case "CANCEL"-> dispose();
                default -> throw new IllegalArgumentException("Invalid action command! This action listener can process only OK, CANCEL commands!");
            }
        };
        okButton.addActionListener(okCancelActionListener);
        cancelButton.addActionListener(okCancelActionListener);
        addFiles.addActionListener(l->{
            if(jFileChooser.showOpenDialog(me) == JFileChooser.APPROVE_OPTION){
                File[] selectedFiles = jFileChooser.getSelectedFiles();
                Arrays.stream(selectedFiles).forEach(file -> {
                    if(!exceptionFilesListModel.contains(file))exceptionFilesListModel.addElement(file);
                });
            }
        });
        deleteFiles.addActionListener(l->{
            for (File selectedFile : exceptionFilesJList.getSelectedValuesList()) {
                exceptionFilesListModel.removeElement(selectedFile);
            }
        });
        exceptionFilesJList.addListSelectionListener(e -> deleteFiles.setEnabled(exceptionFilesJList.getSelectedIndices().length > 0));
    }
    public boolean isOk(){
        return ok;
    }
    public static void main(String[] args){
        System.setProperty("awt.useSystemAAFontSettings","lcd");
        SettingsDialog settingsDialog = new SettingsDialog();
        //settingsDialog.setDefaultCloseOperation(D);
        settingsDialog.setVisible(true);
    }
}
