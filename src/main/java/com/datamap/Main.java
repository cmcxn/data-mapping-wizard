package com.datamap;

import com.datamap.ui.DataMapWizard;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set system look and feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Get screen DPI
                GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                DisplayMode dm = gd.getDisplayMode();
                int screenDPI = Toolkit.getDefaultToolkit().getScreenResolution();
                double scale = screenDPI / 96.0; // Assuming 96 DPI as standard

                // Create and set up the wizard frame
                DataMapWizard wizard = new DataMapWizard();
                wizard.setPreferredSize(new Dimension((int) (800 * scale), (int) (600 * scale)));
                wizard.pack();

                // Center the frame on screen
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (screenSize.width - wizard.getWidth()) / 2;
                int y = (screenSize.height - wizard.getHeight()) / 2;
                wizard.setLocation(x, y);

                // Maximize the window
                wizard.setExtendedState(JFrame.MAXIMIZED_BOTH);

                wizard.setVisible(true);
            }
        });
    }
}