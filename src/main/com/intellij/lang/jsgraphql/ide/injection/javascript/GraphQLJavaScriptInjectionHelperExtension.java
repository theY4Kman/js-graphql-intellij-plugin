package com.intellij.lang.jsgraphql.ide.injection.javascript;

import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionHelperExtension;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

public class GraphQLJavaScriptInjectionHelperExtension implements GraphQLInjectionHelperExtension {
    @Override
    public boolean isGraphQLLanguageInjectionTarget(PsiElement host) {
        return GraphQLLanguageInjectionUtil.isGraphQLLanguageInjectionTarget(host);
    }

    @Override
    public @Nullable String extractGraphQLSourceFromInjectionTarget(PsiElement host) {
        return StringUtils.strip(host.getText(), "` \t\n");
    }
}
