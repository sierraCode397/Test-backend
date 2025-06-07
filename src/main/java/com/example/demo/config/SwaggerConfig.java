package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration Swagger.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Primarket",
                version = "1.0.0",
                description = """
                Bienvenido a la documentación oficial de la API de Primarket.

                Aquí encontrarás la descripción detallada de los distintos endpoints disponibles,
                sus métodos, parámetros de entrada, formatos de respuesta y ejemplos de uso.

                Te recomendamos seguir las instrucciones y ejemplos proporcionados para garantizar
                una correcta implementación y aprovechamiento de los servicios."""
        )
)


public class SwaggerConfig {
}



