package videopoker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
public class VideoPoker {
	protected int played;
	protected int wins;
	protected int last_bet;
	protected char last_command;
	protected Deck played_cards;
	protected List<Statistic> statistics;
	protected String advice;

	protected Deck deck;
	protected Player player;

	protected DoubleBonus variation;
	protected Rules rules;

	/**
	 * Construtor do VideoPoker
	 * @param deck
	 * @param player
	 * 
	 */
	public VideoPoker(Deck deck, Player player) {
		super();
		this.deck = deck;
		this.player = player;

		this.played = 0;
		this.wins = 0;
		this.last_bet = 5;
		this.played_cards = new Deck();
		this.statistics = new ArrayList<Statistic>();
		this.advice = new String();

		this.set_statistics();

		this.variation = new DoubleBonus();
		this.rules = new Rules();
	}
	/**
	 * Onde os comando são analisados e suas ações associadas são iniciadas
	 */
	public void play() {
		String command = new String();
		do {
			command = this.get_command();
			if (this instanceof Debug){
				System.out.println("Command: " + command);
			}

			char c = command.charAt(0);
			switch (c) {
				case 'b':
					System.out.println("-cmd: " + command);
					this.bet(command.split(" "));
					break;
				case '$':
					System.out.println("-cmd: " + command);
					this.credit();
					break;
				case 'd':
					System.out.println("-cmd: " + command);
					this.deal();
					break;
				case 'h':
					System.out.println("-cmd: " + command);
					this.hold(command.split(" "));
					break;
				case 'a':
					System.out.println("-cmd: " + command);
					this.advice();
					break;
				case's':
					System.out.println("-cmd: " + command);
					this.statistics();
					break;
				default:
					System.exit(0);
			}

			if (c == 'h' && this.last_command == 'd') {
				Deck ordered_hand = this.player.get_hand();
				ordered_hand.order_deck();
				String win = this.check_win(ordered_hand, this.last_bet);

				this.played++;
				this.update_statistics(win);
				this.reset_hand();
				this.reset_deck();
				this.last_command = 'h';
				System.out.println();
			}

		System.out.println();
		} while (!(command.equals("e")));
	}
	/**
	 * Para jogo iterativo, é pedido o comando no terminal para o utilisador poder jogar
	 * @return o comando dado pelo utilisador
	 */
	String get_command() {
		String command = new String();
		System.out.println("What's next command?");
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		try {
			command = stdin.readLine();
		} catch(IOException e) {
			System.err.println("Error in reading");
		}
		return command;
	}
	/**
	 * Verificação da aposta do jogador. Esta não pode ser realisada depois de um deal ou de um bet, não pode ser superior a 5 e não pode ser superior ao credito do jogador
	 * @param command
	 */
	void bet(String[] command) {
		if (this.last_command == 'b' || this.last_command == 'd') {
			System.out.println("b: illegal command");
			return;
		}

		Integer bet = this.last_bet;
		if (command.length>1) {
			try{
				bet = Integer.parseInt(String.valueOf(command[1]));
			}
			catch (NumberFormatException ex){
				ex.printStackTrace();
				return;
			}
			if (bet > 5)
				bet = 5;
			if (bet < 1)
				bet = 1;
		}

		if (this.player.sub_credit(bet)) {
			this.last_bet = bet;
			this.last_command = 'b';
			System.out.println("player is betting " + bet.toString());
		} else {
			System.exit(0);
		}
	}
	/**
	 * Imprime os créditos do jogador
	 */
	void credit() {
		System.out.println("player's credits is " + this.player.get_credit());
	}
	/**
	 * No deal é feita a gestão das cartas associadas ao deal de cartas durante o jogo
	 * @return
	 */
	void deal() {
		if (this.last_command != 'b') {
			System.out.println("d: illegal command");
			return;
		}
		
		for (int i=0; i<5; i++) {
			if (deck.get_cards().size()!=0){
				Card card = this.deck.remove_card(0);
				this.player.add_card(card);
			}else{
				System.exit(0);
			}

		}
		System.out.println("player's hand:" + this.player.hand_to_String());
		this.last_command = 'd';
	}

	void hold(String[] command) {
		if (!(this.last_command == 'd' || this.last_command == 'a')) {
			System.out.println("h: illegal command");
			return;
		}

		int index = 0;
		int command_idx = 1;
		for (int i=0; i<5; i++) {
			if (i == index) {
				try{
					if (command_idx < command.length){
						index = Integer.parseInt(String.valueOf(command[command_idx]));		// recebe indice da carta a manter
						command_idx ++;
					} else
						index = 6;
				}
				catch (NumberFormatException ex){
					ex.printStackTrace();
				}
			}
			if (i != index-1) {
				if (deck.get_cards().size()!=0){
					Card card = this.deck.remove_card(0);
					Card removed_card = this.player.replace_card(i, card);
					this.played_cards.add_card(removed_card);
				}else{
					System.exit(0);
				}
			}
		}
		System.out.println("player's hand: " + this.player.hand_to_String());
	}

