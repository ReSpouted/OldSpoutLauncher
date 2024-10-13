package org.respouted;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tasks.mode.TaskParameter;
import org.respouted.auth.Authorizer;
import org.respouted.auth.MicrosoftOauthToken;
import org.respouted.auth.MinecraftToken;
import org.respouted.auth.Profile;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LauncherWindow extends JFrame {
    public static final String LOGGING_IN_DIALOG_TEMPLATE = "<html>Logging in.<br>A page should have opened in your browser.<br>If not, open this URL:</html>";
    private Executor mcpExecutor = Executors.newSingleThreadExecutor();
    public Thread loginThread = null;
    public LauncherMCP mcp;
    public JButton launchButton;
    public JButton loginButton;
    public JButton logoutButton;
    public JLabel loggedInLabel;
    public LoggingInDialog loggingInDialog = new LoggingInDialog();

    //TODO decent layout
    public LauncherWindow(LauncherMCP mcp) {
        super("OldSpout Launcher");
        this.mcp = mcp;
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1000, 1000);

        launchButton = new JButton("Launch");
        launchButton.setEnabled(false);
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
            MinecraftToken token = Storage.INSTANCE.getMinecraftToken();
            Profile profile = Storage.INSTANCE.getProfile();
            /*
                    "--username " + profile.username
                    + " --uuid " + profile.uuid
                    + " --userType msa"
                    + " --accessToken " + token.accessToken
             */
            // --username ${auth_player_name} --session ${auth_session} (--version ${version_name}) --gameDir ${game_directory} --assetsDir ${game_assets}
            String separator = FileSystems.getDefault().getSeparator();
            String classPath = MCPPaths.get(mcp, MCPPaths.BIN, Task.Side.CLIENT).toAbsolutePath() + ":" + MCPPaths.get(mcp, MCPPaths.REMAPPED, Task.Side.CLIENT).toAbsolutePath() + ":" + mcp.getLibraries().stream().map(path -> path.toAbsolutePath().toString()).collect(Collectors.joining(":"));
            String jvm = System.getProperty("java.home") + separator + "bin" + separator + "java";
            Path gameDir = MCPPaths.get(mcp, MCPPaths.GAMEDIR, Task.Side.CLIENT).toAbsolutePath();
            String[] command = new String[]{jvm,
                    "-Djava.library.path=" + MCPPaths.get(mcp, MCPPaths.NATIVES).toAbsolutePath(),
                    "-cp", classPath,
                    "Start",
                    "--username", profile.username,
                    "--session", "token:" + token.accessToken + ":" + profile.uuid,
                    "--gameDir", gameDir.toString(),
                    "--assetsDir", gameDir.resolve("assets").toString()};
            ProcessBuilder builder = new ProcessBuilder(command);
            System.out.println("Running process: " + String.join(" ", command));
            Process process = null;
            try {
                process = builder.start();
                process.waitFor();
            } catch(InterruptedException | IOException ex) {
                throw new RuntimeException(ex);
            }
            launchButton.setEnabled(true);
        }));
        this.add(launchButton);

        loginButton = new JButton("Log in");
        loginButton.setEnabled(false);
        loginButton.setSize(new Dimension(100, 30));
        loginButton.addActionListener(e -> {
            loginButton.setEnabled(false);
            loggingInDialog = new LoggingInDialog();
            loggingInDialog.show();
            loginThread = new Thread(() -> {
                //TODO NOT OUR CLIENT ID!!! IMPORTANT CHANGE THIS SO IT'S OUR CLIENT ID!!!!
                String clientId = "d18bb4d8-a27f-4451-a87f-fe6de4436813";
                MicrosoftOauthToken token = Authorizer.getMicrosoftOauthToken(Authorizer.doMicrosoftInteractiveAuthorization(clientId));
                loggingInDialog.close();
                Storage.INSTANCE.setMicrosoftOauthToken(token);
                Authorizer.initialize();
                refreshElementStatesLater();
            });
            loginThread.start();
        });
        this.add(loginButton);

        logoutButton = new JButton("Log out");
        logoutButton.setEnabled(false);
        logoutButton.setSize(new Dimension(100, 30));
        logoutButton.addActionListener(e -> {
            Storage.INSTANCE.setMicrosoftOauthToken(null);
            Storage.INSTANCE.setMinecraftToken(null);
            Storage.INSTANCE.setProfile(null);
            refreshElementStatesLater();
        });
        this.add(logoutButton);

        loggedInLabel = new JLabel("...");
        this.add(loggedInLabel);

        this.setVisible(true);
    }

    public void refreshElementStatesLater() {
        SwingUtilities.invokeLater(() -> {
            refreshElementStates();
        });
    }

    public void refreshElementStates() {
        if(Storage.INSTANCE.getMicrosoftOauthToken() != null) {
            if(Authorizer.ownsMinecraft()) {
                launchButton.setEnabled(true);
                loggedInLabel.setText("Logged in as " + Storage.INSTANCE.getProfile().username);
            } else {
                launchButton.setEnabled(false);
                loggedInLabel.setText("Logged in with an account that doesn't own Minecraft!");
            }
            logoutButton.setEnabled(true);
            loginButton.setEnabled(false);
        } else {
            launchButton.setEnabled(false);
            loggedInLabel.setText("Not logged in!");
            loginButton.setEnabled(true);
            logoutButton.setEnabled(false);
        }
    }

    public static class LoggingInDialog {
        public Thread thread = null;
        public JLabel label = new JLabel(LOGGING_IN_DIALOG_TEMPLATE);
        public JTextField linkField = new JTextField("...", 20);
        private final String[] options = {"Cancel"};
        private boolean closedForcefully = false;

        public void show() {
            JPanel panel = new JPanel();
            panel.add(label);
            panel.add(linkField);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            linkField.setEditable(false);
            thread = new Thread(() -> {
                JOptionPane.showOptionDialog(Main.window, panel, "Logging in", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if(!closedForcefully && Main.window.loginThread != null) {
                    Main.window.loginThread.interrupt();
                    // re-enable login button
                    Main.window.refreshElementStatesLater();
                }
            });
            thread.start();
        }

        public void close() {
            closedForcefully = true;
            if(thread != null) {
                thread.interrupt();
            }
        }

        public void updateLink(String newLink) {
            linkField.setText(newLink);
        }
    }
}
