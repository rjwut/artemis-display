package com.walkertribe.artemisdisplay.modelviewer;

import com.walkertribe.ian.Context;
import com.walkertribe.ian.DefaultContext;
import com.walkertribe.ian.FilePathResolver;
import com.walkertribe.ian.enums.ShipSystem;
import com.walkertribe.ian.util.Grid;
import com.walkertribe.ian.util.GridCoord;
import com.walkertribe.ian.util.GridNode;
import com.walkertribe.ian.vesseldata.Vessel;

public class GridMap {
  public static void main(String[] args) {
    Context ctx = new DefaultContext(new FilePathResolver(args[0]));
    ctx.getVesselData().getVessels().forEach(vessel -> {
      if (vessel.getGrid() != null) {
        System.out.println(new GridMap(vessel).toString());
        System.out.println();
      }
    });
  }

  private Vessel vessel;

  public GridMap(Vessel vessel) {
    this.vessel = vessel;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(vessel.getFaction().getName()).append(' ').append(vessel.getName()).append('\n');

    for (int y = 0; y < GridCoord.MAX_Y; y++) {
      b.append("  ");

      for (int x = 0; x < GridCoord.MAX_X; x++) {
        b.append(x).append(' ');
      }
    }

    b.append(" x\n");
    Grid grid = vessel.getGrid();

    for (int z = GridCoord.MAX_Z - 1; z >= 0; z--) {
      b.append(z).append(' ');

      for (int y = 0; y < GridCoord.MAX_Y; y++) {
        for (int x = 0; x < GridCoord.MAX_X; x++) {
          GridNode node = grid.getNode(GridCoord.get(x, y, z));
          char c;

          if (node.isAccessible()) {
            ShipSystem sys = node.getSystem();
            c = sys == null ? '·' : sys.name().charAt(0);
          } else {
            c = ' ';
          }

          b.append(c).append(' ');
        }

        b.append("  ");
      }

      b.append('\n');
    }

    b.append("z ");

    for (int y = 0; y < GridCoord.MAX_Y; y++) {
      b.append("   y=").append(y).append("      ");
    }
    return b.toString();
  }
}
