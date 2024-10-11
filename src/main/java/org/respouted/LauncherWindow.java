package org.respouted;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tasks.mode.TaskParameter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LauncherWindow extends JFrame {
    Executor mcpExecutor = Executors.newSingleThreadExecutor();
    public LauncherMCP mcp;
    //TODO decent layout
    public LauncherWindow(LauncherMCP mcp) {
        super("OldSpout Launcher");
        this.mcp = mcp;
        this.setLayout(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1000, 1000);

        JButton launchButton = new JButton("Launch");
        launchButton.setSize(new Dimension(100, 30));
        launchButton.addActionListener(e -> mcpExecutor.execute(() -> {
            launchButton.setEnabled(false);
            mcp.log("Launching the game...");
            if(mcp.getCurrentVersion() == null) {
                mcp.options.setParameter(TaskParameter.SETUP_VERSION, "1.6.4");
                mcp.performTask(TaskMode.SETUP, Task.Side.CLIENT);
            }
            if(!Files.exists(MCPPaths.get(mcp, MCPPaths.PROJECT, Task.Side.CLIENT)) || !Files.exists(MCPPaths.get(mcp, MCPPaths.SOURCE, Task.Side.CLIENT))) {
                mcp.performTask(TaskMode.DECOMPILE, Task.Side.CLIENT);
            }
            if(!Files.exists(mcp.getWorkingDir().resolve("grease"))) {
                try {
                    Git.cloneRepository()
                            .setURI("https://github.com/respouted/grease")
                            .setDirectory(mcp.getWorkingDir().resolve("grease").toFile())
                            .call();
                    MCPPaths.get(mcp, MCPPaths.PATCH, Task.Side.CLIENT).getParent().toFile().mkdirs();
                    Files.move(mcp.getWorkingDir().resolve("grease").resolve("client.patch"), MCPPaths.get(mcp, MCPPaths.PATCH, Task.Side.CLIENT));
                } catch(GitAPIException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if(!Files.exists(MCPPaths.get(mcp, MCPPaths.SOURCE, Task.Side.CLIENT).resolve("org").resolve("spoutcraft"))) {
                mcp.performTask(TaskMode.APPLY_PATCH, Task.Side.CLIENT);
                try {
                    Files.move(mcp.getWorkingDir().resolve("grease").resolve("resources"), MCPPaths.get(mcp, MCPPaths.SOURCE, Task.Side.CLIENT).resolve("org").resolve("spoutcraft").resolve("resources"));
                } catch(IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if(!Files.exists(MCPPaths.get(mcp, MCPPaths.BIN, Task.Side.CLIENT))) {
                mcp.performTask(TaskMode.RECOMPILE, Task.Side.CLIENT);
            }
            mcp.performTask(TaskMode.START, Task.Side.CLIENT);
            launchButton.setEnabled(true);
        }));
        this.add(launchButton);

        this.setVisible(true);
    }
}
