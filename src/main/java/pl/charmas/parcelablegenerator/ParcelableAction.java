/*
 * Copyright (C) 2013 Michał Charmas (http://blog.charmas.pl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.charmas.parcelablegenerator;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParcelableAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);

        GenerateDialog dlg = new GenerateDialog(psiClass);
        dlg.show();

        if (dlg.isOK()) {
            //先清除原先的方法 重新生成，避免自己手动删除修改
            clearOldMethods(e);
            //增加生成get set方法
            AnAction generateGetterAndSetter = ActionManagerImpl.getInstanceEx().getAction("GenerateGetterAndSetter");
            generateGetterAndSetter.actionPerformed(e);

            generateParcelable(psiClass, dlg.getSelectedFields());
        }
    }


    /**
     * 删除老的方法 保证重新生成
     *
     * @param e
     */
    private void clearOldMethods(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        WriteCommandAction.runWriteCommandAction(psiClass.getProject(), () -> {
            @NotNull PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                method.delete();
            }
        });
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offSet = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offSet);
        return PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }

    private void generateParcelable(final PsiClass psiClass, final List<PsiField> fields) {
        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                new CodeGenerator(psiClass, fields).generate();
            }
        }.execute();
    }


    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null && !psiClass.isEnum() && !psiClass.isInterface());
    }


}
