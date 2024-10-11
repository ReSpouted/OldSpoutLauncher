package org.respouted;

import javax.swing.SwingUtilities;

public class Main {
    public static LauncherMCP mcp = new LauncherMCP();
    public static LauncherWindow window = null;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            window = new LauncherWindow(mcp);
        });
    }
}