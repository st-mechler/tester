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
Priorisierte Spaltenauswahl mit einfacher Taktik:
1. Sofortigen Gewinnzug spielen
2. Sofortigen gegnerischen Gewinnzug blocken
3. Naechste verfuegbare Spalte nach Prioritaet waehlen

## UI-Verhalten
- Klick auf Spalte setzt Stein in die naechstfreie Zeile
- Statusleiste zeigt aktuellen Zustand (am Zug, Sieg, Unentschieden)
- Reset startet ein neues Spiel mit leerem Brett
