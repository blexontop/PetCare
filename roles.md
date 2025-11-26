Frederic: Creara los controllers principales, implementara los endpoints, configurara la API de email y ayuda en las pruebas basicas de funcionamiento y conexion con el frontend.

Tomás: Integra una API de mapas para mostrar veterinarios cercanos, implementa una funcion de exportacion (a CSV o PDF), crea el archivo data.sql y redacta el readme.

Héctor: Crea los repositorios para conectar con la base de datos, configura la autenticacion OAtuh, implementa los roles basicos sin complicar la seguridad y diseñara el dashboard del usuario con información simple.

Misael: Diseña las clases JPA (Mascota, Dueño, Cita, Veterinario), Añade relaciones simples, Integra la Dog/Cat API para mostrar imagenes de mascotas, colabora en las pruebas de los datos que llegan al dashborad, diseña el frontend, e implementa el OAUTH de GitHub.


## 🧑‍💻 Frederic (Controllers y API de Email)
Frederic se encargará de la lógica principal de la aplicación, manejando las peticiones y respuestas HTTP, y la integración del servicio de email.

Controllers Principales y Endpoints:

src\main\java\com\petcare\petcare\controller\MainController.java: El controlador principal para manejar las rutas base.

src\main\java\com\petcare\petcare\controller\MascotaController.java: Controlador para gestionar las operaciones relacionadas con las mascotas.

src\main\java\com\petcare\petcare\controller\AdminController.java: Controlador para rutas y lógica de administración.

Configuración de API de Email:

src\main\java\com\petcare\petcare\service\EmailService.java: Clase de servicio que contendrá la lógica de envío de correos.

src\main\java\com\petcare\petcare\controller\EmailApiController.java: Controlador API para endpoints específicos de email (si aplica).

Archivos de Vista y Pruebas Básicas:

src\main\resources\templates\home.html: La página de inicio o principal.

src\main\resources\templates\error.html: Gestión de errores (útil para pruebas de conexión).


## 🗺️ Tomás (API de Mapas, Exportación y Documentación)
Tomás se enfocará en características de utilidad, integración con servicios externos (mapas) y la documentación esencial del proyecto.

Integración de API de Mapas:

src\main\java\com\petcare\petcare\api\ApiMapa.java: Componente para interactuar con la API de mapas.

src\main\java\com\petcare\petcare\service\MapaService.java: Servicio que gestiona la lógica de los mapas (ej. buscar veterinarios cercanos).

src\main\resources\templates\mapa.html: La vista HTML donde se mostrará el mapa.

Función de Exportación:

src\main\java\com\petcare\petcare\controller\ExportMascotaController.java: Controlador para manejar la petición de exportación.

src\main\java\com\petcare\petcare\service\ExportMascotaService.java: Servicio con la lógica para generar el archivo exportado (CSV/PDF).

Archivos de Datos y Documentación:

petcare.sql: Archivo SQL (se asume que es el data.sql mencionado).

info.md o TODO.md: El README se podría alojar en uno de estos, pero el archivo estándar es README.md (no visible, pero se le asigna la tarea de redactarlo).


## 🔒 Héctor (Seguridad, Repositorios y Dashboard)
Héctor es el responsable de la persistencia de datos (conexión a la DB) y la capa de seguridad, además del diseño inicial de la interfaz de usuario clave.

Repositorios (Conexión a DB):

Los repositorios son un paquete que falta en la vista (generalmente repository), pero la tarea implica la creación de las interfaces de Spring Data JPA. Se le asigna la responsabilidad de crear el paquete repository y sus clases (ej. DueñoRepository.java, MascotaRepository.java).

Configuración de Autenticación y Roles:

src\main\java\com\petcare\petcare\security\SecurityConfig.java: Configuración principal de Spring Security, incluyendo OAuth.

src\main\java\com\petcare\petcare\security\GuestOrAuthenticatedAuthorization...: Clase de configuración de autorización.

src\main\java\com\petcare\petcare\security\CustomUserDetailsService.java: Lógica para cargar los detalles del usuario (roles básicos).

src\main\java\com\petcare\petcare\security\CustomAuthenticationSuccessHandle...: Manejador de éxito de autenticación.

src\main\java\com\petcare\petcare\security\GuestFilter.java: Manejo de usuarios anónimos o invitados.

Diseño del Dashboard:

src\main\resources\templates\dashboard.html: La vista principal del usuario.


## 🐶 Misael (Modelo, API de Mascotas y Frontend General)
Misael define la estructura de datos central, integra la API externa de imágenes de mascotas y es el principal diseñador del frontend.

Clases JPA (Modelo de Datos):

src\main\java\com\petcare\petcare\model\Mascota.java

src\main\java\com\petcare\petcare\model\Dueño.java

src\main\java\com\petcare\petcare\model\Cita.java

src\main\java\com\petcare\petcare\model\Veterinario.java

src\main\java\com\petcare\petcare\model\EstadoCita.java

src\main\java\com\petcare\petcare\model\User.java (Para el usuario en el modelo)

src\main\java\com\petcare\petcare\model\AuthProvider.java (Relacionado con OAUTH).

Integración de Dog/Cat API:

src\main\java\com\petcare\petcare\service\DogCatApiService.java: Servicio para llamar a la API externa de imágenes/datos de perros y gatos.

src\main\java\com\petcare\petcare\api\ApiMascotaController.java: Controlador API para exponer las imágenes/datos de la API.

Frontend (Diseño y OAUTH de GitHub):

Vistas Principales y Formularios:

src\main\resources\templates\login.html: Implementación del OAUTH de GitHub y login.

src\main\resources\templates\registration.html

src\main\resources\templates\mascotas.html

src\main\resources\templates\mascota-detalle.html

src\main\resources\templates\mascota-form.html

src\main\resources\templates\dueno-form.html

src\main\resources\templates\admin.html

Fragmentos y Estilos:

src\main\resources\templates\fragments\footer.html

src\main\resources\templates\fragments\header.html

src\main\resources\static\css\styles.css

src\main\resources\static\favicon.svg