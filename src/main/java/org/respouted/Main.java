package org.respouted;

import org.respouted.auth.Authorizer;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Main {
    public static LauncherMCP mcp = new LauncherMCP();
    public static LauncherWindow window = null;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            window = new LauncherWindow(mcp);
            new SwingWorker<Void, Void>() {
                @Override
                public Void doInBackground() throws Exception {
                    Authorizer.initialize();
                    return null;
                }

                @Override
                public void done() {
                    window.refreshElementStates();
                }
            }.execute();
        });
    }
}