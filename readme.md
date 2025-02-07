# Poker-Game

## Autor: Patryk Chamera

Projekt **"Poker-Game"** to gra karciana typu **poker pięciokartowy dobierany**, napisana w języku **Java** w ramach projektu na przedmiot **Programowanie Zaawansowane 2** na studiach.

Gra symuluje klasyczną rozgrywkę w pokera, zawierając mechanizmy zarządzania graczami, talią kart oraz stanem gry. Projekt obejmuje kluczowe elementy, takie jak:
- dodawanie graczy,
- zarządzanie stawkami,
- rozdawanie i wymiana kart,
- obsługa małego i dużego blinda,
- pulę zakładów i porównywanie rąk.

---

## Zasady gry

### Dodawanie graczy
- Gra obsługuje **od 2 do 4 graczy**.
- Każdy gracz dołącza do gry, podając swoje **unikalne ID**.

### Blindy
- **Mały blind**: 20 jednostek.
- **Duży blind**: 40 jednostek.

### Przygotowanie do gry
- Po dołączeniu, gracze muszą oznaczyć gotowość do gry (`READY`).

### Rozdawanie kart
- Każdy gracz na początku otrzymuje **5 kart** z tasowanej talii.

### Akcje graczy
Gracze mogą wykonywać następujące akcje:
- **`FOLD`** – spasowanie ręki.
- **`CALL`** – wyrównanie aktualnej stawki.
- **`RAISE`** – podbicie stawki.
- **`CHECK`** – sprawdzenie bez podbijania.
- **`EXCHANGE`** – wymiana kart w fazie dobierania.
- **`LEAVE`** – opuszczenie gry.

### Porównywanie rąk
- Po zakończeniu rundy następuje **analiza układów**, a najlepsza ręka zgarnia pulę.

---

## Protokół komunikacyjny
Gra działa w architekturze **klient-serwer**, gdzie serwer obsługuje komendy wysyłane przez klientów.

### Komendy klienta

| **Komenda** | **Opis** |
|------------|---------|
| `GAME_ID PLAYER_ID CREATE` | Tworzy nową grę (ID gry, ID gracza). |
| `GAME_ID PLAYER_ID JOIN amount` | Dołączenie do istniejącej gry (ID gry, ID gracza, kwota wejściowa). |
| `GAME_ID PLAYER_ID READY` | Zasygnalizowanie gotowości do gry. |
| `GAME_ID PLAYER_ID FOLD` | Spasowanie ręki. |
| `GAME_ID PLAYER_ID CALL` | Wyrównanie aktualnej stawki. |
| `GAME_ID PLAYER_ID CHECK` | Sprawdzenie (bez podbijania). |
| `GAME_ID PLAYER_ID RAISE raiseValue` | Podbicie stawki o `raiseValue`. |
| `GAME_ID PLAYER_ID EXCHANGE 2,3` | Wymiana kart (numery kart do wymiany). |
| `GAME_ID PLAYER_ID STATUS` | Żądanie statusu gry. |
| `GAME_ID PLAYER_ID LEAVE` | Opuszczenie gry. |

---

## Struktura projektu

Projekt podzielony jest na **cztery moduły**:

### **1. poker-server** 
- Moduł serwera gry.
- Obsługuje komunikację z klientami i zarządza stanem gry.
- Budowany do pliku JAR wykonywalnego.

### **2. poker-client**  
- Tekstowy klient gry umożliwiający interakcję poprzez konsolę.
- Wysyła komendy do serwera i odbiera wyniki.
- Budowany do pliku JAR wykonywalnego.

### **3. poker-model**  
- Zawiera **logikę biznesową gry** – obsługa graczy, talii kart, porównywanie rąk, zarządzanie pulą.

### **4. poker-common** 
- Zawiera **wspólne klasy** wykorzystywane przez inne moduły, np. definicje komunikatów i struktury danych.

---

## 📖 Dokumentacja Javadoc
- Katalog **`javadoc`** zawiera automatycznie wygenerowaną **dokumentację projektu**, dostępną w formacie HTML.
- Dokumentacja obejmuje opisy klas, metod i interfejsów użytych w kodzie.
- Znajduje się w ścieżce:
  ```
  javadoc/site/apidocs/index.html
  ```
  Można ją otworzyć w przeglądarce internetowej.

---

## Uruchamianie gry

### Uruchomienie Serwera
```sh
cd poker-server/target
java -jar poker-server-1.0-SNAPSHOT.jar
```

### Uruchomienie Klienta
```sh
cd poker-client/target
java -jar poker-client-1.0-SNAPSHOT.jar
```

---

## Jakość kodu

Projekt został sprawdzony pod kątem **jakości kodu** przy użyciu **SonarQube**:
- **Pokrycie testami jednostkowymi** na odpowiednim poziomie.
- **Brak błędów krytycznych i wysokiego ryzyka**.
- **Optymalizacja kodu** pod kątem wydajności i czytelności.

---

## Podsumowanie
Projekt **"Poker-Game"** to **symulacja pokera pięciokartowego dobieranego**, działająca w architekturze **klient-serwer**. Obsługuje do **4 graczy** i implementuje pełny mechanizm **zakładów, wymiany kart i porównywania rąk**.

---

