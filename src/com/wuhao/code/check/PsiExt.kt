/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */
package com.wuhao.code.check

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import com.wuhao.code.check.inspection.fix.SpaceQuickFix
import com.wuhao.code.check.inspection.fix.SpaceQuickFix.Position.*
import org.jetbrains.kotlin.idea.core.moveCaret
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * 获取psi元素的所有祖先元素，按距离从近到远
 */
val PsiElement.ancestors: List<PsiElement>
  get() {
    val ancestors = ArrayList<PsiElement>()
    var el: PsiElement? = this
    while (true) {
      ancestors.add(el!!)
      el = el.parent
      if (el == null) {
        break
      }
    }
    return ancestors
  }

/**
 * 获取kotlin方法的方法体（包含括号）
 */
val KtNamedFunction.body: KtBlockExpression?
  get() {
    return this.getChildOfType()
  }

/**
 * 获取目录下所有缓存的文件
 */
val VirtualDirectoryImpl.cachedPosterity: ArrayList<VirtualFile>
  get() {
    val list = ArrayList<VirtualFile>()
    getCachedChildren(list, this)
    return list
  }

/**
 * psi元素的深度
 */
val PsiElement.depth: Int
  get() {
    var depth = 0
    fun analyzeDepth(children: List<PsiElement>) {
      if (children.isNotEmpty()) {
        depth++
        analyzeDepth(children.map { it.children.toList() }.flatten())
      } else {
        return
      }
    }
    analyzeDepth(this.children.toList())
    return depth
  }

/**
 * 是否是父元素的第一个子元素
 */
val PsiElement.isFirstChild: Boolean
  get() {
    return this.parent != null && this.parent.firstChild == this
  }

/**
 * 判断kotlin属性是否val
 */
val KtProperty.isVal: Boolean
  get() {
    return !this.isVar
  }

/**
 * 获取kt元素的工厂类
 */
val PsiElement.ktPsiFactory: KtPsiFactory
  get() = this.project.ktPsiFactory

/**
 * 获取kt元素的工厂类
 */
val Project.ktPsiFactory: KtPsiFactory
  get() {
    if (KT_PSI_FACTORY_CACHE[this] == null) {
      KT_PSI_FACTORY_CACHE[this] = KtPsiFactory(this)
    }
    return KT_PSI_FACTORY_CACHE[this]!!
  }

/**
 * 和当前元素并列的后一个元素
 */
val PsiElement.next: PsiElement
  get() = this.nextSibling

/**
 * 获取当前元素并列的下一个非空白元素
 */
val PsiElement.nextIgnoreWs: PsiElement?
  get() {
    var sibling = this.nextSibling
    while (sibling != null && sibling is PsiWhiteSpace) {
      sibling = sibling.nextSibling
    }
    return sibling
  }

/**
 * 获取所有后代元素
 */
val PsiElement.posterity: ArrayList<PsiElement>
  get() {
    val list = ArrayList<PsiElement>()
    getChildren(list, this)
    return list
  }

/**
 * 和当前元素并列的前一个元素
 */
val PsiElement.prev: PsiElement
  get() = this.prevSibling

/**
 * 获取当前元素之前的第一个非空白元素
 */
val PsiElement.prevIgnoreWs: PsiElement?
  get() {
    var sibling = this.prevSibling
    while (sibling != null && sibling is PsiWhiteSpace) {
      sibling = sibling.prevSibling
    }
    return sibling
  }

/**
 * 获取java psi元素的工厂类
 * @return java psi元素的工厂类
 */
val PsiElement.psiElementFactory: PsiElementFactory
  get() {
    if (PSI_ELEMENT_FACTORY_CACHE[this.project] == null) {
      PSI_ELEMENT_FACTORY_CACHE[this.project] = PsiElementFactoryImpl(PsiManagerEx.getInstanceEx(this.project))
    }
    return PSI_ELEMENT_FACTORY_CACHE[this.project]!!
  }

private val KT_PSI_FACTORY_CACHE = HashMap<Project, KtPsiFactory>()

private val PSI_ELEMENT_FACTORY_CACHE = HashMap<Project, PsiElementFactory>()

/**
 * 获取指定类型的最近的祖先元素
 */
inline fun <reified T> PsiElement.ancestorOfType(): T? {
  var el: PsiElement? = this.parent
  while (el != null && el !is T) {
    el = el.parent
  }
  return el as T?
}

