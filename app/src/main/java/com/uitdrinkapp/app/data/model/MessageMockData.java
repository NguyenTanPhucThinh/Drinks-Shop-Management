package com.uitdrinkapp.app.data.mock;

import com.uitdrinkapp.app.data.model.ChatThread;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Mock hội thoại giữa khách và quán cho tab Tin nhắn. */
public final class MessageMockData {
    private MessageMockData() { }
    private static final long NOW = System.currentTimeMillis();

    public static final List<ChatThread> THREADS = Collections.unmodifiableList(Arrays.asList(
            new ChatThread("t1", "UIT Drink - Quán Demo", "Đơn #TS1024 của bạn đang được pha chế nhé!", NOW - 5 * 60_000L, 1),
            new ChatThread("t2", "UIT Drink - Chi nhánh 2", "Cảm ơn bạn đã ghé quán, hẹn gặp lại!", NOW - 3 * 3_600_000L, 0),
            new ChatThread("t3", "Hỗ trợ khách hàng", "Voucher GIAM10 đã được thêm vào ví của bạn.", NOW - 86_400_000L, 0)));
}