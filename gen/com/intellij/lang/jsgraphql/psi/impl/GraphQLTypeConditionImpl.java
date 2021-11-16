// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.*;
import com.intellij.lang.jsgraphql.psi.*;

public class GraphQLTypeConditionImpl extends GraphQLElementImpl implements GraphQLTypeCondition {

  public GraphQLTypeConditionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GraphQLVisitorBase visitor) {
    visitor.visitTypeCondition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GraphQLVisitorBase) accept((GraphQLVisitorBase)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GraphQLTypeName getTypeName() {
    return findChildByClass(GraphQLTypeName.class);
  }

}
