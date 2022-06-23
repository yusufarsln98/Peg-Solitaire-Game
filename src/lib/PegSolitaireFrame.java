package lib;

// --------- Import java and javax libraries ---------
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;

import javax.swing.WindowConstants;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;


/**
 * This a class for frame of Peg Solitaire Game.
 * It has constants as well as functions and listeners
 */
public class PegSolitaireFrame extends JFrame implements PegSolitaireInterface, Cloneable
{
    // ---------------- Fields ----------------
    private JButton[][][] boards;           // Five board keeps 2d button array.
    private JPanel[] boardPanels;
    private JPanel loadedBPanel;

    private BOARDS activeBoard;
    private JRadioButton[] boardButtons;
    private ButtonGroup buttonsGroup;
    private JPanel buttonsPanel;

    private JPanel funcPanel;
    private JButton autoButton;
    private JButton compButton;
    private JButton resetButton;
    private JButton undoButton;
    private JButton saveButton;
    private JButton loadButton;

    private JTextField scoreField;
    private JTextField boardNameField;
    
    private boolean isLoaded = false;
    private String filename = null;

    // ----------------End Fields ----------------
    

    /**
     * Keeps direction and coordinate of selected peg.
     */
    private static class Move
    {
        public static String direction;
        public static int row;
        public static int clm;
    }

