package tcs.bridge.model;

import java.io.Serializable;

public class Contract implements Serializable {
    final int level; // 1-7
    final Suit trump; // null - no trump
    final Player.Position declarer;
    final int scoreMultiplier; // 1, 2, or 4

    public Contract(int level, Suit trump, Player.Position declarer) {
        this(level, trump, declarer, 1);
    }

    public Contract(int level, Suit trump, Player.Position declarer, int scoreMultiplier) {
        if (level<1 || level>7) throw new IllegalArgumentException("Contract level needs to be within [1,7].");
        if (scoreMultiplier!=1 && scoreMultiplier!=2 && scoreMultiplier!=4) throw new IllegalArgumentException("Score multiplier must be in {1, 2, 4}.");
        if (declarer==null) throw new IllegalArgumentException("Contract.declarer cannot be null.");

        this.level = level;
        this.trump = trump;
        this.declarer = declarer;
        this.scoreMultiplier = scoreMultiplier;
    }

    @Override
    public String toString() {
        String s = String.valueOf(level)+Suit.abbreviationOf(trump)+Player.Position.abbreviationOf(declarer);
        if (scoreMultiplier==2) s += "x";
        if (scoreMultiplier==4) s += "xx";
        return s;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Contract c)) return false;
        return c.scoreMultiplier==scoreMultiplier && c.trump==trump && c.level==level && c.declarer==declarer;
    }

    public Player.Position getDeclarer() {return declarer;}

    public Player.Position getDummy() {return Player.Position.teammate(declarer);}

    public Player.Position getLeftHandDefender() {return Player.Position.next(declarer);}
    public Player.Position getRightHandDefender() {return Player.Position.teammate(getLeftHandDefender());}
}
