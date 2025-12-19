# 🚀 Java Custom HTTP Server

A lightweight web server built from scratch using native Java (`ServerSocket`). This project demonstrates a low-level implementation of the HTTP protocol, capable of serving static content and processing dynamic form data.

## 🌟 Features

- **Built from Scratch:** Manual implementation of the HTTP Request/Response cycle.
- **Static File Serving:** Automatically serves `index.html` and `style.css` from the resources folder.
- **Dynamic POST Handling:** Processes form data sent to the `/submit` endpoint.
- **Custom Template Engine:** Uses a placeholder system (`{{variable}}`) to inject user data into a dynamic response page (`result.html`).
- **Resource Management:** Utilizes `ClassLoader` to ensure cross-platform compatibility when loading project files.
- **URL Decoding:** Handles special characters and spaces from web forms for clean data display.

## 🛠️ Project Structure

```text
HTTPServer/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/whitesnake/httpserver/HTTPServer.java  <-- Server Logic
│       └── resources/
│           ├── index.html   <-- Homepage (Input Form)
│           ├── style.css    <-- Project Styling
│           └── result.html  <-- Dynamic Response Template
└── pom.xml