    /**
     * PegSolitareFrame constructor.
     * Creates a frame and fill that frame with buttons, text fields and panels.
     */
    public PegSolitaireFrame()
    {
        // --- create frame ---
        super("-- Welcome to Peg Solitaire Game --");
        this.setSize(1100, 11 * UNIT_SIZE + 40); // set frame size
        setLayout(null);

        
        setActiveBoard(BOARDS.FRENCH);

        // --- add components then start the game ---
        setBoardPanels();
        setBoards();
        add(boardPanels[getActiveBoard()]);

        setBoardButtons();
        add(buttonsPanel);

        setFunctionButtons();
        add(funcPanel);

        setScoreField();
        setBoardNameField();

        start();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true); // display frame
    }

    /**
     * Clone function to make deep copy.
     */
    @Override
    public PegSolitaireFrame clone()
    {
        PegSolitaireFrame clone = null;
        try
        {
            clone = (PegSolitaireFrame) super.clone();

            clone.boardPanels = new JPanel[BOARD_NUMBER];
            
            for (int i = 0; i < BOARD_NUMBER; i++)
            {
                clone.boardPanels[i] = new JPanel();
                clone.boardPanels[i].setBackground(WAL_COLOR);
                clone.boardPanels[i].setBounds(20, 20, BOARD_DIMENSIONS[i] * UNIT_SIZE, BOARD_DIMENSIONS[i] * UNIT_SIZE);
                clone.boardPanels[i].setLayout(new GridLayout(BOARD_DIMENSIONS[i], BOARD_DIMENSIONS[i], 5, 5));
            }

            clone.boards = new JButton[BOARD_NUMBER][][];

            for (int i = 0; i < BOARD_NUMBER; i++)
                clone.boards[i] = new JButton[BOARD_DIMENSIONS[i]][BOARD_DIMENSIONS[i]];
            
            for (int b = 0; b < BOARD_NUMBER; b++)
                for (int i = 0; i < BOARD_DIMENSIONS[b]; i++)
                    for (int j = 0; j < BOARD_DIMENSIONS[b]; j++)
                        clone.boards[b][i][j] = new JButton();
            
            for (int b = 0; b < BOARD_NUMBER; b++)
                for (int i = 0; i < BOARD_DIMENSIONS[b]; i++)
                    for (int j = 0; j < BOARD_DIMENSIONS[b]; j++)
                    {
                        clone.boards[b][i][j].setSize(boards[b][i][j].getSize());
                        clone.boards[b][i][j].setEnabled(boards[b][i][j].isEnabled());
                        clone.boards[b][i][j].setBackground(boards[b][i][j].getBackground());
                        clone.boards[b][i][j].setActionCommand(boards[b][i][j].getActionCommand());
                    }
            
            for (int b = 0; b < BOARD_NUMBER; b++)
                for (int i = 0; i < BOARD_DIMENSIONS[b]; i++)
                    for (int j = 0; j < BOARD_DIMENSIONS[b]; j++)
                        clone.boardPanels[b].add(boards[b][i][j]);
                        
            
        }
        catch(CloneNotSupportedException cns)
        {
            System.out.println(cns.toString());
            System.exit(1);
        }
        return clone;
    }

    /**
     * Makes the all buttons active.
     * Calls listener functions.
     */
    @Override
    public void start() 
    {
        // --- Call Listeners ---
        for (int i = 0; i < BOARD_NUMBER; i++)
            boardButtons[i].addActionListener(new RadioButtonsListener());
        
        for (int b = 0; b < BOARD_NUMBER - 1; b++)
            for (int i = 0; i < BOARD_DIMENSIONS[b]; i++)
                for (int j = 0; j < BOARD_DIMENSIONS[b]; j++)
                    boards[b][i][j].addActionListener(new PegActionListener());

        autoButton.addActionListener(new AutoActionListener());
        compButton.addActionListener(new CompActionListener());
        resetButton.addActionListener(new ResetActionListener());
        undoButton.addActionListener(new UndoActionListener());
        saveButton.addActionListener(new SaveActionListener());
        loadButton.addActionListener(new LoadActionListener());
    }

    /**
     * Listens the switch buttons.
     * If one of that buttons clicked then changes the active board.
     */
    private class RadioButtonsListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            remove(boardPanels[getActiveBoard()]);
            Move.direction = null;
            Move.row = 0;
            Move.clm = 0;

            if(e.getActionCommand().equals("French"))
                setActiveBoard(BOARDS.FRENCH);
            else if (e.getActionCommand().equals("Wiegleb"))
                setActiveBoard(BOARDS.WIEGLEB);
            else if (e.getActionCommand().equals("Asymmetric"))
                setActiveBoard(BOARDS.ASYMMETRIC);
            else if (e.getActionCommand().equals("English"))
                setActiveBoard(BOARDS.ENGLISH);
            else if (e.getActionCommand().equals("Diamond"))
                setActiveBoard(BOARDS.DIAMOND);
            else if (e.getActionCommand().equals("LOADED BOARD"))
                setActiveBoard(BOARDS.LOADED);

            // --- To refresh the frame(window) 
                
            add(boardPanels[getActiveBoard()]);
            remove(scoreField);
            setScoreField(); 
            remove(boardNameField);
            setBoardNameField();
            setVisible(false); 
            setVisible(true); 
            validate();
        }
    }

    /**
     * Listens the pegs. If one of them is clicked then asks for the direction.
     * If every think is okey, then makes a move.
     */
    private class PegActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            // --- Read the row and column ---
            Move.row = e.getActionCommand().charAt(0) - '0';
            Move.clm = e.getActionCommand().charAt(1) - '0';
            
            // --- Listen the mouse ---
            // Check if direction is selected then call makeAMove() function.
            ((JButton)e.getSource()).addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    JPopupMenu pMenu = new JPopupMenu();
                    JMenuItem[] directions = new JMenuItem[4];
                    for (int i = 0; i < 4; i++)
                    {
                        directions[i] = new JMenuItem(DIRECTIONS[i]);
                        pMenu.add(directions[i]);
                    }

                    if (boards[getActiveBoard()][Move.row][Move.clm].isEnabled())
                        pMenu.show((JButton)(e.getSource()), e.getX(), e.getY());

                    for (int i = 0; i < 4; i++)
                    {
                        directions[i].addActionListener(new ActionListener(){
                            @Override
                            public void actionPerformed(ActionEvent e) 
                            {
                                Move.direction = e.getActionCommand();
                                makeAMove();
                            }
                        });
                    }
                }
            });
        }
    }


    /**
     * Listens the Play Auto button.
     * If it clicked then makes a random move if it is exist.
     */
    private class AutoActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            do
            {
                Random rand = new Random();
                int upperBoundDir = 4;
                int upperBoundInd = BOARD_DIMENSIONS[getActiveBoard()];

                Move.direction = DIRECTIONS[rand.nextInt(upperBoundDir)];
                Move.row = rand.nextInt(upperBoundInd);
                Move.clm = rand.nextInt(upperBoundInd);
                
            } while(!isMoveValid(Move.row, Move.clm, Move.direction) && !isGameOver());

            makeAMove();
        }
    }
    
    /**
     * Listens Play Auto All button.
     * If it is clicked then it randomly finishes the game.
     */
    private class CompActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            // -- Until game over --
            do 
            {
                // -- Until find a legal move --
                do
                {
                    Random rand = new Random();
                    int upperBoundDir = 4;
                    int upperBoundInd = BOARD_DIMENSIONS[getActiveBoard()];

                    Move.direction = DIRECTIONS[rand.nextInt(upperBoundDir)];
                    Move.row = rand.nextInt(upperBoundInd);
                    Move.clm = rand.nextInt(upperBoundInd);

                } while(!isMoveValid(Move.row, Move.clm, Move.direction) && !isGameOver());
                makeAMove();
                
            } while (!isGameOver());
        }
    }

    /**
     * Listens the reset button.
     * If it is clicked then it resets the active board.
     */
    private class ResetActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            switch (getActiveBoard()) 
            {
                case 0:
                    setBoard1();
                    validate();
                    break;
                case 1:
                    setBoard2();
                    validate();
                    break;
                case 2:
                    setBoard3();
                    validate();
                    break;
                case 3:
                    setBoard4();
                    validate();
                    break;
                case 4:
                    setBoard5();
                    validate();
                    break;
                case 5:
                    if (isLoaded)
                    { 
                        loadFile(); validate();
                    }
                    loadedBoardListener();
                    break;
            }
            // Refresh the screen and do not allow make an undo after reset.
            Move.direction = null;
            Move.row = 0;
            Move.clm = 0;

            remove(scoreField);
            setScoreField();
            validate();

            remove(boardNameField);
            setBoardNameField();
            validate();
        }
    }

    /**
     * Listens the undo button.
     * If it is clicked then it takes last move back.
     */
    private class UndoActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            if (Move.direction == null)
            {
                JOptionPane.showMessageDialog(null, "Please make a move first!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            else if (Move.direction.equals("LEFT"))
            {
                boards[getActiveBoard()][Move.row][Move.clm].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm].setEnabled(true);

                boards[getActiveBoard()][Move.row][Move.clm - 1].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm - 1].setEnabled(true);

                boards[getActiveBoard()][Move.row][Move.clm - 2].setBackground(SPC_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm - 2].setEnabled(false);
            }
            else if(Move.direction.equals("RIGHT"))
            {
                boards[getActiveBoard()][Move.row][Move.clm].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm].setEnabled(true);

                boards[getActiveBoard()][Move.row][Move.clm + 1].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm + 1].setEnabled(true);

                boards[getActiveBoard()][Move.row][Move.clm + 2].setBackground(SPC_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm + 2].setEnabled(false);
            }
            else if(Move.direction.equals("UP"))
            {
                boards[getActiveBoard()][Move.row][Move.clm].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm].setEnabled(true);

                boards[getActiveBoard()][Move.row - 1][Move.clm].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row - 1][Move.clm].setEnabled(true);

                boards[getActiveBoard()][Move.row - 2][Move.clm].setBackground(SPC_COLOR);
                boards[getActiveBoard()][Move.row - 2][Move.clm].setEnabled(false);
            }
            else if(Move.direction.equals("DOWN"))
            {
                boards[getActiveBoard()][Move.row][Move.clm].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row][Move.clm].setEnabled(true);

                boards[getActiveBoard()][Move.row + 1][Move.clm].setBackground(PEG_COLOR);
                boards[getActiveBoard()][Move.row + 1][Move.clm].setEnabled(true);

                boards[getActiveBoard()][Move.row + 2][Move.clm].setBackground(SPC_COLOR);
                boards[getActiveBoard()][Move.row + 2][Move.clm].setEnabled(false);
            }

            // -- Reset the score --
            remove(scoreField);
            setScoreField();
        }
    }


    /**
     * Listens the save button.
     * If it is cliked, then it asks for a filename.
     * Finally it saves current board to a file as strings.
     */
    private class SaveActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            String temp = JOptionPane.showInputDialog("Enter filename to save.", ".txt");  
            if (temp != null) 
            {
                filename = temp;
                try
                {
                    FileWriter fileWriter = new FileWriter(filename);
                    fileWriter.write(BOARD_DIMENSIONS[getActiveBoard()] + "\n");
                    for (int i = 0; i < BOARD_DIMENSIONS[getActiveBoard()]; i++)
                    {
                        for (int j = 0; j < BOARD_DIMENSIONS[getActiveBoard()]; j++)
                        {
                            if ((boards[getActiveBoard()][i][j]).getBackground().equals(PEG_COLOR))
                                fileWriter.write("PEG" + "\n");
                            else if ((boards[getActiveBoard()][i][j]).getBackground().equals(WAL_COLOR))
                                fileWriter.write("WAL" + "\n");
                            else if ((boards[getActiveBoard()][i][j]).getBackground().equals(SPC_COLOR))
                                fileWriter.write("SPC" + "\n");
                        }
                    }
                    fileWriter.close(); 
                }
                catch (IOException exp) 
                {
                    JOptionPane.showMessageDialog(null, "Invalid Filename!", "ERROR!", JOptionPane.ERROR_MESSAGE);
                    setVisible(false);
                    setVisible(true);
                    validate();
                }
            }
        }
    }

    /**
     * Listens the load button.
     * If it is cliked, then it ask for a filename.
     * Finally, it loads the board from file and refresh the board.
     */
    private class LoadActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            String temp = JOptionPane.showInputDialog("Enter filename to load.", ".txt");   
            if(temp != null)
            {
                filename = temp;
                // --- Load the board, call listener and refresh the window ---
                loadFile();
                isLoaded = true;
                remove(boardPanels[getActiveBoard()]);
                setActiveBoard(BOARDS.LOADED);
                add(boardPanels[getActiveBoard()]);
                remove(scoreField);
                setScoreField();
                remove(boardNameField);
                setBoardNameField();
                loadedBoardListener();
                validate();
            }
        }
    }

    // --- Listener for loaded board. ---
    public void loadedBoardListener() 
    {
        for (int i = 0; i < BOARD_DIMENSIONS[BOARDS.LOADED.getValue()]; i++)
                for (int j = 0; j < BOARD_DIMENSIONS[BOARDS.LOADED.getValue()]; j++)
                    boards[BOARDS.LOADED.getValue()][i][j].addActionListener(new PegActionListener());
    }

    /**
     * Reads file and creates a board to play game.
     */
    @Override
    public void loadFile() 
    {
        File file = new File(filename);
        try (Scanner reader = new Scanner(file)) 
        {
            String length = reader.nextLine();
            BOARD_DIMENSIONS[BOARDS.LOADED.getValue()] = Integer.parseInt(length);
            loadedBPanel = new JPanel();
            loadedBPanel.setBackground(WAL_COLOR);
            loadedBPanel.setBounds(20, 20, BOARD_DIMENSIONS[BOARDS.LOADED.getValue()] * UNIT_SIZE, BOARD_DIMENSIONS[BOARDS.LOADED.getValue()] * UNIT_SIZE);
            loadedBPanel.setLayout(new GridLayout(BOARD_DIMENSIONS[BOARDS.LOADED.getValue()], BOARD_DIMENSIONS[BOARDS.LOADED.getValue()], 5, 5));
            boards[BOARDS.LOADED.getValue()] = new JButton[BOARD_DIMENSIONS[BOARDS.LOADED.getValue()]][BOARD_DIMENSIONS[BOARDS.LOADED.getValue()]];
            
            remove(boardPanels[BOARDS.LOADED.getValue()]);

            for (int i = 0; i < BOARD_DIMENSIONS[BOARDS.LOADED.getValue()]; i++)
                for (int j = 0; j < BOARD_DIMENSIONS[BOARDS.LOADED.getValue()]; j++)
                {
                    boards[BOARDS.LOADED.getValue()][i][j] = new JButton();
                    boards[BOARDS.LOADED.getValue()][i][j].setSize(UNIT_SIZE, UNIT_SIZE);
                    boards[BOARDS.LOADED.getValue()][i][j].setActionCommand("" + i + j);

                    String data = reader.nextLine();
                    if (data.equals("PEG"))
                    {
                        boards[BOARDS.LOADED.getValue()][i][j].setEnabled(true);
                        boards[BOARDS.LOADED.getValue()][i][j].setBackground(PEG_COLOR);
                    }
                    else if (data.equals("WAL"))
                    {
                        boards[BOARDS.LOADED.getValue()][i][j].setEnabled(false);
                        boards[BOARDS.LOADED.getValue()][i][j].setBackground(WAL_COLOR);
                    }
                    else if (data.equals("SPC"))
                    {
                        boards[BOARDS.LOADED.getValue()][i][j].setEnabled(false);
                        boards[BOARDS.LOADED.getValue()][i][j].setBackground(SPC_COLOR);
                    }
                    loadedBPanel.add(boards[BOARDS.LOADED.getValue()][i][j]);
                }
                boardPanels[BOARDS.LOADED.getValue()] = loadedBPanel;
                add(boardPanels[BOARDS.LOADED.getValue()]);

                setVisible(false);
                setVisible(true);
                
                validate();
                reader.close();
        } 
        catch (FileNotFoundException exception) 
        {
            JOptionPane.showMessageDialog(null, "Invalid Filename!", "ERROR!", JOptionPane.ERROR_MESSAGE);
            setVisible(false);
            setVisible(true);
            validate();
        }
    }

    /**
     * Makes a move according to the direction and coordinate.
     */
    @Override
    public void makeAMove()
    {
        if (isGameOver())
        {
            JOptionPane.showMessageDialog(null, "Game Over! Please change the board or reset!", null, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!isMoveValid(Move.row, Move.clm, Move.direction))
        {
            JOptionPane.showMessageDialog(null, "Invalid Direction!", "Warning", JOptionPane.WARNING_MESSAGE);
            // -- Prevent make undo.
            Move.direction = null;
            Move.row = 0;
            Move.clm = 0;
            return;
        }
        if(Move.direction.equals("LEFT"))
        {
            boards[getActiveBoard()][Move.row][Move.clm].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm].setEnabled(false);

            boards[getActiveBoard()][Move.row][Move.clm - 1].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm - 1].setEnabled(false);

            boards[getActiveBoard()][Move.row][Move.clm - 2].setBackground(PEG_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm - 2].setEnabled(true);
        }
        else if(Move.direction.equals("RIGHT"))
        {
            boards[getActiveBoard()][Move.row][Move.clm].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm].setEnabled(false);

            boards[getActiveBoard()][Move.row][Move.clm + 1].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm + 1].setEnabled(false);

            boards[getActiveBoard()][Move.row][Move.clm + 2].setBackground(PEG_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm + 2].setEnabled(true);
        }
        else if(Move.direction.equals("UP"))
        {
            boards[getActiveBoard()][Move.row][Move.clm].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm].setEnabled(false);

            boards[getActiveBoard()][Move.row - 1][Move.clm].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row - 1][Move.clm].setEnabled(false);

            boards[getActiveBoard()][Move.row - 2][Move.clm].setBackground(PEG_COLOR);
            boards[getActiveBoard()][Move.row - 2][Move.clm].setEnabled(true);
        }
        else if(Move.direction.equals("DOWN"))
        {
            boards[getActiveBoard()][Move.row][Move.clm].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row][Move.clm].setEnabled(false);

            boards[getActiveBoard()][Move.row + 1][Move.clm].setBackground(SPC_COLOR);
            boards[getActiveBoard()][Move.row + 1][Move.clm].setEnabled(false);

            boards[getActiveBoard()][Move.row + 2][Move.clm].setBackground(PEG_COLOR);
            boards[getActiveBoard()][Move.row + 2][Move.clm].setEnabled(true);
        }
        
        remove(scoreField);
        setScoreField();

        if (isGameOver())
            JOptionPane.showMessageDialog(null, "Game Over! Please change the board or reset!", null, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Checks the adjacent pegs and returns true if move is valid,
     * else, returns false.
     */
    @Override
    public boolean isMoveValid(int row, int clm, String dir)
    {
        if(dir.equals("LEFT"))
        {
            if (clm == 0 || clm == 1)
                return false;
            else if (boards[getActiveBoard()][row][clm].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row][clm - 1].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row][clm - 2].getBackground() == SPC_COLOR)
                return true;
        }
        else if(dir.equals("RIGHT"))
        {
            if (clm == BOARD_DIMENSIONS[getActiveBoard()] - 1 || clm == BOARD_DIMENSIONS[getActiveBoard()] - 2)
                return false;
            else if (boards[getActiveBoard()][row][clm].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row][clm + 1].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row][clm + 2].getBackground() == SPC_COLOR)
                return true;
        }
        else if(dir.equals("UP"))
        {
            if (row == 0 || row == 1)
                return false;
            else if (boards[getActiveBoard()][row][clm].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row - 1][clm].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row - 2][clm].getBackground() == SPC_COLOR)
                return true;
        }
        else if(dir.equals("DOWN"))
        {
            if (row == BOARD_DIMENSIONS[getActiveBoard()] - 1 || row == BOARD_DIMENSIONS[getActiveBoard()] - 2)
                return false;
            else if (boards[getActiveBoard()][row][clm].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row + 1][clm].getBackground() == PEG_COLOR &&
                     boards[getActiveBoard()][row + 2][clm].getBackground() == SPC_COLOR)
                return true;
        }   
        return false;
    }

    /**
     * Check all directions and possible moves.
     * Return true if game is over, else, return false.
     */
    @Override
    public boolean isGameOver()
    {
        for (int i = 0; i < BOARD_DIMENSIONS[getActiveBoard()]; i++)
            for (int j = 0; j < BOARD_DIMENSIONS[getActiveBoard()]; j++)
                if(isMoveValid(i, j, "LEFT") || isMoveValid(i, j, "RIGHT") || isMoveValid(i, j, "UP") || isMoveValid(i, j, "DOWN"))
                    return false;
               
        return true;
    }
    
    /**
     * Get active board.
     */
    @Override
    public int getActiveBoard() 
    {
        return activeBoard.getValue();
    }

    /**
     * Set active board.
     */
    @Override
    public void setActiveBoard(BOARDS a) 
    {
        this.activeBoard = a;
    }

    /**
     * Creates panels. They will keep boards inside.
     */
    @Override
    public void setBoardPanels() 
    {
        boardPanels = new JPanel[BOARD_NUMBER];
        for (int i = 0; i < BOARD_NUMBER; i++)
        {
            boardPanels[i] = new JPanel();
            boardPanels[i].setBackground(WAL_COLOR);
            boardPanels[i].setBounds(20, 20, BOARD_DIMENSIONS[i] * UNIT_SIZE, BOARD_DIMENSIONS[i] * UNIT_SIZE);
            boardPanels[i].setLayout(new GridLayout(BOARD_DIMENSIONS[i], BOARD_DIMENSIONS[i], 5, 5));
        }
    }

    /**
     * Create empty boards for each game.
     * Then call setter function of boards.
     * Finally, add the boards into the panels.
     */
    @Override
    public void setBoards()
    {
        //  --- Create boards and call setter functions ---
        boards = new JButton[BOARD_NUMBER][][];
        for (int i = 0; i < BOARD_NUMBER; i++)
            boards[i] = new JButton[BOARD_DIMENSIONS[i]][BOARD_DIMENSIONS[i]];
        
        for (int b = 0; b < BOARD_NUMBER; b++)
            for (int i = 0; i < BOARD_DIMENSIONS[b]; i++)
                for (int j = 0; j < BOARD_DIMENSIONS[b]; j++)
                    boards[b][i][j] = new JButton();

        setBoard1();
        setBoard2();
        setBoard3();
        setBoard4();
        setBoard5();
        setBoardLoaded();

        // --- Add them into the panels ---
        for (int b = 0; b < BOARD_NUMBER; b++)
            for (int i = 0; i < BOARD_DIMENSIONS[b]; i++)
                for (int j = 0; j < BOARD_DIMENSIONS[b]; j++)
                    boardPanels[b].add(boards[b][i][j]);
    }

    /**
     * Setter for French board.
     */
    @Override
    public void setBoard1() 
    {
        for (int i = 0; i < boards[0].length; i++)
        {
            for (int j = 0; j < boards[0][i].length; j++)
            {
                boards[0][i][j].setSize(UNIT_SIZE, UNIT_SIZE);
                boards[0][i][j].setActionCommand("" + i + j);

                if((i < 2 && j < 2 && !(i == 1 && j == 1)) || (i < 2 && j > 4 && !(i == 1 && j == 5)) ||
                   (i > 4 && j < 2 && !(i == 5 && j == 1)) || (i > 4 && j > 4 && !(i == 5 && j == 5)))
                {
                    boards[0][i][j].setEnabled(false);
                    boards[0][i][j].setBackground(WAL_COLOR);
                }
                else
                {
                    boards[0][i][j].setEnabled(true);
                    boards[0][i][j].setBackground(PEG_COLOR);
                }
            }
        }

        boards[0][2][3].setEnabled(false);
        boards[0][2][3].setBackground(SPC_COLOR);
    }

    /**
     * Setter for Wiegleb board.
     */
    @Override
    public void setBoard2() 
    {
        for (int i = 0; i < boards[1].length; i++)
        {
            for (int j = 0; j < boards[1][i].length; j++)
            {
                boards[1][i][j].setSize(UNIT_SIZE, UNIT_SIZE);
                boards[1][i][j].setActionCommand("" + i + j);

                if((i < 3 && j < 3) || (i < 3 && j > 5) || (i > 5 && j < 3) || (i > 5 && j > 5))
                {
                    boards[1][i][j].setEnabled(false); 
                    boards[1][i][j].setBackground(WAL_COLOR);
                }
                else
                {
                    boards[1][i][j].setEnabled(true); 
                    boards[1][i][j].setBackground(PEG_COLOR);
                }
            }
        }
        boards[1][4][4].setEnabled(false);
        boards[1][4][4].setBackground(SPC_COLOR);
    }

    /**
     * Setter for Asymmetric board.
     */
    @Override
    public void setBoard3() 
    {
        for (int i = 0; i < boards[2].length; i++)
        {
            for (int j = 0; j < boards[2][i].length; j++)
            {
                boards[2][i][j].setSize(UNIT_SIZE, UNIT_SIZE);
                boards[2][i][j].setActionCommand("" + i + j);

                if((i < 3 && j < 2) || (i < 3 && j > 4) || (i > 5 && j < 2) || (i > 5 && j > 4))
                {
                    boards[2][i][j].setEnabled(false); 
                    boards[2][i][j].setBackground(WAL_COLOR);
                }
                else
                {
                    boards[2][i][j].setEnabled(true); 
                    boards[2][i][j].setBackground(PEG_COLOR);
                }
            }
        }

        // Make the appropriate changes to create first board.
        boards[2][4][3].setEnabled(false);
        boards[2][4][3].setBackground(SPC_COLOR);
    }

    /**
     * Setter for English board.
     */
    @Override
    public void setBoard4() 
    {
        for (int i = 0; i < boards[3].length; i++)
        {
            for (int j = 0; j < boards[3][i].length; j++)
            {
                boards[3][i][j].setSize(UNIT_SIZE, UNIT_SIZE);
                boards[3][i][j].setActionCommand("" + i + j);

                if((i < 2 && j < 2) || (i < 2 && j > 4) || (i > 4 && j < 2) || (i > 4 && j > 4))
                {
                    boards[3][i][j].setEnabled(false);
                    boards[3][i][j].setBackground(WAL_COLOR);
                }
                else
                {
                    boards[3][i][j].setEnabled(true);
                    boards[3][i][j].setBackground(PEG_COLOR);
                }
            }
        }
        boards[3][3][3].setEnabled(false);
        boards[3][3][3].setBackground(SPC_COLOR);
    }

    /**
     * Setter for Diamond board.
     */
    @Override
    public void setBoard5() 
    {
        for (int i = 0; i < boards[4].length; i++)
        {
            for (int j = 0; j < boards[4][i].length; j++)
            {
                boards[4][i][j].setSize(UNIT_SIZE, UNIT_SIZE);
                boards[4][i][j].setActionCommand("" + i + j);

                if((i == 0 && j != 4) || (i == 1 && j != 3 && j != 4 && j != 5) || 
                   (i == 2 && j == 0) || (i == 2 && j == 1) || (i == 2 && j == 7) || (i == 2 && j == 8) ||
                   (i == 3 && j == 0) || (i == 3 && j == 8) || (i == 5 && j == 0) || (i == 5 && j == 8) ||
                   (i == 6 && j == 0) || (i == 6 && j == 1) || (i == 6 && j == 7) || (i == 6 && j == 8) ||
                   (i == 8 && j != 4) || (i == 7 && j != 3 && j != 4 && j != 5)) 
                {
                    boards[4][i][j].setEnabled(false); 
                    boards[4][i][j].setBackground(WAL_COLOR);
                }
                else
                {
                    boards[4][i][j].setEnabled(true); 
                    boards[4][i][j].setBackground(PEG_COLOR);
                }
            }
        }
        boards[4][4][4].setEnabled(false);
        boards[4][4][4].setBackground(SPC_COLOR);
    }

    /**
     * Setter for if a board already did not loaded.
     */
    @Override
    public void setBoardLoaded() 
    {
        boards[5][0][0].setToolTipText("Load a board!");
        boards[5][0][0].setText("Load a board!");
        boards[5][0][0].setEnabled(false);
    }

    /**
     * Setter for creating a panel then add function buttons such as,
     * Auto Play, Auto Play All, Reset, Undo, Save, Load.
     */
    @Override
    public void setFunctionButtons() 
    {
        funcPanel = new JPanel();
        funcPanel.setBounds(9 * UNIT_SIZE + 60, 200, 300, 100);
        funcPanel.setLayout(new GridLayout(3, 2, 3, 3));
        funcPanel.setBackground(SPC_COLOR);
        
        autoButton = new JButton("Play Auto");
        compButton = new JButton("Play Auto All");
        resetButton = new JButton("Reset");
        undoButton = new JButton("Undo");
        saveButton = new JButton("Save");
        loadButton = new JButton("Load");

        funcPanel.add(autoButton);
        funcPanel.add(compButton);
        funcPanel.add(resetButton);
        funcPanel.add(undoButton);
        funcPanel.add(saveButton);
        funcPanel.add(loadButton);
    }

    /**
     * Setter for radio buttons which works for switch the boards.
     */
    @Override
    public void setBoardButtons() 
    {
        buttonsPanel = new JPanel();
        boardButtons = new JRadioButton[BOARD_NUMBER];
        buttonsGroup = new ButtonGroup();
        
        for (int i = 0; i < BOARD_NUMBER; i++)
        {
            boardButtons[i] = new JRadioButton(BOARDNAMES[i]);
            buttonsGroup.add(boardButtons[i]);
            buttonsPanel.add(boardButtons[i]);
        }
        buttonsPanel.setBounds(20, 9 * UNIT_SIZE + 20, 600, 50);
    }

    /**
     * Set the score field.
     */
    @Override
    public void setScoreField() 
    {
        scoreField = new JTextField("SCORE  " + findScore());
        scoreField.setBackground(SPC_COLOR);
        scoreField.setEditable(false);
        scoreField.setBounds(9 * UNIT_SIZE + 260, 10, 100, 30);
        scoreField.setFont(new Font(null, Font.PLAIN, 18));
        add(scoreField);
        scoreField.repaint();
    }

    /**
     * Find the score.
     */
    @Override
    public int findScore() 
    {
        int counter = 0;

        for (int i = 0; i < BOARD_DIMENSIONS[getActiveBoard()]; i++)
        {
            for (int j = 0; j < BOARD_DIMENSIONS[getActiveBoard()]; j++)
            {
                if (boards[getActiveBoard()][i][j].getBackground().equals(PEG_COLOR))
                counter++;
            }
        }
        return counter;
    }
    
    /**
     * Setter for display board name to the screen.
     */
    @Override
    public void setBoardNameField() 
    {
        boardNameField = new JTextField("Current Board: " + BOARDNAMES[getActiveBoard()]);
        boardNameField.setBackground(SPC_COLOR);
        boardNameField.setEditable(false);
        boardNameField.setBounds(9 * UNIT_SIZE + 60, 170, 300, 30);
        boardNameField.setFont(new Font(null, Font.PLAIN, 16));
        add(boardNameField);
        boardNameField.repaint();
    }

}
