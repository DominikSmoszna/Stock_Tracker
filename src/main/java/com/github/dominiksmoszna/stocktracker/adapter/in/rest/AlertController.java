package com.github.dominiksmoszna.stocktracker.adapter.in.rest;

import com.github.dominiksmoszna.stocktracker.application.dto.AlertDto;
import com.github.dominiksmoszna.stocktracker.application.dto.CreateAlertRequest;
import com.github.dominiksmoszna.stocktracker.common.mapper.AlertMapper;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManageAlertUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final ManageAlertUseCase  manageAlertUseCase;

    @GetMapping("/{userId}")
    public ResponseEntity<List<AlertDto>> getPriceAlerts(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(manageAlertUseCase.getPriceAlerts(userId).stream().map(AlertMapper::toDto).toList());
    }

    @GetMapping("/{userId}/{ticker}")
    public  ResponseEntity<List<AlertDto>> getPriceAlertsByTicker(@PathVariable("userId") UUID userId,@PathVariable("ticker")  String ticker) {
        return ResponseEntity.ok(manageAlertUseCase.getPriceAlertsByTicker(userId,ticker).stream().map(AlertMapper::toDto).toList());
    }

    @PostMapping("/")
    public ResponseEntity<AlertDto> createAlert(@RequestBody @Valid CreateAlertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AlertMapper.toDto(manageAlertUseCase.addAlert(request.userId(), request.ticker(), request.threshold(), request.type())));
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<Void> deleteAlert(@PathVariable("alertId") UUID alertId) {
        manageAlertUseCase.removeAlert(alertId);
        return  ResponseEntity.noContent().build();
    }

}
