package com.barberia.modules.finance.mapper;

import com.barberia.modules.finance.dto.FinanzaResponse;
import com.barberia.modules.finance.entity.Finanza;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class FinanzaMapperImpl implements FinanzaMapper {

    @Override
    public FinanzaResponse toResponse(Finanza f) {
        if ( f == null ) {
            return null;
        }

        FinanzaResponse.FinanzaResponseBuilder finanzaResponse = FinanzaResponse.builder();

        finanzaResponse.id( f.getId() );
        finanzaResponse.descripcion( f.getDescripcion() );
        finanzaResponse.monto( f.getMonto() );
        finanzaResponse.fecha( f.getFecha() );
        finanzaResponse.usuarioNombre( f.getUsuarioNombre() );
        finanzaResponse.referencia( f.getReferencia() );
        finanzaResponse.ventaId( f.getVentaId() );
        finanzaResponse.createdAt( f.getCreatedAt() );

        finanzaResponse.tipo( f.getTipo().name() );
        finanzaResponse.categoria( f.getCategoria().name() );

        return finanzaResponse.build();
    }
}