/**
 * 选择符合条件的第一个子元素
 * @param predicate 筛选条件
 * @return 符合条件的第一个子元素
 */
fun PsiElement.firstChild(predicate: (PsiElement) -> Boolean): PsiElement? {
  return this.children.firstOrNull(predicate)
}

/**
 * 按距离获取祖先元素，0为parent，如果没有找到则返回null
 * @param level 距离
 */
fun PsiElement.getAncestor(level: Int): PsiElement? {
  var el: PsiElement? = this
  for (i in 0 until level) {
    el = el?.parent
    if (el == null) {
      return null
    }
  }
  return el
}

/**
 * 获取指定类型的所有的祖先元素
 */
inline fun <reified T> PsiElement.getAncestorsOfType(): ArrayList<T> {
  val result = arrayListOf<T>()
  var el: PsiElement? = this.parent
  while (el != null) {
    if (el is T) {
      result.add(el)
    }
    el = el.parent
  }
  return result
}

/**
 * 获取连续的指定类型的所有的祖先元素
 */
inline fun <reified T> PsiElement.getContinuousAncestorsMatches(
    predicate: (PsiElement) -> Boolean
): List<T> {
  val result = arrayListOf<T>()
  var el: PsiElement? = this.parent
  while (el != null && el is T && predicate(el)) {
    result.add(el)
    el = el.parent
  }
  return result
}

/**
 * 获取连续的指定类型的所有的祖先元素
 */
inline fun <reified T> PsiElement.getContinuousAncestorsOfType(): ArrayList<T> {
  val result = arrayListOf<T>()
  var el: PsiElement? = this.parent
  while (el != null && el is T) {
    result.add(el)
    el = el.parent
  }
  return result
}

/**
 * 获取空行元素
 */
fun Project.getNewLine(): PsiWhiteSpace {
  return this.ktPsiFactory.createNewLine() as PsiWhiteSpace
}

/**
 * 创建空白行元素
 * @param count 换行数
 * @return 空白行元素
 */
fun PsiElement.getNewLine(count: Int = 1): PsiWhiteSpace {
  return this.ktPsiFactory.createNewLine(count) as PsiWhiteSpace
}

/**
 * 获取当前元素之前连续的同类型元素
 * @return 符合条件的同类型元素
 */
inline fun <reified T> PsiElement.getPrevContinuousSiblingsOfType(): ArrayList<T> {
  var sibling = this.prevSibling
  val result = arrayListOf<T>()
  while (sibling != null && sibling is T) {
    result.add(sibling)
    sibling = sibling.prevSibling
  }
  return result
}

/**
 * 获取当前元素之前连续的同类型元素，且排除空白元素
 * @return 符合条件的同类型元素
 */
inline fun <reified T> PsiElement.getPrevContinuousSiblingsOfTypeIgnoreWhitespace(): ArrayList<T> {
  var sibling = this.prevSibling
  val result = arrayListOf<T>()
  while (sibling != null && (sibling is T || sibling is PsiWhiteSpace)) {
    if (sibling is T) {
      result.add(sibling)
    }
    sibling = sibling.prevSibling
  }
  return result
}

/**
 * 获取当前元素之前指定类型的所有同级元素
 * @return 符合条件的元素结合
 */
inline fun <reified T> PsiElement.getPrevSiblingsOfType(): List<T> {
  var sibling = this.prevSibling
  val list = arrayListOf<T>()
  while (sibling != null) {
    if (sibling is T) {
      list.add(sibling)
    }
    sibling = sibling.prevSibling
  }
  return list
}

/**
 * 获取指定类型的同级元素
 */
inline fun <reified T> PsiElement.getSiblingsOfType(): List<PsiElement> {
  return if (this.parent == null) {
    listOf()
  } else {
    this.parent.children.filter { it is T }
  }
}

/**
 * 获取空白元素
 */
fun getWhiteSpace(project: Project): PsiWhiteSpace {
  return KtPsiFactory(project).createWhiteSpace() as PsiWhiteSpace
}

/**
 * 将当前psi元素插入到指定元素后面
 * @param element 指定的元素
 */
fun PsiElement.insertAfter(element: PsiElement): PsiElement {
  return element.insertElementAfter(this)
}

/**
 * 将当前psi元素插入到指定元素前面
 * @param element 指定的元素
 */
