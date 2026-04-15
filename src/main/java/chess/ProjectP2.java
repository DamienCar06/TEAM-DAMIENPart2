package chess;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main application class for the Chess Phase 2 GUI application.
 */
public class ProjectP2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProjectP2::createAndShowGUI);
    }

    /**
     * Creates and displays the main GUI window.
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chess Phase 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        BoardPanel boardPanel = new BoardPanel();
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(boardPanel.createSidePanel(), BorderLayout.EAST);
        createMenuBar(frame, boardPanel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

     /**
     * Creates and configures the menu bar for the application.
     *
     * @param frame the main application frame
     * @param boardPanel the chess board panel to control
     */
    private static void createMenuBar(JFrame frame, BoardPanel boardPanel) {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> boardPanel.resetGame());

        JMenuItem saveGameItem = new JMenuItem("Save Game");
        saveGameItem.addActionListener(e -> saveGame(frame, boardPanel));

        JMenuItem loadGameItem = new JMenuItem("Load Game");
        loadGameItem.addActionListener(e -> loadGame(frame, boardPanel));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(saveGameItem);
        gameMenu.add(loadGameItem);

        menuBar.add(gameMenu);
        frame.setJMenuBar(menuBar);
    }

     /**
     * Handles the save game operation.
     *
     * @param frame the parent frame for the dialog
     * @param boardPanel the chess board panel containing the game state
     */
    private static void saveGame(JFrame frame, BoardPanel boardPanel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Game");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Chess Game Files (*.chess)", "chess"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".chess")) {
                filePath += ".chess";
            }
            if (boardPanel.saveGame(filePath)) {
                JOptionPane.showMessageDialog(frame, "Game saved successfully!", "Save Game", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to save game.", "Save Game", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the load game operation.
     *
     * @param frame the parent frame for the dialog
     * @param boardPanel the chess board panel to load the game into
     */
    private static void loadGame(JFrame frame, BoardPanel boardPanel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Game");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Chess Game Files (*.chess)", "chess"));
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            if (boardPanel.loadGame(fileToLoad.getAbsolutePath())) {
                JOptionPane.showMessageDialog(frame, "Game loaded successfully!", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to load game.", "Load Game", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
