package tcs.bridge.model;

public class Player extends Hand {
    public static enum Position {
        NORTH,
        EAST,
        SOUTH,
        WEST;

        public static Position next(Position current) {
            return Position.values()[(current.ordinal() + 1) % 4];
        }

        public static Position teammate(Position current) {
            return Position.values()[(current.ordinal() + 2) % 4];
        }

        public static boolean areOpponents(Position x, Position y) {return (x.ordinal()+ y.ordinal())%2==1;}
        public static boolean areTeammates(Position x, Position y) {return !areOpponents(x, y);}

        public static String abbreviationOf(Position position) {
            if (position==NORTH) return "N";
            if (position==SOUTH) return "S";
            if (position==EAST) return "E";
            if (position==WEST) return "W";
            throw new RuntimeException();
        }
    }

    private Position position;

    public Player(Position position) {
        super();
        this.position = position;
    }
    
    public Player(Position position, Hand hand) {
        super(hand.getCards());
        this.position = position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setHand(Hand hand) {
        this.setCards(hand.getCards());
    }

    public Position getPosition() {
        return position;
    }

}
