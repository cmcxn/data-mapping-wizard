package com.datamap.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A JComboBox with autocomplete capabilities.
 * Provides alphabetical sorting and filtering as the user types.
 */
public class AutoCompleteComboBox extends JComboBox<String> {
    private boolean isAutoCompleting = false;
    private final List<String> allItems = new ArrayList<>();

    public AutoCompleteComboBox() {
        setEditable(true);
        setupAutoComplete();
    }

    /**
     * Sets up the autocomplete behavior for the combobox
     */
    private void setupAutoComplete() {
        final JTextField textField = (JTextField) getEditor().getEditorComponent();

        // Add document listener to detect typing
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isAutoCompleting) {
                    filterItems();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isAutoCompleting) {
                    filterItems();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isAutoCompleting) {
                    filterItems();
                }
            }
        });

        // Add action listener for Enter key and selection
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getItemCount() > 0) {
                    hidePopup();
                }
            }
        });

        // Handle selection from popup
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && !isAutoCompleting) {
                    // User has selected an item from the dropdown
                    SwingUtilities.invokeLater(() -> {
                        hidePopup();
                        if (e.getItem() != null) {
                            textField.setText(e.getItem().toString());
                        }
                    });
                }
            }
        });

        // Handle mouse click on combo box
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isPopupVisible()) {
                    showPopup();
                }
            }
        });
    }

    /**
     * Filters the items in the dropdown based on the current text
     */
    private void filterItems() {
        final JTextField textField = (JTextField) getEditor().getEditorComponent();
        final String text = textField.getText();

        SwingUtilities.invokeLater(() -> {
            isAutoCompleting = true;

            if (text.isEmpty()) {
                // Reset to show all items
                setModel(new DefaultComboBoxModel<>(allItems.toArray(new String[0])));
                showPopup();
                isAutoCompleting = false;
                return;
            }

            // Find matching items - match anywhere in the string, not just prefix
            List<String> filteredItems = new ArrayList<>();
            for (String item : allItems) {
                if (item.toLowerCase().contains(text.toLowerCase())) {
                    filteredItems.add(item);
                }
            }

            if (filteredItems.isEmpty()) {
                hidePopup();
            } else {
                // Update the model with filtered items
                setModel(new DefaultComboBoxModel<>(filteredItems.toArray(new String[0])));
                textField.setText(text);
                textField.setCaretPosition(text.length());
                showPopup();
            }

            isAutoCompleting = false;
        });
    }

    /**
     * Updates the items in the autocomplete dropdown
     *
     * @param itemList The list of items to include
     */
    public void setAutoCompleteItems(List<String> itemList) {
        allItems.clear();

        if (itemList != null) {
            // Add all items in alphabetical order
            allItems.addAll(itemList);
            Collections.sort(allItems);
        }

        // Update the model
        isAutoCompleting = true;
        setModel(new DefaultComboBoxModel<>(allItems.toArray(new String[0])));
        isAutoCompleting = false;
    }

    /**
     * Gets the currently selected or entered text
     */
    public String getSelectedText() {
        // Always return the text in the editor component
        return ((JTextField) getEditor().getEditorComponent()).getText();
    }
}