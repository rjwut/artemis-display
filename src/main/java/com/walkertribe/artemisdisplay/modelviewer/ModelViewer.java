package com.walkertribe.artemisdisplay.modelviewer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.walkertribe.ian.Context;
import com.walkertribe.ian.DefaultContext;
import com.walkertribe.ian.FilePathResolver;
import com.walkertribe.ian.enums.CreatureType;
import com.walkertribe.ian.model.Model;
import com.walkertribe.ian.vesseldata.Vessel;

/**
 * A JFrame that allows the user to select a Vessel and display its wireframe model.
 * @author rjwut
 */
public class ModelViewer extends JFrame {
  private static final long serialVersionUID = -4894426069320509798L;

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Path to Artemis install required");
      System.exit(1);
    }

    final Context ctx = new DefaultContext(new FilePathResolver(args[0]));

    SwingUtilities.invokeLater(() -> {
      new ModelViewer(ctx);
    });
  }

  private static final Insets LABEL_INSETS = new Insets(3, 6, 3, 0);
  private static final Insets CONTROL_INSETS = new Insets(3, 6, 3, 6);

  private Context ctx;
  private ViewerPanel viewer;
  private JComboBox<ModelEntry> vesselSelect;
  private JSlider rotateX;
  private JSlider rotateY;
  private JSlider rotateZ;

  private ModelViewer(Context ctx) {
    super("Artemis Model Viewer");
    this.ctx = ctx;
    setLayout(new BorderLayout());
    viewer = new ViewerPanel();
    add(viewer, BorderLayout.CENTER);
    JPanel controls = new JPanel();
    controls.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Vessel
    JLabel label = new JLabel("Vessel");
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = LABEL_INSETS;
    controls.add(label, c);
    ModelEntry[] entries = buildModelEntries();
    vesselSelect = new JComboBox<>(entries);
    vesselSelect.addActionListener(ev -> {
      viewer.setEntry(((ModelEntry) vesselSelect.getSelectedItem()));
    });
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = CONTROL_INSETS;
    controls.add(vesselSelect, c);

    // Rotate X
    label = new JLabel("Rotate X");
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = LABEL_INSETS;
    controls.add(label, c);
    rotateX = new JSlider(0, 359);
    rotateX.setValue(0);
    rotateX.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        viewer.setRotateX(rotateX.getValue() * Math.PI / 180);
      }
    });
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = CONTROL_INSETS;
    controls.add(rotateX, c);

    label = new JLabel("Rotate Y");
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = LABEL_INSETS;
    controls.add(label, c);
    rotateY = new JSlider(0, 359);
    rotateY.setValue(0);
    rotateY.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        viewer.setRotateY(rotateY.getValue() * Math.PI / 180);
      }
    });
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = CONTROL_INSETS;
    controls.add(rotateY, c);

    label = new JLabel("Rotate Z");
    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = LABEL_INSETS;
    controls.add(label, c);
    rotateZ = new JSlider(0, 359);
    rotateZ.setValue(0);
    rotateZ.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        viewer.setRotateZ(rotateZ.getValue() * Math.PI / 180);
      }
    });
    c.gridx++;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = CONTROL_INSETS;
    controls.add(rotateZ, c);

    add(controls, BorderLayout.PAGE_END);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack();
    setVisible(true);

    if (entries.length > 0) {
      viewer.setEntry(entries[0]);
    }
  }

  /**
   * Returns an array of ModelEntry objects to use for the combo box.
   */
  private ModelEntry[] buildModelEntries() {
    List<ModelEntry> entries = ctx.getVesselData().getVessels().stream()
        .map(vessel -> new ModelEntry(vessel))
        .collect(Collectors.toList());

    for (CreatureType creatureType : CreatureType.values()) {
      Model model = creatureType.getModel(ctx);

      if (model != null) {
        entries.add(new ModelEntry(creatureType.name(), model));
      }
    }

    return entries.toArray(new ModelEntry[entries.size()]);
  }

  /**
   * A simple wrapper around Vessel for display in the combo box.
   */
  static class ModelEntry {
    private String name;
    Vessel vessel;
    Model model;

    private ModelEntry(Vessel vessel) {
      name = vessel.getFaction().getName() + " " + vessel.getName();
      this.vessel = vessel;
      model = vessel.getModel();
    }

    private ModelEntry(String name, Model model) {
      this.name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
      this.model = model;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
