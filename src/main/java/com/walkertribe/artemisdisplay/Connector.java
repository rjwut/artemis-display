package com.walkertribe.artemisdisplay;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.iface.ThreadedArtemisNetworkInterface;
import com.walkertribe.ian.world.Artemis;

/**
 * A Runnable that continually attempts to connect to the given host until it connects or a network
 * error occurs.
 * @author rjwut
 */
public class Connector implements Runnable {
  private static final int CONNECT_TIMEOUT_MS = 1000;
  private static final int SLEEP_INTERVAL_MS = 1000;

  /**
   * A callback interface that is notified when a connection is successful or an error occurs.
   */
  public static interface Listener {
    /**
     * Connected to the server
     */
    void attach(ArtemisNetworkInterface iface);

    /**
     * A error has occurred (other than there being no server at the given address)
     */
    void onException(Exception ex);
  }

  private String host;
  private int port;
  private Listener listener;

  /**
   * Creates a Connector that will attempt to connect to the named host and will invoke the given
   * Listener when a connection is made or a network error occurs.
   */
  public Connector(String host, Listener listener) {
    port = Artemis.DEFAULT_PORT;
    int colonPos = host.indexOf(':');

    if (colonPos != -1) {
        port = Integer.parseInt(host.substring(colonPos + 1));
        this.host = host.substring(0, colonPos);
    } else {
      this.host = host;
    }

    this.listener = listener;
  }

  @Override
  public void run() {
    while (true) {
      try {
        // Try to connect
        listener.attach(new ThreadedArtemisNetworkInterface(host, port, CONNECT_TIMEOUT_MS));
        break;
      } catch (SocketTimeoutException | ConnectException ex) {
        // Nobody's there; sleep and try again
        try {
          Thread.sleep(SLEEP_INTERVAL_MS);
        } catch (InterruptedException iex) {
          // We've been told to stop trying
          break;
        }
      } catch (Exception ex) {
        // Something else went wrong
        listener.onException(ex);
        break;
      }
    }
  }
}
