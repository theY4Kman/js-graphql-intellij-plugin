/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.Annotation;
import com.intellij.lang.jsgraphql.languageservice.api.AnnotationsResponse;
import com.intellij.lang.jsgraphql.languageservice.api.Pos;
import com.intellij.lang.jsgraphql.psi.JSGraphQLFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSGraphQLAnnotator extends ExternalAnnotator<JSGraphQLAnnotationResult, JSGraphQLAnnotationResult> {

    private final static Logger log = Logger.getInstance(JSGraphQLAnnotator.class);

    @Nullable
    @Override
    public JSGraphQLAnnotationResult collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        try {
            if(file instanceof JSGraphQLFile) {
                CharSequence buffer = editor.getDocument().getCharsSequence();
                if (buffer.length() > 0) {
                    final AnnotationsResponse annotations = JSGraphQLNodeLanguageServiceClient.getAnnotations(buffer.toString(), file.getProject(), false);
                    return new JSGraphQLAnnotationResult(annotations, editor);
                }
            }
        } catch (Throwable e) {
            if(e instanceof ProcessCanceledException) {
                // annotation was cancelled, e.g. due to an editor being closed
                return null;
            }
            log.error("Error during doAnnotate", e);
        }
        return null;
    }

    @Nullable
    @Override
    public JSGraphQLAnnotationResult doAnnotate(JSGraphQLAnnotationResult collectedInfo) {
        return collectedInfo;
    }

    @Override
    public void apply(@NotNull PsiFile file, JSGraphQLAnnotationResult annotationResult, @NotNull AnnotationHolder holder) {
        if(annotationResult != null) {
            try {
                final Editor editor = annotationResult.getEditor();
                AnnotationsResponse annotationsReponse = annotationResult.getAnnotationsReponse();
                if(annotationsReponse == null) {
                    return;
                }
                for (Annotation annotation : annotationsReponse.getAnnotations()) {
                    LogicalPosition from = getLogicalPosition(annotation.getFrom());
                    LogicalPosition to = getLogicalPosition(annotation.getTo());
                    int fromOffset = editor.logicalPositionToOffset(from);
                    int toOffset = editor.logicalPositionToOffset(to);
                    HighlightSeverity severity = "error".equals(annotation.getSeverity()) ? HighlightSeverity.ERROR : HighlightSeverity.WARNING;
                    if (fromOffset < toOffset) {
                        final String message = StringUtils.substringBefore(annotation.getMessage(), "\n");
                        holder.createAnnotation(severity, TextRange.create(fromOffset, toOffset), message);
                    }
                }
            } catch (Exception e) {
                log.error("Unable to apply annotations", e);
            } finally {
                annotationResult.releaseEditor();
            }
        }
    }


    // --- implementation ----

    @NotNull
    private LogicalPosition getLogicalPosition(Pos pos) {
        return new LogicalPosition(pos.getLine(), pos.getCh());
    }
}
