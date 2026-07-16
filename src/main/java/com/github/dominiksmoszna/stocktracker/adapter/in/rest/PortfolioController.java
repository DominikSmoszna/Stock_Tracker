package com.github.dominiksmoszna.stocktracker.adapter.in.rest;

import com.github.dominiksmoszna.stocktracker.application.dto.CreatePositionRequest;
import com.github.dominiksmoszna.stocktracker.application.dto.PortfolioPositionDto;
import com.github.dominiksmoszna.stocktracker.common.mapper.PortfolioMapper;
import com.github.dominiksmoszna.stocktracker.domain.port.in.ManagePortfolioUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final ManagePortfolioUseCase managePortfolioUseCase;

    @GetMapping("/{userId}")
    public ResponseEntity<List<PortfolioPositionDto>> getPositions(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(managePortfolioUseCase.getPositions(userId).stream().map(PortfolioMapper::toDto).toList());
    }

    @PostMapping("/")
    public ResponseEntity<PortfolioPositionDto> addPosition(@RequestBody @Valid CreatePositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioMapper.toDto(managePortfolioUseCase.addPosition(request.userId(), request.ticker(), request.quantity(), request.purchasePrice(), request.purchaseDate())));
    }

    @DeleteMapping("/{positionId}")
    public ResponseEntity<Void> removePosition(@PathVariable("positionId") UUID positionId) {
        managePortfolioUseCase.removePosition(positionId);
        return  ResponseEntity.noContent().build();
    }
}
