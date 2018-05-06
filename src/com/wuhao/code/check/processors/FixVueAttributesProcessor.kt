/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check.processors

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.xml.XmlTag
import com.wuhao.code.check.LanguageNames
import com.wuhao.code.check.RecursiveVisitor
import com.wuhao.code.check.inspection.fix.VueTemplateTagFix
import org.jetbrains.kotlin.psi.psiUtil.endOffset

/**
 * 格式化代码时自动对vue模板中的标签属性进行排序
 * @author 吴昊
 * @since 1.1
 */
class FixVueAttributesProcessor : PostFormatProcessor {

  override fun processElement(el: PsiElement, styleSettings: CodeStyleSettings): PsiElement {
    if (el.language.displayName == LanguageNames.vue) {
      if (el is XmlTag) {
        VueTemplateTagFix.fixElement(el)
      }
    }
    return el
  }

  override fun processText(file: PsiFile, textRange: TextRange, styleSettings: CodeStyleSettings): TextRange {
    if (file is HtmlFileImpl && file.language.displayName == LanguageNames.vue) {
      val templateTag = file.document?.children?.firstOrNull { it is XmlTag && it.name == "template" }
      if (templateTag != null) {
        object:RecursiveVisitor(templateTag) {
          override fun visit(element: PsiElement) {
            if (element is XmlTag) {
              VueTemplateTagFix.fixWhitespace(element)
            }
          }
        }.run()
      }
    }
    return TextRange(0, file.endOffset)
  }

}
