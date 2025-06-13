[//]: # (Structure project and how organize the code)

# 🏗️ Arquitectura del proyecto

Este proyecto está construido utilizando el framework **Spring Boot** siguiendo el patrón de arquitectura **MVC (Modelo-Vista-Controlador)**. A continuación, se detalla la estructura del proyecto y cómo se organiza el código:

## 📁 Estructura del Proyecto

```plaintext
src/main/java/com/miempresa/miproyecto/
├── config/ # Configuraciones generales (seguridad, CORS, Swagger, etc.)
├── constants/ # Constantes utilizadas en la aplicación
├── controller/ # Controladores que manejan las peticiones HTTP
├── dto/ # Clases de transferencia de datos (Data Transfer Objects)
├── entity/ # Entidades que representan las tablas de la base de datos
├── exception/ # Manejo de excepciones personalizadas
├── repository/ # Interfaces de repositorios para acceso a datos
├── service/ # Servicios que contienen la lógica de negocio
├── utils/ # Utilidades y funciones auxiliares
   
```

---
## 🧩 Capas del Proyecto

### 1. Controladores (`controller`)

- Reciben y responden a las peticiones HTTP.
- Validan las entradas y delegan la lógica de negocio a los servicios.
- Devuelven DTOs (Data Transfer Objects) como respuesta estructuradas.

### 2. Servicios (`service`)

- Contienen la lógica de negocio de la aplicación.
- Interactúan con los repositorios para acceder a los datos.
- Realizan validaciones y transformaciones de datos.
- Se encarga de aplicar las reglas, validaciones y flujos.

### 3. Repositorios (`repository`)

- USan Spring Data JPA para interactuar con la base de datos.
- Acceder directamente a la base de datos.




## 📌 Principales Dependencias
- **Spring Boot**: Framework principal para construir aplicaciones Java.
- **Spring Data JPA**: Facilita la interacción con bases de datos relacionales.
- **Spring Security**: Proporciona autenticación y autorización.
- **Lombok**: Reduce el boilerplate de código en las clases.
- **JWT (JSON Web Token)**: Para manejar la autenticación basada en tokens.
- **Cloudinary**: Para la gestión de imágenes y archivos multimedia.
- **auth0**: Para la autenticación y autorización de usuarios.


## 🔐 Seguridad

Para más detalles sobre cómo se configura la seguridad en la aplicación, consulta el documento [Security](../config/security.md).


## 📊 Flujo de  General

```plaintext
[ Cliente ] -> [ Controlador ] -> [ Servicio ] -> [ Repositorio ] -> [ Base de datos ]
```


