package jcombo_00150;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboPopup;

import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.JComboBox;

/**
 *
 * @author Lenovo
 */

public class Jcombo_00150 extends JComboBox{
    public static enum Alignment {

        LEFT, RIGHT
    }
    public List<List<? extends Object>> tableData;
    public String[] columnNames;
    public int[] columnWidths;
    public int displayColumn;
    public int tabelwidth;
    public int height;
    public int height_total=0;
    public int width_total=0;
    public Alignment popupAlignment = Alignment.LEFT;

    /**
     * Construct a TableComboBox object
     */
    
    public void isi(ResultSet rs) throws SQLException,ClassNotFoundException{
                
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        String[] judul=new String[columnCount];
        int[] width=new int[columnCount];
        List<List<?>> c = new ArrayList<>();
        String[] isi = new String[columnCount];
         
        for (int column = 1; column <= columnCount; column++) {
                judul[column-1]= metaData.getColumnName(column);
                
        }
        
        
        while (rs.next()) {
            
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                isi[columnIndex-1]=(rs.getString(columnIndex));
                if(width[columnIndex-1]<rs.getString(columnIndex).length()){
                        width[columnIndex-1]=(rs.getString(columnIndex).length()+2)*8;
                    }
                }
            c.add(new ArrayList<>(Arrays.asList(isi)));
            
            this.height_total+=50;
        }
               
        for(int i=0;i<columnCount;i++){
            this.width_total+=width[i]+10;
        }
        
        
            
        this.columnNames = judul;
        this.columnWidths = width;
        
        
        this.displayColumn = 0;
        setTableData(c);
        setPopupAlignment(Alignment.LEFT);
        setUI(new TableComboBoxUI());
        setEditable(true);
        setSelectedIndex(-1);   
    }
	
    /**
     * Set the type of alignment for the popup table
     */
    public void setPopupAlignment(Alignment alignment) {
        popupAlignment = alignment;
    }

    /**
     * Populate the combobox and drop-down table with the supplied data. If the
     * supplied List is non-null and non-empty, it is assumed that the data is a
     * List of Lists to be used for the drop-down table. The combobox is also
     * populated with the column data from the column defined by
     * <code>displayColumn</code>.
     */
    public void setTableData(List<List<? extends Object>> tableData) {
        this.tableData = (tableData == null
                ? new ArrayList<List<? extends Object>>() : tableData);

        // even though the incoming data is for the table, we must also
        // populate the combobox's data, so first clear the previous list.
        removeAllItems();

        // then load the combobox with data from the appropriate column
        Iterator<List<? extends Object>> iter = this.tableData.iterator();
        while (iter.hasNext()) {
            List<? extends Object> rowData = iter.next();
            addItem(rowData.get(displayColumn));
        }
    }

    public List<? extends Object> getSelectedRow() {
        return tableData.get(getSelectedIndex());
    }

    public List<? extends Object> getRowAt(int row) {
        return tableData.get(row);
    }

    /**
     * The handler for the combobox's components
     */
    private class TableComboBoxUI extends MetalComboBoxUI {

        /**
         * Create a popup component for the ComboBox
         */
        @Override
        protected ComboPopup createPopup() {
            return new TableComboPopup(comboBox, this);
        }

        /**
         * Return the JList component
         */
        public JList getList() {
            return listBox;
        }
    }

    /**
     * The drop-down of the combobox, which is a JTable instead of a JList.
     */
    private class TableComboPopup extends BasicComboPopup implements ListSelectionListener, ItemListener {

        private final JTable table;
        private TableComboBoxUI comboBoxUI;
        private PopupTableModel tableModel;
        private JScrollPane scroll;
//    private JList list = new JList();
//    private ListSelectionListener selectionListener;
//    private ItemListener itemListener;

        /**
         * Construct a popup component that's a table
         */
        public TableComboPopup(JComboBox combo, TableComboBoxUI ui) {
            super(combo);
            this.comboBoxUI = ui;

            tableModel = new PopupTableModel();
            table = new JTable(tableModel);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.getTableHeader().setReorderingAllowed(false);

            TableColumnModel tableColumnModel = table.getColumnModel();
            tableColumnModel.setColumnSelectionAllowed(false);

            for (int index = 0; index < table.getColumnCount(); index++) {
                TableColumn tableColumn = tableColumnModel.getColumn(index);
                tableColumn.setPreferredWidth(columnWidths[index]);
            }

            scroll = new JScrollPane(table);
            scroll.setHorizontalScrollBarPolicy(
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            ListSelectionModel selectionModel = table.getSelectionModel();
            selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectionModel.addListSelectionListener(this);
            combo.addItemListener(this);

            table.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent event) {
                    Point p = event.getPoint();
                    int row = table.rowAtPoint(p);

                    comboBox.setSelectedIndex(row);
                    hide();
                }
            });

            table.setBackground(UIManager.getColor("ComboBox.listBackground"));
            table.setForeground(UIManager.getColor("ComboBox.listForeground"));
        }

        /**
         * This method is overridden from BasicComboPopup
         */
        @Override
        public void show() {
            if (isEnabled()) {
                super.removeAll();

//                int scrollWidth = table.getPreferredSize().width+((Integer) UIManager.get("ScrollBar.width")).intValue()+1;
//                int scrollHeight = comboBoxUI.getList().getPreferredScrollableViewportSize().height;
                    int scrollWidth = width_total;
                    int scrollHeight = height_total;
                    scroll.setPreferredSize(new Dimension(scrollWidth, scrollHeight));

                super.add(scroll);

                ListSelectionModel selectionModel = table.getSelectionModel();
                selectionModel.removeListSelectionListener(this);
                selectRow();
                selectionModel.addListSelectionListener(this);

                int scrollX = 0;
                int scrollY = comboBox.getBounds().height;

                if (popupAlignment == Alignment.RIGHT) {
                    scrollX = comboBox.getBounds().width - scrollWidth;
                }

                show(comboBox, scrollX, scrollY);
            }
        }

        /**
         * Implemention of ListSelectionListener
         */
        @Override
        public void valueChanged(ListSelectionEvent event) {
            comboBox.setSelectedIndex(table.getSelectedRow());
        }

        /**
         * Implemention of ItemListener
         */
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() != ItemEvent.DESELECTED) {
                ListSelectionModel selectionModel = table.getSelectionModel();
                selectionModel.removeListSelectionListener(this);
                selectRow();
                selectionModel.addListSelectionListener(this);
            }
        }

        /**
         * Sync the selected row of the table with the selected row of the
         * combo.
         */
        private void selectRow() {
            int index = comboBox.getSelectedIndex();

            if (index != -1) {
                table.setRowSelectionInterval(index, index);
                table.scrollRectToVisible(table.getCellRect(index, 0, true));
            }
        }
    }

    /**
     * A model for the popup table's data
     */
    private class PopupTableModel extends AbstractTableModel {

        /**
         * Return the # of columns in the drop-down table
         */
        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        /**
         * Return the # of rows in the drop-down table
         */
        @Override
        public int getRowCount() {
            return tableData == null ? 0 : tableData.size();
        }

        /**
         * Determine the value for a given cell
         */
        @Override
        public Object getValueAt(int row, int col) {
            if (tableData == null || tableData.size() == 0) {
                return "";
            }

            return tableData.get(row).get(col);
        }

        /**
         * All cells in the drop-down table are uneditable
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        /**
         * Pull the column names out of the tableInfo object for the header
         */
        @Override
        public String getColumnName(int column) {
            String columnName = null;

            if (column >= 0 && column < columnNames.length) {
                columnName = columnNames[column].toString();
            }

            return (columnName == null) ? super.getColumnName(column) : columnName;
        }
    }
}

