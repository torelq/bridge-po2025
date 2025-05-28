package tcs.bridge.server;

import tcs.bridge.communication.messages.*;
import tcs.bridge.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class MessageScanner {
    private final Scanner scanner;

    MessageScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    ClientToServerMessage scanMessage() {
        String messageType = scanner.next();
        messageType = messageType.toLowerCase();
        switch (messageType) {
            case "jgr" -> {
                String name = scanner.next();
                Player.Position position = scanPosition(scanner);
                return new JoinGameRequest(name, position);
            }
            case "mbr" -> {
                Player.Position position = scanPosition(scanner);
                Bidding.Bid bid = scanBid(scanner);
                return new MakeBidRequest(position, bid);
            }
            case "pcr" -> {
                Player.Position position = scanPosition(scanner);
                Card card = scanCard(scanner);
                return new PlayCardRequest(position, card);
            }
            case "dis" -> {
                return null;
            }
            case "sm" -> {
                String s = scanner.nextLine();
                return new StringMessage(s);
            }
            case "sr" -> {
                return new StateRequest();
            }
        }
        System.out.println("Invalid command.");
        return scanMessage();
    }

    static Player.Position scanPosition(Scanner scanner) {
        String s = scanner.next();
        s = s.toUpperCase();
        switch (s) {
            case "N" -> {
                return Player.Position.NORTH;
            }
            case "S" -> {
                return Player.Position.SOUTH;
            }
            case "E" -> {
                return Player.Position.EAST;
            }
            case "W" -> {
                return Player.Position.WEST;
            }
        }
        System.out.println("Invalid position.");
        return scanPosition(scanner);
    }

    static Bidding.Bid scanBid(Scanner scanner) {
        String s1 = scanner.next();
        s1 = s1.toLowerCase();
        switch (s1) {
            case "pass", "pas" -> {
                return Bidding.Bid.PASS_BID;
            }
            case "dbl", "ktr" -> {
                return Bidding.Bid.DOUBLE_BID;
            }
            case "rktr", "rdbl" -> {
                return Bidding.Bid.REDOUBLE_BID;
            }
        }

        int level;
        try {
            level = Integer.parseInt(s1);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("Invalid bid.");
            return scanBid(scanner);
        }

        if (level<1 || level>7) {
            System.out.println("Invalid bid.");
            return scanBid(scanner);
        }

        Suit suit = scanSuit(scanner);
        return new Bidding.Bid(level, suit);
    }

    static Suit scanSuit(Scanner scanner) {
        String s = scanner.next().toLowerCase();
        switch (s) {
            case "nt" -> {
                return null;
            }
            case "s" -> {
                return Suit.SPADES;
            }
            case "h" -> {
                return Suit.HEARTS;
            }
            case "d" -> {
                return Suit.DIAMONDS;
            }
            case "c" -> {
                return Suit.CLUBS;
            }
        }
        System.out.println("Invalid suit.");
        return scanSuit(scanner);
    }

    static Rank scanRank(Scanner scanner) {
        String s = scanner.next();
        s = s.toUpperCase();
        for (Rank rank : Rank.values()) {
            if (rank.getName().equals(s)) return rank;
        }
        System.out.println("Invalid rank.");
        return scanRank(scanner);
    }

    static Card scanCard(Scanner scanner) {
        Rank rank = scanRank(scanner);
        Suit suit = scanSuit(scanner);
        return new Card(suit, rank);
    }
}
