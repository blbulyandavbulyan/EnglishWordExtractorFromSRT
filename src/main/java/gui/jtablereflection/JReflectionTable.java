package gui.jtablereflection;

import gui.jtablereflection.interfaces.ReflectionTableClassMark;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class JReflectionTable<T> extends JTable {
    private DefaultCellEditor arrayCellEditors[][];
    private HashMap<Integer, Integer> realColumnIndexToVirtualColumnIndex = new HashMap<>();
    private final JReflectionTable me;
    public JReflectionTable(T []addedContent, Class<T> storedType, Class<? super T> maxParentClass, ResourceBundle rb){
        super(new TableReflectionModel<T>(addedContent, storedType, maxParentClass, rb));
        this.me = this;
        TableReflectionModel<T> tableReflectionModel = (TableReflectionModel<T>) this.dataModel;
        HashMap<Integer, Field> arrayFields = new HashMap<>();
        Field[] columnFields = tableReflectionModel.columnFields;
        for (int i = 0, virtualColumnIndex = 0; i < columnFields.length; i++) {
            if(columnFields[i].getType().isArray())
                realColumnIndexToVirtualColumnIndex.put(i, virtualColumnIndex++);
        }
        if(!realColumnIndexToVirtualColumnIndex.isEmpty()){
            TableCellRenderer arrayCellRenderer = new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    DefaultCellEditor defaultCellEditor = arrayCellEditors[row][realColumnIndexToVirtualColumnIndex.get(column)];
                    return defaultCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
                }
            };
            arrayCellEditors = new DefaultCellEditor[addedContent.length][realColumnIndexToVirtualColumnIndex.size()];
            try {
                for (int i = 0; i < addedContent.length; i++) {
                    for (Map.Entry<Integer, Integer> entry : realColumnIndexToVirtualColumnIndex.entrySet()) {
                        Field arrayField = columnFields[entry.getKey()];
                        JComboBox<Object> jComboBox = new JComboBox<>();

                        Object[] objects = (Object[]) arrayField.get(addedContent[i]);

                        for (Object object : objects) {
                            jComboBox.addItem(object);
                        }
                        jComboBox.setSelectedIndex(0);
                        //Dimension comboBoxPreferredSize = jComboBox.getPreferredSize();
                        //comboBoxPreferredSize.height +=10;
                        //jComboBox.setPreferredSize(comboBoxPreferredSize);
                        arrayCellEditors[i][entry.getValue()] = new DefaultCellEditor(jComboBox);
                        this.setEditingColumn(entry.getKey());
                        //this.setRowHeight(i, comboBoxPreferredSize.height+50);
                        this.setDefaultRenderer(arrayField.getType(), arrayCellRenderer);
                    }
                }
            }
            catch (IllegalAccessException e){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if(realColumnIndexToVirtualColumnIndex.containsKey(column)){
            return arrayCellEditors[row][realColumnIndexToVirtualColumnIndex.get(column)];
        }
        else return super.getCellEditor(row, column);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return realColumnIndexToVirtualColumnIndex.containsKey(column);
    }

}