class ComboKeyHandler extends KeyAdapter {

    private final Jcombo_00150 comboBox;
    //  private final Vector<String> list = new Vector<String>();
    private final List<List<?>> list = new ArrayList<>();

    public ComboKeyHandler(Jcombo_00150 combo) {
        this.comboBox = combo;
        for (int i = 0; i < comboBox.getModel().getSize(); i++) {


            List<? extends Object> rowData = combo.getRowAt(i);
            // name.setText(rowData.get(1).toString());
            //  capital.setText(rowData.get(2).toString());
            list.add(rowData);
            // list.addElement((String) comboBox.getItemAt(i));
        }
    }
    private boolean shouldHide = false;

    @Override
    public void keyTyped(final KeyEvent e) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                String text = ((JTextField) e.getSource()).getText();
                System.out.println(text);
                if (text.length() == 0) {
                    setSuggestionModel(comboBox, list, "");
                    comboBox.hidePopup();
                } else {
                    List<List<?>> m = getSuggestedModel(list, text);
                    if (m.isEmpty() || shouldHide) {
                        comboBox.hidePopup();
                    } else {
                        setSuggestionModel(comboBox, m, text);
                        comboBox.showPopup();
                    }
                }
            }
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        JTextField textField = (JTextField) e.getSource();
        String text = textField.getText();
        shouldHide = false;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:


                for (Iterator<List<?>> it = list.iterator(); it.hasNext();) {
                    if (!it.next().contains("How")) {
                        it.remove(); // NOTE: Iterator's remove method, not ArrayList's, is used.
                    }
                    return;
                }

                /*
                 * for (String s : list { if (s.startsWith(text)) {
                 * textField.setText(s); return; } }
                 *
                 */

                break;
            case KeyEvent.VK_ENTER:
                if (!list.contains(text)) {
                    list.add(new ArrayList<>(Arrays.asList(text, "a", "a")));

                    //setSuggestionModel(comboBox, new DefaultComboBoxModel(list), text);
                    setSuggestionModel(comboBox, getSuggestedModel(list, text), text);
                }
                shouldHide = false;
                break;
            case KeyEvent.VK_ESCAPE:
                shouldHide = true;
                break;
            default:
                break;
        }
    }

    private static void setSuggestionModel(Jcombo_00150 comboBox, List<List<?>> newList, String str) {
        comboBox.setTableData(newList);
        comboBox.setSelectedIndex(-1);
        ((JTextField) comboBox.getEditor().getEditorComponent()).setText(str);
    }

    private static List<List<?>> getSuggestedModel(List<List<?>> list, String text) {
        List<List<?>> m = new ArrayList<>();


        for (Iterator<List<?>> it = list.iterator(); it.hasNext();) {
            if (!it.next().contains(text)) {
                it.remove(); // NOTE: Iterator's remove method, not ArrayList's, is used.
            }
            m = (List<List<?>>) it;
        }
        return m;

        /*
         * for (String s : list) { if (s.startsWith(text)) { m.addElement(s); }
         * } return m;
         *
         */
    }
}
