package org.respouted;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tools.versions.json.Version;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static LauncherMCP mcp = new LauncherMCP();
    public static LauncherWindow window = null;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            window = new LauncherWindow(mcp);
        });
    }
}