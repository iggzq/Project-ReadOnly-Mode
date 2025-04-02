package org.ltz.project_readonly_mode;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import org.jetbrains.annotations.NotNull;

public class SetProjectReadOnlyAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Setting project to read-only") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                setAllFilesReadOnly(project, indicator);
            }
        });
    }

    private void setAllFilesReadOnly(Project project, ProgressIndicator indicator) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] contentRoots = rootManager.getContentRoots();

        for (VirtualFile root : contentRoots) {
            traverseAndSetReadOnly(root, indicator);
        }
    }

    private void traverseAndSetReadOnly(VirtualFile file, ProgressIndicator indicator) {
        if (indicator.isCanceled()) return;

        if (file.isDirectory()) {
            VirtualFile[] children = file.getChildren();
            for (VirtualFile child : children) {
                traverseAndSetReadOnly(child, indicator);
            }
        } else {
            try {
                if (file instanceof NewVirtualFile) {
                    file.setWritable(false);
                }
            } catch (Exception ex) {
                Messages.showErrorDialog("Failed to set file as read-only: " + file.getPresentableUrl(), "Error");
            }
        }
    }
}