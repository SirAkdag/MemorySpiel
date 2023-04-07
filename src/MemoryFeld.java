


//für die Klassen Arrays und Collections
import java.util.Arrays;
import java.util.Collections;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class MemoryFeld {
    //eine innere Klasse für den Eventhandler des Timer
    class TimerHandler implements EventHandler<ActionEvent> {
        @Override
        //die Methode ruft die Methode karteSchliessen() auf
        public void handle(ActionEvent arg0) {
            karteSchliessen();
        }
    }

    //das Array für die Karten
    private MemoryKarte[] karten;

    //das Array für die Namen der Grafiken
    private String[] bilder = {"grafiken/apfel.jpg", "grafiken/birne.jpg", "grafiken/blume.jpg", "grafiken/blume2.jpg",
            "grafiken/ente.jpg", "grafiken/fisch.jpg", "grafiken/fuchs.jpg", "grafiken/igel.jpg",
            "grafiken/kaenguruh.jpg", "grafiken/katze.jpg", "grafiken/kuh.jpg", "grafiken/maus1.jpg",
            "grafiken/maus2.jpg", "grafiken/maus3.jpg", "grafiken/melone.jpg", "grafiken/pilz.jpg",
            "grafiken/ronny.jpg", "grafiken/schmetterling.jpg","grafiken/sonne.jpg",
            "grafiken/wolke.jpg", "grafiken/maus4.jpg"};

    //für die Punkte
    private int menschPunkte, computerPunkte;
    //zwei Labels für die Punkte
    private Label menschPunkteLabel, computerPunkteLabel;

    //wie viele Karten sind aktuell umgedreht?
    private int umgedrehteKarten;

    //für das aktuell umdrehte Paar
    private MemoryKarte[] paar;

    //für den aktuellen Spieler
    private int spieler;

    //das "Gedächtnis" für den Computer
    //er speichert hier wo das Gegenstück liegt
    private int[][] gemerkteKarten;

    //für die Spielstärke
    private int spielstaerke;

    //für den Timer
    private Timeline timer;

    //der Konstruktor
    public MemoryFeld() {
        //das Array für die Karten erstellen, insgesamt 42 Stück
        karten = new MemoryKarte[42];

        //für das Paar
        paar = new MemoryKarte[2];

        //für das Gedächtnis
        //es speichert für jede Karte paarweise die Position im Spielfeld
        gemerkteKarten = new int[2][21];

        //keiner hat zu Beginn einen Punkt
        menschPunkte = 0;
        computerPunkte = 0;

        //es ist keine Karte umgedreht
        umgedrehteKarten = 0;

        //der Mensch fängt an
        spieler = 0;

        //die Spielstärke ist 10
        spielstaerke = 10;

        //es gibt keine gemerkten Karten
        for (int aussen = 0; aussen < 2; aussen++)
            for (int innen = 0; innen < 21; innen++)
                gemerkteKarten[aussen][innen] = -1;
    }

    //die Methode erstellt die Oberfläche und zeichnet die Karten über eine eigene Methode
    //übergeben wird ein FlowPane
    public FlowPane initGUI(FlowPane feld) {
        //für die Ausgaben
        kartenZeichnen(feld);
        menschPunkteLabel = new Label();
        computerPunkteLabel = new Label();
        menschPunkteLabel.setText(Integer.toString(menschPunkte));
        computerPunkteLabel.setText(Integer.toString(computerPunkte));

        //in zwei Spalten anzeigen
        GridPane tempGrid = new GridPane();
        //und einfügen, dabei werden die Koordinaten angegeben
        tempGrid.add(new Label("Mensch: "), 0 , 0 );
        tempGrid.add(menschPunkteLabel, 1, 0);
        tempGrid.add(new Label("Computer: "), 0, 1);
        tempGrid.add(computerPunkteLabel, 1 ,1);
        feld.getChildren().add(tempGrid);
        return feld;
    }

    //das eigentliche Spielfeld erstellen
    private void kartenZeichnen(FlowPane feld) {
        int count = 0;
        for (int i = 0; i <= 41; i++) {
            //eine neue Karte erzeugen
            karten[i] = new MemoryKarte(bilder[count], count, this);
            //bei jeder zweiten Karte kommt auch ein neues Bild
            if ((i + 1) % 2 == 0)
                count++;
        }
        //die Karten werden gemischt
        Collections.shuffle(Arrays.asList(karten));

        //und ins Spielfeld gesetzt
        for (int i = 0; i <= 41; i++) {
            feld.getChildren().add(karten[i]);
            //die Position der Karte setzen
            karten[i].setBildPos(i);
        }
    }

    //die Methode übernimmt die wesentliche Steuerung des Spiels
    //Sie wird beim Anklicken einer Karte ausgeführt
    public void karteOeffnen(MemoryKarte karte) {
        //zum Zwischenspeichern der ID und der Position
        int kartenID, kartenPos;

        //die Karten zwischenspeichern
        paar[umgedrehteKarten]=karte;

        //die ID und die Position beschaffen
        kartenID = karte.getBildID();
        kartenPos = karte.getBildPos();

        //die Karte in das Gedächtnis des Computers eintragen
        //aber nur dann, wenn es noch keinen Eintrag an der entsprechenden Stelle gibt
        if ((gemerkteKarten[0][kartenID] == -1))
            gemerkteKarten[0][kartenID] = kartenPos;
        else
            //wenn es schon einen Eintrag gibt
            //und der nicht mit der aktuellen Position übereinstimmt, dann haben wir die
            //zweite Karte gefunden
            //die wird dann in die zweite Dimension eingetragen
            if (gemerkteKarten[0][kartenID] != kartenPos)
                gemerkteKarten[1][kartenID] = kartenPos;
        //umgedrehte Karten erhöhen
        umgedrehteKarten++;

        //sind zwei Karten umgedreht worden?
        if (umgedrehteKarten == 2) {
            //dann prüfen wir, ob es ein Paar ist
            paarPruefen(kartenID);
            //den Timer erzeugen
            timer = new Timeline(new KeyFrame(Duration.millis(2000), new TimerHandler()));
            //und starten
            timer.play();
        }
        //haben wir zusammen 21 Paare, dann ist das Spiel vorbei
        if (computerPunkte + menschPunkte == 21) {
            //wir beenden es hier direkt
            Platform.exit();
        }
    }

    //die Methode dreht die Karten wieder auf die Rückseite
    //bzw. nimmt sie aus dem Spiel
    private void karteSchliessen() {
        boolean raus = false;
        //ist es ein Paar?
        if (paar[0].getBildID() == paar[1].getBildID())
            raus = true;
        //wenn es ein Paar war, nehmen wir die Karten aus dem Spiel
        //sonst drehen wir sie nur wieder um
        paar[0].rueckseiteZeigen(raus);
        paar[1].rueckseiteZeigen(raus);
        //es ist keine Karte mehr geöffnet
        umgedrehteKarten = 0;
        //hat der Spieler kein Paar gefunden?
        if (raus == false)
            //dann wird der Spieler gewechselt
            spielerWechseln();
        else
            //hat der Computer ein Paar gefunden?
            //dann ist er noch einmal an der Reihe
            if (spieler == 1)
                computerZug();
    }

    //die Methode prüft, ob ein Paar gefunden wurde
    private void paarPruefen(int kartenID) {
        if (paar[0].getBildID() == paar[1].getBildID()) {
            //die Punkte setzen
            paarGefunden();
            //die Karten aus dem Gedächtnis löschen
            gemerkteKarten[0][kartenID]=-2;
            gemerkteKarten[1][kartenID]=-2;
        }
    }

    //die Methode setzt die Punkte, wenn ein Paar gefunden wurde
    private void paarGefunden() {
        //spielt gerade der Mensch?
        if (spieler == 0) {
            menschPunkte++;
            menschPunkteLabel.setText(Integer.toString(menschPunkte));
        }
        else {
            computerPunkte++;
            computerPunkteLabel.setText(Integer.toString(computerPunkte));
        }
    }

    //die Methode wechselt den Spieler
    private void spielerWechseln() {
        //wenn der Mensch an der Reihe war,
        //kommt jetzt der Computer
        if (spieler == 0) {
            spieler = 1;
            computerZug();
        }
        else
            spieler = 0;
    }

    //die Methode setzt die Computerzüge um
    private void computerZug() {
        int kartenZaehler = 0;
        int zufall = 0;
        boolean treffer = false;
        //zur Steuerung über die Spielstärke
        if ((int)(Math.random() * spielstaerke) == 0) {
            //erst einmal nach einem Paar suchen
            //dazu durchsuchen wir das Array gemerkteKarten, bis wir in beiden Dimensionen
            //einen Wert finden
            while ((kartenZaehler < 21) && (treffer == false)) {
                //gibt es in beiden Dimensionen einen Wert größer oder gleich 0?
                if ((gemerkteKarten[0][kartenZaehler] >=0) &&  (gemerkteKarten[1][kartenZaehler] >=0)) {
                    //dann haben wir ein Paar
                    treffer = true;
                    //die Vorderseite der Karte zeigen
                    karten[gemerkteKarten[0][kartenZaehler]].vorderseiteZeigen();
                    //und dann die Karte öffnen
                    karteOeffnen(karten[gemerkteKarten[0][kartenZaehler]]);
                    //die zweite Karte auch
                    karten[gemerkteKarten[1][kartenZaehler]].vorderseiteZeigen();
                    karteOeffnen(karten[gemerkteKarten[1][kartenZaehler]]);
                }
                kartenZaehler++;
            }
        }
        //wenn wir kein Paar gefunden haben, drehen wir zufällig zwei Karten um
        if (treffer == false) {
            //solange eine Zufallszahl suchen, bis eine Karte gefunden wird, die noch im Spiel ist
            do {
                zufall = (int)(Math.random() * karten.length);
            } while (karten[zufall].isNochImSpiel() == false);
            //die erste Karte umdrehen
            //die Vorderseite der Karte zeigen
            karten[zufall].vorderseiteZeigen();
            //und dann die Karte öffnen
            karteOeffnen(karten[zufall]);

            //für die zweite Karte müssen wir außerdem prüfen, ob sie nicht gerade angezeigt wird
            do {
                zufall = (int)(Math.random() * karten.length);
            } while ((karten[zufall].isNochImSpiel() == false) || (karten[zufall].isUmgedreht() == true));
            //und die zweite Karte umdrehen
            karten[zufall].vorderseiteZeigen();
            karteOeffnen(karten[zufall]);
        }
    }

    //die Methode liefert, ob Züge des Menschen erlaubt sind
    //die Rückgabe ist false, wenn gerade der Computer zieht
    //oder wenn schon zwei Karten umgedreht sind
    //sonst ist die Rückgabe true
    public boolean zugErlaubt() {
        boolean erlaubt = true;
        //zieht der Computer?
        if (spieler == 1)
            erlaubt = false;
        //sind schon zwei Karten umdreht?
        if (umgedrehteKarten == 2)
            erlaubt = false;
        return erlaubt;
    }

}
