package src;

import javax.swing.table.DefaultTableModel;

public interface CRUD {
    abstract void create();
    abstract void read();
    abstract void update(DefaultTableModel model);
    abstract void delete();
}
