package org.deepseek.reasonix.deepseekreasonixintellij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public final class ReasonixService {
    private static final Logger LOG = Logger.getInstance(ReasonixService.class);
    public static final String[] COMMAND_ARGS = {"serve", "--addr", "127.0.0.1:8788"};
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    
    private Process process;
    private ExecutorService executorService;
    private boolean isRunning = false;

    public ReasonixService() {
        startReasonixService();
        
        // 注册到 Disposer，在应用关闭时自动停止服务
        Disposer.register(ApplicationManager.getApplication(), this::stopReasonixService);

    }

    private void startReasonixService() {
        String reasonixPath = ReasonixCli.detect();
        
        if (reasonixPath == null) {
            LOG.error("Failed to start Reasonix service: reasonix not found");
            return;
        }

        try {

            String basePath = MyToolWindowFactory.currentProject.getBasePath();
            LOG.info("Starting Reasonix in :"+basePath);
            // 使用 shell/cmd 启动以保留环境变量配置
            String command = reasonixPath + " " + String.join(" ", COMMAND_ARGS);
            
            ProcessBuilder processBuilder;
            if (IS_WINDOWS) {
                // Windows 使用 cmd.exe
                processBuilder = new ProcessBuilder("cmd.exe", "/c", command);

                LOG.info("Using cmd.exe for Windows");
            } else {
                // Unix/Linux/macOS 使用 shell
                String shell = getShell();
                LOG.info("Using shell: " + shell);
                processBuilder = new ProcessBuilder(shell, "-l", "-c", command);
            }
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File(basePath));
            
            LOG.info("Starting Reasonix service: " + command);
            process = processBuilder.start();
            isRunning = true;
            
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                try (InputStream inputStream = process.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOG.info("[Reasonix] " + line);
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        LOG.warn("Error reading Reasonix output", e);
                    }
                }
            });
            
            LOG.info("Reasonix service started successfully");
            
        } catch (IOException e) {
            LOG.error("Failed to start Reasonix service", e);
        }
    }

    public void stopReasonixService() {
        if (process != null) {
            LOG.info("Stopping Reasonix service");
            
            isRunning = false;
            
            if (executorService != null) {
                executorService.shutdownNow();
            }
            
            try {
                process.destroy();
                process.waitFor();
                LOG.info("Reasonix service stopped successfully");
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while stopping Reasonix service", e);
                Thread.currentThread().interrupt();
            } finally {
                process = null;
                executorService = null;
            }
        }
    }

    public boolean isServiceRunning() {
        return isRunning && process != null && process.isAlive();
    }

    public static ReasonixService getInstance() {
        return ApplicationManager.getApplication().getService(ReasonixService.class);
    }

    /** 获取用户的默认 shell */
    private String getShell() {
        // 优先从环境变量获取
        String shell = System.getenv("SHELL");
        if (shell != null && !shell.isEmpty() && new java.io.File(shell).exists()) {
            return shell;
        }
        
        // 检查常见的 shell 路径
        String[] commonShells = {
            "/bin/zsh",
            "/bin/bash",
            "/usr/bin/zsh",
            "/usr/bin/bash",
            "/bin/sh"
        };
        
        for (String s : commonShells) {
            if (new java.io.File(s).exists()) {
                return s;
            }
        }
        
        // 默认返回 /bin/sh
        return "/bin/sh";
    }
}