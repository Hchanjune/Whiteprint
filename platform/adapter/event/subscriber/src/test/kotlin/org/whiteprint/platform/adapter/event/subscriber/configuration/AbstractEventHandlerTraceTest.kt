package org.whiteprint.platform.adapter.event.subscriber.configuration

import io.github.hchanjune.omk.core.event.EventMetadata
import io.github.hchanjune.omk.core.event.EventMetadataExtractor
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.context.support.GenericApplicationContext
import org.whiteprint.platform.core.messaging.inbox.EventInbox
import org.whiteprint.platform.core.messaging.inbox.EventInboxStore
import org.whiteprint.platform.core.messaging.inbox.InboxEventSerializer
import org.whiteprint.platform.core.messaging.model.Event
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AbstractEventHandlerTraceTest {

    private class TestEvent(
        override val eventType: String = "test.event",
        override val schemaVersion: String = "1",
    ) : Event

    /** 핸들러 안에서 `@ManagedEventHandler` 애스펙트가 볼 메타데이터를 그대로 기록한다. */
    private class RecordingHandler : AbstractEventHandler<TestEvent>() {
        override val eventType = "test.event"
        override val eventClass: KClass<TestEvent> = TestEvent::class
        var seen: EventMetadata? = null
        var failure: Exception? = null

        override fun handle(event: TestEvent) {
            seen = EventMetadataExtractor.extract(arrayOf(event))
            failure?.let { throw it }
        }

        fun process(record: EventInbox) = processOne(record)
    }

    private val payload = ByteArray(1)
    private val event = TestEvent()
    private val store = mock(EventInboxStore::class.java)
    private val handler = RecordingHandler()
    private lateinit var context: GenericApplicationContext

    @BeforeTest
    fun setUp() {
        val serializer = mock(InboxEventSerializer::class.java)
        doReturn(event).`when`(serializer).deserialize(payload, TestEvent::class.java)
        context = GenericApplicationContext().apply {
            beanFactory.registerSingleton("store", store)
            beanFactory.registerSingleton("serializer", serializer)
            beanFactory.registerSingleton("properties", EventSubscriberAutoConfigurationProperties())
            beanFactory.registerSingleton("handler", handler)
            refresh()
        }
        handler.setApplicationContext(context)
    }

    @AfterTest
    fun tearDown() {
        handler.destroy()
        context.close()
    }

    private fun record(traceId: String, causationId: String?): EventInbox {
        val record = mock(EventInbox::class.java)
        doReturn(1L).`when`(record).eventId
        doReturn(traceId).`when`(record).traceId
        doReturn(causationId).`when`(record).causationId
        doReturn(payload).`when`(record).payload
        doReturn("test.event").`when`(record).eventType
        doReturn(0).`when`(record).attemptCount
        return record
    }

    @Test
    fun `핸들러는 인박스 레코드의 traceId 와 causationId 를 이어받는다`() {
        handler.process(record(TRACE_ID, CAUSATION_ID))

        assertEquals(TRACE_ID, handler.seen?.traceId)
        assertEquals(CAUSATION_ID, handler.seen?.causationId)
        verify(store).markCompleted(1L)
    }

    @Test
    fun `issuer 와 eventType 은 넘기지 않는다`() {
        handler.process(record(TRACE_ID, CAUSATION_ID))

        assertNull(handler.seen?.issuer)
        assertNull(handler.seen?.eventType)
    }

    @Test
    fun `causationId 가 비어 있으면 traceId 만 넘긴다`() {
        handler.process(record(TRACE_ID, " "))

        assertEquals(TRACE_ID, handler.seen?.traceId)
        assertNull(handler.seen?.causationId)
    }

    @Test
    fun `traceId 가 비어 있으면 지정하지 않는다`() {
        handler.process(record("", CAUSATION_ID))

        assertNull(handler.seen?.traceId)
    }

    @Test
    fun `핸들러가 실패하면 기존처럼 FAILED 로 넘기고 메타데이터를 남기지 않는다`() {
        handler.failure = IllegalStateException("boom")

        handler.process(record(TRACE_ID, CAUSATION_ID))

        verify(store).markFailed(1L, "boom")
        assertNull(EventMetadataExtractor.extract(arrayOf(event)).traceId)
    }

    private companion object {
        const val TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736"
        const val CAUSATION_ID = "00f067aa0ba902b7"
    }
}
