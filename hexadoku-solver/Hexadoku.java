/*
 *  @version Hexadoku.java - A Hexadoku solver. - v1.0beta
 *
 *  @author Copyright (C) 2016 Luca Ballore - Last update: 2015-12-10
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package hexadokuSolver;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/***
 * Define constants for the dimensions and the contents of the puzzle.
 *
 */
class puzzleConstants {
    public static final int PUZZLE_SIDE = 16;
    public static final int SQUARE_SIDE = 4;
    public static final int PUZZLE_SIZE = 256;
    public static final int COLUMN_SIZE = 1024;
    public static final String HEX_ALPHABET = "0123456789ABCDEF";
}

/***
 * This class defines a puzzle and solves it.
 */
public class Hexadoku {

    /***
     * The main function tests the Hexadoku solver with
     * a hardcoded hexadoku puzzle string.
     *
     * @param args
     */
    public static void main(String[] args) {

        Hexadoku hexadoku = new Hexadoku();
        String hexPuzzle = "---------------------" +
                           "|B37C|.F..|26.0|8...|" +
                           "|..6.|.BE.|...4|95..|" +
                           "|....|...0|5...|.2.B|" +
                           "|.0.5|....|..8.|C..6|" +
                           "---------------------" +
                           "|..A1|.5.E|.F4.|7...|" +
                           "|24..|C8..|..6.|.BD1|" +
                           "|....|6.0.|.C..|.4.E|" +
                           "|..3.|4..2|905E|6F..|" +
                           "---------------------" +
                           "|..90|E37.|B..6|.D..|" +
                           "|5.4.|..9.|.E.8|....|" +
                           "|76C.|20..|..3F|..A5|" +
                           "|...3|.A4.|C.0.|F1..|" +
                           "---------------------" +
                           "|0..9|....|....|3.5.|" +
                           "|3CD.|...B|E...|....|" +
                           "|..17|F..8|.A9.|.E.C|" +
                           "|...6|0.C3|..D.|A82.|" +
                           "---------------------";

        int[][] puzzle = hexadoku.parsePuzzleFromString(hexPuzzle);
        hexadoku.solve(puzzle);
    }

    /***
     * Parse the puzzle string.
     *
     * @param puzzle
     * @return Bidimensional array with int values
     */
    int[][] parsePuzzleFromString(String puzzle) {
        char[] charArray = puzzle.toCharArray();
        List<Integer> tempList = new ArrayList<>();
        int[][] parsedPuzzle = new int[puzzleConstants.PUZZLE_SIDE][puzzleConstants.PUZZLE_SIDE];
        int arrayIdx = 0;
        int subArrayIdx = 0;

        for (char ch: charArray) {
            if (ch == '.') {
                tempList.add(-1);
            } else if (puzzleConstants.HEX_ALPHABET.indexOf(ch) != -1) {
                tempList.add(Integer.parseInt(String.valueOf(ch), 16));
            } else {}
        }

        for(int i = 0; i < tempList.size(); i++) {
            parsedPuzzle[arrayIdx][subArrayIdx] = tempList.get(i);
            if (subArrayIdx != puzzleConstants.PUZZLE_SIDE -1) {
                subArrayIdx++;
            } else {
                subArrayIdx = 0;
                arrayIdx++;
            }
        }

        if (!validatePuzzle(charArray, tempList, parsedPuzzle))
            System.exit(0);

        return parsedPuzzle;
    }

    /***
     * Return true if the string puzzle is valid.
     *
     * @param charArray
     * @param elementList
     * @return true if the puzzle is valid, false otherwise
     */
    private boolean validatePuzzle(char[] charArray, List<Integer> elementList, int[][] parsedPuzzle) {
        String validchars = ".|-";
        validchars += puzzleConstants.HEX_ALPHABET;

        if (elementList.size() != puzzleConstants.PUZZLE_SIZE) {
            System.out.println("Invalid number of elements for a hexadoku: " + elementList.size());
            return false;
        }
        for(char ch: charArray) {
            if (validchars.indexOf(ch) == -1) {
                System.out.println("Invalid element found on the puzzle: " + ch);
                return false;
            }
        }
        if (!checkPuzzleConfiguration(parsedPuzzle)) {
            System.out.println("Your puzzle is irregular. Redundant digit(s) found.");
            return false;
        }
        return true;
    }

