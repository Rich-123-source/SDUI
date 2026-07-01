# SDUI-Backend : Bibliothèque avec Interface Pilotée par le Serveur

Bienvenue sur le projet de backend pour une application de gestion de bibliothèque. Ce projet a la particularité d'implémenter une architecture **Server-Driven UI (SDUI)**, permettant de contrôler dynamiquement l'interface utilisateur d'une application cliente (Mobile ou Web) directement depuis le serveur.

Ce README vous guidera pour l'installation, la configuration et l'exploration du projet.

---

## ➤ Table des matières

1.  [Technologies Utilisées](#-technologies-utilisées)
2.  [Prérequis](#-prérequis)
3.  [Installation et Lancement](#-installation-et-lancement)
4.  [Explorer l'API](#-explorer-lapi)
    *   [API de Données (CRUD) avec Swagger](#1-api-de-données-crud-avec-swagger)
    *   [API de Présentation (Server-Driven UI)](#2-api-de-présentation-server-driven-ui-sdui)
5.  [Architecture du Projet](#-architecture-du-projet)
6.  [Comprendre le Server-Driven UI](#-comprendre-le-server-driven-ui-sdui)

---

## ➤ Technologies Utilisées

*   **Langage** : Java 21
*   **Framework** : Spring Boot 3
*   **Base de données** : PostgreSQL
*   **Accès aux données** : Spring Data JPA / Hibernate
*   **Gestion des dépendances** : Maven
*   **Utilitaires** : Lombok
*   **Documentation API** : SpringDoc (Swagger UI)

---

## ➤ Prérequis

Avant de commencer, assurez-vous d'avoir installé les outils suivants sur votre machine :
*   [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) (ou une version compatible)
*   [Apache Maven](https://maven.apache.org/download.cgi)
*   [PostgreSQL](https://www.postgresql.org/download/) (une instance locale ou distante)

---

## ➤ Installation et Lancement

Suivez ces étapes pour lancer le projet localement.

### 1. Cloner le Dépôt
```bash
git clone <URL_DU_DEPOT>
cd sdui
```

### 2. Configurer la Base de Données
Le projet est configuré pour se connecter à une base de données PostgreSQL.

1.  Créez une base de données dans PostgreSQL (par exemple, `sdui`).
2.  Ouvrez le fichier `src/main/resources/application.yaml`.
3.  Modifiez les valeurs de `datasource` pour correspondre à votre configuration locale :
    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/sdui       # Modifiez si nécessaire
        username: root                                # Modifiez votre nom d'utilisateur
        password: root                               # Modifiez votre mot de passe
    ```
4.  La ligne `ddl-auto: update` créera automatiquement les tables au premier démarrage.

### 3. Lancer l'Application
Utilisez Maven pour compiler et lancer le projet :
```bash
mvn spring-boot:run
```
Le serveur démarrera sur `http://localhost:8080`.

---

## ➤ Explorer l'API

Ce projet expose deux types d'API, qui ont des rôles très différents.

### 1. API de Données (CRUD) avec Swagger

Ces API sont les endpoints REST classiques pour créer, lire, mettre à jour et supprimer des données. Elles sont destinées à être utilisées par un back-office ou pour des actions de modification depuis le client.

Pour les explorer :
▶️ **Ouvrez votre navigateur et allez sur : http://localhost:8080/swagger-ui/index.html**

Vous y trouverez la documentation interactive pour les entités `Auteur`, `Livre`, `Utilisateur` et `Emprunt`. Vous pouvez tester chaque endpoint directement depuis cette interface.

### 2. API de Présentation (Server-Driven UI - SDUI)

Ces API ne renvoient pas de données brutes, mais une structure JSON qui décrit comment l'interface utilisateur doit être construite. C'est le cœur de notre architecture dynamique.

Pour les explorer, vous pouvez utiliser un client API (comme Postman) ou simplement votre navigateur en appelant les URL suivantes :

*   `GET http://localhost:8080/api/sdui/home` : Renvoie la structure de l'écran d'accueil.
*   `GET http://localhost:8080/api/sdui/livres` : Renvoie la liste des livres sous forme de composants UI.
*   `GET http://localhost:8080/api/sdui/auteurs/nouveau` : Renvoie un formulaire de création piloté par le serveur.
*   `GET http://localhost:8080/api/sdui/emprunts/nouveau` : Renvoie un formulaire complexe avec des listes déroulantes.

---

## ➤ Architecture du Projet

*   `com.numerum.sdui.entite` : Contient les classes Java représentant les tables de la base de données (JPA Entities).
*   `com.numerum.sdui.repository` : Interfaces Spring Data JPA pour l'accès à la base de données.
*   `com.numerum.sdui.service` : Contient la logique métier (ex: comment enregistrer un emprunt, quels livres sont disponibles).
*   `com.numerum.sdui.controller` :
    *   Contient les contrôleurs REST **classiques** (ex: `LivreController.java`) qui exposent les API CRUD.
    *   Contient le **`SduiController.java`**, qui gère toutes les API de présentation.
*   `com.numerum.sdui.composant` : Les modèles Java (`Screen`, `Component`, `Action`) qui définissent la structure de notre framework SDUI.
*   `com.numerum.sdui.config` : Fichiers de configuration Spring (ex: `OpenApiConfig.java`).

---

## ➤ Comprendre le Server-Driven UI (SDUI)

L'idée principale est de séparer la **logique de présentation** de la **logique de données**.

1.  **Le client demande "Quoi afficher ?"**
    Le front-end (une application mobile, par exemple) appelle un endpoint SDUI comme `/api/sdui/livres`.

2.  **Le serveur répond avec un plan de construction**
    Le serveur ne renvoie pas un simple JSON de données, mais une description de l'interface :
    ```json
    {
      "screenId": "livres_list",
      "title": "Catalogue des livres",
      "components": [
        {
          "type": "CARD",
          "properties": { "title": "Le Seigneur des Anneaux", "subtitle": "Par J.R.R. Tolkien" },
          "children": [
            {
              "type": "BUTTON",
              "properties": { "text": "Supprimer" },
              "action": {
                "type": "API_CALL",
                "destination": "/api/livres/1", // 🔴 Le SDUI dit au client d'appeler le CRUD
                "parameters": { "method": "DELETE" }
              }
            }
          ]
        }
      ]
    }
    ```

3.  **Le client exécute le plan**
    *   Le client lit ce JSON et dessine les composants : une `CARD` avec un `BUTTON`.
    *   Il ne connaît rien à la logique de suppression. Il sait juste que s'il clique sur le bouton, il doit faire un appel `DELETE` à l'URL `/api/livres/1` que le serveur lui a fournie.

Ce découplage permet de modifier l'apparence et le comportement de l'application cliente sans jamais avoir à la redéployer sur les stores. Tout est contrôlé depuis le backend !

---
N'hésitez pas à explorer, modifier et améliorer ce projet !
