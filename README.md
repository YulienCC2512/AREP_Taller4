
# Taller de de modularización con virtualización e Introducción a Docker

## Descripción del Proyecto

Este proyecto es una aplicación web Java basada en Spring Boot que expone un servicio
REST simple. La aplicación responde a peticiones HTTP en el endpoint /greeting con un mensaje de
saludo personalizado. El proyecto está empaquetado usando Maven y se despliega dentro de un contenedor Docker, lo que permite ejecutarlo 
fácilmente en cualquier entorno, incluyendo instancias de AWS EC2.


---

## Instrucciones 

### Requisitos
- Java 
- Git
- Navegador web
- Maven (opcional, para manejar dependencias)

---

### Instalación
1. **Clonar el repositorio**
```bash
git https://github.com/YulienCC2512/AREP_Taller4.git
cd Taller4_AREP
```
 ### Funcionamiento


Servir archivos estáticos

Abre en tu navegador:
```bash
http://localhost:35000/
```
Esto cargará el archivo www/index.html.

Ejemplos:
```bash
http://localhost:35000/style.css 
http://localhost:35000/image.png 
```

---

### Despliegue en una Instancia de AWS EC2

Utilizando coandos de maven verificamos que este funcionando correctamente: 
```
mvn clean package
```

![](images/compilar.png)

Utilizamos un comando par apasar las dependencias y crear el archivo .jar
![](images/compilar2.png)



Nos coneectamos utlizando la consola de git bash;
![](images/conexion.png)

Dentro de git bash posteriro a al actualizacion de paquetes iniciamos con la generacion de la imagend docker:
![](images/craecion.png)

Luego de correr la imagen y verificar los archivos observamos el funcionamiento y la instancia corriendo:
![](images.funcionamiento.png)

Abrimos el navegador y verificamos el funcionamiento:
![](images/front.png)


Constrido con:
- Java Sockets – para la comunicación de red

- Maven – para la gestión del proyecto

- IntelliJ IDEA 

---

## Autor
- Julian Santiago Cardenas




    