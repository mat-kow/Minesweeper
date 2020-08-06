package minesweeper;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        int[][] field = new int[9][9];
        int[][] visibleField = new int[9][9];
        Scanner scanner = new Scanner(System.in);
        System.out.print("How many mines do you want on the field? ");
        int mines = Integer.parseInt(scanner.nextLine());
        List<Coord> freeSpots = getAllCoordsList(9, 9);
        randomMines(field, mines, freeSpots);
        calculateMinesAround(field, freeSpots);
        while (true) {
            System.out.println();
            printField(visibleField, false);
            System.out.print("Set/unset mines marks or claim a cell as free: ");
            String[] line = scanner.nextLine().trim().split("\\s+");
            int x = Integer.parseInt(line[0]) - 1;
            int y = Integer.parseInt(line[1]) - 1;
            boolean isMineGuess = line[2].equals("mine");
            int spotReal = field[y][x];
            int spotVisible = visibleField[y][x];
            if (isMineGuess) { // marking mine
                if (spotVisible == -2) {
                    spotVisible = 0;
                } else if (spotVisible == 0) {
                    spotVisible = -2;
                } else {
                    continue;
                }
                visibleField[y][x] = spotVisible;
                if (checkField(field, visibleField)) {
                    System.out.println("Congratulations! You found all mines!");
                    break;
                }
            } else { // reveal spot
                if (spotReal > 0) { // number found
                    visibleField[y][x] = spotReal;
                    continue;
                }
                if (spotVisible == -2) { // already marked as mine
                    continue;
                }
                if (spotReal == -1) { // stepped on mine
                    System.out.println("You failed");
                    return;
                }
                // hit on 0
                Set<Coord> coordsToCheck = findSpotsAround(new Coord(y, x), field);
                visibleField[y][x] = -10;
                Set<Coord> coordsToCheckOnNextIteration = new HashSet<>();
                Set<Coord> alreadyChecked = new HashSet<>();
                alreadyChecked.add(new Coord(y, x));
                while (true) {
                    boolean everythingChecked = true;
                    for (Coord coord : coordsToCheck) {
                        if (alreadyChecked.contains(coord)) {
                            continue;
                        }
                        everythingChecked = false;
                        alreadyChecked.add(coord);
                        int numberOnSpot = field[coord.getRow()][coord.getCol()];
                        if (numberOnSpot == 0) {
                            coordsToCheckOnNextIteration.addAll(findSpotsAround(coord, field));
                            visibleField[coord.getRow()][coord.getCol()] = -10;
                            alreadyChecked.add(coord);
                        } else if (numberOnSpot > 0) {
                            visibleField[coord.getRow()][coord.getCol()] = numberOnSpot;
                            alreadyChecked.add(coord);
                        }
                    }
                    if (everythingChecked) {
                        break;
                    }
                    coordsToCheck = new HashSet<>(coordsToCheckOnNextIteration);
                    coordsToCheckOnNextIteration = new HashSet<>();
                }
            }
        }
    }

    private static boolean checkField(int[][] field, int[][] visibleField) {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                if (field[i][j] == -1 ^ visibleField[i][j] == -2) { //XOR
                    return false;
                }
            }
        }
        return true;
    }

    private static void randomMines(int[][] field, int mines, List<Coord> freeSpots) {
        Random random = new Random(new Date().getTime());
        while (mines > 0) {
            int index = random.nextInt(freeSpots.size());
            Coord coord = freeSpots.remove(index);
            field[coord.getRow()][coord.getCol()] = -1;
            mines--;
        }
    }

    private static void printField(int[][] field, boolean showMines) {
        StringBuilder numbers = new StringBuilder();
        for (int i = 1; i <= field[0].length; i++) {
            numbers.append(i);
        }
        System.out.println(" │" + numbers.toString() + "│");
        String horBorder = "—│" + "—".repeat(field[0].length) + "│";
        System.out.println(horBorder);
        for (int i = 0; i < field.length; i++) {
            System.out.print((i + 1) + "│");
            for (int e : field[i]) {
                if (e == -1) {//mine
                    if (showMines) {
                        System.out.print("X");
                    } else {
                        System.out.print(".");
                    }
                } else if (e == 0){//free
                    System.out.print(".");
                } else if (e == -2 || e == -3){//guess wrong/ right
                    System.out.print("*");
                } else if (e == -10){//guess 0
                    System.out.print("/");
                } else {
                    System.out.print(e);
                }
            }
            System.out.println("│");
        }
        System.out.println(horBorder);
    }

    private static List<Coord> getAllCoordsList(int rows, int cols) {
        List<Coord> coords = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                coords.add(new Coord(i, j));
            }
        }
        return coords;
    }

    private static Set<Coord> findSpotsAround(Coord spot, int[][] field) {
        Set<Coord> set = new HashSet<>();
        int col = spot.getCol();
        int row = spot.getRow();
        boolean upFree = false;
        boolean downFree = false;
        boolean leftFree = false;
        boolean rightFree = false;
        if (col > 0) {//not first column
            set.add(new Coord(row, col - 1));
            leftFree = true;
        }
        if (col < field[0].length - 1) { //not last column
            set.add(new Coord(row, col + 1));
            rightFree = true;
        }
        if (row > 0) {//not first row
            set.add(new Coord(row - 1, col));
            upFree = true;
        }
        if (row < field.length - 1) { //not last row
            set.add(new Coord(row + 1, col));
            downFree = true;
        }
        if (upFree && leftFree) {
            set.add(new Coord(row - 1, col - 1));
        }
        if (upFree && rightFree) {
            set.add(new Coord(row - 1, col + 1));
        }
        if (downFree && rightFree) {
            set.add(new Coord(row + 1, col + 1));
        }
        if (downFree && leftFree) {
            set.add(new Coord(row + 1, col - 1));
        }
        return set;
    }

    private static void calculateMinesAround(int[][] field, List<Coord> freeSpots) {
        if (field.length < 2 || field[0].length < 2) {
            throw new IllegalArgumentException("Field is too small");
        }
        for (Coord freeSpot : freeSpots) {
            int count = 0;
            Set<Coord> spotsAround = findSpotsAround(freeSpot, field);
            for (Coord spot : spotsAround) {
                if (field[spot.getRow()][spot.getCol()] == -1) {
                    count++;
                }
            }
            field[freeSpot.getRow()][freeSpot.getCol()] = count;
        }
    }
}