    /***
     * Reveal repeated digits on the initial configuration.
     *
     * @param parsedPuzzle
     * @return true if the puzzle is regular, false otherwise.
     */
    private boolean checkPuzzleConfiguration(int[][] parsedPuzzle) {
        for (int i = 0; i < puzzleConstants.PUZZLE_SIDE; i++) {

            int[] row = new int[puzzleConstants.PUZZLE_SIDE];
            int[] square = new int[puzzleConstants.PUZZLE_SIDE];
            int[] column = parsedPuzzle[i].clone();

            for (int j = 0; j < puzzleConstants.PUZZLE_SIDE; j ++) {
                row[j] = parsedPuzzle[j][i];
                square[j] = parsedPuzzle[(i / puzzleConstants.SQUARE_SIDE) * puzzleConstants.SQUARE_SIDE + j /
                        puzzleConstants.SQUARE_SIDE][i * puzzleConstants.SQUARE_SIDE % puzzleConstants.PUZZLE_SIDE + j
                                % puzzleConstants.SQUARE_SIDE];
            }
            if (!(validatePortion(column) && validatePortion(row) && validatePortion(square)))
                return false;
        }
        return true;
    }

    /***
     * Check for repeated digits in a single row, column or submatrix.
     *
     * @param portion
     * @return true if there are no repeated digits, false otherwise.
     */
    private boolean validatePortion(int[] portion) {
        Arrays.sort(portion);
        for (int i = 0; i < portion.length; i++) {
            if (portion[i] == -1)
                continue;
            if (i < puzzleConstants.PUZZLE_SIDE - 1 && (portion[i] == portion[i + 1])) {
                return false;
            }
        }
        return true;
    }

    /***
     * Solve a puzzle.
     *
     * @param puzzle
     */
    void solve(int[][] puzzle) {
        DancingLinks dl = new DancingLinks(puzzle);

        // Print the original puzzle before all the eventual solutions
        printSchema(puzzle, 0);
        dl.solve(this);

        printSolutionStats(dl.solutionNum);
    }

    /***
     * Print a puzzle or solution.
     *
     * @param result
     */
    void printSchema(int[][] result, int solutionNum) {
        System.out.println();
        if (solutionNum == 0) {
            System.out.println("*-*-*-*-*-*-*-*- PUZZLE *-*-*-*-*-*-*-*-*");
        } else {
            System.out.println(String.format("*-*-*-*-*-*-* Solution %d -*-*-*-*-*-*-*-*", solutionNum));
        }

        for (int i = 0; i < puzzleConstants.PUZZLE_SIDE; i++) {
            for (int j = 0; j < puzzleConstants.PUZZLE_SIDE; j++) {
                if (j == 0 && i % puzzleConstants.SQUARE_SIDE == 0) {
                    System.out.println("|---------------------------------------|");
                }
                if (j == 0) {
                    System.out.print("| ");
                }
                if (result[i][j] < 0) {
                    System.out.print(". ");
                } else {
                    System.out.print(Integer.toString(result[i][j], 16).toUpperCase() + " ");
                }
                if ((j + 1) % puzzleConstants.SQUARE_SIDE == 0) {
                    System.out.print("| ");
                }

            }
            System.out.println();
        }
        System.out.println("|---------------------------------------|");
        System.out.println();
    }

    /***
     * Print some statistics related to the found solutions
     * (only the solutions number for now).
     *
     * @param solutionNum
     */
    void printSolutionStats(int solutionNum) {
        String solStats;

        switch (solutionNum) {
            case 0:
                solStats = String.format("No solutions found.");
                break;
            case 1:
                solStats = String.format("%d solution found.", solutionNum);
                break;
            default:
                solStats = String.format("%d solutions found.", solutionNum);
                break;
        }
        System.out.println(solStats);
    }
}

/***
 * This class implements the Dancing Links technique to
 * implement the Knuth's Algorithm X adapted for hexadoku puzzles.
 *
 * More info:
 * - https://en.wikipedia.org/wiki/Dancing_Links
 * - https://en.wikipedia.org/wiki/Knuth%27s_Algorithm_X
 * - https://www.ocf.berkeley.edu/~jchu/publicportal/sudoku/sudoku.paper.html
 */
