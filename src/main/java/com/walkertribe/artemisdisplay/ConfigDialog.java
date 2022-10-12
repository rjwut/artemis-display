package com.walkertribe.artemisdisplay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import com.walkertribe.artemisdisplay.display.Display;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.artemisdisplay.i18n.LocalizedComboBox;
import com.walkertribe.ian.protocol.udp.Server;
import com.walkertribe.ian.protocol.udp.ServerDiscoveryRequester;

/**
 * Window that allows the user to set configuration options.
 * @author rjwut
 */
public class ConfigDialog extends JFrame {
  private static final long serialVersionUID = -2556896504978355092L;

  private static final int MARGIN = 6;

  private Configuration config;
  private LocaleData localeData;
  private JTabbedPane tabs;
  private JLabel hostLabel;
  private JTextField hostField;
  private JButton scanButton;
  private DefaultListModel<Server> serverListModel;
  private JList<Server> serverList;
  private JLabel shipNumberLabel;
  private JSpinner shipNumberField;
  private JLabel artemisInstallLabel;
  private JButton artemisInstallButton;
  private JFileChooser artemisInstallFileChooser;
  private JLabel modeLabel;
  private WindowModeComboBox modeList;
  private JLabel monitorLabel;
  private JSpinner monitorField;
  private JButton identifyButton;
  private JLabel displayLabel;
  private DisplayComboBox displayList;
  private JLabel layoutFileLabel;
  private JButton layoutFileButton;
  private JFileChooser layoutFileChooser;
  private JLabel renderOptionsLabel;
  private Map<Configuration.RenderOption, JCheckBox> renderOptionCheckboxes;
  private JComboBox<LocaleSelection> localeList;
  private JButton startButton;
  private Consumer<Configuration> launchFn;

  /**
   * Displays the configuration dialog using the given Configuration object for initial settings.
   * When the user clicks the "Launch" button, the given launchFn Consumer will be invoked.
   */
  public ConfigDialog(Configuration config, Consumer<Configuration> launchFn) {
    super("Artemis Display");
    this.config = config;
    localeData = LocaleData.get();
    this.launchFn = launchFn;

    try {
      build();
      setFields();
    } catch (RuntimeException ex) {
      dispose();
      throw ex;
    }

    setLocationRelativeTo(null);
    setVisible(true);
    scan();
  }

  /**
   * Updates the interface to use the current locale.
   */
  private void applyLocale() {
    localeData = LocaleData.get();
    tabs.setTitleAt(0, localeData.string("configDialog.connect"));
    tabs.setTitleAt(1, localeData.string("configDialog.advanced"));
    setLabel(hostLabel, "host");
    setToolTip(hostField, "host");
    setButtonText(scanButton, "scan");
    setToolTip(scanButton, "scan");
    setLabel(shipNumberLabel, "shipNumber");
    setToolTip(shipNumberField, "shipNumber");
    setLabel(artemisInstallLabel, "artemisInstall");
    setFileLabel(config.getArtemisInstallPath(), artemisInstallButton);
    setToolTip(artemisInstallButton, "artemisInstall");
    setLabel(modeLabel, "windowMode");
    setToolTip(modeList, "windowMode");
    setLabel(monitorLabel, "monitor");
    setToolTip(monitorField, "monitor");
    setButtonText(identifyButton, "identify");
    setLabel(displayLabel, "display");
    displayList.setLocale(localeData.getLocale());
    setToolTip(displayList, "display");
    setLabel(layoutFileLabel, "layoutFile");
    setFileLabel(config.getLayoutFile(), layoutFileButton);
    setToolTip(layoutFileButton, "layoutFile");
    setLabel(renderOptionsLabel, "renderOptions");
    renderOptionCheckboxes.entrySet().forEach(entry -> {
      JCheckBox box = entry.getValue();
      String key = "configDialog." + entry.getKey().name().toLowerCase();
      box.setText(localeData.string(key));
      box.setToolTipText(localeData.string(key + "_title"));
    });
    setButtonText(startButton, "start");
    setToolTip(startButton, "start");
    pack();
  }

  /**
   * Sets the text of the given label.
   */
  private void setLabel(JLabel label, String key) {
    label.setText(localeData.string("configDialog." + key));
  }

  /**
   * Sets the text of the given button.
   */
  private void setButtonText(JButton button, String key) {
    button.setText(localeData.string("configDialog." + key));
  }

