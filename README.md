# Lost Time at Sea
A project created for SOFTENG 206 (Software Engineering Design 1) at The University of Auckland. Recieved 100% overall.

<img width="1034" alt="title" src="https://github.com/user-attachments/assets/f2fbc1ce-fe27-4b41-b7ae-57bddc39d06c">

## Developers
- Oorja Gandhi
- Mark Zhao
- Macy Butler 

## To setup the API to access Chat Completions and TTS

- add in the root of the project (i.e., the same level where `pom.xml` is located) a file named `apiproxy.config`
- put inside the credentials that you received from no-reply@digitaledu.ac.nz (put the quotes "")

  ```
  email: "UPI@aucklanduni.ac.nz"
  apiKey: "YOUR_KEY"
  ```
  These are your credentials to invoke the APIs. 


## To run the game

`./mvnw clean javafx:run`

## To debug the game

`./mvnw clean javafx:run@debug` then in VS Code "Run & Debug", then run "Debug JavaFX"

## To run codestyle

`./mvnw clean compile exec:java@style`
