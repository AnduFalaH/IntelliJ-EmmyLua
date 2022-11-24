/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.intentionAction;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;

public class QuickRequireAction extends BaseIntentionAction {
    public String name;

    public QuickRequireAction(String name) {
        this.name = name;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Quick require " + name;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Quick require";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return findRequirePath(project, name) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.writeCommandAction(project).run(() -> {
            var requirePath = findRequirePath(project, name);
            if(requirePath == null) {
                return;
            }
            ASTNode node = psiFile.getNode();
            ASTNode nodeInsert = null;
            var elements = node.getChildren(null);
            if(elements.length > 0)
                // "nodeInsert == elements[0]" indicates inserting into the first line,
                //      but is it possible that "elements.length == 0"?
                // Mostly, require statements located at the top of a file, and we do not care orders of them,
                // so here simply choose the first line as the insert location.
                nodeInsert = elements[0];

            String text = String.format("local %1$s = require(\"%2$s\")", name, requirePath);
            var file = LuaElementFactory.INSTANCE.createFile(project, text);
            var newLineNode = LuaElementFactory.INSTANCE.newLine(project).getNode();
            node.addChild(newLineNode, nodeInsert);
            node.addChild(file.getNode().getFirstChildNode(), newLineNode);
        }));
    }

    private String findRequirePath(@NotNull Project project, String name){
        var file = LuaFileUtil.INSTANCE.findFile(project, name);
        if(file == null) {
            return null;
        }
        return LuaFileUtil.INSTANCE.asRequirePath(project, file);
    }
}
