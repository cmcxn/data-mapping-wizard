package com.datamap.ui;

import com.datamap.util.ConfigManager;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class FileTreePanel extends JPanel {
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private File currentFolder;
    private List<Consumer<File>> fileSelectionListeners = new ArrayList<>();
    private DataMapWizard wizard;

    public FileTreePanel(DataMapWizard wizard) {
        this.wizard = wizard;
        setLayout(new BorderLayout());
        initializeUI();
        loadWorkingDirectory();
    }

    private void initializeUI() {
        rootNode = new DefaultMutableTreeNode("Configuration Files");
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);

        // Set custom renderer
        fileTree.setCellRenderer(new JsonFileTreeCellRenderer());

        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Single click
                    TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object userObject = node.getUserObject();
                        if (userObject instanceof FileNode) {
                            FileNode fileNode = (FileNode) userObject;
                            File file = fileNode.getFile();

                            // Only trigger for JSON files (not directories)
                            if (!file.isDirectory() && file.getName().toLowerCase().endsWith(".json")) {
                                loadConfigurationFile(file);
                            }
                        }
                    }
                }
            }
        });

        // Create a panel with title and refresh button
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("JSON Configurations");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 12));

        JButton refreshButton = new JButton("⟳");
        refreshButton.setToolTipText("Refresh file tree");
        refreshButton.setPreferredSize(new Dimension(30, 25));
        refreshButton.addActionListener(e -> refreshFileTree());

        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(refreshButton, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(fileTree), BorderLayout.CENTER);
    }

    private void loadWorkingDirectory() {
        String workingDir = ConfigManager.getCurrentWorkingDirectory();
        if (workingDir != null && !workingDir.trim().isEmpty()) {
            File folder = new File(workingDir);
            if (folder.exists() && folder.isDirectory()) {
                loadFolder(folder);
            }
        }
    }

    public void refreshFileTree() {
        loadWorkingDirectory();
    }

    private void loadConfigurationFile(File jsonFile) {
        try {
            // Create a temporary file to pass to the existing load method
            // We'll modify the generateCodePanel to accept a File parameter
            if (wizard.getGenerateCodePanel() != null) {
                wizard.getGenerateCodePanel().loadConfigurationFromFile(jsonFile);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading configuration: " + e.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean loadFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false;
        }

        this.currentFolder = folder;
        rootNode.removeAllChildren();

        // Set the root node text to show the working directory
        rootNode.setUserObject("Working Directory: " + folder.getName());

        DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(
                new FileNode(folder, true));
        rootNode.add(folderNode);

        loadFilesIntoNode(folder, folderNode);

        // Expand tree to show structure
        treeModel.reload();
        expandAllNodes();

        return true;
    }

    private void loadFilesIntoNode(File folder, DefaultMutableTreeNode node) {
        File[] files = folder.listFiles(file ->
                file.isDirectory() || file.getName().toLowerCase().endsWith(".json"));

        if (files == null) return;

        // Sort files: directories first, then alphabetically
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            }
        });

        for (File file : files) {
            FileNode fileNode = new FileNode(file, file.isDirectory());
            DefaultMutableTreeNode fileTreeNode = new DefaultMutableTreeNode(fileNode);
            node.add(fileTreeNode);

            if (file.isDirectory()) {
                loadFilesIntoNode(file, fileTreeNode);
            }
        }
    }

    private void expandAllNodes() {
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }

    public void addFileSelectionListener(Consumer<File> listener) {
        fileSelectionListeners.add(listener);
    }

    public File getCurrentFolder() {
        return currentFolder;
    }

    // File node class to store file data
    private static class FileNode {
        private File file;
        private boolean isDirectory;

        public FileNode(File file, boolean isDirectory) {
            this.file = file;
            this.isDirectory = isDirectory;
        }

        public File getFile() {
            return file;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }

    // Custom tree cell renderer for JSON files
    private class JsonFileTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Icon jsonIcon;
        private final Icon folderIcon;

        public JsonFileTreeCellRenderer() {
            // Create simple icons or use system icons
            jsonIcon = UIManager.getIcon("FileView.fileIcon");
            folderIcon = UIManager.getIcon("FileView.directoryIcon");
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                      boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof FileNode) {
                    FileNode fileNode = (FileNode) userObject;
                    File file = fileNode.getFile();

                    // Set appropriate icon
                    if (fileNode.isDirectory()) {
                        setIcon(folderIcon);
                    } else if (file.getName().toLowerCase().endsWith(".json")) {
                        setIcon(jsonIcon);
                        // Make JSON files appear slightly different
                        setForeground(selected ? getTextSelectionColor() : new Color(0, 100, 0));
                    }

                    // Set tooltip with full path
                    setToolTipText(file.getAbsolutePath());
                }
            }

            return this;
        }
    }
}