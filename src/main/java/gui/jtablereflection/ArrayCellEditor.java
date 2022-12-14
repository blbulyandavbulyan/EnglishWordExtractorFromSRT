package gui.jtablereflection;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;
import java.util.HashMap;

public class ArrayCellEditor implements TableCellEditor {
    private final  HashMap<RowAndColumnIndex, JComboBox> cellCoordinateToJComboBoxMapper;
    public ArrayCellEditor(HashMap<RowAndColumnIndex, JComboBox> cellCoordinateToJComboBoxMapper){
        this.cellCoordinateToJComboBoxMapper = cellCoordinateToJComboBoxMapper;
    }
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return null;
        //DefaultCellEditor
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    @Override
    public boolean stopCellEditing() {
        return false;
    }

    @Override
    public void cancelCellEditing() {

    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {

    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {

    }
}
