package jtablereflection;

import jtablereflection.interfaces.ReflectionTableClassMark;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class JReflectionTable<T extends ReflectionTableClassMark> extends JTable {
    protected HashMap<RowAndColumnIndex, JComboBox> arrayComboBoxes;
    public JReflectionTable(T []storedData, Class<T> storedType, ResourceBundle rb){
        super(new TableReflectionModel<T>(storedData, storedType, rb));
        TableReflectionModel<T> tableReflectionModel = (TableReflectionModel<T>) this.dataModel;
        HashMap<Integer, Field> arrayFields = new HashMap<>();
        for(int i = 0; i < tableReflectionModel.columnFields.length; i++){
            if(tableReflectionModel.columnFields[i].getType() == Object[].class){
                arrayFields.put(i, tableReflectionModel.columnFields[i]);
            }
        }
        if(arrayFields.size() > 0){
            arrayComboBoxes = new HashMap<>();
            for(int i = 0; i < this.getRowCount(); i++){
                for (Map.Entry<Integer, Field> entry : arrayFields.entrySet()) {
                    JComboBox<Object> jArrayComboBox = new JComboBox<>();
                    Object[] objects = (Object[]) this.getValueAt(i, entry.getKey());
                    for (Object object : objects) jArrayComboBox.addItem(object);
                    arrayComboBoxes.put(new RowAndColumnIndex(i, entry.getKey()), jArrayComboBox);
                }
            }
            for (int columnIndex : arrayFields.keySet()) {
                //todo add processing here to show JComboBox for displaying an array

            }
        }

    }
}
