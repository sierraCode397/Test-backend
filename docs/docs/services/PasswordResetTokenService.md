# Service: PasswordResetTokenService



Servicio encargado de gestionar la creación, almacenamiento y validación de tokens de recuperación de contraseña y códigos de verificación temporales.

---

## 📦 Métodos

### 🔹 `createResetTokenForUser(usuario: User, token: String): PasswordResetToken`

Crea y guarda un nuevo token de recuperación de contraseña para el usuario especificado.

#### Parámetros

| Nombre  | Tipo   | Descripción                                                     |
|---------|--------|-----------------------------------------------------------------|
| `user`  | `User` | Usuario al que se le asignará el token de recuperación.         |
| `token` | `String` | Token generado (usualmente UUID) que identifica la solicitud. |

#### Proceso

1. Se construye una instancia de `PasswordResetToken` con:
    - El token recibido.
    - El usuario asociado.
    - Fecha de expiración a 15 minutos desde la creación.
2. Se imprime el token por consola (debug).
3. Se guarda el token en la base de datos.

#### 🔁 Retorno

Devuelve la instancia guardada de `PasswordResetToken`.

---

### 🔹 `saveVerificationCode(usuario: User, codigoVerificacion: String): void`

Guarda un código de verificación (por ejemplo, un número de 6 dígitos) asociado a un usuario como un token de recuperación.

#### Parámetros

| Nombre             | Tipo     | Descripción                                                       |
|--------------------|----------|-------------------------------------------------------------------|
| `user`             | `User`   | Usuario al que se le asigna el código de verificación.            |
| `verificationCode` | `String` | Código que se enviará al correo para verificar la identidad.

#### Proceso

1. Se construye una instancia de `PasswordResetToken` con:
    - El código de verificación como valor del token.
    - El usuario asociado.
    - Una fecha de expiración fijada a 15 minutos desde la creación.
2. Se almacena el token en la base de datos para su posterior validación.

#### 🔁 Retorno

Este método no retorna valor (`void`), pero guarda el token en la base de datos para su uso en la verificación del usuario.

---

## 📌 Resumen de responsabilidades

La clase `PasswordResetTokenService` encapsula la lógica para:

- Generar tokens únicos o códigos temporales de verificación para recuperación de contraseña.
- Asociar dichos tokens a un usuario específico.
- Definir un tiempo de expiración para cada token.
- Persistir esta información en la base de datos mediante el repositorio correspondiente.

---

## Ejemplo de uso

```java
// Crear token UUID para recuperación
String token = UUID.randomUUID().toString();
tokenService.createResetTokenForUser(usuario, token);

// Crear código de verificación de 6 dígitos
String codigo = String.valueOf(100000 + new SecureRandom().nextInt(900000));
tokenService.saveVerificationCode(usuario, codigo);