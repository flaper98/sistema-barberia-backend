package com.barberia.modules.finance.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.modules.finance.dto.FinanzaRequest;
import com.barberia.modules.finance.dto.FinanzaResponse;
import com.barberia.modules.finance.dto.ResumenFinancieroResponse;
import com.barberia.modules.finance.entity.CategoriaFinanza;
import com.barberia.modules.finance.entity.Finanza;
import com.barberia.modules.finance.entity.TipoFinanza;
import com.barberia.modules.finance.mapper.FinanzaMapper;
import com.barberia.modules.finance.repository.FinanzaRepository;
import com.barberia.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanzaServiceImpl {

    private final FinanzaRepository finanzaRepository;
    private final FinanzaMapper finanzaMapper;

    @Transactional(readOnly = true)
    public PageResponse<FinanzaResponse> listar(TipoFinanza tipo, CategoriaFinanza categoria,
                                                 LocalDate desde, LocalDate hasta, Pageable pageable) {
        return PageResponse.from(
                finanzaRepository.findWithFilters(tipo, categoria, desde, hasta, pageable)
                        .map(finanzaMapper::toResponse));
    }

    @Transactional
    public FinanzaResponse registrar(FinanzaRequest request) {
        CustomUserDetails userDetails = getCurrentUser();

        Finanza finanza = Finanza.builder()
                .tipo(request.getTipo())
                .categoria(request.getCategoria())
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .fecha(request.getFecha())
                .usuarioId(userDetails != null ? userDetails.getId() : null)
                .usuarioNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .referencia(request.getReferencia())
                .build();

        finanza = finanzaRepository.save(finanza);
        log.info("Finanza registrada tipo={} monto={}", request.getTipo(), request.getMonto());
        return finanzaMapper.toResponse(finanza);
    }

    @Transactional(readOnly = true)
    public ResumenFinancieroResponse obtenerResumen(LocalDate desde, LocalDate hasta) {
        BigDecimal ingresos = finanzaRepository.sumByTipoAndFecha(TipoFinanza.INGRESO, desde, hasta);
        BigDecimal egresos = finanzaRepository.sumByTipoAndFecha(TipoFinanza.EGRESO, desde, hasta);
        BigDecimal utilidad = ingresos.subtract(egresos);

        return ResumenFinancieroResponse.builder()
                .totalIngresos(ingresos)
                .totalEgresos(egresos)
                .utilidad(utilidad)
                .periodo(desde + " al " + hasta)
                .build();
    }

    private CustomUserDetails getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) return ud;
        return null;
    }
}
