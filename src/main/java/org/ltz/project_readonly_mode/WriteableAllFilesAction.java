package org.ltz.project_readonly_mode;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class WriteableAllFilesAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // 获取项目根目录
        VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        for (VirtualFile root : contentRoots) {
            if (root != null && root.isValid() && root.isDirectory()) {
                setFilesWriteable(root);
            }
        }
        e.getRequiredData(CommonDataKeys.PROJECT).getMessageBus()
                .syncPublisher(Notifications.TOPIC).notify(new Notification(
                        "Notification.info",  // 使用你的通知组ID
                        "All files in project are now writable.",
                        NotificationType.INFORMATION
                ));
    }
    private static void setFilesWriteable(VirtualFile directory) {
        if (directory == null || !directory.isValid()) return;

        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                setFilesWriteable(child); // 递归处理子目录
            } else {
                setFileWriteable(child);
            }
        }
    }

    private static void setFileWriteable(VirtualFile file) {
        if (file == null || !file.isValid() || !file.isInLocalFileSystem()) return;

        File localFile = new File(file.getPath());
        if (!localFile.setWritable(true)) {
            // 设置失败时记录错误（可选）
            System.err.println("Failed to set file to read-only: " + file.getPath());
        }
    }
}
