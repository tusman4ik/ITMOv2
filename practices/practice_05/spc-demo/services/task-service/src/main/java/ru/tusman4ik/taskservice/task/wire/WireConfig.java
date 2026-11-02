package ru.tusman4ik.taskservice.task.wire;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tusman4ik.taskservice.task.wire.implementations.AttachmentHandler;
import ru.tusman4ik.taskservice.task.wire.implementations.FingerprintCollector;
import ru.tusman4ik.taskservice.task.wire.implementations.MemoizeHandler;
import ru.tusman4ik.taskservice.task.wire.implementations.NodeScanner;
import ru.tusman4ik.taskservice.task.wire.implementations.TemplateHandler;

@Configuration
@RequiredArgsConstructor
public class WireConfig {

    private final NodeScanner nodeScanner;
    private final AttachmentHandler attachmentHandler;
    private final MemoizeHandler memoizeHandler;
    private final TemplateHandler templateHandler;
    private final FingerprintCollector fingerprintCollector;

    @Bean
    public WireChain chain() {
        return WireChain.of(nodeScanner)
                .andAfter(attachmentHandler)
                .andAfter(memoizeHandler)
                .andAfter(templateHandler)
                .andAfter(fingerprintCollector);
    }
}