  /**
   * Sets the tooltip for the given component.
   */
  private void setToolTip(JComponent component, String key) {
    component.setToolTipText(localeData.string("configDialog."  + key + "_title"));
  }

  /**
   * Builds the UI for the configuration dialog.
   */
  private void build() {
    setLayout(new BorderLayout());
    tabs = new JTabbedPane();
    add(tabs, BorderLayout.CENTER);

    // Connect tab
    JPanel tabPanel = new JPanel();
    tabs.addTab("", tabPanel);
    tabPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;

    // Host
    hostLabel = new JLabel();
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(MARGIN, 0, 0, MARGIN);
    tabPanel.add(hostLabel, c);
    JPanel panel = new JPanel(new BorderLayout());
    hostField = new JTextField();
    hostField.getDocument().addDocumentListener(new FieldChangeListener() {
      @Override
      public void update(DocumentEvent ev) {
        config.setHost(hostField.getText());
        updateEnabledState();
      }
    });
    panel.add(hostField, BorderLayout.CENTER);
    scanButton = new JButton();
    scanButton.setEnabled(false);
    scanButton.addActionListener(ev -> {
      scan();
    });
    panel.add(scanButton, BorderLayout.LINE_END);
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(MARGIN, 0, 0, MARGIN);
    tabPanel.add(panel, c);

    // Server list
    serverListModel = new DefaultListModel<>();
    serverList = new JList<Server>(serverListModel);
    serverList.addListSelectionListener(ev -> {
      Server server = serverList.getSelectedValue();

      if (server != null) {
        hostField.setText(server.getIp());
      }
    });
    JScrollPane scrollPane = new JScrollPane(serverList);
    scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 100));
    c.gridy++;
    c.insets = new Insets(0, 0, MARGIN, MARGIN);
    tabPanel.add(scrollPane, c);

    // Ship number
    shipNumberLabel = new JLabel();
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, MARGIN, MARGIN, MARGIN);
    tabPanel.add(shipNumberLabel, c);
    SpinnerNumberModel shipNumberModel = new SpinnerNumberModel(1, 1, 8, 1);
    shipNumberField = new JSpinner(shipNumberModel);
    shipNumberField.addChangeListener(ev -> {
      config.setShipIndex((byte) (((Number) shipNumberField.getValue()).byteValue() - 1));
    });
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(0, 0, MARGIN, MARGIN);
    tabPanel.add(shipNumberField, c);

    // Artemis install location
    artemisInstallLabel = new JLabel();
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(0, MARGIN, MARGIN, MARGIN);
    tabPanel.add(artemisInstallLabel, c);
    artemisInstallButton = new JButton("-");
    artemisInstallFileChooser = new JFileChooser();
    artemisInstallFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    artemisInstallButton.addActionListener(ev -> {
      artemisInstallFileChooser.setSelectedFile(config.getArtemisInstallPath());
      int result = artemisInstallFileChooser.showDialog(ConfigDialog.this, "Select");

      if (result == JFileChooser.APPROVE_OPTION) {
        try {
          File dir = artemisInstallFileChooser.getSelectedFile();
          config.setArtemisInstallPath(dir);
          setFileLabel(dir, artemisInstallButton);
        } catch (IllegalArgumentException ex) {
          JOptionPane.showMessageDialog(ConfigDialog.this, ex.getMessage());
        }
      }
    });
    artemisInstallButton.setPreferredSize(new Dimension(300, artemisInstallButton.getPreferredSize().height));
    artemisInstallButton.setHorizontalAlignment(SwingConstants.LEADING);
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, MARGIN, MARGIN);
    tabPanel.add(artemisInstallButton, c);

    // Window mode
    modeLabel = new JLabel("");
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, MARGIN, MARGIN, MARGIN);
    tabPanel.add(modeLabel, c);
    modeList = new WindowModeComboBox();
    modeList.addActionListener(ev -> {
      config.setWindowMode((WindowMode) modeList.getSelectedValue());
    });
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(0, 0, MARGIN, MARGIN);
    tabPanel.add(modeList, c);

    // Monitor
    monitorLabel = new JLabel();
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, MARGIN, MARGIN, MARGIN);
    tabPanel.add(monitorLabel, c);
    panel = new JPanel();
    int monitorCount = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
    SpinnerNumberModel monitorModel = new SpinnerNumberModel(1, 1, monitorCount, 1);
    monitorField = new JSpinner(monitorModel);
    monitorField.addChangeListener(ev -> {
      config.setMonitor(((Number) monitorField.getValue()).intValue());
    });
    panel.add(monitorField);
    identifyButton = new JButton("-");
    identifyButton.addActionListener(ev -> {
      IdentifyFrame.identify();
    });
    panel.add(identifyButton);
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 0, MARGIN, MARGIN);
    tabPanel.add(panel, c);

    // Display
    displayLabel = new JLabel();
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(0, MARGIN, MARGIN, MARGIN);
    tabPanel.add(displayLabel, c);
    displayList = new DisplayComboBox();
    displayList.addActionListener(ev -> {
      config.setDisplayType(displayList.getSelectedValue());
      setFileLabel(config.getLayoutFile(), layoutFileButton);
      updateEnabledState();
    });
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 0, MARGIN, MARGIN);
    tabPanel.add(displayList, c);

    // Layout file location
    layoutFileLabel = new JLabel();
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, MARGIN, MARGIN, MARGIN);
    tabPanel.add(layoutFileLabel, c);
    layoutFileButton = new JButton("-");
    layoutFileChooser = new JFileChooser();
    layoutFileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
      }

      @Override
      public String getDescription() {
        return ".json files";
      }
    });
    layoutFileChooser.setAcceptAllFileFilterUsed(false);
    layoutFileButton.addActionListener(ev -> {
      layoutFileChooser.setSelectedFile(config.getLayoutFile());
      int result = layoutFileChooser.showDialog(ConfigDialog.this, "Select");

      if (result == JFileChooser.APPROVE_OPTION) {
        try {
          File file = layoutFileChooser.getSelectedFile();
          config.setLayoutFile(file);
          setFileLabel(file, layoutFileButton);
          updateEnabledState();
        } catch (IllegalArgumentException ex) {
          JOptionPane.showMessageDialog(ConfigDialog.this, ex.getMessage());
        }
      }
    });
    layoutFileButton.setHorizontalAlignment(SwingConstants.LEADING);
    layoutFileButton.setPreferredSize(new Dimension(300, layoutFileButton.getPreferredSize().height));
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, MARGIN, MARGIN);
    tabPanel.add(layoutFileButton, c);

    // Advanced tab
    tabPanel = new JPanel();
    tabs.addTab("", tabPanel);
    tabPanel.setLayout(new GridBagLayout());
    c.gridx = 0;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    renderOptionCheckboxes = new HashMap<>();

    // Rendering options
    renderOptionsLabel = new JLabel();
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(MARGIN, MARGIN, 0, 0);
    tabPanel.add(renderOptionsLabel, c);
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(MARGIN, MARGIN, 0, MARGIN);

    for (Configuration.RenderOption option : Configuration.RenderOption.values()) {
      buildRenderOption(option, c, tabPanel);
      c.gridy++;
    }

    // Bottom panel
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EmptyBorder(MARGIN * 2, MARGIN, MARGIN, MARGIN));

    // Start button
    startButton = new JButton("Start");
    startButton.setToolTipText("Launches the display");
    startButton.addActionListener(ev -> {
      dispose();
      launchFn.accept(config);
    });
    panel.add(startButton, BorderLayout.LINE_END);
    add(panel, BorderLayout.PAGE_END);

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    // Locale selector
    JPanel localePanel = new JPanel();
    localePanel.setLayout(new BorderLayout());
    localePanel.add(new JLabel("🌎 "), BorderLayout.LINE_START);
    List<Locale> locales = new ArrayList<>(LocaleData.getSupportedLocales().keySet());
    int localeCount = locales.size();
    LocaleSelection[] localeArray = new LocaleSelection[localeCount];

    for (int i = 0; i < localeCount; i++) {
      localeArray[i] = new LocaleSelection(locales.get(i));
    }

    localeList = new JComboBox<>(localeArray);
    localeList.addActionListener(ev -> {
      config.setLocale(((LocaleSelection) localeList.getSelectedItem()).locale);
      applyLocale();
    });
    localeList.setSelectedIndex(locales.indexOf(localeData.getLocale()));
    localePanel.add(localeList, BorderLayout.CENTER);
    panel.add(localePanel, BorderLayout.LINE_START);

    applyLocale();
    setResizable(false);
    URL url = CanvasFrame.class.getResource("/logo.png");
    setIconImage(new ImageIcon(url).getImage());
  }

  /**
   * Builds a single render option checkbox.
   */
  private void buildRenderOption(final Configuration.RenderOption renderOption,
      GridBagConstraints c, JComponent tabPanel) {
    String key = "configDialog." + renderOption.name().toLowerCase();
    JCheckBox checkbox = new JCheckBox(localeData.string(key));
    checkbox.setToolTipText(localeData.string(key + "_title"));
    checkbox.addActionListener(ev -> {
      config.setRenderOption(renderOption, ((JCheckBox) ev.getSource()).isSelected());
    });
    renderOptionCheckboxes.put(renderOption, checkbox);
    tabPanel.add(checkbox, c);
  }

  /**
   * Updates the fields to match the values currently stored in the Configuration.
   */
  private void setFields() {
    hostField.setText(config.getHost());
    shipNumberField.setValue(config.getShipIndex() + 1);
    setFileLabel(config.getArtemisInstallPath(), artemisInstallButton);
    modeList.setSelectedIndex(config.getWindowMode().ordinal());
    monitorField.setValue(config.getMonitor());
    displayList.setSelectedValue(config.getDisplayType());
    setFileLabel(config.getLayoutFile(), layoutFileButton);

    for (Configuration.RenderOption renderOption : Configuration.RenderOption.values()) {
      renderOptionCheckboxes.get(renderOption).setSelected(config.getRenderOption(renderOption));
    }

    updateEnabledState();
  }

  /**
   * Invoked when the dialog first opens or when the user clicks the "Scan" button. Scans the local
   * network for running Artemis servers.
   */
  private void scan() {
    scanButton.setEnabled(false);
    serverListModel.clear();
    try {
      Runnable requester = new ServerDiscoveryRequester(new ServerDiscoveryRequester.Listener() {
        @Override
        public void onDiscovered(Server server) {
          serverListModel.add(0, server);
        }

        @Override
        public void onStop() {
          scanButton.setEnabled(true);
        }
      }, 2000);
      new Thread(requester).start();
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(ConfigDialog.this, ex.getMessage());
    }
  }

  /**
   * Updates the given JButton's text label to show the given path.
   */
  private static void setFileLabel(File path, JButton button) {
    String label = path != null ? path.getAbsolutePath() : LocaleData.get().string("configDialog.notSet");
    button.setText("\ud83d\udcc1 " + label);
  }

  /**
   * Updates the enabled state of controls.
   */
  private void updateEnabledState() {
    layoutFileButton.setEnabled(displayList.getSelectedValue() == null);
    startButton.setEnabled(config.isReady());
  }

  /**
   * DocumentListener adapter for the host field.
   */
  private static abstract class FieldChangeListener implements DocumentListener {
    public abstract void update(DocumentEvent e);

    @Override
    public void insertUpdate(DocumentEvent e) {
      update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      update(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      update(e);
    }
  }

  /**
   * The combo box that gives the window mode selections.
   */
  private static class WindowModeComboBox extends LocalizedComboBox<WindowMode> {
    private static final long serialVersionUID = -8325077457310643813L;

    private WindowModeComboBox() {
      super("windowMode", WindowMode.values());
    }

    @Override
    protected String getValueKey(WindowMode value) {
      return value.name().toLowerCase();
    }
  }

  /**
   * The combo box that gives the display selections.
   */
  private static class DisplayComboBox extends LocalizedComboBox<Display.Type> {
    private static final long serialVersionUID = -8348945586963937273L;

    private DisplayComboBox() {
      super("display", buildValues());
    }

    @Override
    protected String getValueKey(Display.Type type) {
      return type != null ? type.name().toLowerCase() : "custom";
    }

    private static Display.Type[] buildValues() {
      Display.Type[] types = Display.Type.values();
      Display.Type[] options = new Display.Type[types.length + 1];
      System.arraycopy(types, 0, options, 0, types.length);
      return options;
    }
  }

  /**
   * Simple wrapper around Locale so that we can customize the label.
   */
  private static class LocaleSelection {
    private Locale locale;

    private LocaleSelection(Locale locale) {
      this.locale = locale;
    }

    @Override
    public String toString() {
      String language = locale.getDisplayLanguage(locale);
      String country = locale.getDisplayCountry(locale);
      return language + (country == "" ? "" : " (" + country + ")") + " [" + locale.toLanguageTag().toLowerCase() + "]";
    }
  }
}
