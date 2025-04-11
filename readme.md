# Java Network Poker Game

## Overview

This project is a client-server implementation of the classic **Five-Card Draw Poker** game, developed in **Java**. It simulates a complete poker gameplay loop, handling player management, card dealing, betting rounds, hand evaluation, and network communication for multiplayer sessions. This project was originally developed as part of an academic advanced programming course.

## Features

* **Multiplayer Support:** Supports 2 to 4 players connecting remotely via a client-server architecture.
* **Game Variant:** Implements Five-Card Draw poker rules.
* **Core Gameplay Mechanics:**
    * Handles player joining/leaving with unique IDs and starting stacks.
    * Manages Small Blind (20 units) and Big Blind (40 units).
    * Facilitates standard poker actions: `FOLD`, `CALL`, `CHECK`, `RAISE`.
    * Includes a card `EXCHANGE` phase specific to Draw Poker.
    * Manages player readiness (`READY` state).
* **Betting & Pot Management:** Tracks bets, manages the pot size across betting rounds.
* **Hand Evaluation:** Automatically evaluates and compares player hands at showdown to determine the winner(s).
* **Networking:** Built on a client-server model for distributed gameplay.

## Architecture

The application follows a **client-server architecture**:

* **Server:** Manages the central game state, enforces game rules, handles player connections, and processes client commands.
* **Client:** Connects to the server, sends player actions, and receives game state updates for display (text-based console client provided).

## Communication Protocol

Communication uses a defined text-based protocol. Clients send commands prefixed with `GAME_ID` and `PLAYER_ID`.

**Client Commands:**

| Command                                | Description                                       |
| :------------------------------------- | :------------------------------------------------ |
| `GAME_ID PLAYER_ID CREATE`             | Creates a new game instance.                      |
| `GAME_ID PLAYER_ID JOIN amount`        | Joins an existing game with a starting `amount`.  |
| `GAME_ID PLAYER_ID READY`              | Signals the player is ready to start the game.    |
| `GAME_ID PLAYER_ID FOLD`               | Folds the current hand.                           |
| `GAME_ID PLAYER_ID CALL`               | Calls the current bet amount.                     |
| `GAME_ID PLAYER_ID CHECK`              | Checks (passes the turn if no bet is pending).    |
| `GAME_ID PLAYER_ID RAISE raiseValue`   | Raises the current bet by `raiseValue`.           |
| `GAME_ID PLAYER_ID EXCHANGE cardIndices` | Exchanges cards specified by `cardIndices` (e.g., `2,3`). |
| `GAME_ID PLAYER_ID STATUS`             | Requests the current game status.                 |
| `GAME_ID PLAYER_ID LEAVE`              | Leaves the current game.                          |

*(Server responses are implicitly handled to update client state)*

## Project Structure

The project is organized into four Maven modules:

1.  **`poker-server`**
    * Contains the server application logic.
    * Handles client connections, game state management, and rule enforcement.
    * Builds into an executable JAR.
2.  **`poker-client`**
    * Provides a text-based console client for interacting with the game server.
    * Sends commands and displays server responses.
    * Builds into an executable JAR.
3.  **`poker-model`**
    * Encapsulates the core game logic and domain models (e.g., `Player`, `Card`, `Deck`, `HandEvaluator`, betting rules).
    * Independent of network communication.
4.  **`poker-common`**
    * Contains shared classes, data structures, and constants used by other modules (e.g., command formats, utility classes).

## Documentation

Auto-generated Javadoc documentation for the project is available in HTML format within the repository:

javadoc/site/apidocs/index.html

## Running the Application

### 1. Start the Server

Navigate to the server module's target directory and run the JAR:

```sh
cd poker-server/target
java -jar poker-server-1.0-SNAPSHOT.jar
```

### 2. Start Client(s)

Navigate to the client module's target directory and run the JAR (repeat for each player):

```sh
cd poker-client/target
java -jar poker-client-1.0-SNAPSHOT.jar
```

## Code Quality

Code quality was assessed using SonarQube. The analysis confirmed:

* Adequate unit test coverage.
* Absence of critical or high-severity bugs.
* Code optimized for readability and maintainability according to standard practices.