fun PsiElement.insertBefore(element: PsiElement): PsiElement {
  return element.insertElementBefore(this)
}

/**
 * 在当前psi元素后插入元素
 * @param element 待插入的元素
 */
fun PsiElement.insertElementAfter(element: PsiElement): PsiElement {
  return this.parent.addAfter(element, this)
}

/**
 * 在当前psi元素前插入元素
 * @param element 待插入的元素
 */
fun PsiElement.insertElementBefore(element: PsiElement): PsiElement {
  return this.parent.addBefore(element, this)
}

/**
 * 在当前元素前面插入多个元素
 * @param elements 待插入的元素
 */
fun PsiElement.insertElementsBefore(vararg elements: PsiElement) {
  elements.forEach {
    this.insertElementBefore(it)
  }
}

/**
 * 取第一个特定类型的子元素
 */
inline fun <reified T> PsiElement.isFirstChildOfType(): Boolean {
  return this.parent != null && this.parent.children.firstOrNull { it is T } == this
}

/**
 * 重命名元素
 * @param element 待重命名的元素
 */
fun renameElement(element: PsiElement,
                  caretOffset: Int = -1,
                  parentElement: PsiElement? = null,
                  indexInParent: Int = -1) {
  val context = DataManager.getInstance()
      .dataContextFromFocus.result
  val editor = context.getData(CommonDataKeys.EDITOR)!!
  PsiDocumentManager.getInstance(element.project).doPostponedOperationsAndUnblockDocument(editor.document)
  val realElement = if (parentElement != null && indexInParent >= 0) {
    parentElement.children[indexInParent]
  } else {
    element
  }
  if (caretOffset < 0) {
    editor.moveCaret(realElement.startOffset)
  } else {
    editor.moveCaret(caretOffset)
  }
  editor.settings.isVariableInplaceRenameEnabled = true
  val handler = VariableInplaceRenameHandler()
  handler.invoke(realElement.project, editor, realElement.containingFile, context)
}

/**
 * 在当前元素后面添加空行
 * @param blankLines 空白行数
 */
fun PsiElement.setBlankLineAfter(blankLines: Int = 0) {
  setBlankLine(blankLines, After)
}

/**
 * 在当前元素前面添加空行
 * @param blankLines 空白行数
 */
fun PsiElement.setBlankLineBefore(blankLines: Int = 0) {
  setBlankLine(blankLines, Before)
}

/**
 * 在当前元素前后添加空行
 * @param blankLines 空白行数
 */
fun PsiElement.setBlankLineBoth(blankLines: Int = 0) {
  setBlankLine(blankLines, Both)
}

/**
 *
 * @param list
 * @param virtualDirectoryImpl
 */
private fun getCachedChildren(list: ArrayList<VirtualFile>, virtualDirectoryImpl: VirtualDirectoryImpl) {
  list.addAll(virtualDirectoryImpl.cachedChildren.filter { !it.isDirectory })
  virtualDirectoryImpl.cachedChildren.filter { it is VirtualDirectoryImpl }
      .forEach {
        getCachedChildren(list, it as VirtualDirectoryImpl)
      }
}

/**
 *
 * @param list
 * @param psiElement
 */
private fun getChildren(list: ArrayList<PsiElement>, psiElement: PsiElement) {
  if (psiElement.children.isNotEmpty()) {
    list.addAll(psiElement.children)
    psiElement.children.forEach {
      getChildren(list, it)
    }
  }
}

/**
 * 在当前元素的指定位置添加空行
 * @param blankLines 空白行数
 * @param position 添加空白行的位置
 */
private fun PsiElement.setBlankLine(blankLines: Int, position: SpaceQuickFix.Position) {
  val factory = this.ktPsiFactory
  val lineBreaks = blankLines + 1
  if (position == Before || position == Both) {
    if (this.prev !is PsiWhiteSpace) {
      this.insertElementBefore(factory.createNewLine(lineBreaks))
    } else if (this.prev.getLineCount() != lineBreaks) {
      this.prev.replace(factory.createNewLine(lineBreaks))
    }
  }
  if (position == After || position == Both) {
    if (this.next !is PsiWhiteSpace) {
      this.insertElementAfter(factory.createNewLine(lineBreaks))
    } else if (this.next.getLineCount() != lineBreaks) {
      this.next.replace(factory.createNewLine(lineBreaks))
    }
  }
}

