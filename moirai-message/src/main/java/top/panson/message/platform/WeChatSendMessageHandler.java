/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.message.platform;

import top.panson.common.toolkit.FileUtil;
import top.panson.common.toolkit.Singleton;
import top.panson.common.toolkit.http.HttpUtil;
import top.panson.message.enums.NotifyPlatformEnum;
import top.panson.message.platform.base.AbstractRobotSendMessageHandler;
import top.panson.message.platform.base.RobotMessageActualContent;
import top.panson.message.platform.base.RobotMessageExecuteDTO;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static top.panson.message.platform.constant.WeChatAlarmConstants.*;

/**
 * WeChat send message handler.
 */
@Slf4j
public class WeChatSendMessageHandler extends AbstractRobotSendMessageHandler {

    @Override
    public String getType() {
        return NotifyPlatformEnum.WECHAT.name();
    }

    @Override
    protected RobotMessageActualContent buildMessageActualContent() {
        String weChatAlarmTxtKey = "message/robot/dynamic-thread-pool/wechat-alarm.txt";
        String weChatConfigTxtKey = "message/robot/dynamic-thread-pool/wechat-config.txt";
        return RobotMessageActualContent.builder()
                .receiveSeparator("><@")
                .changeSeparator("  ➲  ")
                .replaceTxt(WE_CHAT_ALARM_TIMOUT_REPLACE_TXT)
                .traceReplaceTxt(WE_CHAT_ALARM_TIMOUT_TRACE_REPLACE_TXT)
                .alarmMessageContent(Singleton.get(weChatAlarmTxtKey, () -> FileUtil.readUtf8String(weChatAlarmTxtKey)))
                .configMessageContent(Singleton.get(weChatConfigTxtKey, () -> FileUtil.readUtf8String(weChatConfigTxtKey)))
                .build();
    }

    @Override
    protected void execute(RobotMessageExecuteDTO robotMessageExecuteDTO) {
        String serverUrl = WE_CHAT_SERVER_URL + robotMessageExecuteDTO.getNotifyConfig().getSecretKey();
        try {
            WeChatReqDTO weChatReq = new WeChatReqDTO();
            weChatReq.setMsgtype("markdown");
            Markdown markdown = new Markdown();
            markdown.setContent(robotMessageExecuteDTO.getText());
            weChatReq.setMarkdown(markdown);
            HttpUtil.post(serverUrl, weChatReq);
        } catch (Exception ex) {
            log.error("WeChat failed to send message", ex);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class WeChatReqDTO {

        private String msgtype;

        private Markdown markdown;
    }

    @Data
    public static class Markdown {

        private String content;
    }
}
