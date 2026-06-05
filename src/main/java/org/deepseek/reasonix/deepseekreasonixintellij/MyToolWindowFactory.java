package org.deepseek.reasonix.deepseekreasonixintellij;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class MyToolWindowFactory implements ToolWindowFactory {
    public static  Project currentProject = null;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        currentProject = project;
        ReasonixService.getInstance();
        MainToolWindow mainToolWindow = new MainToolWindow(project);
        Content content = ContentFactory.getInstance().createContent(mainToolWindow.getContent(), "Reasonix", false);

        toolWindow.getContentManager().addContent(content);
        Disposer.register(currentProject, mainToolWindow);

    }
}