class DancingLinks {
    Hexadoku hexadoku;
    int index;
    Column column;
    Node[] output;
    int solutionNum;

    /***
     * Create a column head and add 1024 (256 x 4) columns.
     * 4096 (16*16*16) rows of four nodes are added to the columns.
     * If a row is part of the puzzle it's removed from
     * the matrix and added to the solution.
     *
     * @param puzzle
     */
    DancingLinks(int[][] puzzle) {
        solutionNum = 0;

        // Column row head.
        Column[] colRow = new Column[puzzleConstants.COLUMN_SIZE];
        column = new Column(null, 0);
        // Create the row of columns.
        for (int i = 0; i < puzzleConstants.COLUMN_SIZE; i++)
            colRow[i] = new Column(column, 0);
        // List of rows that are part of the solution.
        Node[] rowList = new Node[puzzleConstants.PUZZLE_SIZE];
        int idx = 0;
        // For each row, column and possible digit:
        for (int row = 0; row < puzzleConstants.PUZZLE_SIDE; row++)
            for (int col = 0; col < puzzleConstants.PUZZLE_SIDE; col++)
                for (int dgt = 0; dgt < puzzleConstants.PUZZLE_SIDE; dgt++) {
                    // 1. Calculate row number.
                    int rowNum = (row * puzzleConstants.PUZZLE_SIZE) + (col * puzzleConstants.PUZZLE_SIDE) + dgt;
                    // 2. Create the row of nodes.
                    Node nodesRow = new Node(colRow[(row * puzzleConstants.PUZZLE_SIDE) + col], rowNum);
                    nodesRow.add(new Node(colRow[(puzzleConstants.PUZZLE_SIZE * 1) + (row * puzzleConstants.PUZZLE_SIDE) + dgt], rowNum));
                    nodesRow.add(new Node(colRow[(puzzleConstants.PUZZLE_SIZE * 2) + (col * puzzleConstants.PUZZLE_SIDE) + dgt], rowNum));
                    nodesRow.add(new Node(colRow[(puzzleConstants.PUZZLE_SIZE * 3) + ((((row / puzzleConstants.SQUARE_SIDE) * puzzleConstants.SQUARE_SIDE) +
                            (col / puzzleConstants.SQUARE_SIDE)) * puzzleConstants.PUZZLE_SIDE) + dgt], rowNum));

                    // If this row is in the puzzle, add it to the list.
                    if (puzzle[col][row] == dgt)
                        rowList[idx++] = nodesRow;
                }

        // Create an array for the output.
        output = new Node[puzzleConstants.PUZZLE_SIZE];

        // Remove the rows in the list and add them to the output.
        for (int j = 0; j < idx; j++) {
            rowList[j].remove();
            output[index++] = rowList[j];
        }
    }

    /***
     * Rearrange the output to match the puzzle.
     * Convert the row number back to row, column,
     * digit,then update the number of solutions
     * and send the result to the printing function.
     *
     * @param output
     */
    void printResult(int[] output) {
        int result[][] = new int[puzzleConstants.PUZZLE_SIDE][puzzleConstants.PUZZLE_SIDE];

        // Convert the row number back to row, column, digit.
        for (int i = 0; i < puzzleConstants.PUZZLE_SIZE; i++) {
            int val = output[i];
            int dgt = val % puzzleConstants.PUZZLE_SIDE;
            int col = (val / puzzleConstants.PUZZLE_SIDE) % puzzleConstants.PUZZLE_SIDE;
            int row = (val / puzzleConstants.PUZZLE_SIZE) % puzzleConstants.PUZZLE_SIDE;

            result[col][row] = dgt;
        }
        // Update the number of solutions
        solutionNum++;
        // Report the result.
        hexadoku.printSchema(result, solutionNum);
    }

    /**
     * Start the exploring process.
     *
     * @param hex
     */
    void solve(Hexadoku hex) {
        hexadoku = hex;
        explore(index);
    }

