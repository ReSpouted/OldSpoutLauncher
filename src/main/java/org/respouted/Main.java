package org.respouted;

import org.respouted.auth.Authorizer;
import org.respouted.util.Util;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Main {
    public static LauncherMCP mcp;
    public static LauncherWindow window = null;
    public static void main(String[] args) {
        System.out.printf("OldSpoutLauncher v%s\n", Util.VERSION);
        mcp = new LauncherMCP();
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