import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Spreadsheet sheet = null;
        S2VSerializer serializer = new S2VSerializer();

        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {

                    case "1" -> {
                        System.out.print("Rows: ");
                        int rows = Integer.parseInt(sc.nextLine());
                        System.out.print("Cols: ");
                        int cols = Integer.parseInt(sc.nextLine());
                        sheet = new Spreadsheet(rows, cols);
                        System.out.println("Spreadsheet created.");
                    }

                    case "2" -> {
                        requireSheet(sheet);
                        System.out.print("Cell (e.g. A1): ");
                        String coord = sc.nextLine().trim().toUpperCase();
                        int[] rc = Spreadsheet.coordToIndices(coord);

                        System.out.println("1) Text  2) Numeric  3) Formula");
                        String type = sc.nextLine().trim();

                        CellContent content;
                        switch (type) {
                            case "1" -> {
                                System.out.print("Text: ");
                                content = CellContentFactory.createText(sc.nextLine());
                            }
                            case "2" -> {
                                System.out.print("Number: ");
                                content = CellContentFactory.createNumeric(sc.nextLine());
                            }
                            case "3" -> {
                                System.out.print("Formula (=...): ");
                                String f = sc.nextLine();
                                content = CellContentFactory.createFormula(f);
                            }
                            default -> throw new IllegalArgumentException("Invalid type");
                        }

                        sheet.setCell(rc[0], rc[1], content);
                        System.out.println("Cell updated.");
                    }

                    case "3" -> {
                        requireSheet(sheet);
                        System.out.print("Cell: ");
                        int[] rc = Spreadsheet.coordToIndices(sc.nextLine().trim().toUpperCase());
                        CellContent c = sheet.getCell(rc[0], rc[1]).getContent();
                        System.out.println("Raw: " + c.getRaw());
                        System.out.println("Type: " + c.getType());
                    }

                    case "4" -> {
                        requireSheet(sheet);
                        System.out.print("Cell: ");
                        int[] rc = Spreadsheet.coordToIndices(sc.nextLine().trim().toUpperCase());
                        CellContent c = sheet.getCell(rc[0], rc[1]).getContent();

                        System.out.println("1) Numeric  2) Text");
                        String t = sc.nextLine().trim();
                        if ("1".equals(t))
                            System.out.println("Value: " + c.getNumericValue(sheet));
                        else
                            System.out.println("Value: " + c.getTextValue(sheet));
                    }

                    case "5" -> {
                        requireSheet(sheet);
                        System.out.print("From: ");
                        int[] a = Spreadsheet.coordToIndices(sc.nextLine().trim().toUpperCase());
                        System.out.print("To: ");
                        int[] b = Spreadsheet.coordToIndices(sc.nextLine().trim().toUpperCase());
                        sheet.printRawRegion(a[0], a[1], b[0], b[1]);
                    }

                    case "6" -> {
                        requireSheet(sheet);
                        System.out.print("Save path: ");
                        Path p = Paths.get(sc.nextLine());
                        serializer.save(sheet, p);
                        System.out.println("Saved.");
                    }

                    case "7" -> {
                        System.out.print("Load path: ");
                        Path p = Paths.get(sc.nextLine());
                        if (sheet == null) sheet = new Spreadsheet(10, 10);
                        serializer.load(sheet, p);
                        sheet.clearAllFormulaCaches();
                        sheet.recomputeAll();
                        System.out.println("Loaded.");
                    }

                    case "8" -> {
                        sc.close();
                        return;
                    }

                    default -> System.out.println("Unknown option.");
                }
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    private static void requireSheet(Spreadsheet sheet) {
        if (sheet == null)
            throw new IllegalStateException("Create a spreadsheet first.");
    }

    private static void printMenu() {
        System.out.println("""
            --- Menu ---
            1) New spreadsheet
            2) Set cell
            3) View cell
            4) Get value
            5) Print region
            6) Save
            7) Load
            8) Exit
            """);
    }
}
