package com.toocol.termio.core.mosh.core.statesnyc

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.InvalidProtocolBufferException
import com.toocol.termio.core.mosh.core.proto.UserInputPB
import com.toocol.termio.core.mosh.core.proto.UserInputPB.UserMessage
import com.toocol.termio.core.mosh.core.statesnyc.UserEvent.Resize
import com.toocol.termio.core.mosh.core.statesnyc.UserEvent.UserBytes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/6/24 23:18
 * @version:
 */
internal class UserStreamTest {
    @Test
    fun protoUserMessageTest() {
        val output = UserMessage.newBuilder()
        val event = Resize(60, 50)
        val instructionBuilder = UserInputPB.Instruction.newBuilder()
        val resizeBuilder = UserInputPB.ResizeMessage.newBuilder()
        resizeBuilder.width = event.width
        resizeBuilder.height = event.height
        instructionBuilder.setExtension(UserInputPB.resize, resizeBuilder.build())
        output.addInstruction(instructionBuilder)
        val bytes = output.build().toByteArray()
        try {
            val registry = ExtensionRegistry.newInstance()
            registry.add(UserInputPB.resize)
            val userMessage = UserMessage.parseFrom(bytes, registry)
            val instruction = userMessage.getInstruction(0)
            val resizeMessage = instruction.getExtension(UserInputPB.resize)
            Assertions.assertEquals(resizeMessage.width, 60)
            Assertions.assertEquals(resizeMessage.height, 50)
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }
    }

    @Test
    fun subtractTest() {
        val current = UserStream()
        current.pushBack(Resize(100, 100))
        current.pushBack(UserBytes(byteArrayOf(100, 100)))
        val assume = UserStream()
        current.subtract(assume)
        Assertions.assertEquals(2, current.actionSize())
        assume.pushBack(Resize(100, 100))
        assume.pushBack(UserBytes(byteArrayOf(100, 100)))
        current.subtract(assume)
        Assertions.assertEquals(0, current.actionSize())
    }
}