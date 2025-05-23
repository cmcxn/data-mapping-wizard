package com.datamap.ui;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 自动排序且支持模糊匹配的JXComboBox实现
 * 修复版本 - 解决UI卡死和数据异常问题
 */
public class SortedJXComboBox<E extends Comparable<E>> extends JXComboBox {

    private Comparator<E> comparator;
    private boolean autoCompleteEnabled = true;
    private boolean strictMatching = false;
    private FuzzyMatchMode fuzzyMatchMode = FuzzyMatchMode.PREFIX;
    private boolean caseSensitive = false;
    private Vector<E> originalItems = new Vector<>();
    private ObjectToStringConverter stringConverter;

    // 状态控制标志
    private volatile boolean isUpdatingModel = false;
    private volatile boolean isUserTyping = false;
    private String lastFilterText = "";

    // 防抖定时器
    private Timer filterTimer;
    private static final int FILTER_DELAY = 200; // 200ms延迟

    /**
     * 模糊匹配模式枚举
     */
    public enum FuzzyMatchMode {
        PREFIX,     // 前缀匹配（默认）
        CONTAINS,   // 包含匹配
        WILDCARD,   // 通配符匹配（支持 * 和 ?）
        REGEX       // 正则表达式匹配
    }

    public SortedJXComboBox() {
        super(new DefaultComboBoxModel<E>());
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        setFuzzyMatchMode(FuzzyMatchMode.CONTAINS);
        initComponents();
    }

    public SortedJXComboBox(Comparator<E> comparator) {
        super(new DefaultComboBoxModel<E>());
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        initComponents();
    }

    public SortedJXComboBox(E[] items) {
        super(createSortedModel(items, (Comparator<E>) Comparator.naturalOrder()));
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        if (items != null) {
            this.originalItems = new Vector<>(Arrays.asList(items));
        }
        initComponents();
    }

    public SortedJXComboBox(E[] items, Comparator<E> comparator) {
        super(createSortedModel(items, comparator));
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        if (items != null) {
            this.originalItems = new Vector<>(Arrays.asList(items));
        }
        initComponents();
    }

    public SortedJXComboBox(Vector<E> items) {
        super(createSortedModel(items, (Comparator<E>) Comparator.naturalOrder()));
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        if (items != null) {
            this.originalItems = new Vector<>(items);
        }
        initComponents();
    }

    public SortedJXComboBox(ComboBoxModel model) {
        super(new DefaultComboBoxModel<>());
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        setSortedModel(model);
        initComponents();
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        // 初始化防抖定时器
        filterTimer = new Timer(FILTER_DELAY, e -> performDelayedFiltering());
        filterTimer.setRepeats(false);

        initAutoComplete();
    }

    /**
     * 初始化自动完成功能
     */
    private void initAutoComplete() {
        setEditable(true);

        if (fuzzyMatchMode == FuzzyMatchMode.PREFIX) {
            // 使用默认的AutoCompleteDecorator
            try {
                AutoCompleteDecorator.decorate(this);
            } catch (Exception e) {
                System.err.println("AutoCompleteDecorator装饰失败: " + e.getMessage());
            }
        } else {
            // 使用自定义编辑器支持模糊匹配
            setEditor(new FuzzyMatchComboBoxEditor());
        }
    }

    /**
     * 自定义编辑器，支持模糊匹配
     */
    private class FuzzyMatchComboBoxEditor extends BasicComboBoxEditor {
        private JTextField textField;
        private DocumentListener documentListener;

        public FuzzyMatchComboBoxEditor() {
            super();

            if (editor instanceof JTextField) {
                textField = (JTextField) editor;
                setupDocumentListener();
                setupFocusListener();
            }
        }

        private void setupDocumentListener() {
            documentListener = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    handleDocumentChange();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    handleDocumentChange();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    handleDocumentChange();
                }

                private void handleDocumentChange() {
                    if (!isUpdatingModel && autoCompleteEnabled && fuzzyMatchMode != FuzzyMatchMode.PREFIX) {
                        isUserTyping = true;
                        scheduleFiltering();
                    }
                }
            };

