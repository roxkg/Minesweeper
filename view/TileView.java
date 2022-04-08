package view;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.MouseListener;
import java.io.File;

import notifier.ITileStateNotifier;
import view.MinesweeperView.AssetPath;

public class TileView extends JButton implements ITileStateNotifier {
    private static ImageIcon flagIcon = new ImageIcon(AssetPath.FLAG_ICON);
    private static ImageIcon bombIcon = new ImageIcon(AssetPath.BOMB_ICON);

    private int x, y;
    public TileView(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public int getPositionX() {return x;}
    public int getPositionY() {return y;}

    @Override
    public void notifyOpened(int explosiveNeighbourCount) {
        super.setIcon(null);
        super.setText((explosiveNeighbourCount> 0)? Integer.toString(explosiveNeighbourCount) : "");
        super.setEnabled(false);
    }

    @Override
    public void notifyFlagged() {
        super.setIcon(flagIcon);
        final String f="flag.wav";
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

    @Override
    public void notifyUnflagged() {
        super.setIcon(null);
        final String f="unflag.wav";
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

    public void removalAllMouseListeners() {
        for (MouseListener listener : super.getMouseListeners()) 
            this.removeMouseListener(listener);    
    }

    @Override
    public void notifyExplode() {
        super.setText("");
        super.setIcon(bombIcon);
        super.setEnabled(false);
    }

    @Override
    public String toString(){
        return "["+Integer.toString(x)+","+Integer.toString(y)+"]";
    }
    
}
