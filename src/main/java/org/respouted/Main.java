package org.respouted;

import org.respouted.auth.Authorizer;
import org.respouted.util.Util;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class Main {
    public static LauncherMCP mcp;
    public static LauncherWindow window = null;
    public static void main(String[] args) {
        System.out.printf("OldSpoutLauncher v%s\n", Util.VERSION);

        String javaVersion = System.getProperty("java.version");
        if(!javaVersion.startsWith("1.8")) {
            int response = JOptionPane.showOptionDialog(
                    null,
                    String.format("You're running java %s, which is not java 8! The launcher is likely to not work properly.", javaVersion),
                    "Incorrect java version",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"Run anyway", "Exit"},
                    JOptionPane.CANCEL_OPTION
            );
            if(response == 1) {
                System.out.printf("Exiting due to incorrect java version (%s)\n", javaVersion);
                System.exit(1);
            }
        }
        System.out.println("Running java " + javaVersion);

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