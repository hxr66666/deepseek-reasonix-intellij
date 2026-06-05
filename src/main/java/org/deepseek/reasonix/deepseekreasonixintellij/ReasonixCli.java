package org.deepseek.reasonix.deepseekreasonixintellij;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReasonixCli {
    private static final Logger LOG = Logger.getInstance(ReasonixCli.class);
    
    private static final boolean IS_WIN = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private static final long TIMEOUT = 15L;

    private static String cachedPath = null;

    /** Locate the reasonix binary path based on OS. */
    public static String detect() {
        if (cachedPath != null) {
            return cachedPath;
        }

        List<String> paths = new ArrayList<>();

        if (IS_WIN) {
            paths.add(expandEnv("%APPDATA%") + "\\npm\\reasonix.cmd");
            paths.add(expandEnv("%LOCALAPPDATA%") + "\\npm\\reasonix.cmd");
            paths.add(home("AppData\\npm\\reasonix.cmd"));
            paths.add(home("AppData\\Roaming\\npm\\reasonix.cmd"));
        } else {
            // macOS Homebrew
            paths.add("/opt/homebrew/bin/reasonix");
            paths.add("/usr/local/bin/reasonix");
            // Linux / general
            paths.add("/usr/bin/reasonix");
            paths.add("/usr/local/bin/reasonix");
            // npm global
            paths.add(home(".local/bin/reasonix"));
        }

        // Check each path
        for (String p : paths) {
            if (p != null && new File(p).exists()) {
                cachedPath = p;
                LOG.info("Reasonix detected at: " + p);
                return p;
            }
        }

        // Fallback: which/where command
        try {
            List<String> whichCmd;
            if (IS_WIN) {
                whichCmd = List.of("cmd", "/c", "where reasonix 2>nul");
            } else {
                whichCmd = List.of("/bin/sh", "-c", "which reasonix 2>/dev/null");
            }
            
            ProcessBuilder pb = new ProcessBuilder(whichCmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String out = new String(p.getInputStream().readAllBytes()).trim();
            p.waitFor(3, TimeUnit.SECONDS);
            
            if (!out.isEmpty()) {
                String foundPath = out.lines().findFirst().orElse(null);
                if (foundPath != null) {
                    cachedPath = foundPath.trim();
                    LOG.info("Reasonix found via which: " + cachedPath);
                    return cachedPath;
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to detect reasonix via which command", e);
        }

        LOG.warn("Reasonix not found in any known location");
        return null;
    }

    /** Get shell PATH for subprocesses. */
    public static String getShellPath() {
        String envPath = System.getenv("PATH");
        
        if (IS_WIN) {
            String winPath = System.getenv("Path");
            return winPath != null ? winPath : envPath;
        }

        // macOS: GUI apps don't inherit shell profile paths
        String osName = System.getProperty("os.name");
        boolean isMac = osName.toLowerCase().startsWith("mac");
        
        String shell = null;
        if (isMac && new File("/bin/zsh").exists()) {
            shell = "/bin/zsh";
        } else {
            String userShell = System.getenv("SHELL");
            if (userShell != null && !userShell.isBlank() && new File(userShell).exists()) {
                shell = userShell;
            }
        }

        if (shell != null) {
            try {
                ProcessBuilder pb = new ProcessBuilder(shell, "-l", "-c", "echo $PATH");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                String out = new String(p.getInputStream().readAllBytes()).trim();
                p.waitFor(3, TimeUnit.SECONDS);
                if (!out.isBlank()) {
                    return out;
                }
            } catch (Exception e) {
                LOG.warn("Failed to get shell PATH", e);
            }
        }

        return envPath != null ? envPath : "/usr/local/bin:/usr/bin:/bin";
    }

    /** Run reasonix with args, return stdout. */
    public static Result<String> run(String... args) {
        String cmd = detect();
        if (cmd == null) {
            return Result.failure(new Exception("Reasonix not found"));
        }

        List<String> cmdList = new ArrayList<>();
        cmdList.add(cmd);
        for (String arg : args) {
            cmdList.add(arg);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(cmdList);
            pb.environment().put("PATH", getShellPath());
            
            Process p = pb.start();
            boolean finished = p.waitFor(TIMEOUT, TimeUnit.SECONDS);
            
            if (finished) {
                String stdout = new String(p.getInputStream().readAllBytes()).trim();
                String stderr = new String(p.getErrorStream().readAllBytes()).trim();
                
                if (p.exitValue() == 0) {
                    return Result.success(stdout);
                } else {
                    String error = stderr.isBlank() ? stdout : stderr;
                    return Result.failure(new Exception(error));
                }
            } else {
                p.destroyForcibly();
                return Result.failure(new Exception("Process timed out after " + TIMEOUT + "s"));
            }
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    private static String home(String sub) {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            userHome = System.getenv("HOME");
        }
        if (userHome == null) {
            userHome = System.getenv("USERPROFILE");
        }
        if (userHome == null) {
            userHome = ".";
        }
        return new File(userHome, sub).getAbsolutePath();
    }

    private static String expandEnv(String s) {
        if (s == null) return "";
        String result = s;
        result = result.replace("%APPDATA%", System.getenv("APPDATA") != null ? System.getenv("APPDATA") : "");
        result = result.replace("%LOCALAPPDATA%", System.getenv("LOCALAPPDATA") != null ? System.getenv("LOCALAPPDATA") : "");
        return result;
    }

    public static class Result<T> {
        private final T value;
        private final Exception error;

        private Result(T value, Exception error) {
            this.value = value;
            this.error = error;
        }

        public static <T> Result<T> success(T value) {
            return new Result<>(value, null);
        }

        public static <T> Result<T> failure(Exception error) {
            return new Result<>(null, error);
        }

        public boolean isSuccess() {
            return error == null;
        }

        public T getValue() {
            return value;
        }

        public Exception getError() {
            return error;
        }
    }
}