package model;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.time.Duration;

public class Minesweeper extends AbstractMineSweeper {
    private int width;
    private int height;
    AbstractTile[][] tiles;
    int[][] bombNum;
    boolean start;
    int midtime ;
    int countime = 0;
    boolean isCounting;
    int flagNum ;
    @Override
    public int getWidth() {
        return width;
    }
    @Override
    public void setisCounting(boolean counting){isCounting = counting;}
    @Override
    public int getHeight() {
        return height;
    }
    @Override
    public  int getCounttime(){return countime;}
    @Override
    public void startNewGame(Difficulty level) {
        if (level == Difficulty.EASY) {
            startNewGame(8, 8, 10);
            midtime = 120;

        } else if (level == Difficulty.MEDIUM) {
            startNewGame(16, 16, 40);
            midtime = 300;
        } else if (level == Difficulty.HARD) {
            startNewGame(16, 30, 99);
            midtime = 600;
        }
    }

    @Override
    public void startNewGame(int row, int col, int explosionCount) {
        setWorld(new AbstractTile[row][col]);
        randomArrange(explosionCount);
    }

    @Override
    public void toggleFlag(int x, int y) {
        AbstractTile tile = getTile(x, y);
        if(tile.isOpened()){
            return;
        }
        if (!tile.isFlagged()) {
            tile.flag();
            viewNotifier.notifyFlagged(x, y);
            flagNum++;
        } else {
            tile.unflag();
            viewNotifier.notifyUnflagged(x, y);
            flagNum--;
        }
        viewNotifier.notifyFlagCountChanged(flagNum);
        testWin();
    }

    @Override
    public AbstractTile getTile(int x, int y) {
        int col = (x < 0) ? 0 : (x >= getWidth()) ? getWidth() - 1 : x;
        int row = (y < 0) ? 0 : (y >= getHeight()) ? getHeight() - 1 : y;
        return tiles[row][col];
    }

    public int getBombNum(int x, int y) {
        int col = (x < 0) ? 0 : (x >= getWidth()) ? getWidth() - 1 : x;
        int row = (y < 0) ? 0 : (y >= getHeight()) ? getHeight() - 1 : y;
        return bombNum[row][col];
    }

    @Override
    public void setWorld(AbstractTile[][] world) {
        height = world.length;
        width = world[0].length;
        tiles = world;
        viewNotifier.notifyNewGame(height, width);
        start = false;
        isCounting = true;
        countime = 0;
        flagNum = 0;
    }

    @Override
    public void open(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return;
        }
        if (!start) {
            while (getTile(x, y).isExplosive()) {
                int explosionCount = 0;
                for (int i = 0; i < getHeight(); i++){
                    for (int j = 0; j < getWidth();j++){
                        if(getTile(j,i).isExplosive()){
                            explosionCount++;
                        }
                        tiles[i][j] = null;
                    }
                }
                randomArrange(explosionCount);
            }
            deactivateFirstTileRule();
            open(x, y);
        }
        else{
            if(getTile(x,y).isFlagged()){
                return;
            }
            if (getTile(x, y).isExplosive()) {
            for (int r = 0; r < height; ++r) {
                for (int c = 0; c < width; ++c) {
                    if (getTile(c, r).isExplosive()) {
                        viewNotifier.notifyExploded(c, r);
                    }
                    else {
                        viewNotifier.notifyOpened(c, r, getBombNum(c, r));
                    }
                }
            }
            viewNotifier.notifyGameLost();
            }
            else {
            subopen(x, y);
            }
        }
        if(getHeight() > 1 && getWidth() > 1)
        {
            testWin();
            soundPlay("open.wav");
        }

    }

    private void subopen(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return;
        }
        AbstractTile t = getTile(x, y);
        if (t.isExplosive()) return;
        if (t.isFlagged() || t.isOpened()) return;
        if (getBombNum(x, y) != 0) {
            t.open();
            viewNotifier.notifyOpened(x, y, getBombNum(x, y));
            return;
        }
        t.open();
        viewNotifier.notifyOpened(x, y, getBombNum(x, y));
        for (int r = -1;r < 2; ++r) {
            for (int c = -1; c < 2; ++c) {
                if (r == 0 && c == 0) continue;
                subopen(c + x, r + y);
            }
        }
    }

    @Override
    public void flag(int x, int y) {
        getTile(x,y).flag();
    }

    @Override
    public void unflag(int x, int y) {
        getTile(x, y).unflag();
    }


    @Override
    public void deactivateFirstTileRule() {
        start = true;
        generateBombNum();
    }

    @Override
    public AbstractTile generateEmptyTile() {
        AbstractTile tile = new NonExplosiveTile();
        return tile;
    }

    @Override
    public AbstractTile generateExplosiveTile() {
        AbstractTile tile = new ExplosiveTile();
        return tile;
    }

    private int[][] generateBombNum() {
        bombNum = new int[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                if (getTile(j, i).isExplosive()) {
                    continue;
                }
                int num = 0;
                for (int r = -1; (r + i < height) && (r < 2); ++r) {
                    if (r + i < 0) continue;
                    for (int c = -1; (c + j < width) && (c < 2); ++c) {
                        if (c + j < 0) continue;
                        if (getTile(c + j, r + i).isExplosive()) ++num;
                    }
                }
                bombNum[i][j] = num;
            }
        }
        return bombNum;
    }

    private void randomArrange(int explosionCount) {
        int num = 0;
        while (num < explosionCount) {
            int rRow = (int) (Math.random() * getHeight());
            int rCol = (int) (Math.random() * getWidth());
            if (getTile(rCol,rRow)==null) {
                tiles[rRow][rCol] = generateExplosiveTile();
                num++;
            }
        }
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (getTile(j, i) == null) {
                    tiles[i][j] = generateEmptyTile();
                }
            }
        }
    }

    public void threadTime() {
        Thread t = new Thread();
            while(isCounting && midtime>0) {
                try{
                    --midtime;
                    ++countime;
                    viewNotifier.notifyTimeElapsedChanged(Duration.ofSeconds(midtime));
                    t.sleep(1000);
                    }
                catch (Exception e)
                {
                    System.out.println("Error:" + e.toString());
                }
            }
            if(midtime<=0 && isCounting)
            {
                viewNotifier.notifyGameLost();
            }
            if(!isCounting)
            {
                try {
                    t.sleep(1000);
                    threadTime();
                }
                catch (Exception e) {
                    System.out.println("Error:" + e.toString());
                }
            }
        }

   private  void testWin() {
       for(int i = 0 ; i < height ; i++) {
           for(int j = 0;j < width ; j++) {
               if((getTile(j,i).isExplosive()&&getTile(j,i).isFlagged())||(!getTile(j,i).isExplosive()&&getTile(j,i).isOpened()))
               {
                   continue;
               }
               else {
                   return ;
               }
           }
       }
       viewNotifier.notifyGameWon();
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
            System.out.println("Error for open sound.");
        }
    }

}