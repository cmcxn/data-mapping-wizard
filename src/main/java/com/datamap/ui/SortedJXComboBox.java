package com.datamap.ui;

import org.jdesktop.swingx.JXComboBox;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/**
 * 自动排序的JXComboBox实现
 */
public class SortedJXComboBox<E extends Comparable<E>> extends JXComboBox {

    private Comparator<E> comparator;

    /**
     * 创建一个空的排序ComboBox，使用自然排序
     */
    public SortedJXComboBox() {
        super(new DefaultComboBoxModel<E>());
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
    }

    /**
     * 使用指定比较器创建排序ComboBox
     */
    public SortedJXComboBox(Comparator<E> comparator) {
        super(new DefaultComboBoxModel<E>());
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
    }

    /**
     * 使用指定项创建排序ComboBox
     */
    public SortedJXComboBox(E[] items) {
        // 直接将排序后的数组传递给父类构造函数
        super(createSortedModel(items, (Comparator<E>) Comparator.naturalOrder()));
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
    }

    /**
     * 使用指定项和比较器创建排序ComboBox
     */
    public SortedJXComboBox(E[] items, Comparator<E> comparator) {
        // 直接将排序后的数组传递给父类构造函数
        super(createSortedModel(items, comparator));
        this.comparator = comparator != null ? comparator : (Comparator<E>) Comparator.naturalOrder();
    }

    /**
     * 使用Vector创建排序ComboBox
     */
    public SortedJXComboBox(Vector<E> items) {
        // 直接将排序后的Vector传递给父类构造函数
        super(createSortedModel(items, (Comparator<E>) Comparator.naturalOrder()));
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
    }

    /**
     * 使用ComboBoxModel创建排序ComboBox
     */
    public SortedJXComboBox(ComboBoxModel model) {
        // 先用空模型初始化，然后再设置排序后的模型
        super(new DefaultComboBoxModel<>());
        this.comparator = (Comparator<E>) Comparator.naturalOrder();
        setSortedModel(model);
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
        }
    }
}