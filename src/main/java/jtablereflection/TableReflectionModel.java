package jtablereflection;

import jtablereflection.annotations.ReflectionTable;
import jtablereflection.annotations.ReflectionTableColumn;
import jtablereflection.exceptions.AddedContentIsNullException;
import jtablereflection.exceptions.invalidprovidedclass.ClassMustBeAnnotatedReflectionTableException;
import jtablereflection.exceptions.invalidprovidedclass.ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException;
import jtablereflection.exceptions.invalidfield.InvalidColumnIndexException;
import jtablereflection.exceptions.invalidfield.RepeatableColumnIndexException;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Field;
import java.util.*;

public class TableReflectionModel<T> extends AbstractTableModel {
    final Class<T> storedObjectType;
    final T[] storedData;
    final String[] columnNames;
    final Field[] columnFields;
    public TableReflectionModel(T[] addedContent, Class<T> storedObjectType, ResourceBundle rb){
        if(addedContent == null)throw new AddedContentIsNullException();
        this.storedObjectType = storedObjectType;
        this.storedData = addedContent;
        ReflectionTable reflectionTable =storedObjectType.getAnnotation(ReflectionTable.class);
        if(reflectionTable != null){
            Field[] annotatedFields = Arrays.stream(storedObjectType.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(ReflectionTableColumn.class))
                    .toArray(Field[]::new);
            if(annotatedFields.length > 0){
                if(reflectionTable.columnOrder() == ReflectionTable.ColumnOrder.CUSTOM_ORDER){
                    Set<Integer> usedIndexes = new HashSet<>();
                    for (var annotatedField : annotatedFields) {
                        int columnIndex = annotatedField.getAnnotation(ReflectionTableColumn.class).preferredColumnIndex();
                        if(!(columnIndex < annotatedFields.length && columnIndex >= 0))throw new InvalidColumnIndexException();
                        else if(usedIndexes.contains(columnIndex))throw new RepeatableColumnIndexException();
                        else usedIndexes.add(columnIndex);
                    }
                    columnFields = Arrays.stream(annotatedFields).sorted(Comparator.comparingInt(value -> value.getAnnotation(ReflectionTableColumn.class).preferredColumnIndex())).toArray(Field[]::new);
                }
                else columnFields = annotatedFields;
                Arrays.stream(columnFields).forEach(field -> field.setAccessible(true));
                columnNames = new String[columnFields.length];
                for (int i = 0; i < columnNames.length; i++) {
                    columnNames[i] = rb.getString(columnFields[i].getAnnotation(ReflectionTableColumn.class).columnNamePropertiesKey());
                }

            }
            else throw new ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException(storedObjectType);

        }
        else throw new ClassMustBeAnnotatedReflectionTableException(storedObjectType);
    }
    @Override
    public int getRowCount() {
        return storedData.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnFields[columnIndex].getType();
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            return columnFields[columnIndex].get(storedData[rowIndex]);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public Class<T> getStoredObjectType() {
        return storedObjectType;
    }
}
