package view;

import model.Difficulty;
import model.PlayableMinesweeper;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import notifier.IGameStateNotifier;

public class MinesweeperView implements IGameStateNotifier {
    public static final int MAX_TIME = 1;//in minutes
    public static final int TILE_SIZE = 50;
    public static final class AssetPath {
        public static final String CLOCK_ICON = "./assets/icons/clock.png";
        public static final String FLAG_ICON = "./assets/icons/flag.png";
        public static final String BOMB_ICON = "./assets/icons/bomb.png";
        public static final String PANEL_ICON = "./assets/icons/icon.jpg";
    }
    private PlayableMinesweeper gameModel;
    private JFrame window;
    private JMenuBar menuBar;
    private JMenu gameMenu;
    private JMenuItem easyGame, mediumGame, hardGame;
    private TileView[][] tiles;
    private JPanel world = new JPanel();
    private JPanel timerPanel = new JPanel();
    private JPanel flagPanel = new JPanel();
    private JLabel timerView = new JLabel();
    private JLabel flagCountView = new JLabel();

    public MinesweeperView() {
        this.window = new JFrame("Minesweeper");
        window.setIconImage(new ImageIcon(AssetPath.PANEL_ICON).getImage());
        //JMenuBar
        this.menuBar = new JMenuBar();
        this.gameMenu = new JMenu("New Game");
        this.menuBar.add(gameMenu);
        this.easyGame = new JMenuItem("Easy");
        this.gameMenu.add(this.easyGame);
        this.easyGame.addActionListener((ActionEvent e) -> {
            if (gameModel != null)
                gameModel.startNewGame(Difficulty.EASY);
        });
        this.mediumGame = new JMenuItem("Medium");
        this.gameMenu.add(this.mediumGame);
        this.mediumGame.addActionListener((ActionEvent e) -> {
            if (gameModel != null)
                gameModel.startNewGame(Difficulty.MEDIUM);

        });
        this.hardGame = new JMenuItem("Hard");
        this.gameMenu.add(this.hardGame);
        this.hardGame.addActionListener((ActionEvent e) -> {
            if (gameModel != null)
                gameModel.startNewGame(Difficulty.HARD);

        });

        this.window.setJMenuBar(this.menuBar);

        //TimerPanel
        timerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        try {
            JLabel clockIcon = new JLabel(new ImageIcon(ImageIO.read(new File(AssetPath.CLOCK_ICON))));
            clockIcon.setSize(new DimensionUIResource(10, 10));
            timerPanel.add(clockIcon);
            timerPanel.add(new JLabel("TIME ELAPSED: "));

            timerPanel.add(this.timerView);
        } catch (IOException e) {
            System.out.println("Unable to locate clock resource");
        }

        flagPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        try {
            JLabel clockIcon = new JLabel(new ImageIcon(ImageIO.read(new File(AssetPath.FLAG_ICON))));
            clockIcon.setSize(new DimensionUIResource(10, 10));
            flagPanel.add(clockIcon);
            flagPanel.add(new JLabel("FLAG: "));
            flagPanel.add(this.flagCountView);
        } catch (IOException e) {
            System.out.println("Unable to locate flag resource");
        }

        this.window.setLayout(new GridBagLayout());
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        layoutConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        this.window.add(timerPanel, layoutConstraints);
        layoutConstraints.gridx = 1;
        layoutConstraints.gridy = 0;
        this.window.add(flagPanel, layoutConstraints);
        layoutConstraints.fill = GridBagConstraints.BOTH;
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 1;
        layoutConstraints.gridwidth = 2;
        layoutConstraints.weightx = 1.0;
        layoutConstraints.weighty = 1.0;
        this.window.add(world, layoutConstraints);
        this.window.setSize(600,500 );
        this.window.setVisible(true);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.world.setVisible(true);
    }

    public MinesweeperView(PlayableMinesweeper gameModel) {
        this();
        this.setGameModel(gameModel);
    }

    public void setGameModel(PlayableMinesweeper newGameModel) {
        this.gameModel = newGameModel;
        this.gameModel.setGameStateNotifier(this);
    }

    @Override
    public void notifyNewGame(int row, int col) {
        this.flagCountView.setText("0");
        this.window.setSize(col * TILE_SIZE, row * TILE_SIZE + 30);
        this.world.removeAll();

        this.tiles = new TileView[row][col];
        for (int i=0; i<row; ++i) {
            for (int j=0; j<col; ++j) {
                TileView temp = new TileView(j, i);
                temp.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent arg0) {
                        if (arg0.getButton() == MouseEvent.BUTTON1){
                            if (gameModel!=null)
                                gameModel.open(temp.getPositionX(), temp.getPositionY());
                        }
                        else if (arg0.getButton() == MouseEvent.BUTTON3) {
                            if (gameModel!=null)
                                gameModel.toggleFlag(temp.getPositionX(), temp.getPositionY());
                        }
                    }
                });
                this.tiles[i][j] = temp;
                this.world.add(temp);
            }

        }
        this.world.setLayout(new GridLayout(row, col));
        this.world.setVisible(false);
        this.world.setVisible(true);
        this.world.repaint();
    }
    @Override
    public void notifyGameLost() {
        this.removeAllTileEvents();
        gameModel.setisCounting(false);
        soundPlay("bomb.wav");
        JOptionPane.showMessageDialog(null,"Game Lost","GameOver",JOptionPane.PLAIN_MESSAGE);
        //throw new UnsupportedOperationException();
    }
    @Override
    public void notifyGameWon() {
        this.removeAllTileEvents();
        gameModel.setisCounting(false);
        soundPlay("game_win.wav");
        JOptionPane.showMessageDialog(null,"You Win! Time："+Integer.toString(gameModel.getCounttime())+"s","WinGame",JOptionPane.PLAIN_MESSAGE);
        //throw new UnsupportedOperationException();
    }

    private void removeAllTileEvents() {
        for (int i=0; i<this.tiles.length; ++i)
            for (int j=0; j<this.tiles[i].length; ++j)
                this.tiles[i][j].removalAllMouseListeners();
    }

    @Override
    public void notifyFlagCountChanged(int newFlagCount) {
        this.flagCountView.setText(Integer.toString(newFlagCount));
    }

    @Override
    public void notifyTimeElapsedChanged(Duration newTimeElapsed) {
        timerView.setText(
                String.format("%d:%02d", newTimeElapsed.toMinutesPart(), newTimeElapsed.toSecondsPart()));

    }

    @Override
    public void notifyOpened(int x, int y, int explosiveNeighbourCount) {
        this.tiles[y][x].notifyOpened(explosiveNeighbourCount);

    }

    @Override
    public void notifyFlagged(int x, int y) {
        this.tiles[y][x].notifyFlagged();

    }

    @Override
    public void notifyUnflagged(int x, int y) {
        this.tiles[y][x].notifyUnflagged();
    }

    @Override
    public void notifyExploded(int x, int y) {
        this.tiles[y][x].notifyExplode();
    }

    private void soundPlay(String sound)
    {
        final String f=sound;
        Clip c=null;
        try
        {
            c= AudioSystem.getClip();
            c.open(AudioSystem.getAudioInputStream(new File(f)));
            c.loop(0);
        }
        catch(Exception ex)
        {
        }
    }


}
