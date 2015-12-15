package com.intellij.lang.jsgraphql.ide.project;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.content.impl.ContentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JSGraphQLLanguageCompilerToolWindowManager implements Disposable {
    public static final Logger LOGGER = Logger.getInstance("#com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageCompilerToolWindowManager");

    @NotNull
    private final Project myProject;

    private volatile boolean myFirstInitialized;
    @Nullable
    private ConsoleView myConsoleView;
    @Nullable
    private ContentImpl myConsoleContent;
    @Nullable
    private volatile ToolWindow myToolWindow;
    private String myHelpId;
    @Nullable
    private final Icon myIcon;
    @Nullable
    private AnAction[] myActions;
    private final String myToolWindowName;

    public JSGraphQLLanguageCompilerToolWindowManager(@NotNull Project project, @NotNull String toolWindowName, @NotNull String helpId, @Nullable Icon icon, @Nullable AnAction... actions) {
        myProject = project;
        myToolWindowName = toolWindowName;
        myHelpId = helpId;
        myIcon = icon;
        myActions = actions;
    }

    public synchronized void connectToProcessHandler(ProcessHandler handler) {
        init();

        LOGGER.debug("creating tool window");
        ApplicationManager.getApplication().assertIsDispatchThread();

        assert myToolWindow != null;

        ToolWindow toolWindow = myToolWindow;
        if(toolWindow != null) {
            myConsoleView = new ConsoleViewImpl(myProject, GlobalSearchScope.allScope(myProject), true, false);
            final ActionManager actionManager = ActionManager.getInstance();
            final DefaultActionGroup actionGroup = new DefaultActionGroup(myActions);
            final ActionToolbar toolbar = actionManager.createActionToolbar("JSGraphQL", actionGroup, false);
            myConsoleView.getComponent().add(toolbar.getComponent(), BorderLayout.WEST);
            
            myConsoleView.attachToProcess(handler);
            myConsoleContent = new ContentImpl(myConsoleView.getComponent(), "Console", false);
            
            toolWindow.getContentManager().addContent(myConsoleContent);
        }

    }

    public synchronized boolean isConnectedToProcessHandler() {
        return myConsoleView != null;
    }

    public void show() {
        ToolWindow toolWindow = myToolWindow;
        if(toolWindow != null) {
            toolWindow.show((Runnable)null);
        }

    }

    public synchronized void init() {
        if(!myFirstInitialized || myToolWindow == null) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            ToolWindowManager manager = ToolWindowManager.getInstance(myProject);
            ToolWindow toolWindow = manager.registerToolWindow(myToolWindowName, true, ToolWindowAnchor.BOTTOM, myProject, true);
            toolWindow.setIcon(myIcon);
            myToolWindow = toolWindow;
            myFirstInitialized = true;
            LOGGER.debug("initialized tool window");
        }

    }

    public void disconnectFromProcessHandler() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        ConsoleView console = myConsoleView;
        ContentImpl content = myConsoleContent;
        ToolWindow toolWindow = myToolWindow;
        if(console != null && content != null && toolWindow != null) {
            toolWindow.getContentManager().removeContent(content, true);
            myConsoleView = null;
            myConsoleContent = null;
            Disposer.dispose(console);
        }

    }

    public void dispose() {
        cleanPanel();
    }

    public synchronized void cleanPanel() {
        if(!myProject.isDefault()) {
            if(!myProject.isDisposed()) {
                ToolWindowManager.getInstance(myProject).unregisterToolWindow(myToolWindowName);
            }

            ConsoleView view = myConsoleView;
            if(view != null) {
                Disposer.dispose(view);
            }

            myConsoleView = null;
            myToolWindow = null;
            myConsoleContent = null;
        }
    }
}
