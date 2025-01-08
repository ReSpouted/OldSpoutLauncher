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
import org.respouted.util.OS;
import org.respouted.util.Util;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public LauncherWindow(LauncherMCP mcp) {
        super("OldSpout Launcher");
        this.mcp = mcp;
        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        Container contentPane = this.getContentPane();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(500, 350);

        launchButton = new JButton("Launch");
        launchButton.setEnabled(false);
        launchButton.setFont(new Font("Arial", Font.BOLD, 20));
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
            String separator = FileSystems.getDefault().getSeparator();
            String classPath = MCPPaths.get(mcp, MCPPaths.BIN, Task.Side.CLIENT).toAbsolutePath() + OS.getClasspathSeparator() + MCPPaths.get(mcp, MCPPaths.REMAPPED, Task.Side.CLIENT).toAbsolutePath() + OS.getClasspathSeparator() + mcp.getLibraries().stream().map(path -> path.toAbsolutePath().toString()).collect(Collectors.joining(OS.getClasspathSeparator()));
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
            Process process;
            try {
                process = builder.start();
                // Windows hangs if the streams are left open but not read.
                // Unix (or just linux? idk) kills (i think?) the process if the streams get closed.
                if(OS.CURRENT_OS == OS.WINDOWS) {
                    process.getErrorStream().close();
                    process.getOutputStream().close();
                }
                this.setVisible(false);
                process.waitFor();
            } catch(InterruptedException | IOException ex) {
                throw new RuntimeException(ex);
            }
            this.setVisible(true);
            launchButton.setEnabled(true);
        }));
        this.add(launchButton);

        JPanel accountPanel = new JPanel();
        accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.X_AXIS));

        loginButton = new JButton("Log in");
        loginButton.setEnabled(false);
        loginButton.addActionListener(e -> {
            loginButton.setEnabled(false);
            loggingInDialog = new LoggingInDialog();
            loggingInDialog.show();
            loginThread = new Thread(() -> {
                String clientId = "856b1745-0019-4218-93d7-c5291c7675e5";
                MicrosoftOauthToken token = Authorizer.getMicrosoftOauthToken(Authorizer.doMicrosoftInteractiveAuthorization(clientId), false);
                loggingInDialog.close();
                Storage.INSTANCE.setMicrosoftOauthToken(token);
                Authorizer.initialize();
                refreshElementStatesLater();
            });
            loginThread.start();
        });
        accountPanel.add(loginButton);

        logoutButton = new JButton("Log out");
        logoutButton.setEnabled(false);
        logoutButton.addActionListener(e -> {
            Storage.INSTANCE.setMicrosoftOauthToken(null);
            Storage.INSTANCE.setMinecraftToken(null);
            Storage.INSTANCE.setProfile(null);
            refreshElementStatesLater();
        });
        accountPanel.add(logoutButton);

        this.add(accountPanel);

        loggedInLabel = new JLabel("...");
        this.add(loggedInLabel);

        try {
            BufferedImage originalImage = ImageIO.read(LauncherWindow.class.getResourceAsStream("/spoutcraft.png"));
            Image scaledImage = originalImage.getScaledInstance((int) (originalImage.getWidth() * 1.5), (int) (originalImage.getHeight() * 1.5), Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            this.add(logoLabel);
            layout.putConstraint(SpringLayout.NORTH, logoLabel, 25, SpringLayout.NORTH, contentPane);
            layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, logoLabel, 0, SpringLayout.HORIZONTAL_CENTER, contentPane);
        } catch(IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        JLabel versionLabel = new JLabel("v" + Util.VERSION);
        this.add(versionLabel);

        layout.putConstraint(SpringLayout.SOUTH, loggedInLabel, -25, SpringLayout.SOUTH, contentPane);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, loggedInLabel, 0, SpringLayout.HORIZONTAL_CENTER, contentPane);

        layout.putConstraint(SpringLayout.SOUTH, accountPanel, -10, SpringLayout.NORTH, loggedInLabel);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, accountPanel, 0, SpringLayout.HORIZONTAL_CENTER, contentPane);

        layout.putConstraint(SpringLayout.SOUTH, launchButton, -40, SpringLayout.NORTH, accountPanel);
        layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, launchButton, 0, SpringLayout.HORIZONTAL_CENTER, contentPane);

        layout.putConstraint(SpringLayout.WEST, versionLabel, 10, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.SOUTH, versionLabel, -5, SpringLayout.SOUTH, contentPane);

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
