package com.ordg.utl.display;

/**
 *
 * @author Hp
 */
public class ScreenResolution {
    
    private int Width;
    private int height;
    private int centerPointX;
    private int centerPointY;

    public ScreenResolution(int Width, int height) {
        this.Width = Width;
        this.height = height;
        this.centerPointX = Width/2;
        this.centerPointY = height/2;
    }
    
    /**
     * @return the Width
     */
    public int getWidth() {
        return Width;
    }

    /**
     * @param Width the Width to set
     */
    public void setWidth(int Width) {
        this.Width = Width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the centerPointX
     */
    public int getCenterPointX() {
        return centerPointX;
    }

    /**
     * @param centerPointX the centerPointX to set
     */
    public void setCenterPointX(int centerPointX) {
        this.centerPointX = centerPointX;
    }

    /**
     * @return the centerPointY
     */
    public int getCenterPointY() {
        return centerPointY;
    }

    /**
     * @param centerPointY the centerPointY to set
     */
    public void setCenterPointY(int centerPointY) {
        this.centerPointY = centerPointY;
    }
    
}
