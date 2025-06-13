# Entidad: PasswordResetToken



Entidad encargada de gestionar los tokens de recuperación de contraseña asociados a los usuarios del sistema.

### Descripción

La clase `PasswordResetToken` representa un token único que permite a un usuario restablecer su contraseña. Está vinculado a un usuario específico y tiene una fecha de expiración para asegurar su validez temporal. También registra si el token ya ha sido utilizado.

### Campos

| Campo        | Tipo              | Descripción                                                                 |
|--------------|-------------------|-----------------------------------------------------------------------------|
| `id`         | `UUID`            | Identificador único del token. Se genera automáticamente.                  |
| `token`      | `String`          | Cadena única que representa el token. Se utiliza como enlace seguro.       |
| `user`       | `User`            | Relación uno a uno con el usuario al que pertenece el token.               |
| `expiryDate` | `LocalDateTime`   | Fecha y hora límite de validez del token.                                  |
| `used`       | `boolean`         | Indica si el token ya fue utilizado para restablecer la contraseña.        |

### Relaciones

- **usuario**: Relación `@OneToOne` con la entidad `User`. Cada token está asociado a un único usuario.

### Reglas y restricciones

- El `token` debe ser único y no nulo.
- El `user` debe estar presente y correctamente vinculado.
- El `id` es generado automáticamente como UUID.
- Un token **puede ser usado solo una vez** (`usado = true` después de aplicarse).
- La **fecha de expiración** determina si el token aún es válido.


