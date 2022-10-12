package com.walkertribe.artemisdisplay.i18n;

import java.util.Locale;

import javax.swing.JComboBox;

/**
 * A JComboBox that will update its options when you invoke setLocale(). This is done by wrapping
 * each value in the combo box with an object that handles the localization of the label. The
 * wrapper objects are of type LocalizedComboBox.Item<T>, and the underlying value of the currently
 * selected item is available by calling getSelectedValue(). There's also a setSelectedValue()
 * method.
 * @author rjwut
 */
public abstract class LocalizedComboBox<T> extends JComboBox<LocalizedComboBox.Item<T>> {
  private static final long serialVersionUID = 2545094608836031786L;

  private String keyPrefix;

  /**
   * Returns the key that represents this value. This will be combined with "configDialog.<select>."
   * to produce the full localization key.
   */
  protected abstract String getValueKey(T value);

  /**
   * Creates a new LocalizedCombo box, initially rendered in the given Locale.
   */
  protected LocalizedComboBox(String selectName, T[] values) {
    keyPrefix = "configDialog." + selectName + ".";
    super.setLocale(LocaleData.get().getLocale());

    for (T value : values) {
      addItem(new Item<T>(this, value));
    }
  }

  @Override
  public void setLocale(Locale locale) {
    super.setLocale(locale);
    int itemCount = getItemCount();
    int selectedIndex = getSelectedIndex();

    for (int i = 0; i < itemCount; i++) {
      Item<T> item = getItemAt(i);
      removeItemAt(i);
      insertItemAt(item, i);
    }

    setSelectedIndex(selectedIndex);
  }

  /**
   * Returns the currently selected value. This is the value wrapped by the currently selected item.
   */
  @SuppressWarnings("unchecked")
  public T getSelectedValue() {
    return ((Item<T>) getSelectedItem()).value;
  }

  /**
   * Sets the currently selected value.
   */
  public void setSelectedValue(T value) {
    int itemCount = getItemCount();

    for (int i = 0; i < itemCount; i++) {
      Item<T> item = getItemAt(i);

      if (item.value == value) {
        setSelectedIndex(i);
        return;
      }
    }

    setSelectedIndex(0);
  }

  /**
   * Returns the label for the given value.
   */
  String getLabel(T value) {
    return LocaleData.get().string(keyPrefix + getValueKey(value));
  }

  /**
   * A wrapper class for the items in the LocalizedComboBox.
   */
  public static class Item<T> {
    private LocalizedComboBox<T> comboBox;
    private T value;

    /**
     * Create a new Item that represents the given value.
     */
    private Item(LocalizedComboBox<T> comboBox, T value) {
      this.comboBox = comboBox;
      this.value = value;
    }

    @Override
    public String toString() {
      return comboBox.getLabel(value);
    }
  }
}
