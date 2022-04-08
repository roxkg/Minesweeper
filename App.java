import model.Difficulty;
//import model.Minesweeper;
import model.Minesweeper;
import model.PlayableMinesweeper;
import view.MinesweeperView;

import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {

        //Uncomment the lines below once your game model code is ready; don't forget to import your game model 
        Minesweeper model = new Minesweeper();
        MinesweeperView view = new MinesweeperView(model);
        model.setGameStateNotifier(view);
        model.startNewGame(Difficulty.EASY);
        model.threadTime();
    }
}
