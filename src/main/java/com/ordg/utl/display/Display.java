package com.ordg.utl.display;

import java.awt.Dimension;
import javax.swing.JFrame;

public class Display {

    private ScreenResolution screenResolution = null;

    public Display(JFrame mainFrame) {
        screenResolution = new ScreenResolution(mainFrame.getWidth(), mainFrame.getHeight());
        this.setDefaultDimension(new Dimension(900, 900));
    }

    public void setFrameInCenter(JFrame c) {
        
        
        
                
        //Get Center of Child Frame 
        int childCenterX = c.getWidth() / 2;
        int childCenterY = c.getHeight() / 2;

        //Set X , Y position of child frame
        c.setLocation(screenResolution.getCenterPointX() - childCenterX, screenResolution.getCenterPointY() - childCenterY);

    }

    public Dimension getDefaultDimension() {
        return DefaultDimension;
    }

    public void setDefaultDimension(Dimension DefaultDimension) {
        this.DefaultDimension = DefaultDimension;
    }

    private Dimension DefaultDimension = new Dimension();

}
