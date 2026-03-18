package io.github.hazemafaneh.networkinspectionpro

import io.github.hazemafaneh.networkinspectionpro.internal.utils.currentTimeMs
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsText
import io.ktor.util.AttributeKey
import io.ktor.util.toMap

private val RequestIdKey = AttributeKey<String>("NetworkInspectorRequestId")
private val RequestStartKey = AttributeKey<Long>("NetworkInspectorRequestStart")

val NetworkInspectorPlugin = createClientPlugin("NetworkInspectorPlugin") {
    onRequest { request, body ->
        if (!NetworkInspectionPro.isEnabled) return@onRequest

        val id = currentTimeMs().toString() + request.url.buildString()
        val startMs = currentTimeMs()
        request.attributes.put(RequestIdKey, id)
        request.attributes.put(RequestStartKey, startMs)

        val requestBodyText = try {
            body?.toString()
        } catch (_: Exception) {
            null
        }

        val entry = NetworkLogEntry(
            id = id,
            method = request.method.value,
            url = request.url.buildString(),
            requestHeaders = request.headers.build().toMap().mapValues { it.value.joinToString(", ") },
            requestBody = requestBodyText,
            timestampMs = startMs
        )
        NetworkLogStore.addOrUpdate(entry)
    }

    onResponse { response ->
        if (!NetworkInspectionPro.isEnabled) return@onResponse

        val id = response.call.request.attributes.getOrNull(RequestIdKey) ?: return@onResponse
        val startMs = response.call.request.attributes.getOrNull(RequestStartKey) ?: currentTimeMs()
        val durationMs = currentTimeMs() - startMs

        val responseBodyText = try {
            response.bodyAsText()
        } catch (_: Exception) {
            null
        }

        val existing = NetworkLogStore.entries.value.firstOrNull { it.id == id }
        val updated = (existing ?: NetworkLogEntry(
            id = id,
            method = response.call.request.method.value,
            url = response.call.request.url.toString(),
            timestampMs = startMs
        )).copy(
            responseCode = response.status.value,
            responseHeaders = response.headers.toMap().mapValues { it.value.joinToString(", ") },
            responseBody = responseBodyText,
            durationMs = durationMs,
            isError = response.status.value >= 400
        )
        NetworkLogStore.addOrUpdate(updated)
    }
}
