package tcs.bridge.model;

public class Player extends Hand {
    public static enum Position {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private Position position;

    public Player(Position position, Hand hand) {
        super(hand.getCards());
        this.position = position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public Position next() {
        return Position.values()[(position.ordinal() + 1) % 4];
    }

    public Position teammate() {
        return Position.values()[(position.ordinal() + 2) % 4];
    }
}
