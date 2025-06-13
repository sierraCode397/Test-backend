# Autenticación  

## Recuperación de Contraseña
### POST `/auth/forgot-password`

Inicia el proceso de recuperación de contraseña para el usuario asociado al correo electrónico proporcionado.

### Descripción

Este método genera un token de recuperación y un código de verificación de 6 dígitos, luego los asocia al usuario y envía el código al correo electrónico registrado. Este proceso permite validar posteriormente el cambio de contraseña mediante el código enviado.

### Parámetros

| Nombre   | Tipo    | Requerido | Descripción                                                          | 
|----------|---------|-----------|----------------------------------------------------------------------|
| `email`  | String  | Sí        | Correo electrónico del usuario que solicita recuperar su contraseña. |

### Proceso

1. Verifica que el usuario exista mediante su correo electrónico.
2. Genera un token único (UUID) para recuperación.
3. Genera un código de verificación de 6 dígitos.
4. Guarda el token y el código asociados al usuario.
5. Envía el código al correo electrónico del usuario.

### Excepciones

| Tipo               | Condición                                                    |
|--------------------|--------------------------------------------------------------|
| `RuntimeException` | Si no se encuentra un usuario con el correo proporcionado.   |

### Retorno

Devuelve un `String` con un mensaje de confirmación indicando que el código ha sido enviado correctamente al correo.

### Ejemplo de uso (pseudocódigo)

```java
String mensaje = authService.forgotPassword("usuario@ejemplo.com");
// mensaje = "Se ha enviado un correo con el código de verificación para restablecer tu contraseña."
```
## Restablecer Contraseña
### POST `/auth/reset-password`
Restablece la contraseña de un usuario utilizando un token de recuperación previamente generado y enviado por correo electrónico.

### Descripción

Este endpoint permite a un usuario actualizar su contraseña siempre y cuando el token recibido sea válido, no esté expirado y no haya sido utilizado previamente. La nueva contraseña es encriptada antes de ser almacenada en la base de datos.

### Parámetros
| Parámetro        | Tipo   | Requerido | Descripción                                                  |
|------------------|--------|-----------|--------------------------------------------------------------|
| `code`           | String | Sí        | Token único enviado por correo para validar la recuperación. |
| `newPassword`    | String | Sí        | Nueva contraseña que se asignará al usuario.                 |
| `repeatPassowrd` | String | Sí        | Repetición de la contraseña nueva del usuario                |

### Proceso

1. Verifica si el token existe en la base de datos.
2. Valida que el token no haya sido usado previamente.
3. Valida que el token no haya expirado.
4. Verifica que la nueva contraseña no esté vacía.
5. Encripta la contraseña usando `PasswordEncoder`.
6. Actualiza la contraseña del usuario.
7. Marca el token como usado.

### Excepciones

| Tipo                 | Condición                                              |
|----------------------|--------------------------------------------------------|
| `RuntimeException`   | Si el token no existe, ya fue utilizado o ha expirado. |
| `ResponseStatusException` | Si las contraseñas no coinciden                        |

### Retorno

Este método no retorna ningún valor (`void`). Si se ejecuta correctamente, la contraseña del usuario queda actualizada y el token marcado como utilizado.



