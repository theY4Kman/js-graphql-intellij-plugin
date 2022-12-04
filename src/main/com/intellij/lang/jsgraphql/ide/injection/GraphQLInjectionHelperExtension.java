package com.intellij.lang.jsgraphql.ide.injection;

import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.Nullable;

public interface GraphQLInjectionHelperExtension {
    ExtensionPointName<GraphQLInjectionHelperExtension> EP_NAME = ExtensionPointName.create("com.intellij.lang.jsgraphql.injectionHelperExtension");

    /**
     * Gets whether the specified host is a target for GraphQL Injection
     */
    boolean isGraphQLLanguageInjectionTarget(PsiElement host);

    /**
     * Extract GraphQL source from a GraphQL Injection target and create a corresponding GraphQL PsiFile.
     * This will only be called if {@link #isGraphQLLanguageInjectionTarget} returns true for the host.
     *
     * @param host the target for GraphQL Injection
     * @return a GraphQL PsiFile generated from the extracted GraphQL source
     */
    default @Nullable GraphQLFile createGraphQLFileFromInjectionTarget(PsiElement host) {
        final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(host.getProject());
        final String graphQLSource = extractGraphQLSourceFromInjectionTarget(host);
        if (graphQLSource == null) {
            return null;
        }

        final PsiFile graphqlInjectedPsiFile = psiFileFactory.createFileFromText("", GraphQLFileType.INSTANCE, graphQLSource, 0, false, false);
        return (GraphQLFile) graphqlInjectedPsiFile;
    }

    /**
     * Extract GraphQL source from a GraphQL Injection target.
     * This will only be called if {@link #isGraphQLLanguageInjectionTarget} returns true for the host.
     *
     * @param host the target for GraphQL Injection
     * @return the GraphQL source extracted from host
     */
    @Nullable String extractGraphQLSourceFromInjectionTarget(PsiElement host);
}
