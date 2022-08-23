package com.toocol.termio.utilities.event.core

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 16:54
 * @version: 0.0.1
 */
class EventCodec : MessageCodec<AbstractEvent, AbstractEvent>{
    override fun encodeToWire(buffer: Buffer, s: AbstractEvent?) {
        val jsonObject = JsonObject.mapFrom(s)
        jsonObject.put("class", s?.javaClass?.name)
        buffer.appendBuffer(jsonObject.toBuffer())
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer?): AbstractEvent {
        val jsonObject = Json.decodeValue(buffer) as JsonObject
        val clazz = Class.forName(jsonObject.getString("class"))
        jsonObject.remove("class")
        return Json.decodeValue(jsonObject.toBuffer(), clazz) as AbstractEvent
    }

    override fun transform(s: AbstractEvent?): AbstractEvent? {
        return s
    }

    override fun name(): String {
        return "event-codec"
    }

    override fun systemCodecID(): Byte {
        return -1
    }

}