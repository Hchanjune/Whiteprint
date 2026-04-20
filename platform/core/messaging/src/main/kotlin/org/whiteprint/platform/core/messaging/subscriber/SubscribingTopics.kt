package org.whiteprint.platform.core.messaging.subscriber

data class SubscribingTopics(
    val topics: List<String>
) {
    fun asList() = topics
}
