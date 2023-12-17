import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.concurrent.ThreadLocalRandom;

public class MineSweepPart extends JFrame
{
    private static final LocalTime STARTTIME = LocalTime.now();
    private static final long serialVersionUID = 1L;
    private static final int WINDOW_HEIGHT = 760;
    private static final int WINDOW_WIDTH = 760;
    private static final int ROWS = 16;
    private static final int COLS = 16;
    private static final int TOTAL_MINES = 16;

    private static int guessedMinesLeft = MineSweepPart.TOTAL_MINES;
    private static int actualMinesLeft = MineSweepPart.TOTAL_MINES;

    private static final String INITIAL_CELL_TEXT = "";
    private static final String UNEXPOSED_FLAGGED_CELL_TEXT = "@";
    private static final String EXPOSED_MINE_TEXT = "M";

    //visual indication of exposed MyJButton
    private static final Color EXPOSED_CELL_BACKGROUND_COLOR = Color.lightGray;
    //colors used when displaying the getStateStr() String
    private static final Color EXPOSED_CELL_BACKGROUND_COLOR_MAP[] = {Color.lightGray, Color.blue,
            Color.green, Color.cyan, Color.yellow, Color.orange, Color.pink, Color.magenta,
            Color.red, Color.red};

    //holds the "number of mines in perimeter" value for each MyJButton
    private static final int MINEGRID_ROWS = 16;
    private static final int MINEGRID_COLS = 16;
    private int[][] mineGrid = new int[MINEGRID_ROWS][MINEGRID_COLS];

    private static final int NO_MINES_IN_PERIMETER_MINEGRID_VALUE = 0;
    private static final int ALL_MINES_IN_PERIMETER_MINEGRID_VALUE = 8;
    private static final int IS_A_MINE_IN_MINEGRID_VALUE = 9;

    private boolean running = true;

    public MineSweepPart()
    {
        this.setTitle("MineSweap                                      " +
                MineSweepPart.guessedMinesLeft + "Mines Left");
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setResizable(false);
        this.setLayout(new GridLayout(MINEGRID_ROWS, MINEGRID_COLS, 0, 0));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        //set the grid of MyJButtons
        this.createContents();

        //place MINES number of mines in sGrid and adjust all of "mines in perimeter" values
        this.setMines();

        this.setVisible(true);
    }
    public void createContents()
    {
        for (int mgr = 0; mgr < MINEGRID_ROWS; ++mgr)
        {
            for (int mgc = 0; mgc < MINEGRID_COLS ; ++mgc)
            {
                //set sGrid[mgr][mgc] entry to 0 - no mines in its perimeter
                this.mineGrid[mgr][mgc] = NO_MINES_IN_PERIMETER_MINEGRID_VALUE;

                //create a MyJButton that will be at location (mgr, mgc) in the GridLayout
                MyJButton but = new MyJButton(INITIAL_CELL_TEXT, mgr, mgc);

                //register the event handler with this MyJButton
                but.addActionListener(new MyListener());

                //add the MyJButton to the GridLayout collection
                this.add(but);
            }
        }
    }

    // begin nested private class
    private class MyListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            if ( running )
            {
                //used to determine if ctrl or alt key was pressed at the time of mouse action
                int mod = event.getModifiers();
                MyJButton mjb = (MyJButton)event.getSource();

                //is the MyJButton that the mouse action occurred in flagged
                boolean flagged = mjb.getText().equals(MineSweepPart.UNEXPOSED_FLAGGED_CELL_TEXT);

                //is the MyJButton that the mouse action occurred in already exposed
                boolean exposed = mjb.getBackground().equals(EXPOSED_CELL_BACKGROUND_COLOR);

                //flag a cell: ctrl + left click
                if ( !flagged && !exposed && (mod & ActionEvent.CTRL_MASK) != 0)
                {
                    //checks to see if there are any mines left
                    if (MineSweepPart.guessedMinesLeft == 0)
                    {
                        return;
                    }
                    mjb.setText(MineSweepPart.UNEXPOSED_FLAGGED_CELL_TEXT);
                    --MineSweepPart.guessedMinesLeft;

                    //if the MyJButton that the action occurred in is a mine
                    if (mineGrid[mjb.ROW][mjb.COL] == IS_A_MINE_IN_MINEGRID_VALUE)
                    {
                        MineSweepPart.actualMinesLeft --;
                        MineSweepPart.guessedMinesLeft --;

                        //If no mines, show winning dialogue and exit game
                        if (actualMinesLeft == 0)
                        {
                            JOptionPane.showMessageDialog(null, "You WIN! \n It took you " +
                                    " minutes");
                            return;
                        }
                        setTitle("MineSweap                                 " +
                                MineSweepPart.guessedMinesLeft + "Mines left");
                    }


                }
                //unflag a cell : alt + left click
                else if ( flagged && !exposed && (mod & ActionEvent.ALT_MASK) != 0)
                {
                    mjb.setText(INITIAL_CELL_TEXT);
                    ++MineSweepPart.guessedMinesLeft;

                    //if the MyJButton that the mouse action occurred in is a mine
                    if ( mineGrid[mjb.ROW][mjb.COL] == IS_A_MINE_IN_MINEGRID_VALUE)
                    {
                        ++MineSweepPart.actualMinesLeft;
                    }
                    setTitle("MineSweap                                    " +
                            MineSweepPart.guessedMinesLeft + " Mines left");
                }

