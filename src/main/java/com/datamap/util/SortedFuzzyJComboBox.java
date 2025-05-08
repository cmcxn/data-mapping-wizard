package com.datamap.util;import org.jdesktop.swingx.JXComboBox;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * 自定义组件，继承自JXComboBox，提供自动升序排列和模糊搜索功能
 */
public class SortedFuzzyJComboBox extends JXComboBox {

    private final List<Object> originalItems = new ArrayList<>();
    private boolean isFuzzySearchEnabled = true;
    private boolean isInternalUpdate = false;

    /**
     * 默认构造函数
     */
    public SortedFuzzyJComboBox() {
        super();
        initializeComponent();
    }

    /**
     * 使用数组初始化的构造函数
     * @param items 初始项目数组
     */
    public SortedFuzzyJComboBox(Object[] items) {
        super(items); // 直接使用父类构造函数添加初始项目
        initializeComponent();
        if (items != null && items.length > 0) {
            // 存储原始项目用于排序和搜索
            originalItems.addAll(Arrays.asList(items));
            sortItems(); // 对项目进行排序
        }
    }

    /**
     * 使用集合初始化的构造函数
     * @param items 初始项目集合
     */
    public SortedFuzzyJComboBox(Collection<?> items) {
        super();
        initializeComponent();
        setItems(items);
    }

    /**
     * 初始化组件，设置事件监听器和行为
     */
    private void initializeComponent() {
        setEditable(true);
        setupFuzzySearch();
    }

