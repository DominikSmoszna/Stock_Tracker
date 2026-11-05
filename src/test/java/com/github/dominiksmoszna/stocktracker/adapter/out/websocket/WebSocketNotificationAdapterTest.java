package com.github.dominiksmoszna.stocktracker.adapter.out.websocket;

import com.github.dominiksmoszna.stocktracker.application.dto.AlertNotificationDto;
import com.github.dominiksmoszna.stocktracker.domain.model.AlertType;
import com.github.dominiksmoszna.stocktracker.domain.model.PriceAlert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationAdapterTest {

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    void shouldPublishAlertNotificationOnAlertsTopic() {
        Instant triggeredAt = Instant.parse("2026-11-05T15:00:00Z");
        PriceAlert alert = PriceAlert.active(UUID.randomUUID(), UUID.randomUUID(), "AAPL",
                new BigDecimal("100"), AlertType.ABOVE).markTriggered(triggeredAt);
        WebSocketNotificationAdapter adapter = new WebSocketNotificationAdapter(simpMessagingTemplate);

        adapter.sendNotification(alert, new BigDecimal("101.5"));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/alerts"), payload.capture());
        assertThat(payload.getValue()).isEqualTo(new AlertNotificationDto(
                alert.id(), alert.userId(), "AAPL", AlertType.ABOVE,
                new BigDecimal("100"), new BigDecimal("101.5"), triggeredAt));
    }
}
