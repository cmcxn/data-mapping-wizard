package com.datamap;

import com.datamap.util.SortedJXComboBox;

import javax.swing.*;
import java.awt.*;

/**
 * 修复版本的演示程序
 */
public class SortedComboBoxDemo extends JFrame {

    private SortedJXComboBox<String> comboBox;
    private JTextArea outputArea;

    public SortedComboBoxDemo() {
        setTitle("SortedJXComboBox 模糊匹配演示 - 修复版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        pack();
        setLocationRelativeTo(null);

        // 添加窗口关闭时的清理
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (comboBox != null) {
                    comboBox.dispose(); // 清理资源
                }
                System.exit(0);
            }
        });
    }

    private void initializeComponents() {
        String[] sampleData = {
                "Apple iPhone", "Samsung Galaxy", "Google Pixel", "OnePlus Pro",
                "Xiaomi Mi", "Huawei P40", "Sony Xperia", "LG Velvet",
                "Motorola Edge", "Nokia 8.3", "Oppo Find", "Vivo X60",
                "Realme GT", "Honor 20", "Apple MacBook", "Microsoft Surface"
        };

        comboBox = new SortedJXComboBox<>(sampleData);
        comboBox.setPreferredSize(new Dimension(300, 30));

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel("选择或输入:"));
        comboPanel.add(comboBox);

        JPanel controlPanel = createControlPanel();

        mainPanel.add(comboPanel, BorderLayout.NORTH);
        mainPanel.add(controlPanel, BorderLayout.CENTER);
        mainPanel.add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 匹配模式选择
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("匹配模式:"), gbc);

        gbc.gridx = 1;
        JComboBox<SortedJXComboBox.FuzzyMatchMode> modeCombo = new JComboBox<>(SortedJXComboBox.FuzzyMatchMode.values());
        modeCombo.addActionListener(e -> {
            // 在单独的线程中处理模式切换，避免阻塞UI
            SwingUtilities.invokeLater(() -> {
                try {
                    SortedJXComboBox.FuzzyMatchMode selectedMode = (SortedJXComboBox.FuzzyMatchMode) modeCombo.getSelectedItem();
                    comboBox.setFuzzyMatchMode(selectedMode);
                    log("切换到模式: " + selectedMode);
                } catch (Exception ex) {
                    log("模式切换失败: " + ex.getMessage());
                }
            });
        });
        panel.add(modeCombo, gbc);

        // 大小写敏感
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("大小写敏感:"), gbc);

        gbc.gridx = 1;
        JCheckBox caseSensitiveBox = new JCheckBox();
        caseSensitiveBox.addActionListener(e -> {
            comboBox.setCaseSensitive(caseSensitiveBox.isSelected());
            log("大小写敏感: " + caseSensitiveBox.isSelected());
        });
        panel.add(caseSensitiveBox, gbc);

        // 自动完成开关
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("自动完成:"), gbc);

        gbc.gridx = 1;
        JCheckBox autoCompleteBox = new JCheckBox();
        autoCompleteBox.setSelected(true);
        autoCompleteBox.addActionListener(e -> {
            comboBox.setAutoCompleteEnabled(autoCompleteBox.isSelected());
            log("自动完成: " + autoCompleteBox.isSelected());
        });
        panel.add(autoCompleteBox, gbc);

        // 测试按钮
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton testPrefixBtn = new JButton("测试前缀匹配 (App)");
        testPrefixBtn.addActionListener(e -> testMatching("App", SortedJXComboBox.FuzzyMatchMode.PREFIX));
        buttonPanel.add(testPrefixBtn);

        JButton testContainsBtn = new JButton("测试包含匹配 (Sam)");
        testContainsBtn.addActionListener(e -> testMatching("Sam", SortedJXComboBox.FuzzyMatchMode.CONTAINS));
        buttonPanel.add(testContainsBtn);

        JButton testWildcardBtn = new JButton("测试通配符 (*P*)");
        testWildcardBtn.addActionListener(e -> testMatching("*P*", SortedJXComboBox.FuzzyMatchMode.WILDCARD));
        buttonPanel.add(testWildcardBtn);

        JButton clearBtn = new JButton("清空日志");
        clearBtn.addActionListener(e -> outputArea.setText(""));
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void setupEventHandlers() {
        comboBox.addActionListener(e -> {
            Object selected = comboBox.getSelectedItem();
            log("选择了: " + selected);
        });
    }

    private void testMatching(String pattern, SortedJXComboBox.FuzzyMatchMode mode) {
        try {
            comboBox.setFuzzyMatchMode(mode);

            // 使用SwingUtilities.invokeLater确保在EDT上执行
            SwingUtilities.invokeLater(() -> {
                try {
                    comboBox.getEditor().setItem(pattern);
                    log("测试模式 " + mode + " 使用模式: " + pattern);
                } catch (Exception ex) {
                    log("测试失败: " + ex.getMessage());
                }
            });
        } catch (Exception ex) {
            log("模式设置失败: " + ex.getMessage());
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new SortedComboBoxDemo().setVisible(true);
        });
    }
}