    /**
     * This is the exploring procedure from the Dancing Links technique.
     *
     * @param num
     */
    void explore(int num) {
        // If there are no more columns, print the result.
        if (column.rightLink == column) {
            int[] result = new int[num];
            for (int i = 0; i < num; i++)
                result[i] = output[i].rowNum;
            printResult(result);
        }

        // Else find the shortest column and cover it.
        else {
            Column shortestCol = null;
            int maxVal = Integer.MAX_VALUE;

            // Find the shortest column.
            for (Column j = (Column) column.rightLink; j != column; j = (Column) j.rightLink)
                if (maxVal > j.colSize) {
                    shortestCol = j;
                    maxVal = j.colSize;
                }
            shortestCol.cover();

            // For each row in the column:
            for (Node r = shortestCol.downLink; r != shortestCol; r = r.downLink) {
                // 1. save the row in the output array.
                output[num] = r;
                // 2. for each node in this row, cover it's column.
                for (Node j = r.rightLink; j != r; j = j.rightLink)
                    j.colLink.cover();
                // 3. recurse with num + 1.
                explore(num + 1);
                // 4. for each node in this row, uncover it's column.
                for (Node j = r.leftLink; j != r; j = j.leftLink)
                    j.colLink.uncover();
            }
            // Uncover the column.
            shortestCol.uncover();
        }
    }
}


/***
 * A Node is a structure with four links: left, right, up, down,
 * which reference adiacent nodes. It has also a link which references the column and a row number.
 *
 */
class Node {
    Node leftLink;
    Node rightLink;
    Node upLink;
    Node downLink;
    Column colLink;
    int rowNum;

    /***
     * Create a self referencing node.
     *
     * @param col
     * @param rowNum
     */
    Node(Column col, int rowNum) {
        this.leftLink = this;
        this.rightLink = this;
        this.upLink = this;
        this.downLink = this;
        this.colLink = col;
        this.rowNum = rowNum;

        if (col != null)
            col.add(this);
    }

    /***
     * Remove a row of nodes.
     * Cover node's column and move on to the next
     * right while we haven't got back to that node.
     *
     */
    void remove() {
        Node node = this;

        do {
            node.colLink.cover();
            node = node.rightLink;
        }
        while (node != this);
    }

    /***
     * Add a node to the left of this node.
     *
     * @param node
     */
    void add(Node node) {
        node.leftLink = this.leftLink;
        node.rightLink = this;

        this.leftLink.rightLink = node;
        this.leftLink = node;
    }
}


/***
 * A Column object inherits the links from the
 * Node class and has also a size attribute.
 *
 */
class Column extends Node {
    int colSize;

    Column(Column col, int rowNum) {
        super(null, rowNum);

        if (col != null)
            col.add(this);
    }

    /***
     * Cover procedure. One of the optional constraints
     * described on the Dancing Link algorithm. Scan all
     * the rows in the Column and for all the Nodes in each row
     * cover the row and adjusts the Column size.
     *
     */
    void cover() {
        rightLink.leftLink = leftLink;
        leftLink.rightLink = rightLink;

        for (Node i = downLink; i != this; i = i.downLink) {
            for (Node j = i.rightLink; j != i; j = j.rightLink) {
                j.upLink.downLink = j.downLink;
                j.downLink.upLink = j.upLink;
                j.colLink.colSize--;
            }
        }
    }

    /***
     * Uncover procedure. One of the optional constraints
     * described on the Dancing Link algorithm. Scan all
     * the rows in the Column and for all the Nodes in each row
     * uncover the row and adjusts the Column size.
     *
     */
    void uncover() {
        for (Node i = upLink; i != this; i = i.upLink)
            for (Node j = i.leftLink; j != i; j = j.leftLink) {
                j.upLink.downLink = j;
                j.downLink.upLink = j;
                j.colLink.colSize++;
            }

        rightLink.leftLink = this;
        leftLink.rightLink = this;
    }

    /***
     * Add a column to the left of this column.
     *
     * @param col
     */
    void add(Column col) {
        col.leftLink = this.leftLink;
        col.rightLink = this;

        this.leftLink.rightLink = col;
        this.leftLink = col;
    }

    /***
     * Add a node to the end of this column.
     *
     * @param node
     */
    void add(Node node) {
        node.upLink = this.upLink;
        node.downLink = this;

        this.upLink.downLink = node;
        this.upLink = node;
        colSize++;
    }
}

