/*
 * ©2009-2018 南京擎盾信息科技有限公司 All rights reserved.
 */

package com.wuhao.code.check

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.actions.LastRunReformatCodeOptionsProvider
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.codeStyle.arrangement.Rearranger
import com.intellij.psi.codeStyle.arrangement.group.ArrangementGroupingRule
import com.intellij.psi.codeStyle.arrangement.match.ArrangementSectionRule
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementEntryMatcher
import com.intellij.psi.codeStyle.arrangement.match.StdArrangementMatchRule
import com.intellij.psi.codeStyle.arrangement.model.ArrangementAtomMatchCondition
import com.intellij.psi.codeStyle.arrangement.model.ArrangementCompositeMatchCondition
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementExtendableSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementRuleAliasToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettings
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Grouping.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Modifier.*
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Order.*
import java.util.*

/**
 * 项目启动时运行
 * @author 吴昊
 * @since 1.2.6
 */
class PostStart : StartupActivity {

  override fun runActivity(project: Project) {
    val rearrange = Rearranger.EXTENSION.forLanguage(JavaLanguage.INSTANCE)
    val myLastRunSettings = LastRunReformatCodeOptionsProvider(PropertiesComponent.getInstance())
    myLastRunSettings.saveRearrangeCodeState(true)
    myLastRunSettings.saveRearrangeState(JavaLanguage.INSTANCE, true)
    myLastRunSettings.saveOptimizeImportsState(true)
    //    val project = element.psi.project
    val settings = CodeStyle.getSettings(project)
    val commonSettings = settings.getCommonSettings(JavaLanguage.INSTANCE)
    commonSettings.setArrangementSettings(createSettings())

//    ArrangementGroupingRule(ArrangementSettingsToken(), ArrangementSettingsToken())
  }

  private fun createSettings(): StdArrangementSettings {
    val groupingRules = listOf(
        ArrangementGroupingRule(GETTERS_AND_SETTERS, KEEP),
        ArrangementGroupingRule(OVERRIDDEN_METHODS, BY_NAME),
        ArrangementGroupingRule(DEPENDENT_METHODS, BREADTH_FIRST)
    )
    val sections = getRules().map { rule ->
      if (rule.order == null) {
        StdArrangementMatchRule(StdArrangementEntryMatcher(
            ArrangementCompositeMatchCondition().apply {
              rule.template.forEach { token ->
                this.addOperand(ArrangementAtomMatchCondition(token))
              }
            }
        ), BY_NAME)
      } else {
        StdArrangementMatchRule(StdArrangementEntryMatcher(
            ArrangementCompositeMatchCondition().apply {
              rule.template.forEach { token ->
                this.addOperand(ArrangementAtomMatchCondition(token))
              }
            }
        ))
      }
    }.map {
      ArrangementSectionRule.create(it)
    }
    val tokens = listOf(StdArrangementRuleAliasToken("visibility").apply {
      definitionRules = ArrayList<StdArrangementMatchRule>().apply {
        and(this, PUBLIC)
        and(this, PACKAGE_PRIVATE)
        and(this, PROTECTED)
        and(this, PRIVATE)
      }
    })
    return StdArrangementExtendableSettings(groupingRules, sections, tokens!!)
  }

  private fun and(matchRules: MutableList<StdArrangementMatchRule>, vararg conditions: ArrangementSettingsToken) {
    if (conditions.size == 1) {
      matchRules.add(StdArrangementMatchRule(StdArrangementEntryMatcher(ArrangementAtomMatchCondition(
          conditions[0]
      ))))
      return
    }

    val composite = ArrangementCompositeMatchCondition()
    for (condition in conditions) {
      composite.addOperand(ArrangementAtomMatchCondition(condition))
    }
    matchRules.add(StdArrangementMatchRule(StdArrangementEntryMatcher(composite)))
  }


  private fun getRules(): List<RuleDescription> {
    return listOf(
        RuleDescription(listOf(FIELD, PUBLIC, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PUBLIC, STATIC), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED, STATIC), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(INIT_BLOCK, STATIC)),
        RuleDescription(listOf(FIELD, PUBLIC, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE, FINAL), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE, FINAL), BY_NAME),

        RuleDescription(listOf(FIELD, PUBLIC), BY_NAME),
        RuleDescription(listOf(FIELD, PROTECTED), BY_NAME),
        RuleDescription(listOf(FIELD, PACKAGE_PRIVATE), BY_NAME),
        RuleDescription(listOf(FIELD, PRIVATE), BY_NAME),
        RuleDescription(listOf(FIELD), BY_NAME),
        RuleDescription(listOf(INIT_BLOCK)),
        RuleDescription(listOf(CONSTRUCTOR)),

        RuleDescription(listOf(METHOD, PUBLIC, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED, STATIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE, STATIC, FINAL), BY_NAME),

        RuleDescription(listOf(METHOD, PUBLIC, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE, STATIC), BY_NAME),
        RuleDescription(listOf(METHOD, PUBLIC, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE, FINAL), BY_NAME),
        RuleDescription(listOf(METHOD, PUBLIC), BY_NAME),
        RuleDescription(listOf(METHOD, PACKAGE_PRIVATE), BY_NAME),
        RuleDescription(listOf(METHOD, PROTECTED), BY_NAME),
        RuleDescription(listOf(METHOD, PRIVATE), BY_NAME),
        RuleDescription(listOf(METHOD), BY_NAME),
        RuleDescription(listOf(ENUM), BY_NAME),
        RuleDescription(listOf(INTERFACE), BY_NAME),
        RuleDescription(listOf(CLASS, STATIC), BY_NAME),
        RuleDescription(listOf(CLASS, CLASS), BY_NAME))
  }

  /**
   * java代码排序规则描述
   * @author 吴昊
   * @since 1.2.6
   */
  private class RuleDescription(val template: List<ArrangementSettingsToken>) {

    var order: ArrangementSettingsToken? = null

    constructor(template: List<ArrangementSettingsToken>, order: ArrangementSettingsToken)
        : this(template) {
      this.order = order
    }
  }
}
