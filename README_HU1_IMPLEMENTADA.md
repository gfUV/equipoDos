# HU 1.0 - Ventana Splash implementada

Esta versión del proyecto SpinBot implementa la historia de usuario HU 1.0 del miniproyecto Pico Botella.

## Criterios implementados

1. Splash con fondo negro y sin toolbar de Android.
2. Ícono central en forma de botella con animación continua.
3. Texto naranja `pico botella` debajo del ícono.
4. Duración de 5 segundos antes de navegar al Home Principal.
5. El Splash se elimina del back stack con `popUpToInclusive`, por lo tanto desde el Home el botón atrás sale de la app y no vuelve al Splash.
6. Ícono personalizado de la app usando `@drawable/ic_app_icon`.

## Enfoque usado


- Kotlin.
- XML.
- Fragments.
- Navigation Component.
- Corrutina con `lifecycleScope` y `delay(5000L)` para controlar el tiempo del Splash.

## Archivos principales modificados o creados

- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/fragment_splash.xml`
- `app/src/main/res/layout/fragment_home.xml`
- `app/src/main/res/navigation/nav_graph.xml`
- `app/src/main/res/anim/splash_bottle_animation.xml`
- `app/src/main/res/drawable/ic_bottle_splash.xml`
- `app/src/main/res/drawable/ic_app_icon.xml`
- `app/src/main/java/com/wt2dadmuvy/spinbot/view/MainActivity.kt`
- `app/src/main/java/com/wt2dadmuvy/spinbot/view/splash/SplashFragment.kt`
- `app/src/main/java/com/wt2dadmuvy/spinbot/view/home/HomeFragment.kt`
- `app/src/main/AndroidManifest.xml`
