package com.toocol.ssh.core.mosh.core.statesnyc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.toocol.ssh.core.mosh.core.proto.UserInputPB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/6/24 23:18
 * @version:
 */
class UserStreamTest {

    @Test
    void protoUserMessageTest() {
        UserInputPB.UserMessage.Builder output = UserInputPB.UserMessage.newBuilder();
        UserEvent.Resize event = new UserEvent.Resize(60, 50);
        UserInputPB.Instruction.Builder instructionBuilder = UserInputPB.Instruction.newBuilder();
        UserInputPB.ResizeMessage.Builder resizeBuilder = UserInputPB.ResizeMessage.newBuilder();
        resizeBuilder.setWidth(event.getWidth());
        resizeBuilder.setHeight(event.getHeight());
        instructionBuilder.setExtension(UserInputPB.resize, resizeBuilder.build());
        output.addInstruction(instructionBuilder);

        byte[] bytes = output.build().toByteArray();
        try {
            UserInputPB.UserMessage userMessage = UserInputPB.UserMessage.parseFrom(bytes);
            System.out.println(userMessage.toString());
            UserInputPB.Instruction instruction = userMessage.getInstruction(0);
            UserInputPB.ResizeMessage resizeMessage = instruction.getExtension(UserInputPB.resize);
            assertEquals(resizeMessage.getWidth(), 60);
            assertEquals(resizeMessage.getHeight(), 50);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

}