    /**
     * 设置模糊搜索功能
     */
    private void setupFuzzySearch() {
        JTextComponent editor = (JTextComponent) getEditor().getEditorComponent();

        // 添加文档监听器，当输入文字时执行过滤
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (isFuzzySearchEnabled && !isInternalUpdate) filterItems();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isFuzzySearchEnabled && !isInternalUpdate) filterItems();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (isFuzzySearchEnabled && !isInternalUpdate) filterItems();
            }

            private void filterItems() {
                SwingUtilities.invokeLater(() -> {
                    String searchText = editor.getText().toLowerCase();

                    if (searchText.isEmpty()) {
                        // 如果搜索文本为空，显示所有项目
                        resetToOriginalItems();
                    } else {
                        // 过滤并排序匹配的项目
                        filterAndSortItems(searchText);
                    }
                });
            }
        });

        // 添加键盘监听器，提高用户体验
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // 处理回车键，在某些场景下可能需要选择第一个匹配项
                    if (getItemCount() > 0 && isPopupVisible()) {
                        setSelectedIndex(0);
                        hidePopup();
                    }
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // 处理ESC键，隐藏下拉菜单
                    if (isPopupVisible()) {
                        hidePopup();
                        e.consume();
                    }
                }
            }
        });
    }

    /**
     * 根据搜索文本过滤并排序项目
     * @param searchText 搜索文本
     */
    private void filterAndSortItems(String searchText) {
        // 临时禁用模糊搜索以避免递归调用
        isInternalUpdate = true;

        // 保存当前编辑器文本
        String currentText = ((JTextComponent)getEditor().getEditorComponent()).getText();

        // 清除当前模型
        DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
        model.removeAllElements();

        // 过滤并排序匹配的项目
        List<Object> matchingItems = new ArrayList<>();
        for (Object item : originalItems) {
            if (item != null && item.toString().toLowerCase().contains(searchText)) {
                matchingItems.add(item);
            }
        }

        // 尝试排序匹配项目，仅当所有项目都是Comparable时
        sortObjectList(matchingItems);

        // 将匹配项添加到模型
        for (Object item : matchingItems) {
            model.addElement(item);
        }

        // 恢复编辑器文本
        ((JTextComponent)getEditor().getEditorComponent()).setText(currentText);

        // 如果有匹配项并且组件正在显示，则显示下拉菜单
        if (getItemCount() > 0 && isShowing() && isDisplayable()) {
            try {
                showPopup();
            } catch (IllegalComponentStateException e) {
                // 忽略此错误，组件可能尚未完全显示
                System.out.println("组件尚未完全显示，跳过显示下拉菜单");
            }
        }

        // 恢复模糊搜索功能
        isInternalUpdate = false;
    }

    /**
     * 重置为原始项目列表
     */
    private void resetToOriginalItems() {
        isInternalUpdate = true;

        // 清除当前模型
        DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
        model.removeAllElements();

        // 排序原始项目
        List<Object> sortedItems = new ArrayList<>(originalItems);
        sortObjectList(sortedItems);

        // 添加所有原始项目
        for (Object item : sortedItems) {
            model.addElement(item);
        }

        isInternalUpdate = false;
    }

    /**
     * 设置下拉框的项目，自动排序
     * @param items 要设置的项目数组
     */
    public void setItems(Object[] items) {
        if (items != null) {
            setItems(Arrays.asList(items));
        }
    }

    /**
     * 设置下拉框的项目，自动排序
     * @param items 要设置的项目集合
     */
    public void setItems(Collection<?> items) {
        isInternalUpdate = true;

        // 清除原有项目
        originalItems.clear();
        removeAllItems();

        if (items != null && !items.isEmpty()) {
            // 添加新项目到原始集合
            originalItems.addAll(items);

            // 创建排序副本
            List<Object> sortedItems = new ArrayList<>(originalItems);
            sortObjectList(sortedItems);

            // 添加排序后的项目到模型
            DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
            for (Object item : sortedItems) {
                model.addElement(item);
            }

            // 如果有项目，选择第一个
            if (getItemCount() > 0) {
                setSelectedIndex(0);
            }
        }

        isInternalUpdate = false;

        // 调试输出
        System.out.println("SortedFuzzyComboBox: 设置了 " + originalItems.size() + " 个项目，显示了 " + getItemCount() + " 个项目");
    }

    /**
     * 添加项目，保持升序排列
     */
    @Override
    public void addItem(Object item) {
        if (!originalItems.contains(item)) {
            // 添加到原始集合
            originalItems.add(item);

            // 仅在非过滤模式下才添加并重新排序
            if (!isInternalUpdate) {
                // 调用默认模型添加项目
                super.addItem(item);
                sortModel();
            }
        }
    }

    /**
     * 对下拉框中的项目进行排序
     */
    private void sortModel() {
        if (originalItems.isEmpty()) {
            return;
        }

        isInternalUpdate = true;

        // 获取当前选中的项目
        Object selectedItem = getSelectedItem();

        // 创建排序副本
        List<Object> sortedItems = new ArrayList<>(originalItems);
        sortObjectList(sortedItems);

        // 清除所有项目
        removeAllItems();

        // 重新添加排序后的项目
        for (Object item : sortedItems) {
            super.addItem(item);
        }

        // 恢复之前选中的项目
        setSelectedItem(selectedItem);

        isInternalUpdate = false;
    }

    /**
     * 对项目列表进行排序
     */
    private void sortItems() {
        if (!originalItems.isEmpty()) {
            sortObjectList(originalItems);
            resetToOriginalItems();
        }
    }

    /**
     * 尝试对对象列表进行排序，只有当所有对象都是Comparable时才会排序
     * @param items 要排序的项目列表
     */
    @SuppressWarnings("unchecked")
    private void sortObjectList(List<Object> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        boolean allComparable = true;

        // 检查所有项目是否都实现了Comparable接口
        for (Object item : items) {
            if (!(item instanceof Comparable)) {
                allComparable = false;
                break;
            }
        }

        // 如果所有项目都是可比较的，执行排序
        if (allComparable) {
            try {
                Collections.sort((List)items);
            } catch (Exception e) {
                System.err.println("排序出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置是否启用模糊搜索
     * @param enabled 是否启用
     */
    public void setFuzzySearchEnabled(boolean enabled) {
        this.isFuzzySearchEnabled = enabled;
        if (!enabled) {
            resetToOriginalItems();
        }
    }

    /**
     * 获取模糊搜索状态
     * @return 是否启用模糊搜索
     */
    public boolean isFuzzySearchEnabled() {
        return isFuzzySearchEnabled;
    }

    /**
     * 打印当前状态，用于调试
     */
    public void printDebugInfo() {
        System.out.println("===== SortedFuzzyComboBox 调试信息 =====");
        System.out.println("原始项目数量: " + originalItems.size());
        System.out.println("显示项目数量: " + getItemCount());
        System.out.println("原始项目内容: " + originalItems);
        System.out.println("=====================================");
    }
}