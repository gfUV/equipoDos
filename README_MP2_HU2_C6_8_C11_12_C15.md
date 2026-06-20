# Miniproyecto II - HU2 Login y Registro parcial

Esta versión conserva lo ya implementado anteriormente:

- HU1 criterio 4: Splash navega a Login y Registro luego de 5 segundos.
- HU2 criterios 1 al 5: pantalla negra, título, Email, Password con ícono y validación mínima.

## Nuevos criterios implementados

- HU2 criterio 6: ícono de ojo alterna entre mostrar y ocultar la contraseña.
- HU2 criterio 7: botón Login naranja con bordes redondeados, inactivo si Email o Password están vacíos.
- HU2 criterio 8: botón Login habilitado cuando Email y Password tienen datos, con texto blanco en negrita.
- HU2 criterio 11: texto Registrarse ubicado en la parte inferior, gris e inactivo si faltan datos.
- HU2 criterio 12: texto Registrarse habilitado cuando Email y Password tienen datos, color blanco en negrita.
- HU2 criterio 15: ondas blancas inferiores creadas con drawable vectorial XML, sin usar imagen PNG/JPG.

## Archivos principales modificados

- app/src/main/java/com/wt2dadmuvy/spinbot/view/auth/LoginFragment.kt
- app/src/main/java/com/wt2dadmuvy/spinbot/viewmodel/LoginViewModel.kt
- app/src/main/res/layout/fragment_login.xml
- app/src/main/res/drawable/bg_login_bottom_waves.xml
- app/src/main/res/drawable/ic_visibility_closed.xml
- app/src/main/res/values/colors.xml
- app/src/main/res/values/strings.xml

## Pendiente para próximos criterios

No se implementaron todavía Firebase Authentication, acciones reales de Login/Registro ni navegación por autenticación exitosa, porque corresponden a criterios posteriores.
