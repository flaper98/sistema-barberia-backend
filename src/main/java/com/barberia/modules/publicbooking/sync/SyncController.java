package com.barberia.modules.publicbooking.sync;

import com.barberia.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class SyncController {

    private final VercelCitasSyncService syncService;

    /** POST /api/admin/sync/citas — fuerza una sincronización sin esperar al job programado */
    @PostMapping("/citas")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Integer>> sincronizarCitas() {
        int importadas = syncService.sincronizar();
        return ApiResponse.ok("Sincronización completada", Map.of("importadas", importadas));
    }
}