            if (textField != null) {
                textField.getDocument().addDocumentListener(documentListener);
            }
        }

        private void setupFocusListener() {
            if (textField != null) {
                textField.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        // 获得焦点时，如果是模糊匹配模式，显示所有项目
                        if (fuzzyMatchMode != FuzzyMatchMode.PREFIX && !isUpdatingModel) {
                            SwingUtilities.invokeLater(() -> {
                                if (textField.getText().trim().isEmpty()) {
                                    resetToOriginalItems();
                                }
                            });
                        }
                    }
                });
            }
        }

        @Override
        public void setItem(Object anObject) {
            try {
                isUpdatingModel = true;
                if (textField != null && anObject != null) {
                    String text = anObject.toString();
                    textField.setText(text);
                    textField.setCaretPosition(text.length());
                }
            } catch (Exception e) {
                System.err.println("设置编辑器项目时出错: " + e.getMessage());
            } finally {
                isUpdatingModel = false;
            }
        }

        @Override
        public Object getItem() {
            if (textField != null) {
                return textField.getText();
            }
            return super.getItem();
        }
    }

    /**
     * 计划执行过滤（防抖）
     */
    private void scheduleFiltering() {
        if (filterTimer != null) {
            filterTimer.stop();
            filterTimer.start();
        }
    }

    /**
     * 延迟执行过滤
     */
    private void performDelayedFiltering() {
        if (!isUpdatingModel && isUserTyping) {
            String currentText = getCurrentEditorText();
            if (!Objects.equals(currentText, lastFilterText)) {
                lastFilterText = currentText;
                performFiltering(currentText);
            }
            isUserTyping = false;
        }
    }

    /**
     * 获取当前编辑器文本
     */
    private String getCurrentEditorText() {
        try {
            ComboBoxEditor editor = getEditor();
            if (editor != null) {
                Object item = editor.getItem();
                return item != null ? item.toString() : "";
            }
        } catch (Exception e) {
            System.err.println("获取编辑器文本时出错: " + e.getMessage());
        }
        return "";
    }

    /**
     * 执行实际的过滤操作
     */
    private void performFiltering(String inputText) {
        if (isUpdatingModel) return;

        try {
            isUpdatingModel = true;

            if (inputText == null || inputText.trim().isEmpty()) {
                resetToOriginalItemsInternal();
                return;
            }

            Vector<E> filteredItems = new Vector<>();
            String searchText = caseSensitive ? inputText : inputText.toLowerCase();

            for (E item : originalItems) {
                if (item == null) continue;

                String itemText = objectToString(item);
                if (!caseSensitive) {
                    itemText = itemText.toLowerCase();
                }

                if (matchesPattern(itemText, searchText)) {
                    filteredItems.add(item);
                }
            }

            // 排序过滤后的项目
            filteredItems.sort(comparator);

            // 更新模型
            SwingUtilities.invokeLater(() -> updateModelSafely(filteredItems, inputText));

        } catch (Exception e) {
            System.err.println("过滤时出错: " + e.getMessage());
        } finally {
            isUpdatingModel = false;
        }
    }

    /**
     * 安全地更新模型
     */
    private void updateModelSafely(Vector<E> filteredItems, String inputText) {
        try {
            isUpdatingModel = true;

            DefaultComboBoxModel<E> newModel = new DefaultComboBoxModel<>(filteredItems);
            setModel(newModel);

            // 恢复编辑器文本
            ComboBoxEditor editor = getEditor();
            if (editor != null && editor.getEditorComponent() instanceof JTextComponent) {
                JTextComponent textComp = (JTextComponent) editor.getEditorComponent();
                if (!Objects.equals(textComp.getText(), inputText)) {
                    textComp.setText(inputText);
                    textComp.setCaretPosition(inputText.length());
                }
            }

            // 显示或隐藏下拉列表
            if (!filteredItems.isEmpty()) {
                if (!isPopupVisible()) {
                    showPopup();
                }
            } else {
                hidePopup();
            }

        } catch (Exception e) {
            System.err.println("更新模型时出错: " + e.getMessage());
        } finally {
            isUpdatingModel = false;
        }
    }

    /**
     * 检查项目是否匹配搜索模式
     */
    private boolean matchesPattern(String itemText, String searchText) {
        try {
            switch (fuzzyMatchMode) {
                case PREFIX:
                    return itemText.startsWith(searchText);

                case CONTAINS:
                    return itemText.contains(searchText);

                case WILDCARD:
                    return matchesWildcard(itemText, searchText);

                case REGEX:
                    return matchesRegex(itemText, searchText);

                default:
                    return itemText.startsWith(searchText);
            }
        } catch (Exception e) {
            return itemText.contains(searchText);
        }
    }

    private boolean matchesWildcard(String text, String pattern) {
        try {
            String regex = pattern
                    .replace(".", "\\.")
                    .replace("*", ".*")
                    .replace("?", ".");

            Pattern compiledPattern = Pattern.compile(regex,
                    caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            return compiledPattern.matcher(text).matches();
        } catch (PatternSyntaxException e) {
            return text.contains(pattern);
        }
    }

    private boolean matchesRegex(String text, String pattern) {
        try {
            Pattern compiledPattern = Pattern.compile(pattern,
                    caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
            return compiledPattern.matcher(text).find();
        } catch (PatternSyntaxException e) {
            return text.contains(pattern);
        }
    }

    /**
     * 重置为原始项目列表
     */
    private void resetToOriginalItems() {
        if (!isUpdatingModel) {
            SwingUtilities.invokeLater(this::resetToOriginalItemsInternal);
        }
    }

    private void resetToOriginalItemsInternal() {
        try {
            isUpdatingModel = true;
            Vector<E> sortedItems = new Vector<>(originalItems);
            sortedItems.sort(comparator);
            setModel(new DefaultComboBoxModel<>(sortedItems));
        } catch (Exception e) {
            System.err.println("重置原始项目时出错: " + e.getMessage());
        } finally {
            isUpdatingModel = false;
        }
    }

    /**
     * 将对象转换为字符串
     */
    private String objectToString(E obj) {
        if (stringConverter != null) {
            return stringConverter.getPreferredStringForItem(obj);
        }
        return obj != null ? obj.toString() : "";
    }

    // 静态方法创建排序后的模型
    private static <E> DefaultComboBoxModel<E> createSortedModel(E[] items, Comparator<E> comparator) {
        if (items == null || items.length == 0) {
            return new DefaultComboBoxModel<>();
        }

        Comparator<E> comp = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        E[] sortedItems = Arrays.copyOf(items, items.length);
        Arrays.sort(sortedItems, comp);

        return new DefaultComboBoxModel<>(sortedItems);
    }

    private static <E> DefaultComboBoxModel<E> createSortedModel(Vector<E> items, Comparator<E> comparator) {
        if (items == null || items.isEmpty()) {
            return new DefaultComboBoxModel<>();
        }

        Comparator<E> comp = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        Vector<E> sortedItems = new Vector<>(items);
        sortedItems.sort(comp);

        return new DefaultComboBoxModel<>(sortedItems);
    }

    private void setSortedModel(ComboBoxModel model) {
        if (model == null) {
            setModel(new DefaultComboBoxModel<>());
            return;
        }

        Vector<E> items = new Vector<>();
        for (int i = 0; i < model.getSize(); i++) {
            items.add((E) model.getElementAt(i));
        }

        this.originalItems = new Vector<>(items);
        items.sort(comparator);
        setModel(new DefaultComboBoxModel<>(items));
    }

    @Override
    public void addItem(Object item) {
        if (item == null) {
            super.addItem(null);
            return;
        }

        originalItems.add((E) item);

        Vector<E> items = getAllItems();
        items.add((E) item);
        items.sort(comparator);

        Object selectedItem = getSelectedItem();
        setModel(new DefaultComboBoxModel<>(items));

        if (selectedItem != null) {
            setSelectedItem(selectedItem);
        }
    }

    private Vector<E> getAllItems() {
        Vector<E> items = new Vector<>();
        ComboBoxModel model = getModel();
        if (model != null) {
            for (int i = 0; i < model.getSize(); i++) {
                items.add((E) model.getElementAt(i));
            }
        }
        return items;
    }

    public void setComparator(Comparator<E> comparator) {
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        Vector<E> items = new Vector<>(originalItems);
        if (!items.isEmpty()) {
            items.sort(this.comparator);
            setModel(new DefaultComboBoxModel<>(items));
        }
    }

    public void setFuzzyMatchMode(FuzzyMatchMode mode) {
        if (filterTimer != null) {
            filterTimer.stop();
        }

        this.fuzzyMatchMode = mode != null ? mode : FuzzyMatchMode.PREFIX;
        lastFilterText = "";

        // 重新初始化编辑器
        if (autoCompleteEnabled) {
            initAutoComplete();
        }

        // 重置为原始状态
        resetToOriginalItems();
    }

    public FuzzyMatchMode getFuzzyMatchMode() {
        return fuzzyMatchMode;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setStringConverter(ObjectToStringConverter converter) {
        this.stringConverter = converter;
    }

    public Vector<E> getOriginalItems() {
        return new Vector<>(originalItems);
    }

    public void setAutoCompleteEnabled(boolean enable) {
        if (this.autoCompleteEnabled != enable) {
            this.autoCompleteEnabled = enable;
            if (enable) {
                initAutoComplete();
            } else {
                setEditable(false);
                resetToOriginalItems();
            }
        }
    }

    public void setAutoCompleteEnabled(boolean enable, boolean strictMatching) {
        this.strictMatching = strictMatching;
        setAutoCompleteEnabled(enable);
    }

    public void setAutoCompleteEnabled(boolean enable, ObjectToStringConverter converter) {
        this.stringConverter = converter;
        setAutoCompleteEnabled(enable);
    }

    public boolean isAutoCompleteEnabled() {
        return autoCompleteEnabled;
    }

    public boolean isStrictMatching() {
        return strictMatching;
    }

    @Override
    public void setEditable(boolean aFlag) {
        super.setEditable(aFlag);
        if (!aFlag) {
            this.autoCompleteEnabled = false;
        }
    }

    /**
     * 清理资源
     */
    public void dispose() {
        if (filterTimer != null) {
            filterTimer.stop();
            filterTimer = null;
        }
    }
}