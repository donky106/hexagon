package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.retry
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import com.rabbitmq.client.*
import com.rabbitmq.client.AMQP.BasicProperties
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

/**
 * Message handler that can reply messages to a reply queue.
 *
 * TODO Test content type support.
 */
internal class Handler<T : Any, R : Any> internal constructor (
    connectionFactory: ConnectionFactory,
    channel: Channel,
    private val executor: ExecutorService,
    private val type: KClass<T>,
    private val handler: (T) -> R) : DefaultConsumer(channel) {

    private companion object {

        private const val RETRIES = 5
        private const val DELAY = 50L
    }

    private val log: Logger = Logger(this)

    private val client: RabbitMqClient by lazy { RabbitMqClient(connectionFactory) }

    /** @see DefaultConsumer.handleDelivery */
    override fun handleDelivery(
        consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {

        executor.execute {
            val charset = properties.contentEncoding ?: defaultCharset().name()
            val correlationId = properties.correlationId
            val replyTo = properties.replyTo
            val messageContentType = properties.contentType ?: defaultFormat.contentType
            val contentType = formatOf(messageContentType, defaultFormat)

            try {
                log.trace { "Received message ($correlationId) in $charset" }
                val request = String(body, Charset.forName(charset))
                log.trace { "Message body:\n$request" }
                val input = request.parse(type, contentType)

                val response = handler(input)

                if (replyTo != null)
                    handleResponse(response, replyTo, correlationId)
            }
            catch (ex: Exception) {
                log.warn(ex) { "Error processing message ($correlationId) in $charset" }

                if (replyTo != null)
                    handleError(ex, replyTo, correlationId)
            }
            finally {
                retry(RETRIES, DELAY) {
                    channel.basicAck(envelope.deliveryTag, false)
                }
            }
        }
    }

    /** @see DefaultConsumer.handleCancel */
    override fun handleCancel(consumerTag: String?) {
        log.error { "Unexpected cancel for the consumer $consumerTag" }
    }

    override fun handleCancelOk(consumerTag: String) {
        log.debug { "Explicit cancel for the consumer $consumerTag" }
    }

    /** @see DefaultConsumer.handleShutdownSignal */
    override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
        val msg = sig?.localizedMessage ?: ""
        log.error { "Error shutting down $msg" }
    }

    private fun handleResponse(response: R, replyTo: String, correlationId: String?) {
        val output = when (response) {
            is String -> response
            is Int -> response.toString()
            is Long -> response.toString()
            else -> response.serialize()
        }

        client.publish(replyTo, output, correlationId)
    }

    private fun handleError(exception: Exception, replyTo: String, correlationId: String?) {
        val message = exception.message ?: ""
        val errorMessage = if (message.isBlank()) exception.javaClass.name else message
        client.publish(replyTo, errorMessage, correlationId)
    }
}
