package gui.jtablereflection;

import gui.jtablereflection.annotations.ReflectionTable;
import gui.jtablereflection.annotations.ReflectionTableColumn;
import gui.jtablereflection.exceptions.ReflectionTableException;
import gui.jtablereflection.exceptions.structure.invalidclassannotation.SomeParentDoesNotHaveReflectionTableAnnotationException;
import gui.jtablereflection.exceptions.structure.invalidfieldannotation.SomeParentClassDoesNotHaveFieldsAnnotatedReflectionTableColumn;
import gui.jtablereflection.exceptions.AddedContentIsNullException;
import gui.jtablereflection.exceptions.structure.invalidclassannotation.ClassMustBeAnnotatedReflectionTableException;
import gui.jtablereflection.exceptions.structure.invalidclassannotation.ParentClassHasNoIgnoreHeirarchicalColumnOrderParameterException;
import gui.jtablereflection.exceptions.structure.invalidfieldannotation.ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

public class TableReflectionModel<T> extends AbstractTableModel{
    private final Class<T> storedObjectType;
    private final T[] storedData;
    final String[] columnNames;
    final Field[] columnFields;
    private Field[] getReflectionTableFields(Class<T> startClass, Class<? super T> stopClass) throws ReflectionTableException {
        List<Field[]> fieldsList = new LinkedList<>();
        for(Class<? super T> processedClass = startClass;; processedClass = processedClass.getSuperclass()){
            if(!processedClass.isAnnotationPresent(ReflectionTable.class))
                throw new SomeParentDoesNotHaveReflectionTableAnnotationException(processedClass);
            if(processedClass != startClass && processedClass.getAnnotation(ReflectionTable.class).hierarchicalColumnOrder() != ReflectionTable.HierarchicalColumnOrder.IGNORE_THIS_PARAMETER)
                throw new ParentClassHasNoIgnoreHeirarchicalColumnOrderParameterException(processedClass);
            Field[] annotatedFields = Arrays.stream(processedClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(ReflectionTableColumn.class))
                    .toArray(Field[]::new);
            if(annotatedFields.length  > 0){
                if(processedClass.getAnnotation(ReflectionTable.class).columnOrderInClass() == ReflectionTable.ColumnOrderInClass.CUSTOM_ORDER){
                    annotatedFields = Arrays.stream(annotatedFields).sorted(Comparator.comparingInt(field -> field.getAnnotation(ReflectionTableColumn.class).preferredColumnIndex())).toArray(Field[]::new);
                }
                for (Field annotatedField : annotatedFields) {
                    annotatedField.setAccessible(true);
                }
                fieldsList.add(annotatedFields);
            }
            else if(processedClass == startClass)throw new ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException(startClass);
            else throw new SomeParentClassDoesNotHaveFieldsAnnotatedReflectionTableColumn(processedClass);
            if(processedClass == stopClass || stopClass == null)break;
        }
        List<Field> result = new LinkedList<>();
        boolean isOrderFromChildToParent =
                startClass.getAnnotation(ReflectionTable.class).hierarchicalColumnOrder() == ReflectionTable.HierarchicalColumnOrder.FROM_CHILD_TO_PARENT;
        ListIterator<Field[]> fieldsListIterator = fieldsList.listIterator(isOrderFromChildToParent ? fieldsList.size(): 0);
        while ((isOrderFromChildToParent ? fieldsListIterator.hasPrevious() : fieldsListIterator.hasNext())){
            result.addAll(Arrays.asList(
                    isOrderFromChildToParent ? fieldsListIterator.previous() : fieldsListIterator.next())
            );
        }
        return result.toArray(Field[]::new);
    }
    public TableReflectionModel(T[] addedContent, Class<T> storedObjectType, Class<? super T> maxStoreObjectTypeParent, ResourceBundle rb){
        if(addedContent == null)throw new AddedContentIsNullException();
        this.storedObjectType = storedObjectType;
        this.storedData = addedContent;
        ReflectionTable reflectionTable =storedObjectType.getAnnotation(ReflectionTable.class);
        if(reflectionTable != null){
            columnFields = getReflectionTableFields(storedObjectType, maxStoreObjectTypeParent);
            columnNames = new String[columnFields.length];
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = rb.getString(columnFields[i].getAnnotation(ReflectionTableColumn.class).columnNamePropertiesKey());
            }
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

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return super.isCellEditable(rowIndex, columnIndex);
    }

    public Class<T> getStoredObjectType() {
        return storedObjectType;
    }

}
