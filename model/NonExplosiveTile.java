package model;

public class NonExplosiveTile extends AbstractTile{
    private boolean isopen;
    private boolean isflag;
    public NonExplosiveTile(){
        isflag = false;
        isopen = false;
    }
    @Override
    public boolean open() {
        isopen = true;
        return true;
    }

    @Override
    public void flag() {
        isflag = true;
    }

    @Override
    public void unflag() {
        isflag = false;
    }

    @Override
    public boolean isFlagged() {
        return isflag;
    }

    @Override
    public boolean isExplosive() {
        return false;
    }

    @Override
    public boolean isOpened() {
        return isopen;
    }
}
