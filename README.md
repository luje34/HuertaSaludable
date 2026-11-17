# HuertaSaludable
Aplicación sobre gestión colaborativa del huerto escolar

## Conexión con la API local

La app se comunica con la API Spring Boot por HTTP; nunca se conecta directamente a MySQL. Para desarrollo con XAMPP, inicia MySQL y la API en el PC (puerto `8080`) siguiendo la guía del repositorio `APIHuertaSaludable`.

- **Emulador Android:** la URL predeterminada es `http://10.0.2.2:8080/`.
- **Dispositivo físico:** configura la IP LAN del PC, por ejemplo `http://192.168.1.23:8080/`, y permite conexiones entrantes al puerto `8080` en el firewall.

La URL se puede cambiar sin editar archivos versionados mediante la propiedad Gradle `huertaApiBaseUrl` o la variable de entorno `HUERTA_API_BASE_URL`. Por ejemplo, para instalar en un teléfono conectado a la misma red:

```powershell
.\gradlew.bat installDebug -PhuertaApiBaseUrl=http://192.168.1.23:8080/
```

También se puede definir `huertaApiBaseUrl` en `%USERPROFILE%\.gradle\gradle.properties` o `HUERTA_API_BASE_URL` en el entorno desde el que se inicia Android Studio. El manifiesto de `debug` permite HTTP claro para pruebas locales; las compilaciones de producción deben usar HTTPS.

La API es la fuente de verdad para autenticación, sectores, cultivos, tareas y reportes. Room conserva copias para la UI, pero las escrituras requieren conexión y solo se reflejan en la caché tras una respuesta exitosa; no hay una cola de escritura offline ni sincronización offline completa. Sensores e historial siguen siendo locales. Las URI de fotos/evidencias se envían como texto y no se suben al servidor, por lo que otro dispositivo no puede abrir esos archivos locales.
