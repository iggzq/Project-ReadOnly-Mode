package org.ltz.project_readonly_mode;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import org.jetbrains.annotations.NotNull;

public class SetProjectWritableAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Restoring write access") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                setAllFilesWritable(project, indicator);
            }
        });
    }

    private void setAllFilesWritable(Project project, ProgressIndicator indicator) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] contentRoots = rootManager.getContentRoots();

        for (VirtualFile root : contentRoots) {
            traverseAndSetWritable(root, indicator);
        }
    }

    private void traverseAndSetWritable(VirtualFile file, ProgressIndicator indicator) {
        if (indicator.isCanceled()) return;

        if (file.isDirectory()) {
            VirtualFile[] children = file.getChildren();
            for (VirtualFile child : children) {
                traverseAndSetWritable(child, indicator);
            }
        } else {
            try {
                if (file instanceof NewVirtualFile) {
                    file.setWritable(true);
                }
            } catch (Exception ex) {
                // Handle error or log
            }
        }
    }
}