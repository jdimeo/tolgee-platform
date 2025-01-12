package io.tolgee.service.machineTranslation

import io.tolgee.component.machineTranslation.MtServiceManager
import io.tolgee.component.machineTranslation.TranslateResult
import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.constants.Message
import io.tolgee.constants.MtServiceType
import io.tolgee.events.OnAfterMachineTranslationEvent
import io.tolgee.exceptions.BadRequestException
import io.tolgee.helpers.TextHelper
import io.tolgee.model.Language
import io.tolgee.model.Project
import io.tolgee.model.key.Key
import io.tolgee.service.project.ProjectService
import io.tolgee.service.translation.TranslationService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class MtService(
  private val translationService: TranslationService,
  private val machineTranslationManager: MtServiceManager,
  private val applicationEventPublisher: ApplicationEventPublisher,
  private val projectService: ProjectService,
  private val mtServiceConfigService: MtServiceConfigService,
  private val tolgeeProperties: TolgeeProperties
) {
  fun getMachineTranslations(key: Key, targetLanguage: Language):
    Map<MtServiceType, String?>? {
    val baseLanguage = projectService.getOrCreateBaseLanguage(key.project.id)!!
    val baseTranslationText = translationService.find(key, baseLanguage).orElse(null)?.text

    if (baseTranslationText.isNullOrBlank()) {
      return null
    }

    return getMachineTranslations(key.project, baseTranslationText, baseLanguage, targetLanguage)
  }

  fun getMachineTranslations(
    project: Project,
    baseTranslationText: String,
    targetLanguage: Language
  ): Map<MtServiceType, String?>? {
    val baseLanguage = projectService.getOrCreateBaseLanguage(project.id)!!
    return getMachineTranslations(project, baseTranslationText, baseLanguage, targetLanguage)
  }

  fun getPrimaryMachineTranslations(key: Key, targetLanguages: List<Language>):
    List<TranslateResult?> {
    val baseLanguage = projectService.getOrCreateBaseLanguage(key.project.id)!!
    val baseTranslationText = translationService.find(key, baseLanguage).orElse(null)?.text
      ?: return targetLanguages.map { null }
    return getPrimaryMachineTranslations(key.project, baseTranslationText, baseLanguage, targetLanguages)
  }

  private fun getPrimaryMachineTranslations(
    project: Project,
    baseTranslationText: String,
    baseLanguage: Language,
    targetLanguages: List<Language>
  ): List<TranslateResult?> {
    checkTextLength(baseTranslationText)
    val primaryServices = mtServiceConfigService.getPrimaryServices(targetLanguages.map { it.id }, project)
    val prepared = TextHelper.replaceIcuParams(baseTranslationText)

    val serviceIndexedLanguagesMap = targetLanguages
      .asSequence()
      .mapIndexed { idx, lang -> idx to lang }
      .groupBy { primaryServices[it.second.id] }

    val translationResults = serviceIndexedLanguagesMap.map { (service, languageIdxPairs) ->
      service?.let {
        val translateResults = machineTranslationManager.translate(
          prepared.text,
          baseLanguage.tag,
          languageIdxPairs.map { it.second.tag },
          service
        )

        val withReplacedParams = translateResults.map { translateResult ->
          translateResult.translatedText = translateResult.translatedText?.replaceParams(prepared.params)
          translateResult
        }
        languageIdxPairs.map { it.first }.zip(withReplacedParams)
      } ?: languageIdxPairs.map { it.first to null }
    }
      .flatten()
      .sortedBy { it.first }
      .map { it.second }

    val actualPrice = translationResults.sumOf { it?.actualPrice ?: 0 }

    publishOnAfterEvent(prepared, project, actualPrice)

    return translationResults
  }

  fun getMachineTranslations(
    project: Project,
    baseTranslationText: String,
    baseLanguage: Language,
    targetLanguage: Language
  ): Map<MtServiceType, String?>? {
    checkTextLength(baseTranslationText)
    val enabledServices = mtServiceConfigService.getEnabledServices(targetLanguage.id)
    val prepared = TextHelper.replaceIcuParams(baseTranslationText)

    val results = machineTranslationManager
      .translateUsingAll(prepared.text, baseLanguage.tag, targetLanguage.tag, enabledServices)

    val actualPrice = results.entries.sumOf { it.value.actualPrice }

    publishOnAfterEvent(prepared, project, actualPrice)

    return results.map { (serviceName, translated) ->
      serviceName to translated.translatedText?.replaceParams(prepared.params)
    }.toMap()
  }

  private fun publishOnAfterEvent(
    prepared: TextHelper.ReplaceIcuResult,
    project: Project,
    actualPrice: Int
  ) {
    applicationEventPublisher.publishEvent(
      OnAfterMachineTranslationEvent(this, prepared.text, project, actualPrice)
    )
  }

  private fun checkTextLength(text: String) {
    if (text.length > tolgeeProperties.maxTranslationTextLength) {
      throw BadRequestException(Message.TRANSLATION_TEXT_TOO_LONG)
    }
  }

  private fun String.replaceParams(params: Map<String, String>): String {
    var replaced = this
    params.forEach { (placeholder, text) ->
      replaced = replaced.replace(placeholder, text)
    }
    return replaced
  }
}
