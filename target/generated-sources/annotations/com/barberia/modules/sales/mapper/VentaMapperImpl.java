package com.barberia.modules.sales.mapper;

import com.barberia.modules.sales.dto.VentaResponse;
import com.barberia.modules.sales.entity.Venta;
import com.barberia.modules.sales.entity.VentaItem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class VentaMapperImpl implements VentaMapper {

    @Override
    public VentaResponse toResponse(Venta venta) {
        if ( venta == null ) {
            return null;
        }

        VentaResponse.VentaResponseBuilder ventaResponse = VentaResponse.builder();

        ventaResponse.items( ventaItemListToVentaItemResponseList( venta.getItems() ) );
        ventaResponse.id( venta.getId() );
        ventaResponse.clienteId( venta.getClienteId() );
        ventaResponse.clienteNombre( venta.getClienteNombre() );
        ventaResponse.barberoId( venta.getBarberoId() );
        ventaResponse.barberoNombre( venta.getBarberoNombre() );
        ventaResponse.usuarioRegistroNombre( venta.getUsuarioRegistroNombre() );
        ventaResponse.subtotal( venta.getSubtotal() );
        ventaResponse.descuento( venta.getDescuento() );
        ventaResponse.total( venta.getTotal() );
        ventaResponse.citaId( venta.getCitaId() );
        ventaResponse.notas( venta.getNotas() );
        ventaResponse.fecha( venta.getFecha() );

        ventaResponse.tipoVenta( venta.getTipoVenta().name() );
        ventaResponse.metodoPago( venta.getMetodoPago().name() );
        ventaResponse.estado( venta.getEstado().name() );

        return ventaResponse.build();
    }

    @Override
    public VentaResponse.VentaItemResponse toItemResponse(VentaItem item) {
        if ( item == null ) {
            return null;
        }

        VentaResponse.VentaItemResponse.VentaItemResponseBuilder ventaItemResponse = VentaResponse.VentaItemResponse.builder();

        ventaItemResponse.id( item.getId() );
        ventaItemResponse.itemId( item.getItemId() );
        ventaItemResponse.nombre( item.getNombre() );
        ventaItemResponse.precio( item.getPrecio() );
        ventaItemResponse.cantidad( item.getCantidad() );
        ventaItemResponse.subtotal( item.getSubtotal() );

        ventaItemResponse.tipo( item.getTipo().name() );

        return ventaItemResponse.build();
    }

    protected List<VentaResponse.VentaItemResponse> ventaItemListToVentaItemResponseList(List<VentaItem> list) {
        if ( list == null ) {
            return null;
        }

        List<VentaResponse.VentaItemResponse> list1 = new ArrayList<VentaResponse.VentaItemResponse>( list.size() );
        for ( VentaItem ventaItem : list ) {
            list1.add( toItemResponse( ventaItem ) );
        }

        return list1;
    }
}
