package com.barberia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BarberiaApplication {

    // Sin esto, @CreationTimestamp/@UpdateTimestamp de Hibernate y cualquier
    // LocalDateTime.now() del código usan la zona horaria del sistema
    // operativo del servidor -- si esa maquina corre en UTC (comun en VMs/
    // servidores sin configurar), cada fecha se guarda ~5 horas adelantada
    // respecto a la hora real de Lima, apareciendo ventas de la tarde/noche
    // como si fueran de la madrugada del dia siguiente. El "serverTimezone"
    // de la URL JDBC no alcanza para esto -- solo aplica a columnas
    // TIMESTAMP con zona, no a LocalDateTime (columnas DATETIME) generado
    // del lado Java.
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BarberiaApplication.class, args);
    }
}
