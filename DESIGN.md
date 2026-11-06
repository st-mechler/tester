# Design

## Produkt
AWT-basierter Vier-Gewinnt-Prototyp fuer Desktop.

## Kernfunktionen
- Spielfeld mit 7 Spalten und 6 Reihen
- Zwei Modi:
  - Zwei Spieler lokal
  - Spieler gegen Computer
- Sprachumschaltung Deutsch/Englisch
- Gewinnpruefung horizontal, vertikal und diagonal
- Unentschieden-Erkennung bei vollem Board
- Markierung der Gewinnlinie
- Dialoge fuer Spielmodus und Spielende

## Computerlogik
- Vor dem Computer-Spiel wird ein Schwierigkeitsgrad ausgewaehlt.
- Einfach waehlt zufaellig eine verfuegbare Spalte.
- Mittel spielt sofortige Gewinnzuege, blockt sofortige gegnerische Gewinnzuege und bevorzugt danach die Mitte.
- Schwer verwendet eine Minimax-Suche mit Alpha-Beta-Schnitt und einer Brettbewertung.

## UI-Verhalten
- Klick auf Spalte setzt Stein in die naechstfreie Zeile
- Statusleiste zeigt aktuellen Zustand (am Zug, Sieg, Unentschieden)
- Reset startet ein neues Spiel mit leerem Brett
