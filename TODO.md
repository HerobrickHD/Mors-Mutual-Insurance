# 🧾 Mors Mutual Insurance – TODO / Ideenliste

> Übersicht aller noch geplanten oder möglichen Features, Verbesserungen und Fixes  
> (HerobrickHD & Team – laufend erweitert)

---

## 🔧 CORE / FUNKTIONALITÄT

- [ ] **Config-Option für Rückkauf bei vollem Inventar**
  - `require-empty-inventory: true` → Spieler muss komplett leeres Inventar haben.
  - `drop-current-inventory: true` → aktuelles Inventar wird beim Rückkauf droppped.
  - verhindert Exploits (z. B. „Inventar in Kiste legen → Versicherung missbrauchen“).

- [ ] **Preis-Balancing verbessern**
  - smartere Gewichtung (Armor, Tools, Rüstung teurer).
  - ggf. dynamische Kosten je nach Rückkauf-Häufigkeit.
  - Mindest- und Höchstpreisgrenzen (z. B. 3–64 Smaragde).

- [ ] **Soundeffekte**
  - Erfolgreicher Rückkauf → kurzer Erfolgston (`UI_TOAST_CHALLENGE_COMPLETE`).
  - Fehlgeschlagener Rückkauf → Villager ablehnend (`ENTITY_VILLAGER_NO`).
  - Händler-Flüstern → leiser „villager whisper“-Sound.

- [ ] **GUI-Verbesserungen**
  - Freie Slots in GUI einfärben (hell/dunkelblaues Glas für Terminal-Optik).
  - Bestätigungs-GUI visuell aufwerten (animierte Items?).
  - "Nicht genug Smaragde" mit grauer statt grüner Wolle darstellen.

---

## 🛠️ ADMIN-FUNKTIONEN

- [ ] **/mmi list \<spieler\>**
  - Zeigt alle gespeicherten Tode eines Spielers (Datum, Level, Preis, etc.)

- [ ] **/mmi clear \<spieler\>**
  - Löscht den gesamten Versicherungsverlauf des Spielers (aber nicht die „Free Used“-Flag).

- [ ] **/mmi resetfree \<spieler\>**
  - Setzt die Gratis-Versicherung des Spielers zurück.

- [ ] **Logging**
  - In Konsole: Wer kauft was zurück?
  - Optional: `logs.yml` mit Verlauf (Spieler, Zeit, Kosten, Level, etc.)

---

## 🌍 UX / IMMERSION

- [ ] **Externe Sprachdatei**
  - Alle Nachrichten in `messages.yml` auslagern (mehrsprachige Unterstützung).
  - Struktur wie:
    ```yml
    messages:
      restored: "&aDein Inventar wurde wiederhergestellt."
      not_enough: "&cDu hast nicht genug Smaragde."
    ```
  - Nutzen von Adventure MiniMessage (Formatierung & Farben).

- [ ] **NPC-Verhalten**
  - Händler könnte beim Öffnen kurz Partikel oder Emotes zeigen.
  - Später evtl. Custom Name: „§bVersicherungsagent“.

---

## 🧠 EXPLOIT-PREVENTION / BALANCING

- [ ] **Inventar-Exploit verhindern**
  - Prüfen, ob Spieler nach dem Tod wieder Items eingesammelt hat.
  - Idee: Beim Speichern jedes DeathSnapshots alle Items hashen.
  - Beim Rückkauf → prüfen, ob der Spieler dieselben Items im Besitz hat.
  - Falls ja: Warnung oder Preisaufschlag.

- [ ] **Zeitlimit auf Rückkauf**
  - z. B. nur die letzten 10 Minuten / 1 Stunde nach Tod rückkaufbar.

- [ ] **Cooldown zwischen Rückkäufen**
  - Spieler kann nur alle X Minuten einen Rückkauf tätigen.

---

## 🧰 TECHNIK & STABILITÄT

- [ ] **Refactoring: Config / Messages Manager**
  - Eigene Klasse `MessageManager` und `ConfigManager` zur sauberen Trennung.

- [ ] **Optionale Vault-Unterstützung**
  - `payment-mode: EMERALD | VAULT`
  - Falls VAULT aktiv → zieht Geld aus Economy-Plugin statt Smaragden.

- [ ] **Datenkompression**
  - Snapshot-Dateien ggf. in `GZIP` oder `base64` speichern, um Platz zu sparen.

- [ ] **Update-Befehl**
  - `/mmi reload` → lädt Config + Messages ohne Neustart.

---

## 🎨 IDEEN FÜR SPÄTER

- [ ] Händler-Erkennung per Nametag („Versicherungsagent“) statt alle Wandering Traders.
- [ ] Custom Models / Resource Pack → Versicherungs-Terminal mit GUI-Symbolen.
- [ ] Statistik-Command: `/mmi stats` → zeigt, wie viele Spieler ihre Versicherung genutzt haben.
- [ ] Fortschritt-System → z. B. „5x versichert“ = Titel "Dauerpleite".
- [ ] Integration mit DeathLogs / CoreProtect → Inventarprüfung gegen Server-Logs.

---

**Letztes Update:** {{DATUM HIER EINFÜGEN}}