                //expose a cell: left click
                else if ( !flagged && !exposed)
                {
                    exposeCell(mjb);
                }
            }
        }

        public void exposeCell(MyJButton mjb)
        {
            if ( !running )
                return;

            //expose this MyJButton
            mjb.setBackground(EXPOSED_CELL_BACKGROUND_COLOR);
            mjb.setForeground(EXPOSED_CELL_BACKGROUND_COLOR_MAP[mineGrid[mjb.ROW][mjb.COL]]);
            mjb.setText(getGridValueStr(mjb.ROW, mjb.COL));

            //if the MyJButton that was just exposed is a mine
            if ( mineGrid[mjb.ROW][mjb.COL] == IS_A_MINE_IN_MINEGRID_VALUE )
            {
                //show losing message
                JOptionPane.showMessageDialog(null, "Sorry, you lost! \n You found "+
                        (MineSweepPart.TOTAL_MINES - MineSweepPart.actualMinesLeft) + " mines!");
                System.exit(0);
            }

            //if the MyJButton that was just exposed has no mines in its perimeter
            if ( mineGrid[mjb.ROW][mjb.COL] == NO_MINES_IN_PERIMETER_MINEGRID_VALUE )
            {
                //must expose all MyJButtons in its perimeter
                //MyJButton mjbn = (MyJButton)mjb.getParent().getComponent(indn);
                return;
            }
        }
    }
    //end of nested private class

    public static void main(String[] args)
    {
        new MineSweepPart();
    }

    //place MINES number of mines in sGrid and adjust all of the "mines in perimeter" values
    private void setMines()
    {
        for (int i=0; i<TOTAL_MINES; ++i)
        {
            int random_Row = ThreadLocalRandom.current().nextInt(0, ROWS);
            int random_Column = ThreadLocalRandom.current().nextInt(0,COLS);

            //checks to see if cell is not a mine
            if (!(mineGrid[random_Row][random_Column] == IS_A_MINE_IN_MINEGRID_VALUE))
            {
                //make cell a mine
                mineGrid[random_Row][random_Column] = IS_A_MINE_IN_MINEGRID_VALUE;

                //check if row above mine is < grid length
                if (random_Row + 1 < mineGrid.length)
                {
                    //check if next cell ! a mine
                    if (mineGrid[random_Row + 1][random_Column] != IS_A_MINE_IN_MINEGRID_VALUE)
                    {
                        mineGrid[random_Row + 1][random_Column]++;
                    }
                    //if previous column is >=0 && previous cell in next row ! mine
                    if (random_Column - 1 >= 0 && mineGrid[random_Row+1][random_Column-1] != IS_A_MINE_IN_MINEGRID_VALUE)
                    {
                        mineGrid[random_Row+1][random_Column-1]++;
                    }
                }
                //previous row >= 0
                if (random_Row - 1 >= 0)
                {
                    //current cell ! mine
                    if (mineGrid[random_Row -1][random_Column] != IS_A_MINE_IN_MINEGRID_VALUE)
                    {
                        mineGrid[random_Row-1][random_Column]++;
                    }

                    //next row < prev row cell length && ! mine
                    if (random_Column +1 < mineGrid[random_Row-1].length && mineGrid[random_Row-1][random_Column+1]
                    != IS_A_MINE_IN_MINEGRID_VALUE)
                    {
                        mineGrid[random_Row-1][random_Column+1]++;
                    }

                    //prev col >=0 && ! mine
                    if (random_Column-1 >= 0 && mineGrid[random_Row-1][random_Column-1] !=
                    IS_A_MINE_IN_MINEGRID_VALUE)
                    {
                        mineGrid[random_Row-1][random_Column-1]++;
                    }
                }
                //next col< cur row cell length && cur cell !mine
                if (random_Column +1 < mineGrid[random_Row].length &&
                mineGrid[random_Row][random_Column+1] != IS_A_MINE_IN_MINEGRID_VALUE)
                {
                    mineGrid[random_Row][random_Column+1]++;
                }
                //prev col >=0 && !mine
                if (random_Column - 1 >= 0 &&
                mineGrid[random_Row][random_Column-1] != IS_A_MINE_IN_MINEGRID_VALUE)
                {
                    mineGrid[random_Row][random_Column-1]++;
                }
            }

            //otherwise, decrement index of for loop
            else
            {
                i--;
            }

        }
    }

    private String getGridValueStr(int row, int col)
    {
        //no mines in this MyJButton's perimeter
        if (this.mineGrid[row][col] == NO_MINES_IN_PERIMETER_MINEGRID_VALUE )
            return INITIAL_CELL_TEXT;

        //1 to 8 mines in this MyJButton's perimeter
        else if (this.mineGrid[row][col] > NO_MINES_IN_PERIMETER_MINEGRID_VALUE &&
        this.mineGrid[row][col] <= ALL_MINES_IN_PERIMETER_MINEGRID_VALUE)
            return "" + this.mineGrid[row][col];

        //this MyJButton in a mine
        else //this.mineGrid[row][col] = IS_A_MINE_IN_GRID_VALUE
        return MineSweepPart.EXPOSED_MINE_TEXT;



    }
}
