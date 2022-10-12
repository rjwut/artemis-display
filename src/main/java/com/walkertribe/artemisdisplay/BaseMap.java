package com.walkertribe.artemisdisplay;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.walkertribe.ian.enums.ObjectType;
import com.walkertribe.ian.protocol.core.world.DeleteObjectPacket;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.world.ArtemisBase;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * An object that can be used to track the status of bases. The offer() methods keep the Entry
 * objects updated as well as return the corresponding Entry. You can manually create an entry in
 * the BaseMap if you have a base's displayName and it doesn't have an entry in the BaseMap (e.g. you
 * get a message from a base whose ArtemisBase object hasn't arrived yet).
 * @author rjwut
 */
public class BaseMap<T> {
  private static final Comparator<String> NAME_COMPARATOR = Comparator.nullsFirst(String::compareToIgnoreCase);
  public static final Comparator<Entry<?>> ENTRY_COMPARATOR = new Comparator<Entry<?>>() {
    @Override
    public int compare(Entry<?> o1, Entry<?> o2) {
      int c = iff(o1) - iff(o2);

      if (c != 0) {
        return c;
      }

      return NAME_COMPARATOR.compare(o1 == null ? null : o1.displayName, o2 == null ? null : o2.displayName);
    }
  };
  private static final Pattern SHORTNAME_PATTERN = Pattern.compile("^([A-Za-z]{1,2})(\\d{1,2})|^([A-Za-z]).*\\s(\\d+)$|^(\\w{1,4})\\s.*()$|^(\\w{1,4}).*?()$");

  /**
   * An entry for a base in the BaseMap.
   */
  public static class Entry<T> implements Comparable<Entry<T>> {
    private ArtemisBase base;
    private String displayName;
    private BoolState friendly = BoolState.UNKNOWN;
    private boolean alive = true;
    private T data;

    /**
     * Creates a new BaseMap Entry for the given ArtemisBase.
     */
    private Entry(ArtemisBase base, ArtemisPlayer player) {
      this.base = base;
      this.displayName = BaseMap.getDisplayName(base.getName());
      updateIff(base, player);
    }

    /**
     * Creates a new BaseMap Entry for a base with the given name.
     */
    private Entry(String name) {
      displayName = BaseMap.getDisplayName(name);
    }

    /**
     * Returns the ArtemisBase object corresponding to this entry. Note that this may be null in the
     * case of an Entry created by displayName.
     */
    public ArtemisBase getBase() {
      return base;
    }

    /**
     * Returns the displayName to display for this base. Theoretically it's possible for this to be null
     * (if an ArtemisBase hasn't transmitted its displayName yet), but in practice the base's displayName is
     * always transmitted with its first update.
     */
    public String getDisplayName() {
      return displayName;
    }

    /**
     * Returns a BoolState indicating whether or not this base is friendly.
     */
    public BoolState isFriendly() {
      return friendly;
    }

    /**
     * Returns true if this base has not been destroyed.
     */
    public boolean isAlive() {
      return alive;
    }

    /**
     * Returns the data object you have attached to this Entry, or null no data object is attached.
     */
    public T getData() {
      return data;
    }

    /**
     * Attaches the given data object to this Entry.
     */
    public void setData(T data) {
      this.data = data;
    }

    /**
     * Updates this Entry according to the given ArtemisBase object.
     */
    private void update(ArtemisBase update, ArtemisPlayer player) {
      if (base == null) {
        base = update;
      } else {
        base.updateFrom(update);
      }

      CharSequence name = base.getName();

      if (name != null) {
        this.displayName = BaseMap.getDisplayName(name);
      }

      if (friendly == BoolState.UNKNOWN) {
        updateIff(base, player);
      }
    }

    private void updateIff(ArtemisBase update, ArtemisPlayer player) {
      if (player != null) {
        byte playerSide = player.getSide();
        byte baseSide = base.getSide();

        if (playerSide != -1 && baseSide != -1) {
          friendly = BoolState.from(playerSide == baseSide);
        }
      }
    }

    @Override
    public int compareTo(Entry<T> o) {
      return ENTRY_COMPARATOR.compare(this, o);
    }
  }

  private Map<Integer, Entry<T>> idMap = new HashMap<>();
  private Map<String, Entry<T>> nameMap = new HashMap<>();

  /**
   * Retrieves the Entry with the given ID.
   */
  public Entry<T> get(int id) {
    return idMap.get(id);
  }

  /**
   * Returns the Entry for the base with the given displayName. If no such Entry exists, it will be
   * automatically created and returned if autocreate is true; otherwise, this method will return
   * null.
   */
  public Entry<T> get(String name, boolean autocreate) {
    name = getDisplayName(name);
    Entry<T> entry = nameMap.get(name);

    if (entry == null && autocreate) {
      entry = new Entry<T>(name);
      nameMap.put(name, entry);
    }

    return entry;
  }

  /**
   * Updates the entry for the given ArtemisBase and returns it. If no such entry exists, it will
   * be created.
   */
  public Entry<T> offer(ArtemisBase update, ArtemisPlayer player) {
    Integer id = update.getId();
    String name = getDisplayName(update.getName());

    // Look up by ID
    Entry<T> entry = idMap.get(id);

    if (entry == null && name != null) {
      // Maybe we got a message from them first? Try looking up by displayName.
      entry = nameMap.get(name);
    }

    if (entry == null) {
      // Entry doesn't exist; create one
      entry = new Entry<T>(update, player);
      idMap.put(id, entry);

      if (name != null) {
        nameMap.put(name, entry);
      }
    } else {
      entry.update(update, player);

      if (name != null && !nameMap.containsKey(name)) {
        nameMap.put(name, entry);
      }
    }

    return entry;
  }

  /**
   * If this DeleteObjectPacket indicates that a base tracked by this object has been destroyed,
   * the corresponding Entry is updated and returned. Otherwise, this method returns null.
   */
  public Entry<T> offer(DeleteObjectPacket pkt) {
    if (pkt.getTargetType() != ObjectType.BASE) {
      return null;
    }

    Entry<T> entry = idMap.get(pkt.getTarget());

    if (entry != null) {
      entry.alive = false;
    }

    return entry;
  }

  /**
   * Returns a SortedSet containing all Entry objects.
   */
  public SortedSet<Entry<T>> entries() {
    return new TreeSet<>(idMap.values());
  }

  /**
   * Returns a SortedSet containing all Entry objects that match the given Predicate.
   */
  public SortedSet<Entry<T>> entries(Predicate<Entry<T>> predicate) {
    return new TreeSet<>(
        idMap.values().stream().filter(predicate).collect(Collectors.toList())
    );
  }

  /**
   * Clears all Entry objects from the BaseMap.
   */
  public void clear() {
    idMap.clear();
    nameMap.clear();
  }

  /**
   * Returns 0 if this base is friendly, 1 if it's hostile, and 2 if unknown.
   */
  private static int iff(Entry<?> entry) {
    BoolState friendly = entry.isFriendly();
    return friendly == BoolState.TRUE ? 0 : (friendly == BoolState.FALSE ? 1 : 0);
  }

  /**
   * <p>
   * Returns an appropriate display name for the given base. This method attempts to produce a good
   * short name based on known name patterns used in the stock game, then falls back to a more
   * general shortname strategy if that fails. The rules are as follows:
   * </p>
   * <ol>
   * <li>
   * If the name is less than five characters long, the entire name is returned.
   * </li>
   * <li>
   * If the name begins with one or two alphabetic characters followed by one or two digits, those
   * characters are returned.
   * Examples: DS4 Terran Science Base -> DS4
   * </li>
   * <li>
   * If the name begins with an alphabetic character and ends with one or two digits, those
   * characters are returned. Examples: Arvonian Base 14 -> A14
   * </li>
   * <li>
   * If none of the above scenarios apply, the first characters of the string are returned, until we
   * reach a space or four characters, whichever is shortest.
   * </li>
   * </ol>
   */
  private static String getDisplayName(CharSequence name) {
    if (name == null) {
      return null;
    }

    if (name.length() < 5) {
      return name.toString();
    }

    Matcher matcher = SHORTNAME_PATTERN.matcher(name);

    if (matcher.matches()) {
      StringBuilder b = new StringBuilder();
      int count = matcher.groupCount();

      for (int i = 1; i < count; i++) {
        String group = matcher.group(i);

        if (group != null) {
          b.append(group);
        }
      }

      return b.toString();
    }

    throw new RuntimeException("Could not extract shortname: " + name); // should never happen
  }
}
