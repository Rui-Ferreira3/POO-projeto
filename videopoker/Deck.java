package videopoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Deck {


    private ArrayList<Card> deck;


    /**
	 * Constructor do Deck. Cria lista vazia
	 */
    public Deck(){
        this.deck= new ArrayList<Card>();
    }


    /**
	 * Repoe uma carta no final do baralho 
	 * @param card carta a ser reposta no baralho 
	 */
	public void add_card(Card card) {
		deck.add(card);
	}


    /**
	 * Tira uma carta do topo do deck
	 * @param card carta do topo do deck
	 */
	public Card remove_card(Card card) {

        Card carta= deck.remove(0);
        return carta;
	}


    /**
	 * Da shuffle ao deck
	 */
	public void shuffle() {
		Collections.shuffle(deck);
	}

    /**
	 * Cria um deck com 52 cartas 
	 */
    public void create_deck(){

        for (int suit = 0; suit <= 3; suit++) {
            for (int rank = 2; rank <= 13; rank++) {
                deck.add(new Card(rank, suit));
            }
        }
    }


    public void create_from_file() throws FileNotFoundException{

        File file= new File("/Users/ricar/eclipse-workspace/Projeto/card-file.txt");
        try (Scanner scan = new Scanner(file)) {
            int rank=1;
            char suit='C';

            String aux= scan.nextLine();
            String[] cartas = aux.split(" ");

            for(int i=0; i< cartas.length; i++){

                aux= cartas[i];

                if(aux.length() == 2){

                    for(int j=0; j < 15; j++){
                        if(Character.getNumericValue(aux.charAt(0)) == Ranks[j]){
                            rank= Ranks[j];
                            break;
                        }
                    }

                    for(int a=0; a < 4; a++){
                        if(aux.charAt(1) == Suits[a]){
                            suit= Suits[a];
                            break;
                        }
                    }

                    deck.add(new Card(rank, suit));
                }
                else{
                    rank= 10;

                    for(int a=0; a < 4; a++){
                        if(aux.charAt(1) == Suits[a]){
                            suit= Suits[a];
                            break;
                        }
                    }

                    deck.add(new Card(rank, suit));
                }

            }
        }


    }



}
