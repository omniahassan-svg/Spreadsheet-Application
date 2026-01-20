public final class Cell {
    private CellContent content;

    public Cell(CellContent content) {
        this.content = content == null ? new EmptyContent() : content;
    }

    public CellContent getContent() {
        return content;
    }

    public void setContent(CellContent content) {
        this.content = content == null ? new EmptyContent() : content;
    }
}
