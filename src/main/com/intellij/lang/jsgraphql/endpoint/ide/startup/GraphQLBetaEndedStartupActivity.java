/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.startup;

import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.jetbrains.annotations.NotNull;

public class GraphQLBetaEndedStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {

        ApplicationManager.getApplication().executeOnPooledThread(() -> {

            final HttpClientParams params = new HttpClientParams();
            final HttpClient httpClient = new HttpClient(params);
            final GetMethod method = new GetMethod("https://plugins.jetbrains.com/plugins/list?pluginId=8097");

            try {
                final int responseCode = httpClient.executeMethod(method);
                if (responseCode == 200) {
                    final String responseBody = method.getResponseBodyAsString();
                    if (responseBody != null && responseBody.contains("<version>2.0")) { // HACK but good enough to detect the end the beta :D
                        // 2.0 made available by JetBrains
                        final Notification notification = new Notification("GraphQL", JSGraphQLIcons.Logos.GraphQL, "Thank you for testing the GraphQL plugin", null, "The 2.0 beta is over. Please remove the custom GraphQL repository URL in \"Plugins\" > \"Manage plugin repositories\" to get upcoming updates.", NotificationType.INFORMATION, null);
                        notification.setImportant(true).addAction(new NotificationAction("Edit plugin settings") {
                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Plugins");
                            }
                        });

                        Notifications.Bus.notify(notification);
                    }

                }

            } catch (Exception ignored) {
            }

        });

    }
}
