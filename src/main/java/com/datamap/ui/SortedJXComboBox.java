package com.datamap.ui;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/**
 * 自动排序且默认支持输入检索过滤的JXComboBox实现
 */
public class SortedJXComboBox<E extends Comparable<E>> extends JXComboBox {

    private Comparator<E> comparator;
    private boolean autoCompleteEnabled = true; // 默认为true
    private boolean strictMatching = false;

    /**
     * 创建一个空的排序ComboBox，使用自然排序
     */
    public SortedJXComboBox() {
        super(new DefaultComboBoxModel<E>());
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        initAutoComplete(); // 初始化自动完成功能
    }

    /**
     * 使用指定比较器创建排序ComboBox
     */
    public SortedJXComboBox(Comparator<E> comparator) {
        super(new DefaultComboBoxModel<E>());
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        initAutoComplete(); // 初始化自动完成功能
    }

    /**
     * 使用指定项创建排序ComboBox
     */
    public SortedJXComboBox(E[] items) {
        super(createSortedModel(items, (Comparator<E>) Comparator.naturalOrder()));
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        initAutoComplete(); // 初始化自动完成功能
    }

    /**
     * 使用指定项和比较器创建排序ComboBox
     */
    public SortedJXComboBox(E[] items, Comparator<E> comparator) {
        super(createSortedModel(items, comparator));
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        initAutoComplete(); // 初始化自动完成功能
    }

    /**
     * 使用Vector创建排序ComboBox
     */
    public SortedJXComboBox(Vector<E> items) {
        super(createSortedModel(items, (Comparator<E>) Comparator.naturalOrder()));
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        initAutoComplete(); // 初始化自动完成功能
    }

    /**
     * 使用ComboBoxModel创建排序ComboBox
     */
    public SortedJXComboBox(ComboBoxModel model) {
        super(new DefaultComboBoxModel<>());
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        setSortedModel(model);
        initAutoComplete(); // 初始化自动完成功能
    }

    /**
     * 初始化自动完成功能
     */
    private void initAutoComplete() {
        // 关键修复：必须先设置为可编辑，然后应用装饰器
        setEditable(true);
        AutoCompleteDecorator.decorate(this);
    }

    // 静态方法创建排序后的模型 (数组版)
    private static <E> DefaultComboBoxModel<E> createSortedModel(E[] items, Comparator<E> comparator) {
        // 安全检查
        if (items == null || items.length == 0) {
            return new DefaultComboBoxModel<>();
        }

        // 使用提供的比较器排序
        Comparator<E> comp = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        E[] sortedItems = Arrays.copyOf(items, items.length);
        Arrays.sort(sortedItems, comp);

        return new DefaultComboBoxModel<>(sortedItems);
    }

    // 静态方法创建排序后的模型 (Vector版)
    private static <E> DefaultComboBoxModel<E> createSortedModel(Vector<E> items, Comparator<E> comparator) {
        // 安全检查
        if (items == null || items.isEmpty()) {
            return new DefaultComboBoxModel<>();
        }

        // 使用提供的比较器排序
        Comparator<E> comp = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        Vector<E> sortedItems = new Vector<>(items);
        sortedItems.sort(comp);

        return new DefaultComboBoxModel<>(sortedItems);
    }

    /**
     * 根据比较器设置排序后的模型
     */
    private void setSortedModel(ComboBoxModel model) {
        // 如果模型为null，设置一个空模型
        if (model == null) {
            setModel(new DefaultComboBoxModel<>());
            return;
        }

        // 收集现有项目
        Vector<E> items = new Vector<>();
        for (int i = 0; i < model.getSize(); i++) {
            items.add((E) model.getElementAt(i));
        }

        // 排序并设置新模型
        items.sort(comparator);
        setModel(new DefaultComboBoxModel<>(items));
    }

    /**
     * 重写添加项方法，保持排序
     */
    @Override
    public void addItem(Object item) {
        // 如果是null项，直接添加
        if (item == null) {
            super.addItem(null);
            return;
        }

        // 获取当前所有项
        Vector<E> items = getAllItems();

        // 添加新项目
        items.add((E) item);
        items.sort(comparator);

        // 记住选中项
        Object selectedItem = getSelectedItem();

        // 设置新模型
        setModel(new DefaultComboBoxModel<>(items));

        // 恢复选中状态
        if (selectedItem != null) {
            setSelectedItem(selectedItem);
        }

        // 如果启用了自动完成，确保控件仍然处于可编辑状态
        if (this.autoCompleteEnabled) {
            setEditable(true);
        }
    }

    /**
     * 收集当前所有项目
     */
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

    /**
     * 设置自定义比较器
     */
    public void setComparator(Comparator<E> comparator) {
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
        // 使用新比较器重新排序
        Vector<E> items = getAllItems();
        if (!items.isEmpty()) {
            items.sort(this.comparator);
            setModel(new DefaultComboBoxModel<>(items));

            // 如果启用了自动完成，确保控件仍然处于可编辑状态
            if (this.autoCompleteEnabled) {
                setEditable(true);
            }
        }
    }

    /**
     * 启用或禁用输入检索过滤功能
     * @param enable 是否启用
     */
    public void setAutoCompleteEnabled(boolean enable) {
        if (this.autoCompleteEnabled != enable) {
            this.autoCompleteEnabled = enable;
            if (enable) {
                // 确保先设置为可编辑，然后应用装饰器
                setEditable(true);
                AutoCompleteDecorator.decorate(this);
            } else {
                // 注意：一旦启用了AutoComplete，就无法简单地禁用它
                // 需要重新创建一个模型并设置所有项
                Vector<E> items = getAllItems();
                Object selected = getSelectedItem();
                setEditable(false);

                // 重设模型以清除AutoComplete装饰
                DefaultComboBoxModel<E> newModel = new DefaultComboBoxModel<>();
                for (E item : items) {
                    newModel.addElement(item);
                }
                setModel(newModel);

                if (selected != null) {
                    setSelectedItem(selected);
                }
            }
        }
    }

    /**
     * 启用输入检索过滤功能，并配置是否使用严格匹配模式
     * @param enable 是否启用
     * @param strictMatching true表示仅匹配前缀，false表示匹配任何位置
     */
    public void setAutoCompleteEnabled(boolean enable, boolean strictMatching) {
        this.strictMatching = strictMatching;
        setAutoCompleteEnabled(enable);
    }

    /**
     * 启用输入检索过滤功能，使用自定义字符串转换器
     * @param enable 是否启用
     * @param converter 自定义的对象到字符串转换器，用于匹配
     */
    public void setAutoCompleteEnabled(boolean enable, ObjectToStringConverter converter) {
        if (enable) {
            setEditable(true);
            AutoCompleteDecorator.decorate(this, converter);
        } else {
            setAutoCompleteEnabled(false);
        }
        this.autoCompleteEnabled = enable;
    }

    /**
     * 检查是否已启用自动完成/过滤
     * @return 是否已启用
     */
    public boolean isAutoCompleteEnabled() {
        return autoCompleteEnabled;
    }

    /**
     * 检查是否使用严格匹配模式
     * @return 是否使用严格匹配
     */
    public boolean isStrictMatching() {
        return strictMatching;
    }

    /**
     * 重写setEditable方法，确保状态一致性
     */
    @Override
    public void setEditable(boolean aFlag) {
        super.setEditable(aFlag);
        if (!aFlag) {
            this.autoCompleteEnabled = false;
        }
    }
}