package me.whizvox.gameshelf.game;

import org.bson.types.ObjectId;

public class GameRelation {

  public ObjectId game;

  public String relation;

  public GameRelation() {
  }

  public GameRelation(ObjectId game, String relation) {
    this.game = game;
    this.relation = relation;
  }

  public static GameRelation parse(String str) {
    int separatorIndex = str.indexOf(',');
    if (separatorIndex == -1) {
      throw new IllegalArgumentException("Invalid game relation string: " + str);
    }
    String gameIdStr = str.substring(0, separatorIndex);
    String relation = str.substring(separatorIndex + 1);
    return new GameRelation(new ObjectId(gameIdStr), relation);
  }

}
