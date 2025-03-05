package com.datamap;

import com.datamap.ui.DataMapWizard;
import javax.swing.*;

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
                
                DataMapWizard wizard = new DataMapWizard();
                wizard.setVisible(true);
            }
        });
    }
}