package lib;

import java.awt.Color;

/**
 * Interface for Peg Solitaire game
 */
public interface PegSolitaireInterface
{
    public static final int UNIT_SIZE = 70;
    public static final int[] BOARD_DIMENSIONS = {7, 9, 8, 7, 9, 1};
    public static final int BOARD_NUMBER = 6;
    public static final String[] BOARDNAMES = {"French", "Wiegleb", "Asymmetric", "English", "Diamond", "LOADED BOARD"};
    public static final Color PEG_COLOR = Color.green;
    public static final Color WAL_COLOR = Color.black;
    public static final Color SPC_COLOR = Color.white;
    public enum BOARDS 
    {
        FRENCH(0), WIEGLEB(1), ASYMMETRIC(2), ENGLISH(3), DIAMOND(4), LOADED(5);
        private final int value;
        BOARDS(final int newValue) {value = newValue; }
        public int getValue() {return value; }
    }

    public static final String[] DIRECTIONS = {"LEFT", "RIGHT", "UP", "DOWN"};

    public void start();
    public void loadFile();
    public void makeAMove();
    public boolean isMoveValid(int row, int clm, String dir);
    public boolean isGameOver();
    public int getActiveBoard();
    public void setActiveBoard(BOARDS a);
    public void setBoardPanels();
    public void setBoards();
    public void setBoard1();
    public void setBoard2();
    public void setBoard3();
    public void setBoard4();
    public void setBoard5();
    public void setBoardLoaded();
    public void setFunctionButtons();
    public void setBoardButtons(); 
    public void setScoreField(); 
    public int findScore(); 
    public void setBoardNameField(); 
}