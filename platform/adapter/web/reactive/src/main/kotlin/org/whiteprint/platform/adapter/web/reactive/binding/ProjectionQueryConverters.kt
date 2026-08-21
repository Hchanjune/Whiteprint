package org.whiteprint.platform.adapter.web.reactive.binding

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalConverter
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import org.whiteprint.platform.core.projection.model.query.cursor.CursorDirection
import org.whiteprint.platform.core.projection.model.sort.SortDirection
import org.whiteprint.platform.core.projection.model.sort.SortableField
import org.whiteprint.platform.core.projection.policy.SortPolicy
import org.whiteprint.platform.core.projection.policy.SortPolicyException

/**
 * SortableField 구현 enum 전용 웹 바인딩. 영숫자만 남기고 lowercase한 canonical form으로 비교하므로
 * `inserted-at` / `inserted_at` / `insertedAt` / `INSERTED_AT` 전부 같은 값으로 취급한다.
 * (공식 wire name은 paramName의 kebab-case — 나머지는 관대하게 수용)
 *
 * [ConditionalConverter.matches]로 SortableField 구현 enum에만 적용되므로
 * 애플리케이션의 다른 enum 바인딩에는 영향을 주지 않는다.
 * (나중에 등록된 컨버터가 기본 StringToEnum보다 우선한다)
 */
class SortableFieldConverterFactory : ConverterFactory<String, Enum<*>>, ConditionalConverter {

    override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean =
        SortableField::class.java.isAssignableFrom(targetType.type)

    override fun <T : Enum<*>> getConverter(targetType: Class<T>): Converter<String, T> =
        Converter { source ->
            val canonical = canonical(source)
            targetType.enumConstants.firstOrNull { constant ->
                constant is SortableField &&
                    (canonical(constant.paramName) == canonical || canonical(constant.name) == canonical)
            } ?: throw SortPolicyException(
                policy = SortPolicy.SORT_FIELD_MAP_FAILED,
                attributes = mapOf(
                    "keys" to targetType.enumConstants
                        .filterIsInstance<SortableField>()
                        .joinToString(", ") { it.paramName }
                )
            )
        }

    private fun canonical(value: String): String = value.filter { it.isLetterOrDigit() }.lowercase()
}

/** `sortDirection=desc` 같은 소문자 입력 허용. */
class SortDirectionConverter : Converter<String, SortDirection> {
    override fun convert(source: String): SortDirection = SortDirection.valueOf(source.trim().uppercase())
}

/** `direction=forward` 같은 소문자 입력 허용. */
class CursorDirectionConverter : Converter<String, CursorDirection> {
    override fun convert(source: String): CursorDirection = CursorDirection.valueOf(source.trim().uppercase())
}
