/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ordg.utl.display;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTabbedPane;

public class TabButton extends JButton implements ActionListener {
    private final JTabbedPane pane;
    
    public TabButton(JTabbedPane pane) {
        this.pane = pane;
        setPreferredSize(new Dimension(16, 16));
        setToolTipText("Close this tab");
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int i = pane.getSelectedIndex();
        if (i != -1) {
            pane.remove(i);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = getWidth() / 2 - 3;
        int y = getHeight() / 2 - 3;
        g2.drawLine(x, y, x + 6, y + 6);
        g2.drawLine(x + 6, y, x, y + 6);
        g2.dispose();
    }
}
