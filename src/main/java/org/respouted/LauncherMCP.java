package org.respouted;

import org.json.JSONException;
import org.json.JSONObject;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tools.versions.json.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.mcphackers.mcp.MCP;

public class LauncherMCP extends MCP {
    public Version currentVersion = null;
    public boolean active = false;

    public LauncherMCP() {
        // launcher is a gui but retromcp shouldn't directly interact with the user
        this.isGUI = false;
        Path versionPath = MCPPaths.get(this, MCPPaths.VERSION);
        if (Files.exists(versionPath)) {
            try {
                currentVersion = Version.from(new JSONObject(new String(Files.readAllBytes(versionPath))));
            } catch (JSONException | IOException ignored) {
            }
        }
    }

    @Override
    public Path getWorkingDir() {
        return Paths.get("mcp");
    }

    @Override
    public void setProgressBars(List<Task> tasks, TaskMode mode) {
    }

    @Override
    public void clearProgressBars() {
    }

    @Override
    public void log(String msg) {
        System.out.println(msg);
    }

    @Override
    public Version getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void setCurrentVersion(Version version) {
        this.currentVersion = version;
    }

    @Override
    public void setProgress(int barIndex, String progressMessage) {
        log("bar " + barIndex + "updated to " + progressMessage);
    }

    @Override
    public void setProgress(int barIndex, int progress) {
        log("bar " + barIndex + "updated to " + progress + "%");
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean yesNoInput(String title, String msg) {
        log("hello btw u got asced for boolean input: " + msg);
        return false;
    }

    @Override
    public String inputString(String title, String msg) {
        log("hi btw u got asked string input: " + msg);
        return "";
    }

    @Override
    public void showMessage(String title, String msg, int type) {
        log("hi btw i was told to let u know that " + msg);
    }

    @Override
    public void showMessage(String title, String msg, Throwable e) {
        log("hi btw i was told to let u know that " + msg);
    }

    @Override
    public boolean updateDialogue(String changelog, String version) {
        return false;
    }

    @Override
    public Task.Side getSide() {
        return Task.Side.CLIENT;
    }
}
