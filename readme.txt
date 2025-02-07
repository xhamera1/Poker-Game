Opis projektu
Autor: Patryk Chamera
Projekt "Poker-Game" to gra karciana typu poker 5-kartowy dobierany, napisana w języku Java w ramach projektu na przedmiot Programowanie Zaawansowane 2 na studiach. Gra symuluje klasyczną grę w pokera z mechanizmem zarządzania graczami, talią kart oraz stanem gry. Projekt zawiera wszystkie kluczowe elementy takie jak: dodawanie graczy, zarządzanie stawkami, rozdawanie kart, a także obsługę takich pojęć jak mały i duży blind, pulę oraz porównywanie rąk.

Zasady gry
Dodawanie graczy: Gra pozwala na dodanie od 2 do 4 graczy. Gracze mogą dołączyć do gry, podając swoje unikalne ID.

Blindy: Gra wprowadza dwa rodzaje blindów – mały i duży. Stawki wynoszą odpowiednio 20 i 40 jednostek.

Przygotowanie do gry: Po dołączeniu do gry, gracz może wybrać opcję "READY", aby zasygnalizować gotowość do rozpoczęcia rozgrywki.

Talia kart: Talia kart jest tasowana, a następnie każdy gracz otrzymuje dwie karty. Gra odbywa się na stole z 5 wspólnymi kartami.

Akcje graczy: Gracze mogą podejmować różne akcje, takie jak:

Fold – spasowanie ręki.
Call – dorównanie stawki.
Raise – podbicie stawki.
Check – sprawdzenie bez podbijania.
Exchange – wymiana kart.
Leave – opuszczenie gry.
Porównywanie rąk: Po zakończeniu rundy, gracze porównują swoje ręce, a ten z najlepszą ręką wygrywa pulę.

Protokół komunikacyjny
Komunikaty wysyłane przez serwer
Serwer obsługuje różne komendy przychodzące od klienta. Poniżej przedstawione są dostępne komendy oraz ich opis:

GAME_ID PLAYER_ID CREATE
Tworzy nową grę. Parametry: ID gry, ID gracza.

GAME_ID PLAYER_ID JOIN amount
Gracz dołącza do istniejącej gry. Parametry: ID gry, ID gracza, kwota dołączenia.

GAME_ID PLAYER_ID READY
Gracz oznacza, że jest gotowy do gry. Parametry: ID gry, ID gracza.

GAME_ID PLAYER_ID FOLD
Gracz pasuje rękę. Parametry: ID gry, ID gracza.

GAME_ID PLAYER_ID CALL
Gracz sprawdza aktualną stawkę. Parametry: ID gry, ID gracza.

GAME_ID PLAYER_ID CHECK
Gracz sprawdza, nie podbijając stawki ani nie pasując. Parametry: ID gry, ID gracza.

GAME_ID PLAYER_ID RAISE raiseValue
Gracz podbija stawkę o określoną wartość. Parametry: ID gry, ID gracza, wartość podbicia.

GAME_ID PLAYER_ID EXCHANGE 2,3
Gracz wymienia określone karty. Parametry: ID gry, ID gracza, numery kart do wymiany.

GAME_ID PLAYER_ID STATUS
Gracz żąda statusu gry. Parametry: ID gry, ID gracza.

GAME_ID PLAYER_ID LEAVE
Gracz opuszcza grę. Parametry: ID gry, ID gracza.

Struktura projektu
Projekt jest podzielony na cztery główne moduły:

poker-server
Opis: Moduł serwera gry. Zawiera logikę odpowiedzialną za obsługę komunikacji z klientami, zarządzanie stanem gry, przyjmowanie komend i wysyłanie odpowiedzi. Jest budowany do pliku JAR wykonywalnego z zależnościami.

poker-client
Opis: Moduł klienta tekstowego gry. Umożliwia graczom interakcję z grą poprzez konsolę, wysyłanie komend do serwera oraz odbieranie wyników i informacji o stanie gry. Moduł jest budowany do pliku JAR wykonywalnego z zależnościami.

poker-model
Opis: Moduł zawierający logikę biznesową rozgrywki. W tym module znajdują się wszystkie klasy odpowiedzialne za obsługę gry, takie jak: gracze, talia kart, porównywanie rąk, zarządzanie stawkami, itp. Został podzielony na odpowiednie pakiety, co pozwala na łatwą rozbudowę i utrzymanie.

poker-common
Opis: Moduł zawierający klasy pomocnicze, które są wykorzystywane przez pozostałe moduły. Zawiera wspólne definicje, takie jak klasy reprezentujące komunikaty wysyłane przez klienta i serwer, struktury danych oraz inne elementy wspólne dla całego projektu.

Jakość kodu
Projekt przeszedł przez SonarQube Quality Gate z pozytywnym wynikiem. Zostały uwzględnione następujące aspekty:

Pokrycie kodu testami: Pokrycie kodu testami jednostkowymi jest odpowiednie, a testy zostały pomyślnie wykonane.
Brak błędów krytycznych: Projekt nie zawiera błędów krytycznych ani wysokiego ryzyka.
Optymalizacja kodu: Kod został zoptymalizowany pod kątem wydajności i czytelności.