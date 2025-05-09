package tcs.bridge.model;

public class Player extends Hand {
    public static enum Position {
        NORTH,
        EAST,
        SOUTH,
        WEST;

        public Position next(Position current) {
            return Position.values()[(current.ordinal() + 1) % 4];
        }

        public Position teammate(Position current) {
            return Position.values()[(current.ordinal() + 2) % 4];
        }
    }

    private Position position;

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
