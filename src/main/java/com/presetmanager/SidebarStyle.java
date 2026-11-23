package com.presetmanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

/** Compact RuneLite-style visual treatment, without external artwork or fonts. */
final class SidebarStyle {
    static final Color BACKGROUND = new Color(32, 34, 37);
    static final Color SURFACE = new Color(43, 46, 50);
    static final Color BORDER = new Color(65, 69, 74);
    static final Color TEXT = new Color(225, 227, 229);
    static final Color MUTED = new Color(160, 167, 174);
    static final Color ACCENT = new Color(113, 190, 150);

    static void apply(JComponent root) {
        style(root);
    }

    private static void style(Component component) {
        if (component instanceof JComponent) {
            JComponent c = (JComponent) component;
            c.setOpaque(true);
            c.setBackground(BACKGROUND);
            c.setForeground(TEXT);
            if (component instanceof JPanel) {
                c.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            } else if (component instanceof JTextField) {
                c.setBackground(SURFACE);
                c.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(5, 7, 5, 7)));
            } else if (component instanceof AbstractButton) {
                c.setBackground(SURFACE);
                c.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(6, 7, 6, 7)));
                ((AbstractButton) c).setFocusPainted(false);
            } else if (component instanceof JLabel) {
                c.setOpaque(false);
                c.setForeground(MUTED);
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) style(child);
        }
    }

    private SidebarStyle() {}
}
