package org.whiteprint.platform.infra.persistence.jpa.repository

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.whiteprint.platform.infra.persistence.jpa.entity.ProjectionEntity
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

/**
 * 프로젝션 upsert SQL 을 **엔티티 타입마다 한 번 만들어 캐시**한다.
 *
 * 손으로 쓰지 않는 이유는 하나다 — 복제본이 늘 때마다 컴파일러가 검사하지 않는 SQL 이
 * 한 벌씩 복사되고, 그중 하나만 stale 가드를 빠뜨려도 **아무 로그 없이** 이벤트가 사라진다.
 *
 * 만들어지는 모양:
 * ```sql
 * INSERT INTO <table> (<all columns>) VALUES (?, ?, ...)
 * ON CONFLICT (<pk columns>) DO UPDATE SET <non-pk column> = EXCLUDED.<non-pk column>, ...
 * WHERE <table>.version < EXCLUDED.version
 * ```
 * 마지막 `WHERE` 가 **stale 가드**다. mongo 의 `ProjectionRepositorySupport.upsert()` 가
 * `version lt` 조건으로 하는 것과 같은 판정이며, 한 문장이라 원자적이다 —
 * 읽고-비교하고-쓰는 방식과 달리 처리 모드가 무엇이든 안전하다.
 *
 * ⚠ `ON CONFLICT` 는 **PostgreSQL 문법**이다. 다른 방언을 쓰게 되면 이 클래스가 갈라져야 한다.
 */
internal object ProjectionUpsertStatement {

    private val cache = ConcurrentHashMap<Class<*>, Prepared>()

    /** [sql] 의 `?` 순서와 [binders] 순서가 1:1 이다. */
    internal class Prepared(
        val sql: String,
        val binders: List<Field>,
    )

    fun of(type: Class<*>): Prepared = cache.computeIfAbsent(type, ::build)

    private fun build(type: Class<*>): Prepared {
        require(ProjectionEntity::class.java.isAssignableFrom(type)) {
            "upsertProjection 은 ProjectionEntity 하위 타입에만 쓸 수 있다: ${type.name}"
        }

        val table = tableName(type)
        val fields = mappedFields(type)
        require(fields.isNotEmpty()) { "매핑된 @Column 필드가 없다: ${type.name}" }

        val idColumns = fields.filter { it.isAnnotationPresent(Id::class.java) }.map(::columnName)
        require(idColumns.isNotEmpty()) {
            "@Id 가 없다 — 구체 엔티티가 식별자를 선언해야 한다(단일 또는 @IdClass 복합): ${type.name}"
        }

        val columns = fields.map(::columnName)
        require(VERSION_COLUMN in columns) {
            "'$VERSION_COLUMN' 컬럼이 없다 — stale 가드를 걸 수 없다: ${type.name}"
        }

        val assignments = columns.filterNot { it in idColumns }
            .joinToString(", ") { """"$it" = EXCLUDED."$it"""" }

        val sql = buildString {
            append("""INSERT INTO "$table" (""")
            append(columns.joinToString(", ") { """"$it"""" })
            append(") VALUES (")
            append(columns.joinToString(", ") { "?" })
            append(") ON CONFLICT (")
            append(idColumns.joinToString(", ") { """"$it"""" })
            append(") DO UPDATE SET ")
            append(assignments)
            // stale 가드 — 저장된 것보다 새로운 이벤트만 반영한다.
            append(""" WHERE "$table"."$VERSION_COLUMN" < EXCLUDED."$VERSION_COLUMN"""")
        }

        return Prepared(sql, fields)
    }

    /** 상위 클래스(@MappedSuperclass)의 필드까지 훑는다 — 감사 필드가 [ProjectionEntity] 에 있기 때문이다. */
    private fun mappedFields(type: Class<*>): List<Field> {
        val collected = mutableListOf<Field>()
        var current: Class<*>? = type
        while (current != null && current != Any::class.java) {
            current.declaredFields
                .filter { it.isAnnotationPresent(Column::class.java) && !it.isAnnotationPresent(Transient::class.java) }
                .forEach { it.isAccessible = true; collected += it }
            current = current.superclass
        }
        // 컬럼명 중복은 매핑이 깨진 것이다(같은 이름을 두 필드가 주장).
        val duplicated = collected.groupBy(::columnName).filterValues { it.size > 1 }.keys
        require(duplicated.isEmpty()) { "컬럼명이 중복된다 $duplicated: ${type.name}" }
        return collected
    }

    private fun tableName(type: Class<*>): String =
        type.getAnnotation(Table::class.java)?.name?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("@Table(name = ...) 이 필요하다: ${type.name}")

    private fun columnName(field: Field): String =
        field.getAnnotation(Column::class.java).name.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("@Column(name = ...) 이 필요하다: ${field.declaringClass.name}.${field.name}")

    private const val VERSION_COLUMN = "version"

}
