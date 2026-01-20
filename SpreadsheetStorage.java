public interface SpreadsheetStorage {
    int getRowCount();
    int getColCount();


    Cell getCell(int row0, int col0);


    void setCell(int row0, int col0, Cell cell);


    Iterable<Cell[]> rowsIterable();


    default String storageType() { return this.getClass().getSimpleName(); }
}
