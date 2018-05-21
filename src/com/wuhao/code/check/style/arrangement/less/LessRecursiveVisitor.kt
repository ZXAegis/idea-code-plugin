/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check.style.arrangement.less

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor
import org.jetbrains.plugins.less.psi.impl.LESSElementVisitor

/**
 * vue文件psi元素递归访问器
 * @author 吴昊
 * @since
 */
open class LessRecursiveVisitor : LESSElementVisitor(), PsiRecursiveVisitor {

  override fun visitElement(element: PsiElement) {
    element.acceptChildren(this)
  }

}

