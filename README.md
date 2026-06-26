# SmartQueue

SmartQueue is a full-stack appointment booking and queue management system designed to streamline scheduling and reduce wait times.

## Features
- **Appointment Booking**: Users can easily book appointments through an intuitive web interface
- **Queue Management**: Real-time queue status tracking to help users plan their visits
- **Admin Dashboard**: Comprehensive admin panel for managing appointments and monitoring system activity
- **CORS Support**: Secure cross-origin requests handling for API communication

## Tech Stack
- **Backend**: Java with Servlets, Maven build system, and Docker containerization
- **Frontend**: HTML5, CSS3, and JavaScript for responsive user experience
- **Database**: Integrated database layer for persistent data storage

## Project Structure
```
├── backend/          # Java servlet-based REST API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/         # Web interface (HTML/CSS/JS)
│   ├── index.html
│   ├── admin.html
│   ├── script.js
│   └── styles.css
└── README.md
```

## Getting Started

### Backend
1. Navigate to the backend directory: `cd backend`
2. Build the project: `mvn clean install`
3. Deploy to a Java application server or run the Docker container

### Frontend
1. Navigate to the frontend directory
2. Open `index.html` in your web browser or serve through a web server

## Usage
- **User Portal**: Access the appointment booking system through the frontend
- **Admin Panel**: Use `admin.html` to manage appointments and monitor queue status

## Docker
Build and run the application in a container:
```bash
docker build -t smartqueue .
docker run -p 8080:8080 smartqueue
```

## Deployment
- `frontend/`: Deploy as static site on Render, Vercel, or GitHub Pages
- `backend/`: Deploy as web service on Render or similar cloud platform using Docker/Tomcat

## License
[Add your license information here]

## Contact
[Add contact or contribution information here]

