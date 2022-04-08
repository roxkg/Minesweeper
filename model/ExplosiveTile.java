package model;

public class ExplosiveTile extends AbstractTile{
    private boolean isopen;
    private boolean isflag;
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
        return true;
    }

    @Override
    public boolean isOpened() {
        return isopen;
    }
}