	void advice() {
		if (this.last_command != 'd') {
			System.out.println("a: illegal command");
			return;
		}
		String text = new String();
		Deck ordered_hand = this.player.get_hand();
		ordered_hand.order_deck();
		ArrayList<Card> keepers = this.rules.get_optimal(ordered_hand);
		if (keepers == null) {
			text = "";
			return;
		}
		int i = 1;
		for (Card hand_card: this.player.get_hand().get_cards()) {
			for (Card keep_card: keepers) {
				if (hand_card.get_rank() == keep_card.get_rank() && hand_card.get_suit() == keep_card.get_suit()) {
					text += " " + String.valueOf(i);
				}
			}
			i++;
		}
		System.out.println("player should hold cards" + text);
		this.advice = text;
	}

	void statistics() {
		String space;
		System.out.println("Hand\t\t\t|\tNb");
		for (Statistic stat: this.statistics) {
			if (stat.get_stat().length() < 7)
				space = ":\t\t\t";
			else if (stat.get_stat().length() < 15)
				space = ":\t\t";
			else
				space = ":\t";
			System.out.println(stat.get_stat() + space + "|\t" + stat.get_value());
		}
		System.out.println("Hand\t\t\t|\t" + this.played);
		float percent_float = ((float)this.player.get_credit() / (float)this.player.get_initial_credit()) * 100;
		int percent_int = (int) percent_float;
		System.out.println("Credit\t\t\t|     " + this.player.get_credit() + " (" + percent_int + "%)");
	}

	void set_statistics() {
		this.statistics.add(new Statistic("Jacks or Better"));
		this.statistics.add(new Statistic("Two Pair"));
		this.statistics.add(new Statistic("Three of a Kind"));
		this.statistics.add(new Statistic("Straight"));
		this.statistics.add(new Statistic("Flush"));
		this.statistics.add(new Statistic("Full House"));
		this.statistics.add(new Statistic("Four of a Kind"));
		this.statistics.add(new Statistic("Straight Flush"));
		this.statistics.add(new Statistic("Royal Flush"));
		this.statistics.add(new Statistic("Other"));
	}

	void update_statistics(String win) {
		String text = new String();
		if (win.equals("JOB")) {
			this.statistics.get(0).add();
			text = this.statistics.get(0).get_stat();
		}else if (win.equals("TP")) {
			this.statistics.get(1).add();
			text =  this.statistics.get(1).get_stat();
		} else if (win.equals("ToaK")) {
			this.statistics.get(2).add();
			text = this.statistics.get(2).get_stat();
		} else if (win.equals("S")) {
			this.statistics.get(3).add();
			text = this.statistics.get(3).get_stat();
		} else if (win.equals("F")) {
			this.statistics.get(4).add();
			text = this.statistics.get(4).get_stat();
		} else if (win.equals("FH")) {
			this.statistics.get(5).add();
			text = this.statistics.get(5).get_stat();
		} else if (win.equals("FoaK")) {
			this.statistics.get(6).add();
			text = this.statistics.get(6).get_stat();
		} else if (win.equals("SF")) {
			this.statistics.get(7).add();
			text = this.statistics.get(7).get_stat();
		} else if (win.equals("RF")) {
			this.statistics.get(8).add();
			text = this.statistics.get(8).get_stat();
		} else if (win.equals("other"))
			this.statistics.get(9).add();

		if (win.equals("other")) {
			System.out.println("player loses and his credit is " + this.player.get_credit());
		} else {
			System.out.println("player wins with a " + text.toUpperCase() + " and his credit is " + this.player.get_credit());
			this.wins++;
		}
	}

	void reset_hand() {
		for (int i=0; i<5; i++) {
			Card card = this.player.remove_card(0);								// removes card from player hand
			this.played_cards.add_card(card);									// adds card to played card list
		}
	}

	void reset_deck() {
		int num = this.played_cards.get_cards().size();
		for (int i=0; i<num; i++) {
			Card card = this.played_cards.remove_card(0);				// removes card from played card list
			this.deck.add_card(card);											// adds card back to deck
		}
		this.deck.shuffle();
	}

	String check_win(Deck hand, int bet) {
		if (this.rules.check_RF(hand)) {
			this.variation.credit_player(this.player, hand, bet, "RF");
			return "RF";
		} else if (this.rules.check_SF(hand)) {
			this.variation.credit_player(this.player, hand, bet, "SF");
			return "SF";
		} else if (this.rules.check_FoaK(hand)) {
			this.variation.credit_player(this.player, hand, bet, "FoaK");
			return "FoaK";
		} else if (this.rules.check_FH(hand)) {
			this.variation.credit_player(this.player, hand, bet, "FH");
			return "FH";
		} else if (this.rules.check_S(hand)) {
			this.variation.credit_player(this.player, hand, bet, "S");
			return "S";
		} else if (this.rules.check_F(hand)) {
			this.variation.credit_player(this.player, hand, bet, "F");
			return "F";
		} else if (this.rules.check_ToaK(hand)) {
			this.variation.credit_player(this.player, hand, bet, "ToaK");
			return "ToaK";
		} else if (this.rules.check_TP(hand)) {
			this.variation.credit_player(this.player, hand, bet, "TP");
			return "TP";
		} else if (this.rules.check_JOB(hand)) {
			this.variation.credit_player(this.player, hand, bet, "JOB");
			return "JOB";
		} else
			return "other";
	}

}
